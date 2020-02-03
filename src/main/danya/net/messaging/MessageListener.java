package danya.net.messaging;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * Listens for messages and stores them in a buffer to be read
 */
public class MessageListener {

    private static final Logger LOGGER = Logger.getLogger(MessageListener.class.getName());

    private boolean keepListening;

    private final LinkedList<Message> messageBuffer;
    private final DataInputStream dataInputStream;


    public MessageListener(InputStream inputStream){
        this.dataInputStream = new DataInputStream(inputStream);
        messageBuffer = new LinkedList<>();
    }

    public Runnable listenForMessages(){
        return () -> {
            while(keepListening){
                try {
                    String received = dataInputStream.readUTF();
                    Message message = Message.parseMessage(received);
                    messageBuffer.add(message);
                } catch (IOException e) {
                    LOGGER.severe(e.getMessage());
                }
            }
        };
    }

    public Message getNextMessage(){
        if(messageBuffer.isEmpty()) return null;
        return messageBuffer.remove();
    }

    public boolean hasNext() {
        return !messageBuffer.isEmpty();
    }


    public void startListening(){
        keepListening = true;
        new Thread(listenForMessages()).start();
    }
    public void stopListening(){
        keepListening = false;
    }
}
