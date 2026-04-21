package com.stickman.fighting.map;

import com.stickman.fighting.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class MapManager {

    private MapManager() {
        // Utility class
    }

    /**
     * Get predefined platforms for the given map ID.
     * @param mapId integer ID from 1 to 3
     * @return List of Platforms
     */
    public static List<Platform> getPlatforms(int mapId) {
        List<Platform> platforms = new ArrayList<>();
        float pHeight = 16f; // Gọn gàng hơn cho viền đen 2px mỗi bên
        
        // map 1: Flat ground, no platforms. (Default)
        if (mapId == 1) {
            // 0 platforms
        } 
        // map 2: X-Shape / Diamond layout (4 platforms)
        else if (mapId == 2) {
            // Tầng 1
            platforms.add(new Platform(Constants.SCREEN_WIDTH / 2f - 150f, 150f, 300f, pHeight)); // Bục chính giữa
            
            // Tầng 2
            platforms.add(new Platform(50f, 230f, 220f, pHeight)); // Mép trái
            platforms.add(new Platform(Constants.SCREEN_WIDTH - 50f - 220f, 230f, 220f, pHeight)); // Mép phải
            
            // Tầng 3
            platforms.add(new Platform(Constants.SCREEN_WIDTH / 2f - 120f, 310f, 240f, pHeight)); // Đỉnh giữa
        } 
        // map 3: Đấu trường hỗn loạn (7 platforms)
        else if (mapId == 3) {
            // Tầng 1
            platforms.add(new Platform(0f, 150f, 200f, pHeight)); // Rìa sát trái
            platforms.add(new Platform(Constants.SCREEN_WIDTH - 200f, 150f, 200f, pHeight)); // Rìa sát phải
            
            // Tầng 2
            platforms.add(new Platform(Constants.SCREEN_WIDTH / 2f - 180f, 210f, 360f, pHeight)); // Trung tâm lớn
            
            // Tầng 3
            platforms.add(new Platform(150f, 270f, 160f, pHeight)); // Lệch trái
            platforms.add(new Platform(Constants.SCREEN_WIDTH - 150f - 160f, 270f, 160f, pHeight)); // Lệch phải
            
            // Tầng 4
            platforms.add(new Platform(0f, 330f, 130f, pHeight)); // Kịch mép trên bên trái
            platforms.add(new Platform(Constants.SCREEN_WIDTH - 130f, 330f, 130f, pHeight)); // Kịch mép trên bên phải
        }
        
        return platforms;
    }
}
