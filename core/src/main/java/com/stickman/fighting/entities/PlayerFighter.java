package com.stickman.fighting.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;

/**
 * Nhân vật do người chơi điều khiển qua bàn phím.
 *
 * Player 1 (Xanh): A/D di chuyển, W nhảy, U block, I đấm, O đá, L tốc biến
 * Player 2 (Đỏ): ←/→ di chuyển, ↑ nhảy, Num4 block, Num5 đấm, Num6 đá, Num3 tốc
 * biến
 */
public class PlayerFighter extends Fighter {

    public enum PlayerIndex {
        PLAYER_ONE, PLAYER_TWO
    }

    private final PlayerIndex playerIndex;

    // Keybinding
    private final int keyLeft, keyRight, keyJump;
    private final int keyBlock, keyPunch, keyKick, keyDash;
    private final int altKeyBlock, altKeyPunch;

    public PlayerFighter(float startX, float startY,
            PlayerIndex index, Color color) {
        super(startX, startY, color);
        this.playerIndex = index;

        // Gán phím theo người chơi
        if (index == PlayerIndex.PLAYER_ONE) {
            keyLeft = Input.Keys.A;
            keyRight = Input.Keys.D;
            keyJump = Input.Keys.W;
            keyBlock = Input.Keys.U;
            keyPunch = Input.Keys.I;
            keyKick = Input.Keys.O;
            keyDash = Input.Keys.L;
            altKeyBlock = -1;
            altKeyPunch = -1;
        } else {
            keyLeft = Input.Keys.LEFT;
            keyRight = Input.Keys.RIGHT;
            keyJump = Input.Keys.UP;
            keyBlock = Input.Keys.NUMPAD_4;
            keyPunch = Input.Keys.NUMPAD_5;
            keyKick = Input.Keys.NUMPAD_6;
            keyDash = Input.Keys.NUMPAD_3;
            // Hỗ trợ cả hàng số thường cho bàn phím không có numpad riêng.
            altKeyBlock = Input.Keys.NUM_4;
            altKeyPunch = Input.Keys.NUM_5;
        }
    }

    @Override
    protected void updateLogic(float delta) {
        boolean isBlockingNow = isPressed(keyBlock, altKeyBlock);
        setBlocking(isBlockingNow);

        boolean movingLeft = Gdx.input.isKeyPressed(keyLeft);
        boolean movingRight = Gdx.input.isKeyPressed(keyRight);

        if (isBlockingNow) {
            stopHorizontal();
        } else if (movingLeft && !movingRight) {
            moveLeft(delta);
        } else if (movingRight && !movingLeft) {
            moveRight(delta);
        } else {
            stopHorizontal();
        }

        // Nhảy (chỉ khi mới nhấn – justPressed tránh giữ phím)
        if (Gdx.input.isKeyJustPressed(keyJump)) {
            jump();
        }

        // Tốc biến
        if (Gdx.input.isKeyJustPressed(keyDash)) {
            dash();
        }

        // U+I / Num4+Num5: cầu năng lượng
        boolean punchJustPressed = isJustPressed(keyPunch, altKeyPunch);
        if (isBlockingNow && punchJustPressed) {
            energySkill();
            return;
        }

        if (punchJustPressed) {
            punch();
        }
        if (Gdx.input.isKeyJustPressed(keyKick)) {
            kick();
        }

        // Tự hướng mặt về phía đối thủ sẽ được set từ PlayScreen
    }

    private boolean isPressed(int primary, int alternate) {
        return Gdx.input.isKeyPressed(primary)
                || (alternate != -1 && Gdx.input.isKeyPressed(alternate));
    }

    private boolean isJustPressed(int primary, int alternate) {
        return Gdx.input.isKeyJustPressed(primary)
                || (alternate != -1 && Gdx.input.isKeyJustPressed(alternate));
    }

    public PlayerIndex getPlayerIndex() {
        return playerIndex;
    }
}
