package danya.net.messaging;

import java.io.Serializable;

public enum MessageType implements Serializable {

    SYSTEM,
    KEY_INPUT,
    MOUSE_INPUT,
    CHAT_MESSAGE;

}
