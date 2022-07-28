package com.geekbrains.cloud;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class FileMessage implements CloudMessage {
    private final long size;
    private final String name;
    private final byte[] data;



    public FileMessage(Path path) throws IOException {
        size = Files.size(path);
        name = path.getFileName().toString();
        data = Files.readAllBytes(path);
    }

}
