package danya.net.messaging;

public class Message {

    private static final String DELIMITER = "/";

    private final MessageType messageType;
    private final String content;

    public Message(MessageType type, String content){
        this.messageType = type;
        this.content = content;
    }

    public static Message parseMessage(String messageAsString){
        String[] split = messageAsString.split(DELIMITER, 2);
        MessageType messageType = MessageType.valueOf(split[0]);
        return new Message(messageType, split[1]);
    }

    @Override
    public String toString(){
        return messageType + DELIMITER + content;
    }

    public MessageType getMessageType(){
        return messageType;
    }

    public String getContent() {
        return content;
    }


}
