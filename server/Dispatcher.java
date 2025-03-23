package com.server;

import java.util.concurrent.LinkedBlockingQueue;

public class Dispatcher implements Runnable {
    private static final LinkedBlockingQueue<com.utils.Event> packetQueue = new LinkedBlockingQueue<>();

    public static void event(com.utils.Event e) {
        packetQueue.add(e);
        System.out.println("Event added to queue: " + e.packet.getType());
    }

    @Override
    public void run() {
        for (;;) {
            try {
                var e = packetQueue.take();
                System.out.println("Processing event from session: " + e.session);
                processPacket(e.session, e.packet);
            } catch (InterruptedException x) {
                System.out.println("Dispatcher interrupted: " + x.getMessage());
                break;
            }
        }
    }

    private void processPacket(com.server.Session session, com.model.Packet p) {
        System.out.println("Processing packet: " + p.getType());
        try {
            if (p instanceof com.model.HiPacket) {
                com.model.HiPacket hiP = (com.model.HiPacket) p;
                System.out.println("Received HiPacket from user: " + hiP.userId);
                // Логика для HiPacket
                session.send(new com.model.HiPacket("server", "Hello!"));

            } else if (p instanceof com.model.MessagePacket) {
                com.model.MessagePacket mP = (com.model.MessagePacket) p;
                if (session.correspondent == null) {
                    System.out.println("Non-authorized");
                    return;
                }

                com.model.Correspondent correspondent = com.model.Correspondent.findCorrespondent(String.valueOf(mP.correspondentId));

                if (correspondent != null && correspondent.activeSession != null) {
                    System.out.println("Sending message to correspondent, id: " + correspondent.id);
                    correspondent.activeSession.sendMessagePacket(mP);
                } else {
                    System.out.println("Target correspondent not connected, id: " + mP.correspondentId);
                }

            } else if (p instanceof com.model.ListPacket) {
                com.model.ListPacket listPacket = (com.model.ListPacket) p;
                com.model.ListPacket filledListP = new com.model.ListPacket();
                for (com.model.Correspondent c : com.model.Correspondent.listAll()) {
                    filledListP.addItem(c.id, c.login);  // Добавляем всех пользователей в список
                }
                session.send(filledListP);  // Отправляем список пользователей

            } else {
                System.out.println("Unexpected packet type: " + p.getType());
            }
        } catch (Exception ex) {
            System.out.println("Dispatcher problem while processing packet type " + p.getType() + ": " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}