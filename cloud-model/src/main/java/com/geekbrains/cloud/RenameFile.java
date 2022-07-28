package com.geekbrains.cloud;

import lombok.Data;

@Data
public class RenameFile implements CloudMessage {
    private final String source;
    private final String destination;
}
