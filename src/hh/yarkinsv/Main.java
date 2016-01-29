package hh.yarkinsv;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception {
        /* Чтение файла конфигурации */
        InputStream is = new FileInputStream(new File("./resources/server.properties"));
        Properties properties = new Properties();
        properties.load(is);

        /* Настройка и запуск веб-сервера */
        WebServer server = new WebServer();
        server.setPort(Integer.parseInt((String)properties.get("port")));
        server.setRoot((String)properties.get("root"));
        server.setCaching(Boolean.parseBoolean((String)properties.get("useCaching")));

        //WebServerGUI.main(null);
        WebServerGUI gui = new WebServerGUI(server);
    }
}
