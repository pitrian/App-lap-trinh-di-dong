package com.example.appattt.forum;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appattt.R;
import com.example.appattt.models.ForumCategory;
import com.example.appattt.models.ForumThread;
import com.example.appattt.services.ForumFirebaseService;

import java.util.ArrayList;
import java.util.List;

public class CreatePostActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText etTitle, etContent, etTagInput;
    private Spinner spinnerCategory;
    private Button btnPublish, btnCancel;
    private ProgressBar progressBar;
    private TextView tvBackLink;

    private ForumFirebaseService forumService;
    private List<ForumCategory> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        forumService = new ForumFirebaseService();

        initViews();
        setupToolbar();
        loadCategories();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        etTagInput = findViewById(R.id.etTagInput);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnPublish = findViewById(R.id.btnPublish);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);
        tvBackLink = findViewById(R.id.tvBackLink);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create New Post");
        }
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);

        forumService.getAllCategories(new ForumFirebaseService.DataCallback<List<ForumCategory>>() {
            @Override
            public void onSuccess(List<ForumCategory> result) {
                progressBar.setVisibility(View.GONE);
                categories.clear();
                categories.addAll(result);

                // Nếu không có dữ liệu từ Firebase, load mẫu
                if (categories.isEmpty()) {
                    loadSampleCategories();
                }

                // Setup spinner
                List<String> categoryNames = new ArrayList<>();
                for (ForumCategory category : categories) {
                    categoryNames.add(category.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        CreatePostActivity.this,
                        android.R.layout.simple_spinner_item,
                        categoryNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);

                // Chọn item đầu tiên
                if (categoryNames.size() > 0) {
                    spinnerCategory.setSelection(0);
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                // Dùng mẫu nếu có lỗi
                loadSampleCategories();

                List<String> categoryNames = new ArrayList<>();
                for (ForumCategory category : categories) {
                    categoryNames.add(category.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        CreatePostActivity.this,
                        android.R.layout.simple_spinner_item,
                        categoryNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);

                Toast.makeText(CreatePostActivity.this,
                        "Using sample categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSampleCategories() {
        categories.clear();
        // Thêm các categories mẫu
        categories.add(new ForumCategory(
                "general",
                "General Discussion",
                "Talk about anything related to cybersecurity",
                "ic_chat_bubble",
                234,
                1542
        ));
        categories.add(new ForumCategory(
                "room-help",
                "Room Help & Tips",
                "Get help with specific rooms and challenges",
                "ic_help_circle",
                512,
                3241
        ));
        categories.add(new ForumCategory(
                "ctf",
                "CTF Discussions",
                "Discuss Capture The Flag competitions",
                "ic_flag",
                189,
                942
        ));
        categories.add(new ForumCategory(
                "writeups",
                "Write-ups & Guides",
                "Detailed walkthroughs and tutorials",
                "ic_document_text",
                421,
                2134
        ));
    }

    private void setupListeners() {
        // Nút Publish
        btnPublish.setOnClickListener(v -> createPost());

        // Nút Cancel
        btnCancel.setOnClickListener(v -> finish());

        // Back link
        tvBackLink.setOnClickListener(v -> finish());
    }

    private void createPost() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        int selectedPosition = spinnerCategory.getSelectedItemPosition();

        // Validation
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(content)) {
            etContent.setError("Content is required");
            etContent.requestFocus();
            return;
        }

        if (selectedPosition < 0 || selectedPosition >= categories.size()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!forumService.isUserAuthenticated()) {
            Toast.makeText(this, "Please login to create a post", Toast.LENGTH_SHORT).show();
            return;
        }

        ForumCategory selectedCategory = categories.get(selectedPosition);

        // Tạo bài đăng mới
        ForumThread thread = new ForumThread();
        thread.setTitle(title);
        thread.setContent(content);
        thread.setAuthorId(forumService.getCurrentUserId());
        thread.setAuthorName(forumService.getCurrentUserName());
        thread.setCategoryId(selectedCategory.getId());
        thread.setCategoryName(selectedCategory.getName());

        // Thêm tags nếu có - sử dụng setTagsArray để tương thích với model mới
        String tagsInput = etTagInput.getText().toString().trim();
        if (!TextUtils.isEmpty(tagsInput)) {
            String[] tagArray = tagsInput.split(",\\s*");
            thread.setTagsArray(tagArray);
        }

        progressBar.setVisibility(View.VISIBLE);
        btnPublish.setEnabled(false);
        btnCancel.setEnabled(false);

        forumService.createThread(thread, new ForumFirebaseService.EmptyCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                btnPublish.setEnabled(true);
                btnCancel.setEnabled(true);

                Toast.makeText(CreatePostActivity.this,
                        "Post created successfully!", Toast.LENGTH_SHORT).show();

                // Điều hướng về trang chủ forum
                Intent intent = new Intent(CreatePostActivity.this, ForumHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                btnPublish.setEnabled(true);
                btnCancel.setEnabled(true);

                Toast.makeText(CreatePostActivity.this,
                        "Error creating post: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}