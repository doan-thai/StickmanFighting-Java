package com.stickman.fighting.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class WeaponRenderer {
    private static Texture swordTexture;
    private static TextureRegion swordRegion;

    public static void init() {
        if (swordTexture != null) return;

        int w = 128;
        int h = 32;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        // Màu sắc
        Color bladeColor = new Color(0.75f, 0.85f, 0.95f, 1f); // Bạc ánh xanh
        Color guardColor = new Color(0.85f, 0.65f, 0.2f, 1f);  // Vàng đồng
        Color handleColor = new Color(0.4f, 0.2f, 0.1f, 1f);   // Nâu gỗ
        Color outlineColor = new Color(0.1f, 0.1f, 0.1f, 1f);

        // 1. Vẽ Cán kiếm (Handle)
        pm.setColor(outlineColor);
        pm.fillRectangle(4, 12, 22, 8);
        pm.setColor(handleColor);
        pm.fillRectangle(5, 13, 20, 6);

        // 2. Vẽ Chắn tay (Guard)
        pm.setColor(outlineColor);
        pm.fillRectangle(24, 6, 8, 20);
        pm.setColor(guardColor);
        pm.fillRectangle(25, 7, 6, 18);

        // 3. Vẽ Lưỡi kiếm (Blade) - Thuôn nhọn
        int bladeStart = 32;
        int bladeEnd = 120;
        int bladeWidthBase = 8;
        
        // Vẽ outline lưỡi
        pm.setColor(outlineColor);
        for (int i = bladeStart; i < bladeEnd; i++) {
            int currentWidth = bladeWidthBase - (int)((i - bladeStart) * (bladeWidthBase - 2) / (float)(bladeEnd - bladeStart));
            pm.fillRectangle(i, 16 - currentWidth/2 - 1, 1, currentWidth + 2);
        }

        // Vẽ thân lưỡi với Gradient giả lập ánh kim
        for (int i = bladeStart + 1; i < bladeEnd - 1; i++) {
            float progress = (i - bladeStart) / (float)(bladeEnd - bladeStart);
            int currentWidth = bladeWidthBase - (int)(progress * (bladeWidthBase - 2));
            
            for (int j = 0; j < currentWidth; j++) {
                float vPos = j / (float)currentWidth;
                Color c = bladeColor.cpy();
                if (vPos < 0.2f) c.mul(1.2f); // Highlight cạnh trên
                else if (vPos > 0.8f) c.mul(0.8f); // Shadow cạnh dưới
                else if (Math.abs(vPos - 0.5f) < 0.1f) c.mul(0.7f); // Rãnh giữa (Fuller)
                
                pm.setColor(c);
                pm.drawPixel(i, 16 - currentWidth/2 + j);
            }
        }
        
        // Vẽ mũi kiếm (Point)
        pm.setColor(outlineColor);
        pm.drawPixel(bladeEnd, 15);
        pm.drawPixel(bladeEnd, 16);

        swordTexture = new Texture(pm);
        swordTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        swordRegion = new TextureRegion(swordTexture);
        pm.dispose();
    }

    public static TextureRegion getSwordRegion() {
        if (swordRegion == null) init();
        return swordRegion;
    }

    public static void dispose() {
        if (swordTexture != null) {
            swordTexture.dispose();
            swordTexture = null;
            swordRegion = null;
        }
    }
}
