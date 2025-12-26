package com.example.appattt.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appattt.R;

public class RoomOverviewFragment extends Fragment {

    private static final String ARG_ROOM_ID = "ROOM_ID";

    public static RoomOverviewFragment newInstance(String roomId) {
        RoomOverviewFragment fragment = new RoomOverviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_ID, roomId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_room_overview,
                container,
                false
        );
    }
}
