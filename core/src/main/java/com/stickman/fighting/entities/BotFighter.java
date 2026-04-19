package com.stickman.fighting.entities;

import com.badlogic.gdx.graphics.Color;
import com.stickman.fighting.ai.IdleState;
import com.stickman.fighting.ai.StateMachine;

/**
 * BotFighter — Nhân vật AI điều khiển hoàn toàn bởi FSM.
 *
 * Quan hệ với FSM:
 *   BotFighter ──owns──► StateMachine
 *   StateMachine ──holds──► currentState (IdleState/ChaseState/...)
 *   State ──calls──► bot.moveLeft(), bot.attack(), ... (thao tác Fighter)
 *
 * BotFighter KHÔNG chứa logic AI — tất cả nằm trong các State class.
 */
public class BotFighter extends Fighter {

    private final Fighter      target;       // Đối thủ (Player1)
    private final StateMachine stateMachine;
    private final String       botName;

    // Độ khó (ảnh hưởng tốc độ phản ứng & accuracy)
    public enum Difficulty { EASY, NORMAL, HARD }
    private final Difficulty difficulty;

    // reactionDelay — Thời gian chờ trước khi FSM được cập nhật sau mỗi lần chuyển state.
    // Giúp bot không phản ứng tức thì mà có độ trễ tự nhiên hơn.
    private float reactionDelay = 0f;
    private static final float BASE_REACTION_DELAY = 0.18f;

    // ── Constructor ───────────────────────────────────────────────────────────

    public BotFighter(float startX, float startY,
                      Color color, Fighter target) {
        this(startX, startY, color, target, Difficulty.NORMAL);
    }

    public BotFighter(float startX, float startY, Color color,
                      Fighter target, Difficulty difficulty) {
        super(startX, startY, color);
        this.target     = target;
        this.difficulty = difficulty;
        this.botName    = "Bot " + difficulty.name();

        // Khởi động FSM với trạng thái ban đầu là Idle
        this.stateMachine = new StateMachine(this, new IdleState());
    }

    // ── Update Logic (gọi từ Fighter.update()) ────────────────────────────────

    @Override
    protected void updateLogic(float delta) {
        // Giảm reactionDelay mỗi frame
        if (reactionDelay > 0f) {
            reactionDelay -= delta;
            // Dừng di chuyển ngang trong thời gian delay — tránh trượt đà sau khi đổi state
            stopHorizontal();
            return;
        }

        // Áp dụng hệ số độ khó lên delta
        // EASY: Bot "suy nghĩ chậm hơn" → scale delta thấp hơn
        float effectiveDelta = switch (difficulty) {
            case EASY   -> delta * 0.65f;
            case NORMAL -> delta;
            case HARD   -> delta * 1.25f;
        };

        // Delegate toàn bộ hành vi sang FSM
        stateMachine.update(effectiveDelta);
    }

    /**
     * Kích hoạt reactionDelay sau mỗi lần FSM chuyển state.
     * Gọi tự động bởi StateMachine.changeState().
     * Thời gian delay phụ thuộc độ khó — EASY chậm hơn, HARD nhanh hơn.
     */
    public void triggerReactionDelay() {
        reactionDelay = switch (difficulty) {
            case EASY   -> BASE_REACTION_DELAY * 1.6f;
            case NORMAL -> BASE_REACTION_DELAY;
            case HARD   -> BASE_REACTION_DELAY * 0.6f;
        };
    }

    // ── Getters (cần thiết cho các State) ────────────────────────────────────

    public Fighter      getTarget()       { return target; }
    public StateMachine getStateMachine() { return stateMachine; }
    public String       getBotName()      { return botName; }
    public Difficulty   getDifficulty()   { return difficulty; }

    // ── Override reset để reset cả FSM ───────────────────────────────────────

    @Override
    public void reset(float startX, float startY, boolean faceRight) {
        super.reset(startX, startY, faceRight);
        // Reset FSM về Idle (changeState() sẽ gọi triggerReactionDelay() bên trong)
        stateMachine.changeState(new IdleState());
        // Xoá delay SAU khi changeState — để bot không bị đơ khi mới vào trận
        reactionDelay = 0f;
    }
}
