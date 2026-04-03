package com.stickman.fighting.utils;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * SoundGenerator — Tạo âm thanh PCM (sine/noise) mà không cần file asset.
 *
 * Sinh ra các Sound object của LibGDX từ dữ liệu byte[] thuần Java.
 * Dùng làm placeholder cho đến khi có file âm thanh thật.
 *
 * Kỹ thuật:
 *  - Sine wave   → tiếng bíp / nhạc nền đơn giản
 *  - White noise  → tiếng va chạm, impact
 *  - Envelope     → attack + decay để âm thanh tự nhiên hơn
 */
public final class SoundGenerator {

    private static final int SAMPLE_RATE = 44100;
    private static final int BITS        = 16;
    private static final int CHANNELS    = 1; // Mono

    private SoundGenerator() {}

    // ── Public API ────────────────────────────────────────────────────────────

    /** Tiếng đấm: noise burst ngắn với decay nhanh */
    public static byte[] generatePunch() {
        return generateNoiseEnvelope(0.08f, 0.02f, 0.06f, 0.9f);
    }

    /** Tiếng đá: noise thấp hơn, dài hơn */
    public static byte[] generateKick() {
        return generateNoiseEnvelope(0.12f, 0.01f, 0.11f, 0.7f);
    }

    /** Tiếng nhận đòn: noise + pitch drop */
    public static byte[] generateHitReceive() {
        return generateSweep(800f, 200f, 0.10f, 0.6f);
    }

    /** Tiếng nhảy: sine sweep lên */
    public static byte[] generateJump() {
        return generateSweep(300f, 600f, 0.12f, 0.5f);
    }

    /** Tiếng KO: chord trầm + decay dài */
    public static byte[] generateKO() {
        byte[] base = generateSine(80f,  0.8f, 0.8f);
        byte[] harm = generateSine(120f, 0.8f, 0.5f);
        return mixSamples(base, harm);
    }

    /** Tiếng bắt đầu round: ding! cao */
    public static byte[] generateRoundStart() {
        return generateSine(880f, 0.3f, 0.7f);
    }

    /** Tiếng click nút: tick ngắn */
    public static byte[] generateButtonClick() {
        return generateSine(1200f, 0.04f, 0.4f);
    }

    /**
     * Nhạc nền menu: arpeggio đơn giản (pentatonic loop)
     * Ghép nhiều nốt thành một chuỗi dài
     */
    public static byte[] generateMenuMusic() {
        float[] notes = {261.6f, 293.7f, 329.6f, 392.0f, 440.0f,
            392.0f, 329.6f, 293.7f}; // C D E G A G E D
        return generateArpeggio(notes, 0.18f, 0.45f, 4); // 4 lần lặp
    }

    /**
     * Nhạc nền battle: nhịp điệu nhanh hơn, nốt thấp hơn
     */
    public static byte[] generateBattleMusic() {
        float[] notes = {130.8f, 146.8f, 164.8f, 196.0f,
            164.8f, 146.8f, 130.8f, 110.0f}; // C D E G E D C A
        return generateArpeggio(notes, 0.12f, 0.55f, 6);
    }

    /**
     * Nhạc game over: nốt buồn xuống dần
     */
    public static byte[] generateGameOverMusic() {
        float[] notes = {392.0f, 349.2f, 311.1f, 261.6f}; // G F Eb C
        return generateArpeggio(notes, 0.35f, 0.70f, 1);
    }

    // ── Core DSP ──────────────────────────────────────────────────────────────

    /** Sine wave đơn với envelope */
    public static byte[] generateSine(float freq, float durationSec, float volume) {
        int numSamples = (int)(SAMPLE_RATE * durationSec);
        byte[] data    = new byte[numSamples * 2]; // 16-bit = 2 bytes/sample

        for (int i = 0; i < numSamples; i++) {
            double angle   = 2.0 * Math.PI * freq * i / SAMPLE_RATE;
            double envelope = Math.exp(-3.0 * i / numSamples); // Decay tự nhiên
            short  sample  = (short)(Math.sin(angle) * envelope * volume * Short.MAX_VALUE);
            data[i * 2]     = (byte)(sample & 0xFF);
            data[i * 2 + 1] = (byte)((sample >> 8) & 0xFF);
        }
        return data;
    }

    /** White noise với ADSR envelope */
    private static byte[] generateNoiseEnvelope(
        float durationSec, float attackSec, float decaySec, float volume) {

        int total  = (int)(SAMPLE_RATE * durationSec);
        int attack = (int)(SAMPLE_RATE * attackSec);
        int decay  = (int)(SAMPLE_RATE * decaySec);
        byte[] data = new byte[total * 2];

        for (int i = 0; i < total; i++) {
            double env;
            if (i < attack) {
                env = (double) i / attack;                          // Attack
            } else if (i < attack + decay) {
                env = 1.0 - (double)(i - attack) / decay;          // Decay
            } else {
                env = 0.0;
            }
            short sample    = (short)((Math.random() * 2 - 1) * env * volume * Short.MAX_VALUE);
            data[i * 2]     = (byte)(sample & 0xFF);
            data[i * 2 + 1] = (byte)((sample >> 8) & 0xFF);
        }
        return data;
    }

    /** Frequency sweep (pitch thay đổi theo thời gian) */
    private static byte[] generateSweep(
        float startFreq, float endFreq, float durationSec, float volume) {

        int numSamples = (int)(SAMPLE_RATE * durationSec);
        byte[] data    = new byte[numSamples * 2];
        double phase   = 0.0;

        for (int i = 0; i < numSamples; i++) {
            float  t       = (float) i / numSamples;
            float  freq    = startFreq + (endFreq - startFreq) * t;
            double envelope = 1.0 - t; // Linear decay
            phase  += 2.0 * Math.PI * freq / SAMPLE_RATE;
            short  sample  = (short)(Math.sin(phase) * envelope * volume * Short.MAX_VALUE);
            data[i * 2]     = (byte)(sample & 0xFF);
            data[i * 2 + 1] = (byte)((sample >> 8) & 0xFF);
        }
        return data;
    }

    /** Arpeggio: ghép nhiều nốt sine liên tiếp, lặp repeatCount lần */
    private static byte[] generateArpeggio(
        float[] notes, float noteLen, float volume, int repeatCount) {

        int samplesPerNote = (int)(SAMPLE_RATE * noteLen);
        int totalSamples   = samplesPerNote * notes.length * repeatCount;
        byte[] data        = new byte[totalSamples * 2];

        int offset = 0;
        for (int r = 0; r < repeatCount; r++) {
            for (float freq : notes) {
                for (int i = 0; i < samplesPerNote; i++) {
                    double t        = (double) i / samplesPerNote;
                    double envelope = Math.sin(Math.PI * t); // Bell shape
                    double angle    = 2.0 * Math.PI * freq * i / SAMPLE_RATE;
                    short  sample   = (short)(Math.sin(angle) * envelope
                        * volume * Short.MAX_VALUE);
                    int idx         = offset * 2;
                    data[idx]       = (byte)(sample & 0xFF);
                    data[idx + 1]   = (byte)((sample >> 8) & 0xFF);
                    offset++;
                }
            }
        }
        return data;
    }

    /** Mix 2 mảng sample với nhau (lấy trung bình) */
    private static byte[] mixSamples(byte[] a, byte[] b) {
        int len    = Math.max(a.length, b.length);
        byte[] out = new byte[len];

        for (int i = 0; i < len; i += 2) {
            short sA = (i + 1 < a.length)
                ? (short)((a[i] & 0xFF) | (a[i+1] << 8)) : 0;
            short sB = (i + 1 < b.length)
                ? (short)((b[i] & 0xFF) | (b[i+1] << 8)) : 0;
            short mix = (short)((sA + sB) / 2);
            out[i]     = (byte)(mix & 0xFF);
            if (i + 1 < len) out[i + 1] = (byte)((mix >> 8) & 0xFF);
        }
        return out;
    }

    // ── Util: Wrap byte[] thành LibGDX Sound ─────────────────────────────────

    /**
     * Tạo LibGDX Sound từ raw PCM byte[].
     * Dùng AudioDevice để ghi vào buffer nội bộ.
     *
     * LƯU Ý: Trên desktop LWJGL3, cách đáng tin cậy nhất là
     * ghi ra file WAV tạm rồi load lại.
     */
    public static Sound createSound(byte[] pcmData) {
        try {
            // Ghi WAV tạm vào bộ nhớ
            byte[] wavData = wrapPcmInWav(pcmData);
            java.io.File tempFile = java.io.File.createTempFile("sfx_", ".wav");
            tempFile.deleteOnExit();
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                fos.write(wavData);
            }
            FileHandle fh = Gdx.files.absolute(tempFile.getAbsolutePath());
            return Gdx.audio.newSound(fh);
        } catch (Exception e) {
            Gdx.app.error("SoundGenerator", "Failed to create sound: " + e.getMessage());
            return null;
        }
    }

    /** Đóng gói PCM thành WAV header chuẩn (44 bytes header) */
    public static byte[] wrapPcmInWav(byte[] pcm) {
        int dataLen   = pcm.length;
        int totalLen  = dataLen + 44;
        ByteBuffer buf = ByteBuffer.allocate(totalLen)
            .order(ByteOrder.LITTLE_ENDIAN);

        // RIFF header
        buf.put(new byte[]{'R','I','F','F'});
        buf.putInt(totalLen - 8);
        buf.put(new byte[]{'W','A','V','E'});

        // fmt chunk
        buf.put(new byte[]{'f','m','t',' '});
        buf.putInt(16);                   // chunk size
        buf.putShort((short) 1);          // PCM format
        buf.putShort((short) CHANNELS);
        buf.putInt(SAMPLE_RATE);
        buf.putInt(SAMPLE_RATE * CHANNELS * BITS / 8); // byte rate
        buf.putShort((short)(CHANNELS * BITS / 8));    // block align
        buf.putShort((short) BITS);

        // data chunk
        buf.put(new byte[]{'d','a','t','a'});
        buf.putInt(dataLen);
        buf.put(pcm);

        return buf.array();
    }
}
