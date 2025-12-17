package com.example.appattt.forum;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appattt.R;
import com.example.appattt.adapters.CommentAdapter;
import com.example.appattt.fragments.ReplyDialogFragment;
import com.example.appattt.models.ForumPost;
import com.example.appattt.models.Writeup;
import com.example.appattt.services.ForumFirebaseService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WriteupDetailActivity extends AppCompatActivity
        implements ReplyDialogFragment.OnReplyPostedListener {

    private Toolbar toolbar;
    private TextView tvTitle, tvAuthor, tvMetaInfo, tvViews, tvCommentCount;
    private TextView tvLikes, tvComments, tvShares;
    private Chip chipRoom, chipDifficulty;
    private WebView webViewContent;
    private Button btnLike, btnBookmark, btnShare, btnFollow;
    private ImageView ivVerified, ivFeatured;
    private ChipGroup chipGroupTags;
    private RecyclerView rvComments;
    private ProgressBar progressBar;
    private LinearLayout layoutNoComments;
    private FloatingActionButton fabComment;

    private String writeupId;
    private Writeup currentWriteup;
    private boolean isLiked = false;
    private boolean isBookmarked = false;

    private ForumFirebaseService forumService;
    private CommentAdapter commentAdapter;
    private List<ForumPost> commentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writeup_detail);

        writeupId = getIntent().getStringExtra("writeup_id");
        if (writeupId == null) {
            Toast.makeText(this, "Writeup not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        forumService = new ForumFirebaseService();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadWriteupData();
        loadComments();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tvTitle);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvMetaInfo = findViewById(R.id.tvMetaInfo);
        tvViews = findViewById(R.id.tvViews);
        tvCommentCount = findViewById(R.id.tvCommentCount);
        chipRoom = findViewById(R.id.chipRoom);
        chipDifficulty = findViewById(R.id.chipDifficulty);
        webViewContent = findViewById(R.id.webViewContent);
        btnLike = findViewById(R.id.btnLike);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnShare = findViewById(R.id.btnShare);
        btnFollow = findViewById(R.id.btnFollow);
        ivVerified = findViewById(R.id.ivVerified);
        ivFeatured = findViewById(R.id.ivFeatured);
        chipGroupTags = findViewById(R.id.chipGroupTags);
        rvComments = findViewById(R.id.rvComments);
        progressBar = findViewById(R.id.progressBar);
        layoutNoComments = findViewById(R.id.layoutNoComments);
        fabComment = findViewById(R.id.fabComment);

        // Stats views
        tvLikes = findViewById(R.id.tvLikes);
        tvComments = findViewById(R.id.tvComments);
        tvShares = findViewById(R.id.tvShares);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Write-up");
        }
    }

    private void setupRecyclerView() {
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(this, commentList, forumService.getCurrentUserId());
        rvComments.setAdapter(commentAdapter);

        commentAdapter.setOnCommentClickListener(new CommentAdapter.OnCommentClickListener() {
            @Override
            public void onReplyClick(ForumPost comment) {
                showReplyDialog(comment.getId());
            }

            @Override
            public void onUpvoteClick(ForumPost comment) {
                toggleCommentUpvote(comment);
            }

            @Override
            public void onAuthorClick(String authorId) {
                openUserProfile(authorId);
            }

            @Override
            public void onMarkAsSolutionClick(ForumPost comment) {
                // Not applicable for writeups
            }

            @Override
            public void onReportClick(ForumPost comment) {
                reportComment(comment);
            }
        });
    }

    private void loadWriteupData() {
        progressBar.setVisibility(View.VISIBLE);

        forumService.getWriteupById(writeupId, new ForumFirebaseService.DataCallback<Writeup>() {
            @Override
            public void onSuccess(Writeup writeup) {
                progressBar.setVisibility(View.GONE);
                currentWriteup = writeup;
                updateWriteupUI();
                checkIfLiked();
                checkIfBookmarked();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(WriteupDetailActivity.this,
                        "Error loading writeup: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadComments() {
        if (writeupId != null) {
            // Sử dụng phương thức getPostsByThread thay vì getWriteupComments
            forumService.getPostsByThread(writeupId, new ForumFirebaseService.DataCallback<List<ForumPost>>() {
                @Override
                public void onSuccess(List<ForumPost> result) {
                    commentList.clear();
                    commentList.addAll(result);
                    commentAdapter.notifyDataSetChanged();

                    // Update comment count
                    updateCommentCountUI();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(WriteupDetailActivity.this,
                            "Error loading comments: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateWriteupUI() {
        if (currentWriteup == null) return;

        tvTitle.setText(currentWriteup.getTitle());
        tvAuthor.setText(currentWriteup.getAuthorName());
        chipRoom.setText(currentWriteup.getRoomName());
        chipDifficulty.setText(currentWriteup.getDifficulty());
        tvViews.setText(currentWriteup.getViews() + " views");

        // Format meta info
        if (currentWriteup.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String timeAgo = sdf.format(currentWriteup.getCreatedAt());

            // Calculate read time
            String content = currentWriteup.getContent();
            int readTime = 5; // default
            if (content != null && !content.trim().isEmpty()) {
                int wordCount = content.split("\\s+").length;
                readTime = Math.max(1, wordCount / 200);
            }

            tvMetaInfo.setText("Level 42 • " + timeAgo + " • " + readTime + " min read");
        }

        // Update stats
        tvLikes.setText(String.valueOf(currentWriteup.getLikes()));
        tvComments.setText(String.valueOf(currentWriteup.getComments()));
        tvShares.setText(String.valueOf(currentWriteup.getShares()));

        // Display content in WebView
        String content = currentWriteup.getContent();
        if (content != null && !content.trim().isEmpty()) {
            String htmlContent = convertMarkdownToHtml(content);
            webViewContent.loadData(htmlContent, "text/html", "UTF-8");
        } else {
            webViewContent.loadData("<p>No content available</p>", "text/html", "UTF-8");
        }

        // Set badges
        ivVerified.setVisibility(currentWriteup.isVerified() ? View.VISIBLE : View.GONE);
        ivFeatured.setVisibility(currentWriteup.isFeatured() ? View.VISIBLE : View.GONE);

        // Set difficulty color
        setDifficultyColor(currentWriteup.getDifficulty());

        // Add tags
        addTagsToView();
    }

    private void updateCommentCountUI() {
        int commentCount = commentList.size();
        tvCommentCount.setText(commentCount + " comments");
        tvComments.setText(String.valueOf(commentCount));

        // Show/hide empty state
        if (commentCount == 0) {
            layoutNoComments.setVisibility(View.VISIBLE);
            rvComments.setVisibility(View.GONE);
        } else {
            layoutNoComments.setVisibility(View.GONE);
            rvComments.setVisibility(View.VISIBLE);
        }
    }

    private String convertMarkdownToHtml(String markdown) {
        // Simple conversion for now
        String html = markdown
                .replace("\n", "<br>")
                .replace("**", "<b>")
                .replace("__", "<b>")
                .replace("*", "<i>")
                .replace("_", "<i>")
                .replace("```", "<pre><code>")
                .replace("# ", "<h1>")
                .replace("## ", "<h2>")
                .replace("### ", "<h3>");

        return "<html><head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
                "<style>" +
                "body { font-family: sans-serif; color: #FFFFFF; line-height: 1.6; padding: 16px; }" +
                "h1 { color: #4CAF50; } h2 { color: #4CAF50; } h3 { color: #4CAF50; }" +
                "code { background: #2c2c2c; padding: 2px 4px; border-radius: 4px; }" +
                "pre { background: #2c2c2c; padding: 12px; border-radius: 8px; overflow-x: auto; }" +
                "img { max-width: 100%; height: auto; }" +
                "</style></head><body>" + html + "</body></html>";
    }

    private void setDifficultyColor(String difficulty) {
        int colorRes;
        switch (difficulty.toLowerCase()) {
            case "beginner":
                colorRes = R.color.cyber_green_main;
                break;
            case "intermediate":
                colorRes = R.color.cyber_orange_premium;
                break;
            case "advanced":
                colorRes = R.color.cyber_red;
                break;
            case "expert":
                colorRes = R.color.cyber_purple;
                break;
            default:
                colorRes = R.color.cyber_text_secondary;
        }
        chipDifficulty.setChipBackgroundColorResource(colorRes);
    }

    private void addTagsToView() {
        if (currentWriteup == null || currentWriteup.getTags() == null) return;

        chipGroupTags.removeAllViews();

        for (String tag : currentWriteup.getTags()) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setChipBackgroundColorResource(R.color.cyber_card_background);
            chip.setTextColor(getResources().getColor(R.color.cyber_green_main));
            chip.setChipStrokeColorResource(R.color.cyber_green_main);
            chip.setChipStrokeWidth(1f);
            chipGroupTags.addView(chip);
        }
    }

    private void checkIfLiked() {
        if (!forumService.isUserAuthenticated()) {
            isLiked = false;
            btnLike.setSelected(false);
            return;
        }

        // Tạm thời: Trong thực tế cần kiểm tra từ Firebase
        isLiked = false;
        btnLike.setSelected(isLiked);
    }

    private void checkIfBookmarked() {
        if (!forumService.isUserAuthenticated()) {
            isBookmarked = false;
            btnBookmark.setSelected(false);
            return;
        }

        // Tạm thời
        isBookmarked = false;
        btnBookmark.setSelected(isBookmarked);
    }

    private void setupListeners() {
        btnLike.setOnClickListener(v -> toggleLike());
        btnBookmark.setOnClickListener(v -> toggleBookmark());
        btnShare.setOnClickListener(v -> shareWriteup());
        btnFollow.setOnClickListener(v -> toggleFollow());
        fabComment.setOnClickListener(v -> showReplyDialog(null));

        tvAuthor.setOnClickListener(v -> {
            if (currentWriteup != null) {
                openUserProfile(currentWriteup.getAuthorId());
            }
        });

        chipRoom.setOnClickListener(v -> {
            // TODO: Open room detail
            Toast.makeText(this, "Opening room: " + currentWriteup.getRoomName(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void toggleLike() {
        if (!forumService.isUserAuthenticated()) {
            Toast.makeText(this, "Please login to like", Toast.LENGTH_SHORT).show();
            return;
        }

        forumService.toggleWriteupLike(writeupId, new ForumFirebaseService.DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean liked) {
                isLiked = liked;
                btnLike.setSelected(isLiked);

                // Update like count
                if (currentWriteup != null) {
                    int newLikes = currentWriteup.getLikes() + (liked ? 1 : -1);
                    currentWriteup.setLikes(newLikes);
                    tvLikes.setText(String.valueOf(newLikes));
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(WriteupDetailActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleBookmark() {
        if (!forumService.isUserAuthenticated()) {
            Toast.makeText(this, "Please login to bookmark", Toast.LENGTH_SHORT).show();
            return;
        }

        isBookmarked = !isBookmarked;
        btnBookmark.setSelected(isBookmarked);

        // TODO: Save bookmark to Firebase
        Toast.makeText(this, isBookmarked ? "Bookmarked" : "Removed bookmark",
                Toast.LENGTH_SHORT).show();
    }

    private void toggleFollow() {
        if (!forumService.isUserAuthenticated()) {
            Toast.makeText(this, "Please login to follow", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement follow author
        Toast.makeText(this, "Follow feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void shareWriteup() {
        if (currentWriteup == null) return;

        String shareText = currentWriteup.getTitle() + "\n\n" +
                "By " + currentWriteup.getAuthorName() + "\n" +
                "Room: " + currentWriteup.getRoomName() + "\n" +
                "Difficulty: " + currentWriteup.getDifficulty() + "\n\n" +
                "Read more on CyberLearn";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentWriteup.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Write-up"));
    }

    private void showReplyDialog(String parentPostId) {
        if (!forumService.isUserAuthenticated()) {
            Toast.makeText(this, "Please login to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        ReplyDialogFragment dialog = ReplyDialogFragment.newInstance(writeupId, parentPostId);
        dialog.setOnReplyPostedListener(this);
        dialog.show(getSupportFragmentManager(), "reply_dialog");
    }

    private void toggleCommentUpvote(ForumPost comment) {
        if (!forumService.isUserAuthenticated()) {
            Toast.makeText(this, "Please login to upvote", Toast.LENGTH_SHORT).show();
            return;
        }

        forumService.togglePostUpvote(comment.getId(), new ForumFirebaseService.DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean upvoted) {
                // Update UI
                int newUpvotes = comment.getUpvotes() + (upvoted ? 1 : -1);
                comment.setUpvotes(newUpvotes);
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(WriteupDetailActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openUserProfile(String userId) {
        // TODO: Open user profile activity
        Toast.makeText(this, "Opening user profile: " + userId, Toast.LENGTH_SHORT).show();
    }

    private void reportComment(ForumPost comment) {
        // TODO: Implement report comment
        Toast.makeText(this, "Report comment: " + comment.getId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReplyPosted() {
        // Refresh comments
        loadComments();

        // Update writeup comment count
        if (currentWriteup != null) {
            currentWriteup.setComments(currentWriteup.getComments() + 1);
            updateCommentCountUI();
        }
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