package com.stickman.fighting.ai;

import com.stickman.fighting.entities.BotFighter;
import com.stickman.fighting.utils.Constants;

/**
 * IDLE STATE — Bot đứng yên, quan sát đối thủ.
 *
 * Chuyển state khi:
 *  - dist(bot, target) < CHASE_RANGE  → ChaseState
 *
 * Behavior phụ:
 *  - Thỉnh thoảng "taunt" (nhấp nhô nhẹ) để animation sống động hơn
 */
public class IdleState implements State {

    private float idleTimer = 0f;
    private static final float TAUNT_INTERVAL = 2.5f; // Giây

    @Override
    public void enter(BotFighter bot) {
        bot.stopHorizontal();
        idleTimer = 0f;
    }

    @Override
    public void update(BotFighter bot, float delta) {
        idleTimer += delta;

        float dist = distanceTo(bot);

        // ── Điều kiện chuyển state ─────────────────────────────────────────
        if (dist < Constants.AI_CHASE_RANGE) {
            bot.getStateMachine().changeState(new ChaseState());
            return;
        }

        // ── Idle behavior: nhảy nhẹ kiểu "taunt" theo chu kỳ ─────────────
        if (idleTimer >= TAUNT_INTERVAL && bot.isOnGround()) {
            bot.jump();
            idleTimer = 0f;
        }

        // Luôn dừng di chuyển ngang
        bot.stopHorizontal();
    }

    @Override
    public void exit(BotFighter bot) {
        // Không cần dọn dẹp gì
    }

    @Override
    public String getName() { return "IDLE"; }

    private float distanceTo(BotFighter bot) {
        return Math.abs(bot.getCenterX() - bot.getTarget().getCenterX());
    }
}
