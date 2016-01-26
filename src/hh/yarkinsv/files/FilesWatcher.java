package hh.yarkinsv.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FilesWatcher {
    private static String root;

    public static FileInfo getFileInfo(String fileName) throws IOException {
        byte[] file = Files.readAllBytes(Paths.get(root + fileName));
        FileInfo fileInfo = new FileInfo(fileName, file);
        return fileInfo;
    }

    public static void setRoot(String root) {
        FilesWatcher.root = root;
    }

    public static void refreshCache() {

    }
}
