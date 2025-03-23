package com.client;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.UIManager;


public class MessengerClient {
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;

    private static JFrame loginFrame;
    private static JTextField loginField;
    private static JPasswordField passwordField;

    private static JFrame chatFrame;
    private static JTextArea chatArea;
    private static JTextField messageField;
    private static JList<String> userList;

    private static final Logger logger = Logger.getLogger(MessengerClient.class.getName());

    // Если нужны для работы нескольких сессий/чатов
    static final Map<String, com.server.Session> activeSessions = new ConcurrentHashMap<>();
    static final Map<Integer, com.client.ChatWindow> activeChats = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        loginFrame = new JFrame("Login");
        loginFrame.setSize(400, 200);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLayout(new BorderLayout());

        JPanel loginPanel = new JPanel(new GridLayout(3, 2));

        loginPanel.add(new JLabel("Login:"));
        loginField = new JTextField();
        loginPanel.add(loginField);

        loginPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        loginPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> login());
        loginPanel.add(loginButton);

        loginFrame.add(loginPanel, BorderLayout.CENTER);
        loginFrame.setVisible(true);
    }

    private static void login() {
        String login = loginField.getText();
        String password = new String(passwordField.getPassword());

        new Thread(() -> {
            try {
                logger.info("Попытка подключиться к серверу...");
                socket = new Socket("localhost", 10001);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Добавляем shutdown hook для корректного завершения работы клиента
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        if (socket != null && !socket.isClosed()) {
                            socket.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                        if (in != null) {
                            in.close();
                        }
                        logger.info("Client resources closed.");
                    } catch (IOException e) {
                        logger.severe("Error closing client resources: " + e.getMessage());
                    }
                }));

                logger.info("Отправка логина: " + login);
                out.println(login);
                out.println(password);

                String response = in.readLine();
                logger.info("Ответ от сервера: " + response);

                SwingUtilities.invokeLater(() -> {
                    if ("AUTHORIZED".equals(response)) {
                        loginFrame.dispose();
                        createChatWindow();
                        startMessageListener();
                    } else {
                        JOptionPane.showMessageDialog(loginFrame, "Invalid login or password!");
                    }
                });
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(loginFrame, "Connection failed!");
                });
                logger.severe("Connection failed: " + ex.getMessage());
            }
        }).start();
    }

    private static JPanel chatPanel; // Заменяем JTextArea на JPanel
    private static JScrollPane chatScrollPane;

    private static void createChatWindow() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        chatFrame = new JFrame("Messenger");
        chatFrame.setSize(600, 400);
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        // Заголовок с логином пользователя, которому адресовано письмо
        String recipientLogin = "UserRecipient"; // замените на нужный логин
        final JLabel headerLabel = new JLabel("Письмо для: " + recipientLogin, SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        chatFrame.setContentPane(mainPanel);

        // 📌 **Панель чата (с фоновым изображением)**
        com.client.BackgroundPanel chatContainer = new com.client.BackgroundPanel("C:\\repositories\\src\\com\\background.jpg");
        chatContainer.setLayout(new BorderLayout());

        // Инициализация chatPanel для отображения сообщений в виде пузырьков
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setOpaque(false);

        chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setOpaque(false);
        chatScrollPane.getViewport().setOpaque(false);

        chatContainer.add(chatScrollPane, BorderLayout.CENTER);

        // **Поле ввода сообщений**
        messageField = new JTextField();
        messageField.addActionListener(e -> sendMessage());
        chatContainer.add(messageField, BorderLayout.SOUTH);

        mainPanel.add(chatContainer, BorderLayout.CENTER);

        // 📌 **Панель пользователей**
        JPanel usersPanel = new JPanel(new BorderLayout());
        usersPanel.setPreferredSize(new Dimension(150, 400));
        usersPanel.setBackground(new Color(40, 40, 40));

        DefaultListModel<String> userListModel = new DefaultListModel<>();
        userListModel.addElement("User 1");
        userListModel.addElement("User 2");
        userListModel.addElement("User 3");

        userList = new JList<>(userListModel);
        userList.setOpaque(true);
        userList.setForeground(Color.WHITE);
        userList.setBackground(new Color(50, 50, 50));
        userList.setFont(new Font("Arial", Font.PLAIN, 14));
        userList.setFixedCellHeight(30);

        // Добавляем слушатель для обновления заголовка при выборе нового чата
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = userList.getSelectedValue();
                headerLabel.setText("Письмо для: " + selectedUser);
            }
        });

        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(150, 400));
        userScrollPane.setOpaque(true);
        userScrollPane.getViewport().setOpaque(true);

        usersPanel.add(userScrollPane, BorderLayout.CENTER);
        mainPanel.add(usersPanel, BorderLayout.WEST);

        chatFrame.setVisible(true);
    }

    public static com.server.Session getSessionById(String recipientId) {
        return activeSessions.get(recipientId);
    }

    public static void handleIncomingMessage(int senderId, String message) {
        com.client.ChatWindow chat = activeChats.get(senderId);
        if (chat == null) {
            chat = new com.client.ChatWindow("User " + senderId, senderId, out);
            activeChats.put(senderId, chat);
        }
        chat.displayMessage("User " + senderId + ": " + message);
    }

    private static void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            try {
                com.model.MessagePacket messagePacket = new com.model.MessagePacket();
                int selectedUserId = getSelectedUserId();
                messagePacket.correspondentId = selectedUserId;
                messagePacket.text = message;
                messagePacket.writePacket(out);

                messageField.setText("");

                // Добавляем сообщение в виде "пузырька"
                com.client.MessageBubble bubble = new com.client.MessageBubble("You: " + message, true);
                chatPanel.add(bubble);

                // Небольшой отступ между сообщениями
                chatPanel.add(Box.createVerticalStrut(5));

                chatPanel.revalidate();
                chatPanel.repaint();

                // Автопрокрутка вниз
                SwingUtilities.invokeLater(() ->
                        chatScrollPane.getVerticalScrollBar().setValue(
                                chatScrollPane.getVerticalScrollBar().getMaximum())
                );

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(chatFrame, "Failed to send message!");
                logger.severe("Failed to send message: " + ex.getMessage());
            }
        }
    }

    private static int getSelectedUserId() {
        int selectedIndex = userList.getSelectedIndex();
        return selectedIndex + 1;
    }

    private static void startMessageListener() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                while (true) {
                    com.model.Packet packet = com.model.Packet.readPacket(in);
                    if (packet == null) break;

                    if (packet instanceof com.model.MessagePacket) {
                        com.model.MessagePacket msgPacket = (com.model.MessagePacket) packet;
                        SwingUtilities.invokeLater(() -> {
                            com.client.MessageBubble bubble = new com.client.MessageBubble("Received: " + msgPacket.text, false);
                            // Добавляем пузырёк в панель
                            chatPanel.add(bubble);
                            // Добавляем небольшой отступ между сообщениями
                            chatPanel.add(Box.createVerticalStrut(5));
                            chatPanel.revalidate();
                            chatPanel.repaint();
                        });
                    } else if (packet instanceof com.model.ListPacket) {
                        // Обработка пакета со списком пользователей, например:
                        // updateUserList((ListPacket) packet);
                    } else {
                        logger.warning("Received unknown packet type: " + packet.getType());
                    }
                }
            } catch (Exception ex) {
                logger.severe("Error reading packet: " + ex.getMessage());
            }
        });
    }

    public static void setChatArea(JTextArea chatArea) {
        MessengerClient.chatArea = chatArea;
    }
}
