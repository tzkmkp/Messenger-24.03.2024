package com.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

public class ChatWindow {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;

    private int correspondentId;
    private String correspondentLogin;
    private static PrintWriter out;

    public ChatWindow(String correspondentLogin, int correspondentId, PrintWriter out) {
        this.correspondentLogin = correspondentLogin;
        this.correspondentId = correspondentId;
        this.out = out;
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Чат с " + correspondentLogin);
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        inputField = new JTextField();
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        frame.add(inputField, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public void displayMessage(String message) {
        chatArea.append(message + "\n");
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (!text.isEmpty()) {
            com.model.MessagePacket messagePacket = new com.model.MessagePacket();
            messagePacket.correspondentId = correspondentId;
            messagePacket.text = text;

            System.out.println("Отправка сообщения на сервер: " + text + " -> " + correspondentId);

            messagePacket.writePacket(out);
            displayMessage("Вы: " + text);
            inputField.setText("");
        }
    }

    public JFrame getFrame() {
        return frame;
    }
}
