package com.example.appattt.models;

import java.util.Date;
import java.util.List;

public class ForumPost {
    private String id;
    private String threadId;
    private String content;
    private String authorId;
    private String authorName;
    private String parentId;
    private int upvotes;
    private boolean isSolution; // Trường isSolution
    private int depth; // Thêm field depth
    private Date createdAt;
    private String categoryId;

    private List<String> upvotedBy;

    public ForumPost() {}

    public ForumPost(String threadId, String content, String authorId, String authorName) {
        this.threadId = threadId;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
        this.upvotes = 0;
        this.isSolution = false;
        this.depth = 0; // Mặc định depth = 0 (comment gốc)
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }

    // Đổi tên phương thức cho phù hợp (isAnswer -> isSolution)
    public boolean isSolution() { return isSolution; }
    public void setSolution(boolean solution) { isSolution = solution; }

    // Thêm phương thức isAnswer() để tương thích với code cũ (nếu cần)
    public boolean isAnswer() { return isSolution; }
    public void setAnswer(boolean answer) { this.isSolution = answer; }

    // Thêm getter/setter cho depth
    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public List<String> getUpvotedBy() { return upvotedBy; }
    public void setUpvotedBy(List<String> upvotedBy) { this.upvotedBy = upvotedBy; }

    public String getWriteupId() {
        return "";
    }
}