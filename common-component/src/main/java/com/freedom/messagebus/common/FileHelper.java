package com.freedom.messagebus.common;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHelper {

    public static boolean checkFileExists(Path filePath) {
        return Files.exists(filePath);
    }

    public static boolean fileExists(String pathStr) {
        Path filePath = Paths.get(pathStr);
        return Files.exists(filePath);
    }

}
