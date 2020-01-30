package danya.gui;

import danya.net.Client;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class ClientPane extends GridPane {

    Client client;

    public ClientPane(){
        Label label = new Label("ClientPane");
        add(label, 0, 0);
    }

    public void addKeyListener(){
        getScene().setOnKeyPressed(client::sendKeyEvent);
    }

    public void setClient(String hostIP, int port){
        client = new Client(hostIP, port);
    }

}
