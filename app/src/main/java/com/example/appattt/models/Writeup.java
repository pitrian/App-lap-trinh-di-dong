package com.example.appattt.models;

import java.util.Date;
import java.util.List;

public class Writeup {
    private String id;
    private String title;
    private String content;
    private String authorId;
    private String authorName;
    private String roomId;
    private String roomName;
    private String difficulty;
    private List<String> tags;
    private int likes;
    private int views;
    private int comments;
    private int shares; // Thêm field shares
    private boolean verified;
    private boolean isFeatured;
    private Date createdAt;

    public Writeup() {
        this.shares = 0; // Khởi tạo mặc định
    }

    public Writeup(String title, String content, String authorId,
                   String authorName, String roomId, String roomName, String difficulty) {
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
        this.roomId = roomId;
        this.roomName = roomName;
        this.difficulty = difficulty;
        this.likes = 0;
        this.views = 0;
        this.comments = 0;
        this.shares = 0; // Khởi tạo
        this.verified = false;
        this.isFeatured = false;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public int getComments() { return comments; }
    public void setComments(int comments) { this.comments = comments; }

    // Thêm getter/setter cho shares
    public int getShares() { return shares; }
    public void setShares(int shares) { this.shares = shares; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}