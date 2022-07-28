package com.geekbrains.cloud;

import lombok.Data;


@Data
public class GoToDirServer implements CloudMessage {
    private final String name;
    private final String nameUser;



    private Action actionWithFile;


    public GoToDirServer(String name, String nameUser, Action action) {
        this.name = name;
        this.nameUser = nameUser;
        actionWithFile = action;
    }

    public Action getActionWithFile() {
        return actionWithFile;
    }
}

