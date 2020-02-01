package danya.gui;

import danya.net.ConnectionDetails;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.logging.Logger;

public class MenuPane extends GridPane {

    private static final Logger LOGGER = Logger.getLogger(MenuPane.class.getName());

    Button clientButton;

    public MenuPane() {
        setVgap(10);
        add(getMenuTitleLabel(), 0, 0);
        add(getMenuSubtitleLabel(), 0, 1);
        add(getCreateServerButton(), 1, 2);
        add(getUIComponentsForJoiningExistingGame(), 1, 4);
    }

    private Label getMenuTitleLabel() {
        Label menuTitle = new Label("Escape Room");
        menuTitle.setFont(Font.font("Verdana", 40));
        menuTitle.setTextFill(Color.RED);
        return menuTitle;
    }

    private Label getMenuSubtitleLabel() {
        Label menuSubtitle = new Label("A game for Danya");
        menuSubtitle.setFont(Font.font("Verdana", 20));
        return menuSubtitle;
    }

    private Button getCreateServerButton() {
        Button serverButton = new Button("Create Server");
        serverButton.addEventHandler(MouseEvent.MOUSE_CLICKED, onClick -> {
            switchToHostPane();
        });
        return serverButton;
    }


    private GridPane getUIComponentsForJoiningExistingGame() {
        GridPane joinExistingGamePane = new GridPane();
        joinExistingGamePane.setHgap(10);
        joinExistingGamePane.setVgap(10);
        joinExistingGamePane.setPadding(new Insets(3, 3, 3, 3));
        joinExistingGamePane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT)));

        TextField hostAddressField = new TextField();
        hostAddressField.setPromptText("Host IP");

        TextField hostPortField = new TextField();
        hostPortField.setPromptText("Port");

        clientButton = new Button("Join Game");
        clientButton.setDisable(true);
        clientButton.addEventHandler(MouseEvent.MOUSE_CLICKED, onClick -> {
            ConnectionDetails connectionDetails = new ConnectionDetails();
            connectionDetails.setHostAddressFromHostname(hostAddressField.getText());
            connectionDetails.setPortNumber(Integer.parseInt(hostPortField.getText()));
            switchToClientPane(connectionDetails);
        });

        hostAddressField.textProperty().addListener(onAddressFieldChange -> enableClientButtonIfValid(hostAddressField.getText(), hostPortField.getText()));
        hostPortField.textProperty().addListener(onPortFieldChange -> enableClientButtonIfValid(hostAddressField.getText(), hostPortField.getText()));

        joinExistingGamePane.add(hostAddressField, 0, 0);
        joinExistingGamePane.add(hostPortField, 1, 0);
        joinExistingGamePane.add(clientButton, 0, 1, 1, 1);
        return joinExistingGamePane;
    }

    private void enableClientButtonIfValid(String hostAddressFieldText, String hostPortFieldText) {
        clientButton.setDisable(hostAddressFieldText.isEmpty() || !hostPortFieldText.matches("\\d+"));
    }

    private void switchToClientPane(ConnectionDetails connectionDetails) {
        ClientPane clientPane = new ClientPane(connectionDetails);
        PaneController.switchPane(clientPane);
    }

    private void switchToHostPane() {
        HostPane hostPane = new HostPane();
        PaneController.switchPane(hostPane);
    }

}
