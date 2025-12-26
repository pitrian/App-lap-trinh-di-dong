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

    private final Context context;
    private final List<ForumPost> commentList;
    private final String currentUserId;
    private OnCommentClickListener listener;

    public interface OnCommentClickListener {
        void onReplyClick(ForumPost comment);
        void onUpvoteClick(ForumPost comment);
        void onAuthorClick(String authorId);
        void onMarkAsSolutionClick(ForumPost comment);
        void onReportClick(ForumPost comment);   // tạm thời chưa có nút, để dành sau
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

        // Nội dung
        holder.tvContent.setText(comment.getContent());

        // Tác giả
        holder.tvAuthor.setText(comment.getAuthorName());

        // Upvote
        holder.tvUpvotes.setText(String.valueOf(comment.getUpvotes()));

        // Thời gian
        if (comment.getCreatedAt() != null) {
            holder.tvTime.setText(DateUtils.getTimeAgo(comment.getCreatedAt()));
        } else {
            holder.tvTime.setText("");
        }

        // Badge solution
        holder.ivSolutionBadge.setVisibility(comment.isSolution() ? View.VISIBLE : View.GONE);

        // Thụt lề theo depth (comment con)
        int depth = comment.getDepth();
        int marginLeft = depth * 40; // muốn đẹp hơn có thể đổi sang dp

        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) lp;
            params.leftMargin = marginLeft;
            holder.itemView.setLayoutParams(params);
        }

        // Chỉ comment level 0 mới được reply
        holder.btnReply.setVisibility(depth == 0 ? View.VISIBLE : View.GONE);

        // Tạm cho chính chủ comment được mark solution (sau này có threadAuthorId thì sửa lại)
        boolean isThreadAuthor = currentUserId != null
                && currentUserId.equals(comment.getAuthorId());

        holder.btnMarkSolution.setVisibility(
                isThreadAuthor && !comment.isSolution() && depth == 0
                        ? View.VISIBLE : View.GONE
        );

        // Click listeners
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

        // CHƯA có nút report trong layout nên không setOnClickListener cho report
    }

    @Override
    public int getItemCount() {
        return commentList != null ? commentList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvAuthor, tvUpvotes, tvTime;
        ImageView ivSolutionBadge, btnReply, btnUpvote, btnMarkSolution;

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
            // Không gọi findViewById(R.id.btnReport) nữa để tránh lỗi compile
        }
    }
}
