package com.stickman.fighting.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.io.File;
import java.io.FileOutputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * SoundManager — Singleton quản lý toàn bộ âm thanh game.
 *
 * Phân biệt 2 loại:
 *  - Sound  : SFX ngắn (<2s), load vào RAM, gọi nhiều lần đồng thời được.
 *  - Music  : Nhạc nền dài, stream từ file, chỉ 1 track active tại 1 thời điểm.
 *
 * Cách dùng:
 *   SoundManager.getInstance().playSound(SoundEffect.PUNCH);
 *   SoundManager.getInstance().playMusic(MusicTrack.BATTLE);
 *   SoundManager.getInstance().setVolume(0.8f);
 */
public class SoundManager {

    // ── Enum định nghĩa các âm thanh ──────────────────────────────────────────

    public enum SoundEffect {
        PUNCH, KICK, HIT_RECEIVE, JUMP, KO, ROUND_START, BUTTON_CLICK
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

    private float masterVolume  = 0.8f; // 0.0 – 1.0
    private float musicVolume   = 0.5f; // Nhạc nền thấp hơn SFX
    private float sfxVolume     = 1.0f;

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

        // Load SFX
        loadSound(SoundEffect.PUNCH,        SoundGenerator.generatePunch());
        loadSound(SoundEffect.KICK,         SoundGenerator.generateKick());
        loadSound(SoundEffect.HIT_RECEIVE,  SoundGenerator.generateHitReceive());
        loadSound(SoundEffect.JUMP,         SoundGenerator.generateJump());
        loadSound(SoundEffect.KO,           SoundGenerator.generateKO());
        loadSound(SoundEffect.ROUND_START,  SoundGenerator.generateRoundStart());
        loadSound(SoundEffect.BUTTON_CLICK, SoundGenerator.generateButtonClick());

        // Load Music (stream từ WAV tạm)
        loadMusic(MusicTrack.MENU,      SoundGenerator.generateMenuMusic());
        loadMusic(MusicTrack.BATTLE,    SoundGenerator.generateBattleMusic());
        loadMusic(MusicTrack.GAME_OVER, SoundGenerator.generateGameOverMusic());

        initialized = true;
        Gdx.app.log("SoundManager", "Initialized — "
            + sounds.size() + " SFX, " + music.size() + " tracks loaded.");
    }

    // ── Playback API ──────────────────────────────────────────────────────────

    /**
     * Phát SFX. Thread-safe, có thể gọi đồng thời nhiều lần.
     * @param effect Loại SFX
     */
    public void playSound(SoundEffect effect) {
        if (!initialized) return;
        Sound s = sounds.get(effect);
        if (s == null) return;
        float finalVol = masterVolume * sfxVolume;
        s.play(finalVol);
    }

    /**
     * Phát SFX với pitch tùy chỉnh (dùng cho variation — tránh nhàm).
     * @param effect Loại SFX
     * @param pitch  0.5 (chậm/trầm) → 2.0 (nhanh/cao), 1.0 = bình thường
     */
    public void playSoundWithVariation(SoundEffect effect, float pitch) {
        if (!initialized) return;
        Sound s = sounds.get(effect);
        if (s == null) return;
        float finalVol = masterVolume * sfxVolume;
        // Thêm random nhỏ vào pitch để đòn đánh không bị lặp y hệt
        float randomPitch = pitch + (float)(Math.random() * 0.2 - 0.1);
        s.play(finalVol, randomPitch, 0f); // vol, pitch, pan
    }

    /**
     * Phát nhạc nền. Tự dừng track cũ trước khi phát track mới.
     * @param track   Track cần phát
     * @param looping Có loop không (nhạc menu/battle = true, gameover = false)
     */
    public void playMusic(MusicTrack track, boolean looping) {
        if (!initialized) return;
        if (track == currentTrack && currentMusic != null
            && currentMusic.isPlaying()) return; // Đang phát rồi, bỏ qua

        // Dừng track hiện tại (fade out nhanh)
        stopMusic();

        Music m = music.get(track);
        if (m == null) return;

        m.setLooping(looping);
        m.setVolume(masterVolume * musicVolume);
        m.play();

        currentMusic = m;
        currentTrack = track;
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

    // ── Private Loaders ───────────────────────────────────────────────────────

    private void loadSound(SoundEffect effect, byte[] pcmData) {
        try {
            FileHandle fh = writeTempWav(pcmData, effect.name().toLowerCase());
            if (fh != null) {
                Sound s = Gdx.audio.newSound(fh);
                if (s != null) sounds.put(effect, s);
            }
        } catch (Exception e) {
            Gdx.app.error("SoundManager",
                "Failed to load SFX " + effect + ": " + e.getMessage());
        }
    }

    private void loadMusic(MusicTrack track, byte[] pcmData) {
        try {
            FileHandle fh = writeTempWav(pcmData, "music_" + track.name().toLowerCase());
            if (fh != null) {
                Music m = Gdx.audio.newMusic(fh);
                if (m != null) music.put(track, m);
            }
        } catch (Exception e) {
            Gdx.app.error("SoundManager",
                "Failed to load Music " + track + ": " + e.getMessage());
        }
    }

    /**
     * Ghi byte[] PCM thành file WAV tạm trong system temp dir.
     * File tự xóa khi JVM tắt.
     */
    private FileHandle writeTempWav(byte[] pcmData, String name) {
        try {
            File tempFile = File.createTempFile("stickman_" + name + "_", ".wav");
            tempFile.deleteOnExit();
            byte[] wav = SoundGenerator.wrapPcmInWav(pcmData);
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(wav);
            }
            return Gdx.files.absolute(tempFile.getAbsolutePath());
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "writeTempWav failed: " + e.getMessage());
            return null;
        }
    }
}
