package danya.net.messaging;

import java.io.Serializable;

public class Message implements Serializable {

    private MessageType messageType;
    private String content;

    public Message(MessageType type, String content) {
        this.messageType = type;
        this.content = content;
    }

    public Message(){

    }

    public MessageType getMessageType(){
        return messageType;
    }

    public String getContent() {
        return content;
    }


}
