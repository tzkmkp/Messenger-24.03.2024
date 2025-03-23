package com.utils;

public class Event {
    public com.server.Session session;
    public com.model.Packet packet;

    public Event(com.server.Session session, com.model.Packet packet) {
        this.session = session;
        this.packet = packet;
    }
}