package com.geekbrains.cloud.june.regin.Registration;

import java.sql.SQLException;

public interface AuthService {

    void start();

    String checkLoginAndPassword(String login, String password);

    boolean registrationLogin(String login, String password) throws SQLException;


    void end();
}
