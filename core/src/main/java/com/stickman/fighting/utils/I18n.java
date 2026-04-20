package com.stickman.fighting.utils;

import java.util.HashMap;
import java.util.Map;

public class I18n {
    private static final Map<String, String> enDict = new HashMap<>();

    static {
        enDict.put("BẮT ĐẦU", "START");
        enDict.put("HƯỚNG DẪN", "GUIDE");
        enDict.put("CÀI ĐẶT", "SETTINGS");
        enDict.put("Âm thanh", "Sound");
        enDict.put("Thời gian", "Time");
        enDict.put("Ngôn ngữ", "Language");
        enDict.put("Thanh máu", "Health Bar");
        enDict.put("giây", "sec");
        enDict.put("KẾT THÚC", "GAME OVER");
        enDict.put("HÒA NHAU!", "DRAW!");
        enDict.put("HÒA NHAU !", "DRAW !");
        enDict.put("NGƯỜI CHƠI THẮNG !", "PLAYER WINS !");
        enDict.put("MÁY THẮNG !", "BOT WINS !");
        enDict.put("NGƯỜI CHƠI ", "PLAYER ");
        enDict.put(" THẮNG!", " WINS!");
        enDict.put("ĐẤU LẠI", "REMATCH");
        enDict.put("VỀ MENU", "MAIN MENU");
        enDict.put("TẠM DỪNG", "PAUSED");
        enDict.put("TIẾP TỤC", "RESUME");
        enDict.put("CHƠI LẠI", "RESTART");
        enDict.put("THOÁT", "QUIT");
        enDict.put("HƯỚNG DẪN CHƠI", "HOW TO PLAY");
        enDict.put("Đọc nhanh điều khiển và mẹo giao tranh trước khi vào trận.", "Quickly read the controls and combat tips before entering the match.");
        enDict.put("MỤC TIÊU TRẬN ĐẤU", "MATCH OBJECTIVE");
        enDict.put("- Đánh bại đối thủ bằng cách đưa máu về 0.\n- Hết giờ: bên nhiều máu hơn sẽ thắng.\n- Nếu bằng máu khi hết giờ: kết quả hòa.", "- Defeat the opponent by reducing their HP to 0.\n- Time runs out: side with more HP wins.\n- If HP is equal when time runs out: Draw.");
        enDict.put("ĐIỀU KHIỂN NGƯỜI CHƠI 2", "PLAYER 2 CONTROLS");
        enDict.put("- Mũi tên trái/phải: Di chuyển.\n- Mũi tên lên: Nhảy.\n- Num4: Đỡ, Num5: Đấm, Num6: Đá.\n- Num4 + Num5: Energy-shot, Num3: Tốc biến.", "- Left/Right Arrows: Move.\n- Up Arrow: Jump.\n- Num4: Block, Num5: Punch, Num6: Kick.\n- Num4 + Num5: Energy-shot, Num3: Dash.");
        enDict.put("ĐIỀU KHIỂN NGƯỜI CHƠI 1", "PLAYER 1 CONTROLS");
        enDict.put("- A/D: Di chuyển trái phải.\n- W: Nhảy.\n- U: Đỡ, I: Đấm, O: Đá.\n- U + I: Energy-shot, L: Tốc biến.", "- A/D: Move Left/Right.\n- W: Jump.\n- U: Block, I: Punch, O: Kick.\n- U + I: Energy-shot, L: Dash.");
        enDict.put("MẸO CHƠI NHANH", "QUICK TIPS");
        enDict.put("- Đỡ đòn giảm sát thương nhận vào.\n- Kick gây 10 damage.\n- Combo 5 hit không bị đỡ sẽ knockdown.\n- Energy-shot: 14 damage, cooldown 0.75s.", "- Blocking reduces incoming damage.\n- Kick deals 10 damage.\n- 5-hit combo unblocked causes knockdown.\n- Energy-shot: 14 damage, cooldown 0.75s.");
        enDict.put("Nhấn ESC hoặc nút < để quay lại", "Press ESC or < button to go back");
        enDict.put("Hòa!", "Draw!");
        enDict.put("Người chơi ", "Player ");
        enDict.put(" Thắng!", " Wins!");
        enDict.put("QUAY LẠI", "BACK");
        // We can add more if needed
    }

    public static String get(String vnText) {
        if ("English".equals(GameSettings.getInstance().getLanguage())) {
            return enDict.getOrDefault(vnText, vnText);
        }
        return vnText;
    }
}
