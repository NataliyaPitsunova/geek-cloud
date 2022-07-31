package com.geekbrains.cloud;

import lombok.Data;

@Data
public class ResponseServer implements CloudMessage {
    private String nameUser;


    public ResponseServer(String nameUser) {
        this.nameUser = nameUser;
    }



}
