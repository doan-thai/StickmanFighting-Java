package com.stickman.fighting.ai;

import com.stickman.fighting.entities.BotFighter;
import com.stickman.fighting.entities.Fighter;
import com.stickman.fighting.utils.Constants;

/**
 * ATTACK STATE — Bot tấn công khi đủ gần.
 *
 * Chuyển state khi:
 *  - dist > ATTACK_RANGE * 1.5f   → ChaseState  (target thoát ra)
 *  - bot.hp < EVADE_THRESHOLD     → EvadeState
 *
 * Behavior:
 *  - Liên tục gọi attack() (bị giới hạn bởi cooldown trong Fighter)
 *  - Dịch chuyển nhẹ để luôn ở tầm đánh (micro-step)
 *  - Thỉnh thoảng nhảy tấn công (jump attack)
 */
public class AttackState implements State {

    private float jumpAttackTimer = 0f;
    private static final float JUMP_ATTACK_INTERVAL = 2.0f;

    // Bộ đếm "retreat" — lùi lại sau vài đòn để tránh bị phản đòn
    private float retreatTimer = 0f;
    private static final float RETREAT_DURATION = 0.4f;
    private boolean isRetreating = false;
    private int hitCount = 0; // Đếm số đòn đã đánh liên tiếp

    @Override
    public void enter(BotFighter bot) {
        jumpAttackTimer = 0f;
        retreatTimer    = 0f;
        isRetreating    = false;
        hitCount        = 0;
    }

    @Override
    public void update(BotFighter bot, float delta) {
        jumpAttackTimer += delta;

        Fighter target = bot.getTarget();
        float dist     = Math.abs(bot.getCenterX() - target.getCenterX());
        float diffX    = target.getCenterX() - bot.getCenterX();

        // ── Điều kiện chuyển state ─────────────────────────────────────────

        if (bot.getHp() < Constants.AI_EVADE_HP) {
            bot.getStateMachine().changeState(new EvadeState());
            return;
        }

        if (dist > Constants.AI_ATTACK_RANGE * 1.5f) {
            bot.getStateMachine().changeState(new ChaseState());
            return;
        }

        // ── Retreat behavior (lùi lại sau N đòn) ─────────────────────────
        if (isRetreating) {
            retreatTimer -= delta;
            // Lùi ngược chiều target
            if (diffX > 0) bot.moveLeft(delta);
            else           bot.moveRight(delta);

            if (retreatTimer <= 0) {
                isRetreating = false;
                hitCount     = 0;
                bot.stopHorizontal();
            }
            return; // Không attack khi đang lùi
        }

        // ── Attack behavior ───────────────────────────────────────────────

        // Micro-step: giữ khoảng cách tối ưu
        float optimalDist = Constants.AI_ATTACK_RANGE * 0.65f;
        if (dist > optimalDist) {
            // Tiến gần hơn chút
            if (diffX > 0) bot.moveRight(delta);
            else           bot.moveLeft(delta);
        } else {
            bot.stopHorizontal();
        }

        // Tấn công
        // AttackState.java — thay thế đoạn attack trong update()

// Tấn công — attack() trả về true nếu đòn được KH.HOẠT (không phải khi trúng)
// checkHit() trong PlayScreen mới là nơi xác định trúng/trượt
        boolean attackActivated;
        double roll = Math.random();
        if (roll < 0.55) {
            attackActivated = bot.punch();
        } else if (roll < 0.9) {
            attackActivated = bot.kick();
        } else {
            attackActivated = bot.energySkill();
        }
        if (attackActivated) {
            hitCount++;
            if (hitCount >= 3) {
                isRetreating = true;
                retreatTimer = RETREAT_DURATION;
                hitCount     = 0; // reset để lần sau retreat lại
            }
        }

        // Jump attack định kỳ
        if (jumpAttackTimer >= JUMP_ATTACK_INTERVAL && bot.isOnGround()) {
            bot.jump();
            bot.kick();
            jumpAttackTimer = 0f;
        }
    }

    @Override
    public void exit(BotFighter bot) {
        bot.stopHorizontal();
        isRetreating = false;
    }

    @Override
    public String getName() { return "ATTACK"; }
}
