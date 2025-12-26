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
import java.util.List;

public class HotThreadAdapter extends RecyclerView.Adapter<HotThreadAdapter.ViewHolder> {

    private Context context;
    private List<ForumThread> threadList;
    private OnThreadClickListener listener;

    public interface OnThreadClickListener {
        void onThreadClick(ForumThread thread);
    }

    public HotThreadAdapter(Context context, List<ForumThread> threadList) {
        this.context = context;
        this.threadList = threadList;
    }

    public void setOnThreadClickListener(OnThreadClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hot_thread, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForumThread thread = threadList.get(position);

        // Set title
        holder.tvTitle.setText(thread.getTitle());

        // Set author and category
        holder.tvAuthor.setText(thread.getAuthorName());
        holder.tvCategory.setText(thread.getCategoryName());

        // Set stats
        holder.tvReplies.setText(String.valueOf(thread.getReplyCount()));
        holder.tvViews.setText(formatCount(thread.getViews()));
        holder.tvUpvotes.setText(String.valueOf(thread.getUpvotes()));

        // Show solved badge
        holder.ivSolved.setVisibility(thread.isSolved() ? View.VISIBLE : View.GONE);

        // Show hot badge
        holder.ivHotBadge.setVisibility(thread.isHot() ? View.VISIBLE : View.GONE);

        // Set category color
        int cardColor = getCategoryColor(thread.getCategoryName());
        holder.cardView.setCardBackgroundColor(Color.parseColor("#1C2128"));

        // Item click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onThreadClick(thread);
            }
        });
    }

    @Override
    public int getItemCount() {
        return threadList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle, tvAuthor, tvCategory, tvReplies, tvViews, tvUpvotes;
        ImageView ivSolved, ivHotBadge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardHotThread);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvReplies = itemView.findViewById(R.id.tvReplies);
            tvViews = itemView.findViewById(R.id.tvViews);
            tvUpvotes = itemView.findViewById(R.id.tvUpvotes);
            ivSolved = itemView.findViewById(R.id.ivSolved);
            ivHotBadge = itemView.findViewById(R.id.ivHotBadge);
        }
    }

    private String formatCount(int count) {
        if (count >= 1000) {
            return String.format("%.1fK", count / 1000.0);
        }
        return String.valueOf(count);
    }

    private int getCategoryColor(String categoryName) {
        switch (categoryName) {
            case "CTF Discussions":
                return Color.parseColor("#39BF8F");
            case "Write-ups & Guides":
                return Color.parseColor("#8B5CF6");
            case "General Discussion":
                return Color.parseColor("#00A8FF");
            case "Room Help & Tips":
                return Color.parseColor("#39BF8F");
            default:
                return Color.parseColor("#8B949E");
        }
    }
}