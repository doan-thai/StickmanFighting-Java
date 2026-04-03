package com.stickman.fighting.ai;

import com.stickman.fighting.entities.BotFighter;
import com.stickman.fighting.entities.Fighter;
import com.stickman.fighting.utils.Constants;

/**
 * EVADE STATE — Bot né tránh khi HP xuống thấp.
 *
 * Chuyển state khi:
 *  - evadeTimer hết                      → ChaseState  (quay lại chiến đấu)
 *  - bot.hp đã recover (không thể, nhưng
 *    timer hết là đủ trong game này)
 *
 * Behavior:
 *  - Chạy theo hướng ngược lại với target
 *  - Nhảy né nếu target đến gần trong tầm attack
 *  - Giữ khoảng cách an toàn tối thiểu
 */
public class EvadeState implements State {

    private float evadeTimer;
    // Thời gian né trước khi quay lại chiến đấu
    private static final float EVADE_DURATION = 3.0f;

    // Cooldown nhảy né
    private float jumpEvadeCooldown = 0f;
    private static final float JUMP_EVADE_COOLDOWN_MAX = 0.8f;

    @Override
    public void enter(BotFighter bot) {
        evadeTimer       = EVADE_DURATION;
        jumpEvadeCooldown = 0f;
    }

    @Override
    public void update(BotFighter bot, float delta) {
        evadeTimer        -= delta;
        jumpEvadeCooldown -= delta;

        Fighter target = bot.getTarget();
        float diffX    = target.getCenterX() - bot.getCenterX();
        float dist     = Math.abs(diffX);

        // ── Điều kiện thoát Evade ──────────────────────────────────────────
        if (evadeTimer <= 0) {
            // Máu còn thấp → vẫn vào ChaseState (người chơi chịu trách nhiệm)
            bot.getStateMachine().changeState(new ChaseState());
            return;
        }

        // ── Evade behavior ────────────────────────────────────────────────

        // Bước 1: Chạy theo chiều NGƯỢC lại
        if (diffX > 0) {
            // Target ở bên phải → chạy trái
            bot.moveLeft(delta);
        } else {
            // Target ở bên trái → chạy phải
            bot.moveRight(delta);
        }

        // Bước 2: Nhảy né nếu target vào tầm nguy hiểm
        boolean targetDangerous = dist < Constants.AI_ATTACK_RANGE * 1.2f;
        if (targetDangerous && jumpEvadeCooldown <= 0 && bot.isOnGround()) {
            bot.jump();
            jumpEvadeCooldown = JUMP_EVADE_COOLDOWN_MAX;
        }

        // Bước 3: Nếu bị dồn vào góc → đổi hướng và nhảy thoát
        if (isCornered(bot) && bot.isOnGround()) {
            if (jumpEvadeCooldown <= 0) {
                bot.jump();
                // Nhảy về phía target để vượt qua (không còn lựa chọn nào khác)
                if (diffX > 0) bot.moveRight(delta);
                else           bot.moveLeft(delta);
                jumpEvadeCooldown = JUMP_EVADE_COOLDOWN_MAX;
            }
        }
    }

    @Override
    public void exit(BotFighter bot) {
        bot.stopHorizontal();
    }

    @Override
    public String getName() { return "EVADE"; }

    private boolean isCornered(BotFighter bot) {
        float x = bot.getPosition().x;
        return x < 50f || x > Constants.SCREEN_WIDTH - 50f - Fighter.WIDTH;
    }
}
