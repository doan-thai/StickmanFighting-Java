package com.stickman.fighting.ai;

import com.badlogic.gdx.Gdx;
import com.stickman.fighting.entities.BotFighter;

/**
 * StateMachine quản lý việc chuyển đổi giữa các State.
 *
 * Chỉ có MỘT state active tại mỗi thời điểm.
 * Việc chuyển state diễn ra an toàn: exit() → enter() → update().
 *
 * Cách dùng:
 *   stateMachine = new StateMachine(bot, new IdleState());
 *   stateMachine.update(delta);                          // mỗi frame
 *   stateMachine.changeState(new ChaseState());          // khi cần
 */
public class StateMachine {

    private final BotFighter bot;
    private State currentState;
    private State previousState; // Dùng để revert nếu cần

    public StateMachine(BotFighter bot, State initialState) {
        this.bot          = bot;
        this.currentState = initialState;
        this.previousState = initialState;
        currentState.enter(bot);
    }

    /** Gọi mỗi frame từ BotFighter.updateLogic() */
    public void update(float delta) {
        if (currentState != null) {
            currentState.update(bot, delta);
        }
    }

    /**
     * Chuyển sang state mới.
     * Tự động gọi exit() trên state cũ và enter() trên state mới.
     */
    public void changeState(State newState) {
        if (newState == null) return;

        // So sánh bằng class thay vì String name — chính xác hơn
        if (currentState.getClass() == newState.getClass()) return;

        Gdx.app.log("FSM", bot.getBotName() + ": "
            + currentState.getName() + " → " + newState.getName());

        currentState.exit(bot);
        previousState = currentState;
        currentState  = newState;
        currentState.enter(bot);
    }

    /** Quay lại state trước đó (hữu ích sau Evade) */
    public void revertToPreviousState() {
        changeState(previousState);
    }

    public State getCurrentState()  { return currentState; }
    public State getPreviousState() { return previousState; }
    public String getCurrentStateName() {
        return currentState != null ? currentState.getName() : "null";
    }
}
