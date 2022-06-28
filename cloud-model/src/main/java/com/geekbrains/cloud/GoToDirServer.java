package com.geekbrains.cloud;

import lombok.Data;


@Data
public class GoToDirServer implements CloudMessage {
    private final String name;
    private Action actionWithFile;

    public GoToDirServer(String name, Action action) {
        this.name = name;
        actionWithFile = action;
    }
}

;