package com.example.appattt.forum;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appattt.R;
import com.example.appattt.models.Writeup;
import com.example.appattt.services.ForumFirebaseService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubmitWriteupActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText etTitle, etContent, etRoom;
    private AutoCompleteTextView actvDifficulty;
    private ChipGroup chipGroupSelected;
    private Button btnAddTag, btnSubmit;
    private ProgressBar progressBar;
    private TextView tvWordCount;

    private ForumFirebaseService forumService;
    private List<String> selectedTags = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_writeup);

        forumService = new ForumFirebaseService();

        initViews();
        setupToolbar();
        setupDifficultyDropdown();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        etRoom = findViewById(R.id.etRoom);
        actvDifficulty = findViewById(R.id.actvDifficulty);
        chipGroupSelected = findViewById(R.id.chipGroupSelected);
        btnAddTag = findViewById(R.id.btnAddTag);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
        tvWordCount = findViewById(R.id.tvWordCount);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Submit Write-up");
        }
    }

    private void setupDifficultyDropdown() {
        List<String> difficulties = Arrays.asList(
                "Beginner", "Intermediate", "Advanced", "Expert"
        );

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                difficulties
        );
        actvDifficulty.setAdapter(adapter);
        actvDifficulty.setText("Intermediate", false);
    }

    private void setupListeners() {
        btnAddTag.setOnClickListener(v -> addTag());
        btnSubmit.setOnClickListener(v -> submitWriteup());

        // Word count
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int wordCount = s.toString().trim().split("\\s+").length;
                tvWordCount.setText(wordCount + "/5000");
            }
        });

        // Add predefined tags
        addPredefinedTags();
    }

    private void addPredefinedTags() {
        ChipGroup chipGroupPopular = findViewById(R.id.chipGroupPopular);
        if (chipGroupPopular == null) return;

        List<String> predefinedTags = Arrays.asList(
                "Web Security", "Network Security", "Cryptography",
                "Forensics", "Reverse Engineering", "Binary Exploitation",
                "SQL Injection", "XSS", "CTF", "Walkthrough", "Tutorial"
        );

        for (String tag : predefinedTags) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setChipBackgroundColorResource(R.color.cyber_card_background);
            chip.setTextColor(getResources().getColor(R.color.cyber_text_secondary));
            chip.setChipStrokeColorResource(R.color.cyber_border);
            chip.setChipStrokeWidth(1f);
            chip.setClickable(true);
            chip.setCheckable(false);

            chip.setOnClickListener(v -> {
                String tagText = chip.getText().toString();
                if (!selectedTags.contains(tagText)) {
                    addTagToSelected(tagText);
                }
            });

            chipGroupPopular.addView(chip);
        }
    }

    private void addTag() {
        EditText etTag = findViewById(R.id.etTag);
        String tag = etTag.getText().toString().trim();

        if (TextUtils.isEmpty(tag)) {
            etTag.setError("Tag cannot be empty");
            etTag.requestFocus();
            return;
        }

        if (selectedTags.contains(tag)) {
            etTag.setError("Tag already added");
            etTag.requestFocus();
            return;
        }

        addTagToSelected(tag);
        etTag.setText("");
    }

    private void addTagToSelected(String tag) {
        selectedTags.add(tag);

        Chip chip = new Chip(this);
        chip.setText(tag);
        chip.setChipBackgroundColorResource(R.color.cyber_green_main);
        chip.setTextColor(getResources().getColor(R.color.cyber_text_primary));
        chip.setCloseIconVisible(true);

        chip.setOnCloseIconClickListener(v -> {
            chipGroupSelected.removeView(chip);
            selectedTags.remove(tag);
        });

        chipGroupSelected.addView(chip);
    }

    private void submitWriteup() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String roomName = etRoom.getText().toString().trim();
        String difficulty = actvDifficulty.getText().toString().trim();

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

        if (TextUtils.isEmpty(roomName)) {
            etRoom.setError("Room name is required");
            etRoom.requestFocus();
            return;
        }

        if (!forumService.isUserAuthenticated()) {
            Toast.makeText(this, "Please login to submit a writeup", Toast.LENGTH_SHORT).show();
            return;
        }

        Writeup writeup = new Writeup(
                title,
                content,
                forumService.getCurrentUserId(),
                forumService.getCurrentUserName(),
                "", // roomId - could be empty if not linked to a specific room
                roomName,
                difficulty
        );
        writeup.setTags(selectedTags);

        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        forumService.createWriteup(writeup, new ForumFirebaseService.EmptyCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);

                Toast.makeText(SubmitWriteupActivity.this,
                        "Writeup submitted successfully!",
                        Toast.LENGTH_SHORT).show();

                // Return to writeups list
                Intent intent = new Intent(SubmitWriteupActivity.this, WriteupsListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);

                Toast.makeText(SubmitWriteupActivity.this,
                        "Error submitting writeup: " + error, Toast.LENGTH_SHORT).show();
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