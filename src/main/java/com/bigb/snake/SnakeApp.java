package com.bigb.snake;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * JavaFX Snake application.
 */
public class SnakeApp extends Application {
    // Game difficulties mapping with their speed.
    private static final Map<String, Integer> DIFFICULTIES = Stream.of(//
            Map.entry("Easy", 75), //
            Map.entry("Normal", 50), //
            Map.entry("Hard", 25) //
    ).collect(Collectors.collectingAndThen(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> a, LinkedHashMap::new),
            Collections::unmodifiableMap));

    /**
     * Starts the application.
     * 
     * @param stage The JavaFX stage.
     * @throws IOException Error when loading the scene.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Display a dialog to choose the number of players and game speed.
        ChoiceBox<Integer> playersCount = new ChoiceBox<>(FXCollections.observableArrayList(1, 2, 3, 4));
        ChoiceBox<String> difficulty = new ChoiceBox<>(FXCollections.observableList(new ArrayList<>(DIFFICULTIES.keySet())));

        // Default values
        playersCount.setValue(4);
        difficulty.setValue(difficulty.getItems().get(1));

        GridPane gameSettingsGrid = new GridPane(10, 10);
        gameSettingsGrid.addRow(0, new Label("Players:"), playersCount);
        gameSettingsGrid.addRow(1, new Label("Difficulty:"), difficulty);

        Dialog<GameSettings> gameSettingsDlg = new Dialog<>();
        gameSettingsDlg.setTitle("Game Settings");
        gameSettingsDlg.getDialogPane().setContent(gameSettingsGrid);
        gameSettingsDlg.getDialogPane().getButtonTypes().add(ButtonType.OK);
        gameSettingsDlg.setResultConverter(button -> button == ButtonType.OK
                ? new GameSettings(playersCount.getValue(), DIFFICULTIES.get(difficulty.getValue()))
                : null);

        GameSettings gameSettings = gameSettingsDlg.showAndWait()
                .orElseThrow(() -> new RuntimeException("Missing game settings!"));

        // Loads the main scene and configure the game.
        FXMLLoader fxmlLoader = new FXMLLoader(SnakeApp.class.getResource("gameScene.fxml"));
        Parent root = fxmlLoader.load();
        GameController gameController = (GameController) fxmlLoader.getController();
        gameController.setPlayersCount(gameSettings.playersCount());
        gameController.setGameSpeed(gameSettings.gameSpeed());

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

    record GameSettings(int playersCount, int gameSpeed) {
    }
}