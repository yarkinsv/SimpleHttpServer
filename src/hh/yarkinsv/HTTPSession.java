package hh.yarkinsv;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Map;

public class HTTPSession {
    private Charset charset = Charset.forName("UTF-8");
    private CharsetEncoder encoder = charset.newEncoder();
    private final SocketChannel channel;
    private final ByteBuffer buffer = ByteBuffer.allocate(2048);
    public final StringBuilder readLines = new StringBuilder();
    private int mark = 0;

    public HTTPSession(SocketChannel channel) {
        this.channel = channel;
    }

    public String readLine() throws IOException {

        return null;
    }

    public void readData() throws IOException {
        buffer.limit(buffer.capacity());
        int read = channel.read(buffer);
        if (read == -1) {
            throw new IOException("End of stream");
        }
        buffer.flip();
        buffer.position(mark);
    }

    private void writeLine(String line) throws IOException {
        channel.write(encoder.encode(CharBuffer.wrap(line + "\r\n")));
        System.out.println(line);
    }

    public void sendResponse(HTTPResponse response) {
        try {
            writeLine(response.getVersion() + " " + response.getResponseCode() + " " + response.getResponseReason());
            for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
                writeLine(header.getKey() + ": " + header.getValue());
            }
            writeLine("");
            if (response.getContent() != null) {
                channel.write(ByteBuffer.wrap(response.getContent()));
            }
        } catch (IOException ex) {
            // slow silently
        }
    }

    public void close() {
        try {
            channel.close();
        } catch (IOException ex) {
        }
    }
}
