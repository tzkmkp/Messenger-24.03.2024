package com.model;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ListPacket extends com.model.Packet {
    public static final String type = "LIST";

    public static class CorrespondentItem {
        public int id;
        public String login;

        public CorrespondentItem(int id, String login) {
            this.id = id;
            this.login = login;
        }
    }

    public ArrayList<CorrespondentItem> items = new ArrayList<>();

    public void addItem(String id, String login) {
        int parsedId = Integer.parseInt(id); // Преобразуем String в int
        var item = new CorrespondentItem(parsedId, login);
        items.add(item);
    }

    public void addItem(int id, String login) {
        var item = new CorrespondentItem(id, login);
        items.add(item);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void writeBody(PrintWriter writer) throws Exception {
        System.out.println("Writing ListPacket with " + items.size() + " items");
        for (var ci : items) {
            writer.println(ci.id);    // Записываем id
            writer.println(ci.login); // Записываем login
            System.out.println("Wrote item: " + ci.id + ":" + ci.login);
        }
        writer.println(); // Пустая строка для обозначения конца пакета
    }

    @Override
    public void readBody(BufferedReader reader) throws Exception {
        System.out.println("Reading ListPacket");
        String firstLine;
        while ((firstLine = reader.readLine()) != null && !firstLine.isEmpty()) {
            String secondLine = reader.readLine();
            if (secondLine == null) {
                throw new Exception("Unexpected end of stream");
            }
            addItem(Integer.parseInt(firstLine.trim()), secondLine);
            System.out.println("Read item: " + firstLine + ":" + secondLine);
        }
    }

    public ArrayList<CorrespondentItem> getItems() {
        return items;
    }
}
