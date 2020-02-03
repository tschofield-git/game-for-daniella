package danya.net;

import danya.Lock;
import danya.net.messaging.*;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class Server extends Thread{

    private final ServerSocket serverSocket;
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private boolean keepServerAlive = true;
    private final LinkedList<Client> clientList;
    private static final int MAX_NUMBER_OF_CLIENTS = 2;

    public Server() throws IOException {
        InetAddress ipAddress = getCurrentIp();
        serverSocket = new ServerSocket(0, 1, ipAddress);
        clientList = new LinkedList<>();
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

    private void acceptConnection() throws IOException {
        Socket connection = serverSocket.accept();
        Client client = new Client(connection);
        clientList.add(client);
        String clientAddress = connection.getInetAddress().getHostAddress();
        LOGGER.info(() -> "New connection from " + clientAddress);
        releaseLockForHostPane();
    }

    private void releaseLockForHostPane() {
        synchronized (Lock.WAIT_FOR_CLIENTS_TO_CONNECT){
            Lock.WAIT_FOR_CLIENTS_TO_CONNECT.notifyAll();
        }
    }

    @Override
    public void run(){
        LOGGER.info("Running Server: " + serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());
        try {
            waitForAllClientsToConnect();
            alertClientsOfGameStart();
            doMainGameLoop();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    private void alertClientsOfGameStart() {
        for(Client client : clientList){
            Message message = new Message(Sender.SERVER, MessageType.SYSTEM, SystemMessage.GAME_START);
            client.getMessageHandler().sendMessage(message);
        }
    }

    private void doMainGameLoop() throws IOException {
        while (keepServerAlive) {
            for(Client client : clientList){
                readFromClient(client);
                // Feed back to client
            }
        }
    }

    private void waitForAllClientsToConnect() throws IOException {
        while (!allClientsConnected()) {
            acceptConnection();
        }
        LOGGER.info("All clients connected!");
    }

    private void readFromClient(Client client) {
        MessageHandler messageHandler = client.getMessageHandler();
        Message message;
        while((message = messageHandler.readMessage()) != null){
            if(message.getSender().equals(Sender.SERVER)) continue;
            handleClientMessage(message);
        }
    }

    private void handleClientMessage(Message message){
        LOGGER.info(() -> "Server received message: " + message.toString());
        //GameLogic.handleMessage(message);
    }

    private Runnable checkClientsStillConnected(){
//        return () -> {
//            for(Client client : connectionList){
//                MessagePasser messagePasser = clientMessagePasserMap.get(client);
//                messagePasser.nextMessage().equals("-1");
//            }
//        };
        return null;
    }

    public void shutdownServer(){
        keepServerAlive = false;
        LOGGER.info("Shutting down Server: " + serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    private boolean allClientsConnected(){
        return clientList.size() == MAX_NUMBER_OF_CLIENTS;
    }

    public boolean isAnyoneConnected() {
        return !clientList.isEmpty();
    }

    public List<Client> getConnectedClients(){
        return clientList;
    }

    public ConnectionDetails getConnectionDetails(){
        return new ConnectionDetails(serverSocket.getInetAddress(), serverSocket.getLocalPort());
    }
}