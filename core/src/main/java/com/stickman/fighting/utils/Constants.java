package com.stickman.fighting.utils;

public final class Constants {

    private Constants() {
    } // Utility class - no instantiation

    // --- Màn hình ---
    public static final int SCREEN_WIDTH = 1024;
    public static final int SCREEN_HEIGHT = 576;
    public static final String GAME_TITLE = "Stickman Fighting";

    // --- Vật lý ---
    public static final float GRAVITY = -980f; // px/s² (tự code, không Box2D)
    public static final float GROUND_Y = 80f; // Độ cao mặt đất
    public static final float PLAYER_SPEED = 220f;
    public static final float JUMP_VELOCITY = 460f;

    // --- Chiến đấu ---
    public static final float MAX_HP = 100f;
    public static final float HP_SCALE_MIN = 1.0f;
    public static final float HP_SCALE_MAX = 3.0f;
    public static final float ATTACK_RANGE = 80f; // px
    public static final float PUNCH_RANGE = 46f;
    public static final float KICK_RANGE = 62f;
    public static final float ATTACK_COOLDOWN = 0.5f; // fallback
    public static final float PUNCH_DAMAGE = 7f;
    public static final float KICK_DAMAGE = 10f;
    public static final float ENERGY_DAMAGE = 14f;
    public static final float PUNCH_COOLDOWN = 0.22f;
    public static final float KICK_COOLDOWN = 0.34f;
    public static final float ENERGY_COOLDOWN = 0.75f;
    public static final float ENERGY_PROJECTILE_SPEED = 640f;
    public static final float ENERGY_PROJECTILE_RADIUS = 11f;
    public static final float ENERGY_PROJECTILE_LIFETIME = 1.6f;
    public static final float BLOCK_DAMAGE_FACTOR = 0.25f; // giảm 75%
    public static final int KNOCKDOWN_HIT_COUNT = 5;
    public static final float KNOCKDOWN_TIME = 0.65f;
    public static final float DASH_DISTANCE = 110f;
    public static final float DASH_COOLDOWN = 0.7f;

    // --- Timer mặc định ---
    public static final int DEFAULT_ROUND_TIME = 60; // giây

    // --- AI FSM ---
    public static final float AI_CHASE_RANGE = 350f;
    public static final float AI_ATTACK_RANGE = 75f;
    public static final float AI_EVADE_HP = 25f; // Máu < 25 → Evade

    // --- Preferences key ---
    public static final String PREF_NAME = "stickman_prefs";
    public static final String PREF_VOLUME = "volume";
    public static final String PREF_ROUND_TIME = "round_time";
    public static final String PREF_LANGUAGE = "language";
    public static final String PREF_HP_SCALE = "hp_scale";

    // --- Màu sắc chủ đạo (hex) ---
    public static final float[] COLOR_WOOD_DARK = { 0.45f, 0.25f, 0.08f, 1f };
    public static final float[] COLOR_WOOD_MID = { 0.62f, 0.38f, 0.14f, 1f };
    public static final float[] COLOR_PLAYER1 = { 0.20f, 0.60f, 1.00f, 1f }; // Xanh
    public static final float[] COLOR_PLAYER2 = { 1.00f, 0.25f, 0.25f, 1f }; // Đỏ
}
