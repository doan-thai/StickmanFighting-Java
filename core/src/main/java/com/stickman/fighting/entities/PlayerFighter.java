package com.stickman.fighting.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.stickman.fighting.utils.SoundManager;

/**
 * Nhân vật do người chơi điều khiển qua bàn phím.
 */
public class PlayerFighter extends Fighter {

    public enum PlayerIndex {
        PLAYER_ONE, PLAYER_TWO
    }

    private final PlayerIndex playerIndex;

    // Keybinding
    private final int keyLeft, keyRight, keyJump;
    private final int keyBlock, keyPunch, keyKick, keyDash, keyWeapon;
    private final int altKeyBlock, altKeyPunch, altKeyKick, altKeyDash, altKeyWeapon;

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
            keyWeapon = Input.Keys.J;
            altKeyBlock = -1;
            altKeyPunch = -1;
            altKeyKick = -1;
            altKeyDash = -1;
            altKeyWeapon = -1;
        } else {
            keyLeft = Input.Keys.LEFT;
            keyRight = Input.Keys.RIGHT;
            keyJump = Input.Keys.UP;
            // Numpad keys
            keyBlock = Input.Keys.NUMPAD_4;
            keyPunch = Input.Keys.NUMPAD_5;
            keyKick = Input.Keys.NUMPAD_6;
            keyDash = Input.Keys.NUMPAD_3;
            keyWeapon = Input.Keys.NUMPAD_1;
            // Hàng phím số thường làm phím thay thế (Alt)
            altKeyBlock = Input.Keys.NUM_4;
            altKeyPunch = Input.Keys.NUM_5;
            altKeyKick = Input.Keys.NUM_6;
            altKeyDash = Input.Keys.NUM_3;
            altKeyWeapon = Input.Keys.NUM_1;
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

        // Nhảy
        if (Gdx.input.isKeyJustPressed(keyJump)) {
            jump();
        }

        // Tốc biến (Dash)
        if (isJustPressed(keyDash, altKeyDash)) {
            dash();
        }

        // Kích hoạt vũ khí (J / Num1)
        if (isJustPressed(keyWeapon, altKeyWeapon) && getEnergyPercent() >= 0.99f && !isWeaponUsedThisMatch()) {
            setHasWeapon(true);
            energy = 0;
            SoundManager.getInstance().playSound(SoundManager.SoundEffect.ENERGY_HIT);
        }

        // Tuyệt chiêu: Block + Punch (U+I / Num4+5)
        boolean punchJustPressed = isJustPressed(keyPunch, altKeyPunch);
        if (isBlockingNow && punchJustPressed) {
            if (hasWeapon()) {
                throwWeapon();
            } else {
                energySkill();
            }
            return;
        }

        if (punchJustPressed) {
            punch();
        }

        if (isJustPressed(keyKick, altKeyKick)) {
            kick();
        }
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
