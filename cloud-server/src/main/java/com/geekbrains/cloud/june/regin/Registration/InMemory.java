package com.geekbrains.cloud.june.regin.Registration;

import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class InMemory implements AuthService {
    public final Map<String, ClientInfo> users;
    public boolean autorization;

    public InMemory() {
        users = new HashMap<>();
        JdbcApp.main();
        for (int i = 0; i < JdbcApp.logins.size(); i++) {
            users.put("log" + (i + 1), JdbcApp.logins.get(i));
        }
    }

    @Override
    public void start() {
        log.debug("Сервис аутентификации инициализирован");
    }

    @Override
    public synchronized String checkLoginAndPassword(String login, String password) {
        for (ClientInfo user : users.values()
        ) {
            if (user.getLogin().equals(login)) {
                if (user.getPassword().equals(password)) {
                    return login;
                }
            }
        }
        return "No Auth";
    }

    @Override
    public synchronized boolean registrationLogin(String login, String password) throws SQLException {
        for (ClientInfo user : users.values()
        ) {
            autorization = user.getLogin().equals(login);
            if (autorization) {
                return false;
            }

        }
        JdbcApp.insertEx(login, password);
        JdbcApp.disconnect();
        return true;

    }

    @Override
    public void end() {
        log.debug("Сервис аутентификации отключен");
    }
}


