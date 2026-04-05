package com.stickman.fighting.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/**
 * Factory tạo ra một LibGDX {@link Skin} mô phỏng phong cách gỗ (Dojo).
 * Hoàn toàn procedural (dùng Pixmap) – không cần file .atlas hay .skin.
 *
 * Cách dùng:
 *   Skin skin = WoodenSkin.create();
 *   TextButton btn = new TextButton("BẮT ĐẦU", skin);
 */
public final class WoodenSkin {

    // Màu gỗ
    private static final Color WOOD_DARK   = new Color(0.38f, 0.20f, 0.06f, 1f);
    private static final Color WOOD_MID    = new Color(0.62f, 0.38f, 0.14f, 1f);
    private static final Color WOOD_LIGHT  = new Color(0.78f, 0.55f, 0.25f, 1f);
    private static final Color WOOD_HOVER  = new Color(0.72f, 0.46f, 0.18f, 1f);
    private static final Color BORDER      = new Color(0.25f, 0.12f, 0.03f, 1f);
    private static final Color TEXT_COLOR  = new Color(0.98f, 0.92f, 0.75f, 1f); // Kem vàng

    // Palette menu theo tông vàng đồng - nâu
    private static final Color MENU_GOLD_UP = new Color(0.79f, 0.61f, 0.24f, 1f);
    private static final Color MENU_GOLD_OVER = new Color(0.85f, 0.67f, 0.30f, 1f);
    private static final Color MENU_GOLD_DOWN = new Color(0.69f, 0.50f, 0.19f, 1f);
    private static final Color MENU_RED_BROWN_UP = new Color(0.70f, 0.32f, 0.18f, 1f);
    private static final Color MENU_RED_BROWN_OVER = new Color(0.77f, 0.37f, 0.22f, 1f);
    private static final Color MENU_RED_BROWN_DOWN = new Color(0.57f, 0.25f, 0.14f, 1f);
    private static final Color MENU_DARK_BROWN_UP = new Color(0.44f, 0.29f, 0.13f, 1f);
    private static final Color MENU_DARK_BROWN_OVER = new Color(0.51f, 0.34f, 0.16f, 1f);
    private static final Color MENU_DARK_BROWN_DOWN = new Color(0.35f, 0.22f, 0.10f, 1f);
    private static final Color MENU_GOLD_BORDER = new Color(0.81f, 0.61f, 0.22f, 1f);
    private static final Color MENU_CREAM_TEXT = new Color(0.95f, 0.86f, 0.67f, 1f);
    private static final Color MENU_LIGHT_CREAM_TEXT = new Color(0.98f, 0.95f, 0.84f, 1f);

    private WoodenSkin() {}

    private static final String VI_GLYPHS = FreeTypeFontGenerator.DEFAULT_CHARS
        + "ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚÝ"
        + "àáâãèéêìíòóôõùúý"
        + "ĂăĐđĨĩŨũƠơƯư"
        + "ẠạẢảẤấẦầẨẩẪẫẬậ"
        + "ẮắẰằẲẳẴẵẶặẸẹẺẻẼẽẾếỀềỂểỄễỆệ"
        + "ỈỉỊịỌọỎỏỐốỒồỔổỖỗỘộỚớỜờỞởỠỡỢợ"
        + "ỤụỦủỨứỪừỬửỮữỰựỲỳỴỵỶỷỸỹ";

    public static BitmapFont createUIFont(int size) {
        FileHandle fontFile = resolvePreferredFontFile(
            "fonts/NotoSans-Regular.ttf",
            "fonts/DejaVuSans.ttf");
        if (fontFile == null) {
            BitmapFont fallback = new BitmapFont();
            fallback.getData().setScale(Math.max(1f, size / 16f));
            return fallback;
        }

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(fontFile);
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = size;
        p.characters = VI_GLYPHS;
        p.magFilter = Texture.TextureFilter.Linear;
        p.minFilter = Texture.TextureFilter.Linear;
        BitmapFont font = gen.generateFont(p);
        gen.dispose();
        return font;
    }

    private static BitmapFont createUIFont(int size, String[] internalCandidates, String[] windowsCandidates) {
        FileHandle fontFile = resolvePreferredFontFile(internalCandidates, windowsCandidates);
        if (fontFile == null) {
            return createUIFont(size);
        }

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(fontFile);
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = size;
        p.characters = VI_GLYPHS;
        p.magFilter = Texture.TextureFilter.Linear;
        p.minFilter = Texture.TextureFilter.Linear;
        BitmapFont font = gen.generateFont(p);
        gen.dispose();
        return font;
    }

    private static FileHandle resolvePreferredFontFile(String... internalCandidates) {
        for (String c : internalCandidates) {
            FileHandle internal = Gdx.files.internal(c);
            if (internal.exists()) return internal;
        }

        String[] windowsCandidates = new String[] {
            "C:/Windows/Fonts/segoeui.ttf",
            "C:/Windows/Fonts/arial.ttf",
            "C:/Windows/Fonts/tahoma.ttf"
        };
        for (String abs : windowsCandidates) {
            FileHandle fh = Gdx.files.absolute(abs);
            if (fh.exists()) return fh;
        }
        return null;
    }

    private static FileHandle resolvePreferredFontFile(String[] internalCandidates, String[] windowsCandidates) {
        for (String c : internalCandidates) {
            FileHandle internal = Gdx.files.internal(c);
            if (internal.exists()) return internal;
        }

        for (String abs : windowsCandidates) {
            FileHandle fh = Gdx.files.absolute(abs);
            if (fh.exists()) return fh;
        }
        return null;
    }

    public static Skin create() {
        Skin skin = new Skin();

        // 1. Font mặc định (built-in bitmap font)
        BitmapFont font = createUIFont(31,
            new String[] {"fonts/NotoSans-Regular.ttf", "fonts/DejaVuSans.ttf"},
            new String[] {"C:/Windows/Fonts/seguisb.ttf", "C:/Windows/Fonts/segoeui.ttf", "C:/Windows/Fonts/arial.ttf"});
        skin.add("default-font", font, BitmapFont.class);

        // 2. Tạo texture nút gỗ bằng Pixmap
        skin.add("wood-button-up",   makeTexture(WOOD_MID,   BORDER, 200, 55));
        skin.add("wood-button-over", makeTexture(WOOD_HOVER, BORDER, 200, 55));
        skin.add("wood-button-down", makeTexture(WOOD_DARK,  BORDER, 200, 55));

        // 3. TextButton.TextButtonStyle
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font     = skin.getFont("default-font");
        btnStyle.fontColor = TEXT_COLOR;
        btnStyle.up       = new TextureRegionDrawable(
            new TextureRegion(skin.get("wood-button-up", Texture.class)));
        btnStyle.over     = new TextureRegionDrawable(
            new TextureRegion(skin.get("wood-button-over", Texture.class)));
        btnStyle.down     = new TextureRegionDrawable(
            new TextureRegion(skin.get("wood-button-down", Texture.class)));
        skin.add("default", btnStyle, TextButton.TextButtonStyle.class);
        skin.add("wood", btnStyle, TextButton.TextButtonStyle.class);

        Color goldColor = new Color(0xCE9C3EFF);
        Color darkBrown = new Color(0x241302FF);
        Color redOrange = new Color(0xB33A1FFF);
        Color creamLight = new Color(0xFFF0D8FF);
        Color goldenBrown = new Color(0xA4883FFF);
        Color creamPale = new Color(0xFFF4D2FF);

        Color primaryBorder = new Color(0x5D2D0BFF);
        Color successBorder = new Color(0x4A170DFF);
        Color lightBorder = new Color(0x4E3716FF);

        TextButton.TextButtonStyle primaryStyle = new TextButton.TextButtonStyle();
        primaryStyle.font = font;
        primaryStyle.fontColor = darkBrown;
        primaryStyle.overFontColor = new Color(0x1B0F02FF);
        primaryStyle.downFontColor = new Color(0x1B0F02FF);
        primaryStyle.up = createRoundedColorDrawable(goldColor, primaryBorder, 320, 75, 36);
        primaryStyle.over = createRoundedColorDrawable(goldColor.cpy().mul(1.08f), primaryBorder.cpy().mul(0.95f), 320, 75, 36);
        primaryStyle.down = createRoundedColorDrawable(goldColor.cpy().mul(0.90f), primaryBorder, 320, 75, 36);
        skin.add("primary", primaryStyle, TextButton.TextButtonStyle.class);

        TextButton.TextButtonStyle successStyle = new TextButton.TextButtonStyle();
        successStyle.font = font;
        successStyle.fontColor = creamLight;
        successStyle.overFontColor = new Color(1f, 0.98f, 0.92f, 1f);
        successStyle.downFontColor = new Color(0.95f, 0.89f, 0.79f, 1f);
        successStyle.up = createRoundedColorDrawable(redOrange, successBorder, 220, 56, 28);
        successStyle.over = createRoundedColorDrawable(redOrange.cpy().mul(1.08f), successBorder.cpy().mul(0.95f), 220, 56, 28);
        successStyle.down = createRoundedColorDrawable(redOrange.cpy().mul(0.90f), successBorder, 220, 56, 28);
        skin.add("success", successStyle, TextButton.TextButtonStyle.class);

        TextButton.TextButtonStyle dangerStyle = makeButtonStyleFromFileOrColor(
            font,
            "btn_danger_up.png",
            new Color(0.84f, 0.29f, 0.25f, 1f),
            new Color(0.93f, 0.39f, 0.35f, 1f),
            new Color(0.67f, 0.18f, 0.15f, 1f),
            new Color(1f, 0.95f, 0.95f, 1f), 240, 64);
        skin.add("danger", dangerStyle, TextButton.TextButtonStyle.class);

        TextButton.TextButtonStyle lightStyle = new TextButton.TextButtonStyle();
        lightStyle.font = font;
        lightStyle.fontColor = creamPale;
        lightStyle.overFontColor = new Color(1f, 0.98f, 0.90f, 1f);
        lightStyle.downFontColor = new Color(0.95f, 0.89f, 0.76f, 1f);
        lightStyle.up = createRoundedColorDrawable(goldenBrown, lightBorder, 220, 56, 28);
        lightStyle.over = createRoundedColorDrawable(goldenBrown.cpy().mul(1.08f), lightBorder.cpy().mul(0.95f), 220, 56, 28);
        lightStyle.down = createRoundedColorDrawable(goldenBrown.cpy().mul(0.90f), lightBorder, 220, 56, 28);
        skin.add("light", lightStyle, TextButton.TextButtonStyle.class);

        TextButton.TextButtonStyle restartStyle = new TextButton.TextButtonStyle();
        restartStyle.font = font;
        restartStyle.fontColor = new Color(0xFFF5E3FF);
        restartStyle.overFontColor = new Color(0xFFFBEFFF);
        restartStyle.downFontColor = new Color(0xF2E2C8FF);
        Color restartUp = new Color(0xD2A23EFF);
        Color restartOver = new Color(0xE0B14BFF);
        Color restartDown = new Color(0xBE8F31FF);
        Color restartBorder = new Color(0x5F3A09FF);
        restartStyle.up = createRoundedColorDrawable(restartUp, restartBorder, 300, 64, 30);
        restartStyle.over = createRoundedColorDrawable(restartOver, restartBorder.cpy().mul(0.95f), 300, 64, 30);
        restartStyle.down = createRoundedColorDrawable(restartDown, restartBorder, 300, 64, 30);
        skin.add("restart", restartStyle, TextButton.TextButtonStyle.class);

        TextButton.TextButtonStyle quitStyle = new TextButton.TextButtonStyle();
        quitStyle.font = font;
        quitStyle.fontColor = new Color(0xFFF1E9FF);
        quitStyle.overFontColor = new Color(1f, 0.97f, 0.92f, 1f);
        quitStyle.downFontColor = new Color(0.95f, 0.89f, 0.82f, 1f);
        Color quitUp = new Color(0xB74431FF);
        Color quitOver = new Color(0xC8523EFF);
        Color quitDown = new Color(0x9C3626FF);
        Color quitBorder = new Color(0x4A130CFF);
        quitStyle.up = createRoundedColorDrawable(quitUp, quitBorder, 300, 64, 30);
        quitStyle.over = createRoundedColorDrawable(quitOver, quitBorder.cpy().mul(0.95f), 300, 64, 30);
        quitStyle.down = createRoundedColorDrawable(quitDown, quitBorder, 300, 64, 30);
        skin.add("quit", quitStyle, TextButton.TextButtonStyle.class);

        TextButton.TextButtonStyle resumeStyle = new TextButton.TextButtonStyle();
        resumeStyle.font = font;
        resumeStyle.fontColor = new Color(0xEFFFF0FF);
        resumeStyle.overFontColor = new Color(0xF6FFF7FF);
        resumeStyle.downFontColor = new Color(0xDEF7E0FF);
        Color resumeUp = new Color(0x4DAA4FFF);
        Color resumeOver = new Color(0x5ABB5CFF);
        Color resumeDown = new Color(0x3F9441FF);
        Color resumeBorder = new Color(0x1E4E20FF);
        resumeStyle.up = createRoundedColorDrawable(resumeUp, resumeBorder, 300, 64, 30);
        resumeStyle.over = createRoundedColorDrawable(resumeOver, resumeBorder.cpy().mul(0.95f), 300, 64, 30);
        resumeStyle.down = createRoundedColorDrawable(resumeDown, resumeBorder, 300, 64, 30);
        skin.add("resume", resumeStyle, TextButton.TextButtonStyle.class);

        TextButton.TextButtonStyle iconStyle = makeButtonStyle(font,
            new Color(0.97f, 0.86f, 0.35f, 1f),
            new Color(1f, 0.92f, 0.50f, 1f),
            new Color(0.84f, 0.70f, 0.20f, 1f),
            new Color(0.18f, 0.12f, 0.04f, 1f), 72, 56);
        skin.add("icon", iconStyle, TextButton.TextButtonStyle.class);

        // 4. Label.LabelStyle
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font      = skin.getFont("default-font");
        labelStyle.fontColor = TEXT_COLOR;
        skin.add("default", labelStyle, Label.LabelStyle.class);

        // 4b. Title label – to hơn
        BitmapFont titleFont = createUIFont(54);
        skin.add("title-font", titleFont, BitmapFont.class);

        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font      = titleFont;
        titleStyle.fontColor = TEXT_COLOR;
        skin.add("title", titleStyle, Label.LabelStyle.class);

        BitmapFont menuTitleFont = createUIFont(74,
            new String[] {"fonts/NotoSans-Regular.ttf", "fonts/DejaVuSans.ttf"},
            new String[] {"C:/Windows/Fonts/georgiab.ttf", "C:/Windows/Fonts/cambriaz.ttf", "C:/Windows/Fonts/segoeuib.ttf", "C:/Windows/Fonts/segoeui.ttf"});
        skin.add("menu-title-font", menuTitleFont, BitmapFont.class);
        Label.LabelStyle menuTitleStyle = new Label.LabelStyle(menuTitleFont, new Color(1f, 0.92f, 0.66f, 1f));
        skin.add("menuTitle", menuTitleStyle, Label.LabelStyle.class);

        BitmapFont subFont = createUIFont(24);
        skin.add("sub-font", subFont, BitmapFont.class);
        Label.LabelStyle subtitleStyle = new Label.LabelStyle(subFont, new Color(0.95f, 0.90f, 0.75f, 0.94f));
        skin.add("subtitle", subtitleStyle, Label.LabelStyle.class);

        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = loadDrawableOr("slider_track.png",
            makeTexture(WOOD_DARK, BORDER, 200, 10));
        sliderStyle.knobBefore = loadDrawableOr("slider_fill.png",
            makeTexture(new Color(0.40f, 0.72f, 0.96f, 1f), BORDER, 200, 10));
        sliderStyle.knob       = loadDrawableOr("slider_knob.png",
            makeTexture(WOOD_LIGHT, BORDER, 20, 20));
        skin.add("default-horizontal", sliderStyle, Slider.SliderStyle.class);

        // 6. SelectBox style
        SelectBox.SelectBoxStyle selectStyle = buildSelectBoxStyle(skin);
        skin.add("default", selectStyle, SelectBox.SelectBoxStyle.class);

        return skin;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Tạo Texture màu đơn giản với viền */
    private static Texture makeTexture(Color fill, Color border, int w, int h) {
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(fill);
        pixmap.fill();
        pixmap.setColor(border);
        pixmap.drawRectangle(0, 0, w, h);
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private static TextureRegionDrawable createColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    private static TextureRegionDrawable createRoundedColorDrawable(Color fill, Color border, int width, int height, int radius) {
        Texture tex = makeRoundedTexture(fill, border, width, height, radius);
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    private static TextButton.TextButtonStyle makeButtonStyle(
        BitmapFont font,
        Color up,
        Color over,
        Color down,
        Color text,
        int width,
        int height) {

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = text;
        style.up = new TextureRegionDrawable(new TextureRegion(makeTexture(up, BORDER, width, height)));
        style.over = new TextureRegionDrawable(new TextureRegion(makeTexture(over, BORDER, width, height)));
        style.down = new TextureRegionDrawable(new TextureRegion(makeTexture(down, BORDER, width, height)));
        return style;
    }

    private static TextButton.TextButtonStyle makeRoundedButtonStyle(
        BitmapFont font,
        Color up,
        Color over,
        Color down,
        Color text,
        int width,
        int height,
        int radius) {

        return makeRoundedButtonStyle(font, up, over, down, text, BORDER, width, height, radius);
    }

    private static TextButton.TextButtonStyle makeRoundedButtonStyle(
        BitmapFont font,
        Color up,
        Color over,
        Color down,
        Color text,
        Color border,
        int width,
        int height,
        int radius) {

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = text;
        style.up = new TextureRegionDrawable(new TextureRegion(
            makeRoundedTexture(up, border, width, height, radius)));
        style.over = new TextureRegionDrawable(new TextureRegion(
            makeRoundedTexture(over, border, width, height, radius)));
        style.down = new TextureRegionDrawable(new TextureRegion(
            makeRoundedTexture(down, border, width, height, radius)));
        return style;
    }

    private static Texture makeRoundedTexture(Color fill, Color border, int w, int h, int radius) {
        int r = Math.max(2, Math.min(radius, Math.min(w, h) / 2));

        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();

        // Tạo độ nổi bằng chuyển sắc dọc và bóng nhẹ ở nửa dưới.
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (!isInsideRoundedRect(x, y, w, h, r)) continue;
                float ny = h <= 1 ? 0f : (float) y / (float) (h - 1);
                float shade = 1.12f - (0.26f * ny);
                if (ny > 0.62f) shade *= 0.92f;
                float rr = Math.min(1f, fill.r * shade);
                float gg = Math.min(1f, fill.g * shade);
                float bb = Math.min(1f, fill.b * shade);
                pixmap.setColor(rr, gg, bb, fill.a);
                pixmap.drawPixel(x, y);
            }
        }

        // Dải highlight mỏng phía trên để tăng tương phản.
        float highlightAlpha = 0.10f;
        for (int y = 1; y < Math.max(2, h / 4); y++) {
            for (int x = 1; x < w - 1; x++) {
                if (!isInsideRoundedRect(x, y, w, h, r)) continue;
                pixmap.setColor(1f, 1f, 1f, highlightAlpha);
                pixmap.drawPixel(x, y);
            }
        }

        // Viền ngoài rõ hơn để nút không bị chìm vào background.
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (!isInsideRoundedRect(x, y, w, h, r)) continue;
                if (!isInsideRoundedRect(x - 1, y, w, h, r)
                    || !isInsideRoundedRect(x + 1, y, w, h, r)
                    || !isInsideRoundedRect(x, y - 1, w, h, r)
                    || !isInsideRoundedRect(x, y + 1, w, h, r)) {
                    pixmap.setColor(border);
                    pixmap.drawPixel(x, y);
                }
            }
        }

        // Viền trong sáng nhẹ giúp cạnh sắc nét hơn trên nền tối.
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                if (!isInsideRoundedRect(x, y, w, h, r)) continue;
                boolean hasOuter = !isInsideRoundedRect(x - 2, y, w, h, r)
                    || !isInsideRoundedRect(x + 2, y, w, h, r)
                    || !isInsideRoundedRect(x, y - 2, w, h, r)
                    || !isInsideRoundedRect(x, y + 2, w, h, r);
                if (hasOuter) {
                    float a = y < (h / 2) ? 0.22f : 0.14f;
                    pixmap.setColor(1f, 1f, 1f, a);
                    pixmap.drawPixel(x, y);
                }
            }
        }

        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private static boolean isInsideRoundedRect(int x, int y, int w, int h, int r) {
        if (x < 0 || y < 0 || x >= w || y >= h) return false;

        if (x >= r && x < w - r) return true;
        if (y >= r && y < h - r) return true;

        int cx = (x < r) ? (r - 1) : (w - r);
        int cy = (y < r) ? (r - 1) : (h - r);
        int dx = x - cx;
        int dy = y - cy;
        int rr = r - 1;
        return (dx * dx + dy * dy) <= (rr * rr);
    }

    private static TextButton.TextButtonStyle makeButtonStyleFromFileOrColor(
        BitmapFont font,
        String upFile,
        Color up,
        Color over,
        Color down,
        Color text,
        int width,
        int height) {

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = text;

        TextureRegionDrawable fileDrawable = loadDrawable(upFile);
        if (fileDrawable != null) {
            style.up = fileDrawable;
            style.over = fileDrawable;
            style.down = fileDrawable;
        } else {
            style.up = new TextureRegionDrawable(new TextureRegion(makeTexture(up, BORDER, width, height)));
            style.over = new TextureRegionDrawable(new TextureRegion(makeTexture(over, BORDER, width, height)));
            style.down = new TextureRegionDrawable(new TextureRegion(makeTexture(down, BORDER, width, height)));
        }
        return style;
    }

    private static TextureRegionDrawable loadDrawable(String fileName) {
        FileHandle fh = Gdx.files.internal(fileName);
        if (!fh.exists()) return null;
        return new TextureRegionDrawable(new TextureRegion(new Texture(fh)));
    }

    private static TextureRegionDrawable loadDrawableOr(String fileName, Texture fallbackTexture) {
        TextureRegionDrawable fromFile = loadDrawable(fileName);
        if (fromFile != null) return fromFile;
        return new TextureRegionDrawable(new TextureRegion(fallbackTexture));
    }

    /** SelectBox cần thêm List style + ScrollPane style */
    private static SelectBox.SelectBoxStyle buildSelectBoxStyle(Skin skin) {
        BitmapFont font = skin.getFont("default-font");

        // List item style
        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font            = font;
        listStyle.fontColorSelected   = TEXT_COLOR;
        listStyle.fontColorUnselected = TEXT_COLOR;
        listStyle.selection       = new TextureRegionDrawable(
            new TextureRegion(makeTexture(WOOD_DARK, BORDER, 200, 40)));
        skin.add("default", listStyle, List.ListStyle.class);

        // ScrollPane style (tối thiểu)
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        skin.add("default", scrollStyle, ScrollPane.ScrollPaneStyle.class);

        // SelectBox style
        SelectBox.SelectBoxStyle s = new SelectBox.SelectBoxStyle();
        s.font         = font;
        s.fontColor    = TEXT_COLOR;
        s.background   = loadDrawableOr("dropdown_box.png",
            makeTexture(WOOD_MID, BORDER, 200, 50));
        s.listStyle    = listStyle;
        s.scrollStyle  = scrollStyle;
        return s;
    }
}
