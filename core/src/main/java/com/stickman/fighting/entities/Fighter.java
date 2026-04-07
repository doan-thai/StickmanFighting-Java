package com.stickman.fighting.entities;

import com.stickman.fighting.particles.ParticleEffect;
import com.stickman.fighting.particles.ParticleSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.stickman.fighting.utils.Constants;
import com.stickman.fighting.utils.SoundManager;

public abstract class Fighter {

    public enum AnimState {
        IDLE, WALK, JUMP, ATTACK, HIT, DEAD
    }

    public enum AttackType {
        PUNCH, KICK, ENERGY
    }

    protected Vector2 position;
    protected Vector2 velocity;
    protected boolean onGround;
    protected boolean facingRight;

    public static final float WIDTH = 50f;
    public static final float HEIGHT = 120f;

    protected Rectangle bounds;
    protected Rectangle attackBox;

    protected float hp;
    protected float maxHp;
    protected float attackCooldown;
    protected boolean isAttacking;
    protected float attackTimer;
    protected static final float ATTACK_DURATION = 0.25f;
    protected AttackType currentAttackType;
    protected boolean isBlocking;
    protected float dashCooldown;
    protected float knockdownTimer;
    protected int consecutiveHitsReceived;
    protected boolean hasHitTarget; // MỚI: Tránh ngắt animation giữa chừng khi đã trúng đòn

    protected AnimState animState;
    protected float animTimer;
    protected float hitFlashTimer;

    protected Color bodyColor;

    public Fighter(float startX, float startY, Color color) {
        position = new Vector2(startX, startY);
        velocity = new Vector2(0, 0);
        onGround = false;
        facingRight = true;

        bounds = new Rectangle(startX, startY, WIDTH, HEIGHT);
        attackBox = new Rectangle(0, 0, 0, 0);

        maxHp = Constants.MAX_HP;
        this.hp = this.maxHp;
        animState = AnimState.IDLE;
        bodyColor = color;
        currentAttackType = AttackType.PUNCH;
        isBlocking = false;
        dashCooldown = 0f;
        knockdownTimer = 0f;
        consecutiveHitsReceived = 0;
        hasHitTarget = false;
    }

    public void update(float delta) {
        boolean wasOnGround = onGround;

        velocity.y += Constants.GRAVITY * delta;
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        if (position.y <= Constants.GROUND_Y) {
            position.y = Constants.GROUND_Y;
            velocity.y = 0f;
            onGround = true;

            if (!wasOnGround && !isAttacking) {
                setAnimState(AnimState.IDLE);
            }
            if (!wasOnGround && onGround) {
                float dustX = position.x + WIDTH / 2f;
                ParticleSystem.getInstance().emit(ParticleEffect.EffectType.DUST_LAND, dustX, position.y);
            }
        } else {
            onGround = false;
        }

        final float leftLimit = 20f;
        final float rightLimit = Constants.SCREEN_WIDTH - WIDTH - 20f;
        if (position.x < leftLimit) {
            position.x = leftLimit;
            velocity.x = 0f;
        }
        if (position.x > rightLimit) {
            position.x = rightLimit;
            velocity.x = 0f;
        }

        bounds.setPosition(position.x, position.y);

        if (attackCooldown > 0f) attackCooldown -= delta;
        if (dashCooldown > 0f) dashCooldown -= delta;
        if (knockdownTimer > 0f) {
            knockdownTimer -= delta;
            if (knockdownTimer <= 0f && onGround && !isAttacking) {
                setAnimState(AnimState.IDLE);
            }
        }

        if (isAttacking) {
            attackTimer -= delta;
            if (attackTimer <= 0f) {
                isAttacking = false;
                hasHitTarget = false;
                attackBox.set(0f, 0f, 0f, 0f);
                if (onGround) setAnimState(AnimState.IDLE);
                else setAnimState(AnimState.JUMP);
            }
        }

        if (hitFlashTimer > 0f) hitFlashTimer -= delta;

        if (animState == AnimState.WALK || animState == AnimState.IDLE) {
            animTimer += delta;
        }

        if (knockdownTimer > 0f) {
            stopHorizontal();
            isBlocking = false;
            return;
        }
        updateLogic(delta);
    }

    protected abstract void updateLogic(float delta);

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
            onGround = false;
            setAnimState(AnimState.JUMP);
            SoundManager.getInstance().playSound(SoundManager.SoundEffect.JUMP);

            float puffX = position.x + WIDTH / 2f;
            float puffY = position.y;
            ParticleSystem.getInstance().emit(ParticleEffect.EffectType.JUMP_PUFF, puffX, puffY);
        }
    }

    public boolean punch() { return attack(AttackType.PUNCH); }
    public boolean kick() { return attack(AttackType.KICK); }
    public boolean energySkill() { return attack(AttackType.ENERGY); }

    public boolean attack(AttackType type) {
        // KHÔI PHỤC GIỚI HẠN: Bắt buộc phải chờ hết attackCooldown và không đang đánh dở
        if (attackCooldown > 0 || isAttacking || knockdownTimer > 0f) return false;

        if (isBlocking) isBlocking = false;

        currentAttackType = type;
        isAttacking = true;
        attackTimer = ATTACK_DURATION;
        hasHitTarget = false; // Bắt đầu chiêu mới, reset va chạm

        // Thiết lập thời gian hồi chiêu dựa theo loại đòn tấn công
        attackCooldown = switch (type) {
            case PUNCH -> Constants.PUNCH_COOLDOWN;
            case KICK -> Constants.KICK_COOLDOWN;
            case ENERGY -> Constants.ENERGY_COOLDOWN;
        };
        setAnimState(AnimState.ATTACK);

        float boxW = Constants.ATTACK_RANGE;
        float boxH = HEIGHT * 0.5f;
        if (type == AttackType.KICK) boxW += 15f;
        else if (type == AttackType.ENERGY) { boxW += 28f; boxH += 8f; }

        float boxX = facingRight ? position.x + WIDTH : position.x - boxW;
        float boxY = position.y + HEIGHT * 0.25f;
        attackBox.set(boxX, boxY, boxW, boxH);
        return true;
    }

    public boolean checkHit(Fighter target) {
        if (!isAttacking || hasHitTarget) return false;

        if (attackBox.overlaps(target.bounds)) {
            target.receiveHit(getCurrentAttackDamage());

            // ĐÃ XÓA TIA LỬA KHI ĐÁNH TRÚNG (Bỏ ParticleSystem)

            // Tính toán lực đẩy lùi (Knock-back)
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

            // Đánh dấu đã đánh trúng
            hasHitTarget = true;
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
        SoundManager.getInstance().playSoundWithVariation(SoundManager.SoundEffect.HIT_RECEIVE, 1.0f);
    }

    public void setBlocking(boolean blocking) {
        if (knockdownTimer > 0f || isAttacking) {
            this.isBlocking = false;
            return;
        }
        this.isBlocking = blocking;
        if (blocking) stopHorizontal();
    }

    public boolean isBlocking() { return isBlocking; }

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
        float clamped = Math.max(Constants.HP_SCALE_MIN, Math.min(Constants.HP_SCALE_MAX, scale));
        maxHp = Constants.MAX_HP * clamped;
        hp = maxHp;
    }

    private float getCurrentAttackDamage() {
        return switch (currentAttackType) {
            case PUNCH -> Constants.PUNCH_DAMAGE;
            case KICK -> Constants.KICK_DAMAGE;
            case ENERGY -> Constants.ENERGY_DAMAGE;
        };
    }

    public boolean isDead() { return hp <= 0; }

    private static final float LINE_THICKNESS = 6f;

    public void renderFilled(ShapeRenderer sr) {
        Color renderColor = getEffectiveColor();
        sr.setColor(renderColor);

        float cx = position.x + WIDTH / 2f;
        float by = position.y;
        float headR = 15f;
        float headY = by + HEIGHT - headR;

        sr.circle(cx, headY, headR);
    }

    public void renderLines(ShapeRenderer sr) {
        Color renderColor = getEffectiveColor();
        sr.setColor(renderColor);

        float cx = position.x + WIDTH / 2f;
        float by = position.y;
        float headR = 15f;
        float headY = by + HEIGHT - headR;
        float neckY = headY - headR + 2f;

        float hipY = by + HEIGHT * 0.38f;
        float handY = by + HEIGHT * 0.72f;

        float legSwing = 0f;
        if (animState == AnimState.WALK)
            legSwing = (float) Math.sin(animTimer * 10f) * 25f;
        else if (animState == AnimState.JUMP)
            legSwing = -20f;

        // MỚI: Tính toán mức độ hoàn thành của đòn đánh (Sine Wave 0 -> 1 -> 0)
        // Dùng để tạo độ co duỗi tự nhiên của cơ thể
        float p = 0f;
        if (animState == AnimState.ATTACK) {
            float progress = 1f - (attackTimer / ATTACK_DURATION);
            p = (float) Math.sin(progress * Math.PI);
        }

        // 1. GÓC MẶC ĐỊNH LÚC ĐỨNG YÊN
        float armAngleL = 225f - legSwing * 0.6f;
        float armAngleR = 315f + legSwing * 0.6f;
        float legAngleL = 240f + legSwing;
        float legAngleR = 300f - legSwing;

        // Offset linh hoạt của thân
        float modCx = cx;
        float modNeckY = neckY;
        float modHipY = hipY;

        // 2. TƯ THẾ BLOCK
        if (isBlocking) {
            if (facingRight) {
                armAngleL = 70f;
                armAngleR = 45f;
            } else {
                armAngleL = 135f;
                armAngleR = 110f;
            }
        }
        // 3. TƯ THẾ TẤN CÔNG ĐỘNG (Đã làm gọn lại theo ý bạn)
        else if (animState == AnimState.ATTACK) {
            if (currentAttackType == AttackType.PUNCH) {
                if (facingRight) {
                    // Chỉ vung tay phải thẳng ra (360 độ)
                    armAngleR = 315f + 45f * p;
                    // Tay trái và toàn bộ thân dưới giữ nguyên
                } else {
                    // Chỉ vung tay trái thẳng ra (180 độ)
                    armAngleL = 225f - 45f * p;
                    // Tay phải và toàn bộ thân dưới giữ nguyên
                }
            }
            else if (currentAttackType == AttackType.KICK) {
                if (facingRight) {
                    // Chỉ vung chân phải lên cao
                    legAngleR = 300f + 80f * p;
                    // Chân trái và hai tay giữ nguyên như lúc đứng/đi
                } else {
                    // Chỉ vung chân trái lên cao
                    legAngleL = 240f - 80f * p;
                    // Chân phải và hai tay giữ nguyên
                }
            }
            else if (currentAttackType == AttackType.ENERGY) {
                if (facingRight) {
                    armAngleR = 315f + 45f * p;
                    armAngleL = 225f + 135f * p;
                } else {
                    armAngleL = 225f - 45f * p;
                    armAngleR = 315f - 135f * p;
                }
            }
        }
        // 4. TƯ THẾ BỊ HIT
        else if (animState == AnimState.HIT) {
            armAngleL = facingRight ? 200f : 340f;
            armAngleR = facingRight ? 200f : 340f;
            legAngleL = 250f;
            legAngleR = 290f;
            modCx -= (facingRight ? 5f : -5f);
        }

        // --- VẼ LÊN MÀN HÌNH ---
        // Vẽ Thân
        sr.rectLine(modCx, modNeckY, modCx, modHipY, LINE_THICKNESS);

        // Vẽ Tay
        float armLen = 40f;
        float armLX = modCx + (float) Math.cos(Math.toRadians(armAngleL)) * armLen;
        float armLY = handY + (float) Math.sin(Math.toRadians(armAngleL)) * armLen;
        sr.rectLine(modCx, handY, armLX, armLY, LINE_THICKNESS);

        float armRX = modCx + (float) Math.cos(Math.toRadians(armAngleR)) * armLen;
        float armRY = handY + (float) Math.sin(Math.toRadians(armAngleR)) * armLen;
        sr.rectLine(modCx, handY, armRX, armRY, LINE_THICKNESS);

        // Vẽ Chân
        float legLen = HEIGHT * 0.42f;
        float legLX = modCx + (float) Math.cos(Math.toRadians(legAngleL)) * legLen;
        float legLY = modHipY + (float) Math.sin(Math.toRadians(legAngleL)) * legLen;
        sr.rectLine(modCx, modHipY, legLX, legLY, LINE_THICKNESS);

        float legRX = modCx + (float) Math.cos(Math.toRadians(legAngleR)) * legLen;
        float legRY = modHipY + (float) Math.sin(Math.toRadians(legAngleR)) * legLen;
        sr.rectLine(modCx, modHipY, legRX, legRY, LINE_THICKNESS);
    }

    private Color getEffectiveColor() {
        if (hitFlashTimer > 0f && (int) (hitFlashTimer * 20) % 2 == 0) {
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

    public float getHp() { return hp; }
    public float getMaxHp() { return maxHp; }
    public float getHpPercent() { return hp / maxHp; }
    public Rectangle getBounds() { return bounds; }
    public Vector2 getPosition() { return position; }
    public boolean isFacingRight() { return facingRight; }
    public boolean isOnGround() { return onGround; }
    public boolean isAttacking() { return isAttacking; }
    public AttackType getCurrentAttackType() { return currentAttackType; }
    public float getCenterX() { return position.x + WIDTH / 2f; }
    public float getCenterY() { return position.y + HEIGHT / 2f; }

    public void setFacingRight(boolean faceRight) {
        this.facingRight = faceRight;
    }

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

    public void reset(float startX, float startY, boolean faceRight) {
        position.set(startX, startY);
        velocity.set(0, 0);
        hp = maxHp;
        isAttacking = false;
        attackCooldown = 0;
        attackTimer = 0;
        hitFlashTimer = 0;
        currentAttackType = AttackType.PUNCH;
        isBlocking = false;
        dashCooldown = 0f;
        knockdownTimer = 0f;
        consecutiveHitsReceived = 0;
        facingRight = faceRight;
        onGround = false;
        hasHitTarget = false;
        setAnimState(AnimState.IDLE);
    }
}
