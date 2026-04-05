package com.stickman.fighting.utils;

/**
 * Runtime flags for automated UI QA checks.
 *
 * Use system properties (or env vars) to toggle behavior without code changes.
 */
public final class UiQaConfig {

    private UiQaConfig() {}

    public static boolean isEnabled() {
        return getBoolean("ui.qa", "UI_QA", false);
    }

    public static boolean autoOpenSettingsScreen() {
        return getBoolean("ui.qa.openSettings", "UI_QA_OPEN_SETTINGS", isEnabled());
    }

    public static boolean runLayoutChecks() {
        return getBoolean("ui.qa.checkLayout", "UI_QA_CHECK_LAYOUT", isEnabled());
    }

    public static boolean captureScreenshot() {
        return getBoolean("ui.qa.captureScreenshot", "UI_QA_CAPTURE_SCREENSHOT", isEnabled());
    }

    public static boolean exitAfterScreenshot() {
        return getBoolean("ui.qa.exitAfterShot", "UI_QA_EXIT_AFTER_SHOT", false);
    }

    public static int screenshotDelayFrames() {
        return getInt("ui.qa.screenshotDelayFrames", "UI_QA_SCREENSHOT_DELAY_FRAMES", 10);
    }

    public static String outputDir() {
        String value = System.getProperty("ui.qa.outputDir");
        if (value == null || value.isBlank()) {
            value = System.getenv("UI_QA_OUTPUT_DIR");
        }
        return (value == null || value.isBlank()) ? "qa-artifacts" : value;
    }

    private static boolean getBoolean(String propertyKey, String envKey, boolean defaultValue) {
        String value = System.getProperty(propertyKey);
        if (value == null || value.isBlank()) {
            value = System.getenv(envKey);
        }
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        String normalized = value.trim().toLowerCase();
        return "1".equals(normalized)
            || "true".equals(normalized)
            || "yes".equals(normalized)
            || "on".equals(normalized);
    }

    private static int getInt(String propertyKey, String envKey, int defaultValue) {
        String value = System.getProperty(propertyKey);
        if (value == null || value.isBlank()) {
            value = System.getenv(envKey);
        }
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }
}
