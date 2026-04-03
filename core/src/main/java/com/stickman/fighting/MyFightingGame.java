// MyFightingGame.java — thay thế hoàn toàn
package com.stickman.fighting;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.stickman.fighting.screens.MainMenuScreen;
import com.stickman.fighting.utils.SoundManager;

public class MyFightingGame extends Game {

    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Khởi tạo SoundManager TRƯỚC khi vào màn hình đầu tiên
        // (LibGDX audio context đã sẵn sàng tại đây)
        SoundManager.getInstance().initialize();

        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        SoundManager.getInstance().dispose();
        if (screen != null) screen.dispose();
    }
}
