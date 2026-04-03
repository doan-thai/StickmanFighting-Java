package com.stickman.fighting.particles;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Object Pool cho ParticleEffect.
 *
 * Thay vì tạo new ParticleEffect() mỗi lần đánh → tốn GC,
 * pool tái sử dụng các object đã dùng xong.
 *
 * Max pool size = 16 (nhiều hiệu ứng đồng thời tối đa).
 */
public class ParticlePool {

    private static final int MAX_POOL_SIZE = 16;

    private final Deque<ParticleEffect> freeEffects = new ArrayDeque<>();

    public ParticlePool() {
        // Pre-allocate
        for (int i = 0; i < MAX_POOL_SIZE; i++) {
            freeEffects.push(new ParticleEffect());
        }
    }

    /** Lấy một ParticleEffect từ pool */
    public ParticleEffect obtain() {
        if (freeEffects.isEmpty()) {
            return new ParticleEffect(); // Fallback nếu pool cạn
        }
        return freeEffects.pop();
    }

    /** Trả effect đã dùng xong về pool */
    public void free(ParticleEffect effect) {
        if (freeEffects.size() < MAX_POOL_SIZE) {
            freeEffects.push(effect);
        }
        // Nếu pool đầy → để GC thu dọn (hiếm xảy ra)
    }
}
