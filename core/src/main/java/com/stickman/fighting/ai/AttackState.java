package com.stickman.fighting.ai;

import com.stickman.fighting.entities.BotFighter;
import com.stickman.fighting.entities.Fighter;
import com.stickman.fighting.utils.Constants;

/**
 * ATTACK STATE — Bot tấn công khi đủ gần.
 *
 * Chuyển state khi:
 * - dist > ATTACK_RANGE * 1.5f → ChaseState (target thoát ra)
 * - bot.hp < EVADE_THRESHOLD  → EvadeState
 * - target vừa xong đòn        → PunishState (55% xác suất)
 * - sau retreat                 → BlockBaitState (30% xác suất)
 *
 * Behavior:
 * - Dịch chuyển nhẹ để luôn ở tầm đánh (micro-step)
 * - Tấn công liên tục, bị giới hạn bởi cooldown trong Fighter
 * - Retreat sau 4 đòn liên tiếp rồi có thể vào BlockBait
 * - Jump attack định kỳ
 */
public class AttackState implements State {

    private float jumpAttackTimer = 0f;
    private static final float JUMP_ATTACK_INTERVAL = 2.0f;

    // Bộ đếm "retreat" — lùi lại sau vài đòn để tránh bị phản đòn
    private float retreatTimer = 0f;
    private static final float RETREAT_DURATION = 0.4f;
    private boolean isRetreating = false;
    private int hitCount = 0;

    // Tracking trạng thái tấn công của target để detect sơ hở (rising-edge false)
    private boolean wasTargetAttacking = false;

    @Override
    public void enter(BotFighter bot) {
        jumpAttackTimer    = 0f;
        retreatTimer       = 0f;
        isRetreating       = false;
        hitCount           = 0;
        wasTargetAttacking = bot.getTarget().isAttacking();
    }

    @Override
    public void update(BotFighter bot, float delta) {
        jumpAttackTimer += delta;

        Fighter target = bot.getTarget();
        float dist  = Math.abs(bot.getCenterX() - target.getCenterX());
        float diffX = target.getCenterX() - bot.getCenterX();

        // Cập nhật tracking ngay đầu frame — đảm bảo luôn sync dù có early-return
        boolean targetJustFinishedAttack = wasTargetAttacking && !target.isAttacking();
        wasTargetAttacking = target.isAttacking();

        // ── Điều kiện chuyển state (ưu tiên cao) ──────────────────────────────

        if (bot.getHp() < Constants.AI_EVADE_HP) {
            bot.getStateMachine().changeState(new EvadeState());
            return;
        }

        if (dist > Constants.AI_ATTACK_RANGE * 1.5f) {
            bot.getStateMachine().changeState(new ChaseState());
            return;
        }

        // ── Retreat behavior (lùi sau N đòn) ──────────────────────────────────
        if (isRetreating) {
            retreatTimer -= delta;
            // Lùi ngược chiều target
            if (diffX > 0)
                bot.moveLeft(delta);
            else
                bot.moveRight(delta);

            if (retreatTimer <= 0) {
                isRetreating = false;
                hitCount     = 0;
                bot.stopHorizontal();
                // 30% xác suất vào BlockBaitState thay vì tấn công lại ngay
                if (Math.random() < 0.30) {
                    bot.getStateMachine().changeState(new BlockBaitState());
                }
            }
            return; // Không attack khi đang lùi
        }

        // ── Attack behavior ────────────────────────────────────────────────────

        // Phát hiện sơ hở: target vừa kết thúc đòn và đang đủ gần → PunishState
        if (targetJustFinishedAttack && dist < Constants.AI_ATTACK_RANGE * 1.3f) {
            if (Math.random() < 0.55) {
                bot.getStateMachine().changeState(new PunishState());
                return;
            }
        }

        // Micro-step: duy trì khoảng cách tối ưu
        float optimalDist = Constants.AI_ATTACK_RANGE * 0.65f;
        if (dist > optimalDist) {
            if (diffX > 0)
                bot.moveRight(delta);
            else
                bot.moveLeft(delta);
        } else {
            bot.stopHorizontal();
        }

        // Tấn công — attack() trả về true nếu đòn được kích hoạt (cooldown cho phép)
        boolean attackActivated;
        if (Math.random() < 0.56) {
            attackActivated = bot.punch();
        } else {
            attackActivated = bot.kick();
        }
        if (attackActivated) {
            hitCount++;
            // Retreat sau 4 đòn liên tiếp (không phải 3) — bot đánh đủ rồi mới rút
            if (hitCount >= 4) {
                isRetreating = true;
                retreatTimer = RETREAT_DURATION;
                hitCount     = 0;
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
    public String getName() {
        return "ATTACK";
    }
}
