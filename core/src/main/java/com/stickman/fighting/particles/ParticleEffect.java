package com.stickman.fighting.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ParticleEffect — Một hiệu ứng gồm nhiều Particle.
 *
 * Mỗi EffectType định nghĩa cách spawn và behavior khác nhau.
 * Pool-based: được tái sử dụng qua ParticlePool để giảm GC.
 */
public class ParticleEffect {

    // ── Loại hiệu ứng ────────────────────────────────────────────────────────
    public enum EffectType {
        HIT_SPARK, // Tia lửa nhỏ tóe ra — màu vàng/cam
        HIT_IMPACT, // Vòng shockwave mở rộng — trắng/vàng
        BLOOD_BURST, // Chấm đỏ cartoon bắn ra
        KO_EXPLOSION, // Nổ lớn nhiều màu khi KO
        DUST_LAND, // Bụi xám khi đáp đất
        JUMP_PUFF // Khói trắng nhỏ khi nhảy
    }

    // ── Fields ────────────────────────────────────────────────────────────────
    private final List<Particle> particles = new ArrayList<>(32);
    private EffectType type;
    private boolean finished = true;

    // Shockwave ring (chỉ dùng cho HIT_IMPACT)
    private float ringRadius, ringMaxRadius, ringAlpha;
    private boolean hasRing = false;

    // ── Khởi tạo effect tại vị trí (x, y) ───────────────────────────────────
    public void start(EffectType type, float x, float y) {
        this.type = type;
        this.finished = false;
        this.hasRing = false;
        particles.clear();

        switch (type) {
            case HIT_SPARK -> spawnHitSpark(x, y);
            case HIT_IMPACT -> spawnHitImpact(x, y);
            case BLOOD_BURST -> spawnBloodBurst(x, y);
            case KO_EXPLOSION -> spawnKOExplosion(x, y);
            case DUST_LAND -> spawnDustLand(x, y);
            case JUMP_PUFF -> spawnJumpPuff(x, y);
        }
    }

    // ── Update mỗi frame ──────────────────────────────────────────────────────
    public void update(float delta) {
        if (finished)
            return;

        int aliveCount = 0;

        for (Particle p : particles) {
            if (!p.active)
                continue;

            // Vật lý
            p.velocity.y += p.gravity * delta;
            p.velocity.scl(p.drag); // Áp dụng drag
            p.position.x += p.velocity.x * delta;
            p.position.y += p.velocity.y * delta;
            p.rotation += p.rotationSpeed * delta;

            // Giảm thời gian sống
            p.life -= delta;
            if (p.life <= 0f) {
                p.active = false;
            } else {
                aliveCount++;
            }
        }

        // Cập nhật ring (HIT_IMPACT)
        if (hasRing) {
            ringRadius += 280f * delta;
            ringAlpha -= delta * 3.0f;
            if (ringAlpha <= 0f || ringRadius >= ringMaxRadius) {
                hasRing = false;
            } else {
                aliveCount++;
            }
        }

        if (aliveCount == 0)
            finished = true;
    }

    // ── Render ────────────────────────────────────────────────────────────────

    /**
     * Vẽ tất cả particle.
     * Gọi giữa ShapeRenderer begin/end từ bên ngoài KHÔNG được —
     * method này tự quản lý begin/end cho từng ShapeType.
     */
    public void render(ShapeRenderer sr) {
        if (finished)
            return;

        // ── Pass Filled: DOT ──────────────────────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (Particle p : particles) {
            if (!p.active)
                continue;
            if (p.shape != Particle.Shape.DOT)
                continue;

            float ratio = p.getLifeRatio();
            float curSize = lerp(p.endSize, p.size, ratio);
            if (curSize < 0.5f)
                continue;

            Color c = lerpColor(p.endColor, p.color, ratio);
            sr.setColor(c);
            sr.circle(p.position.x, p.position.y, curSize);
        }

        // Ring (HIT_IMPACT) — filled circle rỗng bằng 2 vòng
        // Ring (HIT_IMPACT) — filled circle rỗng bằng 2 vòng
        if (hasRing) {
            sr.setColor(1f, 0.95f, 0.6f, ringAlpha);
            // Sửa (0, 0) thành getRingX(), getRingY()
            sr.circle(getRingX(), getRingY(), ringRadius);
        }
        sr.end();

        // ── Pass Line: LINE, STAR ─────────────────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Line);
        for (Particle p : particles) {
            if (!p.active)
                continue;
            if (p.shape == Particle.Shape.DOT)
                continue;

            float ratio = p.getLifeRatio();
            float curSize = lerp(p.endSize, p.size, ratio);
            if (curSize < 0.5f)
                continue;

            Color c = lerpColor(p.endColor, p.color, ratio);
            sr.setColor(c);

            float rad = p.rotation * MathUtils.degreesToRadians;

            if (p.shape == Particle.Shape.LINE) {
                // Vẽ đường thẳng theo hướng velocity (tia lửa)
                float dx = MathUtils.cos(rad) * curSize;
                float dy = MathUtils.sin(rad) * curSize;
                sr.line(p.position.x - dx, p.position.y - dy,
                        p.position.x + dx, p.position.y + dy);
            } else if (p.shape == Particle.Shape.STAR) {
                // Vẽ dấu × (2 đường chéo)
                float dx1 = MathUtils.cos(rad) * curSize;
                float dy1 = MathUtils.sin(rad) * curSize;
                float dx2 = MathUtils.cos(rad + MathUtils.PI / 2f) * curSize;
                float dy2 = MathUtils.sin(rad + MathUtils.PI / 2f) * curSize;
                sr.line(p.position.x - dx1, p.position.y - dy1,
                        p.position.x + dx1, p.position.y + dy1);
                sr.line(p.position.x - dx2, p.position.y - dy2,
                        p.position.x + dx2, p.position.y + dy2);
            }
        }

        // Ring shockwave (vòng mở rộng)
        if (hasRing && ringRadius > 0) {
            sr.setColor(1f, 0.95f, 0.5f, Math.min(ringAlpha, 1f));
            sr.circle(getRingX(), getRingY(), ringRadius);
            sr.setColor(1f, 1f, 1f, Math.min(ringAlpha * 0.5f, 1f));
            sr.circle(getRingX(), getRingY(), ringRadius * 0.8f);
        }
        sr.end();
    }

    // Lưu tọa độ ring để render
    private float ringCx, ringCy;

    private float getRingX() {
        return ringCx;
    }

    private float getRingY() {
        return ringCy;
    }

    // ── Spawn Definitions ─────────────────────────────────────────────────────

    /**
     * HIT_SPARK — 8-12 tia lửa bắn ra nhiều hướng
     * Màu: vàng → cam → trong suốt
     */
    private void spawnHitSpark(float x, float y) {
        int count = MathUtils.random(8, 12);
        for (int i = 0; i < count; i++) {
            Particle p = newParticle();
            p.position.set(x, y);
            p.shape = Particle.Shape.LINE;

            // Hướng ngẫu nhiên, tập trung theo chiều ngang
            float angle = MathUtils.random(150f, 390f); // Chủ yếu sang ngang
            float speed = MathUtils.random(120f, 380f);
            p.velocity.set(
                    MathUtils.cosDeg(angle) * speed,
                    MathUtils.sinDeg(angle) * speed);
            p.rotation = angle;
            p.rotationSpeed = MathUtils.random(-120f, 120f);

            p.life = p.maxLife = MathUtils.random(0.10f, 0.22f);
            p.size = MathUtils.random(4f, 10f);
            p.endSize = 0f;

            p.gravity = -600f; // Rơi nhanh
            p.drag = 0.92f;

            // Màu: vàng sáng → cam
            p.color.set(1f, MathUtils.random(0.75f, 1.0f), 0f, 1f);
            p.endColor.set(1f, 0.3f, 0f, 0f);
            p.active = true;
        }
    }

    /**
     * HIT_IMPACT — Vòng shockwave + vài chấm trắng
     */
    private void spawnHitImpact(float x, float y) {
        // Vòng ring
        hasRing = true;
        ringCx = x;
        ringCy = y;
        ringRadius = 8f;
        ringMaxRadius = 80f;
        ringAlpha = 1.0f;

        // Vài chấm sáng bắn ra
        int count = MathUtils.random(4, 7);
        for (int i = 0; i < count; i++) {
            Particle p = newParticle();
            p.position.set(x, y);
            p.shape = Particle.Shape.DOT;

            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(60f, 180f);
            p.velocity.set(
                    MathUtils.cosDeg(angle) * speed,
                    MathUtils.sinDeg(angle) * speed);

            p.life = p.maxLife = MathUtils.random(0.15f, 0.30f);
            p.size = MathUtils.random(3f, 7f);
            p.endSize = 0f;
            p.drag = 0.88f;

            p.color.set(1f, 1f, 0.8f, 1f);
            p.endColor.set(1f, 0.8f, 0.2f, 0f);
            p.active = true;
        }
    }

    /**
     * BLOOD_BURST — Chấm đỏ cartoon bắn ra (phong cách anime)
     */
    private void spawnBloodBurst(float x, float y) {
        int count = MathUtils.random(6, 10);
        for (int i = 0; i < count; i++) {
            Particle p = newParticle();
            p.position.set(x + MathUtils.random(-5f, 5f),
                    y + MathUtils.random(-5f, 5f));
            p.shape = Particle.Shape.DOT;

            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(80f, 250f);
            p.velocity.set(
                    MathUtils.cosDeg(angle) * speed,
                    MathUtils.sinDeg(angle) * speed);

            p.life = p.maxLife = MathUtils.random(0.20f, 0.45f);
            p.size = MathUtils.random(3f, 8f);
            p.endSize = p.size * 0.3f; // Không biến mất hoàn toàn

            p.gravity = -400f;
            p.drag = 0.90f;

            // Đỏ tươi → đỏ tối
            p.color.set(1f, 0.05f, 0.05f, 1f);
            p.endColor.set(0.5f, 0f, 0f, 0f);
            p.active = true;
        }
    }

    /**
     * KO_EXPLOSION — Nổ lớn nhiều màu (vàng, cam, trắng, đỏ)
     */
    private void spawnKOExplosion(float x, float y) {
        // Ring lớn
        hasRing = true;
        ringCx = x;
        ringCy = y;
        ringRadius = 15f;
        ringMaxRadius = 160f;
        ringAlpha = 1.2f;

        // Nhiều STAR particles
        int count = MathUtils.random(20, 28);
        Color[] palette = {
                new Color(1f, 1f, 0.2f, 1f), // Vàng
                new Color(1f, 0.5f, 0f, 1f), // Cam
                new Color(1f, 1f, 1f, 1f), // Trắng
                new Color(1f, 0.2f, 0.1f, 1f), // Đỏ
        };

        for (int i = 0; i < count; i++) {
            Particle p = newParticle();
            p.position.set(x + MathUtils.random(-10f, 10f),
                    y + MathUtils.random(-10f, 10f));
            p.shape = (i % 3 == 0) ? Particle.Shape.STAR : Particle.Shape.DOT;

            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(150f, 500f);
            p.velocity.set(
                    MathUtils.cosDeg(angle) * speed,
                    MathUtils.sinDeg(angle) * speed);
            p.rotation = angle;
            p.rotationSpeed = MathUtils.random(-200f, 200f);

            p.life = p.maxLife = MathUtils.random(0.3f, 0.7f);
            p.size = MathUtils.random(5f, 16f);
            p.endSize = 0f;
            p.gravity = -300f;
            p.drag = 0.94f;

            Color chosenColor = palette[MathUtils.random(palette.length - 1)];
            p.color.set(chosenColor);
            p.endColor.set(chosenColor.r, chosenColor.g * 0.3f, 0f, 0f);
            p.active = true;
        }
    }

    /**
     * DUST_LAND — Bụi xám bè ra khi đáp đất
     */
    private void spawnDustLand(float x, float y) {
        int count = MathUtils.random(4, 7);
        for (int i = 0; i < count; i++) {
            Particle p = newParticle();
            p.position.set(x + MathUtils.random(-20f, 20f), y);
            p.shape = Particle.Shape.DOT;

            // Bụi lan ngang, ít bay lên
            float dir = (MathUtils.random() > 0.5f) ? 1f : -1f;
            float speed = MathUtils.random(40f, 130f);
            p.velocity.set(dir * speed, MathUtils.random(20f, 80f));

            p.life = p.maxLife = MathUtils.random(0.25f, 0.50f);
            p.size = MathUtils.random(6f, 14f);
            p.endSize = p.size * 1.5f; // Bụi phình to rồi mờ
            p.drag = 0.85f;

            float gray = MathUtils.random(0.55f, 0.75f);
            p.color.set(gray, gray, gray, 0.7f);
            p.endColor.set(gray, gray, gray, 0f);
            p.active = true;
        }
    }

    /**
     * JUMP_PUFF — Khói trắng nhỏ khi bắt đầu nhảy
     */
    private void spawnJumpPuff(float x, float y) {
        int count = MathUtils.random(3, 5);
        for (int i = 0; i < count; i++) {
            Particle p = newParticle();
            p.position.set(x + MathUtils.random(-15f, 15f), y);
            p.shape = Particle.Shape.DOT;

            p.velocity.set(MathUtils.random(-60f, 60f), MathUtils.random(-30f, 50f));
            p.life = p.maxLife = MathUtils.random(0.15f, 0.30f);
            p.size = MathUtils.random(4f, 10f);
            p.endSize = p.size * 2f;
            p.drag = 0.80f;

            p.color.set(0.9f, 0.9f, 0.9f, 0.6f);
            p.endColor.set(0.9f, 0.9f, 0.9f, 0f);
            p.active = true;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Particle newParticle() {
        Particle p = new Particle();
        p.reset();
        particles.add(p);
        return p;
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private Color lerpColor(Color a, Color b, float t) {
        return new Color(
                lerp(a.r, b.r, t),
                lerp(a.g, b.g, t),
                lerp(a.b, b.b, t),
                lerp(a.a, b.a, t));
    }

    public boolean isFinished() {
        return finished;
    }

    public EffectType getType() {
        return type;
    }
}
