package danya.gui;

import danya.Lock;
import danya.net.Client;
import danya.net.Server;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.logging.Logger;

public class HostPane extends GridPane {

    private static final Logger LOGGER = Logger.getLogger(HostPane.class.getName());
    Server server;

    Label connectedClients;
    Button startGameButton;

    Task<Void> waitForClientsToConnect;

    public HostPane(){
        server = null;
        try {
            server = new Server();
            server.start();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
        addComponentsToPane();
        new Thread(waitForClientsToConnect).start();
    }

    private void addComponentsToPane() {
        waitForClientsToConnect = waitForClientsToConnect();
        connectedClients = new Label("Waiting for connection...");
        connectedClients.textProperty().bind(waitForClientsToConnect.messageProperty());
        add(connectedClients, 0, 0);

        startGameButton = new Button("Start game");
        startGameButton.setDisable(true);
        startGameButton.setOnAction(onClick -> startGame());
        add(startGameButton, 0, 1);

        Button cancelGameButton = new Button("Cancel game");
        cancelGameButton.setOnAction(onClick -> cancelGame());
        add(cancelGameButton, 2, 2);
    }

    private void cancelGame() {
        server.shutdownServer();
        MenuPane menuPane = new MenuPane();
        this.getScene().setRoot(menuPane);
    }

    private void startGame(){
        switchToGamePane();
    }

    private void switchToGamePane(){
        Client client = new Client(server.getConnectionDetails());
        GamePane gamePane = new GamePane(client);
        PaneController.switchPane(gamePane);
        gamePane.addKeyListeners();
    }

    private Task<Void> waitForClientsToConnect() {

        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                synchronized (Lock.WAIT_FOR_CLIENTS_TO_CONNECT){
                    try {
                        while(!server.isAnyoneConnected()){
                            if(isCancelled()) break;
                            Lock.WAIT_FOR_CLIENTS_TO_CONNECT.wait();
                        }
                    } catch (InterruptedException e) {
                        LOGGER.severe(e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                }
                updateMessage("Connected clients: " + server.getConnectedClients().toString());
                startGameButton.setDisable(false);
                return null;
            }
        };
    }

}
