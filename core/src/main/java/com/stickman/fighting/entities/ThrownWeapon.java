package com.stickman.fighting.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.stickman.fighting.utils.Constants;

public class ThrownWeapon {

    private final Fighter owner;
    private final Fighter target;
    private final Rectangle bounds;
    private final float radius;
    
    private Vector2 position;
    private Vector2 velocity;
    private float rotation;
    private boolean active;
    private float lifeLeft;

    public ThrownWeapon(Fighter owner, Fighter target, float startX, float startY) {
        this.owner = owner;
        this.target = target;
        this.position = new Vector2(startX, startY);
        this.velocity = new Vector2(owner.isFacingRight() ? 400f : -400f, 200f);
        this.radius = 15f;
        this.bounds = new Rectangle(startX - radius, startY - radius, radius * 2f, radius * 2f);
        this.active = true;
        this.rotation = 0f;
        this.lifeLeft = 5.0f; // Tồn tại lâu hơn một chút để đuổi kịp
    }

    public void update(float delta) {
        if (!active) return;

        // Logic đuổi mục tiêu (Homing)
        Vector2 targetPos = new Vector2(target.getCenterX(), target.getCenterY());
        Vector2 direction = targetPos.sub(position).nor();
        
        float speed = 550f;
        velocity.lerp(direction.scl(speed), 0.05f); // Bẻ lái dần dần về phía đối thủ

        position.add(velocity.x * delta, velocity.y * delta);
        bounds.setPosition(position.x - radius, position.y - radius);
        rotation += 720f * delta; // Xoay tròn khi bay

        lifeLeft -= delta;
        if (lifeLeft <= 0 || position.x < -100 || position.x > Constants.SCREEN_WIDTH + 100) {
            active = false;
        }
    }

    public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        if (!active) return;

        com.badlogic.gdx.graphics.g2d.TextureRegion sword = com.stickman.fighting.utils.WeaponRenderer.getSwordRegion();
        
        // Vẽ hiệu ứng rực rỡ (Glow)
        batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE);
        batch.setColor(1f, 0.7f, 0.1f, 0.4f);
        batch.draw(sword, position.x - 32, position.y - 16, 32, 16, 128, 32, 0.6f, 0.6f, rotation);
        batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Vẽ kiếm chính
        batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        batch.draw(sword, position.x - 32, position.y - 16, 32, 16, 128, 32, 0.5f, 0.5f, rotation);
    }

    public boolean hits(Fighter target) {
        return active && bounds.overlaps(target.getBounds());
    }

    public float getDamage() {
        // Gây 50% máu hiện tại của đối thủ, tối thiểu 40 sát thương
        return Math.max(40f, target.getHp() * 0.5f);
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }
    
    public Fighter getOwner() {
        return owner;
    }
}
