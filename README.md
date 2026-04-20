# 🥋 Stickman Fighting: AI Battleground

Dự án trò chơi đối kháng 2D sử dụng ngôn ngữ **Java** và framework **libGDX**. Điểm nổi bật của trò chơi là hệ thống điều khiển nhân vật máy (Bot) thông qua **Máy trạng thái hữu hạn (Finite State Machine - FSM)**, mang lại trải nghiệm chiến đấu thông minh và tự nhiên.

---

## 🎮 Tính năng chính

*   **🤖 Hệ thống AI thông minh**: Bot có khả năng phân tích khoảng cách, quản lý tài nguyên và phản xạ dựa trên hành động của người chơi (với độ trễ phản ứng tùy chỉnh theo độ khó).
*   **⚔️ Cơ chế chiến đấu đa dạng**: Bao gồm đấm (Punch), đá (Kick), nhảy và kỹ năng đặc biệt (Energy Skill).
*   **✨ Đồ họa & Hiệu ứng**: Sử dụng hệ thống hạt (Particle System) cho các đòn đánh và âm thanh sống động cho từng tương tác.

---

## 🛠 Yêu cầu hệ thống

Để chạy dự án này, máy tính của bạn cần cài đặt sẵn:

*   **Java Development Kit (JDK)**: Phiên bản 17 trở lên.
*   **Gradle**: (Đã được tích hợp sẵn qua Gradle Wrapper trong dự án).
*   **IDE (Khuyên dùng)**: IntelliJ IDEA Ultimate hoặc VS Code (có cài Extension Pack for Java).

---

## 🚀 Hướng dẫn cài đặt và Chạy code

### 1. Clone dự án

Mở terminal và chạy lệnh sau để tải mã nguồn về máy:

```bash
git clone https://github.com/doan-thai/stickmanfighting-java.git
cd stickmanfighting-java
```

### 2. Cài đặt môi trường

**Với IntelliJ IDEA:**
1. Chọn `File -> Open` và trỏ đến thư mục dự án.
2. Đợi IDE tự động tải các dependencies từ file `build.gradle`.
3. Cấu hình SDK (JDK 17) trong `Project Structure`.

**Với VS Code:**
1. Mở thư mục dự án.
2. VS Code sẽ tự động nhận diện dự án Gradle và build các thành phần cần thiết.

### 3. Chạy trò chơi

Bạn có thể chạy trực tiếp từ terminal bằng lệnh:

**Trên Windows:**
```cmd
gradlew.bat lwjgl3:run
```

**Trên Linux/macOS:**
```bash
./gradlew lwjgl3:run
```
*(Lưu ý: Module `lwjgl3` là launcher dành cho phiên bản máy tính để bàn)*

---

## 📂 Cấu trúc dự án

*   📁 **`core/`**: Chứa mã nguồn chính của trò chơi (Logic AI, Entities, Screens).
*   📁 **`lwjgl3/`**: Chứa mã nguồn launcher cho Desktop (sử dụng thư viện LWJGL3).
*   📁 **`assets/`**: Chứa hình ảnh, âm thanh và font chữ sử dụng trong game.
