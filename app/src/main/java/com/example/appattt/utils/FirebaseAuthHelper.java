package com.example.appattt.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.example.appattt.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirebaseAuthHelper {

    private static final String TAG = "FirebaseAuthHelper";
    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private Context context;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private GoogleSignInClient googleSignInClient;

    private AuthCallback authCallback;

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String error);
        void onUserCreated(FirebaseUser user);
    }

    public FirebaseAuthHelper(Context context) {
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    // ==================== CHECK AUTH STATE ====================

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public String getCurrentUserName() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ?
                (user.getDisplayName() != null ? user.getDisplayName() : "Anonymous") :
                "Anonymous";
    }

    public String getCurrentUserEmail() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    // ==================== EMAIL/PASSWORD AUTH ====================

    public void loginWithEmail(String email, String password, AuthCallback callback) {
        this.authCallback = callback;

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Check if user profile exists in Firestore
                            checkOrCreateUserProfile(user, callback);
                        }
                    } else {
                        callback.onError(task.getException() != null ?
                                task.getException().getMessage() : "Login failed");
                    }
                });
    }

    public void registerWithEmail(String email, String password, String username, AuthCallback callback) {
        this.authCallback = callback;

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Update display name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            // Create user profile in Firestore
                                            createUserProfile(user, username, email, callback);
                                        } else {
                                            callback.onError(updateTask.getException() != null ?
                                                    updateTask.getException().getMessage() : "Update profile failed");
                                        }
                                    });
                        }
                    } else {
                        callback.onError(task.getException() != null ?
                                task.getException().getMessage() : "Registration failed");
                    }
                });
    }

    // ==================== GOOGLE AUTH ====================

    public Intent getGoogleSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public void handleGoogleSignInResult(Intent data, AuthCallback callback) {
        this.authCallback = callback;

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account.getIdToken(), callback);
            }
        } catch (ApiException e) {
            callback.onError("Google sign in failed: " + e.getMessage());
        }
    }

    private void firebaseAuthWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener((Activity) context, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Check if user profile exists in Firestore
                            checkOrCreateUserProfile(user, callback);
                        }
                    } else {
                        callback.onError(task.getException() != null ?
                                task.getException().getMessage() : "Google auth failed");
                    }
                });
    }

    // ==================== USER PROFILE MANAGEMENT ====================

    private void checkOrCreateUserProfile(FirebaseUser firebaseUser, AuthCallback callback) {
        String userId = firebaseUser.getUid();

        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        // User profile exists
                        callback.onSuccess(firebaseUser);
                    } else {
                        // Create new user profile
                        String username = firebaseUser.getDisplayName() != null ?
                                firebaseUser.getDisplayName() : "User";
                        String email = firebaseUser.getEmail() != null ?
                                firebaseUser.getEmail() : "";

                        createUserProfile(firebaseUser, username, email, callback);
                    }
                });
    }

    private void createUserProfile(FirebaseUser firebaseUser, String username, String email, AuthCallback callback) {
        String userId = firebaseUser.getUid();

        Map<String, Object> user = new HashMap<>();
        user.put("uid", userId);
        user.put("username", username);
        user.put("email", email);
        user.put("avatar", firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "");
        user.put("createdAt", System.currentTimeMillis());
        user.put("lastLogin", System.currentTimeMillis());
        user.put("xp", 0);
        user.put("level", 1);
        user.put("rank", "Beginner");
        user.put("badges", new ArrayList<String>());
        user.put("bio", "");
        user.put("website", "");
        user.put("github", "");
        user.put("linkedin", "");
        user.put("country", "");
        user.put("isPremium", false);
        user.put("notificationEnabled", true);
        user.put("emailNotifications", true);

        db.collection("users").document(userId)
                .set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onUserCreated(firebaseUser);
                        callback.onSuccess(firebaseUser);
                    } else {
                        callback.onError("Failed to create user profile");
                    }
                });
    }

    public void updateUserProfile(String username, String bio, String website,
                                  String github, String linkedin, String country,
                                  AuthCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }

        // Update Firebase Auth profile
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update Firestore user profile
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("username", username);
                        updates.put("bio", bio);
                        updates.put("website", website);
                        updates.put("github", github);
                        updates.put("linkedin", linkedin);
                        updates.put("country", country);
                        updates.put("updatedAt", System.currentTimeMillis());

                        db.collection("users").document(user.getUid())
                                .update(updates)
                                .addOnCompleteListener(firestoreTask -> {
                                    if (firestoreTask.isSuccessful()) {
                                        callback.onSuccess(user);
                                    } else {
                                        callback.onError(firestoreTask.getException() != null ?
                                                firestoreTask.getException().getMessage() : "Update failed");
                                    }
                                });
                    } else {
                        callback.onError(task.getException() != null ?
                                task.getException().getMessage() : "Update profile failed");
                    }
                });
    }

    public void updateUserAvatar(Uri imageUri, AuthCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }

        String userId = user.getUid();
        StorageReference avatarRef = storage.getReference()
                .child("avatars")
                .child(userId + ".jpg");

        avatarRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Update Firebase Auth photo URL
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Update Firestore
                                        db.collection("users").document(userId)
                                                .update("avatar", uri.toString())
                                                .addOnCompleteListener(firestoreTask -> {
                                                    if (firestoreTask.isSuccessful()) {
                                                        callback.onSuccess(user);
                                                    } else {
                                                        callback.onError("Failed to update avatar in database");
                                                    }
                                                });
                                    } else {
                                        callback.onError("Failed to update avatar");
                                    }
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    callback.onError("Failed to upload image: " + e.getMessage());
                });
    }

    // ==================== PASSWORD MANAGEMENT ====================

    public void changePassword(String currentPassword, String newPassword, AuthCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            callback.onError("User not logged in");
            return;
        }

        // Re-authenticate user first
        AuthCredential credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential)
                .addOnCompleteListener(reauthTask -> {
                    if (reauthTask.isSuccessful()) {
                        user.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        callback.onSuccess(user);
                                    } else {
                                        callback.onError(updateTask.getException() != null ?
                                                updateTask.getException().getMessage() : "Password update failed");
                                    }
                                });
                    } else {
                        callback.onError("Current password is incorrect");
                    }
                });
    }

    public void resetPassword(String email, AuthCallback callback) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(task.getException() != null ?
                                task.getException().getMessage() : "Failed to send reset email");
                    }
                });
    }

    // ==================== ACCOUNT MANAGEMENT ====================

    public void deleteAccount(String password, AuthCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            callback.onError("User not logged in");
            return;
        }

        // Re-authenticate first
        AuthCredential credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(user.getEmail(), password);

        user.reauthenticate(credential)
                .addOnCompleteListener(reauthTask -> {
                    if (reauthTask.isSuccessful()) {
                        // Delete user data from Firestore first
                        String userId = user.getUid();
                        db.collection("users").document(userId).delete()
                                .addOnCompleteListener(deleteTask -> {
                                    if (deleteTask.isSuccessful()) {
                                        // Delete user from Firebase Auth
                                        user.delete()
                                                .addOnCompleteListener(authDeleteTask -> {
                                                    if (authDeleteTask.isSuccessful()) {
                                                        callback.onSuccess(null);
                                                    } else {
                                                        callback.onError("Failed to delete account: " +
                                                                authDeleteTask.getException().getMessage());
                                                    }
                                                });
                                    } else {
                                        callback.onError("Failed to delete user data");
                                    }
                                });
                    } else {
                        callback.onError("Incorrect password");
                    }
                });
    }

    public void logout() {
        auth.signOut();
        googleSignInClient.signOut();
    }

    // ==================== USER STATS & XP ====================

    public void addXP(int xpToAdd, AuthCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }

        String userId = user.getUid();

        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        int currentXP = task.getResult().getLong("xp").intValue();
                        int currentLevel = task.getResult().getLong("level").intValue();

                        int newXP = currentXP + xpToAdd;
                        int newLevel = calculateLevel(newXP);

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("xp", newXP);
                        updates.put("level", newLevel);
                        updates.put("rank", getRankForLevel(newLevel));

                        db.collection("users").document(userId)
                                .update(updates)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        callback.onSuccess(user);
                                    } else {
                                        callback.onError("Failed to update XP");
                                    }
                                });
                    } else {
                        callback.onError("User profile not found");
                    }
                });
    }

    private int calculateLevel(int xp) {
        // Level formula: 100 XP per level
        return Math.max(1, xp / 100 + 1);
    }

    private String getRankForLevel(int level) {
        if (level < 5) return "Beginner";
        if (level < 10) return "Intermediate";
        if (level < 20) return "Advanced";
        if (level < 30) return "Expert";
        if (level < 50) return "Master";
        return "Legend";
    }

    public void getUserStats(AuthCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }

        db.collection("users").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError("User stats not found");
                    }
                });
    }

    // ==================== NOTIFICATION SETTINGS ====================

    public void updateNotificationSettings(boolean pushEnabled, boolean emailEnabled, AuthCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("notificationEnabled", pushEnabled);
        updates.put("emailNotifications", emailEnabled);

        db.collection("users").document(user.getUid())
                .update(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError("Failed to update notification settings");
                    }
                });
    }
}