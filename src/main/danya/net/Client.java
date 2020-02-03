package danya.net;

import danya.net.messaging.*;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class Client {

    private Socket socket;
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private MessageHandler messageHandler;

    public Client(ConnectionDetails connectionDetails){
        initialise(createSocketConnection(connectionDetails));
    }

    public Client(Socket socket){
        initialise(socket);
    }

    private Socket createSocketConnection(ConnectionDetails connectionDetails){
        LOGGER.info("Connecting to " + connectionDetails.getHostAddress() + " on port " + connectionDetails.getPortNumber());
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
        createMessagePasser();
    }

    private void createMessagePasser() {
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

    public MessageHandler getMessageHandler(){
        return messageHandler;
    }

}
