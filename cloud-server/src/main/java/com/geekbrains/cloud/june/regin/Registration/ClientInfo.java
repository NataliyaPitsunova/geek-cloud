package com.geekbrains.cloud.june.regin.Registration;

public class ClientInfo {

    private final String login;
    private final String password;


    public ClientInfo(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }


}
