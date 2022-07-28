package com.geekbrains.cloud;

import lombok.Data;

@Data
public class RenameFile implements CloudMessage {
    public final String source;
    public final String destination;
}
