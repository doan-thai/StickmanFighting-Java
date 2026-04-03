package com.stickman.fighting.screens;

import com.stickman.fighting.utils.SoundManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

/**
 * MainMenuScreen – Màn hình chính của game.
 *
 * Layout (Scene2D / Table):
 * ┌───────────────────────────────────────┐
 * │  [Settings ⚙]                   (top-right via Stack)
 * │                                       │
 * │         [ BẮT ĐẦU ]  (to nhất)       │
 * │                                       │
 * │  [1 PLAYER]  [2 PLAYER]  [HƯỚNG DẪN] │
 * └───────────────────────────────────────┘
 */
public class MainMenuScreen implements Screen {

    private enum GameMode {
        ONE_PLAYER,
        TWO_PLAYER
    }

    private final MyFightingGame game;
    private Stage stage;
    private Skin  skin;
    private TextButton btn1P;
    private TextButton btn2P;
    private GameMode selectedMode = GameMode.ONE_PLAYER;
    private TextButton.TextButtonStyle modeDefaultStyle;
    private TextButton.TextButtonStyle modeSelectedStyle;

    // Texture tạm (procedural background màu nâu gỗ)
    private Texture bgTexture;
    private Texture settingsIconTexture;

    public MainMenuScreen(MyFightingGame game) {
        this.game = game;
    }

    // ── Vòng đời Screen ───────────────────────────────────────────────────────

    @Override
    public void show() {
        // Viewport giữ tỉ lệ 16:9 đúng theo thiết kế
        stage = new Stage(new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        skin = WoodenSkin.create();
        bgTexture = createBackgroundTexture();

        buildUI();
        SoundManager.getInstance().playMusic(SoundManager.MusicTrack.MENU);
    }

    @Override
    public void render(float delta) {
        // Clear màn hình
        Gdx.gl.glClearColor(0.15f, 0.08f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause()   {}
    @Override public void resume()  {}
    @Override public void hide()    {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (settingsIconTexture != null) settingsIconTexture.dispose();
    }

    // ── Build UI ──────────────────────────────────────────────────────────────

    private void buildUI() {
        // --- Background image phủ toàn màn hình ---
        Image background = new Image(bgTexture);
        background.setFillParent(true);
        stage.addActor(background);

        // ── Root Stack: cho phép chồng layer lên nhau ─────────────────────────
        // Layer 1: Nội dung chính (Table căn giữa)
        // Layer 2: Nút Settings góc trên phải
        Stack rootStack = new Stack();
        rootStack.setFillParent(true);
        stage.addActor(rootStack);

        // --- Layer 1: Bảng nội dung chính ---
        rootStack.add(buildCenterContent());

        // --- Layer 2: Nút Settings góc phải ---
        rootStack.add(buildSettingsOverlay());
    }

    /**
     * Bảng nội dung căn giữa màn hình:
     *   Title label + nút BẮT ĐẦU + hàng nút nhỏ bên dưới
     */
    private Table buildCenterContent() {
        Table table = new Table();
        table.setFillParent(true);
        table.center().padTop(8f);

        // --- Title ---
        BitmapFont titleFont = skin.get("menu-title-font", BitmapFont.class);
        Actor titleLabel = new ShadowTitleActor(titleFont, "STICKMAN FIGHTING");
        titleLabel.setColor(1f, 1f, 1f, 0f);

        table.add(titleLabel).width(860).height(120).padBottom(56).row();

        // --- Nút BẮT ĐẦU (to nhất, ở giữa) ---
        TextButton btnStart = new TextButton("BẮT ĐẦU", skin, "primary");
        btnStart.getColor().a = 0f;
        btnStart.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance()
                    .playSound(SoundManager.SoundEffect.BUTTON_CLICK);
                onStartClicked();
            }
        });
        // Nút này to hơn các nút khác
        table.add(btnStart).width(320).height(75).padTop(10).padBottom(34).row();

        // --- Hàng 3 nút nhỏ bên dưới ---
        Table bottomRow = new Table();
        bottomRow.getColor().a = 0f;

        btn1P = new TextButton("1 PLAYER", skin, "light");
        btn2P = new TextButton("2 PLAYER", skin, "light");
        TextButton btnGuide = new TextButton("HƯỚNG DẪN", skin, "light");

        modeDefaultStyle = new TextButton.TextButtonStyle(
            skin.get("light", TextButton.TextButtonStyle.class));
        modeSelectedStyle = new TextButton.TextButtonStyle(
            skin.get("success", TextButton.TextButtonStyle.class));

        btn1P.setTransform(true);
        btn2P.setTransform(true);
        btn1P.setOrigin(Align.center);
        btn2P.setOrigin(Align.center);

        btn1P.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) { on1PlayerClicked(); }
        });
        btn2P.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) { on2PlayerClicked(); }
        });
        btnGuide.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) { onGuideClicked(); }
        });

        updateModeButtons();

        bottomRow.add(btn1P).width(200).height(55).padRight(16);
        bottomRow.add(btn2P).width(200).height(55).padRight(16);
        bottomRow.add(btnGuide).width(220).height(55);

        table.add(bottomRow).padTop(8f);

        titleLabel.addAction(Actions.fadeIn(0.55f));
        btnStart.addAction(Actions.sequence(Actions.delay(0.18f), Actions.fadeIn(0.38f)));
        bottomRow.addAction(Actions.sequence(Actions.delay(0.26f), Actions.fadeIn(0.4f)));

        return table;
    }

    /**
     * Overlay chứa nút Settings ở góc trên phải.
     * Dùng Table align TopRight để không ảnh hưởng layout chính.
     */
    private Table buildSettingsOverlay() {
        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.top().right().pad(16);

        Actor btnSettings = buildSettingsButton();
        overlay.add(btnSettings).width(72).height(56);

        return overlay;
    }

    // ── Callbacks điều hướng ──────────────────────────────────────────────────

    /** Nút BẮT ĐẦU → vào chế độ 1 Player mặc định */
    private void onStartClicked() {
        boolean isMirror = selectedMode == GameMode.TWO_PLAYER;
        game.setScreen(new PlayScreen(game, isMirror));
    }

    private void on1PlayerClicked() {
        selectedMode = GameMode.ONE_PLAYER;
        SoundManager.getInstance().playSound(SoundManager.SoundEffect.BUTTON_CLICK);
        updateModeButtons();
        animateModeSelection(btn1P);
    }

    private void on2PlayerClicked() {
        selectedMode = GameMode.TWO_PLAYER;
        SoundManager.getInstance().playSound(SoundManager.SoundEffect.BUTTON_CLICK);
        updateModeButtons();
        animateModeSelection(btn2P);
    }

    private void onGuideClicked() {
        game.setScreen(new GuideScreen(game));
    }

    private void onSettingsClicked() {
        // Truyền "this" để SettingScreen biết màn hình nào cần quay về
        game.setScreen(new SettingScreen(game, this));
    }

   private void updateModeButtons() {
    boolean onePlayer = selectedMode == GameMode.ONE_PLAYER;
    btn1P.setStyle(onePlayer ? modeSelectedStyle : modeDefaultStyle);  // ✓ đúng
    btn2P.setStyle(onePlayer ? modeDefaultStyle : modeSelectedStyle);  // ✓ đúng

    // Màu chữ khi được chọn sáng hơn
    btn1P.getLabel().setColor(onePlayer
        ? new Color(0.98f, 0.95f, 0.84f, 1f)   // sáng = đang chọn
        : new Color(0.95f, 0.86f, 0.67f, 1f));  // tối hơn = không chọn
    btn2P.getLabel().setColor(onePlayer
        ? new Color(0.95f, 0.86f, 0.67f, 1f)
        : new Color(0.98f, 0.95f, 0.84f, 1f));
}

    private void animateModeSelection(TextButton button) {
        if (button == null) return;
        button.clearActions();
        button.addAction(Actions.sequence(
            Actions.parallel(
                Actions.scaleTo(1.08f, 1.08f, 0.09f),
                Actions.color(new Color(1f, 1f, 0.92f, 1f), 0.09f)
            ),
            Actions.parallel(
                Actions.scaleTo(1f, 1f, 0.12f),
                Actions.color(Color.WHITE, 0.12f)
            )
        ));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static final class ShadowTitleActor extends Actor {
        private final BitmapFont font;
        private final String text;
        private final GlyphLayout layout;

        private ShadowTitleActor(BitmapFont font, String text) {
            this.font = font;
            this.text = text;
            this.layout = new GlyphLayout(font, text);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            float alpha = getColor().a * parentAlpha;
            float x = getX() + (getWidth() - layout.width) * 0.5f;
            float y = getY() + (getHeight() + layout.height) * 0.5f;

            // Vẽ shadow (offset đen) trước
            font.setColor(0.1f, 0.05f, 0f, 0.85f * alpha);
            font.draw(batch, text, x + 4f, y - 4f);

            // Vẽ chữ vàng đè lên
            font.setColor(0.91f, 0.75f, 0.24f, alpha);
            font.draw(batch, text, x, y);
        }
    }

    /** Tạo background procedural màu gỗ nâu có vân đơn giản */
    private Texture createBackgroundTexture() {
        if (Gdx.files.internal("background_menu.jpg").exists()) {
            return new Texture(Gdx.files.internal("background_menu.jpg"));
        }
        if (Gdx.files.internal("background.jpg").exists()) {
            return new Texture(Gdx.files.internal("background.jpg"));
        }

        int w = Constants.SCREEN_WIDTH;
        int h = Constants.SCREEN_HEIGHT;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        // Nền nâu gỗ trung
        pm.setColor(0.28f, 0.15f, 0.05f, 1f);
        pm.fill();

        // Vẽ các sọc ngang mô phỏng vân gỗ
        for (int y = 0; y < h; y += 28) {
            float alpha = (y % 56 == 0) ? 0.12f : 0.06f;
            pm.setColor(0f, 0f, 0f, alpha);
            pm.drawLine(0, y, w, y);
        }

        // Viền tối xung quanh (vignette đơn giản)
        pm.setColor(0f, 0f, 0f, 0.35f);
        for (int i = 0; i < 40; i++) {
            pm.drawRectangle(i, i, w - i * 2, h - i * 2);
        }

        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private Actor buildSettingsButton() {
        String iconFile = Gdx.files.internal("icon_setting.png").exists()
            ? "icon_setting.png"
            : "icon_settings.png";

        if (Gdx.files.internal(iconFile).exists()) {
            settingsIconTexture = new Texture(Gdx.files.internal(iconFile));
            ImageButton.ImageButtonStyle st = new ImageButton.ImageButtonStyle();
            st.imageUp = new TextureRegionDrawable(new TextureRegion(settingsIconTexture));
            st.imageOver = st.imageUp;
            st.imageDown = st.imageUp;
            ImageButton b = new ImageButton(st);
            b.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent e, Actor a) { onSettingsClicked(); }
            });
            return b;
        }

        TextButton btnSettings = new TextButton("⚙", skin, "icon");
        btnSettings.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) { onSettingsClicked(); }
        });
        return btnSettings;
    }
}
