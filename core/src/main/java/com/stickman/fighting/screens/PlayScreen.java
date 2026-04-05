package com.stickman.fighting.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.stickman.fighting.MyFightingGame;
import com.stickman.fighting.entities.BotFighter;
import com.stickman.fighting.entities.Fighter;
import com.stickman.fighting.entities.Fighter.AttackType;
import com.stickman.fighting.entities.PlayerFighter;
import com.stickman.fighting.particles.ParticleSystem;
import com.stickman.fighting.ui.WoodenSkin;
import com.stickman.fighting.utils.Constants;
import com.stickman.fighting.utils.GameSettings;
import com.stickman.fighting.utils.SoundManager;
import java.util.ArrayDeque;

/**
 * PlayScreen – Màn hình chiến đấu chính.
 */
public class PlayScreen implements Screen {

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final MyFightingGame game;
    private final boolean        twoPlayerMode;

    // ── Render ────────────────────────────────────────────────────────────────
    private ShapeRenderer shapeRenderer;
    private BitmapFont    hudFont;
    private BitmapFont    timerFont;
    private GlyphLayout   glyphLayout;
    private Texture       bgTexture;

    // ── Scene2D ───────────────────────────────────────────────────────────────
    private Stage stage;
    private Skin  skin;

    // HUD widgets
    private ProgressBar hpBarP1;
    private ProgressBar hpBarP2;
    private Label       timerLabel;
    private Label       scoreLabel;
    private float       displayedHpP1 = 1f;
    private float       displayedHpP2 = 1f;

    // Pause overlay
    private Table   pauseOverlay;
    private boolean paused = false;

    // ── Entities ──────────────────────────────────────────────────────────────
    private PlayerFighter player1;
    private Fighter       player2;

    // ── Game State ────────────────────────────────────────────────────────────
    private float roundTimeLeft;
    private int   scoreP1 = 0;
    private int   scoreP2 = 0;

    private float koFreezeTimer = -1f;
    private static final float KO_FREEZE_DURATION = 1.8f;
    private int winnerIndex = 0;

    // ── Textures (memory leak fix) ────────────────────────────────────────────
    private Texture hpBgTexture;
    private Texture hpBarP1Texture;
    private Texture hpBarP2Texture;
    private Texture hpFrameLeftTexture;
    private Texture hpFrameRightTexture;
    private Texture hudBadgeTexture;
    private Texture pausePanelTexture;
    private Texture pauseIconTexture;
    private Texture dimOverlayTexture;

    // ── Attack tracking (SFX) ─────────────────────────────────────────────────
    private boolean player1WasAttacking = false;
    private boolean player2WasAttacking = false;

    // ── Constructor ───────────────────────────────────────────────────────────
    public PlayScreen(MyFightingGame game, boolean twoPlayerMode) {
        this.game          = game;
        this.twoPlayerMode = twoPlayerMode;
    }

    // ── Vòng đời Screen ───────────────────────────────────────────────────────

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        hudFont       = new BitmapFont();
        hudFont.getData().setScale(1.6f);
        timerFont     = new BitmapFont();
        timerFont.getData().setScale(2.8f);
        glyphLayout   = new GlyphLayout();
        bgTexture     = createArenaTexture();

        stage = new Stage(new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT));
        skin  = WoodenSkin.create();

        buildHUD();
        buildPauseOverlay();

        Gdx.input.setInputProcessor(stage);

        spawnFighters();

        roundTimeLeft = GameSettings.getInstance().getRoundTime();

        SoundManager sm = SoundManager.getInstance();
        sm.playMusic(SoundManager.MusicTrack.BATTLE);
        sm.playSound(SoundManager.SoundEffect.ROUND_START);
    }

    @Override
    public void render(float delta) {
        // ESC → toggle pause
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePause();
        }

        // Chỉ update khi không pause và chưa KO
        if (!paused && koFreezeTimer < 0f) {
            update(delta);
        }

        // Đếm ngược sau KO
        if (koFreezeTimer > 0f) {
            koFreezeTimer -= delta;
            if (koFreezeTimer <= 0f) {
                goToGameOver();
                return;
            }
        }

        draw();
    }

    @Override
    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }

    @Override public void pause()  { paused = true; }
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        hudFont.dispose();
        timerFont.dispose();
        stage.dispose();
        skin.dispose();
        if (bgTexture       != null) bgTexture.dispose();
        if (hpBgTexture     != null) hpBgTexture.dispose();
        if (hpBarP1Texture  != null) hpBarP1Texture.dispose();
        if (hpBarP2Texture  != null) hpBarP2Texture.dispose();
        if (hpFrameLeftTexture  != null) hpFrameLeftTexture.dispose();
        if (hpFrameRightTexture != null) hpFrameRightTexture.dispose();
        if (hudBadgeTexture     != null) hudBadgeTexture.dispose();
        if (pausePanelTexture   != null) pausePanelTexture.dispose();
        if (pauseIconTexture    != null) pauseIconTexture.dispose();
        if (dimOverlayTexture   != null) dimOverlayTexture.dispose();
        ParticleSystem.getInstance().clear();
    }

    // ── Update ────────────────────────────────────────────────────────────────

    private void update(float delta) {
        // Lưu trạng thái attack trước khi update
        player1WasAttacking = player1.isAttacking();
        player2WasAttacking = player2.isAttacking();

        player1.update(delta);
        player2.update(delta);

        // Xoay mặt về phía đối thủ
        boolean p1FaceRight = player2.getCenterX() >= player1.getCenterX();
        if (!player1.isAttacking()) player1.setFacingRight(p1FaceRight);
        if (!player2.isAttacking()) player2.setFacingRight(!p1FaceRight);

        // Va chạm thân
        resolveBodyCollision();

        // Kiểm tra đòn đánh
        player1.checkHit(player2);
        player2.checkHit(player1);

        // SFX tấn công (rising edge: false → true)
        SoundManager sm = SoundManager.getInstance();
        if (player1.isAttacking() && !player1WasAttacking) {
            AttackType t = player1.getCurrentAttackType();
            if (t == AttackType.KICK) {
                sm.playSoundWithVariation(SoundManager.SoundEffect.KICK, 0.95f);
            } else {
                sm.playSoundWithVariation(SoundManager.SoundEffect.PUNCH, 1.0f);
            }
        }
        if (player2.isAttacking() && !player2WasAttacking) {
            AttackType t = player2.getCurrentAttackType();
            if (t == AttackType.KICK) {
                sm.playSoundWithVariation(SoundManager.SoundEffect.KICK, 0.95f);
            } else {
                sm.playSoundWithVariation(SoundManager.SoundEffect.PUNCH, 1.0f);
            }
        }

        // Timer
        roundTimeLeft -= delta;
        if (roundTimeLeft < 0f) roundTimeLeft = 0f;

        // Cập nhật HUD
        updateHUD();

        // Kiểm tra KO / hết giờ
        checkRoundEnd();

        // Cập nhật particle
        ParticleSystem.getInstance().update(delta);
    }

    private void resolveBodyCollision() {
        if (!player1.getBounds().overlaps(player2.getBounds())) return;

        float p1Cx   = player1.getCenterX();
        float p2Cx   = player2.getCenterX();
        float overlap = (Fighter.WIDTH - Math.abs(p1Cx - p2Cx)) / 2f + 1f;

        if (p1Cx < p2Cx) {
            player1.offsetX(-overlap);
            player2.offsetX(overlap);
        } else {
            player1.offsetX(overlap);
            player2.offsetX(-overlap);
        }
    }

    private void checkRoundEnd() {
        if (player1.isDead() && koFreezeTimer < 0f) {
            scoreP2++;
            winnerIndex = 2;
            triggerKO();
        } else if (player2.isDead() && koFreezeTimer < 0f) {
            scoreP1++;
            winnerIndex = 1;
            triggerKO();
        } else if (roundTimeLeft <= 0f && koFreezeTimer < 0f) {
            if (player1.getHp() > player2.getHp()) {
                scoreP1++;
                winnerIndex = 1;
            } else if (player2.getHp() > player1.getHp()) {
                scoreP2++;
                winnerIndex = 2;
            } else {
                winnerIndex = 0; // Hòa khi hết giờ và HP bằng nhau
            }
            triggerKO();
        }
    }

    private void triggerKO() {
        koFreezeTimer = KO_FREEZE_DURATION;
        updateScoreLabel();

        if (winnerIndex == 0) {
            float midX = (player1.getCenterX() + player2.getCenterX()) * 0.5f;
            float midY = (player1.getCenterY() + player2.getCenterY()) * 0.5f + 20f;
            ParticleSystem.getInstance().emitKO(midX, midY);
            return;
        }

        Fighter loser;
        if (player1.isDead()) {
            loser = player1;
        } else if (player2.isDead()) {
            loser = player2;
        } else if (player1.getHp() < player2.getHp()) {
            loser = player1;
        } else {
            loser = player2;
        }
        ParticleSystem.getInstance().emitKO(
            loser.getCenterX(), loser.getCenterY() + 20f);
    }

    private void goToGameOver() {
        game.setScreen(new GameOverScreen(
            game, winnerIndex, scoreP1, scoreP2, twoPlayerMode));
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    private void draw() {
        Gdx.gl.glClearColor(0.1f, 0.06f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1. Background
        game.batch.setProjectionMatrix(stage.getCamera().combined);
        game.batch.begin();
        game.batch.draw(bgTexture, 0, 0,
            Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        game.batch.end();

        // 2. Fighters + Particles
        renderFighters();

        // 3. KO overlay
        if (koFreezeTimer > 0f) {
            drawKOOverlay();
        }

        // 4. HUD (luôn trên cùng)
        // Vẫn act khi pause để overlay/animation và input UI hoạt động bình thường.
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    private void renderFighters() {
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);

        // Pass 1: Filled (đầu tròn)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        player1.renderFilled(shapeRenderer);
        player2.renderFilled(shapeRenderer);
        shapeRenderer.end();

        // Pass 2: Lines (thân, tay, chân)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        player1.renderLines(shapeRenderer);
        player2.renderLines(shapeRenderer);
        shapeRenderer.end();

        // Pass 3: Particles
        ParticleSystem.getInstance().render(shapeRenderer);
    }

    private void drawKOOverlay() {
        game.batch.setProjectionMatrix(stage.getCamera().combined);
        game.batch.begin();
        timerFont.setColor(Color.ORANGE);
        glyphLayout.setText(timerFont, "K.O.!");
        timerFont.draw(game.batch, "K.O.!",
            (Constants.SCREEN_WIDTH  - glyphLayout.width)  / 2f,
            Constants.SCREEN_HEIGHT * 0.68f);
        game.batch.end();
    }

    // ── HUD ───────────────────────────────────────────────────────────────────

    private void buildHUD() {
        Table root = new Table();
        root.setFillParent(true);
        root.top().pad(12);

        // HP bars
        ProgressBar.ProgressBarStyle p1Style = makeHpBarStyle(
            new Color(0.2f, 0.6f, 1f, 1f), true);
        hpBarP1 = new ProgressBar(0f, 1f, 0.01f, false, p1Style);
        hpBarP1.setValue(1f);

        ProgressBar.ProgressBarStyle p2Style = makeHpBarStyle(
            new Color(1f, 0.25f, 0.25f, 1f), false);
        hpBarP2 = new ProgressBar(0f, 1f, 0.01f, false, p2Style);
        hpBarP2.setValue(1f);

        BitmapFont scoreFont = WoodenSkin.createUIFont(42);
        BitmapFont timerFontUi = WoodenSkin.createUIFont(34);
        skin.add("hud-score-font", scoreFont, BitmapFont.class);
        skin.add("hud-timer-font", timerFontUi, BitmapFont.class);
        skin.add("hudScore", new Label.LabelStyle(scoreFont, new Color(1f, 0.94f, 0.78f, 1f)), Label.LabelStyle.class);
        skin.add("hudTimer", new Label.LabelStyle(timerFontUi, new Color(1f, 0.88f, 0.50f, 1f)), Label.LabelStyle.class);

        // Labels P1 / P2
        Label labelP1 = new Label("P1", skin);
        labelP1.setColor(new Color(0.2f, 0.6f, 1f, 1f));
        labelP1.setFontScale(1.1f);

        Label labelP2 = new Label("P2", skin);
        labelP2.setColor(new Color(1f, 0.25f, 0.25f, 1f));
        labelP2.setFontScale(1.1f);

        // Bảng giữa: Score + Timer
        Table midTable = new Table();
        scoreLabel = new Label(scoreP1 + " - " + scoreP2, skin, "hudScore");
        scoreLabel.setAlignment(Align.center);
        timerLabel = new Label(String.valueOf((int) roundTimeLeft), skin, "hudTimer");
        timerLabel.setAlignment(Align.center);

        Table timerBox = new Table();
        timerBox.setBackground(new TextureRegionDrawable(new TextureRegion(makeHudBadgeTexture())));
        timerBox.pad(5f, 16f, 5f, 16f);
        timerBox.add(timerLabel).center();

        midTable.add(scoreLabel).expandX().center().padBottom(2f).row();
        midTable.add(timerBox).expandX().center();

        // Nút Pause
        TextButton btnPause = new TextButton("|| PAUSE", skin, "light");
        btnPause.getLabel().setFontScale(0.9f);
        btnPause.setTransform(true);
        btnPause.setOrigin(Align.center);
        btnPause.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a) { togglePause(); }
        });

        // Hàng 1: chỉ nút Pause ở góc trên bên phải
        Table pauseRow = new Table();
        pauseRow.add().expandX().fillX();
        pauseRow.add(btnPause).width(146).height(48).right();

        // Hàng 2: thông tin trận đấu + thanh máu đẩy sát hai mép hơn
        Table leftHud = new Table();
        leftHud.add(labelP1).padRight(6f);
        leftHud.add(hpBarP1).height(24).expandX().fillX().minWidth(300f);

        Table rightHud = new Table();
        rightHud.right();
        rightHud.add(hpBarP2).height(24).expandX().fillX().minWidth(300f).padRight(6f);
        rightHud.add(labelP2);

        Table battleHudRow = new Table();
        battleHudRow.add(leftHud).expandX().fillX().padLeft(4f).padRight(8f);
        battleHudRow.add(midTable).width(120).padLeft(8f).padRight(8f);
        battleHudRow.add(rightHud).expandX().fillX().padLeft(8f).padRight(4f);

        root.add(pauseRow).expandX().fillX().row();
        root.add(battleHudRow).expandX().fillX().padTop(8f);
        stage.addActor(root);
    }

    private void updateHUD() {
        float smoothing = 0.18f;
        displayedHpP1 += (player1.getHpPercent() - displayedHpP1) * smoothing;
        displayedHpP2 += (player2.getHpPercent() - displayedHpP2) * smoothing;

        hpBarP1.setValue(displayedHpP1);
        hpBarP2.setValue(displayedHpP2);
        timerLabel.setText(String.valueOf((int) Math.ceil(roundTimeLeft)));
    }

    private void updateScoreLabel() {
        scoreLabel.setText(scoreP1 + " - " + scoreP2);
    }

    // ── Pause Overlay ─────────────────────────────────────────────────────────

    private void buildPauseOverlay() {
        // Dim background
        Pixmap dimPm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        dimPm.setColor(0f, 0f, 0f, 0.55f);
        dimPm.fill();
        dimOverlayTexture = new Texture(dimPm);
        dimPm.dispose();

        Image dimBg = new Image(dimOverlayTexture);
        dimBg.setFillParent(true);
        dimBg.setName("dimBg");
        dimBg.setVisible(false);
        dimBg.getColor().a = 0f;

        // Panel gỗ
        pauseOverlay = new Table();
        pauseOverlay.setFillParent(true);
        pauseOverlay.center();
        pauseOverlay.setName("pausePanel");
        pauseOverlay.setVisible(false);
        pauseOverlay.setTransform(true);
        pauseOverlay.setScale(0.92f);
        pauseOverlay.getColor().a = 0f;

        Label pauseTitle = new Label("TẠM DỪNG", skin, "title");
        pauseTitle.setAlignment(Align.center);

        TextButton btnResume  = new TextButton("TIẾP TỤC", skin, "resume");
        TextButton btnRestart = new TextButton("CHƠI LẠI", skin, "restart");
        TextButton btnQuit    = new TextButton("THOÁT",    skin, "quit");

        btnResume.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a) { togglePause(); }
        });
        btnRestart.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new PlayScreen(game, twoPlayerMode));
            }
        });
        btnQuit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        Table woodPanel = new Table();
        woodPanel.setBackground(loadPausePanelBackground());
        woodPanel.pad(34, 36, 34, 36);
        woodPanel.add(pauseTitle).padBottom(24).row();
        woodPanel.add(btnResume).width(300).height(64).padBottom(14).row();
        woodPanel.add(btnRestart).width(300).height(64).padBottom(14).row();
        woodPanel.add(btnQuit).width(300).height(64);

        pauseOverlay.add(woodPanel);

        stage.addActor(dimBg);
        stage.addActor(pauseOverlay);
    }

    private void togglePause() {
        paused = !paused;
        Actor dim = stage.getRoot().findActor("dimBg");
        if (dim != null) {
            dim.clearActions();
        }
        pauseOverlay.clearActions();

        if (paused) {
            pauseOverlay.setVisible(true);
            if (dim != null) {
                dim.setVisible(true);
                dim.addAction(Actions.fadeIn(0.2f));
            }
            pauseOverlay.setScale(0.9f);
            pauseOverlay.getColor().a = 0f;
            pauseOverlay.addAction(Actions.parallel(
                Actions.fadeIn(0.24f),
                Actions.scaleTo(1f, 1f, 0.24f)
            ));
        } else {
            if (dim != null) {
                dim.addAction(Actions.sequence(
                    Actions.fadeOut(0.2f),
                    Actions.visible(false)
                ));
            }
            pauseOverlay.addAction(Actions.sequence(
                Actions.parallel(
                    Actions.fadeOut(0.2f),
                    Actions.scaleTo(0.92f, 0.92f, 0.2f)
                ),
                Actions.visible(false)
            ));
        }

        SoundManager sm = SoundManager.getInstance();
        if (paused) sm.pauseMusic();
        else        sm.resumeMusic();
    }

    // ── Spawn ─────────────────────────────────────────────────────────────────

    private void spawnFighters() {
        float p1X    = Constants.SCREEN_WIDTH * 0.25f;
        float p2X    = Constants.SCREEN_WIDTH * 0.65f;
        float startY = Constants.GROUND_Y;
        float hpScale = GameSettings.getInstance().getHpScale();

        player1 = new PlayerFighter(
            p1X, startY,
            PlayerFighter.PlayerIndex.PLAYER_ONE,
            new Color(0.2f, 0.6f, 1f, 1f));
        player1.setMaxHpScale(hpScale);
        player1.setFacingRight(true);

        if (twoPlayerMode) {
            player2 = new PlayerFighter(
                p2X, startY,
                PlayerFighter.PlayerIndex.PLAYER_TWO,
                new Color(1f, 0.25f, 0.25f, 1f));
        } else {
            player2 = new BotFighter(
                p2X, startY,
                new Color(1f, 0.25f, 0.25f, 1f),
                player1,
                BotFighter.Difficulty.NORMAL);
        }
        player2.setMaxHpScale(hpScale);
        player2.setFacingRight(false);
    }

    // ── Texture Helpers ───────────────────────────────────────────────────────

    private ProgressBar.ProgressBarStyle makeHpBarStyle(Color barColor, boolean isP1) {
        if (hpBgTexture == null) hpBgTexture = loadOrCreate("hp_frame_left.png", 1, 1,
            new Color(0.18f, 0.18f, 0.18f, 0.9f));
        if (hpFrameLeftTexture == null) hpFrameLeftTexture = loadOrCreate("hp_frame_left.png", 300, 22,
            new Color(0.18f, 0.18f, 0.18f, 0.9f));
        if (hpFrameRightTexture == null) hpFrameRightTexture = loadOrCreate("hp_frame_right.png", 300, 22,
            new Color(0.18f, 0.18f, 0.18f, 0.9f));

        Texture barTex;
        if (isP1) {
            hpBarP1Texture = loadOrCreate("hp_fill_blue.png", 300, 18, barColor);
            barTex = hpBarP1Texture;
        } else {
            hpBarP2Texture = loadOrCreate("hp_fill_red.png", 300, 18, barColor);
            barTex = hpBarP2Texture;
        }

        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        style.background = new TextureRegionDrawable(new TextureRegion(
            isP1 ? hpFrameLeftTexture : hpFrameRightTexture));
        style.knobBefore = new TextureRegionDrawable(new TextureRegion(barTex));
        return style;
    }

    private Texture makePanelTexture() {
        Pixmap pm = new Pixmap(300, 320, Pixmap.Format.RGBA8888);
        pm.setColor(0.55f, 0.32f, 0.10f, 0.95f);
        pm.fill();

        for (int y = 10; y < 320; y += 20) {
            pm.setColor(0.40f, 0.22f, 0.08f, 0.28f);
            pm.drawLine(6, y, 294, y);
        }

        pm.setColor(0.30f, 0.15f, 0.04f, 1f);
        pm.drawRectangle(0, 0, 300, 320);
        pm.drawRectangle(3, 3, 294, 314);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private Texture makeHudBadgeTexture() {
        if (hudBadgeTexture != null) {
            return hudBadgeTexture;
        }
        Pixmap pm = new Pixmap(160, 56, Pixmap.Format.RGBA8888);
        pm.setColor(0.16f, 0.11f, 0.08f, 0.92f);
        pm.fill();
        pm.setColor(0.78f, 0.60f, 0.30f, 1f);
        pm.drawRectangle(0, 0, 160, 56);
        pm.drawRectangle(2, 2, 156, 52);
        hudBadgeTexture = new Texture(pm);
        pm.dispose();
        return hudBadgeTexture;
    }

    private Texture createArenaTexture() {
        if (Gdx.files.internal("background_battle.jpg").exists()) {
            return new Texture(Gdx.files.internal("background_battle.jpg"));
        }
        if (Gdx.files.internal("background.jpg").exists()) {
            return new Texture(Gdx.files.internal("background.jpg"));
        }

        int w = Constants.SCREEN_WIDTH;
        int h = Constants.SCREEN_HEIGHT;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        // Tường nền
        pm.setColor(0.38f, 0.25f, 0.10f, 1f);
        pm.fill();

        // Sàn gỗ
        pm.setColor(0.52f, 0.33f, 0.12f, 1f);
        pm.fillRectangle(0, 0, w, (int)(Constants.GROUND_Y + 10));

        // Vân sàn
        pm.setColor(0.42f, 0.26f, 0.09f, 1f);
        for (int x = 0; x < w; x += 60) {
            pm.drawLine(x, 0, x, (int) Constants.GROUND_Y);
        }

        // Đường phân cách sàn / tường
        pm.setColor(0.22f, 0.12f, 0.04f, 1f);
        pm.fillRectangle(0, (int) Constants.GROUND_Y, w, 4);

        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private Button buildIconButton(String iconFile, String fallbackText, String fallbackStyle) {
        if (Gdx.files.internal(iconFile).exists()) {
            Texture t = new Texture(Gdx.files.internal(iconFile));
            if ("pause_icon.png".equals(iconFile) || "icon_pause.png".equals(iconFile)) {
                pauseIconTexture = t;
            }
            ImageButton.ImageButtonStyle st = new ImageButton.ImageButtonStyle();
            st.imageUp = new TextureRegionDrawable(new TextureRegion(t));
            st.imageOver = st.imageUp;
            st.imageDown = st.imageUp;
            return new ImageButton(st);
        }
        return new TextButton(fallbackText, skin, fallbackStyle);
    }

    private TextureRegionDrawable loadPausePanelBackground() {
        if (Gdx.files.internal("panel_hanging.png").exists()) {
            // panel_hanging hiện có nền caro trắng/xám bị bake vào ảnh,
            // loại bỏ phần nền edge-connected để trả về trong suốt thật.
            pausePanelTexture = loadPanelTextureWithoutChecker("panel_hanging.png");
            return new TextureRegionDrawable(new TextureRegion(pausePanelTexture));
        }
        if (Gdx.files.internal("panel_wood.png").exists()) {
            pausePanelTexture = new Texture(Gdx.files.internal("panel_wood.png"));
            return new TextureRegionDrawable(new TextureRegion(pausePanelTexture));
        }
        pausePanelTexture = makePanelTexture();
        return new TextureRegionDrawable(new TextureRegion(pausePanelTexture));
    }

    private Texture loadPanelTextureWithoutChecker(String fileName) {
        Pixmap pm = new Pixmap(Gdx.files.internal(fileName));
        stripEdgeCheckerboard(pm);
        Texture texture = new Texture(pm);
        pm.dispose();
        return texture;
    }

    private void stripEdgeCheckerboard(Pixmap pm) {
        int w = pm.getWidth();
        int h = pm.getHeight();
        boolean[] visited = new boolean[w * h];
        ArrayDeque<Integer> queue = new ArrayDeque<>();

        for (int x = 0; x < w; x++) {
            enqueueChecker(pm, x, 0, visited, queue, w, h);
            enqueueChecker(pm, x, h - 1, visited, queue, w, h);
        }
        for (int y = 1; y < h - 1; y++) {
            enqueueChecker(pm, 0, y, visited, queue, w, h);
            enqueueChecker(pm, w - 1, y, visited, queue, w, h);
        }

        while (!queue.isEmpty()) {
            int idx = queue.removeFirst();
            int x = idx % w;
            int y = idx / w;

            int pixel = pm.getPixel(x, y);
            pm.drawPixel(x, y, pixel & 0xFFFFFF00);

            enqueueChecker(pm, x + 1, y, visited, queue, w, h);
            enqueueChecker(pm, x - 1, y, visited, queue, w, h);
            enqueueChecker(pm, x, y + 1, visited, queue, w, h);
            enqueueChecker(pm, x, y - 1, visited, queue, w, h);
        }
    }

    private void enqueueChecker(
        Pixmap pm,
        int x,
        int y,
        boolean[] visited,
        ArrayDeque<Integer> queue,
        int w,
        int h) {

        if (x < 0 || x >= w || y < 0 || y >= h) return;
        int idx = y * w + x;
        if (visited[idx]) return;

        int pixel = pm.getPixel(x, y);
        if (!isCheckerLikePixel(pixel)) return;

        visited[idx] = true;
        queue.addLast(idx);
    }

    private boolean isCheckerLikePixel(int pixel) {
        int a = pixel & 0xFF;
        if (a < 220) return false;

        int r = (pixel >>> 24) & 0xFF;
        int g = (pixel >>> 16) & 0xFF;
        int b = (pixel >>> 8) & 0xFF;
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        int avg = (r + g + b) / 3;

        return avg >= 170 && avg <= 245 && (max - min) <= 18;
    }

    private Texture loadOrCreate(String fileName, int w, int h, Color fill) {
        if (Gdx.files.internal(fileName).exists()) {
            return new Texture(Gdx.files.internal(fileName));
        }
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(fill);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }
}
