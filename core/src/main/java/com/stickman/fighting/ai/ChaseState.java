package com.stickman.fighting.ai;

import com.stickman.fighting.entities.BotFighter;
import com.stickman.fighting.entities.Fighter;
import com.stickman.fighting.utils.Constants;

/**
 * CHASE STATE — Bot chạy về phía đối thủ.
 *
 * Chuyển state khi:
 *  - dist > CHASE_RANGE                        → IdleState  (target bỏ chạy xa)
 *  - dist < ATTACK_RANGE                       → AttackState
 *  - bot.hp < EVADE_THRESHOLD                  → EvadeState
 *
 * Behavior thêm:
 *  - Nhảy qua đối thủ nếu bị dồn vào góc
 *  - Nhảy vượt qua nếu target đang ở trên cao
 */
public class ChaseState implements State {

    private float jumpCooldown = 0f;
    private static final float JUMP_COOLDOWN_MAX = 1.2f;

    @Override
    public void enter(BotFighter bot) {
        jumpCooldown = 0f;
    }

    @Override
    public void update(BotFighter bot, float delta) {
        jumpCooldown -= delta;

        Fighter target = bot.getTarget();
        float dist     = Math.abs(bot.getCenterX() - target.getCenterX());
        float diffX    = target.getCenterX() - bot.getCenterX();

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

        // ── Chase behavior ────────────────────────────────────────────────

        // Di chuyển về phía target
        if (diffX > 0) {
            bot.moveRight(delta);
        } else {
            bot.moveLeft(delta);
        }

        // Nhảy nếu target đang cao hơn hoặc bị chặn
        if (jumpCooldown <= 0 && bot.isOnGround()) {
            boolean targetIsHigher = target.getPosition().y > bot.getPosition().y + 30f;
            boolean botCornered    = isBotCornered(bot);

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
    public String getName() { return "CHASE"; }

    /** Kiểm tra bot có bị dồn vào góc màn hình không */
    private boolean isBotCornered(BotFighter bot) {
        float x = bot.getPosition().x;
        return x < 60f || x > Constants.SCREEN_WIDTH - 60f - Fighter.WIDTH;
    }
}
