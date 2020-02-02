package danya.net;

public class Message {

    public static final String CLIENT_PREFIX = "CLIENT";
    public static final String SERVER_PREFIX = "SERVER";
    public static final String GAME_START = "GAME_IS_STARTED";

    private static final String DELIMITER = "/";

    private final String sender;
    private final String content;

    public Message(String sender, String content){
        this.sender = sender;
        this.content = content;
    }

    public static Message parseMessage(String messageAsString){
        String[] split = messageAsString.split(DELIMITER);
        return new Message(split[0], split[1]);
    }

    @Override
    public String toString(){
        return sender + "/" + content;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }


}
