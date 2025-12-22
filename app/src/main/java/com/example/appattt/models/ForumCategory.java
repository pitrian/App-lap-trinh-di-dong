package com.example.appattt.models;

public class ForumCategory {
    private String id;
    private String name;
    private String description;
    private String icon;
    private int topicCount;
    private int postCount;
    private int order;
    private boolean isLocked;
    private long lastPostTime;

    // Constructors
    public ForumCategory() {}

    public ForumCategory(String id, String name, String description, String icon,
                         int topicCount, int postCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.topicCount = topicCount;
        this.postCount = postCount;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public int getTopicCount() { return topicCount; }
    public void setTopicCount(int topicCount) { this.topicCount = topicCount; }

    public int getPostCount() { return postCount; }
    public void setPostCount(int postCount) { this.postCount = postCount; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

    public long getLastPostTime() { return lastPostTime; }
    public void setLastPostTime(long lastPostTime) { this.lastPostTime = lastPostTime; }
}