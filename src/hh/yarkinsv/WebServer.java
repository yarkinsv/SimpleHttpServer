package hh.yarkinsv;

import hh.yarkinsv.files.FilesWatcher;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WebServer implements Runnable {
    private String root;
    private boolean caching = true;
    private boolean isRunning = true;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    private BlockingQueue workingQueue;
    private Charset charset = Charset.forName("UTF-8");
    private CharsetEncoder encoder = charset.newEncoder();

    protected WebServer(InetSocketAddress address) throws IOException {
        workingQueue = new LinkedBlockingQueue<SelectionKey>();
        this.selector = initSelector(address);
    }

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

    public String getRoot(String root) {
        return root;
    }

    public void setCaching(boolean caching) {
        this.caching = caching;
    }

    public boolean getCaching() {
        return caching;
    }

    @Override
    public final void run() {
        isRunning = true;
        FilesWatcher filesWatcher = null;
        try {
            filesWatcher = new FilesWatcher(this.root, this.caching);
        } catch (IOException ex) {

        }

        new Thread(new ResponseWorker(workingQueue, filesWatcher)).start();
        new Thread(new ResponseWorker(workingQueue, filesWatcher)).start();

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
                shutdown();
                throw new RuntimeException(ex);
            }
        }
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

        System.out.println(sb.toString());
        HTTPRequest request = new HTTPRequest(sb.toString());

        key.attach(request);

        this.workingQueue.offer(key);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        HTTPResponse response = (HTTPResponse) key.attachment();
        writeLine(socketChannel, response.getVersion() + " " + response.getResponseCode() + " " + response.getResponseReason());
        for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
            writeLine(socketChannel, header.getKey() + ": " + header.getValue());
        }
        writeLine(socketChannel, "");
        if (response.getContent() != null) {
            socketChannel.write(ByteBuffer.wrap(response.getContent()));
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private void writeLine(SocketChannel channel, String line) throws IOException {
        channel.write(encoder.encode(CharBuffer.wrap(line + "\r\n")));
        System.out.println(line);
    }

    private final void shutdown() {
        isRunning = false;
        try {
            selector.close();
            serverChannel.close();
        } catch (IOException ex) {
        }
    }
}