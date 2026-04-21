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
import com.stickman.fighting.entities.EnergyProjectile;
import com.stickman.fighting.entities.Fighter;
import com.stickman.fighting.entities.Fighter.AttackType;
import com.stickman.fighting.entities.PlayerFighter;
import com.stickman.fighting.particles.ParticleSystem;
import com.stickman.fighting.ui.WoodenSkin;
import com.stickman.fighting.utils.Constants;
import com.stickman.fighting.utils.GameSettings;
import com.stickman.fighting.utils.SoundManager;
import com.stickman.fighting.utils.I18n;
import java.util.ArrayDeque;
import java.util.List;
import com.stickman.fighting.map.Platform;

/**
 * PlayScreen – Màn hình chiến đấu chính.
 */
public class PlayScreen implements Screen {

    // —— Dependencies ——————————————————————————————————————————————————————
    private final MyFightingGame game;
    private final boolean twoPlayerMode;

    // —— Render ————————————————————————————————————————————————————————————————
    private ShapeRenderer shapeRenderer;
    private BitmapFont hudFont;
    private BitmapFont timerFont;
    private GlyphLayout glyphLayout;
    private Texture bgTexture;

    // —— Scene2D ———————————————————————————————————————————————————————————————
    private Stage stage;
    private Skin skin;

    // HUD widgets
    private ProgressBar hpBarP1;
    private ProgressBar hpBarP2;
    private ProgressBar energyBarP1;
    private ProgressBar energyBarP2;
    private Label timerLabel;
    private Label scoreLabel;
    private Label scoreShadowLabel; // MỚI: Dùng để tạo hiệu ứng đổ bóng cho Tỉ số
    private float displayedHpP1 = 1f;
    private float displayedHpP2 = 1f;
    private float displayedEnergyP1 = 1f;
    private float displayedEnergyP2 = 1f;

    // Pause overlay
    private Table pauseOverlay;
    private boolean paused = false;

    // —— Entities ——————————————————————————————————————————————————————————————
    private PlayerFighter player1;
    private Fighter player2;

    // —— Game State ————————————————————————————————————————————————————————————
    private float roundTimeLeft;
    private int scoreP1 = 0;
    private int scoreP2 = 0;
    private int mapId;
    private List<Platform> platforms;

    private boolean isGameOver = false;
    private int winnerIndex = 0;

    // —— Textures (memory leak fix) ————————————————————————————————————————————
    private Texture hpBgTexture;
    private Texture hpBarP1Texture;
    private Texture hpBarP2Texture;
    private Texture energyBgTexture;
    private Texture energyBarP1Texture;
    private Texture energyBarP2Texture;
    private Texture hpFrameLeftTexture;
    private Texture hpFrameRightTexture;
    private Texture hudBadgeTexture;
    private Texture pausePanelTexture;
    private Texture pauseIconTexture;
    private Texture dimOverlayTexture;
    private Texture emptyKnobTexture;

    // —— Attack tracking (SFX) —————————————————————————————————————————————————
    private boolean player1WasAttacking = false;
    private boolean player2WasAttacking = false;
    private final ArrayDeque<EnergyProjectile> energyProjectiles = new ArrayDeque<>();
    private final ArrayDeque<com.stickman.fighting.entities.ThrownWeapon> thrownWeapons = new ArrayDeque<>();

    // —— Constructor ———————————————————————————————————————————————————————————
    // Constructor 1: Dùng cho ván đầu tiên (Tỉ số bắt đầu từ 0-0)
    public PlayScreen(MyFightingGame game, boolean twoPlayerMode, int mapId) {
        this(game, twoPlayerMode, 0, 0, mapId);
    }

    // Constructor 2: Dùng cho các ván Đấu Lại (Nhận tỉ số cũ truyền vào)
    public PlayScreen(MyFightingGame game, boolean twoPlayerMode, int scoreP1, int scoreP2, int mapId) {
        this.game = game;
        this.twoPlayerMode = twoPlayerMode;
        this.scoreP1 = scoreP1;
        this.scoreP2 = scoreP2;
        this.mapId = mapId;
        this.platforms = com.stickman.fighting.map.MapManager.getPlatforms(mapId);
    }

    // —— Vòng đời Screen —————————————————————————————————————————————————————

    @Override
    public void show() {
        com.stickman.fighting.utils.WeaponRenderer.init();
        shapeRenderer = new ShapeRenderer();
        hudFont = new BitmapFont();
        hudFont.getData().setScale(1.6f);

        // THAY ĐỔI: Dùng font TTF chất lượng cao thay vì font mặc định bị vỡ nét
        timerFont = WoodenSkin.createUIFont(120);

        glyphLayout = new GlyphLayout();
        bgTexture = createArenaTexture();

        stage = new Stage(new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT));
        skin = WoodenSkin.create();

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
        // [CÁCH SỬA LỖI LAG]: Ngăn "Spiral of Death" khi game bị khựng
        // Ép delta tối đa chỉ bằng 0.05 giây (tương đương 20 FPS).
        // Nếu lag hơn, game sẽ chạy chậm lại thay vì nhảy cóc và gây tràn RAM.
        float safeDelta = Math.min(delta, 0.05f);

        // ESC → toggle pause (Chỉ cho phép bấm Pause khi trận đấu chưa kết thúc)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !isGameOver) {
            togglePause();
        }

        // Chỉ update game logic (đánh nhau) khi không pause và chưa game over
        if (!paused && !isGameOver) {
            update(safeDelta); // DÙNG safeDelta thay vì delta
        }

        // Vẫn cập nhật HUD (để thanh máu tụt mượt mà) và Particle (máu/bụi bay) khi Game Over
        if (isGameOver) {
            updateHUD();
            ParticleSystem.getInstance().update(safeDelta); // DÙNG safeDelta thay vì delta
        }

        draw();
    }
    @Override
    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        hudFont.dispose();
        timerFont.dispose();
        stage.dispose();
        skin.dispose();
        if (bgTexture != null)
            bgTexture.dispose();
        if (hpBgTexture != null)
            hpBgTexture.dispose();
        if (hpBarP1Texture != null)
            hpBarP1Texture.dispose();
        if (hpBarP2Texture != null)
            hpBarP2Texture.dispose();
        if (energyBgTexture != null)
            energyBgTexture.dispose();
        if (energyBarP1Texture != null)
            energyBarP1Texture.dispose();
        if (energyBarP2Texture != null)
            energyBarP2Texture.dispose();
        if (hpFrameLeftTexture != null)
            hpFrameLeftTexture.dispose();
        if (hpFrameRightTexture != null)
            hpFrameRightTexture.dispose();
        if (hudBadgeTexture != null)
            hudBadgeTexture.dispose();
        if (pausePanelTexture != null)
            pausePanelTexture.dispose();
        if (pauseIconTexture != null)
            pauseIconTexture.dispose();
        if (dimOverlayTexture != null)
            dimOverlayTexture.dispose();
        if (emptyKnobTexture != null)
            emptyKnobTexture.dispose();
        ParticleSystem.getInstance().clear();
    }

    // —— Update ————————————————————————————————————————————————————————————————————————

    private void update(float delta) {
        // Lưu trạng thái attack trước khi update
        player1WasAttacking = player1.isAttacking();
        player2WasAttacking = player2.isAttacking();

        player1.update(delta, platforms);
        player2.update(delta, platforms);

        // Xoay mặt về phía đối thủ
        boolean p1FaceRight = player2.getCenterX() >= player1.getCenterX();
        if (!player1.isAttacking())
            player1.setFacingRight(p1FaceRight);
        if (!player2.isAttacking())
            player2.setFacingRight(!p1FaceRight);

        // Va chạm thân
        resolveBodyCollision();

        // Kiểm tra đòn đánh
        player1.checkHit(player2);
        player2.checkHit(player1);

        // SFX tấn công (rising edge: false → true)
        if (player1.isAttacking() && !player1WasAttacking) {
            onAttackStarted(player1);
        }
        if (player2.isAttacking() && !player2WasAttacking) {
            onAttackStarted(player2);
        }

        updateEnergyProjectiles(delta);
        updateThrownWeapons(delta);

        // Timer
        roundTimeLeft -= delta;
        if (roundTimeLeft < 0f)
            roundTimeLeft = 0f;

        // Cập nhật HUD
        updateHUD();

        // Kiểm tra KO / hết giờ
        checkRoundEnd();

        // Cập nhật particle
        ParticleSystem.getInstance().update(delta);
    }

    private void resolveBodyCollision() {
        if (!player1.getBounds().overlaps(player2.getBounds()))
            return;

        float p1Cx = player1.getCenterX();
        float p2Cx = player2.getCenterX();
        float overlap = (Fighter.WIDTH - Math.abs(p1Cx - p2Cx)) / 2f;

        if (p1Cx < p2Cx) {
            player1.offsetX(-overlap);
            player2.offsetX(overlap);
        } else {
            player1.offsetX(overlap);
            player2.offsetX(-overlap);
        }
    }

    private void checkRoundEnd() {
        if (isGameOver)
            return; // Trận đấu đã kết thúc thì không check nữa

        if (player1.isDead()) {
            scoreP2++;
            winnerIndex = 2;
            triggerGameOver();
        } else if (player2.isDead()) {
            scoreP1++;
            winnerIndex = 1;
            triggerGameOver();
        } else if (roundTimeLeft <= 0f) {
            if (player1.getHp() > player2.getHp()) {
                scoreP1++;
                winnerIndex = 1;
            } else if (player2.getHp() > player1.getHp()) {
                scoreP2++;
                winnerIndex = 2;
            } else {
                winnerIndex = 0; // Hòa
            }
            triggerGameOver();
        }
    }

    private void triggerGameOver() {
        isGameOver = true;
        updateScoreLabel();

        // 1. Tạo hiệu ứng Particle (máu/bụi) ngay tại vị trí người thua
        if (winnerIndex == 0) {
            float midX = (player1.getCenterX() + player2.getCenterX()) * 0.5f;
            float midY = (player1.getCenterY() + player2.getCenterY()) * 0.5f + 20f;
            ParticleSystem.getInstance().emitKO(midX, midY);
        } else {
            Fighter loser;
            if (player1.isDead())
                loser = player1;
            else if (player2.isDead())
                loser = player2;
            else
                loser = (player1.getHp() < player2.getHp()) ? player1 : player2;
            ParticleSystem.getInstance().emitKO(loser.getCenterX(), loser.getCenterY() + 20f);
        }

        // 2. Xử lý âm thanh
        SoundManager sm = SoundManager.getInstance();
        sm.pauseMusic(); // Dừng nhạc nền chiến đấu
        sm.playSound(SoundManager.SoundEffect.KO);

        com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
            @Override
            public void run() {
                sm.playMusic(SoundManager.MusicTrack.GAME_OVER, false);
            }
        }, 1.0f);

        // 3. Hiển thị bảng Game Over
        showGameOverOverlay();
    }

    private void showGameOverOverlay() {
        // Tận dụng lớp làm tối (dimBg) của Menu Pause
        Actor dim = stage.getRoot().findActor("dimBg");
        if (dim != null) {
            dim.setVisible(true);
            dim.addAction(Actions.fadeIn(0.4f));
        }

        // Tạo bảng UI chính
        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.center();
        overlay.setTransform(true);
        overlay.setScale(0.8f); // Thu nhỏ lúc đầu để làm hiệu ứng pop-up
        overlay.getColor().a = 0f;

        // Bảng gỗ
        Table woodPanel = new Table();
        if (Gdx.files.internal("panel_wood.png").exists()) {
            if (pausePanelTexture == null) {
                pausePanelTexture = new Texture(Gdx.files.internal("panel_wood.png"));
            }
            woodPanel.setBackground(new TextureRegionDrawable(new TextureRegion(pausePanelTexture)));
        } else {
            woodPanel.setBackground(loadPausePanelBackground());
        }
        woodPanel.pad(40, 50, 40, 50);

        // Chữ Tiêu đề KẾT THÚC (Dùng style "title" có sẵn trong skin)
        Label titleLabel = new Label(I18n.get("KẾT THÚC"), skin, "title");
        titleLabel.setColor(new Color(1.00f, 0.85f, 0.20f, 1f));
        titleLabel.setAlignment(Align.center);

        // Chữ Người thắng
        String winText = I18n.get("HÒA NHAU!");
        if (winnerIndex != 0) {
            if (!twoPlayerMode) {
                // Chế độ 1 người chơi (Đấu với máy)
                winText = (winnerIndex == 1) ? I18n.get("NGƯỜI CHƠI THẮNG !") : I18n.get("MÁY THẮNG !");
            } else {
                // Chế độ 2 người chơi
                winText = I18n.get("NGƯỜI CHƠI ") + winnerIndex + I18n.get(" THẮNG!");
            }
        }

        Label winLabel = new Label(winText, skin, "hudScore");
        winLabel.setColor(Color.WHITE);
        winLabel.setFontScale(0.85f);
        winLabel.setAlignment(Align.center);

        // Tỉ số (Xóa chữ, chỉ để số, phóng to và đổi màu nổi bật)
        // Dùng luôn style "hudScore" thay vì "hudTimer" để số trông dày dặn hơn
        Label scoreLabel = new Label(scoreP1 + " - " + scoreP2, skin, "hudScore");
        scoreLabel.setColor(new Color(1.00f, 0.85f, 0.20f, 1f)); // <-- Màu vàng (tone-sur-tone với chữ KẾT THÚC)
        scoreLabel.setFontScale(1.6f); // <-- Phóng to chữ số lên
        scoreLabel.setAlignment(Align.center);

        // Nút bấm
        TextButton btnRematch = new TextButton(I18n.get("ĐẤU LẠI"), skin, "restart");
        TextButton btnBack = new TextButton(I18n.get("VỀ MENU"), skin, "quit");

        btnRematch.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a) {
                // Truyền scoreP1 và scoreP2 hiện tại sang ván chơi mới
                game.setScreen(new PlayScreen(game, twoPlayerMode, scoreP1, scoreP2, mapId));
            }
        });
        btnBack.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        // Đẩy UI vào Panel gỗ
        woodPanel.add(titleLabel).padBottom(15).row();
        woodPanel.add(winLabel).padBottom(15).row();
        woodPanel.add(scoreLabel).padBottom(35).row(); // Tăng khoảng trống dưới số cho thoáng
        woodPanel.add(btnRematch).width(280).height(65).padBottom(15).row();
        woodPanel.add(btnBack).width(280).height(65);

        overlay.add(woodPanel);
        stage.addActor(overlay);

        // Hiệu ứng hiện ra mượt mà và nảy (swingOut)
        overlay.addAction(Actions.parallel(
                Actions.fadeIn(0.5f),
                Actions.scaleTo(1f, 1f, 0.5f, com.badlogic.gdx.math.Interpolation.swingOut)));
    }

    // —— Draw ——————————————————————————————————————————————————————————————————————————

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

        // 4. HUD (luôn trên cùng)
        // Vẫn act khi pause để overlay/animation và input UI hoạt động bình thường.
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    private void renderFighters() {
        // Bật Blending để vẽ bóng mờ (Alpha trong suốt)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);

        // GỘP CHUNG VÀO 1 PASS FILLED DUY NHẤT
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // --- Render Platforms ---
        for (Platform plat : platforms) {
            float px = plat.getX();
            float py = plat.getY();
            float pw = plat.getWidth();
            float ph = plat.getHeight();

            // 1. Viền đen
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.rect(px - 2, py - 2, pw + 4, ph + 4);

            // 2. Thân bục màu gỗ
            shapeRenderer.setColor(Constants.COLOR_WOOD_MID[0], Constants.COLOR_WOOD_MID[1], Constants.COLOR_WOOD_MID[2], Constants.COLOR_WOOD_MID[3]);
            shapeRenderer.rect(px, py, pw, ph);

            // 3. Bề mặt màu vàng/xanh sáng
            shapeRenderer.setColor(0.7f, 0.95f, 0.15f, 1f);
            shapeRenderer.rect(px, py + ph - 4f, pw, 4f);
        }

        // Vẽ đầu
        player1.renderFilled(shapeRenderer);
        player2.renderFilled(shapeRenderer);
        renderEnergyProjectiles(shapeRenderer);

        // Vẽ thân, tay, chân (dùng rectLine đặc ruột)
        player1.renderLines(shapeRenderer);
        player2.renderLines(shapeRenderer);

        shapeRenderer.end();

        // Pass 3: SpriteBatch (Dùng cho Sprite và Tuyệt chiêu vịnh nước)
        game.batch.begin();
        player1.renderWeapon(game.batch);
        player2.renderWeapon(game.batch);
        for (com.stickman.fighting.entities.ThrownWeapon weapon : thrownWeapons) {
            weapon.render(game.batch);
        }
        game.batch.end();

        // Pass Particles (Giữ nguyên)
        ParticleSystem.getInstance().render(shapeRenderer);

        // Tắt Blending sau khi vẽ xong
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void onAttackStarted(Fighter attacker) {
        AttackType type = attacker.getCurrentAttackType();
        SoundManager sm = SoundManager.getInstance();

        if (type == AttackType.KICK) {
            sm.playSoundWithVariation(SoundManager.SoundEffect.KICK, 0.95f);
        } else {
            sm.playSoundWithVariation(SoundManager.SoundEffect.PUNCH, 1.0f);
        }

        if (type == AttackType.ENERGY) {
            spawnEnergyProjectile(attacker);
        }
        if (type == AttackType.THROW_WEAPON) {
            spawnThrownWeapon(attacker);
        }
    }

    private void spawnThrownWeapon(Fighter attacker) {
        Fighter target = attacker == player1 ? player2 : player1;
        float startX = attacker.getCenterX();
        float startY = attacker.getPosition().y + Fighter.HEIGHT * 0.62f;

        thrownWeapons.addLast(new com.stickman.fighting.entities.ThrownWeapon(
                attacker,
                target,
                startX,
                startY));
    }

    private void updateThrownWeapons(float delta) {
        var it = thrownWeapons.iterator();
        while (it.hasNext()) {
            com.stickman.fighting.entities.ThrownWeapon weapon = it.next();
            weapon.update(delta);

            if (!weapon.isActive()) {
                it.remove();
                continue;
            }

            Fighter target = weapon.getOwner() == player1 ? player2 : player1;
            if (!target.isDead() && weapon.hits(target)) {
                target.receiveUltimateHit(weapon.getDamage());
                weapon.deactivate();
                it.remove();
                SoundManager.getInstance().playSound(SoundManager.SoundEffect.ENERGY_HIT);
            }
        }
    }

    private void spawnEnergyProjectile(Fighter attacker) {
        float dir = attacker.isFacingRight() ? 1f : -1f;
        float startX = attacker.getCenterX() + dir * (Fighter.WIDTH * 0.55f);
        float startY = attacker.getPosition().y + Fighter.HEIGHT * 0.62f;

        energyProjectiles.addLast(new EnergyProjectile(
                attacker,
                startX,
                startY,
                attacker.isFacingRight(),
                Constants.ENERGY_DAMAGE));

        // Giữ số lượng projectile ổn định trong trường hợp spam chiêu.
        while (energyProjectiles.size() > 24) {
            energyProjectiles.removeFirst();
        }
    }

    private void updateEnergyProjectiles(float delta) {
        var it = energyProjectiles.iterator();
        while (it.hasNext()) {
            EnergyProjectile projectile = it.next();
            projectile.update(delta);

            // Chặn đạn bay ra khỏi màn hình
            if (projectile.getX() < -200 || projectile.getX() > Constants.SCREEN_WIDTH + 200) {
                projectile.deactivate();
            }

            if (!projectile.isActive()) {
                it.remove();
                continue;
            }

            Fighter target = projectile.getOwner() == player1 ? player2 : player1;
            // KHI QUẢ CẦU TRÚNG ĐÍCH
            if (!target.isDead() && projectile.hits(target)) {
                target.receiveHit(projectile.getDamage());
                projectile.deactivate();
                it.remove();

                // [THÊM DÒNG NÀY VÀO ĐÂY] Phát tiếng nổ khi trúng địch
                SoundManager.getInstance().playSound(SoundManager.SoundEffect.ENERGY_HIT);
            }
        }
    }

    private void renderEnergyProjectiles(ShapeRenderer sr) {
        for (EnergyProjectile projectile : energyProjectiles) {
            projectile.render(sr);
        }
    }

    // —— HUD ———————————————————————————————————————————————————————————————————————————

    private void buildHUD() {
        Table root = new Table();
        root.setFillParent(true);
        root.top().padTop(2);

        // 1. Khởi tạo thanh máu
        ProgressBar.ProgressBarStyle p1Style = makeHpBarStyle(
                new Color(0.2f, 0.6f, 1f, 1f), true);
        hpBarP1 = new ProgressBar(0f, 1f, 0.01f, false, p1Style);
        hpBarP1.setValue(1f);

        ProgressBar.ProgressBarStyle p2Style = makeHpBarStyle(
                new Color(1f, 0.25f, 0.25f, 1f), false);
        hpBarP2 = new ProgressBar(0f, 1f, 0.01f, false, p2Style);
        // BẮT BUỘC LÀ 0f: Vì P2 chạy ngược, 0 có nghĩa là "Đầy máu"
        hpBarP2.setValue(0f);

        // Khởi tạo thanh Năng Lượng
        ProgressBar.ProgressBarStyle p1EnergyStyle = makeEnergyBarStyle(
                new Color(1f, 0.85f, 0f, 1f), true); // Vàng
        energyBarP1 = new ProgressBar(0f, 1f, 0.01f, false, p1EnergyStyle);
        energyBarP1.setValue(1f);

        ProgressBar.ProgressBarStyle p2EnergyStyle = makeEnergyBarStyle(
                new Color(1f, 0.85f, 0f, 1f), false); // Vàng
        energyBarP2 = new ProgressBar(0f, 1f, 0.01f, false, p2EnergyStyle);
        energyBarP2.setValue(0f);

        // Thiết lập Font chữ
        BitmapFont scoreFont = WoodenSkin.createUIFont(42);
        BitmapFont timerFontUi = WoodenSkin.createUIFont(34);
        skin.add("hud-score-font", scoreFont, BitmapFont.class);
        skin.add("hud-timer-font", timerFontUi, BitmapFont.class);

        // ĐỔI MÀU: Sửa màu cam thành màu Vàng Gold sáng (1f, 0.85f, 0f, 1f) cho nổi bật
        skin.add("hudScore", new Label.LabelStyle(scoreFont, Color.WHITE), Label.LabelStyle.class);
        skin.add("hudTimer", new Label.LabelStyle(timerFontUi, Color.WHITE), Label.LabelStyle.class);

        // ================= THAY THẾ TOÀN BỘ PHẦN DƯỚI NÀY =================

        // 2. TẠO HIỆU ỨNG CHỮ NỔI (DROP SHADOW) CHO TỶ SỐ

        // Lớp 1: Chữ đổ bóng (Nằm dưới)
        scoreShadowLabel = new Label(scoreP1 + " - " + scoreP2, skin, "title"); // Đổi thành "title"
        scoreShadowLabel.setAlignment(Align.center);
        scoreShadowLabel.setFontScale(1.0f); // Hạ scale xuống 1.0
        scoreShadowLabel.setColor(new Color(0.1f, 0.05f, 0f, 0.9f));

        // Lớp 2: Chữ hiển thị chính (Nằm trên)
        scoreLabel = new Label(scoreP1 + " - " + scoreP2, skin, "title"); // Đổi thành "title"
        scoreLabel.setAlignment(Align.center);
        scoreLabel.setFontScale(1.0f); // Hạ scale xuống 1.0
        scoreLabel.setColor(new Color(1f, 0.9f, 0.1f, 1f));

        // Dùng Stack để đè 2 chữ lên nhau
        Stack scoreStack = new Stack();

        Table shadowTable = new Table();
        shadowTable.add(scoreShadowLabel).padTop(6).padLeft(6); // Đẩy bóng lệch xuống dưới và sang phải 6px

        Table frontTable = new Table();
        frontTable.add(scoreLabel);

        scoreStack.add(shadowTable);
        scoreStack.add(frontTable);

        // -- Cấu hình Thời gian
        timerLabel = new Label(String.valueOf((int) roundTimeLeft), skin, "hudTimer");
        timerLabel.setAlignment(Align.center);
        timerLabel.setFontScale(1.1f);
        timerLabel.setColor(new Color(1f, 1f, 1f, 0.9f));

        // 3. Nút Pause
        Button.ButtonStyle pauseStyle = new Button.ButtonStyle();
        if (pauseIconTexture == null) {
            pauseIconTexture = createPauseIconTexture(48);
        }
        pauseStyle.up = new TextureRegionDrawable(new TextureRegion(pauseIconTexture));
        pauseStyle.over = pauseStyle.up;
        pauseStyle.down = pauseStyle.up;

        Button btnPause = new Button(pauseStyle);
        btnPause.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a) {
                togglePause();
            }
        });

        // 4. Bố cục lại Table chính

        Table topRow = new Table();
        topRow.add().width(56);
        // SỬA DÒNG NÀY: Xóa bỏ ".padTop(5f)" ở cuối để Tỷ số nhích lên cao nhất có thể
        topRow.add(scoreStack).expandX().center();
        topRow.add(btnPause).size(46, 46).right().padRight(10);

        // DÒNG 2: Thanh Máu P1 - Thời Gian - Thanh Máu P2
        Table leftHud = new Table();
        leftHud.add(hpBarP1).expandX().fillX().height(30f).row();
        leftHud.add(energyBarP1).expandX().fillX().height(4f).padTop(2f);

        Table rightHud = new Table();
        rightHud.add(hpBarP2).expandX().fillX().height(30f).row();
        rightHud.add(energyBarP2).expandX().fillX().height(4f).padTop(2f);

        Table battleHudRow = new Table();
        // Thêm padRight(50f) và padLeft(50f) để đẩy thanh máu cách xa đồng hồ đếm ngược
        battleHudRow.add(leftHud).expandX().fillX().padLeft(20f).padRight(50f);
        battleHudRow.add(timerLabel).width(120).center();
        battleHudRow.add(rightHud).expandX().fillX().padLeft(50f).padRight(20f);

        root.add(topRow).expandX().fillX().row();

        // SỬA DÒNG NÀY: Giảm .padTop(8f) xuống thành .padTop(2f) để Thanh máu ép sát
        // vào hàng Tỷ số
        root.add(battleHudRow).expandX().fillX().padTop(2f);

        stage.addActor(root);
    }

    private void updateHUD() {
        float smoothing = 0.18f;
        displayedHpP1 += (player1.getHpPercent() - displayedHpP1) * smoothing;
        displayedHpP2 += (player2.getHpPercent() - displayedHpP2) * smoothing;
        displayedEnergyP1 += (player1.getEnergyPercent() - displayedEnergyP1) * smoothing;
        displayedEnergyP2 += (player2.getEnergyPercent() - displayedEnergyP2) * smoothing;

        hpBarP1.setValue(displayedHpP1);
        hpBarP2.setValue(1f - displayedHpP2);

        energyBarP1.setValue(displayedEnergyP1);
        energyBarP2.setValue(1f - displayedEnergyP2);

        timerLabel.setText(String.valueOf((int) Math.ceil(roundTimeLeft)));
    }

    private void updateScoreLabel() {
        scoreLabel.setText(scoreP1 + " - " + scoreP2);
        if (scoreShadowLabel != null) {
            scoreShadowLabel.setText(scoreP1 + " - " + scoreP2);
        }
    }

    // —— Pause Overlay —————————————————————————————————————————————————————————

    private void buildPauseOverlay() {
        // 1. Dim background (Màng làm tối toàn màn hình)
        Pixmap dimPm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        dimPm.setColor(0f, 0f, 0f, 0.75f); // Tối 75%
        dimPm.fill();
        dimOverlayTexture = new Texture(dimPm);
        dimPm.dispose();

        Image dimBg = new Image(dimOverlayTexture);
        dimBg.setFillParent(true);
        dimBg.setName("dimBg");
        dimBg.setVisible(false);
        dimBg.getColor().a = 0f;

        // 2. Overlay chứa Panel gỗ
        pauseOverlay = new Table();
        pauseOverlay.setFillParent(true);
        pauseOverlay.center();
        pauseOverlay.setName("pausePanel");
        pauseOverlay.setVisible(false);
        pauseOverlay.setTransform(true);
        pauseOverlay.setScale(0.92f);
        pauseOverlay.getColor().a = 0f;

        // 3. SỬ DỤNG CÁC STYLE CÓ SẴN TỪ WOODENSKIN (Khác màu, có bo góc)
        // "primary": Thường là màu nổi bật (Xanh dương/Xanh lá)
        // Đổi "primary", "light" và "danger" thành các style chuẩn dưới đây:
        TextButton btnResume = new TextButton(I18n.get("TIẾP TỤC"), skin, "resume");
        TextButton btnRestart = new TextButton(I18n.get("CHƠI LẠI"), skin, "restart");
        TextButton btnQuit = new TextButton(I18n.get("THOÁT"), skin, "quit");
        // Scale nhẹ chữ lên 10% để cân đối với kích thước to của nút
        btnResume.getLabel().setFontScale(1.1f);
        btnRestart.getLabel().setFontScale(1.1f);
        btnQuit.getLabel().setFontScale(1.1f);

        // Map sự kiện cho các nút
        btnResume.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a) {
                togglePause();
            }
        });
        btnRestart.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new PlayScreen(game, twoPlayerMode, mapId));
            }
        });
        btnQuit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        // 4. Lắp ráp bảng gỗ trung tâm
        Table woodPanel = new Table();

        // Load trực tiếp panel_wood.png
        if (Gdx.files.internal("panel_wood.png").exists()) {
            if (pausePanelTexture != null)
                pausePanelTexture.dispose();
            pausePanelTexture = new Texture(Gdx.files.internal("panel_wood.png"));
            woodPanel.setBackground(new TextureRegionDrawable(new TextureRegion(pausePanelTexture)));
        } else {
            woodPanel.setBackground(loadPausePanelBackground()); // Fallback
        }

        woodPanel.pad(40, 50, 40, 50);
        woodPanel.defaults().width(320).height(65).padBottom(20f);

        // Chữ Tiêu đề TẠM DỪNG (Dùng style "title" từ skin)
        Label titleLabel = new Label(I18n.get("TẠM DỪNG"), skin, "title");
        titleLabel.setColor(new Color(1.00f, 0.88f, 0.40f, 1f));
        titleLabel.setAlignment(Align.center);

        woodPanel.add(titleLabel).padBottom(30).row();
        woodPanel.add(btnResume).row();
        woodPanel.add(btnRestart).row();
        woodPanel.add(btnQuit).padBottom(0f);

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
                    Actions.scaleTo(1f, 1f, 0.24f)));
        } else {
            if (dim != null) {
                dim.addAction(Actions.sequence(
                        Actions.fadeOut(0.2f),
                        Actions.visible(false)));
            }
            pauseOverlay.addAction(Actions.sequence(
                    Actions.parallel(
                            Actions.fadeOut(0.2f),
                            Actions.scaleTo(0.92f, 0.92f, 0.2f)),
                    Actions.visible(false)));
        }

        SoundManager sm = SoundManager.getInstance();
        if (paused)
            sm.pauseMusic();
        else
            sm.resumeMusic();
    }

    // —— Spawn —————————————————————————————————————————————————————————————————————

    private void spawnFighters() {
        float p1X = Constants.SCREEN_WIDTH * 0.25f;
        float p2X = Constants.SCREEN_WIDTH * 0.65f;
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
        energyProjectiles.clear();
    }

    // —— Texture Helpers ———————————————————————————————————————————————————————
    // Hàm tự động tạo một khối màu đơn sắc co giãn tự do, KHÔNG BAO GIỜ TRÀN VIỀN
    // Thêm tham số height vào để ép LibGDX phải vẽ chiều cao

    // Xóa hàm createSolidColor cũ và sửa lại makeHpBarStyle
    private ProgressBar.ProgressBarStyle makeHpBarStyle(Color barColor, boolean isP1) {
        // Khởi tạo và LƯU LẠI Texture để sau này dispose
        if (hpBgTexture == null) {
            Pixmap bgPm = new Pixmap(1, 24, Pixmap.Format.RGBA8888);
            bgPm.setColor(new Color(0.18f, 0.18f, 0.18f, 0.9f));
            bgPm.fill();
            hpBgTexture = new Texture(bgPm);
            bgPm.dispose();
        }

        Pixmap fillPm = new Pixmap(1, 24, Pixmap.Format.RGBA8888);
        fillPm.setColor(barColor);
        fillPm.fill();
        Texture fillTex = new Texture(fillPm);
        fillPm.dispose();

        // Lưu đúng texture của P1 hoặc P2
        if (isP1)
            hpBarP1Texture = fillTex;
        else
            hpBarP2Texture = fillTex;

        // Tạo cục knob tàng hình (chỉ cần tạo 1 lần)
        if (emptyKnobTexture == null) {
            Pixmap knobPm = new Pixmap(1, 24, Pixmap.Format.RGBA8888);
            knobPm.setColor(Color.CLEAR);
            knobPm.fill();
            emptyKnobTexture = new Texture(knobPm);
            knobPm.dispose();
        }

        TextureRegionDrawable bgDrawable = new TextureRegionDrawable(new TextureRegion(hpBgTexture));
        bgDrawable.setMinHeight(24f);
        bgDrawable.setMinWidth(0f);

        TextureRegionDrawable fillDrawable = new TextureRegionDrawable(new TextureRegion(fillTex));
        fillDrawable.setMinHeight(24f);
        fillDrawable.setMinWidth(0f);

        TextureRegionDrawable emptyKnob = new TextureRegionDrawable(new TextureRegion(emptyKnobTexture));
        emptyKnob.setMinHeight(24f);
        emptyKnob.setMinWidth(0f);

        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        style.background = bgDrawable;
        style.knob = emptyKnob;

        if (isP1) {
            style.knobBefore = fillDrawable;
        } else {
            style.knobAfter = fillDrawable;
        }
        return style;
    }

    private ProgressBar.ProgressBarStyle makeEnergyBarStyle(Color barColor, boolean isP1) {
        int height = 8;
        if (energyBgTexture == null) {
            Pixmap bgPm = new Pixmap(1, height, Pixmap.Format.RGBA8888);
            bgPm.setColor(new Color(0.18f, 0.18f, 0.18f, 0.9f));
            bgPm.fill();
            energyBgTexture = new Texture(bgPm);
            bgPm.dispose();
        }

        Pixmap fillPm = new Pixmap(1, height, Pixmap.Format.RGBA8888);
        fillPm.setColor(barColor);
        fillPm.fill();
        Texture fillTex = new Texture(fillPm);
        fillPm.dispose();

        if (isP1)
            energyBarP1Texture = fillTex;
        else
            energyBarP2Texture = fillTex;

        if (emptyKnobTexture == null) {
            Pixmap knobPm = new Pixmap(1, height, Pixmap.Format.RGBA8888);
            knobPm.setColor(Color.CLEAR);
            knobPm.fill();
            emptyKnobTexture = new Texture(knobPm);
            knobPm.dispose();
        }

        TextureRegionDrawable bgDrawable = new TextureRegionDrawable(new TextureRegion(energyBgTexture));
        bgDrawable.setMinHeight((float) height);
        bgDrawable.setMinWidth(0f);

        TextureRegionDrawable fillDrawable = new TextureRegionDrawable(new TextureRegion(fillTex));
        fillDrawable.setMinHeight((float) height);
        fillDrawable.setMinWidth(0f);

        TextureRegionDrawable emptyKnob = new TextureRegionDrawable(new TextureRegion(emptyKnobTexture));
        emptyKnob.setMinHeight((float) height);
        emptyKnob.setMinWidth(0f);

        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        style.background = bgDrawable;
        style.knob = emptyKnob;

        if (isP1) {
            style.knobBefore = fillDrawable;
        } else {
            style.knobAfter = fillDrawable;
        }
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

    private Texture createPauseIconTexture(int size) {
        // Supersample để icon mượt hơn, giảm cảm giác răng cưa/pixel.
        int sample = 4;
        int hiSize = size * sample;
        Pixmap hi = new Pixmap(hiSize, hiSize, Pixmap.Format.RGBA8888);
        hi.setColor(0f, 0f, 0f, 0f);
        hi.fill();

        int x = sample;
        int y = sample;
        int w = hiSize - sample * 2;
        int h = hiSize - sample * 2;
        int radius = Math.max(sample * 4, hiSize / 8);

        drawRoundedRectFill(hi, x, y, w, h, radius, new Color(0.96f, 0.93f, 0.84f, 1f));
        drawRoundedRectBorder(hi, x, y, w, h, radius, new Color(0.16f, 0.10f, 0.04f, 1f));
        drawRoundedRectBorder(hi, x + sample, y + sample, w - sample * 2, h - sample * 2,
                Math.max(sample * 2, radius - sample), new Color(0.16f, 0.10f, 0.04f, 1f));

        int barWidth = Math.max(sample * 4, hiSize / 9);
        int barHeight = Math.max(sample * 16, (int) (hiSize * 0.56f));
        int gap = Math.max(sample * 5, hiSize / 7);
        int total = barWidth * 2 + gap;
        int x0 = (hiSize - total) / 2;
        int y0 = (hiSize - barHeight) / 2;

        hi.setColor(0.10f, 0.08f, 0.06f, 1f);
        hi.fillRectangle(x0, y0, barWidth, barHeight);
        hi.fillRectangle(x0 + barWidth + gap, y0, barWidth, barHeight);

        Pixmap pm = downsamplePixmap(hi, sample);
        hi.dispose();

        Texture t = new Texture(pm);
        pm.dispose();
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }

    private Pixmap downsamplePixmap(Pixmap src, int factor) {
        int outW = src.getWidth() / factor;
        int outH = src.getHeight() / factor;
        Pixmap out = new Pixmap(outW, outH, Pixmap.Format.RGBA8888);

        for (int y = 0; y < outH; y++) {
            for (int x = 0; x < outW; x++) {
                int sumR = 0;
                int sumG = 0;
                int sumB = 0;
                int sumA = 0;
                for (int sy = 0; sy < factor; sy++) {
                    for (int sx = 0; sx < factor; sx++) {
                        int px = src.getPixel(x * factor + sx, y * factor + sy);
                        sumR += (px >>> 24) & 0xFF;
                        sumG += (px >>> 16) & 0xFF;
                        sumB += (px >>> 8) & 0xFF;
                        sumA += px & 0xFF;
                    }
                }
                int samples = factor * factor;
                int r = sumR / samples;
                int g = sumG / samples;
                int b = sumB / samples;
                int a = sumA / samples;
                out.drawPixel(x, y, (r << 24) | (g << 16) | (b << 8) | a);
            }
        }
        return out;
    }

    private void drawRoundedRectFill(Pixmap pm, int x, int y, int w, int h, int radius, Color color) {
        pm.setColor(color);
        for (int py = y; py < y + h; py++) {
            for (int px = x; px < x + w; px++) {
                if (isInsideRoundedRect(px, py, x, y, w, h, radius)) {
                    pm.drawPixel(px, py);
                }
            }
        }
    }

    private void drawRoundedRectBorder(Pixmap pm, int x, int y, int w, int h, int radius, Color color) {
        pm.setColor(color);
        for (int py = y + 1; py < y + h - 1; py++) {
            for (int px = x + 1; px < x + w - 1; px++) {
                if (!isInsideRoundedRect(px, py, x, y, w, h, radius)) {
                    continue;
                }
                boolean border = !isInsideRoundedRect(px - 1, py, x, y, w, h, radius)
                        || !isInsideRoundedRect(px + 1, py, x, y, w, h, radius)
                        || !isInsideRoundedRect(px, py - 1, x, y, w, h, radius)
                        || !isInsideRoundedRect(px, py + 1, x, y, w, h, radius);
                if (border) {
                    pm.drawPixel(px, py);
                }
            }
        }
    }

    private boolean isInsideRoundedRect(int px, int py, int x, int y, int w, int h, int radius) {
        int r = Math.max(1, Math.min(radius, Math.min(w, h) / 2));
        int lx = px - x;
        int ly = py - y;

        if (lx >= r && lx < w - r) {
            return ly >= 0 && ly < h;
        }
        if (ly >= r && ly < h - r) {
            return lx >= 0 && lx < w;
        }

        int cx = lx < r ? r : (w - r - 1);
        int cy = ly < r ? r : (h - r - 1);
        int dx = lx - cx;
        int dy = ly - cy;
        return (dx * dx + dy * dy) <= (r * r);
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
        pm.fillRectangle(0, 0, w, (int) (Constants.GROUND_Y + 10));

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

        if (x < 0 || x >= w || y < 0 || y >= h)
            return;
        int idx = y * w + x;
        if (visited[idx])
            return;

        int pixel = pm.getPixel(x, y);
        if (!isCheckerLikePixel(pixel))
            return;

        visited[idx] = true;
        queue.addLast(idx);
    }

    private boolean isCheckerLikePixel(int pixel) {
        int a = pixel & 0xFF;
        if (a < 220)
            return false;

        int r = (pixel >>> 24) & 0xFF;
        int g = (pixel >>> 16) & 0xFF;
        int b = (pixel >>> 8) & 0xFF;
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        int avg = (r + g + b) / 3;

        return avg >= 170 && avg <= 245 && (max - min) <= 18;
    }

    // MỚI: Hàm tạo hình ảnh nền cho Tỷ số (Badge) với viền kim loại và nền mờ
    private Texture createScoreBadgeTexture() {
        // Dùng kỹ thuật Supersampling x3 để render hình ảnh to, sau đó thu nhỏ lại giúp
        // nét vẽ mượt mà, không bị răng cưa
        int sample = 3;
        int w = 160 * sample;
        int h = 54 * sample;
        int radius = 18 * sample;

        Pixmap hi = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        hi.setColor(0f, 0f, 0f, 0f);
        hi.fill();

        // Vẽ nền tối mờ (đen ánh nâu 85%)
        drawRoundedRectFill(hi, 0, 0, w, h, radius, new Color(0.12f, 0.08f, 0.05f, 0.85f));

        // Vẽ viền ngoài cùng màu vàng kim rực rỡ
        drawRoundedRectBorder(hi, 0, 0, w, h, radius, new Color(1f, 0.85f, 0.2f, 1f));
        // Vẽ viền trong màu đồng để tạo độ sâu 3D
        drawRoundedRectBorder(hi, sample, sample, w - sample * 2, h - sample * 2, Math.max(1, radius - sample),
                new Color(0.8f, 0.5f, 0.1f, 1f));
        // Vẽ viền trong cùng tối màu để ép làm nổi nền chữ
        drawRoundedRectBorder(hi, sample * 2, sample * 2, w - sample * 4, h - sample * 4,
                Math.max(1, radius - sample * 2), new Color(0.3f, 0.15f, 0.05f, 1f));

        Pixmap pm = downsamplePixmap(hi, sample);
        hi.dispose();

        Texture t = new Texture(pm);
        pm.dispose();
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }
}
