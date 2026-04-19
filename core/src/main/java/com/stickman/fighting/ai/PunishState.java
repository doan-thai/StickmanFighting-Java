package com.stickman.fighting.ai;

import com.stickman.fighting.entities.BotFighter;
import com.stickman.fighting.entities.Fighter;
import com.stickman.fighting.utils.Constants;

/**
 * PUNISH STATE — Bot phản công nhanh sau khi bait block thành công
 * hoặc phát hiện sơ hở của đối thủ (vừa xong đòn tấn công).
 *
 * Chuyển state khi:
 *  - punishTimer hết → ChaseState
 *  - bot.hp < AI_EVADE_HP → EvadeState
 *  - target thoát quá xa → ChaseState
 *
 * Behavior:
 *  - Tiến vào tầm đánh ngay lập tức
 *  - Ưu tiên kick (damage cao, "hiệu ứng khựng") kể cả khi đang di chuyển vào
 *  - Nếu kick đang cooldown thì dùng punch thay thế
 */
public class PunishState implements State {

    private float punishTimer;
    private static final float PUNISH_DURATION = 1.0f;

    @Override
    public void enter(BotFighter bot) {
        punishTimer = PUNISH_DURATION;
        bot.setBlocking(false);
    }

    @Override
    public void update(BotFighter bot, float delta) {
        punishTimer -= delta;

        Fighter target = bot.getTarget();
        float dist  = Math.abs(bot.getCenterX() - target.getCenterX());
        float diffX = target.getCenterX() - bot.getCenterX();

        // ── Điều kiện thoát ──────────────────────────────────────────────────

        // Máu nguy hiểm → Evade
        if (bot.getHp() < Constants.AI_EVADE_HP) {
            bot.getStateMachine().changeState(new EvadeState());
            return;
        }

        // Hết thời gian punish hoặc target thoát xa → Chase
        if (punishTimer <= 0f || dist > Constants.AI_ATTACK_RANGE * 2.0f) {
            bot.getStateMachine().changeState(new ChaseState());
            return;
        }

        // ── Punish behavior ───────────────────────────────────────────────────

        // Tiến sát vào tầm đánh tối ưu (trong khi vẫn cố đánh liên tục)
        float optimalDist = Constants.AI_ATTACK_RANGE * 0.7f;
        if (dist > optimalDist) {
            // Đang tiến vào → VẪN cố đấm/đá luôn (không chờ đến tầm mới đánh)
            if (diffX > 0) bot.moveRight(delta);
            else           bot.moveLeft(delta);
            // Thử kick trước, không được thì punch khi đang di chuyển
            if (!bot.kick()) {
                bot.punch();
            }
        } else {
            // Đã đủ gần → dừng lại và đánh mạnh
            bot.stopHorizontal();
            if (!bot.kick()) {
                bot.punch();
            }
        }
    }

    @Override
    public void exit(BotFighter bot) {
        bot.stopHorizontal();
    }

    @Override
    public String getName() {
        return "PUNISH";
    }
}
