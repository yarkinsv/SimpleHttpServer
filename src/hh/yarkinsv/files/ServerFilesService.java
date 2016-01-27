package hh.yarkinsv.files;

import java.io.IOException;
import java.io.File;
import java.nio.file.*;
import java.util.*;

public class ServerFilesService {
    private String root;
    private boolean useCaching;
    private Map<String, FileInfo> cache = Collections.synchronizedMap(new HashMap<>());

    private final char FOLDER_DELIMITER = '/';

    public ServerFilesService(String root, boolean useCaching) throws IOException {
        this.root = root;
        this.useCaching = useCaching;
        if (this.useCaching) {
            refreshCache();
        }
    }

    public FileInfo getFileInfo(String fileName) throws IOException {
        String absoluteFileName = root + FOLDER_DELIMITER + fileName;

        if (useCaching) {
            return readFileInfoFromCache(absoluteFileName);
        } else {
            return readFileInfo(absoluteFileName);
        }
    }

    public void refreshCache() {
        new Thread(() -> {
            try {
                for (File file : getFileList("")) {
                    if (cache.containsKey(file.getAbsolutePath())) {
                        cache.remove(file.getAbsolutePath());
                    }
                    readFileInfoFromCache(file.getAbsolutePath());
                }
            } catch (IOException ex) {
            }
        }).start();
    }

    private FileInfo readFileInfo(String fileName) throws IOException {
        byte[] file = Files.readAllBytes(Paths.get(fileName));
        FileInfo fileInfo = new FileInfo(fileName, file);
        return fileInfo;
    }

    private FileInfo readFileInfoFromCache(String fileName) throws IOException {
        if (!cache.containsKey(fileName)) {
            cache.put(fileName, readFileInfo(fileName));
        }
        return cache.get(fileName);
    }

    private List<File> getFileList(String path) throws IOException {
        List<File> result = new ArrayList<File>();
        File[] files = new File(this.root + path).listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                result.addAll(getFileList(path + FOLDER_DELIMITER + file.getName()));
            } else if (file.isFile()) {
                result.add(file);
            }
        }
        return result;
    }
}
