package danya.gui;

import danya.Lock;
import danya.net.Client;
import danya.net.ConnectionDetails;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.logging.Logger;

public class ClientPane extends GridPane {

    private static final Logger LOGGER = Logger.getLogger(ClientPane.class.getName());
    Task waitForHostToStartGame;
    boolean gameStarted = false;

    Client client;

    public ClientPane(ConnectionDetails connectionDetails){
        client = new Client(connectionDetails);

        Label label = new Label("Waiting for host to start game...");
        add(label, 0, 0);

        waitForHostToStartGame = waitForHostToStartGame();
        new Thread(waitForHostToStartGame).start();
    }

    private void switchToGamePane(){
        GamePane gamePane = new GamePane(client);
        PaneController.switchPane(gamePane);
        gamePane.addKeyListeners();
    }

    private Task<Void> waitForHostToStartGame() {

        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                synchronized (Lock.WAIT_FOR_HOST_TO_START){
                    while(!gameStarted){
                        if(isCancelled()) break;
                        Lock.WAIT_FOR_HOST_TO_START.wait();
                        gameStarted = true;
                    }
                    LOGGER.info("Game started");
                }
                switchToGamePane();
                return null;
            }
        };
    }

}
