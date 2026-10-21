package com.bigb.snake;

import com.bigb.snake.domain.PlayerData;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import lombok.Setter;

/**
 * Main controller
 */
public class GameController {
    /** The time in milliseconds that the game will wait before moving players so the lower it is, the faster it will be. */
    private static final int MOVE_SPEED = 50;

    /**
     * Dummy player to generate the tri state check during the playing loop (many players standing vs one player standing vs
     * all dead).
     */
    private static final PlayerData GAME_ON = new PlayerData(Color.WHITE, "DUMMY", null, null);

    /** Base players data. */
    private static final PlayerData[] PLAYER_TYPES = { //
            new PlayerData(Color.RED, "Red", KeyCode.Q, KeyCode.W), //
            new PlayerData(Color.BLUE, "Blue", KeyCode.O, KeyCode.P), //
            new PlayerData(Color.GREEN, "Green", KeyCode.LEFT, KeyCode.RIGHT), //
            new PlayerData(Color.YELLOW, "Yellow", KeyCode.NUMPAD4, KeyCode.NUMPAD6) //
    };

    /** Main container. */
    private @FXML Parent gameRoot;

    /** Play area. */
    private @FXML GameArea gameArea;

    /** New game button. */
    private @FXML Button newGameBtn;

    /** Label for the first player's score. */
    private @FXML Label score1;

    /** Label for the second player's score. */
    private @FXML Label score2;

    /** Label for the third player's score. */
    private @FXML Label score3;

    /** Label for the fourth player's score. */
    private @FXML Label score4;

    /** The loop handling the players movement. */
    private Timeline gameLoop;

    /** The loop handling the timer. */
    private Timeline timerLoop;

    /** Label for the game time. */
    private @FXML Label gameTime;

    /** Binded property for the game time value. */
    private StringProperty gameTimeProp = new SimpleStringProperty("00:00");

    /** Number of minutes for the current game. */
    private int minutes;

    /** Number of seconds for the current minute in the game. */
    private int seconds;

    /** Number of players in the game. */
    private @Setter int playersCount;

    /** Players data for the current game. */
    private PlayerData[] players;

    /** Bounded keys for the configured players' movement. */
    private EnumMap<KeyCode, PlayerData> playerKeys;

    /**
     * Initialize the players, game area and loops.
     */
    public void initialize() {
        // Initialize the players and key press once everything is ready.
        Platform.runLater(() -> {
            gameRoot.getScene().setOnKeyPressed(this::handleKeyPress);

            players = Arrays.copyOf(PLAYER_TYPES, playersCount);
            playerKeys = new EnumMap<>(KeyCode.class);
            for (PlayerData player : players) {
                playerKeys.put(player.getLeftKey(), player);
                playerKeys.put(player.getRightKey(), player);
            }
        });

        newGameBtn.setOnAction(this::newGame);

        // Initialize the game timer loop.
        gameTime.textProperty().bind(gameTimeProp);
        timerLoop = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            // Update the game timer
            if (seconds++ == 60) {
                seconds = 0;
                minutes++;
            }

            refreshTimer();
        }));
        timerLoop.setCycleCount(Animation.INDEFINITE);

        // Initialize the main game loop.
        gameLoop = new Timeline(new KeyFrame(Duration.millis(MOVE_SPEED), event -> {
            // Update the players position
            PlayerData lastPlayerStanding = null;
            for (PlayerData player : players) {
                if (player.isAlive() && gameArea.movePlayer(player)) {
                    // Still alive
                    if (lastPlayerStanding == null) {
                        lastPlayerStanding = player;
                    } else {
                        // More than one surviver
                        lastPlayerStanding = GAME_ON;
                    }
                }
            }

            if (lastPlayerStanding == null) {
                stopGame();
                System.out.println(players.length > 1 ? "Draw!!!"
                        : "%s player survived for %s and %s seconds!".formatted(players[0].getName(), minutes, seconds));
            } else if (lastPlayerStanding != GAME_ON && players.length > 1) {
                stopGame();
                lastPlayerStanding.win();
                System.out.println("%s player wins!".formatted(lastPlayerStanding.getName()));
            }
        }));
        gameLoop.setCycleCount(Animation.INDEFINITE);

        // Binds the score boxes to their player.
        List<Label> scoreBoxes = Arrays.asList(score1, score2, score3, score4);
        for (int i = 0; i < scoreBoxes.size(); i++) {
            PlayerData player = PLAYER_TYPES[i];
            scoreBoxes.get(i).textProperty()
                    .bind(Bindings.concat("%s Player Score: ".formatted(player.getName()), player.getScore()));
        }
    }

    /**
     * Starts a new game.
     * 
     * @param event The click button event.
     */
    private void newGame(ActionEvent event) {
        newGameBtn.setDisable(true);
        minutes = 0;
        seconds = 0;
        refreshTimer();

        gameArea.newGame(players, () -> {
            timerLoop.play();
            gameLoop.play();
        });
    }

    /**
     * Stops the current game.
     */
    private void stopGame() {
        gameLoop.stop();
        timerLoop.stop();
        newGameBtn.setDisable(false);
    }

    /**
     * Refresh the game timer value.
     */
    private void refreshTimer() {
        gameTimeProp.set("%s:%s".formatted(getTimerValue(minutes), getTimerValue(seconds)));
    }

    /**
     * Retrieve a clock like value for the timer where value under 10 will be prefixed by '0'.
     * 
     * @param value The value
     * @return String The formatted value always having 2 characters.
     */
    private String getTimerValue(int value) {
        return value < 10 ? "0%s".formatted(value) : String.valueOf(value);
    }

    /**
     * Handles pressed keys to change players' direction.
     * 
     * @param event The pressed key.
     */
    private void handleKeyPress(KeyEvent event) {
        if (gameLoop.getStatus() != Status.RUNNING) {
            // Now playing
            return;
        }

        PlayerData player = playerKeys.get(event.getCode());
        if (player != null) {
            player.turn(event.getCode());
        }
    }
}
