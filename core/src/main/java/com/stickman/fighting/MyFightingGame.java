// MyFightingGame.java — thay thế hoàn toàn
package com.stickman.fighting;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.stickman.fighting.screens.MainMenuScreen;
import com.stickman.fighting.screens.SettingScreen;
import com.stickman.fighting.utils.SoundManager;
import com.stickman.fighting.utils.UiQaConfig;

public class MyFightingGame extends Game {

    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Khởi tạo SoundManager TRƯỚC khi vào màn hình đầu tiên
        // (LibGDX audio context đã sẵn sàng tại đây)
        SoundManager.getInstance().initialize();

        if (UiQaConfig.autoOpenSettingsScreen()) {
            MainMenuScreen mainMenuScreen = new MainMenuScreen(this);
            setScreen(mainMenuScreen);
            setScreen(new SettingScreen(this, mainMenuScreen));
            return;
        }

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
        com.stickman.fighting.particles.ParticleSystem.getInstance().dispose();
        if (screen != null) screen.dispose();
    }
}
