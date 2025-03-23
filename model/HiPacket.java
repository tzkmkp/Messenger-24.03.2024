package com.model;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class HiPacket extends com.model.Packet {
    public static final String type = "HI";  // Тип пакета
    public String userId;  // ID пользователя
    public String message; // Сообщение

    // Конструктор по умолчанию (необходим для десериализации)
    public HiPacket() {
    }

    // Конструктор с параметрами
    public HiPacket(String userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void writeBody(PrintWriter writer) throws Exception {
        writer.println(userId);    // Записываем userId
        writer.println(message);   // Записываем сообщение
    }

    @Override
    public void readBody(BufferedReader reader) throws Exception {
        userId = reader.readLine();   // Читаем userId
        message = reader.readLine();  // Читаем сообщение
    }
}