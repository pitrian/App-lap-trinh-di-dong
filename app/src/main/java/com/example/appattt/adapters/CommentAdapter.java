package com.example.appattt.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private OnCommentClickListener listener;
    private String currentUserId;

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

    public void updateData(List<ForumPost> newList) {
        this.commentList = newList;
        notifyDataSetChanged();
    }

    public void addComment(ForumPost comment) {
        commentList.add(comment);
        notifyItemInserted(commentList.size() - 1);
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

        holder.tvAuthor.setText(comment.getAuthorName());
        holder.tvContent.setText(comment.getContent());
        holder.tvTime.setText(DateUtils.getTimeAgo(comment.getCreatedAt()));
        holder.tvUpvotes.setText(String.valueOf(comment.getUpvotes()));

        // Show solution badge
        if (comment.isAnswer()) {
            holder.tvSolutionBadge.setVisibility(View.VISIBLE);
            holder.ivSolutionBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvSolutionBadge.setVisibility(View.GONE);
            holder.ivSolutionBadge.setVisibility(View.GONE);
        }

        // Show reply count if any
        if (comment.getDepth() == 0) {
            holder.tvReplyCount.setVisibility(View.VISIBLE);
        } else {
            holder.tvReplyCount.setVisibility(View.GONE);
        }

        // Set upvote button state
        holder.btnUpvote.setSelected(comment.getUpvotes() > 0);

        // Enable mark as solution if user is thread author
        boolean canMarkAsSolution = false; // TODO: Check if current user is thread author
        holder.btnMarkSolution.setVisibility(canMarkAsSolution ? View.VISIBLE : View.GONE);

        // Set listeners
        holder.btnReply.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReplyClick(comment);
            }
        });

        holder.btnUpvote.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpvoteClick(comment);
            }
        });

        holder.tvAuthor.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAuthorClick(comment.getAuthorId());
            }
        });

        holder.btnMarkSolution.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMarkAsSolutionClick(comment);
            }
        });

        holder.btnMore.setOnClickListener(v -> {
            showMoreOptions(comment, holder.btnMore);
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        LinearLayout ivSolutionBadge;
        TextView tvAuthor, tvContent, tvTime, tvUpvotes, tvSolutionBadge, tvReplyCount;
        View btnReply, btnUpvote, btnMarkSolution, btnMore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivSolutionBadge = itemView.findViewById(R.id.ivSolutionBadge);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUpvotes = itemView.findViewById(R.id.tvUpvotes);
            tvSolutionBadge = itemView.findViewById(R.id.tvSolutionBadge);
            tvReplyCount = itemView.findViewById(R.id.tvReplyCount);
            btnReply = itemView.findViewById(R.id.btnReply);
            btnUpvote = itemView.findViewById(R.id.btnUpvote);
            btnMarkSolution = itemView.findViewById(R.id.btnMarkSolution);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }

    private void showMoreOptions(ForumPost comment, View anchor) {
        // TODO: Implement options menu (report, copy, share, etc.)
    }
}