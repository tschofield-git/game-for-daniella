package danya.net;

import danya.Lock;
import javafx.concurrent.Task;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class Client {

    private Socket socket;
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private MessagePasser messagePasser;

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
            this.messagePasser = new MessagePasser(socket);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public void sendKeyEvent(KeyEvent keyEvent){
        String message = keyEvent.getEventType().getName() + "|" + keyEvent.getCode();
        LOGGER.info(() -> "Sending keyEvent " + message);
        messagePasser.sendMessageFromClient(message);
    }

    public void closeClientConnection(){
        LOGGER.info("Closing client connection");
        try {
            messagePasser.sendMessageFromClient("-1");
            socket.close();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public MessagePasser getMessagePasser(){
        return messagePasser;
    }

}
