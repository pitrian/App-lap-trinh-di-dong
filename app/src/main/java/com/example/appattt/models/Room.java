package com.example.appattt.models;

public class Room {

    // ====== Thông tin cơ bản ======
    public String id;          // room id (vd: web-01)
    public String title;       // Web Application Security
    public String description; // mô tả
    public String difficulty;  // Easy | Medium | Hard
    public String category;    // Web | Linux | Windows | Crypto | Forensics

    // ====== Tiến độ học ======
    public int progress;       // %
    public boolean completed;  // đã hoàn thành hay chưa

    // ====== Điểm thưởng ======
    public int xp;             // XP nhận được

    // ==========================
    // CONSTRUCTOR
    // ==========================
    public Room(
            String id,
            String title,
            String description,
            String difficulty,
            String category,
            int progress,
            boolean completed,
            int xp
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.category = category;
        this.progress = progress;
        this.completed = completed;
        this.xp = xp;
    }
}
