package com.stickman.fighting.map;

import com.badlogic.gdx.math.Rectangle;

public class Platform {
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final Rectangle bounds;

    public Platform(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
