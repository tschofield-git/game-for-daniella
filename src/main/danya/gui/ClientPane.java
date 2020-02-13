package danya.gui;

import danya.Lock;
import danya.net.Client;
import danya.net.ConnectionDetails;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.logging.Logger;

public class ClientPane extends GridPane {

    private static final Logger LOGGER = Logger.getLogger(ClientPane.class.getName());
    Task<Void> waitForHostToStartGame;
    boolean gameStarted = false;

    Client client;

    public ClientPane(ConnectionDetails connectionDetails){
        client = new Client(connectionDetails);

        Label label = new Label("Waiting for host to start game...");
        add(label, 0, 0);

        Button button = new Button("Quit");
        button.setOnAction(onClick -> stopWaitingForHost());
        add(button, 1, 1);

        waitForHostToStartGame = waitForHostToStartGame();
        new Thread(waitForHostToStartGame).start();
        waitForHostToStartGame.setOnSucceeded(onSuccess -> GamePane.switchToGamePane(client));
    }

    private void stopWaitingForHost() {
        client.closeClientConnection();
        MenuPane menuPane = new MenuPane();
        this.getScene().setRoot(menuPane);
    }

    private Task<Void> waitForHostToStartGame() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                synchronized (Lock.WAIT_FOR_HOST_TO_START){
                    while(!gameStarted){
                        if(isCancelled()) break;
                        LOGGER.info("Waiting for game to start");
                        Lock.WAIT_FOR_HOST_TO_START.wait();
                        gameStarted = true;
                    }
                    LOGGER.info("Game started");
                }
                return null;
            }
        };
    }

}
