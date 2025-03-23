package com.client;

import javax.swing.*;
import java.awt.*;

public class MessageBubble extends JPanel {

    public MessageBubble(String message, boolean isSender) {
        // Вместо FlowLayout используем BoxLayout, чтобы пузырёк
        // не растягивался на всю ширину родителя
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        // Заменяем переносы строк на <br>
        String textWithBr = message.replaceAll("(\r\n|\n|\r)", "<br>");
        // HTML для автопереноса (width='250') + вставка <br> для \n
        String textHtml = "<html><body width='250'>" + textWithBr + "</body></html>";

        JLabel messageLabel = new JLabel(textHtml);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setBorder(null);
        messageLabel.setForeground(isSender ? Color.WHITE : Color.BLACK);

        // Панель-пузырёк с закруглёнными углами
        JPanel bubblePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Фон пузырька
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Граница пузырька
                g2.setColor(isSender ? new Color(0, 102, 204) : new Color(200, 200, 200));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                // Дополнительно ограничиваем максимальную ширину пузырька
                Dimension d = super.getPreferredSize();
                int maxWidth = 300; // Можно менять при желании
                if (d.width > maxWidth) {
                    d.width = maxWidth;
                }
                return d;
            }
        };
        bubblePanel.setOpaque(false);
        bubblePanel.setBackground(isSender ? new Color(0, 122, 255) : new Color(230, 230, 230));
        bubblePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        bubblePanel.add(messageLabel, BorderLayout.CENTER);

        // Выравнивание пузырька слева/справа
        bubblePanel.setAlignmentX(isSender ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        // Добавляем bubblePanel в текущую панель
        add(bubblePanel);

        // Выравниваем сам контейнер (MessageBubble)
        setAlignmentX(isSender ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
    }
}
