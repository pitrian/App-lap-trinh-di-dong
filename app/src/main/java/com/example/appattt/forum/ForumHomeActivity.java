package com.example.appattt.forum;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appattt.R;
import com.example.appattt.adapters.CategoryAdapter;
import com.example.appattt.adapters.HotThreadAdapter;
import com.example.appattt.models.ForumCategory;
import com.example.appattt.models.ForumThread;
import com.example.appattt.services.ForumFirebaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ForumHomeActivity extends AppCompatActivity {

    private RecyclerView rvCategories, rvHotThreads;
    private EditText etSearch;
    private ProgressBar progressIndicator;
    private ImageView fabNewPost;
    private View cardNewPost, cardMyThreads, tvViewAllHot;
    private TextView tvTotalThreads, tvActiveUsers;

    private ForumFirebaseService forumService;
    private CategoryAdapter categoryAdapter;
    private HotThreadAdapter hotThreadsAdapter;
    private List<ForumCategory> categoryList = new ArrayList<>();
    private List<ForumThread> hotThreadsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_home);

        forumService = new ForumFirebaseService();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadCategories();
        loadHotThreads();
        loadForumStats();
        setupListeners();
    }

    private void initViews() {
        rvCategories = findViewById(R.id.rvCategories);
        rvHotThreads = findViewById(R.id.rvHotThreads);
        etSearch = findViewById(R.id.etSearch);
        progressIndicator = findViewById(R.id.progressIndicator);
        fabNewPost = findViewById(R.id.fabNewPost);
        cardNewPost = findViewById(R.id.cardNewPost);
        cardMyThreads = findViewById(R.id.cardMyThreads);
        tvViewAllHot = findViewById(R.id.tvViewAllHot);
        tvTotalThreads = findViewById(R.id.tvTotalThreads);
        tvActiveUsers = findViewById(R.id.tvActiveUsers);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        // Categories RecyclerView
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(this, categoryList);
        rvCategories.setAdapter(categoryAdapter);

        categoryAdapter.setOnCategoryClickListener(new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(ForumCategory category) {
                openThreadList(category);
            }

            @Override
            public void onCategoryLongClick(ForumCategory category, View view) {
                showCategoryContextMenu(category, view);
            }
        });

        // Hot Threads RecyclerView
        rvHotThreads.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false
        ));
        hotThreadsAdapter = new HotThreadAdapter(this, hotThreadsList);
        rvHotThreads.setAdapter(hotThreadsAdapter);

        hotThreadsAdapter.setOnThreadClickListener(thread -> {
            openThreadDetail(thread);
        });
    }

    private void loadCategories() {
        progressIndicator.setVisibility(View.VISIBLE);

        forumService.getAllCategories(new ForumFirebaseService.DataCallback<List<ForumCategory>>() {
            @Override
            public void onSuccess(List<ForumCategory> result) {
                progressIndicator.setVisibility(View.GONE);
                categoryList.clear();
                categoryList.addAll(result);
                categoryAdapter.notifyDataSetChanged();

                if (categoryList.isEmpty()) {
                    loadSampleCategories();
                }
            }

            @Override
            public void onError(String error) {
                progressIndicator.setVisibility(View.GONE);
                Toast.makeText(ForumHomeActivity.this,
                        "⚠️ Connection issue", Toast.LENGTH_SHORT).show();
                loadSampleCategories();
            }
        });
    }

    private void loadSampleCategories() {
        categoryList.clear();
        categoryList.add(new ForumCategory(
                "general",
                "General Discussion",
                "Talk about anything related to cybersecurity",
                "ic_chat_bubble",
                234,
                1542
        ));
        categoryList.add(new ForumCategory(
                "ctf",
                "CTF Discussions",
                "Discuss Capture The Flag competitions",
                "ic_flag",
                189,
                942
        ));
        categoryList.add(new ForumCategory(
                "writeups",
                "Write-ups & Guides",
                "Detailed walkthroughs and tutorials",
                "ic_document_text",
                421,
                2134
        ));
        categoryList.add(new ForumCategory(
                "help",
                "Room Help & Tips",
                "Get help with specific rooms and challenges",
                "ic_help_circle",
                512,
                3241
        ));
        categoryList.add(new ForumCategory(
                "bugbounty",
                "Bug Bounty",
                "Discuss bug bounty programs and findings",
                "ic_bug",
                156,
                783
        ));
        categoryAdapter.notifyDataSetChanged();
    }

    private void loadHotThreads() {
        progressIndicator.setVisibility(View.VISIBLE);

        forumService.getLatestThreads(5, new ForumFirebaseService.DataCallback<List<ForumThread>>() {
            @Override
            public void onSuccess(List<ForumThread> result) {
                progressIndicator.setVisibility(View.GONE);
                hotThreadsList.clear();
                hotThreadsList.addAll(result);
                hotThreadsAdapter.notifyDataSetChanged();

                if (hotThreadsList.isEmpty()) {
                    loadSampleHotThreads();
                }
            }

            @Override
            public void onError(String error) {
                progressIndicator.setVisibility(View.GONE);
                loadSampleHotThreads();
                Log.e("ForumHomeActivity", "Error loading hot threads: " + error);
            }
        });
    }

    private void loadSampleHotThreads() {
        hotThreadsList.clear();

        // Thread 1
        ForumThread thread1 = new ForumThread();
        thread1.setId("1");
        thread1.setTitle("CTF 2024 Winter Championship - Discussion");
        thread1.setContent("Official discussion thread for CTF 2024 Winter Championship...");
        thread1.setAuthorId("user789");
        thread1.setAuthorName("CTFMaster");
        thread1.setCategoryId("ctf");
        thread1.setCategoryName("CTF Discussions");
        thread1.setUpvotes(87);
        thread1.setViews(2314);
        thread1.setReplyCount(63);
        thread1.setSolved(true);
        thread1.setHot(true);
        thread1.setLastActivity(System.currentTimeMillis() - (3 * 60 * 60 * 1000)); // 3 hours ago
        hotThreadsList.add(thread1);

        // Thread 2
        ForumThread thread2 = new ForumThread();
        thread2.setId("2");
        thread2.setTitle("Complete Guide: Linux Privilege Escalation");
        thread2.setContent("Complete step-by-step guide for Linux privilege escalation...");
        thread2.setAuthorId("user456");
        thread2.setAuthorName("L33tHacker");
        thread2.setCategoryId("writeups");
        thread2.setCategoryName("Write-ups & Guides");
        thread2.setUpvotes(342);
        thread2.setViews(2847);
        thread2.setReplyCount(42);
        thread2.setHot(true);
        thread2.setLastActivity(System.currentTimeMillis() - (2 * 60 * 60 * 1000)); // 2 hours ago
        hotThreadsList.add(thread2);

        // Thread 3
        ForumThread thread3 = new ForumThread();
        thread3.setId("3");
        thread3.setTitle("How to set up a home penetration testing lab?");
        thread3.setContent("Beginner-friendly guide to setting up a home lab...");
        thread3.setAuthorId("user123");
        thread3.setAuthorName("CyberStudent");
        thread3.setCategoryId("general");
        thread3.setCategoryName("General Discussion");
        thread3.setUpvotes(125);
        thread3.setViews(891);
        thread3.setReplyCount(28);
        thread3.setHot(true);
        thread3.setLastActivity(System.currentTimeMillis() - (45 * 60 * 1000)); // 45 mins ago
        hotThreadsList.add(thread3);

        hotThreadsAdapter.notifyDataSetChanged();
    }

    private void loadForumStats() {
        forumService.getForumStats(new ForumFirebaseService.DataCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                Object totalThreads = result.get("totalThreads");
                if (totalThreads != null) {
                    long threads = (totalThreads instanceof Long) ? (Long) totalThreads :
                            ((Integer) totalThreads).longValue();
                    tvTotalThreads.setText(formatNumber(threads));
                }

                Object activeUsers = result.get("activeUsers");
                if (activeUsers != null) {
                    long users = (activeUsers instanceof Long) ? (Long) activeUsers :
                            ((Integer) activeUsers).longValue();
                    tvActiveUsers.setText(formatNumber(users));
                }
            }

            @Override
            public void onError(String error) {
                tvTotalThreads.setText("1.2K");
                tvActiveUsers.setText("342");
            }
        });
    }

    private String formatNumber(long number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        }
        return String.valueOf(number);
    }

    private void setupListeners() {
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    searchThreads(query);
                }
                return true;
            }
            return false;
        });

        fabNewPost.setOnClickListener(v -> handleNewPost());
        cardNewPost.setOnClickListener(v -> handleNewPost());

        cardMyThreads.setOnClickListener(v -> {
            if (forumService.isUserAuthenticated()) {
                openMyThreads();
            } else {
                showLoginRequiredMessage("view your archive");
            }
        });

        tvViewAllHot.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForumThreadListActivity.class);
            intent.putExtra("filter", "hot");
            intent.putExtra("title", "Active Incidents");
            startActivity(intent);
        });

    }


    private void handleNewPost() {
        if (forumService.isUserAuthenticated()) {
            startActivity(new Intent(this, CreatePostActivity.class));
        } else {
            showLoginRequiredMessage("create a post");
        }
    }

    private void openThreadList(ForumCategory category) {
        Intent intent = new Intent(this, ForumThreadListActivity.class);
        intent.putExtra("category_id", category.getId());
        intent.putExtra("category_name", category.getName());
        startActivity(intent);
    }

    private void openThreadDetail(ForumThread thread) {
        Intent intent = new Intent(this, ForumThreadDetailActivity.class);
        intent.putExtra("thread_id", thread.getId());
        intent.putExtra("thread_title", thread.getTitle());
        startActivity(intent);
    }

    private void openMyThreads() {
        Intent intent = new Intent(this, ForumThreadListActivity.class);
        intent.putExtra("filter", "my");
        intent.putExtra("title", "My Archive");
        intent.putExtra("user_id", forumService.getCurrentUserId());
        startActivity(intent);
    }

    private void searchThreads(String query) {
        Intent intent = new Intent(this, ForumThreadListActivity.class);
        intent.putExtra("search_query", query);
        intent.putExtra("title", "Search: " + query);
        startActivity(intent);
    }

    private void showCategoryContextMenu(ForumCategory category, View anchorView) {
        androidx.appcompat.widget.PopupMenu popupMenu =
                new androidx.appcompat.widget.PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.menu_category_context, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_view) {
                openThreadList(category);
                return true;
            } else if (id == R.id.menu_copy_link) {
                copyCategoryLink(category);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void copyCategoryLink(ForumCategory category) {
        String link = "https://cyberlearn.com/forum/category/" + category.getId();
        android.content.ClipboardManager clipboard =
                (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Forum Link", link);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Link copied", Toast.LENGTH_SHORT).show();
    }

    private void showLoginRequiredMessage(String action) {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("🔐 Login Required")
                .setMessage("Please login to " + action)
                .setPositiveButton("Login", (dialog, which) -> {
                    Toast.makeText(this, "Go to login...", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHotThreads();
        loadForumStats();
    }
}