package hh.yarkinsv.files;

import java.io.IOException;
import java.io.File;
import java.nio.file.*;
import java.util.*;

public class FilesWatcher {

    public class FilesLoader implements Runnable {
        private WatchService watchService;

        public FilesLoader(WatchService watchService) {
            this.watchService = watchService;
        }

        public void run() {
            for (;;) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException x) {
                    return;
                }

                for (WatchEvent<?> event: key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    WatchEvent<Path> ev = (WatchEvent<Path>)event;
                    Path filename = ev.context();
                    System.out.format("Emailing file %s%n", filename);
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        }
    }

    private String root;
    private boolean useCaching;
    private Map<String, FileInfo> cache = Collections.synchronizedMap(new HashMap<>());
    private WatchService watchService;

    private final char FOLDER_DELIMITER = '/';

    public FilesWatcher(String root, boolean useCaching) throws IOException {
        this.root = root;
        this.useCaching = useCaching;
        if (this.useCaching) {
            try {
                watchService = FileSystems.getDefault().newWatchService();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            getFileList("");
            new Thread(new FilesLoader(watchService)).start();
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

    public void refreshCache() throws IOException {
        for (File file : getFileList("")) {
            FileInfo fileInfo = readFileInfoFromCache(file.getAbsolutePath());
        }
    }

    public List<File> getFileList(String path) throws IOException {
        if (this.useCaching) {
            Paths.get(this.root).register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        }
        List<File> result = new ArrayList<File>();
        File[] files = new File(this.root + path).listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                if (this.useCaching) {
                    Paths.get(file.getAbsolutePath()).register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                }
                result.addAll(getFileList(path + FOLDER_DELIMITER + file.getName()));
            } else if (file.isFile()) {
                result.add(file);
            }
        }
        return result;
    }
}
