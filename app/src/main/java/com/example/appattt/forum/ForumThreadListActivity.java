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
import com.example.appattt.adapters.ThreadAdapter;
import com.example.appattt.models.ForumThread;
import com.example.appattt.services.ForumFirebaseService;

import java.util.ArrayList;
import java.util.List;

public class ForumThreadListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvThreads;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private TextView tvEmptyMessage;

    private String categoryId;
    private String categoryName;
    private String searchQuery;

    private ForumFirebaseService forumService;
    private ThreadAdapter threadAdapter;
    private List<ForumThread> threadList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_thread_list);

        forumService = new ForumFirebaseService();

        // Get extras
        categoryId = getIntent().getStringExtra("category_id");
        categoryName = getIntent().getStringExtra("category_name");
        searchQuery = getIntent().getStringExtra("search_query");

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadThreads();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvThreads = findViewById(R.id.rvThreads);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(categoryName != null ? categoryName : "Search Results");
        }
    }

    private void setupRecyclerView() {
        rvThreads.setLayoutManager(new LinearLayoutManager(this));
        threadAdapter = new ThreadAdapter(this, threadList);
        rvThreads.setAdapter(threadAdapter);

        threadAdapter.setOnItemClickListener(new ThreadAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ForumThread thread) {
                openThreadDetail(thread);
            }

            @Override
            public void onAuthorClick(String authorId) {
                openUserProfile(authorId);
            }

            @Override
            public void onCategoryClick(String categoryId) {
                // Already in category, do nothing or refresh
            }
        });
    }

    private void loadThreads() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        if (searchQuery != null) {
            // Search threads
            forumService.searchThreads(searchQuery, new ForumFirebaseService.DataCallback<List<ForumThread>>() {
                @Override
                public void onSuccess(List<ForumThread> result) {
                    progressBar.setVisibility(View.GONE);
                    threadList.clear();
                    threadList.addAll(result);
                    threadAdapter.notifyDataSetChanged();

                    if (threadList.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        tvEmptyMessage.setText("No threads found for \"" + searchQuery + "\"");
                    }
                }

                @Override
                public void onError(String error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ForumThreadListActivity.this,
                            "Error searching: " + error, Toast.LENGTH_SHORT).show();
                    layoutEmpty.setVisibility(View.VISIBLE);
                }
            });
        } else if (categoryId != null) {
            // Load threads by category
            forumService.getThreadsByCategory(categoryId, "newest", 0,
                    new ForumFirebaseService.DataCallback<List<ForumThread>>() {
                        @Override
                        public void onSuccess(List<ForumThread> result) {
                            progressBar.setVisibility(View.GONE);
                            threadList.clear();
                            threadList.addAll(result);
                            threadAdapter.notifyDataSetChanged();

                            if (threadList.isEmpty()) {
                                layoutEmpty.setVisibility(View.VISIBLE);
                                tvEmptyMessage.setText("No threads in this category yet");
                            }
                        }

                        @Override
                        public void onError(String error) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ForumThreadListActivity.this,
                                    "Error loading threads: " + error, Toast.LENGTH_SHORT).show();
                            layoutEmpty.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    private void openThreadDetail(ForumThread thread) {
        Intent intent = new Intent(this, ForumThreadDetailActivity.class);
        intent.putExtra("thread_id", thread.getId());
        startActivity(intent);
    }

    private void openUserProfile(String userId) {
        // TODO: Open user profile activity
        Toast.makeText(this, "Opening user profile: " + userId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh threads
        loadThreads();
    }
}