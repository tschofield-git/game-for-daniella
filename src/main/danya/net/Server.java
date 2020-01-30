package danya.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;
import java.util.logging.Logger;

public class Server extends Thread{

    private ServerSocket serverSocket;
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private boolean keepServerAlive = true;

    public Server() throws IOException {
        InetAddress ipAddress = getCurrentIp();
        serverSocket = new ServerSocket(0, 1, ipAddress);
    }

    private InetAddress getCurrentIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces
                        .nextElement();
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                while(nias.hasMoreElements()) {
                    InetAddress ia= nias.nextElement();
                    if (!ia.isLinkLocalAddress()
                            && !ia.isLoopbackAddress()
                            && ia instanceof Inet4Address) {
                        return ia;
                    }
                }
            }
        } catch (SocketException e) {
            LOGGER.severe(e.getMessage());
        }
        return null;
    }

    private void listen() throws IOException {
        String data = null;
        Socket client = this.serverSocket.accept();
        String clientAddress = client.getInetAddress().getHostAddress();
        LOGGER.info("New connection from " + clientAddress);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream()));
        while ( (data = in.readLine()) != null ) {
            LOGGER.info("Message from " + clientAddress + ": " + data);
        }
    }

    @Override
    public void run(){
        LOGGER.info("Running Server: " + serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());
        try {
            while (keepServerAlive) {
                listen();
            }
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public void shutdownServer(){
        LOGGER.info("Shutting down Server: " + serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());
        keepServerAlive = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public InetAddress getServerAddress(){
        return serverSocket.getInetAddress();
    }

    public int getServerPort(){
        return serverSocket.getLocalPort();
    }
}