package com.bigb.snake;

import com.bigb.snake.domain.DigitalDigits;
import com.bigb.snake.domain.Direction;
import com.bigb.snake.domain.PlayerData;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.mutable.MutableObject;

/**
 * Extension of a {@link TilePane} handling the game area where each tiles represent the blocks for the moving snakes. As the
 * size of a single tile block is hardcoded, the size of the game area will auto adjust itself based on the screen size.
 */
public class GameArea extends TilePane {
    /** The size of a single tile block in pixels. */
    private static final int BLOCK_SIZE = 5;

    /** The color or a block. */
    private static final Color BLOCK_COLOR = Color.BLACK;

    /** The number of blocks per row based on the screen size. */
    private final int blocksPerRow;

    /** Computed starting position and direction for each player. */
    private final PlayerStartingData[] playersStartingData;

    private final EventHandler<ActionEvent> countdownFrameHandler;
    private int countdownValue;

    public GameArea() {
        // Game needs a square area, so using the smaller of the two dimensions with a
        // small offset for controls.
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double gameAreaSize = Math.min(screenSize.getWidth(), screenSize.getHeight()) * 0.80;
        blocksPerRow = (int) (gameAreaSize / BLOCK_SIZE);
        super.setWidth(blocksPerRow * BLOCK_SIZE);
        super.setHeight(blocksPerRow * BLOCK_SIZE);
        super.setPrefRows(blocksPerRow);
        super.setPrefColumns(1);
        super.setMaxSize(blocksPerRow * BLOCK_SIZE, blocksPerRow * BLOCK_SIZE);

        for (int i = 0; i < blocksPerRow; i++) {
            TilePane row = new TilePane();
            row.setPrefColumns(blocksPerRow);
            row.setPrefRows(1);
            for (int j = 0; j < blocksPerRow; j++) {
                row.getChildren().add(new Rectangle(BLOCK_SIZE, BLOCK_SIZE, Color.WHITE));
            }

            super.getChildren().add(row);
        }

        // Calculating the starting position of players as -20% of the middle
        int middle = blocksPerRow / 2;
        int startOffset = (int) ((double) middle * 0.2);
        IntFunction<Integer> startingPos = offsetMultiplicator -> middle + startOffset * offsetMultiplicator;
        this.playersStartingData = new PlayerStartingData[] { //
                new PlayerStartingData(Direction.UP, startingPos.apply(-1), startingPos.apply(-1), 0), //
                new PlayerStartingData(Direction.DOWN, startingPos.apply(1), startingPos.apply(1), 1), //
                new PlayerStartingData(Direction.LEFT, startingPos.apply(-1), startingPos.apply(1), 2), //
                new PlayerStartingData(Direction.RIGHT, startingPos.apply(1), startingPos.apply(-1), 3) //
        };

        // Calculating the even length of blocks for a single "pixel" in the countdown
        // timer so that the 5x5 digital clock
        // use 60% of the available space inside the players starting area.
        double internalAreaSize = startOffset * 2;
        int digitalClockPixelSize = evenValue((int) (internalAreaSize * 0.6 / 5));
        int middleClockOffset = digitalClockPixelSize / 2;

        // Calculating the positions of the digital clock "pixels" boundaries.
        int[] clockPositions = new int[6];
        for (int i = 0; i < clockPositions.length; i++) {
            boolean pastMid = i >= 3;
            int multiplicator = pastMid ? i - 3 : i - 2;
            clockPositions[i] = middle + (middleClockOffset * (pastMid ? 1 : -1)) + (digitalClockPixelSize * multiplicator);
        }

        this.countdownFrameHandler = e -> {
            DigitalDigits digit = DigitalDigits.values()[--this.countdownValue];
            for (int y = clockPositions[0]; y < clockPositions[clockPositions.length - 1]; y++) {
                // Transform Y to a 5x5 coordinate
                int yClock = (y - clockPositions[0]) / digitalClockPixelSize;

                TilePane row = getRow(y);
                for (int x = clockPositions[0]; x < clockPositions[clockPositions.length - 1]; x++) {
                    // Transform X to a 5x5 coordinate
                    int xClock = (x - clockPositions[0]) / digitalClockPixelSize;

                    // Since the navigation is column based, we must pivot the coordinates
                    Color color = digit.isIlluminated(yClock, xClock) ? Color.PURPLE : Color.BLACK;
                    getBlock(row, x).setFill(color);
                }
            }
        };
    }

    /**
     * Initialize the game area for a new game.
     * 
     * @param players The players.
     */
    public void newGame(PlayerData[] players, Runnable countdownCallback) {
        PlayerStartingData[] gamePlayersStartingData = Arrays.copyOf(playersStartingData, players.length);

        // Sort by starting position on a y basis
        Arrays.sort(gamePlayersStartingData);

        MutableObject<PlayerStartingData> nextPlayer = new MutableObject<>(
                findNextPlayerStartingPosition(0, 0, gamePlayersStartingData));

        // Paint each blocks with the right color.
        forEachBlock((coord, block) -> {
            if (nextPlayer.get() != null) {
                int x = coord[0];
                int y = coord[1];
                PlayerStartingData player = nextPlayer.get();
                if (x == player.x && y == player.y) {
                    // Handling the next player starting position.
                    PlayerData playerData = players[player.index];
                    block.setFill(playerData.getColor());
                    playerData.newGame(player.direction, x, y);
                    nextPlayer.setValue(findNextPlayerStartingPosition(x, y, gamePlayersStartingData));
                    return;
                }
            }

            block.setFill(BLOCK_COLOR);
        });

        // Create a loop that willdisplay the countdown for the new game
        countdownValue = DigitalDigits.values().length;
        countdownFrameHandler.handle(null);
        Timeline countdownTimer = new Timeline(new KeyFrame(Duration.millis(1000), e -> {
            countdownFrameHandler.handle(e);
            if (countdownValue == 0) {
                countdownCallback.run();
            }
        }));
        countdownTimer.setCycleCount(DigitalDigits.values().length - 1);
        countdownTimer.play();
    }

    /**
     * Move a player one block in its facing direction.
     * 
     * @param player The player to move.
     * @return boolean True if the player is still alive and false if it just crashed on another player or the boundary.
     */
    public boolean movePlayer(PlayerData player) {
        int[] position = player.move();
        if (isOutOfBound(position[0]) || isOutOfBound(position[1])) {
            // Out of bound
            player.setAlive(false);
        } else {
            Rectangle block = getBlock(position[0], position[1]);
            if (!block.getFill().equals(BLOCK_COLOR)) {
                // Crash
                player.setAlive(false);
            } else {
                block.setFill(player.getColor());
            }
        }

        return player.isAlive();
    }

    /**
     * Retrieve the block at given coordinates.
     * 
     * @param x The X value.
     * @param y The Y value.
     * @return Rectangle The block.
     * @throws IndexOutOfBoundsException If the coordinates are out of the game area as there's no check for that.
     */
    private Rectangle getBlock(int x, int y) {
        return getBlock(getRow(y), x);
    }

    /**
     * Retrieve the block for a given row and X coordinate.
     * 
     * @param row The row.
     * @param y The Y value.
     * @return Rectangle The block.
     * @throws IndexOutOfBoundsException If the coordinates are out of the game area as there's no check for that.
     */
    private Rectangle getBlock(TilePane row, int x) {
        return (Rectangle) row.getChildren().get(x);
    }

    /**
     * Retrieve a given row of the game area.
     * 
     * @param index The row index in the tile pane (i.e. the Y value).
     * @return TilePane The row tile pane.
     * @throws IndexOutOfBoundsException If the row is out of the game area as there's no check for that.
     */
    private TilePane getRow(int index) {
        return (TilePane) super.getChildren().get(index);
    }

    /**
     * Check if a given X or Y position is out of bound.
     * 
     * @param position The X or Y position to check.
     * @return boolean True if out of bound and false otherwise.
     */
    private boolean isOutOfBound(int position) {
        return position < 0 || position >= blocksPerRow;
    }

    /**
     * Navigate over each block of the game area to perform a given action on them.
     * 
     * @param blockConsumer The consumer of the coordinates and block.
     */
    private void forEachBlock(BiConsumer<int[], Rectangle> blockConsumer) {
        for (int y = 0; y < super.getChildren().size(); y++) {
            TilePane row = getRow(y);
            for (int x = 0; x < row.getChildren().size(); x++) {
                blockConsumer.accept(new int[] { x, y }, getBlock(row, x));
            }
        }
    }

    /**
     * Finds the next starting player based on given coordinates.
     * 
     * @param x The X value.
     * @param y The Y value.
     * @param playersStartingData The players starting information.
     * @return PlayerStartingData The next player that will be encountered when navigating the game area.
     */
    private static PlayerStartingData findNextPlayerStartingPosition(int x, int y,
            PlayerStartingData[] playersStartingData) {

        for (int i = 0; i < playersStartingData.length; i++) {
            PlayerStartingData playerStartingData = playersStartingData[i];
            // Navigating through the game area is column first.
            if (playerStartingData.y > y || playerStartingData.y == y && playerStartingData.x > x) {
                return playerStartingData;
            }
        }

        // No more player
        return null;
    }

    private static int evenValue(int value) {
        return value % 2 == 0 ? value : value + 1;
    }

    @RequiredArgsConstructor
    private static final class PlayerStartingData implements Comparable<PlayerStartingData> {
        private final Direction direction;
        private final int x;
        private final int y;
        private final int index;

        @Override
        public int compareTo(PlayerStartingData other) {
            if (y == other.y) {
                return Integer.compare(x, other.x);
            }

            return Integer.compare(y, other.y);
        }
    }
}
