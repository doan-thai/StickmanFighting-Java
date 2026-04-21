package com.stickman.fighting.entities;

import com.stickman.fighting.particles.ParticleEffect;
import com.stickman.fighting.particles.ParticleSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.stickman.fighting.map.Platform;
import com.stickman.fighting.utils.Constants;
import com.stickman.fighting.utils.SoundManager;
import java.util.List;

public abstract class Fighter {

    public enum AnimState {
        IDLE, WALK, JUMP, ATTACK, HIT, DEAD
    }

    public enum AttackType {
        PUNCH, KICK, ENERGY, THROW_WEAPON
    }

    protected Vector2 position;
    protected Vector2 velocity;
    protected boolean onGround;
    protected boolean facingRight;

    public static final float WIDTH = 38f;
    public static final float HEIGHT = 90f;

    protected Rectangle bounds;
    protected Rectangle attackBox;

    protected float hp;
    protected float maxHp;
    protected float energy;
    protected float maxEnergy;
    protected float attackCooldown;
    protected boolean isAttacking;
    protected float attackTimer;
    protected static final float ATTACK_DURATION = 0.25f;
    protected AttackType currentAttackType;
    protected boolean isBlocking;
    protected float dashCooldown;
    protected float knockdownTimer;
    protected int consecutiveHitsReceived;
    protected boolean hasHitTarget; // TrÃ¡nh ngáº¯t animation giá»¯a chá»«ng khi Ä‘Ã£ trÃºng Ä‘Ã²n
    protected float stunTimer; // Hiá»‡u á»©ng "khá»±ng" sau khi bá»‹ Ä‘Ã¡ (Kick) â€” ngÄƒn di chuyá»ƒn & táº¥n
                               // cÃ´ng

    protected boolean hasWeapon = false;
    protected boolean weaponUsedThisMatch = false;

    protected AnimState animState;
    protected float animTimer;
    protected float hitFlashTimer;

    protected Color bodyColor;

    // Hệ thống Trail cho vũ khí
    protected static class WeaponTrail {
        float x, y, rotation;
        float life;
        boolean facingRight;
    }

    protected java.util.List<WeaponTrail> weaponTrails = new java.util.ArrayList<>();

    public Fighter(float startX, float startY, Color color) {
        position = new Vector2(startX, startY);
        velocity = new Vector2(0, 0);
        onGround = false;
        facingRight = true;

        bounds = new Rectangle(startX, startY, WIDTH, HEIGHT);
        attackBox = new Rectangle(0, 0, 0, 0);

        maxHp = Constants.MAX_HP;
        this.hp = this.maxHp;
        maxEnergy = 100f;
        this.energy = 50f; // Khởi đầu 50% để tránh OP
        animState = AnimState.IDLE;
        bodyColor = color;
        currentAttackType = AttackType.PUNCH;
        isBlocking = false;
        dashCooldown = 0f;
        knockdownTimer = 0f;
        consecutiveHitsReceived = 0;
        hasHitTarget = false;
    }

    // Lá»›p ná»™i bá»™ Ä‘á»ƒ lÆ°u trá»¯ tráº¡ng thÃ¡i bÃ³ng má» khi tá»‘c biáº¿n
    protected class DashGhost {
        float x, y;
        AnimState animState;
        float animTimer;
        float attackTimer;
        AttackType currentAttackType;
        boolean isBlocking;
        boolean facingRight;
        float lifeTime;
        float maxLifeTime = 0.35f;
    }

    protected java.util.List<DashGhost> dashGhosts = new java.util.ArrayList<>();

    public void update(float delta, List<Platform> platforms) {
        boolean wasOnGround = onGround;
        float prevY = position.y;

        velocity.y += Constants.GRAVITY * delta;
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        boolean landedOnPlatform = false;
        if (velocity.y < 0) {
            for (Platform plat : platforms) {
                float platTop = plat.getY() + plat.getHeight();
                if (prevY >= platTop - 0.1f) {
                    if (position.y <= platTop) {
                        if (position.x + WIDTH > plat.getX() && position.x < plat.getX() + plat.getWidth()) {
                            position.y = platTop;
                            velocity.y = 0f;
                            onGround = true;
                            landedOnPlatform = true;
                            break;
                        }
                    }
                }
            }
        }

        if (!landedOnPlatform && position.y <= Constants.GROUND_Y) {
            position.y = Constants.GROUND_Y;
            velocity.y = 0f;
            onGround = true;
            landedOnPlatform = true; // Use this variable to represent landing on anything
        } else if (!landedOnPlatform && position.y > Constants.GROUND_Y) {
            onGround = false;
        }

        if (landedOnPlatform) {
            if (!wasOnGround && !isAttacking) {
                setAnimState(AnimState.IDLE);
            }
            if (!wasOnGround) {
                float dustX = position.x + WIDTH / 2f;
                ParticleSystem.getInstance().emit(ParticleEffect.EffectType.DUST_LAND, dustX, position.y);
            }
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

        if (attackCooldown > 0f)
            attackCooldown -= delta;
        if (dashCooldown > 0f)
            dashCooldown -= delta;
        if (stunTimer > 0f)
            stunTimer -= delta;
        if (knockdownTimer > 0f) {
            knockdownTimer -= delta;
            if (knockdownTimer <= 0f && onGround && !isAttacking) {
                setAnimState(AnimState.IDLE);
            }
        }

        if (isAttacking) {
            attackTimer -= delta;

            // Cáº­p nháº­t vá»‹ trÃ­ attackBox theo nhÃ¢n váº­t
            float boxW = Constants.PUNCH_RANGE;
            float boxH = HEIGHT * 0.5f;
            if (currentAttackType == AttackType.KICK)
                boxW = Constants.KICK_RANGE;
            else if (currentAttackType == AttackType.ENERGY) {
                boxW = Constants.ATTACK_RANGE + 28f;
                boxH += 8f;
            }
            float boxX = facingRight ? position.x + WIDTH : position.x - boxW;
            float boxY = position.y + HEIGHT * 0.25f;
            attackBox.set(boxX, boxY, boxW, boxH);

            if (attackTimer <= 0f) {
                isAttacking = false;
                hasHitTarget = false;
                attackBox.set(0f, 0f, 0f, 0f);
                if (onGround)
                    setAnimState(AnimState.IDLE);
                else
                    setAnimState(AnimState.JUMP);
            }
        }

        if (hitFlashTimer > 0f)
            hitFlashTimer -= delta;

        if (animState == AnimState.WALK || animState == AnimState.IDLE) {
            animTimer += delta;
        }

        // Cáº­p nháº­t thá»i gian sá»‘ng cá»§a cÃ¡c bÃ³ng má»
        for (int i = dashGhosts.size() - 1; i >= 0; i--) {
            DashGhost g = dashGhosts.get(i);
            g.lifeTime += delta;
            if (g.lifeTime >= g.maxLifeTime) {
                dashGhosts.remove(i);
            }
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
        if (knockdownTimer > 0f || isBlocking || stunTimer > 0f)
            return;
        velocity.x = -Constants.PLAYER_SPEED;
        if (!isAttacking)
            facingRight = false;
        if (onGround && !isAttacking)
            setAnimState(AnimState.WALK);
    }

    public void moveRight(float delta) {
        if (knockdownTimer > 0f || isBlocking || stunTimer > 0f)
            return;
        velocity.x = Constants.PLAYER_SPEED;
        if (!isAttacking)
            facingRight = true;
        if (onGround && !isAttacking)
            setAnimState(AnimState.WALK);
    }

    public void stopHorizontal() {
        velocity.x = 0;
        if (onGround && !isAttacking)
            setAnimState(AnimState.IDLE);
    }

    public void jump() {
        if (onGround && knockdownTimer <= 0f && stunTimer <= 0f) {
            velocity.y = Constants.JUMP_VELOCITY;
            onGround = false;
            setAnimState(AnimState.JUMP);
            SoundManager.getInstance().playSound(SoundManager.SoundEffect.JUMP);

            float puffX = position.x + WIDTH / 2f;
            float puffY = position.y;
            ParticleSystem.getInstance().emit(ParticleEffect.EffectType.JUMP_PUFF, puffX, puffY);
        }
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

    public boolean throwWeapon() {
        if (!hasWeapon)
            return false;
        if (attack(AttackType.THROW_WEAPON)) {
            hasWeapon = false; // Mất luôn vũ khí sau khi ném
            return true;
        }
        return false;
    }

    public boolean attack(AttackType type) {
        // Có thể spam các chiêu miễn là con năng lượng (Bỏ check attackCooldown)
        if (isAttacking || knockdownTimer > 0f || stunTimer > 0f)
            return false;

        if (type == AttackType.ENERGY && energy < 20f) {
            return false;
        }

        if (isBlocking)
            isBlocking = false;

        currentAttackType = type;
        isAttacking = true;
        attackTimer = ATTACK_DURATION;
        hasHitTarget = false; // Bắt đầu chiêu mới, reset va chạm

        // Vẫn thiết lập cooldown để quản lý trạng thái, nhưng không chặn spam
        attackCooldown = switch (type) {
            case PUNCH -> Constants.PUNCH_COOLDOWN;
            case KICK -> Constants.KICK_COOLDOWN;
            case ENERGY, THROW_WEAPON -> Constants.ENERGY_COOLDOWN;
        };

        if (type == AttackType.ENERGY) {
            energy -= 20f;
        }

        setAnimState(AnimState.ATTACK);

        float boxW = Constants.PUNCH_RANGE;
        float boxH = HEIGHT * 0.5f;
        if (type == AttackType.KICK)
            boxW = Constants.KICK_RANGE;
        else if (type == AttackType.ENERGY) {
            boxW = Constants.ATTACK_RANGE + 28f;
            boxH += 8f;
        }

        float boxX = facingRight ? position.x + WIDTH : position.x - boxW;
        float boxY = position.y + HEIGHT * 0.25f;
        attackBox.set(boxX, boxY, boxW, boxH);
        return true;
    }

    public boolean checkHit(Fighter target) {
        if (!isAttacking || hasHitTarget)
            return false;
        if (currentAttackType == AttackType.ENERGY)
            return false;

        // Cháº·n trÃºng Ä‘Ã²n áº£o: má»¥c tiÃªu pháº£i á»Ÿ trÆ°á»›c máº·t vÃ  trong
        // táº§m ngang há»£p lá»‡.
        float dir = facingRight ? 1f : -1f;
        float toTarget = target.getCenterX() - getCenterX();
        if (toTarget * dir <= 0f)
            return false;

        float edgeDistance;
        if (facingRight) {
            edgeDistance = target.bounds.x - (bounds.x + bounds.width);
        } else {
            edgeDistance = bounds.x - (target.bounds.x + target.bounds.width);
        }

        float maxReach = switch (currentAttackType) {
            case PUNCH -> Constants.PUNCH_RANGE;
            case KICK -> Constants.KICK_RANGE;
            case ENERGY, THROW_WEAPON -> Constants.ATTACK_RANGE + 28f;
        };
        if (edgeDistance > maxReach)
            return false;

        if (attackBox.overlaps(target.bounds)) {
            target.receiveHit(getCurrentAttackDamage());

            // Ãp dá»¥ng hiá»‡u á»©ng "khá»±ng" (stun) Ä‘áº·c biá»‡t khi bá»‹ Ä‘Ã¡
            if (currentAttackType == AttackType.KICK) {
                target.applyStun(0.22f);
                recoverEnergy(12f);
            } else if (currentAttackType == AttackType.PUNCH) {
                recoverEnergy(10f);
            }

            // TÃ­nh toÃ¡n lá»±c Ä‘áº©y lÃ¹i (Knock-back)
            float knockDir = facingRight ? 1f : -1f;
            float kbX = switch (currentAttackType) {
                case PUNCH -> 150f;
                case KICK -> 200f;
                case ENERGY, THROW_WEAPON -> 240f;
            };
            float kbY = switch (currentAttackType) {
                case PUNCH -> 95f;
                case KICK -> 130f;
                case ENERGY, THROW_WEAPON -> 160f;
            };
            target.velocity.x = knockDir * kbX;
            target.velocity.y = kbY;

            // ÄÃ¡nh dáº¥u Ä‘Ã£ Ä‘Ã¡nh trÃºng
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

    /**
     * Nhận sát thương từ chiêu tối thượng (ví dụ: ném kiếm).
     * Nếu block, chỉ giảm 20% sát thương (nhận 80%).
     */
    public void receiveUltimateHit(float damage) {
        float finalDamage = isBlocking ? damage * 0.8f : damage;
        hp = Math.max(0f, hp - finalDamage);

        // Luôn bị khựng nhẹ khi dính chiêu này
        hitFlashTimer = 0.25f;
        setAnimState(AnimState.HIT);
        isBlocking = false;

        SoundManager.getInstance().playSoundWithVariation(SoundManager.SoundEffect.HIT_RECEIVE, 0.8f);
    }

    /**
     * Ãp dá»¥ng hiá»‡u á»©ng khá»±ng (stun) ngáº¯n sau khi bá»‹ Ä‘Ã¡.
     * Trong thá»i gian nÃ y nhÃ¢n váº­t khÃ´ng thá»ƒ di chuyá»ƒn hoáº·c táº¥n
     * cÃ´ng.
     * Knockdown cÃ³ Æ°u tiÃªn cao hÆ¡n (stun khÃ´ng ghi Ä‘Ã¨ knockdown).
     */
    public void applyStun(float duration) {
        if (knockdownTimer <= 0f) {
            stunTimer = Math.max(stunTimer, duration);
        }
    }

    /**
     * Tráº£ vá» true náº¿u nhÃ¢n váº­t Ä‘ang trong thá»i gian há»“i chiÃªu
     * (Ä‘Ã£ táº¥n cÃ´ng xong nhÆ°ng chÆ°a thá»ƒ táº¥n cÃ´ng láº¡i).
     * Há»¯u Ã­ch cho AI Ä‘á»ƒ phÃ¡t hiá»‡n sÆ¡ há»Ÿ cá»§a Ä‘á»‘i thá»§.
     */
    public boolean isInAttackCooldown() {
        return false;
    }

    public void setBlocking(boolean blocking) {
        if (knockdownTimer > 0f || isAttacking) {
            this.isBlocking = false;
            return;
        }
        this.isBlocking = blocking;
        if (blocking)
            stopHorizontal();
    }

    public boolean isBlocking() {
        return isBlocking;
    }

    public boolean dash() {
        if (knockdownTimer > 0f || stunTimer > 0f || energy < 10f)
            return false;

        energy -= 10f;

        float dir = facingRight ? 1f : -1f;
        float leftLimit = 20f;
        float rightLimit = Constants.SCREEN_WIDTH - WIDTH - 20f;

        float startX = position.x;
        float targetX = position.x + (dir * Constants.DASH_DISTANCE);

        if (targetX < leftLimit)
            targetX = leftLimit;
        if (targetX > rightLimit)
            targetX = rightLimit;

        float actualDist = targetX - startX;

        // Táº¡o cÃ¡c bÃ³ng má» dá»c theo Ä‘Æ°á»ng lÆ°á»›t
        if (Math.abs(actualDist) > 5f) {
            int numGhosts = 5;
            for (int i = 0; i < numGhosts; i++) {
                DashGhost ghost = new DashGhost();
                ghost.x = startX + (actualDist * ((float) i / numGhosts));
                ghost.y = position.y;
                ghost.animState = animState;
                ghost.animTimer = animTimer;
                ghost.attackTimer = attackTimer;
                ghost.currentAttackType = currentAttackType;
                ghost.isBlocking = isBlocking;
                ghost.facingRight = facingRight;
                // CÃ¡c bÃ³ng sinh ra á»Ÿ xa (i nhá») báº¯t Ä‘áº§u vá»›i lifeTime lá»›n hÆ¡n
                // Ä‘á»ƒ biáº¿n máº¥t nhanh hÆ¡n
                ghost.lifeTime = (numGhosts - 1 - i) * 0.05f;
                dashGhosts.add(ghost);
            }
        }

        position.x = targetX;
        bounds.setPosition(position.x, position.y);
        return true;
    }

    public void setMaxHpScale(float scale) {
        float clamped = Math.max(Constants.HP_SCALE_MIN, Math.min(Constants.HP_SCALE_MAX, scale));
        maxHp = Constants.MAX_HP * clamped;
        hp = maxHp;
    }

    private float getCurrentAttackDamage() {
        float damage = switch (currentAttackType) {
            case PUNCH -> Constants.PUNCH_DAMAGE;
            case KICK -> Constants.KICK_DAMAGE;
            case ENERGY, THROW_WEAPON -> Constants.ENERGY_DAMAGE;
        };
        if (currentAttackType == AttackType.PUNCH && hasWeapon) {
            damage *= 2f;
        }
        return damage;
    }

    public boolean hasWeapon() {
        return hasWeapon;
    }

    public void setHasWeapon(boolean hasWeapon) {
        this.hasWeapon = hasWeapon;
        if (hasWeapon) {
            this.weaponUsedThisMatch = true;
        }
    }

    public boolean isWeaponUsedThisMatch() {
        return weaponUsedThisMatch;
    }

    public boolean isDead() {
        return hp <= 0;
    }

    private static final float LINE_THICKNESS = 5f;

    public void renderFilled(ShapeRenderer sr) {
        // Render bÃ³ng má» phÃ­a dÆ°á»›i
        for (DashGhost g : dashGhosts) {
            if (g.lifeTime >= g.maxLifeTime)
                continue;
            float alpha = 1f - (g.lifeTime / g.maxLifeTime);
            alpha = Math.max(0f, Math.min(alpha * 0.45f, 0.45f));
            Color ghostColor = new Color(bodyColor.r, bodyColor.g, bodyColor.b, alpha);
            drawFilledAt(sr, g.x, g.y, g.facingRight, false, g.attackTimer, g.currentAttackType, ghostColor);
        }

        Color renderColor = getEffectiveColor();
        drawFilledAt(sr, position.x, position.y, facingRight, isAttacking, attackTimer, currentAttackType, renderColor);
    }

    private void drawFilledAt(ShapeRenderer sr, float px, float py, boolean fRight, boolean attacking, float attTimer,
            AttackType attType, Color color) {
        sr.setColor(color);
        float cx = px + WIDTH / 2f;
        float by = py;
        float headR = 12f;
        float headY = by + HEIGHT - headR;

        sr.circle(cx, headY, headR);

        if (attacking && attType == AttackType.ENERGY) {
            float progress = 1f - (attTimer / ATTACK_DURATION);
            float eased = (float) Math.sin(progress * Math.PI);
            float orbOffset = 36f + 42f * eased;
            float orbX = fRight ? (cx + orbOffset) : (cx - orbOffset);
            float orbY = py + HEIGHT * 0.62f;
            float orbR = 8f + 6f * eased;

            sr.setColor(0.45f, 0.90f, 1.0f, color.a);
            sr.circle(orbX, orbY, orbR);

            sr.setColor(0.75f, 0.98f, 1.0f, color.a * 0.55f);
            sr.circle(orbX, orbY, orbR + 4f);

            sr.setColor(color);
        }
    }

    public void renderLines(ShapeRenderer sr) {
        // Render bÃ³ng má»
        for (DashGhost g : dashGhosts) {
            if (g.lifeTime >= g.maxLifeTime)
                continue;
            float alpha = 1f - (g.lifeTime / g.maxLifeTime);
            alpha = Math.max(0f, Math.min(alpha * 0.45f, 0.45f));
            Color ghostColor = new Color(bodyColor.r, bodyColor.g, bodyColor.b, alpha);
            drawLinesAt(sr, g.x, g.y, g.facingRight, g.animState, g.animTimer, g.isBlocking, g.currentAttackType,
                    g.attackTimer, ghostColor);
        }

        Color renderColor = getEffectiveColor();
        drawLinesAt(sr, position.x, position.y, facingRight, animState, animTimer, isBlocking, currentAttackType,
                attackTimer, renderColor);
    }

    private void drawLinesAt(ShapeRenderer sr, float px, float py, boolean fRight, AnimState aState, float aTimer,
            boolean blocking, AttackType attType, float attTimer, Color color) {
        sr.setColor(color);

        float cx = px + WIDTH / 2f;
        float by = py;
        float headR = 12f;
        float headY = by + HEIGHT - headR;
        float neckY = headY - headR + 2f;

        // Vẽ đầu (Line/Wireframe cũng cần có đầu tròn)
        sr.circle(cx, headY, headR);

        float hipY = by + HEIGHT * 0.38f;
        float armBaseY = by + HEIGHT * 0.72f;

        float legSwing = 0f;
        if (aState == AnimState.WALK)
            legSwing = (float) Math.sin(aTimer * 10f) * 25f;
        else if (aState == AnimState.JUMP)
            legSwing = -20f;

        float p = 0f;
        if (aState == AnimState.ATTACK) {
            float progress = 1f - (attTimer / ATTACK_DURATION);
            p = (float) Math.sin(progress * Math.PI);
        }

        float armAngleL = 225f - legSwing * 0.6f;
        float armAngleR = 315f + legSwing * 0.6f;
        float legAngleL = 240f + legSwing;
        float legAngleR = 300f - legSwing;

        float modCx = cx;
        float modNeckY = neckY;
        float modHipY = hipY;

        if (blocking) {
            if (fRight) {
                armAngleL = 70f;
                armAngleR = 45f;
            } else {
                armAngleL = 135f;
                armAngleR = 110f;
            }
        } else if (aState == AnimState.ATTACK) {
            if (attType == AttackType.PUNCH) {
                if (fRight) {
                    armAngleR = 315f + 45f * p;
                } else {
                    armAngleL = 225f - 45f * p;
                }
            } else if (attType == AttackType.KICK) {
                if (fRight) {
                    legAngleR = 300f + 80f * p;
                } else {
                    legAngleL = 240f - 80f * p;
                }
            } else if (attType == AttackType.ENERGY) {
                if (fRight) {
                    armAngleR = 315f + 45f * p;
                    armAngleL = 225f + 135f * p;
                } else {
                    armAngleL = 225f - 45f * p;
                    armAngleR = 315f - 135f * p;
                }
            }
        } else if (aState == AnimState.HIT) {
            armAngleL = fRight ? 200f : 340f;
            armAngleR = fRight ? 200f : 340f;
            legAngleL = 250f;
            legAngleR = 290f;
            modCx -= (fRight ? 5f : -5f);
        }

        sr.rectLine(modCx, modNeckY, modCx, modHipY, LINE_THICKNESS);

        float armLen = 30f;
        float armLX = modCx + (float) Math.cos(Math.toRadians(armAngleL)) * armLen;
        float armLY = armBaseY + (float) Math.sin(Math.toRadians(armAngleL)) * armLen;
        sr.rectLine(modCx, armBaseY, armLX, armLY, LINE_THICKNESS);

        float armRX = modCx + (float) Math.cos(Math.toRadians(armAngleR)) * armLen;
        float armRY = armBaseY + (float) Math.sin(Math.toRadians(armAngleR)) * armLen;
        sr.rectLine(modCx, armBaseY, armRX, armRY, LINE_THICKNESS);

        float legLen = HEIGHT * 0.42f;
        float legLX = modCx + (float) Math.cos(Math.toRadians(legAngleL)) * legLen;
        float legLY = modHipY + (float) Math.sin(Math.toRadians(legAngleL)) * legLen;
        sr.rectLine(modCx, modHipY, legLX, legLY, LINE_THICKNESS);

        float legRX = modCx + (float) Math.cos(Math.toRadians(legAngleR)) * legLen;
        float legRY = modHipY + (float) Math.sin(Math.toRadians(legAngleR)) * legLen;
        sr.rectLine(modCx, modHipY, legRX, legRY, LINE_THICKNESS);
    }

    /**
     * Cập nhật vệt mờ của vũ khí (Trail)
     */
    protected void updateWeaponTrail(float delta, float handX, float handY, float angle) {
        if (!hasWeapon) {
            weaponTrails.clear();
            return;
        }

        // Tạo ghost mới nếu đang tấn công hoặc di chuyển nhanh
        if (isAttacking || velocity.len() > 100f) {
            WeaponTrail trail = new WeaponTrail();
            trail.x = handX;
            trail.y = handY;
            trail.rotation = angle;
            trail.life = 0.25f;
            trail.facingRight = facingRight;
            weaponTrails.add(trail);
        }

        // Cập nhật ghost cũ
        for (int i = weaponTrails.size() - 1; i >= 0; i--) {
            weaponTrails.get(i).life -= delta;
            if (weaponTrails.get(i).life <= 0)
                weaponTrails.remove(i);
        }
    }

    public void renderWeapon(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        if (!hasWeapon)
            return;

        // Tính toán vị trí tay để vẽ kiếm
        float cx = position.x + WIDTH / 2f;
        float armBaseY = position.y + HEIGHT * 0.72f;

        float legSwing = 0f;
        if (animState == AnimState.WALK)
            legSwing = (float) Math.sin(animTimer * 10f) * 25f;

        float armAngle = facingRight ? (315f + legSwing * 0.6f) : (225f - legSwing * 0.6f);

        if (isBlocking) {
            armAngle = facingRight ? 45f : 135f;
        } else if (isAttacking && currentAttackType == AttackType.PUNCH) {
            float progress = 1f - (attackTimer / ATTACK_DURATION);
            float p = (float) Math.sin(progress * Math.PI);
            armAngle = facingRight ? (315f + 45f * p) : (225f - 45f * p);
        }

        float armLen = 30f;
        float handX = cx + (float) Math.cos(Math.toRadians(armAngle)) * armLen;
        float handY = armBaseY + (float) Math.sin(Math.toRadians(armAngle)) * armLen;

        // Vẽ Trail
        updateWeaponTrail(com.badlogic.gdx.Gdx.graphics.getDeltaTime(), handX, handY, armAngle);
        com.badlogic.gdx.graphics.g2d.TextureRegion sword = com.stickman.fighting.utils.WeaponRenderer.getSwordRegion();

        for (WeaponTrail t : weaponTrails) {
            batch.setColor(1f, 1f, 1f, t.life * 2f);
            batch.draw(sword, t.x, t.y - 16, 0, 16, 128, 32, 0.45f, 0.45f, t.rotation);
        }

        // Vẽ Kiếm chính
        batch.setColor(Color.WHITE);
        batch.draw(sword, handX, handY - 16, 0, 16, 128, 32, 0.45f, 0.45f, armAngle);

        // Nếu đủ năng lượng ném, kiếm phát sáng nhẹ
        if (energy >= 99f) {
            batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE);
            batch.setColor(1f, 0.8f, 0.2f, 0.3f);
            batch.draw(sword, handX, handY - 16, 0, 16, 128, 32, 0.5f, 0.5f, armAngle);
            batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                    com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        }

        // Reset color so later draws (e.g. background next frame) are not
        // tinted/dimmed.
        batch.setColor(Color.WHITE);
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

    public float getHp() {
        return hp;
    }

    public float getMaxHp() {
        return maxHp;
    }

    public float getHpPercent() {
        return hp / maxHp;
    }

    public float getEnergyPercent() {
        return energy / maxEnergy;
    }

    public void recoverEnergy(float amount) {
        energy = Math.min(maxEnergy, energy + amount);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Vector2 getPosition() {
        return position;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public AttackType getCurrentAttackType() {
        return currentAttackType;
    }

    public float getCenterX() {
        return position.x + WIDTH / 2f;
    }

    public float getCenterY() {
        return position.y + HEIGHT / 2f;
    }

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
        energy = 50f; // Reset về 50% thay vì 100%
        isAttacking = false;
        attackCooldown = 0;
        attackTimer = 0;
        hitFlashTimer = 0;
        currentAttackType = AttackType.PUNCH;
        isBlocking = false;
        dashCooldown = 0f;
        knockdownTimer = 0f;
        stunTimer = 0f;
        consecutiveHitsReceived = 0;
        facingRight = faceRight;
        onGround = false;
        hasHitTarget = false;
        dashGhosts.clear();
        setAnimState(AnimState.IDLE);
    }
}
