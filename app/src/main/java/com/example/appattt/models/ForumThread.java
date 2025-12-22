package com.example.appattt.models;

import com.example.appattt.utils.DateUtils;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class ForumThread {
    private String id;
    private String title;
    private String content;
    private String authorId;
    private String authorName;
    private String categoryId;
    private String categoryName;
    private int upvotes;
    private int views;
    private int replyCount;
    private boolean isSolved;
    private boolean isHot;
    private boolean isPinned;
    private long createdAt;
    private long lastActivity;
    private List<String> tags; // Đổi từ String[] sang List<String> cho Firebase

    // Constructors
    public ForumThread() {
        this.isPinned = false;
        this.createdAt = System.currentTimeMillis();
        this.lastActivity = System.currentTimeMillis();
        this.upvotes = 0;
        this.views = 0;
        this.replyCount = 0;
        this.isSolved = false;
        this.isHot = false;
        this.tags = new ArrayList<>();
    }

    public ForumThread(String title, String content, String authorId,
                       String authorName, String categoryId, String categoryName) {
        this();
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
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

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }

    public boolean isSolved() { return isSolved; }
    public void setSolved(boolean solved) { isSolved = solved; }

    public boolean isHot() { return isHot; }
    public void setHot(boolean hot) { isHot = hot; }

    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastActivity() { return lastActivity; }
    public void setLastActivity(long lastActivity) { this.lastActivity = lastActivity; }

    // Thêm phương thức cho tags
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    // Thêm phương thức để set tags từ mảng String[]
    public void setTagsArray(String[] tagsArray) {
        if (tagsArray != null) {
            this.tags = Arrays.asList(tagsArray);
        } else {
            this.tags = new ArrayList<>();
        }
    }

    public String[] getTagsArray() {
        if (tags != null && !tags.isEmpty()) {
            return tags.toArray(new String[0]);
        }
        return new String[0];
    }

    // Phương thức tiện ích
    public String getTimeAgo() {
        if (createdAt == 0) {
            return "Just now";
        }
        return DateUtils.getTimeAgo(createdAt);
    }

    // Thêm phương thức để tăng views
    public void incrementViews() {
        this.views++;
    }

    // Thêm phương thức để tăng upvotes
    public void incrementUpvotes() {
        this.upvotes++;
    }

    // Thêm phương thức để giảm upvotes
    public void decrementUpvotes() {
        this.upvotes = Math.max(0, this.upvotes - 1);
    }

    // Thêm phương thức để tăng reply count
    public void incrementReplyCount() {
        this.replyCount++;
        this.lastActivity = System.currentTimeMillis(); // Cập nhật lastActivity khi có reply mới
    }
}