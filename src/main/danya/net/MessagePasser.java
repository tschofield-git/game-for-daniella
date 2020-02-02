package danya.net;

import danya.Lock;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.logging.Logger;

public class MessagePasser {

    private boolean keepListening;
    private final LinkedList<Message> messageBuffer;

    private static final Logger LOGGER = Logger.getLogger(MessagePasser.class.getName());

    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;

    public MessagePasser(Socket socket) throws IOException {
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        messageBuffer = new LinkedList<>();
        keepListening = true;
        new Thread(listenForMessages()).start();
    }

    public void sendMessageFromClient(String content){
        Message message = new Message(Message.CLIENT_PREFIX, content);
        sendMessage(message);
    }

    public void sendMessageFromServer(String content){
        Message message = new Message(Message.SERVER_PREFIX, content);
        sendMessage(message);
    }

    private void sendMessage(Message message){
        try {
            dataOutputStream.writeUTF(message.toString());
            dataOutputStream.flush();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public Runnable listenForMessages(){
        return () -> {
            while(keepListening){
                try {
                    String received = dataInputStream.readUTF();
                    Message message = Message.parseMessage(received);
                    messageBuffer.add(message);
                    if(message.getSender().equals(Message.CLIENT_PREFIX)) continue;
                    handleReceivedMessage(message);
                } catch (IOException e) {
                    LOGGER.severe(e.getMessage());
                }
            }
        };
    }

    private void handleReceivedMessage(Message message) {
        LOGGER.info(() -> "Client received message from server: " + message.toString());
        if(message.getContent().equals(Message.GAME_START)) releaseLock();
    }

    private void releaseLock() {
        synchronized (Lock.WAIT_FOR_HOST_TO_START){
            LOGGER.info("Server: Game starting");
            Lock.WAIT_FOR_HOST_TO_START.notifyAll();
        }
    }

    public Message nextMessage(){
        if(messageBuffer.isEmpty()) return null;
        return messageBuffer.remove();
    }

    public boolean hasNext() {
        return !messageBuffer.isEmpty();
    }

    public void stopListening(){
        keepListening = false;
    }

}
