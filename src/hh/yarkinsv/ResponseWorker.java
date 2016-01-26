package hh.yarkinsv;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class ResponseWorker implements Runnable {
    private final BlockingQueue<HTTPRequest> queue;
    private final Selector selector;
    private String root;

    private HTTPRequest request;

    public ResponseWorker(BlockingQueue<HTTPRequest> queue, String root, Selector selector) {
        this.queue = queue;
        this.root = root;
        this.selector = selector;
    }

    public void run() {
        while (true) {
            try {
                this.request = queue.take();
                sendResponse(getResponse(this.request));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendResponse(HTTPResponse response) throws IOException {
        response.getSocketChannel().register(selector, SelectionKey.OP_WRITE, response);
        selector.wakeup();
    }

    private HTTPResponse getResponse(HTTPRequest request) {
        HTTPResponse response = new HTTPResponse(request.getSocketChannel());

        try {
            response.setContent(Files.readAllBytes(Paths.get(root + request.getLocation())));
        } catch (IOException ex) {
            response.setResponseType(HTTPResponse.ResponseType.FileNotFound);
        }

        response.addDefaultHeaders();
        return response;
    }
}
