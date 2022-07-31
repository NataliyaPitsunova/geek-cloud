package com.geekbrains.cloud;

public class SignIn implements CloudMessage {
    private final String login;
    private final String password;

    public Action getActionWithFile() {
        return actionWithFile;
    }

    private Action actionWithFile;


    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public SignIn(String login, String password, Action action) {
        this.login = login;
        this.password = password;
        actionWithFile = action;
    }


}
