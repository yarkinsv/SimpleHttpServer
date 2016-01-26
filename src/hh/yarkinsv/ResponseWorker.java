package hh.yarkinsv;

import hh.yarkinsv.files.FilesWatcher;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

public class ResponseWorker implements Runnable {
    private BlockingQueue<SelectionKey> queue;
    private String root;

    private HTTPRequest request;
    private Selector selector;
    private SocketChannel socketChannel;

    public ResponseWorker(BlockingQueue<SelectionKey> queue) {
        this.queue = queue;
        this.root = root;
        this.selector = selector;
    }

    public void run() {
        while (true) {
            try {
                SelectionKey key = queue.take();
                this.request = (HTTPRequest) key.attachment();
                this.selector = key.selector();
                this.socketChannel = (SocketChannel) key.channel();
                sendResponse(getResponse(this.request));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendResponse(HTTPResponse response) throws IOException {
        this.socketChannel.register(this.selector, SelectionKey.OP_WRITE, response);
        selector.wakeup();
    }

    private HTTPResponse getResponse(HTTPRequest request) {
        HTTPResponse response = new HTTPResponse();

        if (!request.isValid()) {
            response.setResponseType(HTTPResponse.ResponseType.BadRequest);
        } else if (!request.getMethod().equals("GET")) {
            response.setResponseType(HTTPResponse.ResponseType.MethodNotAllowed);
        }
        else {
            try {
                String encoding = request.getHeader("Accept-Charset");
                response.setContent(FilesWatcher.getFileInfo(request.getLocation()).getFileBody(encoding));
                response.setResponseType(HTTPResponse.ResponseType.OK);
            } catch (IOException ex) {
                response.setResponseType(HTTPResponse.ResponseType.FileNotFound);
            } catch (Exception ex) {
                response.setResponseType(HTTPResponse.ResponseType.InternalServerError);
            }
        }

        response.addHeaders();
        return response;
    }
}
