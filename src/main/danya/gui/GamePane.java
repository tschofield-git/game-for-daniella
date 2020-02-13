package danya.gui;

import danya.net.Client;
import danya.net.messaging.GameUpdatePacket;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;

import java.util.logging.Logger;

public class GamePane extends GridPane {

    private static final Logger LOGGER = Logger.getLogger(GamePane.class.getName());

    Client client;

    boolean gameRunning = true;

    Label chatHistory;
    TextField chatBox;

    public static void switchToGamePane(Client client){
        GamePane gamePane = new GamePane(client);
        PaneController.switchPane(gamePane);
        gamePane.addKeyListeners();
    }

    public GamePane(Client client){
        this.client = client;
        addComponentsToPane();
        new Thread(listenForGameUpdatePackets()).start();
    }

    private void addComponentsToPane() {

        chatHistory = new Label();
        add(chatHistory, 1, 1);

        chatBox = new TextField();
        chatBox.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode().equals(KeyCode.ENTER)){
                client.sendChatMessage(chatBox.getText());
                chatBox.clear();
            }
        });

        add(chatBox, 1, 2);

    }

    private void addKeyListeners(){
        getScene().setOnKeyPressed(this::sendKeyEventIfChatboxNotFocused);
        getScene().setOnKeyReleased(this::sendKeyEventIfChatboxNotFocused);
    }

    private void sendKeyEventIfChatboxNotFocused(KeyEvent keyEvent){
        if(!chatBox.isFocused()) client.sendKeyEvent(keyEvent);
    }

    private Task<Void> listenForGameUpdatePackets(){

        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                LOGGER.info("Listening for game update packets");
                while(gameRunning) {
                    GameUpdatePacket gameUpdatePacket = client.readGameUpdatePacket();
                    LOGGER.info(gameUpdatePacket::toString);
                    processGameUpdatePacket(gameUpdatePacket);
                }
                return null;
            }
        };

    }

    private void processGameUpdatePacket(GameUpdatePacket gameUpdatePacket) {
        processChatMessages(gameUpdatePacket);
    }

    private void processChatMessages(GameUpdatePacket gameUpdatePacket) {
        Platform.runLater(() -> chatHistory.setText(chatHistory.getText() + gameUpdatePacket.getNewChatMessages()));
    }

}
