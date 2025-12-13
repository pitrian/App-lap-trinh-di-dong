package com.example.appattt;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private AppCompatButton resetPasswordBtn;
    private TextView tvGoToLogin;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        initViews();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        resetPasswordBtn = findViewById(R.id.resetPasswordBtn);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        resetPasswordBtn.setOnClickListener(v -> resetPassword());

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void resetPassword() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

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

        // Hiển thị trạng thái loading
        resetPasswordBtn.setEnabled(false);
        resetPasswordBtn.setText("Đang gửi...");

        // Gửi email reset mật khẩu
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    resetPasswordBtn.setEnabled(true);
                    resetPasswordBtn.setText("Send Reset Link");

                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Đã gửi link reset mật khẩu đến email của bạn. Vui lòng kiểm tra hộp thư.",
                                Toast.LENGTH_LONG).show();

                        // Tự động quay về login sau 2 giây
                        new android.os.Handler().postDelayed(() -> {
                            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                            finish();
                        }, 2000);
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Gửi email thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}