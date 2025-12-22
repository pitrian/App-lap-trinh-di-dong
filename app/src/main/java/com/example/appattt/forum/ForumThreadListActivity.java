package com.example.appattt.forum;

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
    private TextView tvEmptyTitle, tvEmptyMessage;

    private String categoryId;
    private String categoryName;
    private String filter;
    private String searchQuery;
    private String title;
    private String userId;

    private ForumFirebaseService forumService;
    private ThreadAdapter threadAdapter;
    private List<ForumThread> threadList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_thread_list);

        forumService = new ForumFirebaseService();

        // Get data from intent
        categoryId = getIntent().getStringExtra("category_id");
        categoryName = getIntent().getStringExtra("category_name");
        filter = getIntent().getStringExtra("filter");
        searchQuery = getIntent().getStringExtra("search_query");
        title = getIntent().getStringExtra("title");
        userId = getIntent().getStringExtra("user_id");

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
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            // Set title based on context
            if (title != null) {
                getSupportActionBar().setTitle(title);
            } else if (categoryName != null) {
                getSupportActionBar().setTitle(categoryName);
            } else if (searchQuery != null) {
                getSupportActionBar().setTitle("Search: " + searchQuery);
            } else {
                getSupportActionBar().setTitle("Threads");
            }
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
                // Do nothing
            }

            @Override
            public void onUpvoteClick(ForumThread thread, int position) {
                toggleThreadUpvote(thread, position);
            }
        });
    }

    private void loadThreads() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        if (searchQuery != null && !searchQuery.isEmpty()) {
            // Handle search
            forumService.searchThreads(searchQuery, new ForumFirebaseService.DataCallback<List<ForumThread>>() {
                @Override
                public void onSuccess(List<ForumThread> result) {
                    progressBar.setVisibility(View.GONE);
                    threadList.clear();
                    threadList.addAll(result);
                    threadAdapter.notifyDataSetChanged();

                    if (threadList.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        tvEmptyTitle.setText("No Results Found");
                        tvEmptyMessage.setText("No threads match your search: " + searchQuery);
                    }
                }

                @Override
                public void onError(String error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ForumThreadListActivity.this,
                            "Search failed: " + error, Toast.LENGTH_SHORT).show();
                    layoutEmpty.setVisibility(View.VISIBLE);
                    tvEmptyTitle.setText("Search Error");
                    tvEmptyMessage.setText("Please try again later.");
                }
            });
        } else if (filter != null) {
            // Handle filters
            switch (filter) {
                case "hot":
                    loadHotThreads();
                    break;
                case "my":
                    if (userId != null) {
                        loadUserThreads(userId);
                    } else {
                        loadSampleThreads();
                    }
                    break;
                case "new":
                    loadRecentThreads(7); // Last 7 days
                    break;
                default:
                    if (categoryId != null) {
                        loadCategoryThreads(categoryId, filter);
                    } else {
                        loadSampleThreads();
                    }
            }
        } else {
            // Default: load threads by category or all threads
            if (categoryId != null) {
                loadCategoryThreads(categoryId, "newest");
            } else {
                loadAllThreads();
            }
        }
    }

    private void loadCategoryThreads(String categoryId, String filterType) {
        forumService.getThreadsByCategory(categoryId, filterType, 0,
                new ForumFirebaseService.DataCallback<List<ForumThread>>() {
                    @Override
                    public void onSuccess(List<ForumThread> result) {
                        progressBar.setVisibility(View.GONE);
                        threadList.clear();
                        threadList.addAll(result);
                        threadAdapter.notifyDataSetChanged();

                        if (threadList.isEmpty()) {
                            layoutEmpty.setVisibility(View.VISIBLE);
                            tvEmptyTitle.setText("No Threads Yet");
                            tvEmptyMessage.setText("Be the first to start a discussion!");
                        }
                    }

                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ForumThreadListActivity.this,
                                "Error loading threads: " + error, Toast.LENGTH_SHORT).show();
                        layoutEmpty.setVisibility(View.VISIBLE);
                        tvEmptyTitle.setText("Error Loading");
                        tvEmptyMessage.setText("Please check your connection.");
                    }
                });
    }

    private void loadHotThreads() {
        forumService.getHotThreads(50, new ForumFirebaseService.DataCallback<List<ForumThread>>() {
            @Override
            public void onSuccess(List<ForumThread> result) {
                progressBar.setVisibility(View.GONE);
                threadList.clear();
                threadList.addAll(result);
                threadAdapter.notifyDataSetChanged();

                if (threadList.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    tvEmptyTitle.setText("No Hot Threads");
                    tvEmptyMessage.setText("There are no trending threads at the moment.");
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                loadSampleThreads();
            }
        });
    }

    private void loadUserThreads(String userId) {
        forumService.getThreadsByUser(userId, "newest",
                new ForumFirebaseService.DataCallback<List<ForumThread>>() {
                    @Override
                    public void onSuccess(List<ForumThread> result) {
                        progressBar.setVisibility(View.GONE);
                        threadList.clear();
                        threadList.addAll(result);
                        threadAdapter.notifyDataSetChanged();

                        if (threadList.isEmpty()) {
                            layoutEmpty.setVisibility(View.VISIBLE);
                            tvEmptyTitle.setText("No Threads");
                            tvEmptyMessage.setText("You haven't created any threads yet.");
                        }
                    }

                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        loadSampleThreads();
                    }
                });
    }

    private void loadRecentThreads(int days) {
        // Tạm thời sử dụng phương thức getThreadsByCategory với categoryId = null
        // Hoặc tạo phương thức getRecentThreads trong ForumFirebaseService
        forumService.getThreadsByCategory(null, "newest", 50,
                new ForumFirebaseService.DataCallback<List<ForumThread>>() {
                    @Override
                    public void onSuccess(List<ForumThread> result) {
                        progressBar.setVisibility(View.GONE);
                        threadList.clear();
                        threadList.addAll(result);
                        threadAdapter.notifyDataSetChanged();

                        if (threadList.isEmpty()) {
                            layoutEmpty.setVisibility(View.VISIBLE);
                            tvEmptyTitle.setText("No Recent Threads");
                            tvEmptyMessage.setText("No threads in the last " + days + " days.");
                        }
                    }

                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        loadSampleThreads();
                    }
                });
    }

    private void loadAllThreads() {
        // Load all threads without category filter
        forumService.getThreadsByCategory(null, "newest", 0,
                new ForumFirebaseService.DataCallback<List<ForumThread>>() {
                    @Override
                    public void onSuccess(List<ForumThread> result) {
                        progressBar.setVisibility(View.GONE);
                        threadList.clear();
                        threadList.addAll(result);
                        threadAdapter.notifyDataSetChanged();

                        if (threadList.isEmpty()) {
                            layoutEmpty.setVisibility(View.VISIBLE);
                            tvEmptyTitle.setText("No Threads");
                            tvEmptyMessage.setText("There are no threads in the forum.");
                        }
                    }

                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        loadSampleThreads();
                    }
                });
    }

    private void loadSampleThreads() {
        progressBar.setVisibility(View.GONE);
        threadList.clear();

        // Add sample threads for testing
        for (int i = 1; i <= 5; i++) {
            ForumThread thread = new ForumThread();
            thread.setId("sample_" + i);
            thread.setTitle("Sample Thread " + i + " - " + (categoryName != null ? categoryName : "Discussion"));
            thread.setContent("This is a sample thread content for testing purposes.");
            thread.setAuthorId("user_" + i);
            thread.setAuthorName("User" + i);
            thread.setCategoryId(categoryId != null ? categoryId : "general");
            thread.setCategoryName(categoryName != null ? categoryName : "General");
            thread.setUpvotes(i * 10);
            thread.setViews(i * 50);
            thread.setReplyCount(i * 3);
            threadList.add(thread);
        }

        threadAdapter.notifyDataSetChanged();
    }

    private void openThreadDetail(ForumThread thread) {
        // TODO: Open thread detail activity
        Toast.makeText(this, "Opening thread: " + thread.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void openUserProfile(String userId) {
        // TODO: Open user profile
        Toast.makeText(this, "Opening user profile: " + userId, Toast.LENGTH_SHORT).show();
    }

    private void toggleThreadUpvote(ForumThread thread, int position) {
        if (!forumService.isUserAuthenticated()) {
            Toast.makeText(this, "Please login to upvote", Toast.LENGTH_SHORT).show();
            return;
        }

        forumService.toggleThreadUpvote(thread.getId(), new ForumFirebaseService.DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean upvoted) {
                // Update UI
                int newUpvotes = thread.getUpvotes() + (upvoted ? 1 : -1);
                thread.setUpvotes(newUpvotes);
                threadAdapter.notifyItemChanged(position);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ForumThreadListActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
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