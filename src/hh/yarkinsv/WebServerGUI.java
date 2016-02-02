package hh.yarkinsv;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;

public class WebServerGUI extends JFrame {
    private JButton runButton;
    private JButton restartButton;
    private JCheckBox checkBoxUserCaching;
    private JPanel JPanel;
    private JTextField textRoot;
    private JTextField textPort;
    private JTextField textLastUpdate;
    private JTextField textSizeOfCache;
    private JTextField textFilesInCache;
    private JButton updateCacheButton;
    private JTextArea loggingTextArea;
    private JPanel logPane;
    private JScrollPane scrollPane;
    private JButton saveSettingsButton;
    private JLabel textStatus;
    private JButton enableDisableLoggingButton;
    private JLabel textLoggin;

    private WebServer webServer;
    private StringBuilder sb = new StringBuilder();

    public WebServerGUI(WebServer webServer) {
        this.webServer = webServer;

        updateCacheButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                webServer.updateCache();
                updateServerInfo();
            }
        });

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (webServer.isRunning()) {
                    return;
                }
                configureWebServer();
                try {
                    webServer.run();
                } catch (RuntimeException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Не удалось запустить сервер", JOptionPane.ERROR_MESSAGE);
                }
                updateServerInfo();
            }
        });

        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartServer();
                updateServerInfo();
            }
        });

        saveSettingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Properties props = new Properties();
                    props.setProperty("root", textRoot.getText());
                    props.setProperty("port", textPort.getText());
                    props.setProperty("useCaching", String.valueOf(checkBoxUserCaching.isSelected()));
                    File f = new File("./resources/server.properties");
                    OutputStream out = new FileOutputStream(f);
                    props.store(out, "");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        enableDisableLoggingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textLoggin.getText().equals("Логирование включено")) {
                    textLoggin.setText("Логирование отключено");
                    textLoggin.setForeground(new Color(-3793377));
                    webServer.removeLogListeners();
                } else {
                    textLoggin.setText("Логирование включено");
                    textLoggin.setForeground(new Color(0x06C625));
                    webServer.addLogListener(new LogEventListener() {
                        @Override
                        public void logEventAdded(String log) {
                            sb.append(System.lineSeparator());
                            sb.append(log);
                            loggingTextArea.setText(sb.toString());
                            JScrollBar vertical = scrollPane.getVerticalScrollBar();
                            vertical.setValue(vertical.getMaximum() + 100);
                        }
                    });
                }
            }
        });

        this.addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                webServer.stop();
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

        this.add(this.JPanel);
        this.pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setTitle("Java NIO Server");
        updateServerInfo();
    }

    private void updateServerInfo() {
        textRoot.setText(webServer.getRoot());
        textPort.setText(String.valueOf(webServer.getPort()));
        checkBoxUserCaching.setSelected(webServer.getCaching());
        textFilesInCache.setText(String.valueOf(webServer.getFilesInCache()));
        textSizeOfCache.setText(String.valueOf(webServer.getSizeOfCache()) + " Kb");
        if (webServer.isRunning()) {
            textStatus.setForeground(new Color(0x06C625));
            textStatus.setText("Сервер запущен");
        } else {
            textStatus.setForeground(new Color(-3793377));
            textStatus.setText("Сервер остановлен");
        }
    }

    private void configureWebServer() {
        webServer.setRoot(textRoot.getText());
        webServer.setPort(Integer.parseInt(textPort.getText()));
        webServer.setCaching(checkBoxUserCaching.isSelected());

        webServer.addCacheRefreshedListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateServerInfo();
                Date date = new Date();
                textLastUpdate.setText(String.valueOf(date));
            }
        });
    }

    private void restartServer() {
        webServer.stop();
        webServer = new WebServer();
        configureWebServer();
        if (textLoggin.getText().equals("Логирование включено")) {
            webServer.addLogListener(new LogEventListener() {
                @Override
                public void logEventAdded(String log) {
                    sb.append(System.lineSeparator());
                    sb.append(log);
                    loggingTextArea.setText(sb.toString());
                    JScrollBar vertical = scrollPane.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum() + 100);
                }
            });
        }
        webServer.run();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        JPanel = new JPanel();
        JPanel.setLayout(new GridLayoutManager(3, 1, new Insets(5, 5, 5, 5), -1, -1));
        final javax.swing.JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(5, 5, 5, 5), -1, -1));
        JPanel.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, true));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null));
        final javax.swing.JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, true));
        final JLabel label1 = new JLabel();
        label1.setFocusable(false);
        label1.setText("Files in cache:");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(77, 16), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setFocusable(false);
        label2.setText("Size of cache:");
        panel2.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(77, 16), null, 0, false));
        textLastUpdate = new JTextField();
        textLastUpdate.setEditable(false);
        panel2.add(textLastUpdate, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        textSizeOfCache = new JTextField();
        textSizeOfCache.setEditable(false);
        panel2.add(textSizeOfCache, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        textFilesInCache = new JTextField();
        textFilesInCache.setEditable(false);
        textFilesInCache.setEnabled(true);
        panel2.add(textFilesInCache, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setFocusable(false);
        label3.setText("Last update:");
        panel2.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final javax.swing.JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, true));
        updateCacheButton = new JButton();
        updateCacheButton.setText("Update Cache");
        panel3.add(updateCacheButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(150, -1), 0, false));
        enableDisableLoggingButton = new JButton();
        enableDisableLoggingButton.setText("Enable/Disable Logging");
        panel3.add(enableDisableLoggingButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        textLoggin = new JLabel();
        textLoggin.setForeground(new Color(-3794932));
        textLoggin.setText("Логирование отключено");
        panel3.add(textLoggin, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final javax.swing.JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 1, new Insets(5, 5, 5, 5), -1, -1));
        JPanel.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, true));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null));
        final javax.swing.JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, true));
        final JLabel label4 = new JLabel();
        label4.setFocusable(false);
        label4.setHorizontalAlignment(2);
        label4.setText("Root Directory:");
        panel5.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(71, 30), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setFocusable(false);
        label5.setText("Use Caching:");
        panel5.add(label5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(71, 16), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setFocusable(false);
        label6.setText("Port:");
        panel5.add(label6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(71, 16), null, 0, false));
        checkBoxUserCaching = new JCheckBox();
        checkBoxUserCaching.setEnabled(true);
        checkBoxUserCaching.setText("");
        panel5.add(checkBoxUserCaching, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(229, 24), null, 0, false));
        textRoot = new JTextField();
        textRoot.setEditable(true);
        panel5.add(textRoot, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textPort = new JTextField();
        textPort.setEditable(true);
        panel5.add(textPort, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final javax.swing.JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        runButton = new JButton();
        runButton.setText("Run");
        panel6.add(runButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(100, -1), 0, false));
        restartButton = new JButton();
        restartButton.setText("Restart");
        panel6.add(restartButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(100, -1), 0, false));
        final Spacer spacer2 = new Spacer();
        panel6.add(spacer2, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(191, 11), null, 0, false));
        textStatus = new JLabel();
        textStatus.setForeground(new Color(-14694813));
        textStatus.setText("Сервер остановлен");
        panel6.add(textStatus, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveSettingsButton = new JButton();
        saveSettingsButton.setText("Save settings");
        panel6.add(saveSettingsButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        logPane = new JPanel();
        logPane.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        JPanel.add(logPane, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(500, 300), null, null, 0, false));
        logPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null));
        scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(22);
        logPane.add(scrollPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        loggingTextArea = new JTextArea();
        loggingTextArea.setEditable(false);
        loggingTextArea.setLineWrap(true);
        scrollPane.setViewportView(loggingTextArea);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return JPanel;
    }
}
