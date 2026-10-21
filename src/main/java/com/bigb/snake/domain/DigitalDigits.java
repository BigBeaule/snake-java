package com.bigb.snake.domain;

import lombok.RequiredArgsConstructor;

/**
 * Enumeration for the supported digits in the countdown digital clock.
 */
@RequiredArgsConstructor
public enum DigitalDigits {
    ZERO(new boolean[][] { //
            { false, false, false, false, false }, //
            { false, false, false, false, false }, //
            { false, false, false, false, false }, //
            { false, false, false, false, false }, //
            { false, false, false, false, false },//
    }),

    ONE(new boolean[][] { //
            { false, false, true, false, false }, //
            { false, true, true, false, false }, //
            { false, false, true, false, false }, //
            { false, false, true, false, false }, //
            { false, true, true, true, false },//
    }),

    TWO(new boolean[][] { //
            { true, true, true, true, true }, //
            { false, false, false, false, true }, //
            { true, true, true, true, true }, //
            { true, false, false, false, false }, //
            { true, true, true, true, true },//
    }),

    THREE(new boolean[][] { //
            { true, true, true, true, true }, //
            { false, false, false, false, true }, //
            { true, true, true, true, true }, //
            { false, false, false, false, true }, //
            { true, true, true, true, true },//
    });

    /**
     * The matrix reprensenting the illumination status of each pixel in the digit. There's probably a better way to come up
     * with that status on the fly, but that's enough in the context of this simple project.
     */
    private final boolean[][] pixelsMatrix;

    /**
     * Check if a given pixel is illuminated for this digit or not.
     * 
     * @param x The X value.
     * @param y The Y value.
     * @return True if that pixel should be illuminated and false otherwise.
     */
    public boolean isIlluminated(int x, int y) {
        return pixelsMatrix[x][y];
    }
}
