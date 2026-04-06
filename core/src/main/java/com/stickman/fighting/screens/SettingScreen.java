package com.stickman.fighting.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.stickman.fighting.MyFightingGame;
import com.stickman.fighting.utils.Constants;
import com.stickman.fighting.utils.GameSettings;
import com.stickman.fighting.utils.SoundManager;
import com.stickman.fighting.utils.UiQaConfig;

/**
 * SettingScreen — Hiển thị như một Overlay/Popup bảng gỗ đè lên màn hình trước.
 *
 * Layout bảng gỗ (căn giữa màn hình):
 * ┌──────────────────────────────────────┐
 * │ CÀI ĐẶT [ĐÓNG] │
 * ├──────────────────────────────────────┤
 * │ 🔊 Âm thanh ══════●═══ 80% │
 * │ ⏱ Thời gian [60 giây ▼] │
 * │ 🌐 Ngôn ngữ [Tiếng Việt ▼] │
 * │ ❤ Thanh máu ══════════● 100% │
 * └──────────────────────────────────────┘
 *
 * Các fix so với version cũ:
 * - Slider fill đổi từ xanh dương → vàng đồng (#C8963C) cho đồng bộ tông gỗ
 * - % thanh máu hiển thị trực tiếp theo hệ số: 1.0→100%, 3.0→300%
 * - Nút ĐÓNG có bo góc rounded nhất quán
 * - Các Texture tạo trong buildSliderStyle/buildSelectBoxStyle được track để
 * dispose
 * - SelectBox có arrow indicator rõ hơn
 */
public class SettingScreen implements Screen {

    private static final float TIME_SELECT_BOX_WIDTH = 228f;
    private static final float LANG_SELECT_BOX_WIDTH = 228f;
    private static final float SELECT_BOX_HEIGHT = 50f;
    private static final float PANEL_WIDTH = 616f; // 560 * 1.10
    private static final int PANEL_TEXTURE_HEIGHT = 462; // 420 * 1.10
    private static final float CONTENT_ROW_WIDTH = PANEL_WIDTH * 0.92f; // shrink inner bars by 8%
    private static final float LABEL_COL_WIDTH = 210f;
    private static final float CONTROL_COL_WIDTH = 238f;
    private static final float VALUE_COL_WIDTH = 68f;

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final MyFightingGame game;
    private final Screen previousScreen;
    private final GameSettings settings;

    // ── Scene2D ───────────────────────────────────────────────────────────────
    private Stage stage;
    private Skin skin;

    // ── Widgets ───────────────────────────────────────────────────────────────
    private Slider sliderVolume;
    private Slider sliderHp;
    private SelectBox<String> selectTime;
    private SelectBox<String> selectLang;
    private Label labelVolumeVal;
    private Label labelHpVal;
    private Table panelTable;
    private TextButton btnClose;
    private boolean closeRequested;
    private boolean qaLayoutChecked;
    private boolean qaScreenshotTaken;
    private int qaFrameCounter;

    // ── Textures (tracked để dispose) ────────────────────────────────────────
    private Texture dimTexture;
    private Texture panelTexture;
    private Texture panelHeaderTexture;
    private Texture rowTexture;
    private Texture rowAltTexture;
    private Texture speakerOnTexture;
    private Texture speakerOffTexture;
    private Texture dividerTexture;

    // Slider textures — tracked riêng để dispose
    private Texture sliderTrackTex;
    private Texture sliderFillTex;
    private Texture sliderKnobTex;
    private Texture sliderKnobOverTex;

    // SelectBox textures — tracked riêng
    private final Array<Texture> selectTextures = new Array<>();

    // Volume toggle
    private ImageButton btnVolumeToggle;
    private ImageButton.ImageButtonStyle volumeToggleStyle;
    private float previousVolumeBeforeMute = 0.8f;

    // ── Constructor ───────────────────────────────────────────────────────────
    public SettingScreen(MyFightingGame game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
        this.settings = GameSettings.getInstance();
    }

    // ── Vòng đời Screen ───────────────────────────────────────────────────────

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        createTextures();
        skin = buildSettingSkin();
        buildUI();
        closeRequested = false;
        qaLayoutChecked = false;
        qaScreenshotTaken = false;
        qaFrameCounter = 0;
    }

    @Override
    public void render(float delta) {
        if (closeRequested) {
            finalizeClose();
            return;
        }

        // Render màn hình phía sau trước
        if (previousScreen != null) {
            previousScreen.render(delta);
        } else {
            Gdx.gl.glClearColor(0.15f, 0.08f, 0.02f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }

        // ESC → đóng setting
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            requestClose();
            return;
        }

        stage.act(delta);
        stage.draw();

        if (UiQaConfig.isEnabled()) {
            runQaAutomation();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F9)) {
            captureScreenshot("manual");
        }
    }

    @Override
    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        disposeTextures();
    }

    // ── Build UI ──────────────────────────────────────────────────────────────

    private void buildUI() {
        // Layer 1: Dim overlay
        Image dimBg = new Image(dimTexture);
        dimBg.setFillParent(true);
        stage.addActor(dimBg);

        // Layer 2: Panel căn giữa
        Table root = new Table();
        root.setFillParent(true);
        root.center();
        root.add(buildPanel()).width(PANEL_WIDTH).pad(20);
        stage.addActor(root);
    }

    /**
     * Bảng gỗ chính:
     * headerRow → Title + nút ĐÓNG
     * divider
     * row × 4 → [Label] [Widget] [Giá trị]
     */
    private Table buildPanel() {
        panelTable = new Table();
        panelTable.setBackground(new TextureRegionDrawable(new TextureRegion(panelTexture)));
        panelTable.setClip(true);

        panelTable.add(buildHeader()).width(CONTENT_ROW_WIDTH).center().row();
        panelTable.add(buildDivider()).width(CONTENT_ROW_WIDTH).center().height(3).padBottom(6).row();
        panelTable.add(buildVolumeRow()).width(CONTENT_ROW_WIDTH).center().padBottom(4).row();
        panelTable.add(buildTimeRow()).width(CONTENT_ROW_WIDTH).center().padBottom(4).row();
        panelTable.add(buildLanguageRow()).width(CONTENT_ROW_WIDTH).center().padBottom(4).row();
        panelTable.add(buildHpRow()).width(CONTENT_ROW_WIDTH).center().padBottom(8).row();

        return panelTable;
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private Table buildHeader() {
        Table header = new Table();
        header.setBackground(new TextureRegionDrawable(new TextureRegion(panelHeaderTexture)));
        header.pad(14, 20, 14, 16);

        Label title = new Label("CÀI ĐẶT", skin, "header");
        title.setAlignment(Align.left);

        // Nút đóng theo tông gỗ/cổ điển để đồng bộ toàn bộ panel.
        btnClose = new TextButton("X", skin, "close");
        btnClose.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().playSound(SoundManager.SoundEffect.BUTTON_CLICK);
                requestClose();
            }
        });

        header.add(title).expandX().left();
        header.add(btnClose).right().width(108).height(44);
        return header;
    }

    private void runQaAutomation() {
        qaFrameCounter++;

        if (!qaLayoutChecked && UiQaConfig.runLayoutChecks() && qaFrameCounter >= 2) {
            qaLayoutChecked = true;
            validateLayoutAndReport();
        }

        int screenshotDelay = Math.max(2, UiQaConfig.screenshotDelayFrames());
        if (!qaScreenshotTaken && UiQaConfig.captureScreenshot() && qaFrameCounter >= screenshotDelay) {
            qaScreenshotTaken = true;
            captureScreenshot("auto");
            if (UiQaConfig.exitAfterScreenshot()) {
                Gdx.app.log("UI-QA", "Exit after screenshot is enabled. Closing app.");
                Gdx.app.exit();
            }
        }
    }

    private void validateLayoutAndReport() {
        if (panelTable == null || selectTime == null || selectLang == null) {
            Gdx.app.error("UI-QA", "Layout check skipped because key widgets are not ready.");
            return;
        }

        Rectangle panelRect = getActorStageRect(panelTable);
        Rectangle timeRect = getActorStageRect(selectTime);
        Rectangle langRect = getActorStageRect(selectLang);
        Rectangle volumeValRect = getActorStageRect(labelVolumeVal);
        Rectangle hpValRect = getActorStageRect(labelHpVal);

        int warnings = 0;
        warnings += assertInside(panelRect, timeRect, "selectTime");
        warnings += assertInside(panelRect, langRect, "selectLang");

        float leftDiff = Math.abs(timeRect.x - langRect.x);
        if (leftDiff > 1.5f) {
            warnings++;
            Gdx.app.error("UI-QA", "Left edges are not aligned: delta=" + leftDiff);
        }

        float panelRight = panelRect.x + panelRect.width;
        float volumeGap = panelRight - (volumeValRect.x + volumeValRect.width);
        if (volumeGap < 8f) {
            warnings++;
            Gdx.app.error("UI-QA", "Volume value is too close to panel right edge: gap=" + volumeGap);
        }

        float hpGap = panelRight - (hpValRect.x + hpValRect.width);
        if (hpGap < 8f) {
            warnings++;
            Gdx.app.error("UI-QA", "HP value is too close to panel right edge: gap=" + hpGap);
        }

        if (btnClose != null && btnClose.getWidth() < 100f) {
            warnings++;
            Gdx.app.error("UI-QA", "Close button width is smaller than expected: " + btnClose.getWidth());
        }

        float timeBgWidth = selectTime.getStyle().background != null
                ? selectTime.getStyle().background.getMinWidth()
                : 0f;
        if (Math.abs(timeBgWidth - selectTime.getWidth()) > 1.5f) {
            warnings++;
            Gdx.app.error("UI-QA", "Time SelectBox background width mismatch: bg=" + timeBgWidth
                    + ", actor=" + selectTime.getWidth());
        }

        float langBgWidth = selectLang.getStyle().background != null
                ? selectLang.getStyle().background.getMinWidth()
                : 0f;
        if (Math.abs(langBgWidth - selectLang.getWidth()) > 1.5f) {
            warnings++;
            Gdx.app.error("UI-QA", "Language SelectBox background width mismatch: bg=" + langBgWidth
                    + ", actor=" + selectLang.getWidth());
        }

        if (warnings == 0) {
            Gdx.app.log("UI-QA", "SettingScreen layout checks passed with 0 warnings.");
        } else {
            Gdx.app.error("UI-QA", "SettingScreen layout checks finished with warnings=" + warnings);
        }
    }

    private Rectangle getActorStageRect(Actor actor) {
        Vector2 position = actor.localToStageCoordinates(new Vector2(0f, 0f));
        return new Rectangle(position.x, position.y, actor.getWidth(), actor.getHeight());
    }

    private int assertInside(Rectangle parent, Rectangle child, String name) {
        boolean inside = child.x >= parent.x
                && child.y >= parent.y
                && child.x + child.width <= parent.x + parent.width
                && child.y + child.height <= parent.y + parent.height;
        if (inside)
            return 0;

        Gdx.app.error("UI-QA", name + " is outside panel bounds."
                + " parent=" + parent + ", child=" + child);
        return 1;
    }

    private void captureScreenshot(String source) {
        Pixmap rawPixmap = Pixmap.createFromFrameBuffer(
                0,
                0,
                Gdx.graphics.getBackBufferWidth(),
                Gdx.graphics.getBackBufferHeight());
        Pixmap pixmap = flipVertically(rawPixmap);
        try {
            String fileName = "setting_" + System.currentTimeMillis() + "_" + source + ".png";
            FileHandle output = Gdx.files.local(UiQaConfig.outputDir()).child(fileName);
            output.parent().mkdirs();
            PixmapIO.writePNG(output, pixmap);
            Gdx.app.log("UI-QA", "Screenshot saved: " + output.path());
        } finally {
            rawPixmap.dispose();
            pixmap.dispose();
        }
    }

    private Pixmap flipVertically(Pixmap source) {
        Pixmap flipped = new Pixmap(source.getWidth(), source.getHeight(), source.getFormat());
        for (int y = 0; y < source.getHeight(); y++) {
            flipped.drawPixmap(
                    source,
                    0,
                    y,
                    0,
                    source.getHeight() - 1 - y,
                    source.getWidth(),
                    1);
        }
        return flipped;
    }

    // ── Row: Âm Thanh ────────────────────────────────────────────────────────

    private Table buildVolumeRow() {
        Table row = buildRowBase(true);

        Label label = new Label("Âm thanh", skin, "rowLabel");
        row.add(label).width(LABEL_COL_WIDTH).left();

        // Slider âm lượng (0 → 1)
        volumeToggleStyle = buildVolumeToggleStyle();
        btnVolumeToggle = new ImageButton(volumeToggleStyle);
        btnVolumeToggle.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                toggleMute();
            }
        });

        sliderVolume = new Slider(0f, 1f, 0.01f, false, skin, "setting");
        sliderVolume.setValue(settings.getVolume());
        if (sliderVolume.getValue() > 0.001f) {
            previousVolumeBeforeMute = sliderVolume.getValue();
        }

        labelVolumeVal = new Label(toPercent(settings.getVolume()), skin, "rowValue");
        labelVolumeVal.setAlignment(Align.right);

        sliderVolume.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float val = sliderVolume.getValue();
                labelVolumeVal.setText(toPercent(val));
                settings.setVolume(val);

                if (val > 0.001f) {
                    previousVolumeBeforeMute = val;
                }
                updateVolumeToggleIcon();
                SoundManager.getInstance().setVolume(val);

                if (val > 0.001f) {
                    SoundManager.getInstance().playSound(SoundManager.SoundEffect.BUTTON_CLICK);
                }
            }
        });

        updateVolumeToggleIcon();

        Table controlCell = new Table();
        controlCell.left();
        controlCell.add(btnVolumeToggle).size(30).padRight(8);
        controlCell.add(sliderVolume).width(190).height(24);

        row.add(controlCell).width(CONTROL_COL_WIDTH).left();
        row.add(labelVolumeVal).width(VALUE_COL_WIDTH).right().padRight(4);
        return row;
    }

    // ── Row: Thời Gian ────────────────────────────────────────────────────────

    private Table buildTimeRow() {
        Table row = buildRowBase(false);

        Label label = new Label("Thời gian", skin, "rowLabel");
        row.add(label).width(LABEL_COL_WIDTH).left();

        Array<String> timeOptions = new Array<>();
        timeOptions.add("60 giây");
        timeOptions.add("120 giây");
        timeOptions.add("180 giây");

        selectTime = new SelectBox<>(skin, "setting-time");
        selectTime.setItems(timeOptions);

        switch (settings.getRoundTime()) {
            case 120 -> selectTime.setSelected("120 giây");
            case 180 -> selectTime.setSelected("180 giây");
            default -> selectTime.setSelected("60 giây");
        }

        selectTime.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String sel = selectTime.getSelected();
                int time = switch (sel) {
                    case "120 giây" -> 120;
                    case "180 giây" -> 180;
                    default -> 60;
                };
                settings.setRoundTime(time);
            }
        });

        selectTime.setAlignment(Align.left);

        Table controlCell = new Table();
        controlCell.left();
        controlCell.add(selectTime).width(TIME_SELECT_BOX_WIDTH).height(SELECT_BOX_HEIGHT);

        row.add(controlCell).width(CONTROL_COL_WIDTH).left();
        row.add(new Label("", skin)).width(VALUE_COL_WIDTH).padRight(4);
        return row;
    }

    // ── Row: Ngôn Ngữ ─────────────────────────────────────────────────────────

    private Table buildLanguageRow() {
        Table row = buildRowBase(true);

        Label label = new Label("Ngôn ngữ", skin, "rowLabel");
        row.add(label).width(LABEL_COL_WIDTH).left();

        Array<String> langOptions = new Array<>();
        langOptions.add("Tiếng Việt");
        langOptions.add("English");

        selectLang = new SelectBox<>(skin, "setting-lang");
        selectLang.setItems(langOptions);

        if ("English".equals(settings.getLanguage())) {
            selectLang.setSelected("English");
        } else {
            selectLang.setSelected("Tiếng Việt");
        }

        selectLang.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String sel = selectLang.getSelected();
                settings.setLanguage("English".equals(sel) ? "English" : "Vietnamese");
                // TODO: Trigger i18n reload sau khi có hệ thống đa ngôn ngữ
            }
        });

        selectLang.setAlignment(Align.left);

        Table controlCell = new Table();
        controlCell.left();
        controlCell.add(selectLang).width(LANG_SELECT_BOX_WIDTH).height(SELECT_BOX_HEIGHT);

        row.add(controlCell).width(CONTROL_COL_WIDTH).left();
        row.add(new Label("", skin)).width(VALUE_COL_WIDTH).padRight(4);
        return row;
    }

    // ── Row: Thanh Máu ────────────────────────────────────────────────────────

    private Table buildHpRow() {
        Table row = buildRowBase(false);

        Label label = new Label("Thanh máu", skin, "rowLabel");
        row.add(label).width(LABEL_COL_WIDTH).left();

        // Slider HP scale (1.0 → 3.0, bước 0.1)
        sliderHp = new Slider(Constants.HP_SCALE_MIN, Constants.HP_SCALE_MAX, 0.1f, false, skin, "setting");
        sliderHp.setValue(settings.getHpScale());

        labelHpVal = new Label(toPercent(settings.getHpScale()), skin, "rowValue");
        labelHpVal.setAlignment(Align.right);

        sliderHp.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float val = sliderHp.getValue();
                labelHpVal.setText(toPercent(val));
                settings.setHpScale(val);
            }
        });

        Table controlCell = new Table();
        controlCell.left();
        controlCell.add(sliderHp).width(190).height(24);

        row.add(controlCell).width(CONTROL_COL_WIDTH).left();
        row.add(labelHpVal).width(VALUE_COL_WIDTH).right().padRight(4);
        return row;
    }

    // ── Helpers UI ────────────────────────────────────────────────────────────

    /**
     * Tạo một hàng cài đặt với nền xen kẽ sáng/tối (zebra stripe).
     */
    private Table buildRowBase(boolean light) {
        Table row = new Table();
        row.setBackground(new TextureRegionDrawable(
                new TextureRegion(light ? rowTexture : rowAltTexture)));
        row.pad(6, 16, 6, 16);
        return row;
    }

    private Image buildDivider() {
        if (dividerTexture != null)
            dividerTexture.dispose();
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(0.22f, 0.11f, 0.03f, 1f);
        pm.fill();
        dividerTexture = new Texture(pm);
        pm.dispose();
        return new Image(dividerTexture);
    }

    // ── Volume Toggle ─────────────────────────────────────────────────────────

    private ImageButton.ImageButtonStyle buildVolumeToggleStyle() {
        if (speakerOnTexture == null)
            speakerOnTexture = createSpeakerIconTexture(36, false);
        if (speakerOffTexture == null)
            speakerOffTexture = createSpeakerIconTexture(36, true);

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = new TextureRegionDrawable(new TextureRegion(speakerOnTexture));
        style.imageOver = style.imageUp;
        style.imageDown = style.imageUp;
        return style;
    }

    private void updateVolumeToggleIcon() {
        if (btnVolumeToggle == null || volumeToggleStyle == null)
            return;
        Texture icon = (sliderVolume != null && sliderVolume.getValue() > 0.001f)
                ? speakerOnTexture
                : speakerOffTexture;
        TextureRegionDrawable d = new TextureRegionDrawable(new TextureRegion(icon));
        volumeToggleStyle.imageUp = d;
        volumeToggleStyle.imageOver = d;
        volumeToggleStyle.imageDown = d;
        btnVolumeToggle.getImage().setDrawable(d);
    }

    private Texture createSpeakerIconTexture(int size, boolean muted) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();

        Color body = new Color(0.98f, 0.97f, 0.93f, 1f);
        Color outline = new Color(0.25f, 0.15f, 0.06f, 1f);
        Color wave = new Color(0.78f, 0.59f, 0.20f, 1f); // vàng đồng, đồng bộ với slider

        int midY = size / 2;
        int bodyW = 8;
        int bodyH = 12;

        pm.setColor(body);
        pm.fillRectangle(5, midY - bodyH / 2, bodyW, bodyH);
        pm.fillTriangle(13, midY - 8, 24, midY - 12, 24, midY + 12);

        pm.setColor(outline);
        pm.drawRectangle(5, midY - bodyH / 2, bodyW, bodyH);
        pm.drawLine(13, midY - 8, 24, midY - 12);
        pm.drawLine(13, midY + 8, 24, midY + 12);
        pm.drawLine(24, midY - 12, 24, midY + 12);

        if (!muted) {
            pm.setColor(wave);
            pm.drawLine(26, midY - 6, 30, midY - 2);
            pm.drawLine(30, midY - 2, 30, midY + 2);
            pm.drawLine(30, midY + 2, 26, midY + 6);
            pm.drawLine(29, midY - 10, 34, midY - 4);
            pm.drawLine(34, midY - 4, 34, midY + 4);
            pm.drawLine(34, midY + 4, 29, midY + 10);
        } else {
            pm.setColor(new Color(0.72f, 0.18f, 0.14f, 1f));
            pm.drawLine(28, midY - 10, 34, midY + 10);
            pm.drawLine(34, midY - 10, 28, midY + 10);
        }

        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private void toggleMute() {
        if (sliderVolume == null)
            return;
        float current = sliderVolume.getValue();
        if (current > 0.001f) {
            previousVolumeBeforeMute = current;
            sliderVolume.setValue(0f);
        } else {
            float restore = previousVolumeBeforeMute > 0.001f ? previousVolumeBeforeMute : 0.8f;
            sliderVolume.setValue(restore);
        }
    }

    // ── Save & Navigate ───────────────────────────────────────────────────────

    private void requestClose() {
        closeRequested = true;
    }

    private void finalizeClose() {
        settings.save();
        game.setScreen(previousScreen);
        dispose();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Chuyển 0.0–1.0 → "80%" */
    private String toPercent(float value) {
        int pct = Math.round(value * 100f);
        return pct + "%";
    }

    // ── Skin ──────────────────────────────────────────────────────────────────

    /**
     * Tạo Skin đầy đủ cho màn hình Setting.
     * Styles:
     * "header" – Label tiêu đề to, màu vàng kem
     * "rowLabel" – Label từng dòng cài đặt
     * "rowValue" – Label hiển thị giá trị số (%, giây…)
     * "close" – TextButton nút đóng (gỗ đậm, bo góc nhẹ)
     * "setting" – Slider & SelectBox style
     */
    private Skin buildSettingSkin() {
        Skin sk = new Skin();

        // ── Fonts ─────────────────────────────────────────────────────────────
        BitmapFont fontHeader = com.stickman.fighting.ui.WoodenSkin.createUIFont(40);
        BitmapFont fontRow = com.stickman.fighting.ui.WoodenSkin.createUIFont(24);
        BitmapFont fontValue = com.stickman.fighting.ui.WoodenSkin.createUIFont(24);
        BitmapFont fontDefault = com.stickman.fighting.ui.WoodenSkin.createUIFont(24);
        BitmapFont fontSelect = com.stickman.fighting.ui.WoodenSkin.createUIFont(21);
        BitmapFont fontClose = com.stickman.fighting.ui.WoodenSkin.createUIFont(28);

        sk.add("fontHeader", fontHeader, BitmapFont.class);
        sk.add("fontRow", fontRow, BitmapFont.class);
        sk.add("fontValue", fontValue, BitmapFont.class);
        sk.add("default-font", fontDefault, BitmapFont.class);
        sk.add("fontSelect", fontSelect, BitmapFont.class);
        sk.add("fontClose", fontClose, BitmapFont.class);

        // ── Màu chữ ───────────────────────────────────────────────────────────
        Color creamColor = new Color(0.98f, 0.95f, 0.86f, 1f);
        Color yellowColor = new Color(1.00f, 0.88f, 0.40f, 1f);
        Color grayColor = new Color(0.96f, 0.92f, 0.83f, 1f);

        // ── Label Styles ──────────────────────────────────────────────────────
        sk.add("header", new Label.LabelStyle(fontHeader, yellowColor), Label.LabelStyle.class);
        sk.add("rowLabel", new Label.LabelStyle(fontRow, creamColor), Label.LabelStyle.class);
        sk.add("rowValue", new Label.LabelStyle(fontValue, grayColor), Label.LabelStyle.class);
        sk.add("default", new Label.LabelStyle(fontDefault, creamColor), Label.LabelStyle.class);

        // ── Button Styles ─────────────────────────────────────────────────────
        Color border = new Color(0.22f, 0.10f, 0.02f, 1f);
        Color woodUp = new Color(0.55f, 0.30f, 0.09f, 1f);
        Color woodOver = new Color(0.68f, 0.42f, 0.14f, 1f);
        Color woodDown = new Color(0.35f, 0.17f, 0.04f, 1f);

        // "default" TextButton
        TextButton.TextButtonStyle defaultBtnStyle = new TextButton.TextButtonStyle();
        defaultBtnStyle.font = fontRow;
        defaultBtnStyle.fontColor = creamColor;
        defaultBtnStyle.up = drawableFlat(woodUp, border, 180, 48);
        defaultBtnStyle.over = drawableFlat(woodOver, border, 180, 48);
        defaultBtnStyle.down = drawableFlat(woodDown, border, 180, 48);
        sk.add("default", defaultBtnStyle, TextButton.TextButtonStyle.class);

        // Nút close chuyển sang tông gỗ để tránh lạc phong cách võ đường.
        Color closeUp = new Color(0.36f, 0.18f, 0.05f, 1f);
        Color closeOver = new Color(0.49f, 0.27f, 0.10f, 1f);
        Color closeDown = new Color(0.28f, 0.13f, 0.04f, 1f);
        TextButton.TextButtonStyle closeBtnStyle = new TextButton.TextButtonStyle();
        closeBtnStyle.font = fontClose;
        closeBtnStyle.fontColor = new Color(1.00f, 0.95f, 0.75f, 1f);
        closeBtnStyle.overFontColor = new Color(1.0f, 0.98f, 0.84f, 1f);
        closeBtnStyle.downFontColor = new Color(0.95f, 0.88f, 0.66f, 1f);
        closeBtnStyle.up = drawableRounded(closeUp, border, 108, 44, 10);
        closeBtnStyle.over = drawableRounded(closeOver, border, 108, 44, 10);
        closeBtnStyle.down = drawableRounded(closeDown, border, 108, 44, 10);
        sk.add("close", closeBtnStyle, TextButton.TextButtonStyle.class);

        // ── Slider Style "setting" ────────────────────────────────────────────
        Slider.SliderStyle sliderStyle = buildSliderStyle();
        sk.add("setting", sliderStyle, Slider.SliderStyle.class);
        sk.add("default-horizontal", sliderStyle, Slider.SliderStyle.class);

        // ── SelectBox Style "setting" ─────────────────────────────────────────
        SelectBox.SelectBoxStyle timeSelectStyle = buildSelectBoxStyle(fontSelect, creamColor,
                Math.round(TIME_SELECT_BOX_WIDTH));
        SelectBox.SelectBoxStyle langSelectStyle = buildSelectBoxStyle(fontSelect, creamColor,
                Math.round(LANG_SELECT_BOX_WIDTH));
        sk.add("setting-time", timeSelectStyle, SelectBox.SelectBoxStyle.class);
        sk.add("setting-lang", langSelectStyle, SelectBox.SelectBoxStyle.class);
        sk.add("default", langSelectStyle, SelectBox.SelectBoxStyle.class);

        return sk;
    }

    /**
     * FIX: Slider fill màu vàng đồng thay vì xanh dương cũ.
     * Track nền tối, knob tròn màu kem.
     */
    private Slider.SliderStyle buildSliderStyle() {
        // Track nền (nâu tối)
        Pixmap trackPm = new Pixmap(200, 8, Pixmap.Format.RGBA8888);
        trackPm.setColor(0.20f, 0.12f, 0.04f, 1f);
        trackPm.fill();
        trackPm.setColor(0.15f, 0.08f, 0.02f, 1f);
        trackPm.drawRectangle(0, 0, 200, 8);
        sliderTrackTex = new Texture(trackPm);
        trackPm.dispose();

        // FIX: Fill màu vàng đồng (#C8963C) — thay xanh dương cũ
        Pixmap fillPm = new Pixmap(200, 8, Pixmap.Format.RGBA8888);
        fillPm.setColor(0.78f, 0.59f, 0.20f, 1f); // vàng đồng
        fillPm.fill();
        sliderFillTex = new Texture(fillPm);
        fillPm.dispose();

        // Knob tròn màu kem
        int knobSize = 26;
        Pixmap knobPm = new Pixmap(knobSize, knobSize, Pixmap.Format.RGBA8888);
        knobPm.setColor(0f, 0f, 0f, 0f);
        knobPm.fill();
        knobPm.setColor(0.95f, 0.90f, 0.80f, 1f);
        knobPm.fillCircle(knobSize / 2, knobSize / 2, knobSize / 2 - 1);
        knobPm.setColor(0.22f, 0.10f, 0.02f, 1f);
        knobPm.drawCircle(knobSize / 2, knobSize / 2, knobSize / 2 - 1);
        sliderKnobTex = new Texture(knobPm);
        knobPm.dispose();

        // Knob hover (sáng hơn, viền vàng)
        Pixmap knobOverPm = new Pixmap(knobSize, knobSize, Pixmap.Format.RGBA8888);
        knobOverPm.setColor(0f, 0f, 0f, 0f);
        knobOverPm.fill();
        knobOverPm.setColor(1f, 1f, 0.85f, 1f);
        knobOverPm.fillCircle(knobSize / 2, knobSize / 2, knobSize / 2 - 1);
        knobOverPm.setColor(0.78f, 0.59f, 0.20f, 1f); // viền vàng đồng khi hover
        knobOverPm.drawCircle(knobSize / 2, knobSize / 2, knobSize / 2 - 1);
        sliderKnobOverTex = new Texture(knobOverPm);
        knobOverPm.dispose();

        Slider.SliderStyle style = new Slider.SliderStyle();
        style.background = new TextureRegionDrawable(new TextureRegion(sliderTrackTex));
        style.knobBefore = new TextureRegionDrawable(new TextureRegion(sliderFillTex));
        style.knob = new TextureRegionDrawable(new TextureRegion(sliderKnobTex));
        style.knobOver = new TextureRegionDrawable(new TextureRegion(sliderKnobOverTex));
        return style;
    }

    /** SelectBox dropdown style đồng bộ tông gỗ */
    private SelectBox.SelectBoxStyle buildSelectBoxStyle(
            BitmapFont font, Color fontColor, int boxWidth) {

        Color bgColor = new Color(0.50f, 0.28f, 0.08f, 1f);
        Color bgOverColor = new Color(0.62f, 0.38f, 0.14f, 1f);
        Color borderColor = new Color(0.22f, 0.10f, 0.02f, 1f);
        Color listBgColor = new Color(0.42f, 0.23f, 0.07f, 0.97f);
        Color selectColor = new Color(0.78f, 0.59f, 0.20f, 0.60f); // highlight vàng đồng

        // Tạo textures, track để dispose
        Texture bgTex = makeSelectBoxTex(bgColor, borderColor, boxWidth, Math.round(SELECT_BOX_HEIGHT),
                new Color(0.96f, 0.90f, 0.78f, 1f));
        Texture bgOverTex = makeSelectBoxTex(bgOverColor, borderColor, boxWidth, Math.round(SELECT_BOX_HEIGHT),
                new Color(1f, 0.95f, 0.82f, 1f));
        Texture listBgTex = makeFlatTex(listBgColor, borderColor, boxWidth, 46);
        Texture selectionTex = makeFlatTex(selectColor, borderColor, boxWidth, 46);
        Texture scrollBgTex = makeFlatTex(new Color(0.38f, 0.20f, 0.06f, 0.97f), borderColor, boxWidth, 120);

        trackSelectTexture(bgTex);
        trackSelectTexture(bgOverTex);
        trackSelectTexture(listBgTex);
        trackSelectTexture(selectionTex);
        trackSelectTexture(scrollBgTex);

        // List style (dropdown items)
        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font = font;
        listStyle.fontColorSelected = new Color(1f, 1f, 0.85f, 1f);
        listStyle.fontColorUnselected = fontColor;
        listStyle.selection = new TextureRegionDrawable(new TextureRegion(selectionTex));
        listStyle.background = new TextureRegionDrawable(new TextureRegion(listBgTex));
        // ScrollPane style
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = new TextureRegionDrawable(new TextureRegion(scrollBgTex));

        // SelectBox style
        SelectBox.SelectBoxStyle style = new SelectBox.SelectBoxStyle();
        style.font = font;
        style.fontColor = fontColor;
        style.listStyle = listStyle;
        style.scrollStyle = scrollStyle;

        style.background = new TextureRegionDrawable(new TextureRegion(bgTex));
        style.backgroundOver = new TextureRegionDrawable(new TextureRegion(bgOverTex));
        style.backgroundOpen = new TextureRegionDrawable(new TextureRegion(bgOverTex));

        return style;
    }

    private void trackSelectTexture(Texture tex) {
        selectTextures.add(tex);
    }

    private Texture makeSelectBoxTex(Color fill, Color border, int w, int h, Color arrowColor) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(fill);
        pm.fill();

        pm.setColor(border);
        pm.drawRectangle(0, 0, w, h);

        int arrowCenterX = w - 24;
        int arrowCenterY = h / 2 + 1;
        int arrowHalfWidth = 6;
        int arrowHeight = 4;
        pm.setColor(arrowColor);
        pm.fillTriangle(
                arrowCenterX - arrowHalfWidth,
                arrowCenterY - arrowHeight,
                arrowCenterX + arrowHalfWidth,
                arrowCenterY - arrowHeight,
                arrowCenterX,
                arrowCenterY + arrowHeight);

        pm.setColor(border.r, border.g, border.b, 0.4f);
        pm.drawLine(w - 44, 7, w - 44, h - 8);

        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    // ── Texture Factories ─────────────────────────────────────────────────────

    private void createTextures() {
        // Dim overlay
        Pixmap dimPm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        dimPm.setColor(0f, 0f, 0f, 0.60f);
        dimPm.fill();
        dimTexture = new Texture(dimPm);
        dimPm.dispose();

        if (Gdx.files.internal("panel_wood.png").exists()) {
            panelTexture = new Texture(Gdx.files.internal("panel_wood.png"));
        } else {
            panelTexture = createWoodTexture((int) PANEL_WIDTH, PANEL_TEXTURE_HEIGHT,
                    new Color(0.55f, 0.32f, 0.10f, 0.97f),
                    new Color(0.48f, 0.27f, 0.08f, 0.97f),
                    new Color(0.25f, 0.12f, 0.03f, 1f));
        }

        panelHeaderTexture = createWoodTexture((int) PANEL_WIDTH, 72,
                new Color(0.42f, 0.22f, 0.06f, 1f),
                new Color(0.36f, 0.18f, 0.04f, 1f),
                new Color(0.20f, 0.09f, 0.02f, 1f));

        rowTexture = createSolidRounded((int) PANEL_WIDTH, 62,
                new Color(0.60f, 0.36f, 0.12f, 0.85f),
                new Color(0.25f, 0.12f, 0.03f, 0f));

        rowAltTexture = createSolidRounded((int) PANEL_WIDTH, 62,
                new Color(0.48f, 0.27f, 0.08f, 0.85f),
                new Color(0.25f, 0.12f, 0.03f, 0f));
    }

    /** Texture gỗ có vân ngang */
    private Texture createWoodTexture(int w, int h, Color base, Color grain, Color border) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(base);
        pm.fill();
        for (int y = 0; y < h; y += 18) {
            pm.setColor(grain.r, grain.g, grain.b, 0.25f);
            pm.drawLine(0, y, w, y);
            pm.setColor(grain.r, grain.g, grain.b, 0.10f);
            pm.drawLine(0, y + 1, w, y + 1);
        }
        pm.setColor(border);
        pm.drawRectangle(0, 0, w, h);
        pm.drawRectangle(1, 1, w - 2, h - 2);
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private Texture createSolidRounded(int w, int h, Color fill, Color border) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(fill);
        pm.fill();
        pm.setColor(border);
        pm.drawRectangle(0, 0, w, h);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private Texture makeFlatTex(Color fill, Color border, int w, int h) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(fill);
        pm.fill();
        pm.setColor(border);
        pm.drawRectangle(0, 0, w, h);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    /** Drawable vuông (flat) */
    private TextureRegionDrawable drawableFlat(Color fill, Color border, int w, int h) {
        return new TextureRegionDrawable(new TextureRegion(makeFlatTex(fill, border, w, h)));
    }

    /** FIX: Drawable bo góc rounded — dùng cho nút ĐÓNG */
    private TextureRegionDrawable drawableRounded(Color fill, Color border, int w, int h, int radius) {
        int r = Math.max(2, Math.min(radius, Math.min(w, h) / 2));
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();

        // Fill
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (insideRounded(x, y, w, h, r)) {
                    pm.setColor(fill);
                    pm.drawPixel(x, y);
                }
            }
        }
        // Border
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (!insideRounded(x, y, w, h, r))
                    continue;
                if (!insideRounded(x - 1, y, w, h, r) || !insideRounded(x + 1, y, w, h, r)
                        || !insideRounded(x, y - 1, w, h, r) || !insideRounded(x, y + 1, w, h, r)) {
                    pm.setColor(border);
                    pm.drawPixel(x, y);
                }
            }
        }

        Texture tex = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    private boolean insideRounded(int x, int y, int w, int h, int r) {
        if (x < 0 || y < 0 || x >= w || y >= h)
            return false;
        if (x >= r && x < w - r)
            return true;
        if (y >= r && y < h - r)
            return true;
        int cx = (x < r) ? (r - 1) : (w - r);
        int cy = (y < r) ? (r - 1) : (h - r);
        int dx = x - cx, dy = y - cy, rr = r - 1;
        return (dx * dx + dy * dy) <= (rr * rr);
    }

    // ── Dispose ───────────────────────────────────────────────────────────────

    private void disposeTextures() {
        if (dimTexture != null)
            dimTexture.dispose();
        if (panelTexture != null)
            panelTexture.dispose();
        if (panelHeaderTexture != null)
            panelHeaderTexture.dispose();
        if (rowTexture != null)
            rowTexture.dispose();
        if (rowAltTexture != null)
            rowAltTexture.dispose();
        if (speakerOnTexture != null)
            speakerOnTexture.dispose();
        if (speakerOffTexture != null)
            speakerOffTexture.dispose();
        if (dividerTexture != null)
            dividerTexture.dispose();
        // Slider textures
        if (sliderTrackTex != null)
            sliderTrackTex.dispose();
        if (sliderFillTex != null)
            sliderFillTex.dispose();
        if (sliderKnobTex != null)
            sliderKnobTex.dispose();
        if (sliderKnobOverTex != null)
            sliderKnobOverTex.dispose();

        // SelectBox textures
        for (Texture tex : selectTextures) {
            if (tex != null)
                tex.dispose();
        }
        selectTextures.clear();
    }
}