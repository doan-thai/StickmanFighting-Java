package com.stickman.fighting.ai;

import com.stickman.fighting.entities.BotFighter;

/**
 * Interface cơ sở cho mọi trạng thái trong FSM của Bot.
 *
 * Mỗi State chịu trách nhiệm:
 *  - enter()  : Khởi tạo khi bước vào trạng thái
 *  - update() : Logic mỗi frame
 *  - exit()   : Dọn dẹp khi rời trạng thái
 *
 * State KHÔNG lưu trữ dữ liệu của Bot — tất cả data nằm trong BotFighter.
 * State chỉ chứa thuần túy HÀNH VI (behavior).
 */
public interface State {

    /** Gọi một lần khi StateMachine chuyển vào state này */
    void enter(BotFighter bot);

    /**
     * Gọi mỗi frame khi state đang active.
     * State tự quyết định khi nào cần yêu cầu StateMachine chuyển state.
     *
     * @param bot   Nhân vật Bot đang được điều khiển
     * @param delta Thời gian frame (giây)
     */
    void update(BotFighter bot, float delta);

    /** Gọi một lần khi StateMachine rời khỏi state này */
    void exit(BotFighter bot);

    /** Tên state để debug / log */
    String getName();
}
