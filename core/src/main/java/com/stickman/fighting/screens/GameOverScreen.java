package com.stickman.fighting.screens;

import com.stickman.fighting.utils.SoundManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.stickman.fighting.MyFightingGame;
import com.stickman.fighting.ui.WoodenSkin;
import com.stickman.fighting.utils.Constants;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Màn hình K.O. – hiển thị người thắng và cho phép chơi lại.
 */
public class GameOverScreen implements Screen {

    private final MyFightingGame game;
    private final int winnerIndex, scoreP1, scoreP2;
    private final boolean twoPlayerMode;
    private Stage stage;
    private Skin  skin;
    private Texture panelTexture;
    private Texture dimTexture;

    public GameOverScreen(MyFightingGame game,
                          int winnerIndex, int scoreP1, int scoreP2) {
        this(game, winnerIndex, scoreP1, scoreP2, false);
    }

    public GameOverScreen(MyFightingGame game,
                          int winnerIndex, int scoreP1, int scoreP2,
                          boolean twoPlayerMode) {
        this.game        = game;
        this.winnerIndex = winnerIndex;
        this.scoreP1     = scoreP1;
        this.scoreP2     = scoreP2;
        this.twoPlayerMode = twoPlayerMode;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT));
        Gdx.input.setInputProcessor(stage);
        skin  = WoodenSkin.create();
        buildUI();

        SoundManager sm = SoundManager.getInstance();
        sm.playSound(SoundManager.SoundEffect.KO);
        // Delay nhỏ rồi phát nhạc game over (dùng Timer của LibGDX)
        com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
            @Override public void run() {
                sm.playMusic(SoundManager.MusicTrack.GAME_OVER, false);
            }
        }, 1.0f);
    }

    private void buildUI() {
        dimTexture = makeSolidTexture(new Color(0f, 0f, 0f, 0.35f));
        Image dim = new Image(dimTexture);
        dim.setFillParent(true);
        stage.addActor(dim);

        Table root = new Table();
        root.setFillParent(true);
        root.center();
        root.setTransform(true);
        root.setScale(0.92f);
        root.getColor().a = 0f;

        Table panel = new Table();
        panelTexture = makePanelTexture();
        panel.setBackground(new TextureRegionDrawable(new TextureRegion(panelTexture)));
        panel.pad(24, 26, 24, 26);

        // K.O. label
        Label koLabel = new Label("K.O.", skin, "title");
        koLabel.setColor(new Color(1f, 0.60f, 0.15f, 1f));
        koLabel.setAlignment(Align.center);

        // Winner text
        String winText = winnerIndex == 0
            ? "Hòa!"
            : "Người chơi " + winnerIndex + " Thắng!";
        Label winLabel = new Label(winText, skin);
        winLabel.setAlignment(Align.center);

        // Score
        Label scoreLabel = new Label(scoreP1 + "  -  " + scoreP2, skin);
        scoreLabel.setAlignment(Align.center);

        TextButton btnRematch = new TextButton("ĐẤU LẠI",  skin, "primary");
        TextButton btnBack    = new TextButton("QUAY LẠI", skin, "light");

        btnRematch.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new PlayScreen(game, twoPlayerMode));
            }
        });
        btnBack.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        panel.add(koLabel).padBottom(12).row();
        panel.add(winLabel).padBottom(8).row();
        panel.add(scoreLabel).padBottom(30).row();
        panel.add(btnRematch).width(272).height(66).padBottom(12).row();
        panel.add(btnBack).width(272).height(58);

        root.add(panel);

        stage.addActor(root);
        root.addAction(Actions.parallel(
            Actions.fadeIn(0.28f),
            Actions.scaleTo(1f, 1f, 0.28f)
        ));
    }

    private Texture makePanelTexture() {
        if (Gdx.files.internal("panel_wood.png").exists()) {
            return new Texture(Gdx.files.internal("panel_wood.png"));
        }

        Pixmap pm = new Pixmap(340, 340, Pixmap.Format.RGBA8888);
        pm.setColor(0.56f, 0.32f, 0.10f, 0.96f);
        pm.fill();
        pm.setColor(0.27f, 0.14f, 0.04f, 1f);
        pm.drawRectangle(0, 0, 340, 340);
        pm.drawRectangle(2, 2, 336, 336);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private Texture makeSolidTexture(Color c) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.04f, 0.01f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}
    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
        if (panelTexture != null) panelTexture.dispose();
        if (dimTexture != null) dimTexture.dispose();
    }
}
