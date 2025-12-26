package com.example.appattt.forum;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appattt.R;
import com.example.appattt.adapters.WriteupAdapter;
import com.example.appattt.models.Writeup;
import com.example.appattt.services.ForumFirebaseService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class WriteupsListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvWriteups;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private TextView tvEmptyTitle, tvEmptyMessage;
    private FloatingActionButton fabAddWriteup;
    private TabLayout tabLayout;

    private TextView tvFeaturedTitle, tvFeaturedAuthor;
    private View cardFeatured;
    private String featuredWriteupId;
    private String currentFilter = "recent";
    private ForumFirebaseService forumService;
    private WriteupAdapter writeupAdapter;
    private List<Writeup> writeupList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writeups_list);

        forumService = new ForumFirebaseService();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadWriteups(currentFilter);
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvWriteups = findViewById(R.id.rvWriteups);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        fabAddWriteup = findViewById(R.id.fabAddWriteup);
        tabLayout = findViewById(R.id.tabLayout);
        cardFeatured = findViewById(R.id.cardFeatured);
        tvFeaturedTitle = findViewById(R.id.tvFeaturedTitle);
        tvFeaturedAuthor = findViewById(R.id.tvFeaturedAuthor);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Write-ups & Guides");
        }
    }

    private void setupRecyclerView() {
        rvWriteups.setLayoutManager(new LinearLayoutManager(this));
        writeupAdapter = new WriteupAdapter(this, writeupList);
        rvWriteups.setAdapter(writeupAdapter);

        writeupAdapter.setOnWriteupClickListener(new WriteupAdapter.OnWriteupClickListener() {
            @Override
            public void onWriteupClick(Writeup writeup) {
                openWriteupDetail(writeup);
            }

            @Override
            public void onAuthorClick(String authorId) {
                openUserProfile(authorId);
            }

            @Override
            public void onLikeClick(Writeup writeup) {
                toggleWriteupLike(writeup);
            }
        });
    }

    private void loadWriteups(String filter) {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        // Ẩn cardFeatured cho đến khi có dữ liệu
        cardFeatured.setVisibility(View.GONE);

        forumService.getAllWriteups(filter, 0, new ForumFirebaseService.DataCallback<List<Writeup>>() {
            @Override
            public void onSuccess(List<Writeup> result) {
                progressBar.setVisibility(View.GONE);
                writeupList.clear();
                writeupList.addAll(result);
                writeupAdapter.notifyDataSetChanged();

                // Update featured writeup
                if (!writeupList.isEmpty()) {
                    // Assuming the first writeup is featured
                    Writeup featured = writeupList.get(0);
                    featuredWriteupId = featured.getId();
                    tvFeaturedTitle.setText(featured.getTitle());
                    // Có thể tính readTime nếu có, tạm thời để 15 min read
                    tvFeaturedAuthor.setText("by " + featured.getAuthorName() + " • 15 min read");
                    cardFeatured.setVisibility(View.VISIBLE);
                } else {
                    featuredWriteupId = null;
                    cardFeatured.setVisibility(View.GONE);
                }

                if (writeupList.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    tvEmptyTitle.setText("No Write-ups Yet");
                    tvEmptyMessage.setText("Be the first to share your knowledge!");
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(WriteupsListActivity.this,
                        "Error loading writeups: " + error, Toast.LENGTH_SHORT).show();
                layoutEmpty.setVisibility(View.VISIBLE);
                tvEmptyTitle.setText("Error Loading");
                tvEmptyMessage.setText("Please check your connection and try again.");
                cardFeatured.setVisibility(View.GONE);
            }
        });
    }


    private void setupListeners() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        currentFilter = "recent";
                        break;
                    case 1:
                        currentFilter = "popular";
                        break;
                    case 2:
                        currentFilter = "trending";
                        break;
                    case 3:
                        currentFilter = "beginner";
                        break;
                    case 4:
                        currentFilter = "advanced";
                        break;
                }
                loadWriteups(currentFilter);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        fabAddWriteup.setOnClickListener(v -> {
            Intent intent = new Intent(WriteupsListActivity.this, SubmitWriteupActivity.class);
            startActivity(intent);
        });

        // Featured writeup click
        cardFeatured.setOnClickListener(v -> {
            if (featuredWriteupId != null) {
                Intent intent = new Intent(WriteupsListActivity.this, WriteupDetailActivity.class);
                intent.putExtra("writeup_id", featuredWriteupId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No featured writeup available", Toast.LENGTH_SHORT).show();
            }
        });

        // Create first button
        findViewById(R.id.btnCreateFirst).setOnClickListener(v -> {
            Intent intent = new Intent(WriteupsListActivity.this, SubmitWriteupActivity.class);
            startActivity(intent);
        });
    }

    private void openWriteupDetail(Writeup writeup) {
        Intent intent = new Intent(this, WriteupDetailActivity.class);
        intent.putExtra("writeup_id", writeup.getId());
        startActivity(intent);
    }

    private void openUserProfile(String userId) {
        // TODO: Open user profile
        Toast.makeText(this, "Opening user profile: " + userId, Toast.LENGTH_SHORT).show();
    }

    private void toggleWriteupLike(Writeup writeup) {
        if (!forumService.isUserAuthenticated()) {
            Toast.makeText(this, "Please login to like", Toast.LENGTH_SHORT).show();
            return;
        }

        forumService.toggleWriteupLike(writeup.getId(), new ForumFirebaseService.DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean liked) {
                // Update UI
                int newLikes = writeup.getLikes() + (liked ? 1 : -1);
                writeup.setLikes(newLikes);
                writeupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(WriteupsListActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show();
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