package com.stickman.fighting.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.stickman.fighting.utils.Constants;

public class EnergyProjectile {

    private final Fighter owner;
    private final Rectangle bounds;
    private final float radius;
    private final float damage;
    private final float velocityX;

    private float x;
    private float y;
    private float lifeLeft;
    private boolean active;

    public EnergyProjectile(Fighter owner, float startX, float startY, boolean toRight, float damage) {
        this.owner = owner;
        this.x = startX;
        this.y = startY;
        this.radius = Constants.ENERGY_PROJECTILE_RADIUS;
        this.damage = damage;
        this.velocityX = (toRight ? 1f : -1f) * Constants.ENERGY_PROJECTILE_SPEED;
        this.lifeLeft = Constants.ENERGY_PROJECTILE_LIFETIME;
        this.active = true;
        this.bounds = new Rectangle(startX - radius, startY - radius, radius * 2f, radius * 2f);
    }

    public void update(float delta) {
        if (!active)
            return;

        x += velocityX * delta;
        lifeLeft -= delta;
        bounds.setPosition(x - radius, y - radius);

        if (lifeLeft <= 0f || x < -80f || x > Constants.SCREEN_WIDTH + 80f) {
            active = false;
        }
    }

    public void render(ShapeRenderer sr) {
        if (!active)
            return;

        sr.setColor(0.70f, 0.98f, 1.0f, 0.45f);
        sr.circle(x, y, radius + 6f);

        sr.setColor(0.45f, 0.90f, 1.0f, 1f);
        sr.circle(x, y, radius);
    }

    public boolean hits(Fighter target) {
        return active && bounds.overlaps(target.getBounds());
    }

    public float getX() {
        return x;
    }

    public Fighter getOwner() {
        return owner;
    }

    public float getDamage() {
        return damage;
    }

    public boolean isMovingRight() {
        return velocityX >= 0f;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }
}