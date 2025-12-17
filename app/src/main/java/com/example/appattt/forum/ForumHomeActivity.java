package com.example.appattt.forum;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.appattt.adapters.ThreadAdapter;
import com.example.appattt.models.ForumCategory;
import com.example.appattt.models.ForumThread;
import com.example.appattt.services.ForumFirebaseService;

import java.util.ArrayList;
import java.util.List;

public class ForumHomeActivity extends AppCompatActivity {

    private RecyclerView rvCategories, rvHotThreads;
    private EditText etSearch;
    private ProgressBar progressBar;
    private ImageView fabNewPost;
    private View cardNewPost, cardMyThreads, tvViewAllHot;
    private TextView tvTotalThreads, tvActiveUsers;

    private ForumFirebaseService forumService;
    private CategoryAdapter categoryAdapter;
    private ThreadAdapter hotThreadsAdapter;
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
        setupListeners();
        loadForumStats();
    }

    private void initViews() {
        rvCategories = findViewById(R.id.rvCategories);
        rvHotThreads = findViewById(R.id.rvHotThreads);
        etSearch = findViewById(R.id.etSearch);
        progressBar = findViewById(R.id.progressBar);
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
            getSupportActionBar().setTitle("Community Forum");
        }

        // Xử lý menu icon click (nếu có drawer)
        ImageView menuIcon = toolbar.findViewById(R.id.toolbar).getRootView().findViewById(R.id.menu_icon);
        if (menuIcon != null) {
            menuIcon.setOnClickListener(v -> {
                // TODO: Open navigation drawer
                Toast.makeText(this, "Open menu", Toast.LENGTH_SHORT).show();
            });
        }
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
        rvHotThreads.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        hotThreadsAdapter = new ThreadAdapter(this, hotThreadsList);
        rvHotThreads.setAdapter(hotThreadsAdapter);

        hotThreadsAdapter.setOnItemClickListener(new ThreadAdapter.OnItemClickListener() {
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
                // Do nothing or open category
            }
        });
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);

        forumService.getAllCategories(new ForumFirebaseService.DataCallback<List<ForumCategory>>() {
            @Override
            public void onSuccess(List<ForumCategory> result) {
                progressBar.setVisibility(View.GONE);
                categoryList.clear();
                categoryList.addAll(result);
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ForumHomeActivity.this,
                        "Error loading categories: " + error, Toast.LENGTH_SHORT).show();
                // Load sample categories as fallback
                loadSampleCategories();
            }
        });
    }

    private void loadSampleCategories() {
        // Sample data for testing
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
                "help",
                "Room Help & Tips",
                "Get help with specific rooms and challenges",
                "ic_help_circle",
                512,
                3241
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

        categoryAdapter.notifyDataSetChanged();
    }

    private void loadHotThreads() {
        // For now, load sample hot threads
        hotThreadsList.clear();

        hotThreadsList.add(new ForumThread(
                "Best practices for SQL injection prevention?",
                "Discuss best practices for preventing SQL injection attacks...",
                "user123",
                "SecurityNinja",
                "general",
                "General Discussion"
        ));
        hotThreadsList.get(0).setId("1");
        hotThreadsList.get(0).setUpvotes(342);
        hotThreadsList.get(0).setViews(1234);
        hotThreadsList.get(0).setReplyCount(42);
        hotThreadsList.get(0).setSolved(true);

        hotThreadsList.add(new ForumThread(
                "Stuck on Web App Security Room - Task 5",
                "Need help with Web App Security Room Task 5...",
                "user456",
                "BeginnerHacker",
                "help",
                "Room Help & Tips"
        ));
        hotThreadsList.get(1).setId("2");
        hotThreadsList.get(1).setUpvotes(15);
        hotThreadsList.get(1).setViews(542);
        hotThreadsList.get(1).setReplyCount(15);

        hotThreadsList.add(new ForumThread(
                "CTF 2024 Winter Championship - Discussion",
                "Discussion thread for CTF 2024 Winter Championship...",
                "user789",
                "CTFMaster",
                "ctf",
                "CTF Discussions"
        ));
        hotThreadsList.get(2).setId("3");
        hotThreadsList.get(2).setUpvotes(87);
        hotThreadsList.get(2).setViews(2314);
        hotThreadsList.get(2).setReplyCount(87);

        hotThreadsAdapter.notifyDataSetChanged();
    }

    private void loadForumStats() {
        // For now, set sample stats
        tvTotalThreads.setText("1.2K");
        tvActiveUsers.setText("342");

        // TODO: Fetch real stats from Firebase
        // In real app, you would query Firestore for total thread count
        // and active users count (users online in last 24 hours)
    }

    private void setupListeners() {
        // Search functionality
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

        // New Post FAB
        fabNewPost.setOnClickListener(v -> {
            if (forumService.isUserAuthenticated()) {
                startActivity(new Intent(this, CreatePostActivity.class));
            } else {
                showLoginRequiredMessage("create a post");
            }
        });

        // New Post Card
        cardNewPost.setOnClickListener(v -> {
            if (forumService.isUserAuthenticated()) {
                startActivity(new Intent(this, CreatePostActivity.class));
            } else {
                showLoginRequiredMessage("create a post");
            }
        });

        // My Threads Card
        cardMyThreads.setOnClickListener(v -> {
            if (forumService.isUserAuthenticated()) {
                openMyThreads();
            } else {
                showLoginRequiredMessage("view your threads");
            }
        });

        // View All Hot Topics
        tvViewAllHot.setOnClickListener(v -> {
            // Open all hot threads in a list
            Intent intent = new Intent(this, ForumThreadListActivity.class);
            intent.putExtra("filter", "hot");
            intent.putExtra("category_name", "Hot Topics");
            startActivity(intent);
        });
    }

    private void showCategoryContextMenu(ForumCategory category, View anchorView) {
        // Create popup menu
        androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.menu_category_context, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_view) {
                openThreadList(category);
                return true;
            } else if (id == R.id.menu_view_new) {
                openCategoryWithFilter(category, "new");
                return true;
            } else if (id == R.id.menu_mark_read) {
                markCategoryAsRead(category);
                return true;
            } else if (id == R.id.menu_notifications) {
                showNotificationSettings(category);
                return true;
            } else if (id == R.id.menu_copy_link) {
                copyCategoryLink(category);
                return true;
            } else if (id == R.id.menu_share) {
                shareCategory(category);
                return true;
            }
            return false;
        });

        popupMenu.show();
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

    private void openCategoryWithFilter(ForumCategory category, String filter) {
        Intent intent = new Intent(this, ForumThreadListActivity.class);
        intent.putExtra("category_id", category.getId());
        intent.putExtra("category_name", category.getName());
        intent.putExtra("filter", filter);
        startActivity(intent);
    }

    private void openMyThreads() {
        Intent intent = new Intent(this, ForumThreadListActivity.class);
        intent.putExtra("filter", "my");
        intent.putExtra("category_name", "My Threads");
        intent.putExtra("user_id", forumService.getCurrentUserId());
        startActivity(intent);
    }

    private void openUserProfile(String userId) {
        // TODO: Open user profile activity
        Toast.makeText(this, "Opening user profile: " + userId, Toast.LENGTH_SHORT).show();
    }

    private void searchThreads(String query) {
        Intent intent = new Intent(this, ForumThreadListActivity.class);
        intent.putExtra("search_query", query);
        startActivity(intent);
    }

    private void markCategoryAsRead(ForumCategory category) {
        // TODO: Implement mark category as read
        Toast.makeText(this,
                "Marked " + category.getName() + " as read",
                Toast.LENGTH_SHORT).show();
    }

    private void showNotificationSettings(ForumCategory category) {
        // TODO: Show notification settings dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Notification Settings for " + category.getName())
                .setItems(new String[]{"All posts", "Only mentions", "None"}, (dialog, which) -> {
                    String[] options = {"All posts", "Only mentions", "None"};
                    Toast.makeText(this,
                            "Set to: " + options[which],
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void copyCategoryLink(ForumCategory category) {
        // In real app, this would be a real URL
        String link = "https://cyberlearn.com/forum/category/" + category.getId();

        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Forum Link", link);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void shareCategory(ForumCategory category) {
        String shareText = "Check out " + category.getName() + " on CyberLearn Forum: " +
                "https://cyberlearn.com/forum/category/" + category.getId();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, category.getName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Category"));
    }

    private void showLoginRequiredMessage(String action) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Login Required")
                .setMessage("Please login to " + action)
                .setPositiveButton("Login", (dialog, which) -> {
                    // TODO: Navigate to login screen
                    Toast.makeText(this, "Redirect to login...", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data if needed
        // You can add logic here to refresh categories or hot threads
    }
}