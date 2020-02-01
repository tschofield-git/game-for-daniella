package danya.gui;

import danya.net.Client;
import javafx.scene.layout.GridPane;

public class GamePane extends GridPane {

    Client client;

    public GamePane(Client client){
        this.client = client;
    }

    public void addKeyListeners(){
        getScene().setOnKeyPressed(client::sendKeyEvent);
        getScene().setOnKeyReleased(client::sendKeyEvent);
    }


}
