package hh.yarkinsv;

import hh.yarkinsv.files.ContentType;
import hh.yarkinsv.files.FileInfo;
import hh.yarkinsv.files.ServerFilesService;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

public class ResponseWorker implements Runnable {
    private BlockingQueue<SelectionKey> queue;
    private ServerFilesService serverFilesService;
    private HTTPRequest request;
    private Selector selector;
    private SocketChannel socketChannel;

    public ResponseWorker(BlockingQueue<SelectionKey> queue, ServerFilesService serverFilesService) {
        this.queue = queue;
        this.serverFilesService = serverFilesService;
    }

    public void run() {
        boolean isRunning = true;
        while (isRunning) {
            try {
                SelectionKey key = queue.take();
                this.request = (HTTPRequest) key.attachment();
                this.selector = key.selector();
                this.socketChannel = (SocketChannel) key.channel();
                sendResponse(getResponse(this.request));
            } catch (InterruptedException ex) {
                isRunning = false;
            } catch (IOException ex) {

            }
        }
    }

    private void sendResponse(HTTPResponse response) throws IOException {
        this.socketChannel.register(this.selector, SelectionKey.OP_WRITE, response);
        selector.wakeup();
    }

    private HTTPResponse getResponse(HTTPRequest request) {
        HTTPResponse response = new HTTPResponse();
        response.addDefaultHeaders();

        if (!request.isValid()) {
            response.setResponseType(HTTPResponse.ResponseType.BadRequest);
        } else if (!request.getMethod().equals("GET")) {
            response.setResponseType(HTTPResponse.ResponseType.MethodNotAllowed);
        } else {
            try {
                String encoding = request.getHeader("Accept-Charset");
                for (String charset : encoding.split(",")) {
                    charset = charset.trim();
                    if (charset.equals("UTF-8") || charset.equals("US-ASCII")) {
                        encoding = charset;
                    }
                }
                FileInfo fileInfo = serverFilesService.getFileInfo(request.getLocation());

                if (fileInfo == null || fileInfo.getContentType() == null) {
                    throw new IOException("File Not Found");
                }

                String etag = request.getHeader("If-None-Match");
                if (etag != null && !etag.isEmpty() && fileInfo.getEtag().equals(etag)) {
                    response.setResponseType(HTTPResponse.ResponseType.NotModified);
                } else {
                    response.setContent(fileInfo.getFileBody(encoding));
                    if (!fileInfo.getEtag().isEmpty()) {
                        response.setHeader("Etag", fileInfo.getEtag());
                    }
                    String contentType = fileInfo.getContentType().getName();
                    if (fileInfo.getContentType() == ContentType.Text) {
                        if (encoding.equals("UTF-8") || encoding.equals("US-ASCII")) {
                            contentType += "; " + encoding;
                        }
                    }
                    response.setHeader("Content-type", contentType);
                    response.setResponseType(HTTPResponse.ResponseType.OK);
                }
            } catch (IOException ex) {
                response.setResponseType(HTTPResponse.ResponseType.FileNotFound);
            } catch (Exception ex) {
                response.setResponseType(HTTPResponse.ResponseType.InternalServerError);
                ex.printStackTrace();
            }
        }

        response.setHeader("Content-Length", Integer.toString(response.getContent().length));

        return response;
    }
}
