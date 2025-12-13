package com.example.appattt;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword;
    private TextInputEditText etVerifyCode;
    private AppCompatButton btnCreateAccount, btnVerifyCode;
    private TextView tvGoToLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String sentCode = null;
    private boolean otpSectionVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        etVerifyCode = findViewById(R.id.etVerifyCode);
        btnVerifyCode = findViewById(R.id.btnVerifyCode);

        etVerifyCode.setVisibility(View.GONE);
        btnVerifyCode.setVisibility(View.GONE);

        btnCreateAccount.setOnClickListener(v -> sendOtpCode());
        btnVerifyCode.setOnClickListener(v -> verifyCodeAndRegister());

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void sendOtpCode() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim().toLowerCase() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Vui lòng nhập tên đăng nhập");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Vui lòng nhập lại mật khẩu");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu không khớp");
            etConfirmPassword.requestFocus();
            return;
        }

        int code = (int) (Math.random() * 900000) + 100000;
        sentCode = String.valueOf(code);

        setSendingOtpLoading(true);

        new Thread(() -> {
            try {
                String subject = "Mã xác nhận đăng ký App ATTT";
                String body = "Xin chào " + username +
                        ",\n\nMã xác nhận đăng ký App ATTT của bạn là: " + sentCode +
                        "\n\nVui lòng không chia sẻ mã này cho người khác.";

                MailSender.sendMail(email, subject, body);
            } catch (Exception e) {
                e.printStackTrace();
                String msg = e.getMessage();
                runOnUiThread(() -> {
                    setSendingOtpLoading(false);
                    Toast.makeText(RegisterActivity.this,
                            "Không gửi được email: " + msg,
                            Toast.LENGTH_LONG).show();
                });
                return;
            }

            runOnUiThread(() -> {
                setSendingOtpLoading(false);

                if (!otpSectionVisible) {
                    otpSectionVisible = true;
                    etVerifyCode.setVisibility(View.VISIBLE);
                    btnVerifyCode.setVisibility(View.VISIBLE);
                }

                Toast.makeText(RegisterActivity.this,
                        "Đã gửi mã xác nhận tới email. Vui lòng kiểm tra hộp thư.",
                        Toast.LENGTH_LONG).show();

                btnCreateAccount.setText("Gửi lại mã xác nhận");
                etVerifyCode.requestFocus();
            });
        }).start();
    }

    private void verifyCodeAndRegister() {
        String inputCode = etVerifyCode.getText() != null ? etVerifyCode.getText().toString().trim() : "";

        if (TextUtils.isEmpty(inputCode)) {
            etVerifyCode.setError("Vui lòng nhập mã xác nhận");
            etVerifyCode.requestFocus();
            return;
        }

        if (sentCode == null) {
            Toast.makeText(this,
                    "Bạn chưa yêu cầu gửi mã. Vui lòng bấm Gửi mã xác nhận trước.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!inputCode.equals(sentCode)) {
            etVerifyCode.setError("Mã xác nhận không đúng");
            etVerifyCode.requestFocus();
            return;
        }

        String username = etUsername.getText() != null ? etUsername.getText().toString().trim().toLowerCase() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Tên đăng nhập không hợp lệ", Toast.LENGTH_LONG).show();
            return;
        }

        setRegisterLoading(true);

        db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        setRegisterLoading(false);
                        Toast.makeText(this, "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác", Toast.LENGTH_LONG).show();
                        return;
                    }
                    createFirebaseUser(email, password, username);
                })
                .addOnFailureListener(e -> {
                    setRegisterLoading(false);
                    Toast.makeText(this, "Lỗi kiểm tra username: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }

    private void createFirebaseUser(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        setRegisterLoading(false);

                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(this,
                                    "Email này đã được đăng ký, vui lòng dùng email khác",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this,
                                    "Đăng ký thất bại: " + (e != null ? e.getMessage() : ""),
                                    Toast.LENGTH_LONG).show();
                        }
                        if (e != null) e.printStackTrace();
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        setRegisterLoading(false);
                        Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("uid", user.getUid());
                    data.put("username", username);
                    data.put("email", email);
                    data.put("role", "user");
                    data.put("createdAt", Timestamp.now());

                    db.collection("users")
                            .document(user.getUid())
                            .set(data)
                            .addOnSuccessListener(unused -> {
                                setRegisterLoading(false);
                                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                setRegisterLoading(false);
                                e.printStackTrace();
                                Toast.makeText(this, "Lỗi lưu user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                });
    }

    private void setSendingOtpLoading(boolean loading) {
        if (loading) {
            btnCreateAccount.setEnabled(false);
            btnCreateAccount.setText("ĐANG GỬI MÃ...");
        } else {
            btnCreateAccount.setEnabled(true);
            if (otpSectionVisible) {
                btnCreateAccount.setText("Gửi lại mã xác nhận");
            } else {
                btnCreateAccount.setText("Send Verification Code");
            }
        }
    }

    private void setRegisterLoading(boolean loading) {
        if (loading) {
            btnVerifyCode.setEnabled(false);
            btnVerifyCode.setText("ĐANG XỬ LÝ...");
            btnCreateAccount.setEnabled(false);
        } else {
            btnVerifyCode.setEnabled(true);
            btnVerifyCode.setText("Verify vs Create Account");
            btnCreateAccount.setEnabled(true);
            if (otpSectionVisible) {
                btnCreateAccount.setText("Gửi lại mã xác nhận");
            } else {
                btnCreateAccount.setText("Send Verification Code");
            }
        }
    }
}
