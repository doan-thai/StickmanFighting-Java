package com.stickman.fighting.ai;

import com.stickman.fighting.entities.BotFighter;
import com.stickman.fighting.entities.Fighter;
import com.stickman.fighting.utils.Constants;

/**
 * CHASE STATE — Bot chạy về phía đối thủ.
 *
 * Chuyển state khi:
 * - dist > CHASE_RANGE → IdleState (target bỏ chạy xa)
 * - dist < ATTACK_RANGE → AttackState
 * - bot.hp < EVADE_THRESHOLD → EvadeState
 *
 * Behavior thêm:
 * - Nhảy qua đối thủ nếu bị dồn vào góc
 * - Nhảy vượt qua nếu target đang ở trên cao
 */
public class ChaseState implements State {

    private float jumpCooldown = 0f;
    private static final float JUMP_COOLDOWN_MAX = 1.2f;
    private static final float RANGED_ENERGY_CHANCE = 0.45f;
    private static final float RANGED_ENERGY_DIST_MIN_SCALE = 1.2f;
    private static final float RANGED_ENERGY_DIST_MAX_SCALE = 2.6f;
    private static final float RANGED_ENERGY_COOLDOWN_MIN = 0.55f;
    private static final float RANGED_ENERGY_COOLDOWN_MAX = 1.35f;
    private float rangedEnergyTimer = 0f;

    // Timer để kiểm soát tần suất chuyển sang BlockBaitState khi áp sát
    private float blockBaitTimer = 0f;
    private static final float BLOCK_BAIT_COOLDOWN = 4.5f;

    @Override
    public void enter(BotFighter bot) {
        jumpCooldown = 0f;
        rangedEnergyTimer = 0f;
        // Khởi tạo với cooldown ngắn hơn để có thể bait sớm hơn khi áp sát
        blockBaitTimer = 2.0f;
    }

    @Override
    public void update(BotFighter bot, float delta) {
        jumpCooldown -= delta;
        rangedEnergyTimer -= delta;
        blockBaitTimer -= delta;

        Fighter target = bot.getTarget();
        float dist = Math.abs(bot.getCenterX() - target.getCenterX());
        float diffX = target.getCenterX() - bot.getCenterX();

        // ── Điều kiện chuyển state ─────────────────────────────────────────

        // Hết máu → Evade (ưu tiên cao nhất)
        if (bot.getHp() < Constants.AI_EVADE_HP) {
            bot.getStateMachine().changeState(new EvadeState());
            return;
        }

        // Target bỏ chạy quá xa → về Idle
        if (dist > Constants.AI_CHASE_RANGE) {
            bot.getStateMachine().changeState(new IdleState());
            return;
        }

        // Đủ gần → Attack
        if (dist < Constants.AI_ATTACK_RANGE) {
            bot.getStateMachine().changeState(new AttackState());
            return;
        }

        // Cơ hội BlockBait khi đang trong vùng trung giãn (gần nhưng chưa đến tầm đánh)
        if (blockBaitTimer <= 0f
                && dist < Constants.AI_ATTACK_RANGE * 1.5f
                && bot.isOnGround()
                && !bot.getTarget().isAttacking()) {
            // Reset timer trước — dù có chuyển state hay không, để bờ né kiểm tra lại liên tục
            blockBaitTimer = BLOCK_BAIT_COOLDOWN;
            if (Math.random() < 0.30) {
                bot.getStateMachine().changeState(new BlockBaitState());
                return;
            }
        }

        // ── Chase behavior ────────────────────────────────────────────────

        float minDist = Constants.AI_ATTACK_RANGE * RANGED_ENERGY_DIST_MIN_SCALE;
        float maxDist = Constants.AI_ATTACK_RANGE * RANGED_ENERGY_DIST_MAX_SCALE;
        boolean inRangedEnergyWindow = dist >= minDist && dist <= maxDist;
        if (inRangedEnergyWindow && rangedEnergyTimer <= 0f) {
            if (Math.random() < RANGED_ENERGY_CHANCE) {
                bot.energySkill();
            }
            resetRangedEnergyTimer();
        }

        // Di chuyển về phía target
        if (diffX > 0) {
            bot.moveRight(delta);
        } else {
            bot.moveLeft(delta);
        }

        // Nhảy nếu target đang cao hơn hoặc bị chặn
        if (jumpCooldown <= 0 && bot.isOnGround()) {
            boolean targetIsHigher = target.getPosition().y > bot.getPosition().y + 30f;
            boolean botCornered = isBotCornered(bot);

            if (targetIsHigher || botCornered) {
                bot.jump();
                jumpCooldown = JUMP_COOLDOWN_MAX;
            }
        }
    }

    @Override
    public void exit(BotFighter bot) {
        bot.stopHorizontal();
    }

    @Override
    public String getName() {
        return "CHASE";
    }

    /** Kiểm tra bot có bị dồn vào góc màn hình không */
    private boolean isBotCornered(BotFighter bot) {
        float x = bot.getPosition().x;
        return x < 60f || x > Constants.SCREEN_WIDTH - 60f - Fighter.WIDTH;
    }

    private void resetRangedEnergyTimer() {
        double roll = Math.random();
        rangedEnergyTimer = RANGED_ENERGY_COOLDOWN_MIN
                + (float) roll * (RANGED_ENERGY_COOLDOWN_MAX - RANGED_ENERGY_COOLDOWN_MIN);
    }
}
