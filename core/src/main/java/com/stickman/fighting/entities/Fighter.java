package com.stickman.fighting.entities;

import com.stickman.fighting.particles.ParticleEffect;
import com.stickman.fighting.particles.ParticleSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.stickman.fighting.utils.Constants;
import com.stickman.fighting.utils.SoundManager;

/**
 * Lớp cha trừu tượng cho mọi nhân vật chiến đấu.
 *
 * Chứa:
 *  - Vật lý thủ công: trọng lực, velocity, vị trí
 *  - Bounding box AABB cho va chạm (com.badlogic.gdx.math.Rectangle)
 *  - Hệ thống HP, damage, cooldown
 *  - Render stickman bằng ShapeRenderer (không cần sprite)
 *  - Trạng thái animation đơn giản (IDLE, WALK, JUMP, ATTACK, HIT, DEAD)
 */
public abstract class Fighter {

    // ── Trạng thái Animation ──────────────────────────────────────────────────
    public enum AnimState {
        IDLE, WALK, JUMP, ATTACK, HIT, DEAD
    }

    public enum AttackType {
        PUNCH, KICK, ENERGY
    }

    // ── Vị trí & Vật lý ──────────────────────────────────────────────────────
    protected Vector2 position;   // Góc dưới-trái của bounding box
    protected Vector2 velocity;
    protected boolean onGround;
    protected boolean facingRight; // Hướng nhân vật nhìn

    // ── Kích thước nhân vật ───────────────────────────────────────────────────
    public static final float WIDTH  = 40f;
    public static final float HEIGHT = 80f;

    // ── Bounding Box ──────────────────────────────────────────────────────────
    protected Rectangle bounds;      // Thân (body) – dùng cho va chạm push
    protected Rectangle attackBox;   // Hitbox tấn công (chỉ active khi attack)

    // ── Chiến đấu ─────────────────────────────────────────────────────────────
    protected float hp;
    protected float maxHp;
    protected float attackCooldown; // Thời gian còn lại trước khi attack tiếp
    protected boolean isAttacking;
    protected float attackTimer;    // Thời gian animation attack còn lại
    protected static final float ATTACK_DURATION = 0.25f; // giây
    protected AttackType currentAttackType;
    protected boolean isBlocking;
    protected float dashCooldown;
    protected float knockdownTimer;
    protected int consecutiveHitsReceived;

    // ── Animation ─────────────────────────────────────────────────────────────
    protected AnimState animState;
    protected float     animTimer;  // Dùng cho bộ đếm animation (walk cycle...)
    protected float     hitFlashTimer; // Nhấp nháy khi bị đánh

    // ── Màu sắc ───────────────────────────────────────────────────────────────
    protected Color bodyColor;

    // ── Constructor ───────────────────────────────────────────────────────────
    public Fighter(float startX, float startY, Color color) {
        position    = new Vector2(startX, startY);
        velocity    = new Vector2(0, 0);
        onGround    = false;
        facingRight = true;

        bounds     = new Rectangle(startX, startY, WIDTH, HEIGHT);
        attackBox  = new Rectangle(0, 0, 0, 0); // Vô hiệu hóa mặc định

        maxHp      = Constants.MAX_HP;
        hp         = maxHp;
        animState  = AnimState.IDLE;
        bodyColor  = color;
        currentAttackType = AttackType.PUNCH;
        isBlocking = false;
        dashCooldown = 0f;
        knockdownTimer = 0f;
        consecutiveHitsReceived = 0;
    }

    // ── Update chính ──────────────────────────────────────────────────────────

    /**
     * Gọi mỗi frame. Xử lý:
     *  1. Trọng lực
     *  2. Di chuyển theo velocity
     *  3. Giới hạn biên màn hình
     *  4. Cooldown
     *  5. Cập nhật bounds & attackBox
     *  6. Logic con (subclass)
     */
    // Fighter.java — thay thế toàn bộ method update()
    public void update(float delta) {
        // 1. Lưu trạng thái đất của frame trước
        boolean wasOnGround = onGround;

        // 2. Áp dụng trọng lực TRƯỚC khi di chuyển
        velocity.y += Constants.GRAVITY * delta;

        // 3. Di chuyển
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        // 4. Kiểm tra chạm đất — phải check SAU khi di chuyển
        if (position.y <= Constants.GROUND_Y) {
            position.y  = Constants.GROUND_Y;
            velocity.y  = 0f;
            onGround    = true;

            // Nếu vừa đáp xuống đất → reset animation
            if (!wasOnGround && !isAttacking) {
                setAnimState(AnimState.IDLE);
            }
            if (!wasOnGround && onGround) {
                // Vừa đáp đất → bụi
                float dustX = position.x + WIDTH / 2f;
                ParticleSystem.getInstance().emit(
                    ParticleEffect.EffectType.DUST_LAND, dustX, position.y);
            }
        } else {
            onGround = false;
        }

        // 5. Giới hạn biên trái/phải
        final float leftLimit  = 20f;
        final float rightLimit = Constants.SCREEN_WIDTH - WIDTH - 20f;
        if (position.x < leftLimit)  { position.x = leftLimit;  velocity.x = 0f; }
        if (position.x > rightLimit) { position.x = rightLimit; velocity.x = 0f; }

        // 6. Cập nhật bounds
        bounds.setPosition(position.x, position.y);

        // 7. Cooldown tấn công
        if (attackCooldown > 0f) attackCooldown -= delta;
        if (dashCooldown > 0f) dashCooldown -= delta;
        if (knockdownTimer > 0f) {
            knockdownTimer -= delta;
            if (knockdownTimer <= 0f && onGround && !isAttacking) {
                setAnimState(AnimState.IDLE);
            }
        }

        // 8. Xử lý animation attack
        if (isAttacking) {
            attackTimer -= delta;
            if (attackTimer <= 0f) {
                isAttacking = false;
                attackBox.set(0f, 0f, 0f, 0f);
                if (onGround) setAnimState(AnimState.IDLE);
                else          setAnimState(AnimState.JUMP);
            }
        }

        // 9. Hit flash timer
        if (hitFlashTimer > 0f) hitFlashTimer -= delta;

        // 10. Walk animation timer
        if (animState == AnimState.WALK || animState == AnimState.IDLE) {
            animTimer += delta;
        }

        // 11. Logic subclass (input / AI)
        if (knockdownTimer > 0f) {
            stopHorizontal();
            isBlocking = false;
            return;
        }
        updateLogic(delta);
    }

    /** Subclass ghi đè để thêm logic riêng (input / AI) */
    protected abstract void updateLogic(float delta);

    // ── Hành động ─────────────────────────────────────────────────────────────

    public void moveLeft(float delta) {
        if (knockdownTimer > 0f || isBlocking) return;
        velocity.x = -Constants.PLAYER_SPEED;
        facingRight = false;
        if (onGround && !isAttacking) setAnimState(AnimState.WALK);
    }

    public void moveRight(float delta) {
        if (knockdownTimer > 0f || isBlocking) return;
        velocity.x = Constants.PLAYER_SPEED;
        facingRight = true;
        if (onGround && !isAttacking) setAnimState(AnimState.WALK);
    }

    public void stopHorizontal() {
        velocity.x = 0;
        if (onGround && !isAttacking) setAnimState(AnimState.IDLE);
    }

    public void jump() {
        if (onGround && knockdownTimer <= 0f) {
            velocity.y = Constants.JUMP_VELOCITY;
            onGround   = false;
            setAnimState(AnimState.JUMP);
            SoundManager.getInstance().playSound(SoundManager.SoundEffect.JUMP);

            // Phát bụi nhảy
            float puffX = position.x + WIDTH / 2f;
            float puffY = position.y;
            ParticleSystem.getInstance().emit(
                ParticleEffect.EffectType.JUMP_PUFF, puffX, puffY);
        }
    }

    /**
     * Kích hoạt đòn tấn công.
     * Tạo attackBox phía trước mặt nhân vật.
     */
    public boolean attack() {
        return attack(AttackType.PUNCH);
    }

    public boolean punch() {
        return attack(AttackType.PUNCH);
    }

    public boolean kick() {
        return attack(AttackType.KICK);
    }

    public boolean energySkill() {
        return attack(AttackType.ENERGY);
    }

    public boolean attack(AttackType type) {
        if (attackCooldown > 0 || isAttacking || knockdownTimer > 0f) return false;
        if (isBlocking) isBlocking = false;

        currentAttackType = type;
        isAttacking = true;
        attackTimer = ATTACK_DURATION;
        attackCooldown = switch (type) {
            case PUNCH -> Constants.PUNCH_COOLDOWN;
            case KICK -> Constants.KICK_COOLDOWN;
            case ENERGY -> Constants.ENERGY_COOLDOWN;
        };
        setAnimState(AnimState.ATTACK);

        float boxW = Constants.ATTACK_RANGE;
        float boxH = HEIGHT * 0.5f;
        if (type == AttackType.KICK) {
            boxW += 15f;
        } else if (type == AttackType.ENERGY) {
            boxW += 28f;
            boxH += 8f;
        }

        float boxX = facingRight ? position.x + WIDTH : position.x - boxW;
        float boxY = position.y + HEIGHT * 0.25f;
        attackBox.set(boxX, boxY, boxW, boxH);
        return true;
    }

    /**
     * Kiểm tra hitbox của fighter này có trúng target không.
     * Nếu trúng → gây damage và kích hoạt hiệu ứng hit cho target.
     */
    public boolean checkHit(Fighter target) {
        if (!isAttacking) return false;
        if (attackBox.overlaps(target.bounds)) {
            target.receiveHit(getCurrentAttackDamage());

            // Tính điểm va chạm (giữa hai nhân vật)
            float hitX = (getCenterX() + target.getCenterX()) / 2f;
            float hitY = target.getCenterY() + 10f;

            // Phát hiệu ứng combo (spark + impact + blood)
            ParticleSystem.getInstance().emitHitCombo(hitX, hitY);

            // Knock-back
            float knockDir = facingRight ? 1f : -1f;
            float kbX = switch (currentAttackType) {
                case PUNCH -> 150f;
                case KICK -> 200f;
                case ENERGY -> 240f;
            };
            float kbY = switch (currentAttackType) {
                case PUNCH -> 95f;
                case KICK -> 130f;
                case ENERGY -> 160f;
            };
            target.velocity.x = knockDir * kbX;
            target.velocity.y = kbY;

            // Mỗi đòn chỉ trúng 1 lần.
            isAttacking = false;
            attackTimer = 0f;
            attackBox.set(0f, 0f, 0f, 0f);
            return true;
        }
        return false;
    }

    public void receiveHit(float damage) {
        float finalDamage = isBlocking ? damage * Constants.BLOCK_DAMAGE_FACTOR : damage;
        hp = Math.max(0f, hp - finalDamage);
        consecutiveHitsReceived = isBlocking ? 0 : (consecutiveHitsReceived + 1);

        if (consecutiveHitsReceived >= Constants.KNOCKDOWN_HIT_COUNT) {
            knockdownTimer = Constants.KNOCKDOWN_TIME;
            velocity.y = Math.max(velocity.y, 180f);
            consecutiveHitsReceived = 0;
        }

        isBlocking = false;
        hitFlashTimer = 0.15f;
        setAnimState(AnimState.HIT);
        SoundManager.getInstance()
            .playSoundWithVariation(SoundManager.SoundEffect.HIT_RECEIVE, 1.0f);
    }

    public void setBlocking(boolean blocking) {
        if (knockdownTimer > 0f || isAttacking) {
            this.isBlocking = false;
            return;
        }
        this.isBlocking = blocking;
        if (blocking) stopHorizontal();
    }

    public boolean isBlocking() {
        return isBlocking;
    }

    public boolean dash() {
        if (dashCooldown > 0f || knockdownTimer > 0f) return false;
        float dir = facingRight ? 1f : -1f;
        offsetX(dir * Constants.DASH_DISTANCE);
        float leftLimit = 20f;
        float rightLimit = Constants.SCREEN_WIDTH - WIDTH - 20f;
        if (position.x < leftLimit) position.x = leftLimit;
        if (position.x > rightLimit) position.x = rightLimit;
        bounds.setPosition(position.x, position.y);
        dashCooldown = Constants.DASH_COOLDOWN;
        return true;
    }

    public void setMaxHpScale(float scale) {
        float clamped = Math.max(0.25f, Math.min(2.0f, scale));
        maxHp = Constants.MAX_HP * clamped;
        hp = Math.min(hp, maxHp);
    }

    private float getCurrentAttackDamage() {
        return switch (currentAttackType) {
            case PUNCH -> Constants.PUNCH_DAMAGE;
            case KICK -> Constants.KICK_DAMAGE;
            case ENERGY -> Constants.ENERGY_DAMAGE;
        };
    }

    public boolean isDead() {
        return hp <= 0;
    }

    // ── Render (ShapeRenderer) ────────────────────────────────────────────────

    /**
     * Vẽ stickman hoàn toàn bằng ShapeRenderer.
     * Gọi giữa shapeRenderer.begin(ShapeType.Line/Filled) ... end()
     *
     * Cấu trúc stickman:
     *   ○  ← Đầu (ellipse)
     *   │  ← Thân
     *  / \ ← Hai chân (góc thay đổi theo walk cycle)
     * /   \
     */
    // Fighter.java — XÓA method render() cũ, THÊM 2 method sau:

    /**
     * Vẽ phần Filled (đầu tròn).
     * Gọi giữa sr.begin(ShapeType.Filled) ... sr.end()
     */
    public void renderFilled(ShapeRenderer sr) {
        Color renderColor = getEffectiveColor();
        sr.setColor(renderColor);

        float cx    = position.x + WIDTH / 2f;
        float by    = position.y;
        float headR = 12f;
        float headY = by + HEIGHT - headR;

        sr.ellipse(cx - headR, headY, headR * 2f, headR * 2f);
    }

    /**
     * Vẽ phần Line (thân, tay, chân).
     * Gọi giữa sr.begin(ShapeType.Line) ... sr.end()
     */
    public void renderLines(ShapeRenderer sr) {
        Color renderColor = getEffectiveColor();
        sr.setColor(renderColor);

        float cx    = position.x + WIDTH / 2f;
        float by    = position.y;
        float headR = 12f;
        float headY = by + HEIGHT - headR;
        float neckY = headY - headR;
        float hipY  = by + HEIGHT * 0.40f;
        float handY = by + HEIGHT * 0.55f;

        // Góc swing
        float legSwing = 0f;
        if      (animState == AnimState.WALK) legSwing = (float)Math.sin(animTimer * 8f) * 18f;
        else if (animState == AnimState.JUMP) legSwing = -20f;

        float armSwing = (animState == AnimState.ATTACK)
            ? (facingRight ? 35f : -35f)
            : -legSwing * 0.5f;

        // Thân
        sr.line(cx, neckY, cx, hipY);

        // Tay trái
        float armLen  = 30f;
        float armLX   = (float)Math.cos(Math.toRadians(210 + armSwing)) * armLen;
        float armLY   = (float)Math.sin(Math.toRadians(210 + armSwing)) * armLen;
        sr.line(cx, handY, cx + armLX, handY + armLY);

        // Tay phải
        float armRX = (float)Math.cos(Math.toRadians(330 - armSwing)) * armLen;
        float armRY = (float)Math.sin(Math.toRadians(330 - armSwing)) * armLen;
        sr.line(cx, handY, cx + armRX, handY + armRY);

        // Chân trái
        float legLen = HEIGHT * 0.42f;
        float legLX  = (float)Math.cos(Math.toRadians(240 + legSwing)) * legLen;
        float legLY  = (float)Math.sin(Math.toRadians(240 + legSwing)) * legLen;
        sr.line(cx, hipY, cx + legLX, hipY + legLY);

        // Chân phải
        float legRX = (float)Math.cos(Math.toRadians(300 - legSwing)) * legLen;
        float legRY = (float)Math.sin(Math.toRadians(300 - legSwing)) * legLen;
        sr.line(cx, hipY, cx + legRX, hipY + legRY);
    }

    /** Màu thực tế sau khi áp dụng hit flash */
    private Color getEffectiveColor() {
        if (hitFlashTimer > 0f && (int)(hitFlashTimer * 20) % 2 == 0) {
            return Color.WHITE;
        }
        return bodyColor;
    }

    @SuppressWarnings("unused")
    private void debugRenderBounds(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(Color.YELLOW);
        sr.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        if (isAttacking) {
            sr.setColor(Color.RED);
            sr.rect(attackBox.x, attackBox.y, attackBox.width, attackBox.height);
        }
        sr.end();
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public float     getHp()         { return hp; }
    public float     getMaxHp()      { return maxHp; }
    public float     getHpPercent()  { return hp / maxHp; }
    public Rectangle getBounds()     { return bounds; }
    public Vector2   getPosition()   { return position; }
    public boolean   isFacingRight() { return facingRight; }
    public boolean   isOnGround()    { return onGround; }
    public boolean   isAttacking()   { return isAttacking; }
    public AttackType getCurrentAttackType() { return currentAttackType; }

    public float getCenterX() { return position.x + WIDTH / 2f; }
    public float getCenterY() { return position.y + HEIGHT / 2f; }

    /** Dùng cho lớp điều phối gameplay để xoay hướng nhìn mà không phá encapsulation. */
    public void setFacingRight(boolean faceRight) {
        this.facingRight = faceRight;
    }

    /** Dịch chuyển ngang thủ công (ví dụ tách va chạm thân) và sync lại bounds. */
    public void offsetX(float deltaX) {
        this.position.x += deltaX;
        this.bounds.setPosition(this.position.x, this.position.y);
    }

    protected void setAnimState(AnimState state) {
        if (this.animState != state) {
            this.animState = state;
            this.animTimer = 0f;
        }
    }

    /** Gọi khi bắt đầu round mới */
    public void reset(float startX, float startY, boolean faceRight) {
        position.set(startX, startY);
        velocity.set(0, 0);
        hp             = maxHp;
        isAttacking    = false;
        attackCooldown = 0;
        attackTimer    = 0;
        hitFlashTimer  = 0;
        currentAttackType = AttackType.PUNCH;
        isBlocking = false;
        dashCooldown = 0f;
        knockdownTimer = 0f;
        consecutiveHitsReceived = 0;
        facingRight    = faceRight;
        onGround       = false;
        setAnimState(AnimState.IDLE);
    }
}
