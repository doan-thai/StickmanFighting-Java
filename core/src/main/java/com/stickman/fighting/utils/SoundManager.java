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
            // Tải tiếng vung tay/chân 1 lần, dùng chung cho cả ĐẤM và ĐÁ
            Sound swingSound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/swoosh.wav"));
            sounds.put(SoundEffect.PUNCH, swingSound);
            sounds.put(SoundEffect.KICK, swingSound);

            // Tải các hiệu ứng còn lại
            sounds.put(SoundEffect.HIT_RECEIVE, Gdx.audio.newSound(Gdx.files.internal("audio/sfx/hit.wav")));
            sounds.put(SoundEffect.JUMP, Gdx.audio.newSound(Gdx.files.internal("audio/sfx/jump.wav")));
            sounds.put(SoundEffect.ENERGY_HIT, Gdx.audio.newSound(Gdx.files.internal("audio/sfx/energy_hit.wav")));

        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Lỗi khi load SFX: " + e.getMessage());
        }

        // 2. LOAD MUSIC (Nhạc nền dài) - Chú ý file của bạn là .wav
        try {
            music.put(MusicTrack.MENU, Gdx.audio.newMusic(Gdx.files.internal("audio/music/menu_bgm.wav")));
            music.put(MusicTrack.BATTLE, Gdx.audio.newMusic(Gdx.files.internal("audio/music/battle_bgm.wav")));
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Lỗi khi load Music: " + e.getMessage());
        }

        initialized = true;
        Gdx.app.log("SoundManager", "Initialized — "
            + sounds.size() + " SFX, " + music.size() + " tracks loaded.");
    }

    // ── Playback API ──────────────────────────────────────────────────────────

    public void playSound(SoundEffect effect) {
        Sound sound = sounds.get(effect);
        if (sound != null) {
            sound.play(masterVolume * sfxVolume);
        }
    }

    public void playSoundWithVariation(SoundEffect effect, float basePitch) {
        Sound sound = sounds.get(effect);
        if (sound != null) {
            long id = sound.play(masterVolume * sfxVolume);
            float pitch = basePitch + (MathUtils.random(-0.1f, 0.1f));
            sound.setPitch(id, pitch);
        }
    }

    /** Phát nhạc nền có tùy chọn Loop */
    public void playMusic(MusicTrack track, boolean looping) {
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
        for (Sound s : sounds.values()) if (s != null) s.dispose();
        for (Music m : music.values())  if (m != null) m.dispose();
        sounds.clear();
        music.clear();
        initialized = false;
        instance    = null;
        Gdx.app.log("SoundManager", "Disposed.");
    }
}
