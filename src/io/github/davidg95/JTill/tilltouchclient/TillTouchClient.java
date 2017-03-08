/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.tilltouchclient;

import io.github.davidg95.JTill.jtill.ServerConnection;
import java.awt.Image;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 *
 * @author 1301480
 */
public class TillTouchClient {

    public static String HOST_NAME = "Test";
    public static String SERVER_ADDRESS = "127.0.0.1";
    public static int PORT = 600;

    private static ServerConnection sc;
    public static GUI g;

    private static Properties properties;

    public static Image icon;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new TillTouchClient().start();
    }

    public TillTouchClient() {
        icon = new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/tillIcon.png")).getImage();
        loadProperties();
        g = new GUI(sc);
        try {
            sc = new ServerConnection();
            sc.setGUI(g);
            sc.connect(SERVER_ADDRESS, PORT, HOST_NAME);
        } catch (IOException ex) {
            int opt = JOptionPane.showOptionDialog(null, "Error connecting to server " + SERVER_ADDRESS + " on port " + PORT + "\nTry again?", "Connection Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, new javax.swing.ImageIcon(getClass().getResource("/io/github/davidg95/JTill/resources/tillIcon.png")), null, null);
            if (opt == JOptionPane.YES_OPTION) {
                initialSetup();
                saveProperties();
            } else {
                System.exit(0);
            }
        }
    }

    public void start() {
        g.setVisible(true);
        g.login();
        g.setButtons();
    }

    public static ServerConnection getServerConnection() {
        return sc;
    }

    public static Image getIcon() {
        return icon;
    }

    public static void loadProperties() {
        properties = new Properties();
        InputStream in;

        try {
            in = new FileInputStream("server.properties");

            properties.load(in);

            HOST_NAME = properties.getProperty("host");
            SERVER_ADDRESS = properties.getProperty("address", SERVER_ADDRESS);
            PORT = Integer.parseInt(properties.getProperty("port", Integer.toString(PORT)));

            in.close();
        } catch (FileNotFoundException | UnknownHostException ex) {
            initialSetup();
            saveProperties();
        } catch (IOException ex) {
        }
    }

    public static void saveProperties() {
        properties = new Properties();
        OutputStream out;

        try {
            out = new FileOutputStream("server.properties");

            HOST_NAME = InetAddress.getLocalHost().getHostName();

            properties.setProperty("host", HOST_NAME);
            properties.setProperty("address", SERVER_ADDRESS);
            properties.setProperty("port", Integer.toString(PORT));

            properties.store(out, null);
            out.close();
        } catch (FileNotFoundException | UnknownHostException ex) {
        } catch (IOException ex) {
        }
    }

    public static void initialSetup() {
        SERVER_ADDRESS = (String) JOptionPane.showInputDialog(null, "Enter JTill Server IP address", "Initial Setup", JOptionPane.PLAIN_MESSAGE, null, null, SERVER_ADDRESS);
        PORT = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter port number", "600"));
    }

}
