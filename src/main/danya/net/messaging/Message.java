package danya.net.messaging;

public class Message {

    private static final String DELIMITER = "/";

    private final Sender sender;
    private final MessageType messageType;

    private final String content;

    public Message(Sender sender, MessageType type, String content){
        this.sender = sender;
        this.messageType = type;
        this.content = content;
    }

    public static Message parseMessage(String messageAsString){
        String[] split = messageAsString.split(DELIMITER, 3);
        Sender sender = Sender.valueOf(split[0]);
        MessageType messageType = MessageType.valueOf(split[1]);
        return new Message(sender, messageType, split[2]);
    }

    @Override
    public String toString(){
        return sender + DELIMITER + messageType + DELIMITER + content;
    }

    public Sender getSender() {
        return sender;
    }

    public MessageType getMessageType(){
        return messageType;
    }

    public String getContent() {
        return content;
    }


}
