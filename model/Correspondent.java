package com.model;

import com.server.Session;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Correspondent {
    public final String id; // Изменено на String
    public final String login;

    public Session activeSession; // Активная сессия пользователя

    public void bindSession(Session session) {
        // Если уже существует активная сессия, можно её закрыть или выполнить дополнительную логику
        if (this.activeSession != null && this.activeSession != session) {
        }
        this.activeSession = session;
    }

    public Correspondent(String id, String login) {
        this.id = id;
        this.login = login;
    }

    private static final Map<String, Correspondent> correspondentById = new HashMap<>();
    private static final Map<String, Correspondent> correspondentByLogin = new HashMap<>();

    // Статический блок для инициализации данных
    static {
        registerCorrespondent(new Correspondent("1", "user1"));
        registerCorrespondent(new Correspondent("2", "user2"));
        registerCorrespondent(new Correspondent("3", "user3"));
    }

    public static void registerCorrespondent(Correspondent c) {
        correspondentById.put(c.id, c);
        correspondentByLogin.put(c.login, c);
    }

    public static Correspondent findCorrespondent(String id) {
        return correspondentById.get(id);
    }

    public static Correspondent findCorrespondentByLogin(String login) {
        return correspondentByLogin.get(login);
    }

    public static Collection<Correspondent> listAll() {
        return correspondentById.values();
    }
}