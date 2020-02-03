package danya.net.messaging;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

public class MessageSender {

    private static final Logger LOGGER = Logger.getLogger(MessageSender.class.getName());

    DataOutputStream dataOutputStream;

    public MessageSender(OutputStream outputStream){
        this.dataOutputStream = new DataOutputStream(outputStream);
    }

    public void sendMessage(Message message){
        try {
            dataOutputStream.writeUTF(message.toString());
            dataOutputStream.flush();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }



}
