package com.bigb.snake;

import java.io.IOException;
import java.util.List;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Stage;

/**
 * JavaFX Snake application.
 */
public class SnakeApp extends Application {

    /** 
     * Starts the application.
     * 
     * @param stage The JavaFX stage.
     * @throws IOException Error when loading the scene.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Display a dialog to choose the number of players.
        ChoiceDialog<Integer> playersCountDlg = new ChoiceDialog<Integer>(4, List.of(1, 2, 3, 4));
        playersCountDlg.setTitle("");
        playersCountDlg.setHeaderText("");
        playersCountDlg.setContentText("How many players?");
        playersCountDlg.getDialogPane().getButtonTypes().remove(ButtonType.CANCEL);
        int playersCount = playersCountDlg.showAndWait().orElse(0);

        // Loads the main scene and sets the number of players.
        FXMLLoader fxmlLoader = new FXMLLoader(SnakeApp.class.getResource("gameScene.fxml"));
        Parent root = fxmlLoader.load();
        ((GameController)fxmlLoader.getController()).setPlayersCount(playersCount);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        // Ensure that restoring the window's size reverts it to the proper size.
        stage.maximizedProperty().addListener((obs, wasFull, isFull) -> {
            if (!isFull) {
                stage.sizeToScene();
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}