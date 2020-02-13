package danya.net.messaging;

import java.io.Serializable;

public class GameUpdatePacket extends Message implements Serializable {

    String newChatMessages;

    public GameUpdatePacket(){
        newChatMessages = "";
    }

    public void addNewChatMessage(String chatMessage){
        newChatMessages += chatMessage + "\n";
    }

    public String getNewChatMessages(){ return newChatMessages; }

    public boolean isAnythingToUpdate(){
        return !newChatMessages.isBlank();
    }


}
