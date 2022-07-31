package com.geekbrains.cloud;

import lombok.Data;

@Data
public class ResponseRegistr implements CloudMessage{
    private boolean registr;

    public boolean isRegistr() {
        return registr;
    }

    public ResponseRegistr(boolean registr) {
        this.registr = registr;
    }
}
