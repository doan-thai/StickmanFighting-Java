package com.stickman.fighting.particles;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ParticleSystem — Singleton quản lý toàn bộ particle effects.
 *
 * Cách dùng từ PlayScreen:
 * // Khi đánh trúng:
 * ParticleSystem.getInstance().emit(
 * ParticleEffect.EffectType.HIT_SPARK, hitX, hitY);
 *
 * // Trong render():
 * ParticleSystem.getInstance().render(shapeRenderer);
 *
 * // Trong update():
 * ParticleSystem.getInstance().update(delta);
 */
public class ParticleSystem {

    private static ParticleSystem instance;

    public static ParticleSystem getInstance() {
        if (instance == null)
            instance = new ParticleSystem();
        return instance;
    }

    private final List<ParticleEffect> activeEffects = new ArrayList<>(16);
    private final ParticlePool pool = new ParticlePool();

    private ParticleSystem() {
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Phát hiệu ứng tại vị trí (x, y).
     * Thread-safe với main loop vì LibGDX single-threaded.
     */
    public void emit(ParticleEffect.EffectType type, float x, float y) {
        ParticleEffect effect = pool.obtain();
        effect.start(type, x, y);
        activeEffects.add(effect);
    }

    /** Phát combo: spark + impact cùng lúc (đòn đánh mạnh) */
    public void emitHitCombo(float x, float y) {
        emit(ParticleEffect.EffectType.HIT_SPARK, x, y);
        emit(ParticleEffect.EffectType.HIT_IMPACT, x, y);
        emit(ParticleEffect.EffectType.BLOOD_BURST, x, y);
    }

    /** Phát hiệu ứng KO explosion */
    public void emitKO(float x, float y) {
        emit(ParticleEffect.EffectType.KO_EXPLOSION, x, y);
        // Thêm vài hit spark xung quanh
        for (int i = 0; i < 3; i++) {
            float ox = x + (float) (Math.random() * 60 - 30);
            float oy = y + (float) (Math.random() * 40 - 20);
            emit(ParticleEffect.EffectType.HIT_SPARK, ox, oy);
        }
    }

    /**
     * Cập nhật tất cả effects, thu hồi effect đã xong về pool.
     * Gọi mỗi frame từ PlayScreen.update().
     */
    public void update(float delta) {
        Iterator<ParticleEffect> it = activeEffects.iterator();
        while (it.hasNext()) {
            ParticleEffect effect = it.next();
            effect.update(delta);
            if (effect.isFinished()) {
                it.remove();
                pool.free(effect); // Trả về pool
            }
        }
    }

    /**
     * Render tất cả effects.
     * Gọi sau khi render fighters, trước khi render HUD.
     * ShapeRenderer projection matrix phải được set trước khi gọi.
     */
    public void render(ShapeRenderer sr) {
        if (activeEffects.isEmpty())
            return;
        for (ParticleEffect effect : activeEffects) {
            effect.render(sr);
        }
    }

    /** Xóa toàn bộ effects (dùng khi reset round) */
    public void clear() {
        for (ParticleEffect e : activeEffects)
            pool.free(e);
        activeEffects.clear();
    }

    public void dispose() {
        clear();
        instance = null;
    }

    public int getActiveCount() {
        return activeEffects.size();
    }
}
