package com.example.appattt.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appattt.R;
import com.example.appattt.RoomDetailActivity;

import com.example.appattt.models.Room;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    private List<Room> rooms;

    public RoomAdapter(List<Room> rooms) {
        this.rooms = rooms;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Room r = rooms.get(position);

        holder.title.setText(r.title);
        holder.desc.setText(r.description);
        holder.diff.setText(r.difficulty);
        holder.progress.setText(r.progress + "% completion");

        // =====================
        // 🔥 CLICK ROOM
        // =====================
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, RoomDetailActivity.class);
            intent.putExtra("ROOM_ID", r.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, desc, diff, progress;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            desc = itemView.findViewById(R.id.tvDesc);
            diff = itemView.findViewById(R.id.tvDifficulty);
            progress = itemView.findViewById(R.id.tvProgress);
        }
    }
}
