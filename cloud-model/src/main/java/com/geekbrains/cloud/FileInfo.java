package com.geekbrains.cloud;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static java.nio.file.Files.*;

public class FileInfo implements CloudMessage {
    private String fileName;
    private FileType type;
    private Long size;
    private LocalDateTime lastModified;


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public enum FileType {
        FILE("F"), DIRECTORY("D");

        private String name;

        public String getName() {
            return name;
        }

        FileType(String name) {
            this.name = name;
        }
    }


    public FileInfo(Path path) {
        try {
            this.fileName = path.getFileName().toString();
            this.type = isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
            if (this.type == FileType.DIRECTORY) {
                this.size = -1L;
            } else {
                this.size = size(path);
            }
            this.lastModified = LocalDateTime.ofInstant(getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(0));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
