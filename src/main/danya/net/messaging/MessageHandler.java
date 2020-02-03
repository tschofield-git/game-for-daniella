package danya.net.messaging;

import danya.Lock;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class MessageHandler {


    private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());

    private final MessageSender messageSender;
    private final MessageListener messageListener;

    public MessageHandler(Socket socket) throws IOException {
        messageSender = new MessageSender(socket.getOutputStream());
        messageListener = new MessageListener(socket.getInputStream());
        messageListener.startListening();
    }

    public void sendMessage(Message message){
        messageSender.sendMessage(message);
    }

    public Message readMessage(){
        return messageListener.getNextMessage();
    }

    private void handleMessage(Message message) {
        LOGGER.info(() -> "Handling message: " + message.toString());
        switch(message.getMessageType()){
            case SYSTEM:
                handleSystemMessage(message.getContent());
                break;
            case KEY_INPUT:
                handleKeyInput();
                break;
            case MOUSE_INPUT:
                handleMouseInput();
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
            LOGGER.info("Server: Game starting");
            Lock.WAIT_FOR_HOST_TO_START.notifyAll();
        }
    }

    private void handleKeyInput() {
    }

    private void handleMouseInput() {
    }

    private void handleChatMessage() {
    }

}
