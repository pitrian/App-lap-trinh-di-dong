package com.example.appattt.forum;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.example.appattt.models.ForumThread;
import com.example.appattt.services.ForumFirebaseService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ForumThreadDetailActivity extends AppCompatActivity
        implements ReplyDialogFragment.OnReplyPostedListener {

    private Toolbar toolbar;
    private TextView tvTitle, tvContent, tvAuthor, tvCategory, tvUpvotes, tvViews, tvReplyCount, tvTime;
    private Button btnUpvote, btnBookmark;
    private ImageView btnShare;
    private RecyclerView rvComments;
    private ProgressBar progressBar;
    private FloatingActionButton fabReply;
    private Button btnMarkSolved;

    private String threadId;
    private ForumThread currentThread;
    private boolean isUpvoted = false;
    private boolean isBookmarked = false;

    private ForumFirebaseService forumService;
    private CommentAdapter commentAdapter;
    private List<ForumPost> commentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_thread_detail);

        threadId = getIntent().getStringExtra("thread_id");
        if (threadId == null) {
            Toast.makeText(this, "Thread not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        forumService = new ForumFirebaseService();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadThreadData();
        loadComments();
        setupListeners();

        // Increment view count
        forumService.incrementThreadView(threadId);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tvTitle);
        tvContent = findViewById(R.id.tvContent);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvCategory = findViewById(R.id.tvCategory);
        tvUpvotes = findViewById(R.id.tvUpvotes);
        tvViews = findViewById(R.id.tvViews);
        tvReplyCount = findViewById(R.id.tvReplyCount);
        tvTime = findViewById(R.id.tvTime);
        btnUpvote = findViewById(R.id.btnUpvote);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnShare = findViewById(R.id.btnShare);
        rvComments = findViewById(R.id.rvComments);
        progressBar = findViewById(R.id.progressBar);
        fabReply = findViewById(R.id.fabReply);
        btnMarkSolved = findViewById(R.id.btnMarkSolved);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thread");
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
                markAsSolution(comment);
            }

            @Override
            public void onReportClick(ForumPost comment) {
                reportComment(comment);
            }
        });
    }

    private void loadThreadData() {
        progressBar.setVisibility(View.VISIBLE);

        forumService.getThreadById(threadId, new ForumFirebaseService.DataCallback<ForumThread>() {
            @Override
            public void onSuccess(ForumThread thread) {
                progressBar.setVisibility(View.GONE);
                currentThread = thread;
                updateThreadUI();

                // Check if user has upvoted
                forumService.checkThreadUpvote(threadId, new ForumFirebaseService.DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        isUpvoted = result;
                        btnUpvote.setSelected(isUpvoted);
                    }

                    @Override
                    public void onError(String error) {
                        // Ignore error
                    }
                });
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ForumThreadDetailActivity.this,
                        "Error loading thread: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadComments() {
        forumService.getPostsByThread(threadId, new ForumFirebaseService.DataCallback<List<ForumPost>>() {
            @Override
            public void onSuccess(List<ForumPost> result) {
                commentList.clear();
                commentList.addAll(result);
                commentAdapter.notifyDataSetChanged();

                // Update reply count in UI
                if (currentThread != null) {
                    tvReplyCount.setText(String.valueOf(commentList.size()));
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ForumThreadDetailActivity.this,
                        "Error loading comments: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateThreadUI() {
        if (currentThread == null) return;

        tvTitle.setText(currentThread.getTitle());
        tvContent.setText(currentThread.getContent());
        tvAuthor.setText(currentThread.getAuthorName());
        tvCategory.setText(currentThread.getCategoryName());
        tvUpvotes.setText(String.valueOf(currentThread.getUpvotes()));
        tvViews.setText(String.valueOf(currentThread.getViews()));
        tvReplyCount.setText(String.valueOf(currentThread.getReplyCount()));
        tvTime.setText(currentThread.getTimeAgo());

        // Show/hide mark as solved button
        boolean isThreadAuthor = forumService.getCurrentUserId() != null &&
                forumService.getCurrentUserId().equals(currentThread.getAuthorId());
        btnMarkSolved.setVisibility(isThreadAuthor && !currentThread.isSolved() ? View.VISIBLE : View.GONE);

        // Update toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentThread.isSolved() ? "✓ Solved" : "Thread");
        }
    }

    private void setupListeners() {
        btnUpvote.setOnClickListener(v -> toggleThreadUpvote());

        btnBookmark.setOnClickListener(v -> toggleBookmark());

        fabReply.setOnClickListener(v -> showReplyDialog(null));

        tvAuthor.setOnClickListener(v -> {
            if (currentThread != null) {
                openUserProfile(currentThread.getAuthorId());
            }
        });

        tvCategory.setOnClickListener(v -> {
            // TODO: Open category
        });

        btnMarkSolved.setOnClickListener(v -> {
            showMarkSolvedDialog();
        });

        btnShare.setOnClickListener(v -> {
            // TODO: Share thread
        });
    }

    private void toggleThreadUpvote() {
        if (!forumService.isUserAuthenticated()) {
            Toast.makeText(this, "Please login to upvote", Toast.LENGTH_SHORT).show();
            return;
        }

        forumService.toggleThreadUpvote(threadId, new ForumFirebaseService.DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean upvoted) {
                isUpvoted = upvoted;
                btnUpvote.setSelected(isUpvoted);

                // Update count
                if (currentThread != null) {
                    int newCount = currentThread.getUpvotes() + (upvoted ? 1 : -1);
                    currentThread.setUpvotes(newCount);
                    tvUpvotes.setText(String.valueOf(newCount));
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ForumThreadDetailActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleBookmark() {
        isBookmarked = !isBookmarked;
        btnBookmark.setSelected(isBookmarked);

        // TODO: Save bookmark to Firebase
        Toast.makeText(this, isBookmarked ? "Bookmarked" : "Removed bookmark",
                Toast.LENGTH_SHORT).show();
    }

    private void showReplyDialog(String parentPostId) {
        if (!forumService.isUserAuthenticated()) {
            Toast.makeText(this, "Please login to reply", Toast.LENGTH_SHORT).show();
            return;
        }

        ReplyDialogFragment dialog = ReplyDialogFragment.newInstance(threadId, parentPostId);
        dialog.setOnReplyPostedListener(this);
        dialog.show(getSupportFragmentManager(), "reply_dialog");
    }

    private void toggleCommentUpvote(ForumPost comment) {
        // TODO: Implement comment upvote
    }

    private void openUserProfile(String userId) {
        // TODO: Open user profile
    }

    private void markAsSolution(ForumPost comment) {
        // TODO: Implement mark as solution
    }

    private void reportComment(ForumPost comment) {
        // TODO: Implement report
    }

    private void showMarkSolvedDialog() {
        // TODO: Show dialog to select solution comment
    }

    @Override
    public void onReplyPosted() {
        // Refresh comments
        loadComments();

        // Update thread reply count
        if (currentThread != null) {
            currentThread.setReplyCount(currentThread.getReplyCount() + 1);
            tvReplyCount.setText(String.valueOf(currentThread.getReplyCount()));
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