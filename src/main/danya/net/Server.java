package danya.net;

import danya.Lock;
import danya.net.messaging.*;
import javafx.concurrent.Task;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class Server extends Thread{

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final ServerSocket serverSocket;
    private boolean keepServerAlive = true;
    private boolean gameStarted = false;

    private final LinkedList<MessageHandler> messageHandlersForEachClientConnection;
    private final HashMap<MessageHandler, LinkedList<Message>> messageBufferForEachClientConnection;

    private static final int MAX_NUMBER_OF_CLIENTS = 2;

    private GameUpdatePacket gameUpdatePacket;

    public Server() throws IOException {
        InetAddress ipAddress = getCurrentIp();
        serverSocket = new ServerSocket(0, 1, ipAddress);
        messageHandlersForEachClientConnection = new LinkedList<>();
        messageBufferForEachClientConnection = new HashMap<>();
    }

    @Override
    public void run(){
        LOGGER.info("Running Server: " + serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());
        try {
            waitForAllClientsToConnect();
            LOGGER.info("All clients connected!");
            waitForStartGameToBeClicked();
            LOGGER.info("Game start clicked");
            alertClientsOfGameStart();
            LOGGER.info("Clients alerted of game start");
            doMainGameLoop();
        } catch (InterruptedException e) {
            LOGGER.severe(e.getMessage());
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    private void waitForAllClientsToConnect() throws IOException {
        while (keepServerAlive && !areAllClientsConnected()) {
            acceptConnection();
        }
    }

    private void acceptConnection() throws IOException {
        Socket connection = serverSocket.accept();

        MessageHandler messageHandler = new MessageHandler(connection);
        messageHandlersForEachClientConnection.add(messageHandler);

        LinkedList<Message> messageBuffer = new LinkedList<>();
        messageBufferForEachClientConnection.put(messageHandler, messageBuffer);

        new Thread(bufferMessagesFromClient(messageBuffer, messageHandler)).start();

        String clientAddress = connection.getInetAddress().getHostAddress();
        LOGGER.info(() -> "New connection from " + clientAddress);
        releaseLockForHostPane();
    }

    private void releaseLockForHostPane() {
        synchronized (Lock.WAIT_FOR_CLIENTS_TO_CONNECT){
            Lock.WAIT_FOR_CLIENTS_TO_CONNECT.notifyAll();
        }
    }

    private void waitForStartGameToBeClicked() throws InterruptedException {
        synchronized (Lock.WAIT_FOR_START_GAME_CLICKED){
            while(!gameStarted) {
                Lock.WAIT_FOR_START_GAME_CLICKED.wait();
            }
        }
    }

    private void alertClientsOfGameStart() {
        for(MessageHandler messageHandler : messageHandlersForEachClientConnection){
            Message message = new Message(MessageType.SYSTEM, SystemMessage.GAME_START);
            messageHandler.sendMessage(message);
        }
    }

    private void doMainGameLoop() {
        LOGGER.info("Main loop started");
        while (keepServerAlive) {
            gameUpdatePacket = new GameUpdatePacket();
            for(MessageHandler messageHandler : messageHandlersForEachClientConnection){
                handleAllClientMessages(messageHandler);
            }
            if(!gameUpdatePacket.isAnythingToUpdate()) continue;
            for(MessageHandler messageHandler : messageHandlersForEachClientConnection){
                messageHandler.sendMessage(gameUpdatePacket);
            }
        }
    }

    private void handleAllClientMessages(MessageHandler messageHandler) {
        LinkedList<Message> messages = messageBufferForEachClientConnection.get(messageHandler);
        while(!messages.isEmpty()) {
            Message message = messages.remove();
            handleClientMessage(messageHandler, message);
        }
    }

    private void handleClientMessage(MessageHandler messageHandler, Message message){
        LOGGER.info(() -> "Server received message: " + message.toString());
        switch(message.getMessageType()){
            case SYSTEM:
                handleSystemMessage(messageHandler, message);
                break;
            case KEY_INPUT:
                handleKeyInput();
                break;
            case MOUSE_INPUT:
                handleMouseInput();
                break;
            case CHAT_MESSAGE:
                handleChatMessage(message.getContent());
                break;
            default:
                LOGGER.severe("Invalid Message type");
        }
    }

    private void handleSystemMessage(MessageHandler messageHandler, Message message) {
        switch (message.getContent()){
            case SystemMessage.GOODBYE:
                handleClientDisconnect(messageHandler);
                break;
            default:
                LOGGER.severe("Invalid system message");
        }
    }

    private void handleClientDisconnect(MessageHandler messageHandler) {
        messageHandlersForEachClientConnection.remove(messageHandler);
        LOGGER.info(() -> "Client disconnected " + messageHandler.toString());
    }

    private void handleKeyInput() {
    }

    private void handleMouseInput() {
    }

    private void handleChatMessage(String content) {
        LOGGER.info(() -> "Adding chat message: " + content);
        gameUpdatePacket.addNewChatMessage(content);
    }

    private Task<Void> bufferMessagesFromClient(LinkedList<Message> buffer, MessageHandler clientMessageHandler){
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while(keepServerAlive) {
                    Message message = clientMessageHandler.readMessage();
                    buffer.add(message);
                }
                return null;
            }
        };
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

    public boolean areAllClientsConnected(){
        return messageHandlersForEachClientConnection.size() == MAX_NUMBER_OF_CLIENTS;
    }

    public ConnectionDetails getConnectionDetails(){
        return new ConnectionDetails(serverSocket.getInetAddress(), serverSocket.getLocalPort());
    }

    public List<MessageHandler> getConnectedClients() {
        return messageHandlersForEachClientConnection;
    }

    public void setGameStartedToTrue() {
        gameStarted = true;
    }
}