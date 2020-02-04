package danya.net;

import danya.Lock;
import danya.net.messaging.*;
import javafx.concurrent.Task;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class Client {

    private Socket socket;
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private boolean keepClientAlive = true;

    private MessageHandler messageHandler;

    public Client(ConnectionDetails connectionDetails){
        initialise(createSocketConnection(connectionDetails));
    }

    public Client(Socket socket){
        initialise(socket);
    }

    private Socket createSocketConnection(ConnectionDetails connectionDetails){
        LOGGER.info(() -> "Connecting to " + connectionDetails.getHostAddress() + " on port " + connectionDetails.getPortNumber());
        Socket connection = null;
        try {
            connection = new Socket(connectionDetails.getHostAddress(), connectionDetails.getPortNumber());
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
        return connection;
    }

    private void initialise(Socket socket){
        this.socket = socket;
        createMessageHandler();
        new Thread(listenForServerMessages()).start();
    }

    private void createMessageHandler() {
        try {
            this.messageHandler = new MessageHandler(socket);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public void sendKeyEvent(KeyEvent keyEvent){
        String content = keyEvent.getEventType().getName() + "|" + keyEvent.getCode();
        Message message = new Message(Sender.CLIENT, MessageType.KEY_INPUT, content);
        messageHandler.sendMessage(message);
    }

    private Task<Void> listenForServerMessages() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                while(keepClientAlive) {
                    while (!messageHandler.hasMessage()) {
                        synchronized (Lock.WAIT_FOR_SERVER_MESSAGE) {
                            Lock.WAIT_FOR_SERVER_MESSAGE.wait();
                        }
                    }
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
            case CHAT_MESSAGE:
                handleChatMessage();
                break;
            default:
                LOGGER.severe("Invalid Message type");
        }
    }

    private void handleSystemMessage(String content) {
        if(content.equals(SystemMessage.GAME_START)) releaseGameStartLock();
    }

    private void releaseGameStartLock() {
        synchronized (Lock.WAIT_FOR_HOST_TO_START){
            LOGGER.info("Game starting");
            Lock.WAIT_FOR_HOST_TO_START.notifyAll();
        }
    }

    private void handleChatMessage() {
    }

    public MessageHandler getMessageHandler(){
        return messageHandler;
    }

    public void closeClientConnection(){
        LOGGER.info("Closing client connection");
        try {
            Message message = new Message(Sender.CLIENT, MessageType.SYSTEM, SystemMessage.GOODBYE);
            messageHandler.sendMessage(message);
            socket.close();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }


}
