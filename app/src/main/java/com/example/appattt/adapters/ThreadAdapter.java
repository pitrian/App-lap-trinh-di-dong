package com.example.appattt.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appattt.R;
import com.example.appattt.models.ForumThread;
import com.example.appattt.utils.DateUtils;

import java.util.Date;
import java.util.List;

public class ThreadAdapter extends RecyclerView.Adapter<ThreadAdapter.ViewHolder> {

    private Context context;
    private List<ForumThread> threadList;
    private OnItemClickListener listener;

    // Interface để xử lý sự kiện click
    public interface OnItemClickListener {
        void onItemClick(ForumThread thread);
        void onAuthorClick(String authorId);
        void onCategoryClick(String categoryId);

        void onUpvoteClick(ForumThread thread, int position);
    }

    public ThreadAdapter(Context context, List<ForumThread> threadList) {
        this.context = context;
        this.threadList = threadList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<ForumThread> newList) {
        this.threadList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_thread, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForumThread thread = threadList.get(position);

        holder.tvTitle.setText(thread.getTitle());
        holder.tvAuthor.setText(thread.getAuthorName());
        holder.tvCategory.setText(thread.getCategoryName());
        holder.tvReplies.setText(String.valueOf(thread.getReplyCount()));
        holder.tvViews.setText(String.valueOf(thread.getViews()));
        holder.tvUpvotes.setText(String.valueOf(thread.getUpvotes()));
        holder.tvTime.setText(DateUtils.getTimeAgo(new Date(thread.getCreatedAt())));

        // Set solved badge
        if (thread.isSolved()) {
            holder.tvSolved.setVisibility(View.VISIBLE);
        } else {
            holder.tvSolved.setVisibility(View.GONE);
        }

        // Set pinned badge
        if (thread.isPinned()) {
            holder.tvPinned.setVisibility(View.VISIBLE);
        } else {
            holder.tvPinned.setVisibility(View.GONE);
        }

        // Set category color
        String categoryColor = getCategoryColor(thread.getCategoryName());
        holder.tvCategory.setTextColor(Color.parseColor(categoryColor));

        // Item click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(thread);
            }
        });

        // Author click
        holder.tvAuthor.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAuthorClick(thread.getAuthorId());
            }
        });

        // Category click
        holder.tvCategory.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(thread.getCategoryId());
            }
        });
    }

    // GHI CHÚ: Khối interface bị trùng lặp đã được xóa khỏi đây

    @Override
    public int getItemCount() {
        return threadList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle, tvAuthor, tvCategory, tvReplies, tvViews, tvUpvotes, tvTime, tvSolved, tvPinned;
        ImageView ivAuthor;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvTitle = itemView.findViewById(R.id.tvThreadTitle);
            tvAuthor = itemView.findViewById(R.id.tvThreadAuthor);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvReplies = itemView.findViewById(R.id.tvReplies);
            tvViews = itemView.findViewById(R.id.tvViews);
            tvUpvotes = itemView.findViewById(R.id.tvUpvotes);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSolved = itemView.findViewById(R.id.tvSolved);
            tvPinned = itemView.findViewById(R.id.tvPinned);
            ivAuthor = itemView.findViewById(R.id.ivAuthor);
        }
    }

    private String getCategoryColor(String categoryName) {
        // Mặc định là màu xám nếu categoryName là null để tránh lỗi
        if (categoryName == null) {
            return "#757575"; // Grey
        }
        switch (categoryName) {
            case "General Discussion":
                return "#4CAF50"; // Green
            case "Room Help & Tips":
                return "#2196F3"; // Blue
            case "CTF Discussions":
                return "#FF9800"; // Orange
            case "Write-ups & Guides":
                return "#9C27B0"; // Purple
            default:
                return "#757575"; // Grey
        }
    }
}
