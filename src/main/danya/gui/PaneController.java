package danya.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.logging.Logger;

public class PaneController extends Application {

    private static final Logger LOGGER = Logger.getLogger(PaneController.class.getName());

    static Stage stage;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        stage.setOnCloseRequest(t -> {
            try {
                stop();
            } catch (Exception e) {
                LOGGER.severe(e.getMessage());
            }
        });

        MenuPane menuPane = new MenuPane();

        Scene scene = new Scene(menuPane, 600, 400);
        stage.setScene(scene);
        stage.show();
    }

    public static void switchPane(Pane paneToSwitchTo){
        stage.getScene().setRoot(paneToSwitchTo);
    }
}
