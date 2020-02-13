package danya.net.messaging;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class MessageHandler {


    private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());

    final ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;

    public MessageHandler(Socket socket) throws IOException {
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());

        //objectOutputStream.flush();
    }

    public void sendMessage(Message message){
        LOGGER.info(() -> "Sending message " + message);
        try {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public Message readMessage(){
        try {
            // readUTF should block until a message is available
            synchronized (objectInputStream) {
                return (Message) objectInputStream.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.severe(e.getMessage());
        }
        return null;
    }

}
