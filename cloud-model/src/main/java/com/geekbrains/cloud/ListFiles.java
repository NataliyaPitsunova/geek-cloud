package com.geekbrains.cloud;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ListFiles implements CloudMessage {
    private List<FileInfo> files;


    public ListFiles(Path path) throws IOException {
        FileInfo next = new FileInfo(path);
        next.setFileName("[ ... ]");

        files = Files.list(path)
                .map(FileInfo::new)
                .collect(Collectors.toList());

        if (!(path.endsWith("server_files"))) {
            files.add(0, next);
        }

    }
}


