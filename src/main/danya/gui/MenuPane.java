package danya.gui;

import danya.net.Server;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.logging.Logger;

public class MenuPane extends GridPane {

    private static final Logger LOGGER = Logger.getLogger(MenuPane.class.getName());

    public MenuPane() {
        setVgap(10);
        add(menuTitle(), 0, 0);
        add(menuSubtitle(), 0, 1);
        add(serverButton(), 1, 2);
        add(clientPane(), 1, 4);
    }

    private Label menuTitle(){
        Label menuTitle = new Label("Escape Room");
        menuTitle.setFont(Font.font("Verdana", 40));
        menuTitle.setTextFill(Color.RED);
        return menuTitle;
    }

    private Label menuSubtitle(){
        Label menuSubtitle = new Label("A game for Danya");
        menuSubtitle.setFont(Font.font("Verdana", 20));
        return menuSubtitle;
    }

    private Button serverButton(){
        Button serverButton = new Button("Create Server");
        serverButton.addEventHandler(MouseEvent.MOUSE_CLICKED, onClick -> {
            Server server = null;
            try {
                server = new Server();
                server.start();
            } catch (IOException e) {
                LOGGER.severe(e.getMessage());
            }
        });
        return serverButton;
    }

    private GridPane clientPane(){
        GridPane clientPane = new GridPane();
        clientPane.setHgap(10);
        clientPane.setVgap(10);
        clientPane.setPadding(new Insets(3, 3, 3, 3));
        clientPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT)) );

        TextField hostAddress = new TextField();
        hostAddress.setPromptText("Host IP");

        TextField hostPort = new TextField();
        hostPort.setPromptText("Port");

        Button clientButton = new Button("Join Game");
        clientButton.setDisable(true);
        clientButton.addEventHandler(MouseEvent.MOUSE_CLICKED, onClick -> switchToClientPane(hostAddress.getText(), Integer.parseInt(hostPort.getText())));

        hostAddress.addEventHandler(KeyEvent.KEY_RELEASED, onKeyTyped -> enableClientButtonIfValid(hostAddress, hostPort, clientButton));

        hostPort.addEventHandler(KeyEvent.KEY_RELEASED, onKeyTyped -> enableClientButtonIfValid(hostAddress, hostPort, clientButton));

        clientPane.add(hostAddress, 0, 0);
        clientPane.add(hostPort, 1, 0);
        clientPane.add(clientButton, 0, 1, 1, 1);
        return clientPane;
    }

    private void enableClientButtonIfValid(TextField hostAddress, TextField hostPort, Button clientButton) {
        clientButton.setDisable(hostAddress.getText().isEmpty() || !hostPort.getText().matches("\\d+"));
    }

    private void switchToClientPane(String hostAddress, int hostPort){
        ClientPane clientPane = new ClientPane();
        clientPane.setClient(hostAddress, hostPort);
        this.getScene().setRoot(clientPane);
        clientPane.addKeyListener();
    }

}
