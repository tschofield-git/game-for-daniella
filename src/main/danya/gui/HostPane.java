package danya.gui;

import danya.Lock;
import danya.net.Client;
import danya.net.Server;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.logging.Logger;

public class HostPane extends GridPane {

    private static final Logger LOGGER = Logger.getLogger(HostPane.class.getName());
    Server server;
    Client client;

    Label connectedClients;
    Button startGameButton;

    Task<Void> waitForClientsToConnect;

    public HostPane(){
        startServer();
        createClientForHost();
        addComponentsToPane();
        new Thread(waitForClientsToConnect).start();
    }

    private void startServer() {
        server = null;
        try {
            server = new Server();
            server.start();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    private void createClientForHost() {
        client = new Client(server.getConnectionDetails());
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
        client.closeClientConnection();
        server.shutdownServer();
        MenuPane menuPane = new MenuPane();
        this.getScene().setRoot(menuPane);
    }

    private void startGame(){
        notifyServerOfStartGameClicked();
        GamePane.switchToGamePane(client);
    }

    private void notifyServerOfStartGameClicked() {
        server.setGameStartedToTrue();
        synchronized (Lock.WAIT_FOR_START_GAME_CLICKED) {
            Lock.WAIT_FOR_START_GAME_CLICKED.notifyAll();
        }
    }

    private Task<Void> waitForClientsToConnect() {

        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                synchronized (Lock.WAIT_FOR_CLIENTS_TO_CONNECT){
                    try {
                        while(!server.areAllClientsConnected()){
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
