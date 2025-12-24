package com.example.appattt.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.appattt.fragments.RoomOverviewFragment;
import com.example.appattt.fragments.RoomTasksFragment;

public class RoomDetailPagerAdapter extends FragmentStateAdapter {

    private final String roomId;

    public RoomDetailPagerAdapter(
            @NonNull FragmentActivity activity,
            String roomId
    ) {
        super(activity);
        this.roomId = roomId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return RoomOverviewFragment.newInstance(roomId);
        } else {
            return RoomTasksFragment.newInstance(roomId);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Overview + Tasks
    }
}
