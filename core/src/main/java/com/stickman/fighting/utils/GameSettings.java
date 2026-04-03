package com.stickman.fighting.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Singleton lưu và đọc cài đặt game qua LibGDX Preferences (tương đương SharedPrefs).
 */
public class GameSettings {

    private static GameSettings instance;
    private final Preferences prefs;

    private float volume;
    private int   roundTime;
    private String language;
    private float hpScale;

    private GameSettings() {
        prefs     = Gdx.app.getPreferences(Constants.PREF_NAME);
        load();
    }

    public static GameSettings getInstance() {
        if (instance == null) instance = new GameSettings();
        return instance;
    }

    // ── Load từ disk ──────────────────────────────────────────────────────────
    private void load() {
        volume    = prefs.getFloat(Constants.PREF_VOLUME,     0.8f);
        roundTime = prefs.getInteger(Constants.PREF_ROUND_TIME, Constants.DEFAULT_ROUND_TIME);
        language  = prefs.getString(Constants.PREF_LANGUAGE,  "Vietnamese");
        hpScale   = prefs.getFloat(Constants.PREF_HP_SCALE,   1.0f);
    }

    // ── Save xuống disk ───────────────────────────────────────────────────────
    public void save() {
        prefs.putFloat(Constants.PREF_VOLUME,      volume);
        prefs.putInteger(Constants.PREF_ROUND_TIME, roundTime);
        prefs.putString(Constants.PREF_LANGUAGE,   language);
        prefs.putFloat(Constants.PREF_HP_SCALE,    hpScale);
        prefs.flush(); // Ghi xuống file
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public float getVolume()            { return volume; }
    public void  setVolume(float v)     { this.volume = v; }

    public int   getRoundTime()         { return roundTime; }
    public void  setRoundTime(int t)    { this.roundTime = t; }

    public String getLanguage()         { return language; }
    public void   setLanguage(String l) { this.language = l; }

    public float getHpScale()           { return hpScale; }
    public void  setHpScale(float s)    { this.hpScale = s; }
}
