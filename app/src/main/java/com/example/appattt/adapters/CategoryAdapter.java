package com.example.appattt.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appattt.R;
import com.example.appattt.models.ForumCategory;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private Context context;
    private List<ForumCategory> categoryList;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(ForumCategory category);
        void onCategoryLongClick(ForumCategory category, View view);
    }

    public CategoryAdapter(Context context, List<ForumCategory> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<ForumCategory> newList) {
        this.categoryList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForumCategory category = categoryList.get(position);

        // Set category name
        holder.tvCategoryName.setText(category.getName());

        // Set description
        if (category.getDescription() != null && !category.getDescription().isEmpty()) {
            holder.tvDescription.setText(category.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Set stats
        holder.tvTopicCount.setText(formatCount(category.getTopicCount()) + " topics");
        holder.tvPostCount.setText(formatCount(category.getPostCount()) + " posts");

        // Set icon based on category name or icon field
        int iconRes = getIconForCategory(category.getName());
        holder.ivIcon.setImageResource(iconRes);

        // Set icon tint color
        int iconColor = getIconColorForCategory(category.getName());
        holder.ivIcon.setColorFilter(context.getResources().getColor(iconColor));

        // Set background based on category
        int backgroundRes = getBackgroundForCategory(category.getName());
        holder.cardView.setBackgroundResource(backgroundRes);

        // Show badge if there are new posts
        boolean hasNewPosts = hasNewPostsInCategory(category);
        holder.ivNewBadge.setVisibility(hasNewPosts ? View.VISIBLE : View.GONE);

        // Show pinned badge for important categories
        boolean isPinned = isCategoryPinned(category.getName());
        holder.ivPinnedBadge.setVisibility(isPinned ? View.VISIBLE : View.GONE);

        // Item click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });

        // Long click for options
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onCategoryLongClick(category, v);
                return true;
            }
            return false;
        });

        // Stats click to view all topics
        holder.layoutStats.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivIcon, ivNewBadge, ivPinnedBadge;
        TextView tvCategoryName, tvDescription, tvTopicCount, tvPostCount;
        View layoutStats;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardCategory);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            ivNewBadge = itemView.findViewById(R.id.ivNewBadge);
            ivPinnedBadge = itemView.findViewById(R.id.ivPinnedBadge);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTopicCount = itemView.findViewById(R.id.tvTopicCount);
            tvPostCount = itemView.findViewById(R.id.tvPostCount);
            layoutStats = itemView.findViewById(R.id.layoutStats);
        }
    }

    private int getIconForCategory(String categoryName) {
        switch (categoryName) {
            case "General Discussion":
                return R.drawable.ic_chat_bubble;
            case "Room Help & Tips":
                return R.drawable.ic_help_circle;
            case "CTF Discussions":
                return R.drawable.ic_flag;
            case "Write-ups & Guides":
                return R.drawable.ic_document_text;
            case "Bug Bounty":
                return R.drawable.ic_bug;
            case "Career Advice":
                return R.drawable.ic_briefcase;
            case "Tool Discussions":
                return R.drawable.ic_tool;
            case "News & Updates":
                return R.drawable.ic_newspaper;
            case "Off-Topic":
                return R.drawable.ic_coffee;
            default:
                return R.drawable.ic_folder;
        }
    }

    private int getIconColorForCategory(String categoryName) {
        switch (categoryName) {
            case "General Discussion":
                return R.color.cyber_green_main;
            case "Room Help & Tips":
                return R.color.cyber_blue;
            case "CTF Discussions":
                return R.color.cyber_orange_premium;
            case "Write-ups & Guides":
                return R.color.cyber_purple;
            default:
                return R.color.cyber_text_secondary;
        }
    }

    private int getBackgroundForCategory(String categoryName) {
        switch (categoryName) {
            case "General Discussion":
                return R.drawable.bg_category_green;
            case "Room Help & Tips":
                return R.drawable.bg_category_blue;
            case "CTF Discussions":
                return R.drawable.bg_category_orange;
            case "Write-ups & Guides":
                return R.drawable.bg_category_purple;
            default:
                return R.drawable.bg_category_default;
        }
    }

    private String formatCount(int count) {
        if (count >= 1000) {
            return String.format("%.1fK", count / 1000.0);
        }
        return String.valueOf(count);
    }

    private boolean hasNewPostsInCategory(ForumCategory category) {
        // In a real app, you would check if user has seen latest posts
        // For now, show badge for categories with recent activity
        return category.getPostCount() > 0 &&
                (category.getName().equals("CTF Discussions") ||
                        category.getName().equals("General Discussion"));
    }

    private boolean isCategoryPinned(String categoryName) {
        // Pin important categories
        return categoryName.equals("Announcements") ||
                categoryName.equals("Getting Started");
    }
}