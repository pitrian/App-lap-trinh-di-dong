package com.example.appattt.services;

import android.util.Log;
import com.example.appattt.models.ForumCategory;
import com.example.appattt.models.ForumThread;
import com.example.appattt.models.ForumPost;
import com.example.appattt.models.Writeup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForumFirebaseService {

    private static final String TAG = "ForumFirebaseService";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // Collection names
    private static final String COLLECTION_CATEGORIES = "forum_categories";
    private static final String COLLECTION_THREADS = "forum_threads";
    private static final String COLLECTION_POSTS = "forum_posts";
    private static final String COLLECTION_WRITEUPS = "writeups";
    private static final String COLLECTION_USERS = "users";

    public interface DataCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public interface EmptyCallback {
        void onSuccess();
        void onError(String error);
    }

    // ========== USER ==========
    public boolean isUserAuthenticated() {
        return auth.getCurrentUser() != null;
    }

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public String getCurrentUserName() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                return displayName;
            }
            String email = user.getEmail();
            if (email != null) {
                return email.split("@")[0];
            }
        }
        return "Anonymous";
    }

    // ========== CATEGORIES ==========
    public void getAllCategories(DataCallback<List<ForumCategory>> callback) {
        db.collection(COLLECTION_CATEGORIES)
                .orderBy("order")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ForumCategory> categories = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            ForumCategory category = document.toObject(ForumCategory.class);
                            if (category != null) {
                                category.setId(document.getId());
                                categories.add(category);
                            }
                        }
                        callback.onSuccess(categories);
                    } else {
                        Log.e(TAG, "Error getting categories: ", task.getException());
                        callback.onError(task.getException() != null ?
                                task.getException().getMessage() : "Unknown error");
                    }
                });
    }

    public void getCategoryById(String categoryId, DataCallback<ForumCategory> callback) {
        db.collection(COLLECTION_CATEGORIES)
                .document(categoryId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        ForumCategory category = task.getResult().toObject(ForumCategory.class);
                        if (category != null) {
                            category.setId(task.getResult().getId());
                        }
                        callback.onSuccess(category);
                    } else {
                        callback.onError("Category not found");
                    }
                });
    }

    // ========== THREADS ==========


    public void getThreadById(String threadId, DataCallback<ForumThread> callback) {
        if (threadId == null || threadId.isEmpty()) {
            callback.onError("Thread ID is empty");
            return;
        }

        db.collection(COLLECTION_THREADS)
                .document(threadId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Dùng factory method mới
                            ForumThread thread = ForumThread.fromDocument(document);
                            callback.onSuccess(thread);
                        } else {
                            callback.onError("Thread not found");
                        }
                    } else {
                        callback.onError(task.getException() != null ?
                                task.getException().getMessage() : "Unknown error");
                    }
                });
    }


    public void getHotThreads(int limit, DataCallback<List<ForumThread>> callback) {
        db.collection(COLLECTION_THREADS)
                .whereEqualTo("hot", true)
                .orderBy("lastActivity", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ForumThread> threads = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            ForumThread thread = document.toObject(ForumThread.class);
                            if (thread != null) {
                                thread.setId(document.getId());
                                threads.add(thread);
                            }
                        }
                        callback.onSuccess(threads);
                    } else {
                        callback.onError(task.getException() != null ?
                                task.getException().getMessage() : "Error loading threads");
                    }
                });
    }


    public void incrementThreadView(String threadId) {
        db.collection(COLLECTION_THREADS)
                .document(threadId)
                .update("views", FieldValue.increment(1))
                .addOnFailureListener(e -> Log.e(TAG, "Error incrementing view: ", e));
    }

    public void toggleThreadUpvote(String threadId, DataCallback<Boolean> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        db.collection(COLLECTION_THREADS)
                .document(threadId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        List<String> upvotedBy = (List<String>) doc.get("upvotedBy");
                        if (upvotedBy == null) upvotedBy = new ArrayList<>();

                        boolean isUpvoted = upvotedBy.contains(userId);
                        if (isUpvoted) {
                            // Remove upvote
                            upvotedBy.remove(userId);
                            db.collection(COLLECTION_THREADS)
                                    .document(threadId)
                                    .update(
                                            "upvotes", FieldValue.increment(-1),
                                            "upvotedBy", upvotedBy
                                    )
                                    .addOnSuccessListener(aVoid -> callback.onSuccess(false))
                                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
                        } else {
                            // Add upvote
                            upvotedBy.add(userId);
                            db.collection(COLLECTION_THREADS)
                                    .document(threadId)
                                    .update(
                                            "upvotes", FieldValue.increment(1),
                                            "upvotedBy", upvotedBy
                                    )
                                    .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
                        }
                    } else {
                        callback.onError("Thread not found");
                    }
                });
    }

    public void checkThreadUpvote(String threadId, DataCallback<Boolean> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onSuccess(false);
            return;
        }

        db.collection(COLLECTION_THREADS)
                .document(threadId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> upvotedBy = (List<String>) task.getResult().get("upvotedBy");
                        callback.onSuccess(upvotedBy != null && upvotedBy.contains(userId));
                    } else {
                        callback.onSuccess(false);
                    }
                });
    }


    public void getPostsByThread(String threadId, DataCallback<List<ForumPost>> callback) {
        db.collection(COLLECTION_POSTS)
                .whereEqualTo("threadId", threadId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ForumPost> posts = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            ForumPost post = document.toObject(ForumPost.class);
                            if (post != null) {
                                post.setId(document.getId());
                                posts.add(post);
                            }
                        }
                        callback.onSuccess(posts);
                    } else {
                        callback.onError("Error loading posts");
                    }
                });
    }

    // ========== WRITEUPS ==========
    // Trong class ForumFirebaseService





    public void toggleWriteupLike(String writeupId, DataCallback<Boolean> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        db.collection(COLLECTION_WRITEUPS)
                .document(writeupId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        List<String> likedBy = (List<String>) doc.get("likedBy");
                        if (likedBy == null) likedBy = new ArrayList<>();

                        boolean isLiked = likedBy.contains(userId);
                        if (isLiked) {
                            // Remove like
                            likedBy.remove(userId);
                            db.collection(COLLECTION_WRITEUPS)
                                    .document(writeupId)
                                    .update(
                                            "likes", FieldValue.increment(-1),
                                            "likedBy", likedBy
                                    )
                                    .addOnSuccessListener(aVoid -> callback.onSuccess(false))
                                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
                        } else {
                            // Add like
                            likedBy.add(userId);
                            db.collection(COLLECTION_WRITEUPS)
                                    .document(writeupId)
                                    .update(
                                            "likes", FieldValue.increment(1),
                                            "likedBy", likedBy
                                    )
                                    .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
                        }
                    } else {
                        callback.onError("Writeup not found");
                    }
                });
    }

    public void searchThreads(String query, DataCallback<List<ForumThread>> callback) {
        db.collection(COLLECTION_THREADS)
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(50)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ForumThread> threads = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            ForumThread thread = document.toObject(ForumThread.class);
                            if (thread != null) {
                                thread.setId(document.getId());
                                threads.add(thread);
                            }
                        }
                        callback.onSuccess(threads);
                    } else {
                        callback.onError("Search failed: " + task.getException().getMessage());
                    }
                });
    }



    // Thêm vào ForumFirebaseService.java

    public void getThreadsByCategory(String categoryId, String filter, int limit, DataCallback<List<ForumThread>> callback) {
        Query query = db.collection(COLLECTION_THREADS);

        // Filter by category
        if (categoryId != null && !categoryId.isEmpty()) {
            query = query.whereEqualTo("categoryId", categoryId);
        }

        // Apply sorting based on filter
        switch (filter.toLowerCase()) {
            case "newest":
                query = query.orderBy("createdAt", Query.Direction.DESCENDING);
                break;
            case "hot":
                query = query.orderBy("lastActivity", Query.Direction.DESCENDING);
                break;
            case "most_viewed":
                query = query.orderBy("views", Query.Direction.DESCENDING);
                break;
            case "most_upvoted":
                query = query.orderBy("upvotes", Query.Direction.DESCENDING);
                break;
            default:
                query = query.orderBy("createdAt", Query.Direction.DESCENDING);
        }

        // Apply limit if specified
        if (limit > 0) {
            query = query.limit(limit);
        }

        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ForumThread> threads = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            ForumThread thread = document.toObject(ForumThread.class);
                            if (thread != null) {
                                thread.setId(document.getId());
                                threads.add(thread);
                            }
                        }
                        callback.onSuccess(threads);
                    } else {
                        callback.onError("Error loading threads");
                    }
                });
    }

    // Giữ phương thức cũ để tương thích
    public void getThreadsByCategory(String categoryId, DataCallback<List<ForumThread>> callback) {
        getThreadsByCategory(categoryId, "newest", 0, callback);
    }

    // Thêm phương thức get user threads
    public void getThreadsByUser(String userId, String filter, DataCallback<List<ForumThread>> callback) {
        Query query = db.collection(COLLECTION_THREADS)
                .whereEqualTo("authorId", userId);

        switch (filter.toLowerCase()) {
            case "newest":
                query = query.orderBy("createdAt", Query.Direction.DESCENDING);
                break;
            case "most_viewed":
                query = query.orderBy("views", Query.Direction.DESCENDING);
                break;
            default:
                query = query.orderBy("createdAt", Query.Direction.DESCENDING);
        }

        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ForumThread> threads = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            ForumThread thread = document.toObject(ForumThread.class);
                            if (thread != null) {
                                thread.setId(document.getId());
                                threads.add(thread);
                            }
                        }
                        callback.onSuccess(threads);
                    } else {
                        callback.onError("Error loading user threads");
                    }
                });
    }

    // Thêm phương thức get recent threads
    public void getRecentThreads(int days, DataCallback<List<ForumThread>> callback) {
        long cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L);
        Date cutoffDate = new Date(cutoffTime);

        db.collection(COLLECTION_THREADS)
                .whereGreaterThan("createdAt", cutoffDate)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ForumThread> threads = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            ForumThread thread = document.toObject(ForumThread.class);
                            if (thread != null) {
                                thread.setId(document.getId());
                                threads.add(thread);
                            }
                        }
                        callback.onSuccess(threads);
                    } else {
                        callback.onError("Error loading recent threads");
                    }
                });
    }

    public void getForumStats(DataCallback<Map<String, Object>> callback) {
        db.collection("forum_stats").document("overall")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        Map<String, Object> stats = task.getResult().getData();
                        callback.onSuccess(stats);
                    } else {
                        // Fallback to sample data
                        Map<String, Object> stats = new HashMap<>();
                        stats.put("totalThreads", 1250);
                        stats.put("activeUsers", 342);
                        stats.put("totalPosts", 8921);
                        callback.onSuccess(stats);
                    }
                });
    }

    // ========== PRIVATE HELPER METHODS ==========
    private void updateCategoryStats(String categoryId, int threadIncrement, int postIncrement) {
        if (categoryId == null || categoryId.isEmpty()) return;

        Map<String, Object> updates = new HashMap<>();
        if (threadIncrement != 0) {
            updates.put("topicCount", FieldValue.increment(threadIncrement));
        }
        if (postIncrement != 0) {
            updates.put("postCount", FieldValue.increment(postIncrement));
        }

        if (!updates.isEmpty()) {
            db.collection(COLLECTION_CATEGORIES)
                    .document(categoryId)
                    .update(updates)
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Error updating category stats: ", e));
        }
    }

    public void getWriteupById(String writeupId, DataCallback<Writeup> callback) {
        if (writeupId == null || writeupId.isEmpty()) {
            callback.onError("Writeup ID is empty");
            return;
        }

        db.collection(COLLECTION_WRITEUPS)
                .document(writeupId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Writeup writeup = document.toObject(Writeup.class);
                            if (writeup != null) {
                                writeup.setId(document.getId());
                                callback.onSuccess(writeup);
                            } else {
                                callback.onError("Failed to parse writeup data");
                            }
                        } else {
                            callback.onError("Writeup not found");
                        }
                    } else {
                        callback.onError(task.getException() != null ? task.getException().getMessage() : "Unknown error");
                    }
                });
    }


    public void getWriteupComments(String writeupId, DataCallback<List<ForumPost>> callback) {
        db.collection(COLLECTION_POSTS)
                .whereEqualTo("writeupId", writeupId)  // Giả sử có trường writeupId trong forum post
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ForumPost> posts = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            ForumPost post = document.toObject(ForumPost.class);
                            if (post != null) {
                                post.setId(document.getId());
                                posts.add(post);
                            }
                        }
                        callback.onSuccess(posts);
                    } else {
                        callback.onError("Error loading writeup comments");
                    }
                });
    }

    // Thêm vào ForumFirebaseService.java

    // ========== POST UPVOTE ==========
    public void togglePostUpvote(String postId, DataCallback<Boolean> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        db.collection(COLLECTION_POSTS)
                .document(postId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        List<String> upvotedBy = (List<String>) doc.get("upvotedBy");
                        if (upvotedBy == null) upvotedBy = new ArrayList<>();

                        boolean isUpvoted = upvotedBy.contains(userId);
                        if (isUpvoted) {
                            // Remove upvote
                            upvotedBy.remove(userId);
                            db.collection(COLLECTION_POSTS)
                                    .document(postId)
                                    .update(
                                            "upvotes", FieldValue.increment(-1),
                                            "upvotedBy", upvotedBy
                                    )
                                    .addOnSuccessListener(aVoid -> callback.onSuccess(false))
                                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
                        } else {
                            // Add upvote
                            upvotedBy.add(userId);
                            db.collection(COLLECTION_POSTS)
                                    .document(postId)
                                    .update(
                                            "upvotes", FieldValue.increment(1),
                                            "upvotedBy", upvotedBy
                                    )
                                    .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
                        }
                    } else {
                        callback.onError("Post not found");
                    }
                });
    }

    // Thêm phương thức check if post is upvoted by current user
    public void checkPostUpvote(String postId, DataCallback<Boolean> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onSuccess(false);
            return;
        }

        db.collection(COLLECTION_POSTS)
                .document(postId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> upvotedBy = (List<String>) task.getResult().get("upvotedBy");
                        callback.onSuccess(upvotedBy != null && upvotedBy.contains(userId));
                    } else {
                        callback.onSuccess(false);
                    }
                });
    }

    public void getLatestThreads(int limit, DataCallback<List<ForumThread>> callback) {
        db.collection("forum_threads")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ForumThread> threads = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ForumThread thread = document.toObject(ForumThread.class);
                            thread.setId(document.getId());
                            threads.add(thread);
                        }
                        callback.onSuccess(threads);
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    public void createThread(ForumThread thread, EmptyCallback callback) {
        Map<String, Object> threadData = new HashMap<>();
        threadData.put("title", thread.getTitle());
        threadData.put("content", thread.getContent());
        threadData.put("authorId", thread.getAuthorId());
        threadData.put("authorName", thread.getAuthorName());
        threadData.put("categoryId", thread.getCategoryId());
        threadData.put("categoryName", thread.getCategoryName());
        threadData.put("upvotes", thread.getUpvotes());
        threadData.put("views", thread.getViews());
        threadData.put("replyCount", thread.getReplyCount());
        threadData.put("solved", thread.isSolved());
        threadData.put("hot", thread.isHot());
        threadData.put("pinned", thread.isPinned());

        // SỬA: Dùng long thay vì Date
        threadData.put("createdAt", System.currentTimeMillis());
        threadData.put("lastActivity", System.currentTimeMillis());

        // Tags
        if (thread.getTags() != null && !thread.getTags().isEmpty()) {
            threadData.put("tags", thread.getTags());
        } else {
            threadData.put("tags", new ArrayList<String>());
        }

        db.collection(COLLECTION_THREADS)
                .add(threadData)
                .addOnSuccessListener(documentReference -> {
                    thread.setId(documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating thread", e);
                    callback.onError(e.getMessage());
                });
    }

    private void updateForumStats(int threadIncrement, int replyIncrement) {
        // Cập nhật thống kê forum
        db.collection("forum_stats").document("overall")
                .update(
                        "totalThreads", FieldValue.increment(threadIncrement),
                        "totalReplies", FieldValue.increment(replyIncrement)
                );
    }

    private void updateThreadReplyCount(String threadId, int increment) {
        if (threadId == null || threadId.isEmpty()) return;

        db.collection(COLLECTION_THREADS)
                .document(threadId)
                .update(
                        "replyCount", FieldValue.increment(increment),
                        "lastActivity", new Date()
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error updating thread reply count: ", e));
    }

    public void getAllWriteups(String filter, int limit, DataCallback<List<Writeup>> callback) {
        Query query = db.collection("writeups");

        // Lọc theo filter
        switch (filter) {
            case "recent":
                query = query.orderBy("createdAt", Query.Direction.DESCENDING);
                break;
            case "popular":
                query = query.orderBy("likes", Query.Direction.DESCENDING);
                break;
            case "trending":
                query = query.orderBy("views", Query.Direction.DESCENDING);
                break;
            case "beginner":
                query = query.whereEqualTo("difficulty", "Beginner")
                        .orderBy("createdAt", Query.Direction.DESCENDING);
                break;
            case "advanced":
                query = query.whereEqualTo("difficulty", "Advanced")
                        .orderBy("createdAt", Query.Direction.DESCENDING);
                break;
            default:
                query = query.orderBy("createdAt", Query.Direction.DESCENDING);
        }

        if (limit > 0) {
            query = query.limit(limit);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Writeup> writeups = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Writeup writeup = doc.toObject(Writeup.class);
                        if (writeup != null) {
                            writeup.setId(doc.getId());
                            writeups.add(writeup);
                        }
                    }
                    callback.onSuccess(writeups);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void createPost(ForumPost post, EmptyCallback callback) {
        Map<String, Object> postData = new HashMap<>();
        postData.put("threadId", post.getThreadId());
        postData.put("writeupId", post.getWriteupId()); // Thêm dòng này
        postData.put("content", post.getContent());
        postData.put("authorId", post.getAuthorId());
        postData.put("authorName", post.getAuthorName());
        postData.put("parentId", post.getParentId());
        postData.put("upvotes", 0);
        postData.put("isSolution", false);
        postData.put("depth", post.getDepth());
        postData.put("createdAt", new Date());

        db.collection(COLLECTION_POSTS)
                .add(postData)
                .addOnSuccessListener(documentReference -> {
                    // Nếu là comment của thread
                    if (post.getThreadId() != null && !post.getThreadId().isEmpty()) {
                        updateThreadReplyCount(post.getThreadId(), 1);
                    }
                    // Nếu là comment của writeup
                    if (post.getWriteupId() != null && !post.getWriteupId().isEmpty()) {
                        db.collection(COLLECTION_WRITEUPS)
                                .document(post.getWriteupId())
                                .update("comments", FieldValue.increment(1))
                                .addOnFailureListener(e -> Log.e(TAG, "Error updating writeup comment count: ", e));
                    }
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating post: ", e);
                    callback.onError(e.getMessage());
                });
    }

    public void createWriteup(Writeup writeup, EmptyCallback callback) {
        Map<String, Object> writeupData = new HashMap<>();
        writeupData.put("title", writeup.getTitle());
        writeupData.put("content", writeup.getContent());
        writeupData.put("authorId", writeup.getAuthorId());
        writeupData.put("authorName", writeup.getAuthorName());
        writeupData.put("roomId", writeup.getRoomId());
        writeupData.put("roomName", writeup.getRoomName());
        writeupData.put("difficulty", writeup.getDifficulty());
        writeupData.put("tags", writeup.getTags());
        writeupData.put("likes", 0);
        writeupData.put("views", 0);
        writeupData.put("comments", 0);
        writeupData.put("shares", 0); // Thêm dòng này
        writeupData.put("verified", false);
        writeupData.put("isFeatured", false);
        writeupData.put("createdAt", new Date());

        db.collection(COLLECTION_WRITEUPS)
                .add(writeupData)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating writeup: ", e);
                    callback.onError(e.getMessage());
                });
    }


}