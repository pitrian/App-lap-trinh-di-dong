package com.example.appattt.forum;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
    private EditText etTitle, etContent;
    private Spinner spinnerCategory;
    private Button btnSubmit;
    private ProgressBar progressBar;

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
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
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
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CreatePostActivity.this,
                        "Error loading categories: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> createPost());
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

        ForumThread thread = new ForumThread(
                title,
                content,
                forumService.getCurrentUserId(),
                forumService.getCurrentUserName(),
                selectedCategory.getId(),
                selectedCategory.getName()
        );

        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        forumService.createThread(thread, new ForumFirebaseService.EmptyCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);

                Toast.makeText(CreatePostActivity.this,
                        "Post created successfully!", Toast.LENGTH_SHORT).show();

                // Return to forum
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);

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