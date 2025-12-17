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
import com.example.appattt.models.Writeup;
import com.example.appattt.utils.DateUtils;

import java.util.List;

public class WriteupAdapter extends RecyclerView.Adapter<WriteupAdapter.ViewHolder> {

    private Context context;
    private List<Writeup> writeupList;
    private OnWriteupClickListener listener;

    public interface OnWriteupClickListener {
        void onWriteupClick(Writeup writeup);
        void onAuthorClick(String authorId);
        void onLikeClick(Writeup writeup);
    }

    public WriteupAdapter(Context context, List<Writeup> writeupList) {
        this.context = context;
        this.writeupList = writeupList;
    }

    public void setOnWriteupClickListener(OnWriteupClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<Writeup> newList) {
        this.writeupList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_writeup, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Writeup writeup = writeupList.get(position);

        holder.tvTitle.setText(writeup.getTitle());
        holder.tvAuthor.setText(writeup.getAuthorName());
        holder.tvRoom.setText(writeup.getRoomName());
        holder.tvDifficulty.setText(writeup.getDifficulty());
        holder.tvLikes.setText(String.valueOf(writeup.getLikes()));
        holder.tvViews.setText(String.valueOf(writeup.getViews()));
        holder.tvComments.setText(String.valueOf(writeup.getComments()));
        holder.tvTime.setText(DateUtils.getTimeAgo(writeup.getCreatedAt()));

        // Set difficulty color
        int difficultyColor = getDifficultyColor(writeup.getDifficulty());
        holder.tvDifficulty.setTextColor(context.getResources().getColor(difficultyColor));
        holder.tvDifficulty.setBackgroundResource(getDifficultyBackground(writeup.getDifficulty()));

        // Set featured badge
        if (writeup.isFeatured()) {
            holder.tvFeatured.setVisibility(View.VISIBLE);
        } else {
            holder.tvFeatured.setVisibility(View.GONE);
        }

        // Set verified badge
        if (writeup.isVerified()) {
            holder.ivVerified.setVisibility(View.VISIBLE);
        } else {
            holder.ivVerified.setVisibility(View.GONE);
        }

        // Set tags
        if (writeup.getTags() != null && !writeup.getTags().isEmpty()) {
            StringBuilder tags = new StringBuilder();
            for (String tag : writeup.getTags()) {
                tags.append("#").append(tag).append(" ");
            }
            holder.tvTags.setText(tags.toString().trim());
            holder.tvTags.setVisibility(View.VISIBLE);
        } else {
            holder.tvTags.setVisibility(View.GONE);
        }

        // Item click
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWriteupClick(writeup);
            }
        });

        // Author click
        holder.tvAuthor.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAuthorClick(writeup.getAuthorId());
            }
        });

        // Like click
        holder.btnLike.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLikeClick(writeup);
            }
        });
    }

    @Override
    public int getItemCount() {
        return writeupList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle, tvAuthor, tvRoom, tvDifficulty, tvLikes, tvViews, tvComments, tvTime, tvTags, tvFeatured;
        ImageView ivVerified, btnLike;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvRoom = itemView.findViewById(R.id.tvRoom);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvViews = itemView.findViewById(R.id.tvViews);
            tvComments = itemView.findViewById(R.id.tvComments);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTags = itemView.findViewById(R.id.tvTags);
            tvFeatured = itemView.findViewById(R.id.tvFeatured);
            ivVerified = itemView.findViewById(R.id.ivVerified);
            btnLike = itemView.findViewById(R.id.btnLike);
        }
    }

    private int getDifficultyColor(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "beginner":
                return R.color.cyber_green_main;
            case "intermediate":
                return R.color.cyber_orange_premium;
            case "advanced":
                return R.color.cyber_red;
            case "expert":
                return R.color.cyber_purple;
            default:
                return R.color.cyber_text_secondary;
        }
    }

    private int getDifficultyBackground(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "beginner":
                return R.drawable.bg_difficulty_beginner;
            case "intermediate":
                return R.drawable.bg_difficulty_intermediate;
            case "advanced":
                return R.drawable.bg_difficulty_advanced;
            default:
                return R.drawable.bg_difficulty_default;
        }
    }
}