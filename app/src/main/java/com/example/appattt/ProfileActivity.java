package com.example.appattt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvLevel, tvXP;
    private ProgressBar pbXP;
    private MaterialButton btnEdit, btnBadges, btnRanking, btnSaved;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        initViews();
        setupToolbar();
        loadUserData();
        setupNavigation();
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tvUsername);
        tvLevel = findViewById(R.id.tvLevel);
        tvXP = findViewById(R.id.tvXP);
        pbXP = findViewById(R.id.pbXP);
        btnEdit = findViewById(R.id.btnEditProfile);
        btnBadges = findViewById(R.id.btnBadges);
        btnRanking = findViewById(R.id.btnRanking);
        btnSaved = findViewById(R.id.btnSaved);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        // Truy xuất dữ liệu User
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                tvUsername.setText(doc.getString("username"));
                long xp = doc.getLong("xp") != null ? doc.getLong("xp") : 0;
                long level = doc.getLong("level") != null ? doc.getLong("level") : 1;

                tvLevel.setText("LEVEL " + level);
                long nextLevelXP = level * 1000;
                tvXP.setText(xp + " / " + nextLevelXP + " XP");
                pbXP.setProgress((int) ((xp * 100) / nextLevelXP));
            }
        });
    }

    private void setupNavigation() {
        // 1. Chỉnh sửa hồ sơ (Màn 33) [cite: 212]
        btnEdit.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng: Chỉnh sửa thông tin [Màn 33]", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(this, ProfileEditActivity.class));
        });

        // 2. Bộ sưu tập huy hiệu (Màn 30)
        btnBadges.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng: Kho huy hiệu [Màn 30]", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(this, BadgesGridActivity.class));
        });

        // 3. Bảng xếp hạng (Màn 31)
        btnRanking.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng: Bảng xếp hạng thế giới [Màn 31]", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(this, RankingBoardActivity.class));
        });

        // 4. Phòng đã lưu (Màn 32) [cite: 208]
        btnSaved.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng: Danh sách phòng đã lưu [Màn 32]", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(this, SavedRoomsActivity.class));
        });
    }
}