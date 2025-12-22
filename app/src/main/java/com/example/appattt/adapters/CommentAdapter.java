package com.example.appattt.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appattt.R;
import com.example.appattt.models.ForumPost;
import com.example.appattt.utils.DateUtils;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context context;
    private List<ForumPost> commentList;
    private String currentUserId;
    private OnCommentClickListener listener;

    public interface OnCommentClickListener {
        void onReplyClick(ForumPost comment);
        void onUpvoteClick(ForumPost comment);
        void onAuthorClick(String authorId);
        void onMarkAsSolutionClick(ForumPost comment);
        void onReportClick(ForumPost comment);
    }

    public CommentAdapter(Context context, List<ForumPost> commentList, String currentUserId) {
        this.context = context;
        this.commentList = commentList;
        this.currentUserId = currentUserId;
    }

    public void setOnCommentClickListener(OnCommentClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForumPost comment = commentList.get(position);

        // Set content
        holder.tvContent.setText(comment.getContent());

        // Set author
        holder.tvAuthor.setText(comment.getAuthorName());

        // Set upvotes
        holder.tvUpvotes.setText(String.valueOf(comment.getUpvotes()));

        // Set time
        if (comment.getCreatedAt() != null) {
            holder.tvTime.setText(DateUtils.getTimeAgo(comment.getCreatedAt()));
        }

        // Show solution badge - SỬA: Sử dụng isSolution() thay vì isAnswer()
        holder.ivSolutionBadge.setVisibility(comment.isSolution() ? View.VISIBLE : View.GONE);

        // Adjust margin based on depth - SỬA: Sử dụng getDepth()
        int depth = comment.getDepth();
        int margin = depth * 40; // 40dp per level

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        params.leftMargin = margin;
        holder.itemView.setLayoutParams(params);

        // Show reply button only for top-level comments (depth == 0) - SỬA
        holder.btnReply.setVisibility(depth == 0 ? View.VISIBLE : View.GONE);

        // Show mark as solution button if user is thread author and comment is not already solution
        boolean isThreadAuthor = currentUserId != null && currentUserId.equals(comment.getAuthorId());
        holder.btnMarkSolution.setVisibility(
                isThreadAuthor && !comment.isSolution() && depth == 0 ? View.VISIBLE : View.GONE
        );

        // Button listeners
        holder.btnReply.setOnClickListener(v -> {
            if (listener != null) listener.onReplyClick(comment);
        });

        holder.btnUpvote.setOnClickListener(v -> {
            if (listener != null) listener.onUpvoteClick(comment);
        });

        holder.tvAuthor.setOnClickListener(v -> {
            if (listener != null) listener.onAuthorClick(comment.getAuthorId());
        });

        holder.btnMarkSolution.setOnClickListener(v -> {
            if (listener != null) listener.onMarkAsSolutionClick(comment);
        });

        holder.btnReport.setOnClickListener(v -> {
            if (listener != null) listener.onReportClick(comment);
        });
    }

    @Override
    public int getItemCount() {
        return commentList != null ? commentList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvAuthor, tvUpvotes, tvTime;
        ImageView ivSolutionBadge, btnReply, btnUpvote, btnMarkSolution, btnReport;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvUpvotes = itemView.findViewById(R.id.tvUpvotes);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivSolutionBadge = itemView.findViewById(R.id.ivSolutionBadge);
            btnReply = itemView.findViewById(R.id.btnReply);
            btnUpvote = itemView.findViewById(R.id.btnUpvote);
            btnMarkSolution = itemView.findViewById(R.id.btnMarkSolution);
        }
    }
}