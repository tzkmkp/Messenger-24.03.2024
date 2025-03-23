package com.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class MessagePacket extends Packet {
    public int correspondentId;
    public String text;
    public static final String type = "MESSAGE";

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void writeBody(PrintWriter writer) throws Exception {
        writer.println(correspondentId);
        writer.println(text != null ? text : "");
    }

    @Override
    public void readBody(BufferedReader reader) throws Exception {
        String idLine = reader.readLine();
        if (idLine == null) {
            throw new IOException("Unexpected end of stream when reading correspondentId");
        }
        try {
            correspondentId = Integer.parseInt(idLine.trim());
        } catch (NumberFormatException e) {
            throw new IOException("Invalid correspondentId: " + idLine, e);
        }

        text = reader.readLine();
        if (text == null) {
            throw new IOException("Unexpected end of stream when reading text");
        }
    }
}
