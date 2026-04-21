package com.stickman.fighting.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * SoundManager — Singleton quản lý toàn bộ âm thanh game.
 *
 * Phân biệt 2 loại:
 * - Sound  : SFX ngắn (<2s), load vào RAM, gọi nhiều lần đồng thời được.
 * - Music  : Nhạc nền dài, stream từ file, chỉ 1 track active tại 1 thời điểm.
 *
 * Cách dùng:
 * SoundManager.getInstance().playSound(SoundEffect.PUNCH);
 * SoundManager.getInstance().playMusic(MusicTrack.BATTLE);
 * SoundManager.getInstance().setVolume(0.8f);
 */
public class SoundManager {

    // ── Enum định nghĩa các âm thanh ──────────────────────────────────────────

    public enum SoundEffect {
        PUNCH, KICK, HIT_RECEIVE, JUMP, KO, ROUND_START, BUTTON_CLICK, ENERGY_HIT
    }

    public enum MusicTrack {
        MENU, BATTLE, GAME_OVER
    }

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static SoundManager instance;

    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    // ── Fields ────────────────────────────────────────────────────────────────
    private final Map<SoundEffect, Sound> sounds = new EnumMap<>(SoundEffect.class);
    private final Map<MusicTrack, Music>  music  = new EnumMap<>(MusicTrack.class);

    private Music   currentMusic      = null;
    private MusicTrack currentTrack   = null;

    private float masterVolume  = 1.0f; // Để mặc định là 100%
    private float musicVolume   = 1.0f; // Cho nhạc to lên bằng SFX
    private float sfxVolume     = 0.5f; // Giảm nhẹ SFX để không bị chói tai

    private boolean initialized = false;

    // ── Init ──────────────────────────────────────────────────────────────────

    private SoundManager() {}

    /**
     * Khởi tạo và load tất cả âm thanh.
     * Gọi MỘT LẦN trong MyFightingGame.create() sau khi LibGDX đã sẵn sàng.
     */
    public void initialize() {
        if (initialized) return;

        // Sync volume từ settings
        masterVolume = GameSettings.getInstance().getVolume();

        // 1. LOAD SFX (Hiệu ứng âm thanh ngắn)
        try {
            // Tải các hiệu ứng âm thanh hiện có
            Sound swingSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/swoosh.wav"));
            Sound hitSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/hit.wav"));
            Sound jumpSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/jump.wav"));
            Sound energyHitSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/energy_hit.wav"));

            // Gán PUNCH và KICK vào swingSound
            sounds.put(SoundEffect.PUNCH, swingSound);
            sounds.put(SoundEffect.KICK, swingSound);

            // Gán các hiệu ứng khác
            sounds.put(SoundEffect.HIT_RECEIVE, hitSound);
            sounds.put(SoundEffect.JUMP, jumpSound);
            sounds.put(SoundEffect.ENERGY_HIT, energyHitSound);

        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Lỗi khi load SFX: " + e.getMessage());
        }

        // 2. LOAD MUSIC (Nhạc nền dài)
        try {
            music.put(MusicTrack.MENU, Gdx.audio.newMusic(Gdx.files.internal("audio/music/menu_bgm.wav")));
            music.put(MusicTrack.BATTLE, Gdx.audio.newMusic(Gdx.files.internal("audio/music/battle_bgm.wav")));
            
            // Chỉ nạp nhạc nền Game Over nếu file thực sự tồn tại
            if (Gdx.files.internal("audio/music/game_over_bgm.wav").exists()) {
                music.put(MusicTrack.GAME_OVER, Gdx.audio.newMusic(Gdx.files.internal("audio/music/game_over_bgm.wav")));
            }
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Lỗi khi load Music: " + e.getMessage());
        }

        initialized = true;
        Gdx.app.log("SoundManager", "Initialized — "
            + sounds.size() + " SFX, " + music.size() + " tracks loaded.");
    }

    // ── Playback API ──────────────────────────────────────────────────────────

    public void playSound(SoundEffect effect) {
        if (!initialized) return;
        Sound sound = sounds.get(effect);
        if (sound != null) {
            sound.play(masterVolume * sfxVolume);
        }
    }

    public void playSoundWithVariation(SoundEffect effect, float basePitch) {
        if (!initialized) return;
        Sound sound = sounds.get(effect);
        if (sound != null) {
            long id = sound.play(masterVolume * sfxVolume);
            float pitch = basePitch + (MathUtils.random(-0.1f, 0.1f));
            sound.setPitch(id, pitch);
        }
    }

    /** Phát nhạc nền có tùy chọn Loop */
    public void playMusic(MusicTrack track, boolean looping) {
        if (!initialized) return;
        if (currentTrack == track) {
            // Nếu nhạc đang phát chính là track này thì chỉ cần đảm bảo nó đang chạy
            if (currentMusic != null && !currentMusic.isPlaying()) currentMusic.play();
            return;
        }

        stopMusic();

        Music m = music.get(track);
        if (m != null) {
            currentMusic = m;
            currentTrack = track;

            // Thiết lập âm lượng dựa trên cả Master và Music volume
            currentMusic.setVolume(masterVolume * musicVolume);

            // ÉP BUỘC lặp lại ở đây
            currentMusic.setLooping(looping);

            currentMusic.play();
        }
    }

    /** Overload tiện lợi — loop mặc định true */
    public void playMusic(MusicTrack track) {
        playMusic(track, track != MusicTrack.GAME_OVER);
    }

    /** Dừng nhạc nền hiện tại */
    public void stopMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.stop();
        }
        currentMusic = null;
        currentTrack = null;
    }

    /** Pause/Resume nhạc nền (dùng khi game bị pause) */
    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
        }
    }

    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
        }
    }

    // ── Volume Control ────────────────────────────────────────────────────────

    /**
     * Set master volume (0.0 – 1.0).
     * Áp dụng ngay lập tức cho cả nhạc đang phát.
     */
    public void setVolume(float volume) {
        this.masterVolume = Math.max(0f, Math.min(1f, volume));
        // Cập nhật nhạc đang chạy
        if (currentMusic != null) {
            currentMusic.setVolume(masterVolume * musicVolume);
        }
    }

    public float getVolume()       { return masterVolume; }
    public float getMusicVolume()  { return musicVolume; }
    public float getSfxVolume()    { return sfxVolume; }

    public void setMusicVolume(float v) {
        musicVolume = Math.max(0f, Math.min(1f, v));
        if (currentMusic != null) currentMusic.setVolume(masterVolume * musicVolume);
    }

    public void setSfxVolume(float v) {
        sfxVolume = Math.max(0f, Math.min(1f, v));
    }

    public boolean isMusicPlaying() {
        return currentMusic != null && currentMusic.isPlaying();
    }

    // ── Dispose ───────────────────────────────────────────────────────────────

    public void dispose() {
        stopMusic();
        
        // Sử dụng Set để giải phóng bộ nhớ duy nhất một lần cho mỗi đối tượng (tránh lỗi double-dispose)
        java.util.Set<Sound> uniqueSounds = new java.util.HashSet<>(sounds.values());
        for (Sound s : uniqueSounds) if (s != null) s.dispose();
        
        java.util.Set<Music> uniqueMusic = new java.util.HashSet<>(music.values());
        for (Music m : uniqueMusic) if (m != null) m.dispose();
        
        sounds.clear();
        music.clear();
        initialized = false;
        instance    = null;
        Gdx.app.log("SoundManager", "Disposed.");
    }
}
