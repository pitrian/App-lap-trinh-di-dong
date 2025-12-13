package com.example.appattt;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1000;
    private static final String TAG = "LoginActivity";

    private TextInputEditText userEdt, passEdt;
    private AppCompatButton loginBtn;
    private LinearLayout googleLoginBtn;
    private TextView forgetPassTxt, textSignup;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        initGoogleSignIn();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User already logged in. Navigating to DashboardActivity.");
            checkRoleAndGoNext(currentUser);
        }
    }

    private void initViews() {
        userEdt = findViewById(R.id.userEdt);
        passEdt = findViewById(R.id.passEdt);
        forgetPassTxt = findViewById(R.id.forgetPassTxt);
        textSignup = findViewById(R.id.textSignup);
        loginBtn = findViewById(R.id.loginBtn);
        googleLoginBtn = findViewById(R.id.googleLoginBtn);

        loginBtn.setOnClickListener(v -> login());

        forgetPassTxt.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

        textSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        if (googleLoginBtn != null) {
            googleLoginBtn.setOnClickListener(v -> signInWithGoogle());
        }
    }

    private void initGoogleSignIn() {
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            googleSignInClient = GoogleSignIn.getClient(this, gso);
            Log.d(TAG, "Google Sign-In client initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Google Sign-In: " + e.getMessage(), e);
            showToast("Google Sign-In chưa được cấu hình đúng");
        }
    }

    private void login() {
        String loginId = userEdt.getText() != null ? userEdt.getText().toString().trim() : "";
        String password = passEdt.getText() != null ? passEdt.getText().toString().trim() : "";

        if (TextUtils.isEmpty(loginId)) {
            showToast("Vui lòng nhập tên đăng nhập hoặc email");
            userEdt.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showToast("Vui lòng nhập mật khẩu");
            passEdt.requestFocus();
            return;
        }

        setLoading(true);

        if (loginId.contains("@")) {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(loginId).matches()) {
                setLoading(false);
                showToast("Email không hợp lệ");
                userEdt.requestFocus();
                return;
            }
            signInWithEmail(loginId, password);
        } else {
            loginWithUsername(loginId.toLowerCase(), password);
        }
    }

    private void loginWithUsername(String username, String password) {
        Log.d(TAG, "Looking up username: " + username);

        db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        setLoading(false);
                        Log.e(TAG, "Error querying users: ", task.getException());
                        showToast("Lỗi kết nối, vui lòng thử lại");
                        return;
                    }

                    QuerySnapshot snap = task.getResult();
                    if (snap == null || snap.isEmpty()) {
                        setLoading(false);
                        showToast("Không tìm thấy tài khoản với tên đăng nhập này");
                        return;
                    }

                    String email = snap.getDocuments().get(0).getString("email");
                    if (email == null || email.isEmpty()) {
                        setLoading(false);
                        showToast("Tài khoản này không có email hợp lệ");
                        return;
                    }

                    signInWithEmail(email, password);
                });
    }

    private void signInWithEmail(String email, String password) {
        Log.d(TAG, "Signing in with email: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);

                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        Log.e(TAG, "Login failed: ", e);
                        showToast("Sai email/tên đăng nhập hoặc mật khẩu");
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        showToast("Có lỗi xảy ra, vui lòng thử lại");
                        return;
                    }

                    checkRoleAndGoNext(user);
                });
    }

    private void signInWithGoogle() {
        if (googleSignInClient == null) {
            showToast("Google Sign-In chưa được cấu hình");
            return;
        }

        setLoading(true);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account == null) {
                    setLoading(false);
                    showToast("Không lấy được thông tin tài khoản Google");
                    return;
                }
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                setLoading(false);
                Log.e(TAG, "Google sign in failed", e);
                showToast("Đăng nhập Google thất bại");
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "Authenticating with Firebase Google");

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        setLoading(false);
                        Log.e(TAG, "Firebase Google auth failed", task.getException());
                        showToast("Đăng nhập Google thất bại");
                        return;
                    }

                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser == null) {
                        setLoading(false);
                        showToast("Có lỗi xảy ra, vui lòng thử lại");
                        return;
                    }

                    saveUserIfNeedAndGoNext(firebaseUser, account);
                });
    }

    private void saveUserIfNeedAndGoNext(FirebaseUser firebaseUser, GoogleSignInAccount account) {
        String uid = firebaseUser.getUid();
        String email = firebaseUser.getEmail();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        String displayName = account.getDisplayName();
                        if (displayName == null || displayName.isEmpty()) {
                            if (email != null && email.contains("@")) {
                                displayName = email.substring(0, email.indexOf("@"));
                            } else {
                                displayName = "user";
                            }
                        }

                        Map<String, Object> data = new HashMap<>();
                        data.put("uid", uid);
                        data.put("username", displayName.toLowerCase());
                        data.put("email", email);
                        data.put("phone", "");
                        data.put("provider", "google");
                        data.put("role", "user");
                        data.put("createdAt", Timestamp.now());

                        db.collection("users")
                                .document(uid)
                                .set(data)
                                .addOnSuccessListener(aVoid -> {
                                    setLoading(false);
                                    goToDashboardActivity();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error saving user to Firestore", e);
                                    setLoading(false);
                                    goToDashboardActivity();
                                });
                    } else {
                        setLoading(false);
                        goToDashboardActivity();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking user in Firestore", e);
                    setLoading(false);
                    goToDashboardActivity();
                });
    }

    private void checkRoleAndGoNext(FirebaseUser user) {
        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String role = "user";
                    if (doc.exists()) {
                        String r = doc.getString("role");
                        if (r != null && !r.trim().isEmpty()) role = r.trim();
                    }
                    Log.d(TAG, "User role: " + role);
                    goToDashboardActivity();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user role", e);
                    goToDashboardActivity();
                });
    }

    private void goToDashboardActivity() {
        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
        finish();
    }

    private void setLoading(boolean loading) {
        if (loading) {
            loginBtn.setEnabled(false);
            loginBtn.setText("Đang đăng nhập...");
            if (googleLoginBtn != null) {
                googleLoginBtn.setEnabled(false);
                googleLoginBtn.setClickable(false);
            }
        } else {
            loginBtn.setEnabled(true);
            loginBtn.setText("Login");
            if (googleLoginBtn != null) {
                googleLoginBtn.setEnabled(true);
                googleLoginBtn.setClickable(true);
            }
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
