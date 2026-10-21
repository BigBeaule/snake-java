package com.bigb.snake.domain;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import lombok.Data;

/**
 * A player's data.
 */
@Data
public class PlayerData {
    /** The player's color. */
    private final Color color;

    /** The player's name. */
    private final String name;

    /** The player's key for a left turn. */
    private final KeyCode leftKey;

    /** The player's key for a right turn */
    private final KeyCode rightKey;

    /** The player's current direction. */
    private Direction direction;

    /** The player's current X value on the game area. */
    private int x;

    /** The player's current Y value on the game area. */
    private int y;

    /** Flag telling if the player is still alive. */
    private boolean alive;

    /** Bindable property for the player's score. */
    private IntegerProperty score = new SimpleIntegerProperty(0);

    /**
     * Setup a new game for that player.
     * 
     * @param direction The starting direction.
     * @param x         The starting X value.
     * @param y         The starting Y value.
     */
    public void newGame(Direction direction, int x, int y) {
        this.direction = direction;
        this.x = x;
        this.y = y;
        this.alive = true;
    }

    /**
     * Move the player one block in its current facing position.
     * 
     * @return int[] The coordinates of its new position.
     */
    public int[] move() {
        return switch (direction) {
        case UP -> new int[] { x, --y };
        case DOWN -> new int[] { x, ++y };
        case LEFT -> new int[] { --x, y };
        case RIGHT -> new int[] { ++x, y };
        };
    }

    /**
     * Turn the player left or right based on the given key and its current direction. For example, a player moving down will
     * start moving left on the screen when turning right.
     * 
     * @param key The pressed key.
     * @throws IllegalArgumentException When the given key does not correspond to the player left or right key.
     */
    public void turn(KeyCode key) {
        if (key != leftKey && key != rightKey) {
            throw new IllegalArgumentException("Player %s cannot trigger a turn with key %s".formatted(name, key.getName()));
        }

        boolean left = key == leftKey;
        this.direction = switch (direction) {
        case UP -> left ? Direction.LEFT : Direction.RIGHT;
        case DOWN -> left ? Direction.RIGHT : Direction.LEFT;
        case LEFT -> left ? Direction.DOWN : Direction.UP;
        case RIGHT -> left ? Direction.UP : Direction.DOWN;
        };
    }

    /**
     * Player won the game!
     */
    public void win() {
        score.set(score.get() + 1);
    }
}
