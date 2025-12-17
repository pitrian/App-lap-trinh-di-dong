package com.example.appattt.models;

import java.util.Date;

public class ForumPost {
    private String id;
    private String threadId;
    private String content;
    private String authorId;
    private String authorName;
    private String authorAvatar;
    private int upvotes;
    private String parentPostId; // For nested replies
    private int depth; // 0 = top-level comment
    private boolean isAnswer; // Marked as solution
    private Date createdAt;
    private Date updatedAt;

    // Constructors
    public ForumPost() {}

    public ForumPost(String threadId, String content, String authorId, String authorName, String parentPostId) {
        this.threadId = threadId;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
        this.parentPostId = parentPostId;
        this.upvotes = 0;
        this.depth = parentPostId == null ? 0 : 1;
        this.isAnswer = false;
        this.createdAt = new Date();
        this.updatedAt = new Date();
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

    public String getAuthorAvatar() { return authorAvatar; }
    public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }

    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }

    public String getParentPostId() { return parentPostId; }
    public void setParentPostId(String parentPostId) { this.parentPostId = parentPostId; }

    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }

    public boolean isAnswer() { return isAnswer; }
    public void setAnswer(boolean answer) { isAnswer = answer; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}