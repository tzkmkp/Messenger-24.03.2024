package com.server;

import com.model.MessagePacket;
import com.model.ListPacket;
import com.model.Packet;
import com.model.Correspondent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Session extends Thread {
    private final Socket socket;
    public Correspondent correspondent;
    private BufferedReader reader;
    private PrintWriter writer;
    private String userId; // ID текущего пользователя

    // Статическое поле для хранения всех активных сессий
    private static final Map<String, Session> sessions = new HashMap<>();

    // Статическое поле для хранения учетных данных пользователей
    private static final Map<String, String> userCredentials = Map.of(
            "1", "1",
            "2", "2",
            "3", "3"
    );

    private static final Logger logger = Logger.getLogger(Session.class.getName());

    public Session(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            this.reader = reader;
            this.writer = writer;

            // Чтение логина и пароля
            String login = reader.readLine();
            String password = reader.readLine();

            logger.info("Received login: " + login + ", password: " + password);

            // Проверка учетных данных
            if (userCredentials.containsKey(login) && userCredentials.get(login).equals(password)) {
                writer.println("AUTHORIZED");
                logger.info("User authenticated: " + login);

                // Инициализация correspondent через уже зарегистрированного пользователя
                this.correspondent = Correspondent.findCorrespondentByLogin(login);
                if (this.correspondent != null) {
                    this.correspondent.bindSession(this);
                } else {
                    logger.warning("Correspondent not found for login: " + login);
                }

                // Сохраняем ID пользователя
                this.userId = login;

                // Добавляем сессию в Map
                sessions.put(login, this);

                // Отправляем список пользователей
                ListPacket listPacket = new ListPacket();
                for (Correspondent c : Correspondent.listAll()) {
                    listPacket.addItem(c.id, c.login);
                }
                this.send(listPacket);

                // Обработка входящих сообщений
                while (true) {
                    Packet packet = Packet.readPacket(reader); // Чтение пакета
                    if (packet == null) break;

                    if (packet instanceof MessagePacket) {
                        MessagePacket msgPacket = (MessagePacket) packet;
                        String recipientId = String.valueOf(msgPacket.correspondentId);
                        Session recipientSession = getSessionById(recipientId);
                        if (recipientSession != null && !recipientSession.socket.isClosed()) {
                            recipientSession.send(msgPacket);
                            logger.info("Отправляем сообщение пользователю " + recipientId + ": " + msgPacket.text);
                        } else {
                            logger.info("User " + recipientId + " not found or offline");
                        }
                    }
                }
            } else {
                writer.println("INVALID");
                logger.info("Invalid login or password for: " + login);
            }
        } catch (IOException ex) {
            logger.severe("Error in session for user " + userId + ": " + ex.getMessage());
        } finally {
            close();
        }
    }

    // Метод для отправки пакетов
    public void send(Packet p) {
        if (p instanceof MessagePacket) {
            sendMessagePacket((MessagePacket) p);
        } else if (p instanceof ListPacket) {
            sendListPacket((ListPacket) p);
        } else {
            logger.warning("Unexpected packet type: " + p.getType());
        }
    }

    // Метод для отправки MessagePacket
    public void sendMessagePacket(MessagePacket messagePacket) {
        if (correspondent != null) {
            logger.info("Sending MessagePacket to user " + correspondent.login + ": " + messagePacket.text);
        } else {
            logger.warning("Correspondent is null for user " + userId);
        }
        try {
            messagePacket.writePacket(writer);
            writer.flush(); // Убедимся, что данные отправлены
        } catch (Exception e) {
            logger.severe("Error sending message to user " + userId + ": " + e.getMessage());
        }
    }

    // Метод для отправки ListPacket
    private void sendListPacket(ListPacket listPacket) {
        logger.info("Sending ListPacket with " + listPacket.getItems().size() + " items to user " + userId);
        try {
            listPacket.writePacket(writer);
            writer.flush(); // Убедимся, что данные отправлены
        } catch (Exception e) {
            logger.severe("Error sending ListPacket to user " + userId + ": " + e.getMessage());
        }
    }

    // Метод для получения сессии по ID пользователя
    public static Session getSessionById(String userId) {
        return sessions.get(userId);
    }

    // Метод для закрытия ресурсов
    public void close() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
            sessions.remove(userId); // Удаляем сессию из Map при закрытии
            logger.info("Session closed for user " + userId);
        } catch (IOException e) {
            logger.severe("Error closing resources for user " + userId + ": " + e.getMessage());
        }
    }
}
