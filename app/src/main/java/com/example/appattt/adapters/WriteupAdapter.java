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
import com.example.appattt.models.Writeup;
import com.example.appattt.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        // Set title
        holder.tvTitle.setText(writeup.getTitle());

        // Set author
        holder.tvAuthor.setText(writeup.getAuthorName());

        // Set room and difficulty
        holder.tvRoom.setText(writeup.getRoomName());
        holder.tvDifficulty.setText(writeup.getDifficulty());

        // Set stats
        holder.tvComments.setText(String.valueOf(writeup.getComments()));
        holder.tvLikes.setText(String.valueOf(writeup.getLikes()));
        holder.tvViews.setText(String.valueOf(writeup.getViews()));

        // Set verified badge
        holder.ivVerified.setVisibility(writeup.isVerified() ? View.VISIBLE : View.GONE);

        // Set date
        if (writeup.getCreatedAt() != null) {
            // Sử dụng DateUtils thay vì SimpleDateFormat
            holder.tvDate.setText(DateUtils.getTimeAgo(writeup.getCreatedAt()));
        } else {
            holder.tvDate.setText("Recently");
        }
        // Item click
        holder.itemView.setOnClickListener(v -> {
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
        return writeupList != null ? writeupList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvRoom, tvDifficulty, tvComments, tvLikes, tvViews, tvDate;
        ImageView ivVerified, btnLike;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvRoom = itemView.findViewById(R.id.tvRoom);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvComments = itemView.findViewById(R.id.tvComments);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvViews = itemView.findViewById(R.id.tvViews);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivVerified = itemView.findViewById(R.id.ivVerified);
            btnLike = itemView.findViewById(R.id.btnLike);
        }
    }

    private int getDifficultyColor(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "beginner":
                return R.color.cyber_green_main;
            case "intermediate":
                return R.color.cyber_blue;
            case "advanced":
                return R.color.cyber_orange_premium;
            case "expert":
                return R.color.cyber_red;
            default:
                return R.color.cyber_text_secondary;
        }
    }
}