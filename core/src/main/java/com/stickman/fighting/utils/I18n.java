package com.stickman.fighting.utils;

import java.util.HashMap;
import java.util.Map;

public class I18n {
    private static final Map<String, String> enDict = new HashMap<>();
    private static final Map<String, String> viKeyDict = new HashMap<>();
    private static final Map<String, String> enKeyDict = new HashMap<>();

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
        enDict.put("Đọc nhanh điều khiển và mẹo giao tranh trước khi vào trận.",
                "Quickly read the controls and combat tips before entering the match.");
        enDict.put("MỤC TIÊU TRẬN ĐẤU", "MATCH OBJECTIVE");
        enDict.put(
                "- Đánh bại đối thủ bằng cách đưa máu về 0.\n- Hết giờ: bên nhiều máu hơn sẽ thắng.\n- Nếu bằng máu khi hết giờ: kết quả hòa.",
                "- Defeat the opponent by reducing their HP to 0.\n- Time runs out: side with more HP wins.\n- If HP is equal when time runs out: Draw.");
        enDict.put("ĐIỀU KHIỂN NGƯỜI CHƠI 2", "PLAYER 2 CONTROLS");
        enDict.put(
                "- Mũi tên trái/phải: Di chuyển.\n- Mũi tên lên: Nhảy.\n- Num4: Đỡ, Num5: Đấm, Num6: Đá.\n- Num1: Vũ khí (khi đầy), Num3: Tốc biến.\n- Num4 + Num5: Tuyệt chiêu (Energy/Ném kiếm).",
                "- Arrows: Move.\n- Up Arrow: Jump.\n- Num4: Block, Num5: Punch, Num6: Kick.\n- Num1: Weapon (if full), Num3: Dash.\n- Num4 + Num5: Ultimate (Energy/Throw).");
        enDict.put("ĐIỀU KHIỂN NGƯỜI CHƠI 1", "PLAYER 1 CONTROLS");
        enDict.put(
                "- A/D: Di chuyển trái phải.\n- W: Nhảy.\n- U: Đỡ, I: Đấm, O: Đá.\n- J: Vũ khí (khi đầy), L: Tốc biến.\n- U + I: Tuyệt chiêu (Energy/Ném kiếm).",
                "- A/D: Move Left/Right.\n- W: Jump.\n- U: Block, I: Punch, O: Kick.\n- J: Weapon (if full), L: Dash.\n- U + I: Ultimate (Energy/Throw).");
        enDict.put("MẸO CHƠI NHANH", "QUICK TIPS");
        enDict.put(
                "- Vũ khí: Sát thương Punch x2.\n- Ném kiếm (Ultimate): Đuổi mục tiêu, gây 50% máu hiện tại (Min 40).\n- Block Ultimate: Vẫn nhận 80% sát thương.\n- Mỗi trận chỉ được lấy vũ khí 1 lần.",
                "- Weapon: Double Punch damage.\n- Throw Sword (Ultimate): Homing, deals 50% current HP (Min 40).\n- Block Ultimate: Still takes 80% damage.\n- Weapon can only be taken once per match.");
        enDict.put("Nhấn ESC hoặc nút < để quay lại", "Press ESC or < button to go back");
        enDict.put("Hòa!", "Draw!");
        enDict.put("Người chơi ", "Player ");
        enDict.put(" Thắng!", " Wins!");
        enDict.put("QUAY LẠI", "BACK");
        enDict.put("Map 1 (Phẳng)", "Map 1 (Flat)");
        enDict.put("Map 2 (4 Bục)", "Map 2 (4 Platforms)");
        enDict.put("Map 3 (7 Bục)", "Map 3 (7 Platforms)");
        enDict.put("CHỌN BẢN ĐỒ", "CHOOSE MAP");
        enDict.put("TRỞ LẠI", "BACK");

        putKey("guide.title", "HƯỚNG DẪN CHƠI", "HOW TO PLAY");
        putKey("guide.subtitle",
                "Đọc nhanh điều khiển và mẹo giao tranh trước khi vào trận.",
                "Quickly read the controls and combat tips before entering the match.");
        putKey("guide.objective.title", "MỤC TIÊU TRẬN ĐẤU", "MATCH OBJECTIVE");
        putKey("guide.objective.content",
                "- Đánh bại đối thủ bằng cách đưa máu về 0.\n"
                        + "- Hết giờ: bên nhiều máu hơn sẽ thắng.\n"
                        + "- Nếu bằng máu khi hết giờ: kết quả hòa.",
                "- Defeat the opponent by reducing their HP to 0.\n"
                        + "- Time runs out: side with more HP wins.\n"
                        + "- If HP is equal when time runs out: Draw.");
        putKey("guide.p1.title", "ĐIỀU KHIỂN NGƯỜI CHƠI 1", "PLAYER 1 CONTROLS");
        putKey("guide.p1.content",
                "- A/D: Di chuyển trái/phải.\n"
                        + "- W: Nhảy.\n"
                        + "- U/I/O: Đỡ/Đấm/Đá.\n"
                        + "- L: Tốc biến, J: Rút vũ khí (đầy năng lượng).\n"
                        + "- U + I: Tuyệt chiêu (Energy-shot hoặc ném kiếm).",
                "- A/D: Move left/right.\n"
                        + "- W: Jump.\n"
                        + "- U/I/O: Block/Punch/Kick.\n"
                        + "- L: Dash, J: Draw weapon (when energy is full).\n"
                        + "- U + I: Ultimate (Energy shot or sword throw).");
        putKey("guide.p2.title", "ĐIỀU KHIỂN NGƯỜI CHƠI 2", "PLAYER 2 CONTROLS");
        putKey("guide.p2.content",
                "- Mũi tên trái/phải: Di chuyển, mũi tên lên: Nhảy.\n"
                        + "- Num4/5/6 (hoặc 4/5/6): Đỡ/Đấm/Đá.\n"
                        + "- Num3 (hoặc 3): Tốc biến.\n"
                        + "- Num1 (hoặc 1): Rút vũ khí (đầy năng lượng).\n"
                        + "- Num4 + Num5: Tuyệt chiêu (Energy-shot hoặc ném kiếm).",
                "- Left/Right arrows: Move, Up arrow: Jump.\n"
                        + "- Num4/5/6 (or 4/5/6): Block/Punch/Kick.\n"
                        + "- Num3 (or 3): Dash.\n"
                        + "- Num1 (or 1): Draw weapon (when energy is full).\n"
                        + "- Num4 + Num5: Ultimate (Energy shot or sword throw).");
        putKey("guide.tips.title", "MẸO CHƠI NHANH", "QUICK TIPS");
        putKey("guide.tips.content",
                "- Đỡ đòn giảm 75% sát thương nhận vào.\n"
                        + "- Kick: 10 damage; Energy-shot/Ném kiếm: 14 damage.\n"
                        + "- Đấm khi cầm vũ khí gây gấp đôi sát thương.\n"
                        + "- Mỗi trận chỉ rút vũ khí được 1 lần.",
                "- Blocking reduces incoming damage by 75%.\n"
                        + "- Kick: 10 damage; Energy shot/Sword throw: 14 damage.\n"
                        + "- Punch deals double damage while holding a weapon.\n"
                        + "- You can draw a weapon only once per match.");
        putKey("guide.backHint", "Nhấn ESC hoặc nút < để quay lại", "Press ESC or < button to go back");
    }

    private static void putKey(String key, String vietnamese, String english) {
        viKeyDict.put(key, vietnamese);
        enKeyDict.put(key, english);
    }

    public static String get(String vnText) {
        if ("English".equals(GameSettings.getInstance().getLanguage())) {
            return enDict.getOrDefault(vnText, vnText);
        }
        return vnText;
    }

    public static String tr(String key) {
        return tr(key, key);
    }

    public static String tr(String key, String vietnameseFallback) {
        if ("English".equals(GameSettings.getInstance().getLanguage())) {
            if (enKeyDict.containsKey(key)) {
                return enKeyDict.get(key);
            }
            return enDict.getOrDefault(vietnameseFallback, vietnameseFallback);
        }

        if (viKeyDict.containsKey(key)) {
            return viKeyDict.get(key);
        }
        return vietnameseFallback;
    }
}
