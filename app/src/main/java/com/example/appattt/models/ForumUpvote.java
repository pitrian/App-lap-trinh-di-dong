package com.example.appattt.models;

import java.util.Date;

public class ForumUpvote {
    private String id;
    private String userId;
    private String threadId;
    private String postId;
    private Date createdAt;

    public ForumUpvote() {}

    public ForumUpvote(String userId, String threadId, String postId) {
        this.userId = userId;
        this.threadId = threadId;
        this.postId = postId;
        this.createdAt = new Date();
        this.id = userId + "_" + (threadId != null ? threadId : postId);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}