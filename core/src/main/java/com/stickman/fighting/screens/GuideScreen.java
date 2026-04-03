package com.stickman.fighting.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.stickman.fighting.MyFightingGame;
import com.stickman.fighting.ui.WoodenSkin;
import com.stickman.fighting.utils.Constants;
import com.stickman.fighting.utils.SoundManager;

public class GuideScreen implements Screen {

    private final MyFightingGame game;

    private Stage stage;
    private Skin skin;

    private Texture backgroundTexture;
    private final Array<Texture> generatedTextures = new Array<>();

    public GuideScreen(MyFightingGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        skin = WoodenSkin.create();
        installGuideStyles();

        backgroundTexture = createGuideBackgroundTexture();
        buildUI();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            goBackToMenu();
            return;
        }

        Gdx.gl.glClearColor(0.02f, 0.05f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(stage.getCamera().combined);
        game.batch.begin();
        game.batch.draw(backgroundTexture, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        for (Texture t : generatedTextures) {
            t.dispose();
        }
        generatedTextures.clear();
    }

    private void buildUI() {
        TextButton backButton = new TextButton("<", skin, "guideBack");
        enhanceBackButton(backButton, 1.07f);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                goBackToMenu();
            }
        });

        Table backOverlay = new Table();
        backOverlay.setFillParent(true);
        backOverlay.top().left().pad(16f, 18f, 0f, 0f);
        backOverlay.add(backButton).width(88f).height(52f);

        Table root = new Table();
        root.setFillParent(true);
        root.top().pad(28f, 12f, 12f, 12f);

        Label title = new Label("HUONG DAN CHOI", skin, "guideTitle");
        title.setAlignment(Align.center);

        Label subtitle = new Label(
            "Doc nhanh dieu khien va meo giao tranh truoc khi vao tran.",
            skin,
            "guideSubtitle");
        subtitle.setAlignment(Align.center);
        subtitle.setWrap(true);

        root.add(title).expandX().center().padBottom(4f).row();
        root.add(subtitle).width(760f).center().padBottom(14f).row();

        Table content = new Table();
        content.defaults().top();

        Table leftCol = new Table();
        leftCol.top();
        leftCol.add(makeGuideCard(
            "MUC TIEU TRAN DAU",
            "- Danh bai doi thu bang cach dua mau ve 0.\n"
                + "- Het gio: ben nhieu mau hon se thang.\n"
                + "- Neu bang mau khi het gio: ket qua hoa.",
            new Color(1.00f, 0.67f, 0.18f, 1f),
            386,
            174)).width(386f).height(174f).padBottom(10f).row();

        leftCol.add(makeGuideCard(
            "DIEU KHIEN NGUOI CHOI 2",
            "- Mui ten trai/phai: Di chuyen.\n"
                + "- Mui ten len: Nhay.\n"
                + "- Num4: Do, Num5: Dam, Num6: Da.\n"
                + "- Num4 + Num5: Energy-shot, Num3: Toc bien.",
            new Color(1.00f, 0.34f, 0.34f, 1f),
            386,
            196)).width(386f).height(196f);

        Table centerCol = new Table();
        centerCol.top();
        Image stickmanArt = new Image(new TextureRegionDrawable(
            new TextureRegion(createStickmanIllustrationTexture())));
        centerCol.add(stickmanArt).width(190f).height(388f).center();

        Table rightCol = new Table();
        rightCol.top();
        rightCol.add(makeGuideCard(
            "DIEU KHIEN NGUOI CHOI 1",
            "- A/D: Di chuyen trai phai.\n"
                + "- W: Nhay.\n"
                + "- U: Do, I: Dam, O: Da.\n"
                + "- U + I: Energy-shot, L: Toc bien.",
            new Color(0.30f, 0.72f, 1.00f, 1f),
            386,
            184)).width(386f).height(184f).padBottom(10f).row();

        rightCol.add(makeGuideCard(
            "MEO CHOI NHANH",
            "- Do don giam sat thuong nhan vao.\n"
                + "- Kick gay 10 damage.\n"
                + "- Combo 5 hit khong bi do se knockdown.\n"
                + "- Energy-shot: 14 damage, cooldown 0.75s.",
            new Color(0.44f, 0.95f, 0.50f, 1f),
            386,
            186)).width(386f).height(186f);

        content.add(leftCol).width(392f).padRight(6f).fillY();
        content.add(centerCol).width(192f).padLeft(4f).padRight(4f).fillY();
        content.add(rightCol).width(392f).padLeft(6f).fillY();

        root.add(content).expand().fill().row();

        Label backHint = new Label("Nhan ESC hoac nut mui ten de quay lai", skin, "guideHint");
        backHint.setAlignment(Align.center);
        root.add(backHint).center().padTop(8f).padBottom(2f);

        stage.addActor(root);
        stage.addActor(backOverlay);
    }

    private Table makeGuideCard(
        String heading,
        String content,
        Color accentColor,
        int cardWidth,
        int cardHeight) {

        Table card = new Table();
        card.setBackground(new TextureRegionDrawable(
            new TextureRegion(createNeonCardTexture(cardWidth, cardHeight, accentColor))));
        card.top().left().pad(12f, 14f, 12f, 14f);
        card.setClip(true);

        Label cardTitle = new Label(heading, skin, "guideCardTitle");
        cardTitle.setColor(accentColor);
        cardTitle.setAlignment(Align.left);

        Label cardContent = new Label(content, skin, "guideCardContent");
        cardContent.setWrap(true);
        cardContent.setAlignment(Align.topLeft);

        card.add(cardTitle).left().expandX().fillX().padBottom(8f).row();
        card.add(cardContent).left().top().width(cardWidth - 28f).expand().fill();

        return card;
    }

    private void goBackToMenu() {
        SoundManager.getInstance().playSound(SoundManager.SoundEffect.BUTTON_CLICK);
        game.setScreen(new MainMenuScreen(game));
    }

    private void installGuideStyles() {
        BitmapFont titleFont = WoodenSkin.createUIFont(42);
        BitmapFont subtitleFont = WoodenSkin.createUIFont(20);
        BitmapFont cardTitleFont = WoodenSkin.createUIFont(24);
        BitmapFont cardContentFont = WoodenSkin.createUIFont(17);
        BitmapFont hintFont = WoodenSkin.createUIFont(16);
        BitmapFont backFont = WoodenSkin.createUIFont(34);

        skin.add("guide-title-font", titleFont, BitmapFont.class);
        skin.add("guide-subtitle-font", subtitleFont, BitmapFont.class);
        skin.add("guide-card-title-font", cardTitleFont, BitmapFont.class);
        skin.add("guide-card-content-font", cardContentFont, BitmapFont.class);
        skin.add("guide-hint-font", hintFont, BitmapFont.class);
        skin.add("guide-back-font", backFont, BitmapFont.class);

        skin.add("guideTitle", new Label.LabelStyle(
            titleFont, new Color(0.88f, 0.96f, 1f, 1f)), Label.LabelStyle.class);
        skin.add("guideSubtitle", new Label.LabelStyle(
            subtitleFont, new Color(0.63f, 0.74f, 0.84f, 1f)), Label.LabelStyle.class);
        skin.add("guideCardTitle", new Label.LabelStyle(
            cardTitleFont, new Color(0.92f, 0.96f, 1f, 1f)), Label.LabelStyle.class);
        skin.add("guideCardContent", new Label.LabelStyle(
            cardContentFont, new Color(0.82f, 0.88f, 0.95f, 1f)), Label.LabelStyle.class);
        skin.add("guideHint", new Label.LabelStyle(
            hintFont, new Color(0.56f, 0.67f, 0.79f, 1f)), Label.LabelStyle.class);

        TextButton.TextButtonStyle backStyle = new TextButton.TextButtonStyle();
        backStyle.font = backFont;
        backStyle.fontColor = new Color(0.88f, 0.95f, 1f, 1f);
        backStyle.up = new TextureRegionDrawable(new TextureRegion(
            createRoundedButtonTexture(88, 52,
                new Color(0.08f, 0.13f, 0.22f, 0.95f),
                new Color(0.32f, 0.85f, 1.0f, 0.95f),
                16)));
        backStyle.over = new TextureRegionDrawable(new TextureRegion(
            createRoundedButtonTexture(88, 52,
                new Color(0.10f, 0.17f, 0.28f, 0.98f),
                new Color(0.52f, 0.95f, 1.0f, 1f),
                16)));
        backStyle.down = new TextureRegionDrawable(new TextureRegion(
            createRoundedButtonTexture(88, 52,
                new Color(0.06f, 0.10f, 0.18f, 1f),
                new Color(0.28f, 0.72f, 0.95f, 1f),
                16)));
        skin.add("guideBack", backStyle, TextButton.TextButtonStyle.class);
    }

    private void enhanceBackButton(TextButton button, float hoverScale) {
        button.setTransform(true);
        button.setOrigin(Align.center);
        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                button.clearActions();
                button.addAction(Actions.parallel(
                    Actions.scaleTo(hoverScale, hoverScale, 0.12f),
                    Actions.color(new Color(0.65f, 0.95f, 1f, 1f), 0.12f)
                ));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                button.clearActions();
                button.addAction(Actions.parallel(
                    Actions.scaleTo(1f, 1f, 0.12f),
                    Actions.color(Color.WHITE, 0.12f)
                ));
            }
        });
    }

    private Texture createNeonCardTexture(int w, int h, Color accentColor) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        int radius = Math.min(28, Math.min(w, h) / 4);

        Color baseTop = new Color(0.08f, 0.11f, 0.17f, 0.94f);
        Color baseBottom = new Color(0.05f, 0.08f, 0.13f, 0.94f);
        Color innerBorder = accentColor.cpy().lerp(Color.WHITE, 0.24f);
        Color outerGlow = accentColor.cpy();
        outerGlow.a = 0.28f;

        for (int y = 0; y < h; y++) {
            float t = (float) y / (float) Math.max(1, h - 1);
            float r = baseBottom.r + (baseTop.r - baseBottom.r) * t;
            float g = baseBottom.g + (baseTop.g - baseBottom.g) * t;
            float b = baseBottom.b + (baseTop.b - baseBottom.b) * t;
            float a = baseTop.a;

            for (int x = 0; x < w; x++) {
                if (insideRoundedRect(x, y, w, h, radius)) {
                    pm.drawPixel(x, y, Color.rgba8888(r, g, b, a));
                } else if (insideRoundedRect(x, y, w, h, radius + 3)) {
                    pm.drawPixel(x, y, Color.rgba8888(outerGlow));
                }
            }
        }

        // Viền chính + viền sáng trong để tạo cảm giác glow.
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                if (!insideRoundedRect(x, y, w, h, radius)) continue;

                boolean borderPixel =
                    !insideRoundedRect(x - 1, y, w, h, radius)
                    || !insideRoundedRect(x + 1, y, w, h, radius)
                    || !insideRoundedRect(x, y - 1, w, h, radius)
                    || !insideRoundedRect(x, y + 1, w, h, radius);

                if (borderPixel) {
                    pm.drawPixel(x, y, Color.rgba8888(accentColor));
                    continue;
                }

                boolean innerGlowPixel =
                    !insideRoundedRect(x - 2, y, w, h, radius)
                    || !insideRoundedRect(x + 2, y, w, h, radius)
                    || !insideRoundedRect(x, y - 2, w, h, radius)
                    || !insideRoundedRect(x, y + 2, w, h, radius);

                if (innerGlowPixel) {
                    pm.drawPixel(x, y, Color.rgba8888(innerBorder));
                }
            }
        }

        Texture texture = new Texture(pm);
        pm.dispose();
        generatedTextures.add(texture);
        return texture;
    }

    private Texture createRoundedButtonTexture(int w, int h, Color fill, Color border, int radius) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (insideRoundedRect(x, y, w, h, radius)) {
                    pm.drawPixel(x, y, Color.rgba8888(fill));
                }
            }
        }

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                if (!insideRoundedRect(x, y, w, h, radius)) continue;
                boolean borderPixel =
                    !insideRoundedRect(x - 1, y, w, h, radius)
                    || !insideRoundedRect(x + 1, y, w, h, radius)
                    || !insideRoundedRect(x, y - 1, w, h, radius)
                    || !insideRoundedRect(x, y + 1, w, h, radius);
                if (borderPixel) {
                    pm.drawPixel(x, y, Color.rgba8888(border));
                }
            }
        }

        Texture texture = new Texture(pm);
        pm.dispose();
        generatedTextures.add(texture);
        return texture;
    }

    private Texture createStickmanIllustrationTexture() {
        int w = 190;
        int h = 388;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        // Nền kính tối nhẹ.
        for (int y = 0; y < h; y++) {
            float t = (float) y / (float) Math.max(1, h - 1);
            Color c = new Color(0.04f + 0.02f * t, 0.07f + 0.04f * t, 0.12f + 0.08f * t, 0.50f);
            for (int x = 0; x < w; x++) {
                if (insideRoundedRect(x, y, w, h, 24)) {
                    pm.drawPixel(x, y, Color.rgba8888(c));
                }
            }
        }

        Color blue = new Color(0.34f, 0.78f, 1f, 1f);
        Color red = new Color(1f, 0.38f, 0.38f, 1f);

        drawStickman(pm, 58, 110, blue, true);
        drawStickman(pm, 132, 104, red, false);

        // Sàn neon.
        drawLineThick(pm, 20, 70, 170, 70, 2, new Color(0.38f, 0.90f, 1f, 0.95f));

        Texture texture = new Texture(pm);
        pm.dispose();
        generatedTextures.add(texture);
        return texture;
    }

    private void drawStickman(Pixmap pm, int cx, int by, Color color, boolean faceRight) {
        int headR = 14;
        int headY = by + 120;
        int neckY = headY - headR;
        int hipY = by + 60;
        int shoulderY = by + 88;

        pm.setColor(color);
        pm.drawCircle(cx, headY, headR);
        drawLineThick(pm, cx, neckY, cx, hipY, 2, color);

        int armForward = faceRight ? 26 : -26;
        int armBack = -armForward;
        drawLineThick(pm, cx, shoulderY, cx + armForward, shoulderY + 16, 2, color);
        drawLineThick(pm, cx, shoulderY, cx + armBack, shoulderY - 12, 2, color);

        int legForward = faceRight ? 22 : -22;
        int legBack = -legForward;
        drawLineThick(pm, cx, hipY, cx + legForward, by + 20, 2, color);
        drawLineThick(pm, cx, hipY, cx + legBack, by + 12, 2, color);
    }

    private void drawLineThick(Pixmap pm, int x1, int y1, int x2, int y2, int thickness, Color color) {
        pm.setColor(color);
        int half = Math.max(1, thickness);
        for (int o = -half; o <= half; o++) {
            pm.drawLine(x1 + o, y1, x2 + o, y2);
            pm.drawLine(x1, y1 + o, x2, y2 + o);
        }
    }

    private boolean insideRoundedRect(int x, int y, int w, int h, int radius) {
        int r = Math.max(1, Math.min(radius, Math.min(w, h) / 2));

        if (x >= r && x < w - r) return y >= 0 && y < h;
        if (y >= r && y < h - r) return x >= 0 && x < w;

        int cx = x < r ? r : (w - r - 1);
        int cy = y < r ? r : (h - r - 1);
        int dx = x - cx;
        int dy = y - cy;
        return (dx * dx + dy * dy) <= (r * r);
    }

    private Texture createGuideBackgroundTexture() {
        int w = Constants.SCREEN_WIDTH;
        int h = Constants.SCREEN_HEIGHT;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        Color top = new Color(0.03f, 0.08f, 0.16f, 1f);
        Color bottom = new Color(0.01f, 0.03f, 0.08f, 1f);

        for (int y = 0; y < h; y++) {
            float t = (float) y / (float) Math.max(1, h - 1);
            float r = bottom.r + (top.r - bottom.r) * t;
            float g = bottom.g + (top.g - bottom.g) * t;
            float b = bottom.b + (top.b - bottom.b) * t;
            for (int x = 0; x < w; x++) {
                pm.drawPixel(x, y, Color.rgba8888(r, g, b, 1f));
            }
        }

        // Grid neon nhẹ.
        for (int y = 28; y < h; y += 32) {
            float alpha = (y % 64 == 0) ? 0.12f : 0.06f;
            pm.setColor(0.25f, 0.75f, 1f, alpha);
            pm.drawLine(0, y, w, y);
        }
        for (int x = 30; x < w; x += 36) {
            float alpha = (x % 72 == 0) ? 0.09f : 0.04f;
            pm.setColor(0.25f, 0.75f, 1f, alpha);
            pm.drawLine(x, 0, x, h);
        }

        // Vignette tối nhẹ quanh mép.
        pm.setColor(0f, 0f, 0f, 0.45f);
        for (int i = 0; i < 30; i++) {
            pm.drawRectangle(i, i, w - i * 2, h - i * 2);
        }

        Texture texture = new Texture(pm);
        pm.dispose();
        return texture;
    }
}
