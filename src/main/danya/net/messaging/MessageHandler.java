package danya.net.messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class MessageHandler {


    private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());

    private final DataOutputStream dataOutputStream;
    private final DataInputStream dataInputStream;

    public MessageHandler(Socket socket) throws IOException {
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataInputStream = new DataInputStream(socket.getInputStream());
    }

    public void sendMessage(Message message){
        LOGGER.info(() -> "Sending message " + message);
        try {
            dataOutputStream.writeUTF(message.toString());
            dataOutputStream.flush();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public Message readMessage(){
        try {
            // readUTF should block until a message is available
            String received = dataInputStream.readUTF();
            return Message.parseMessage(received);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
        return null;
    }

    public boolean hasMessage(){
        try {
            return dataInputStream.available() > 0;
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
        return false;
    }

}
