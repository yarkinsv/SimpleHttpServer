package hh.yarkinsv;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class HTTPRequest {
    private final String raw;
    private String method;
    private String location;
    private String version;
    private Map<String, String> headers = new HashMap<String, String>();
    private SocketChannel socketChannel;
    private boolean isValid = true;

    public HTTPRequest(String raw, SocketChannel socketChannel) {
        this.raw = raw;
        this.socketChannel = socketChannel;
        try {
            String[] lines = raw.split("\r\n");

            StringTokenizer tokenizer = new StringTokenizer(lines[0]);
            this.method = tokenizer.nextToken().toUpperCase();
            this.location = tokenizer.nextToken();
            this.version = tokenizer.nextToken();

            for (int i = 1; i < lines.length; i++) {
                String[] keyVal = lines[i].split(":", 2);
                this.headers.put(keyVal[0], keyVal[1]);
            }
        } catch (Exception ex) {
            this.isValid = false;
        }
    }

    public String getMethod() {
        return method;
    }

    public String getLocation() {
        return location;
    }

    public String getHead(String key) {
        return headers.get(key);
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public boolean isValid() {
        return isValid;
    }
}
