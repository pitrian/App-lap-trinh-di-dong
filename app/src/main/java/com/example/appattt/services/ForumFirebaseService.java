package com.example.appattt.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import com.example.appattt.models.ForumCategory;
import com.example.appattt.models.ForumThread;
import com.example.appattt.models.ForumPost;
import com.example.appattt.models.ForumUpvote;
import com.example.appattt.models.Writeup;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForumFirebaseService {
    private static final String TAG = "ForumFirebaseService";

    // Collection names
    private static final String COLLECTION_CATEGORIES = "forum_categories";
    private static final String COLLECTION_THREADS = "forum_threads";
    private static final String COLLECTION_POSTS = "forum_posts";
    private static final String COLLECTION_UPVOTES = "forum_upvotes";
    private static final String COLLECTION_WRITEUPS = "writeups";
    private static final String COLLECTION_USERS = "users";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public interface DataCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public interface EmptyCallback {
        void onSuccess();
        void onError(String error);
    }

    public ForumFirebaseService() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // =============== CATEGORY METHODS ===============

    public void getAllCategories(DataCallback<List<ForumCategory>> callback) {
        db.collection(COLLECTION_CATEGORIES)
                .orderBy("order")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ForumCategory> categories = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            ForumCategory category = doc.toObject(ForumCategory.class);
                            category.setId(doc.getId());
                            categories.add(category);
                        }
                        callback.onSuccess(categories);
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    // =============== THREAD METHODS ===============

    public void getThreadsByCategory(String categoryId, String sortBy, int limit,
                                     DataCallback<List<ForumThread>> callback) {
        Query query = db.collection(COLLECTION_THREADS)
                .whereEqualTo("categoryId", categoryId);

        // Apply sorting
        switch (sortBy) {
            case "newest":
                query = query.orderBy("createdAt", Query.Direction.DESCENDING);
                break;
            case "popular":
                query = query.orderBy("upvotes", Query.Direction.DESCENDING);
                break;
            case "most_commented":
                query = query.orderBy("replyCount", Query.Direction.DESCENDING);
                break;
            case "solved":
                query = query.whereEqualTo("isSolved", true)
                        .orderBy("createdAt", Query.Direction.DESCENDING);
                break;
            default:
                query = query.orderBy("createdAt", Query.Direction.DESCENDING);
        }

        // Apply limit
        if (limit > 0) {
            query = query.limit(limit);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<ForumThread> threads = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    ForumThread thread = doc.toObject(ForumThread.class);
                    thread.setId(doc.getId());
                    threads.add(thread);
                }
                callback.onSuccess(threads);
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    public void getThreadById(String threadId, DataCallback<ForumThread> callback) {
        db.collection(COLLECTION_THREADS)
                .document(threadId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            ForumThread thread = doc.toObject(ForumThread.class);
                            thread.setId(doc.getId());
                            callback.onSuccess(thread);
                        } else {
                            callback.onError("Thread not found");
                        }
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    public void createThread(ForumThread thread, EmptyCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }

        // Set author info
        thread.setAuthorId(currentUser.getUid());
        thread.setAuthorName(currentUser.getDisplayName() != null ?
                currentUser.getDisplayName() : "Anonymous");

        // Add to Firestore
        db.collection(COLLECTION_THREADS)
                .add(thread)
                .addOnSuccessListener(documentReference -> {
                    String threadId = documentReference.getId();

                    // Update category count
                    incrementCategoryCount(thread.getCategoryId(), "topicCount", 1);

                    // Add initial view by author
                    incrementThreadView(threadId);

                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void incrementThreadView(String threadId) {
        db.collection(COLLECTION_THREADS)
                .document(threadId)
                .update("views", FieldValue.increment(1))
                .addOnFailureListener(e -> Log.e(TAG, "Error incrementing views", e));
    }

    public void toggleThreadUpvote(String threadId, DataCallback<Boolean> callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }

        String upvoteId = currentUser.getUid() + "_" + threadId;
        DocumentReference upvoteRef = db.collection(COLLECTION_UPVOTES).document(upvoteId);

        // Check if already upvoted
        upvoteRef.get().addOnCompleteListener(checkTask -> {
            if (checkTask.isSuccessful()) {
                boolean hasUpvoted = checkTask.getResult().exists();

                if (hasUpvoted) {
                    // Remove upvote
                    WriteBatch batch = db.batch();
                    batch.delete(upvoteRef);
                    batch.update(db.collection(COLLECTION_THREADS).document(threadId),
                            "upvotes", FieldValue.increment(-1));

                    batch.commit().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess(false); // Upvote removed
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    });
                } else {
                    // Add upvote
                    ForumUpvote upvote = new ForumUpvote(currentUser.getUid(), threadId, null);

                    WriteBatch batch = db.batch();
                    batch.set(upvoteRef, upvote);
                    batch.update(db.collection(COLLECTION_THREADS).document(threadId),
                            "upvotes", FieldValue.increment(1));

                    batch.commit().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess(true); // Upvote added
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    });
                }
            } else {
                callback.onError(checkTask.getException().getMessage());
            }
        });
    }

    public void checkThreadUpvote(String threadId, DataCallback<Boolean> callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onSuccess(false);
            return;
        }

        String upvoteId = currentUser.getUid() + "_" + threadId;
        db.collection(COLLECTION_UPVOTES)
                .document(upvoteId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(task.getResult().exists());
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    public void markThreadAsSolved(String threadId, String solutionPostId, EmptyCallback callback) {
        WriteBatch batch = db.batch();

        // Mark thread as solved
        batch.update(db.collection(COLLECTION_THREADS).document(threadId),
                "isSolved", true);

        // Mark post as answer
        if (solutionPostId != null) {
            batch.update(db.collection(COLLECTION_POSTS).document(solutionPostId),
                    "isAnswer", true);
        }

        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    // =============== POST/COMMENT METHODS ===============

    public void getPostsByThread(String threadId, DataCallback<List<ForumPost>> callback) {
        db.collection(COLLECTION_POSTS)
                .whereEqualTo("threadId", threadId)
                .whereEqualTo("parentPostId", null) // Only top-level comments
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ForumPost> posts = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            ForumPost post = doc.toObject(ForumPost.class);
                            post.setId(doc.getId());
                            posts.add(post);
                        }
                        callback.onSuccess(posts);
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    public void getRepliesByPost(String postId, DataCallback<List<ForumPost>> callback) {
        db.collection(COLLECTION_POSTS)
                .whereEqualTo("parentPostId", postId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ForumPost> replies = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            ForumPost reply = doc.toObject(ForumPost.class);
                            reply.setId(doc.getId());
                            replies.add(reply);
                        }
                        callback.onSuccess(replies);
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    public void createPost(ForumPost post, EmptyCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }

        // Set author info
        post.setAuthorId(currentUser.getUid());
        post.setAuthorName(currentUser.getDisplayName() != null ?
                currentUser.getDisplayName() : "Anonymous");

        // --- Start of Transaction for atomicity ---
        // First, get the categoryId from the thread.
        getThreadCategoryId(post.getThreadId(), new DataCallback<String>() {
            @Override
            public void onSuccess(String categoryId) {
                if (categoryId == null) {
                    // If we can't find the category, we can't update its count.
                    // Decide how to handle this. Maybe just commit the post creation.
                    // For now, we'll proceed without the category update.
                    WriteBatch batch = db.batch();
                    DocumentReference postRef = db.collection(COLLECTION_POSTS).document();
                    batch.set(postRef, post);
                    batch.update(db.collection(COLLECTION_THREADS).document(post.getThreadId()),
                            "replyCount", FieldValue.increment(1));

                    batch.commit().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    });
                    return;
                }

                // Now that we have the categoryId, perform all writes in a single batch
                WriteBatch batch = db.batch();

                // 1. Add the new post
                DocumentReference postRef = db.collection(COLLECTION_POSTS).document();
                batch.set(postRef, post);

                // 2. Increment thread reply count
                batch.update(db.collection(COLLECTION_THREADS).document(post.getThreadId()),
                        "replyCount", FieldValue.increment(1));

                // 3. Increment category post count
                batch.update(db.collection(COLLECTION_CATEGORIES).document(categoryId),
                        "postCount", FieldValue.increment(1)); // Assuming COLLECTION_CATEGORIES

                // Commit all operations together
                batch.commit().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
            }

            @Override
            public void onError(String error) {
                // Failed to get the thread details, so we can't proceed.
                // Pass the error back to the original caller.
                callback.onError("Failed to retrieve thread details: " + error);
            }
        });
    }


    public void togglePostUpvote(String postId, DataCallback<Boolean> callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }

        String upvoteId = currentUser.getUid() + "_" + postId;
        DocumentReference upvoteRef = db.collection(COLLECTION_UPVOTES).document(upvoteId);

        upvoteRef.get().addOnCompleteListener(checkTask -> {
            if (checkTask.isSuccessful()) {
                boolean hasUpvoted = checkTask.getResult().exists();

                if (hasUpvoted) {
                    // Remove upvote
                    WriteBatch batch = db.batch();
                    batch.delete(upvoteRef);
                    batch.update(db.collection(COLLECTION_POSTS).document(postId),
                            "upvotes", FieldValue.increment(-1));

                    batch.commit().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess(false);
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    });
                } else {
                    // Add upvote
                    ForumUpvote upvote = new ForumUpvote(currentUser.getUid(), null, postId);

                    WriteBatch batch = db.batch();
                    batch.set(upvoteRef, upvote);
                    batch.update(db.collection(COLLECTION_POSTS).document(postId),
                            "upvotes", FieldValue.increment(1));

                    batch.commit().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess(true);
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    });
                }
            } else {
                callback.onError(checkTask.getException().getMessage());
            }
        });
    }

    // =============== WRITEUP METHODS ===============

    public void getAllWriteups(String sortBy, int limit, DataCallback<List<Writeup>> callback) {
        Query query = db.collection(COLLECTION_WRITEUPS)
                .whereEqualTo("isVerified", true);

        switch (sortBy) {
            case "recent":
                query = query.orderBy("createdAt", Query.Direction.DESCENDING);
                break;
            case "popular":
                query = query.orderBy("likes", Query.Direction.DESCENDING);
                break;
            case "trending":
                // Trending = high views recently
                query = query.orderBy("views", Query.Direction.DESCENDING);
                break;
            default:
                query = query.orderBy("createdAt", Query.Direction.DESCENDING);
        }

        if (limit > 0) {
            query = query.limit(limit);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Writeup> writeups = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Writeup writeup = doc.toObject(Writeup.class);
                    writeup.setId(doc.getId());
                    writeups.add(writeup);
                }
                callback.onSuccess(writeups);
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    public void getWriteupById(String writeupId, DataCallback<Writeup> callback) {
        db.collection(COLLECTION_WRITEUPS)
                .document(writeupId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            Writeup writeup = doc.toObject(Writeup.class);
                            writeup.setId(doc.getId());

                            // Increment view count
                            incrementWriteupView(writeupId);

                            callback.onSuccess(writeup);
                        } else {
                            callback.onError("Writeup not found");
                        }
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    public void createWriteup(Writeup writeup, EmptyCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }

        writeup.setAuthorId(currentUser.getUid());
        writeup.setAuthorName(currentUser.getDisplayName() != null ?
                currentUser.getDisplayName() : "Anonymous");

        db.collection(COLLECTION_WRITEUPS)
                .add(writeup)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void toggleWriteupLike(String writeupId, DataCallback<Boolean> callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }

        String likeId = currentUser.getUid() + "_" + writeupId;
        DocumentReference likeRef = db.collection("writeup_likes").document(likeId);

        likeRef.get().addOnCompleteListener(checkTask -> {
            if (checkTask.isSuccessful()) {
                boolean hasLiked = checkTask.getResult().exists();

                if (hasLiked) {
                    // Remove like
                    WriteBatch batch = db.batch();
                    batch.delete(likeRef);
                    batch.update(db.collection(COLLECTION_WRITEUPS).document(writeupId),
                            "likes", FieldValue.increment(-1));

                    batch.commit().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess(false);
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    });
                } else {
                    // Add like
                    Map<String, Object> like = new HashMap<>();
                    like.put("userId", currentUser.getUid());
                    like.put("writeupId", writeupId);
                    like.put("createdAt", FieldValue.serverTimestamp());

                    WriteBatch batch = db.batch();
                    batch.set(likeRef, like);
                    batch.update(db.collection(COLLECTION_WRITEUPS).document(writeupId),
                            "likes", FieldValue.increment(1));

                    batch.commit().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess(true);
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    });
                }
            } else {
                callback.onError(checkTask.getException().getMessage());
            }
        });
    }

    // =============== SEARCH METHODS ===============



    public void searchThreads(String query, DataCallback<List<ForumThread>> callback) {
        db.collection(COLLECTION_THREADS)
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ForumThread> threads = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            ForumThread thread = doc.toObject(ForumThread.class);
                            thread.setId(doc.getId());
                            threads.add(thread);
                        }
                        callback.onSuccess(threads);
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    // =============== HELPER METHODS ===============

    private void incrementCategoryCount(String categoryId, String field, int value) {
        db.collection(COLLECTION_CATEGORIES)
                .document(categoryId)
                .update(field, FieldValue.increment(value))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating category count", e));
    }

    private void incrementWriteupView(String writeupId) {
        db.collection(COLLECTION_WRITEUPS)
                .document(writeupId)
                .update("views", FieldValue.increment(1))
                .addOnFailureListener(e -> Log.e(TAG, "Error incrementing writeup views", e));
    }

    private void getThreadCategoryId(String threadId, DataCallback<String> callback) {
        db.collection(COLLECTION_THREADS)
                .document(threadId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String categoryId = task.getResult().getString("categoryId");
                        callback.onSuccess(categoryId);
                    } else {
                        callback.onSuccess(null);
                    }
                });
    }

    public boolean isUserAuthenticated() {
        return auth.getCurrentUser() != null;
    }

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public String getCurrentUserName() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ?
                (user.getDisplayName() != null ? user.getDisplayName() : "Anonymous") :
                "Anonymous";
    }



    // Lấy tổng số threads
    public void getTotalThreadCount(DataCallback<Integer> callback) {
        db.collection(COLLECTION_THREADS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(task.getResult().size());
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    // Lấy số active users (users online trong 24h)
    public void getActiveUsersCount(DataCallback<Integer> callback) {
        // Đây là logic đơn giản, trong thực tế cần track user activity
        db.collection(COLLECTION_USERS)
                .limit(100) // Giới hạn để test
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(task.getResult().size());
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    // Lấy hot threads (most upvoted/commented)
    public void getHotThreads(int limit, DataCallback<List<ForumThread>> callback) {
        db.collection(COLLECTION_THREADS)
                .orderBy("upvotes", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ForumThread> threads = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            ForumThread thread = doc.toObject(ForumThread.class);
                            thread.setId(doc.getId());
                            threads.add(thread);
                        }
                        callback.onSuccess(threads);
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }
}