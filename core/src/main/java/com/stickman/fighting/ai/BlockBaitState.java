package com.stickman.fighting.ai;

import com.stickman.fighting.entities.BotFighter;
import com.stickman.fighting.entities.Fighter;
import com.stickman.fighting.utils.Constants;

/**
 * BLOCK_BAIT STATE — Bot chủ động block để nhử đòn đối thủ, sau đó
 * chuyển sang PunishState khi phát hiện thời điểm phù hợp.
 *
 * Chuyển state khi:
 *  - Đối thủ vừa kết thúc đòn tấn công → PunishState
 *  - baitTimer hết mà target không tấn công → ChaseState
 *  - bot.hp < AI_EVADE_HP → EvadeState
 *  - Target rời quá xa → ChaseState
 *
 * Behavior:
 *  - Duy trì isBlocking = true mỗi frame (kể cả sau khi reactionDelay kết thúc)
 *  - Theo dõi trạng thái tấn công của đối thủ để phát hiện thời điểm phản công
 */
public class BlockBaitState implements State {

    private float   baitTimer;
    // Thời gian tối đa để chờ đối thủ tấn công
    private static final float BAIT_DURATION = 0.80f;

    // Theo dõi việc đối thủ có tấn công không (để detect rising-edge false)
    private boolean wasTargetAttacking;

    @Override
    public void enter(BotFighter bot) {
        baitTimer          = BAIT_DURATION;
        wasTargetAttacking = bot.getTarget().isAttacking();
        // Không gọi setBlocking(true) ở đây vì reactionDelay sẽ chạy stopHorizontal()
        // và setBlocking cần được duy trì trong update() sau khi delay xong
    }

    @Override
    public void update(BotFighter bot, float delta) {
        baitTimer -= delta;

        Fighter target = bot.getTarget();
        float dist = Math.abs(bot.getCenterX() - target.getCenterX());

        // Luôn duy trì block trong suốt state này
        // setBlocking gọi stopHorizontal() bên trong nếu blocking=true
        bot.setBlocking(true);

        // ── Điều kiện thoát ─────────────────────────────────────────────────

        // Máu nguy hiểm → Evade
        if (bot.getHp() < Constants.AI_EVADE_HP) {
            bot.getStateMachine().changeState(new EvadeState());
            return;
        }

        // Target rời quá xa → Chase (bait không còn hiệu quả)
        if (dist > Constants.AI_ATTACK_RANGE * 2.2f) {
            bot.getStateMachine().changeState(new ChaseState());
            return;
        }

        // Phát hiện: đối thủ VỪA kết thúc đòn tấn công (rising-edge: true→false)
        boolean targetJustFinishedAttack = wasTargetAttacking && !target.isAttacking();
        wasTargetAttacking = target.isAttacking();

        if (targetJustFinishedAttack) {
            // Đối thủ vừa kết thúc đòn → phản công ngay!
            bot.getStateMachine().changeState(new PunishState());
            return;
        }

        // Thời gian bait hết mà không ai tấn công → quay lại Chase
        if (baitTimer <= 0f) {
            bot.getStateMachine().changeState(new ChaseState());
        }
    }

    @Override
    public void exit(BotFighter bot) {
        // Bỏ block khi thoát state (để có thể tấn công ngay trong PunishState)
        bot.setBlocking(false);
    }

    @Override
    public String getName() {
        return "BLOCK_BAIT";
    }
}
