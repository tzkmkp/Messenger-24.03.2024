package com.model;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Map;
import java.util.function.Supplier;

public abstract class Packet {
    private static Map<String, Supplier<Packet>> typeMap = Map.of(
            com.model.HiPacket.type, com.model.HiPacket::new,
            com.model.MessagePacket.type, com.model.MessagePacket::new,
            com.model.ListPacket.type, com.model.ListPacket::new
    );

    public abstract String getType();
    public abstract void writeBody(PrintWriter writer) throws Exception;
    public abstract void readBody(BufferedReader reader) throws Exception;

    public void writePacket(PrintWriter writer) {
        try {
            writer.println(getType());
            writeBody(writer);
        } catch(Exception x) {
            throw new RuntimeException(x);
        }
    }

    public static Packet readPacket(BufferedReader reader) {
        try {
            String type = reader.readLine();
            if(type == null) type = "";
            Supplier<Packet> supplier = typeMap.get(type);
            if(supplier == null) {
                System.out.println("Unrecognized message type: " + type);
                return null;
            }
            Packet packet = supplier.get();
            packet.readBody(reader);
            return packet;
        } catch(Exception x) {
            throw new RuntimeException(x);
        }
    }

    // Добавлен новый метод readText для чтения многострочного текста из BufferedReader
    protected String readText(BufferedReader reader) throws Exception {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(line);
        }
        return sb.toString();
    }
}