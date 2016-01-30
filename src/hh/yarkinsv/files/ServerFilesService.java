package hh.yarkinsv.files;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.File;
import java.nio.file.*;
import java.util.*;

public class ServerFilesService {
    private String root;
    private boolean useCaching;
    private Map<String, FileInfo> cache = Collections.synchronizedMap(new HashMap<>());
    private ActionListener cacheRefreshedListener;

    public ServerFilesService(String root, boolean useCaching) throws IOException {
        this.root = root;
        this.useCaching = useCaching;
        if (this.useCaching) {
            refreshCache();
        }
    }

    public FileInfo getFileInfo(String fileName) throws IOException {
        String absoluteFileName = root + FileSystems.getDefault().getSeparator() + fileName;

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
                if (cacheRefreshedListener != null) {
                    cacheRefreshedListener.actionPerformed(new ActionEvent(this, 1, "Cache refreshed"));
                }
            } catch (IOException ex) {
            }
        }).start();
    }

    public int getFilesInCache() {
        if (!useCaching) {
            return 0;
        }
        return cache.size();
    }

    public int getSizeOfCache() {
        if (!useCaching) {
            return 0;
        }
        int result = 0;
        for (FileInfo file : cache.values()) {
            result += file.getBodySize();
        }
        return result;
    }

    public void addCacheRefreshedListener(ActionListener listener) {
        cacheRefreshedListener = listener;
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
        if (files == null) {
            return result;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                result.addAll(getFileList(path + FileSystems.getDefault().getSeparator() + file.getName()));
            } else if (file.isFile() && ContentType.getContentTypeByExtension(file.getName().substring(file.getName().lastIndexOf(".") + 1)) != null) {
                result.add(file);
            }
        }
        return result;
    }
}
