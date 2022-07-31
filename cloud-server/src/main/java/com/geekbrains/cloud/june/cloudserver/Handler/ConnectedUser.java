package com.geekbrains.cloud.june.cloudserver.Handler;

import java.util.ArrayList;

public class ConnectedUser {
    private ArrayList<String> loginUsersOnline;

    public ConnectedUser() {
        this.loginUsersOnline = new ArrayList<>();
    }

    public void addUser(String login) {
        this.loginUsersOnline.add(login);
    }

    public void removeUser(String login) {
        this.loginUsersOnline.remove(login);
    }

    public void getUsers() {
        for (String user : loginUsersOnline.stream().toList()
        ) {
            System.out.println(user);
        }
    }

    public boolean findUserOnline(String login) {
        for (String user : loginUsersOnline.stream().toList()
        ) {
            if (user.equals(login)) {
                return true;
            }
        }
        return false;
    }
}
