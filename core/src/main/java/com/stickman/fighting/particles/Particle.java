package com.stickman.fighting.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * Một hạt đơn lẻ trong hệ thống particle.
 * Dữ liệu thuần — không chứa logic render.
 */
public class Particle {

    public enum Shape { DOT, LINE, STAR }

    // ── Vị trí & Vật lý ──────────────────────────────────────────────────────
    public final Vector2 position = new Vector2();
    public final Vector2 velocity = new Vector2();
    public float         rotation;       // Độ (°) — dùng cho LINE, STAR
    public float         rotationSpeed;  // Độ/giây

    // ── Vòng đời ──────────────────────────────────────────────────────────────
    public float life;      // Thời gian còn lại (giây)
    public float maxLife;   // Thời gian sống tối đa (giây)
    public boolean active;  // Đang dùng hay đã chết

    // ── Hình dạng & Màu ───────────────────────────────────────────────────────
    public float size;          // Bán kính hoặc độ dài (px)
    public float endSize;       // Kích thước cuối (shrink effect)
    public final Color color    = new Color();
    public final Color endColor = new Color(); // Màu khi life → 0
    public Shape shape;

    // ── Vật lý mở rộng ────────────────────────────────────────────────────────
    public float gravity;    // Trọng lực riêng của particle (có thể 0)
    public float drag;       // Lực cản (0 = không cản, 0.95 = cản mạnh)

    // ── Pool reset ────────────────────────────────────────────────────────────
    public void reset() {
        position.set(0, 0);
        velocity.set(0, 0);
        rotation      = 0f;
        rotationSpeed = 0f;
        life          = 0f;
        maxLife       = 1f;
        active        = false;
        size          = 4f;
        endSize       = 0f;
        color.set(Color.WHITE);
        endColor.set(Color.WHITE);
        endColor.a    = 0f; // Mặc định fade out
        shape         = Shape.DOT;
        gravity       = 0f;
        drag          = 1.0f;
    }

    /** Tỉ lệ còn sống: 1.0 (mới sinh) → 0.0 (sắp chết) */
    public float getLifeRatio() {
        return maxLife > 0 ? life / maxLife : 0f;
    }
}
