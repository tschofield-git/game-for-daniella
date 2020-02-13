package danya.net;

import danya.Lock;
import danya.net.messaging.*;
import javafx.concurrent.Task;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class Client {

    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final Socket socket;
    private boolean keepListeningForServerMessages = true;

    private MessageHandler messageHandler;

    public Client(ConnectionDetails connectionDetails){
        socket = createSocketConnection(connectionDetails);
        createMessageHandler();
        new Thread(listenForServerMessages()).start();
    }

    private Socket createSocketConnection(ConnectionDetails connectionDetails){
        LOGGER.info(() -> "Connecting to " + connectionDetails.getHostAddress() + " on port " + connectionDetails.getPortNumber());
        Socket connection = null;
        try {
            connection = new Socket(connectionDetails.getHostAddress(), connectionDetails.getPortNumber());
            LOGGER.info("Connected successfully");
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
        return connection;
    }

    private void createMessageHandler() {
        try {
            this.messageHandler = new MessageHandler(socket);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
        LOGGER.info("Message handler created successfully");
    }

    public void sendKeyEvent(KeyEvent keyEvent){
        String content = keyEvent.getEventType().getName() + "|" + keyEvent.getCode();
        Message message = new Message(MessageType.KEY_INPUT, content);
        messageHandler.sendMessage(message);
    }

    public void sendChatMessage(String content){
        Message message = new Message(MessageType.CHAT_MESSAGE, content);
        messageHandler.sendMessage(message);
    }

    public GameUpdatePacket readGameUpdatePacket(){
        return (GameUpdatePacket) messageHandler.readMessage();
    }

    private Task<Void> listenForServerMessages() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                LOGGER.info("Client is now listening for messages");
                while(keepListeningForServerMessages) {
                    Message message = messageHandler.readMessage();
                    LOGGER.info(message::toString);
                    handleMessage(message);
                }
                return null;
            }
        };
    }

    private void handleMessage(Message message) {
        LOGGER.info(() -> "Handling message: " + message.toString());
        switch(message.getMessageType()){
            case SYSTEM:
                handleSystemMessage(message.getContent());
                break;
            default:
                LOGGER.severe("Invalid Message type");
        }
    }

    private void handleSystemMessage(String content) {
        if(content.equals(SystemMessage.GAME_START)) {
            releaseGameStartLock();
            keepListeningForServerMessages = false;
        }
    }

    private void releaseGameStartLock() {
        synchronized (Lock.WAIT_FOR_HOST_TO_START){
            LOGGER.info("Game starting");
            Lock.WAIT_FOR_HOST_TO_START.notifyAll();
        }
    }

    public void closeClientConnection(){
        LOGGER.info("Closing client connection");
        keepListeningForServerMessages = false;
        try {
            Message message = new Message(MessageType.SYSTEM, SystemMessage.GOODBYE);
            messageHandler.sendMessage(message);
            socket.close();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }


}
