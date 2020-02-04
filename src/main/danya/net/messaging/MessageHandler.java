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

    public boolean hasMessage(){
        return messageListener.hasNext();
    }

}
