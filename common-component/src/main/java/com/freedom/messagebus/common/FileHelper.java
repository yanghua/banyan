package com.freedom.messagebus.common;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHelper {

    public static boolean checkFileExists(@NotNull Path filePath) {
        return Files.exists(filePath);
    }

    public static boolean fileExists(@NotNull String pathStr) {
        Path filePath = Paths.get(pathStr);
        return Files.exists(filePath);
    }

}
