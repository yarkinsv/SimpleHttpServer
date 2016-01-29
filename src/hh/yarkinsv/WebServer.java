package hh.yarkinsv;

import hh.yarkinsv.files.ServerFilesService;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WebServer implements Runnable {
    private String root;
    private boolean caching = true;
    private boolean isRunning = true;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    private BlockingQueue workingQueue = new LinkedBlockingQueue<SelectionKey>();;
    private Charset charset = Charset.forName("UTF-8");
    private CharsetEncoder encoder = charset.newEncoder();
    private ServerFilesService serverFilesService;
    private InetSocketAddress address;

    private List<LogEventListener> logEventListeners = new ArrayList<>();

    private Selector initSelector(InetSocketAddress address) throws IOException {
        Selector socketSelector = SelectorProvider.provider().openSelector();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(address);
        serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
        return socketSelector;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getRoot() {
        return this.root;
    }

    public void setCaching(boolean caching) {
        this.caching = caching;
    }

    public boolean getCaching() {
        return caching;
    }

    public int getPort() {
        return address.getPort();
    }

    public void setPort(int port) {
        this.address = new InetSocketAddress(port);
    }

    @Override
    public final void run() {
        isRunning = true;

        try {
            this.selector = initSelector(address);
            serverFilesService = new ServerFilesService(this.root, this.caching);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        new Thread(new ResponseWorker(workingQueue, serverFilesService)).start();
        new Thread(new ResponseWorker(workingQueue, serverFilesService)).start();

        new Thread(() ->
        {
            while (isRunning) {
                try {
                    selector.select();

                    Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                    while (selectedKeys.hasNext()) {
                        SelectionKey key = selectedKeys.next();
                        selectedKeys.remove();

                        if (!key.isValid()) {
                            continue;
                        } else if (key.isAcceptable()) {
                            this.accept(key);
                        } else if (key.isReadable()) {
                            this.read(key);
                        } else if (key.isWritable()) {
                            this.write(key);
                        }
                    }
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                } catch (CancelledKeyException ex) {
                    System.out.println(ex.getMessage());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }).start();
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(this.selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        this.readBuffer.clear();
        int numRead;
        try {
            numRead = socketChannel.read(this.readBuffer);
        } catch (IOException ex) {
            key.cancel();
            socketChannel.close();
            return;
        }

        if (numRead == -1) {
            key.cancel();
            socketChannel.close();
            return;
        }

        this.readBuffer.flip();
        StringBuilder sb = new StringBuilder();
        while (this.readBuffer.hasRemaining()) {
            char c = (char) this.readBuffer.get();
            sb.append(c);
        }

        logAdded(sb.toString());
        HTTPRequest request = new HTTPRequest(sb.toString());

        key.attach(request);

        this.workingQueue.offer(key);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        HTTPResponse response = (HTTPResponse) key.attachment();
        try {
            writeLine(socketChannel, response.getVersion() + " " + response.getResponseCode() + " " + response.getResponseReason());
            for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
                writeLine(socketChannel, header.getKey() + ": " + header.getValue());
            }
            writeLine(socketChannel, "");
            if (response.getContent() != null) {
                ByteBuffer buffer = ByteBuffer.wrap(response.getContent());
                while (buffer.hasRemaining()) {
                    socketChannel.write(buffer);
                }
            }
            key.interestOps(SelectionKey.OP_READ);
        } catch (Exception ex) {
            key.cancel();
        }
    }

    private void writeLine(SocketChannel channel, String line) throws IOException {
        channel.write(encoder.encode(CharBuffer.wrap(line + "\r\n")));
        logAdded(line);
    }

    public final void updateCache() {
        if (!this.caching || serverFilesService == null) {
            return;
        }
        serverFilesService.refreshCache();
    }

    public int getFilesInCache() {
        if (serverFilesService == null) {
            return 0;
        }
        return serverFilesService.getFilesInCache();
    }

    public long getSizeOfCache() {
        if (serverFilesService == null) {
            return 0;
        }
        return serverFilesService.getSizeOfCache();
    }

    public final void stop() {
        isRunning = false;
        try {
            selector.close();
            serverChannel.close();
        } catch (IOException ex) {}
    }

    public void addLogListener(LogEventListener listener) {
        logEventListeners.add(listener);
    }

    private void logAdded(String log) {
        for (LogEventListener listener : logEventListeners) {
            listener.logEventAdded(log);
        }
    }
}

