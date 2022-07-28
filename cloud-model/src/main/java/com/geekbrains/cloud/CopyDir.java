package com.geekbrains.cloud;

import lombok.Data;

@Data
public class CopyDir implements CloudMessage {
    private final String name;
}