package com.example.appattt.forum;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForumThreadDetailActivity extends AppCompatActivity
        implements ReplyDialogFragment.OnReplyPostedListener {

    // UI
    private Toolbar toolbar;
    private TextView tvTitle, tvContent, tvAuthor, tvCategory,
            tvUpvotes, tvViews, tvReplyCount, tvTime;
    private ImageView btnUpvote, btnBookmark, btnShare;
    private RecyclerView rvComments;
    private ProgressBar progressBar;
    private FloatingActionButton fabReply;
    private Button btnMarkSolved;

    // Data
    private String threadId;
    private ForumThread currentThread;
    private boolean isUpvoted = false;
    private boolean isBookmarked = false;

    // Services / adapter
    private ForumFirebaseService forumService;
    private CommentAdapter commentAdapter;
    private final List<ForumPost> commentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_thread_detail);

        // Lấy threadId từ Intent
        threadId = getIntent().getStringExtra("thread_id");
        if (threadId == null || threadId.isEmpty()) {
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

        // Tăng view
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

        // 2 nút này là ImageView trong layout
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

                // Kiểm tra đã upvote chưa
                forumService.checkThreadUpvote(threadId,
                        new ForumFirebaseService.DataCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                isUpvoted = result;
                                btnUpvote.setSelected(isUpvoted);
                            }

                            @Override
                            public void onError(String error) {
                                // bỏ qua, không critical
                            }
                        });
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ForumThreadDetailActivity.this,
                        "Error loading thread: " + error,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadComments() {
        forumService.getPostsByThread(threadId,
                new ForumFirebaseService.DataCallback<List<ForumPost>>() {
                    @Override
                    public void onSuccess(List<ForumPost> result) {
                        commentList.clear();
                        commentList.addAll(result);
                        commentAdapter.notifyDataSetChanged();

                        if (currentThread != null) {
                            tvReplyCount.setText(String.valueOf(commentList.size()));
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ForumThreadDetailActivity.this,
                                "Error loading comments: " + error,
                                Toast.LENGTH_SHORT).show();
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

        boolean isThreadAuthor = forumService.getCurrentUserId() != null &&
                forumService.getCurrentUserId().equals(currentThread.getAuthorId());

        // Chỉ tác giả thread mới thấy nút mark solved
        btnMarkSolved.setVisibility(
                isThreadAuthor && !currentThread.isSolved() ? View.VISIBLE : View.GONE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(
                    currentThread.isSolved() ? "✓ Solved" : "Thread");
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
            // Tạm thời chỉ show toast, sau này muốn mở màn CategoryDetail thì thêm Intent ở đây
            if (currentThread != null) {
                Toast.makeText(
                        this,
                        "Open category: " + currentThread.getCategoryName(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        btnMarkSolved.setOnClickListener(v -> showMarkSolvedDialog());

        btnShare.setOnClickListener(v -> shareThread());
    }

    // ---------- THREAD ACTIONS ----------

    private void toggleThreadUpvote() {
        if (!forumService.isUserAuthenticated()) {
            Toast.makeText(this, "Please login to upvote", Toast.LENGTH_SHORT).show();
            return;
        }

        forumService.toggleThreadUpvote(threadId,
                new ForumFirebaseService.DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean upvoted) {
                        isUpvoted = upvoted;
                        btnUpvote.setSelected(isUpvoted);

                        if (currentThread != null) {
                            int delta = upvoted ? 1 : -1;
                            int newCount = currentThread.getUpvotes() + delta;
                            if (newCount < 0) newCount = 0;
                            currentThread.setUpvotes(newCount);
                            tvUpvotes.setText(String.valueOf(newCount));
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ForumThreadDetailActivity.this,
                                "Error: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void toggleBookmark() {
        if (!forumService.isUserAuthenticated()) {
            Toast.makeText(this, "Please login to bookmark", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiện tại chỉ lưu trạng thái local + Toast.
        // Nếu muốn lưu Firebase: tạo collection "forum_thread_bookmarks".
        isBookmarked = !isBookmarked;
        btnBookmark.setSelected(isBookmarked);

        Toast.makeText(
                this,
                isBookmarked ? "Thread bookmarked" : "Bookmark removed",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void shareThread() {
        if (currentThread == null) return;

        String shareText = currentThread.getTitle() + "\n\n" +
                currentThread.getContent();

        android.content.Intent sendIntent = new android.content.Intent();
        sendIntent.setAction(android.content.Intent.ACTION_SEND);
        sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");

        android.content.Intent shareIntent =
                android.content.Intent.createChooser(sendIntent, "Share thread");
        startActivity(shareIntent);
    }

    // ---------- COMMENT ACTIONS ----------

    private void showReplyDialog(String parentPostId) {
        if (!forumService.isUserAuthenticated()) {
            Toast.makeText(this, "Please login to reply", Toast.LENGTH_SHORT).show();
            return;
        }

        ReplyDialogFragment dialog =
                ReplyDialogFragment.newInstance(threadId, parentPostId);
        dialog.setOnReplyPostedListener(this);
        dialog.show(getSupportFragmentManager(), "reply_dialog");
    }

    private void toggleCommentUpvote(ForumPost comment) {
        if (!forumService.isUserAuthenticated()) {
            Toast.makeText(this, "Please login to upvote", Toast.LENGTH_SHORT).show();
            return;
        }

        forumService.togglePostUpvote(comment.getId(),
                new ForumFirebaseService.DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        // Reload lại list comment cho chắc
                        loadComments();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ForumThreadDetailActivity.this,
                                "Error: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openUserProfile(String userId) {
        if (userId == null || userId.isEmpty()) return;

        // Nếu bạn có màn Profile riêng thì tạo Intent ở đây.
        // Tạm thời chỉ Toast cho an toàn, tránh lỗi ClassNotFound.
        Toast.makeText(
                this,
                "Opening user profile: " + userId,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void markAsSolution(ForumPost comment) {
        if (currentThread == null) return;

        String currentUserId = forumService.getCurrentUserId();
        if (currentUserId == null ||
                !currentUserId.equals(currentThread.getAuthorId())) {
            Toast.makeText(this,
                    "Only thread author can mark solution",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Đánh dấu solved + lưu id comment giải quyết (solutionPostId)
        FirebaseFirestore.getInstance()
                .collection("forum_threads")
                .document(threadId)
                .update("solved", true,
                        "solutionPostId", comment.getId())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Marked as solution",
                            Toast.LENGTH_SHORT).show();

                    btnMarkSolved.setVisibility(View.GONE);
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("✓ Solved");
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    private void reportComment(ForumPost comment) {
        String currentUserId = forumService.getCurrentUserId();
        if (currentUserId == null) {
            Toast.makeText(this,
                    "Please login to report",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> report = new HashMap<>();
        report.put("type", "comment");
        report.put("threadId", threadId);
        report.put("postId", comment.getId());
        report.put("reporterId", currentUserId);
        report.put("createdAt", new Date());

        db.collection("forum_reports")
                .add(report)
                .addOnSuccessListener(ref -> Toast.makeText(this,
                        "Reported comment",
                        Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    private void showMarkSolvedDialog() {
        if (currentThread == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Mark thread as solved")
                .setMessage("Mark this thread as solved?")
                .setPositiveButton("Mark solved",
                        (DialogInterface dialog, int which) -> {
                            // Không chọn comment cụ thể, chỉ đánh solved = true
                            FirebaseFirestore.getInstance()
                                    .collection("forum_threads")
                                    .document(threadId)
                                    .update("solved", true)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this,
                                                "Thread marked as solved",
                                                Toast.LENGTH_SHORT).show();
                                        btnMarkSolved.setVisibility(View.GONE);
                                        if (getSupportActionBar() != null) {
                                            getSupportActionBar()
                                                    .setTitle("✓ Solved");
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this,
                                                    "Error: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show());
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // Khi ReplyDialogFragment post thành công
    @Override
    public void onReplyPosted() {
        loadComments();

        if (currentThread != null) {
            currentThread.setReplyCount(currentThread.getReplyCount() + 1);
            tvReplyCount.setText(String.valueOf(currentThread.getReplyCount()));
        }
    }

    // Xử lý nút back trên toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
