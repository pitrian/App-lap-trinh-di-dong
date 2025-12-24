package com.example.appattt.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appattt.R;
import com.example.appattt.adapters.RoomAdapter;
import com.example.appattt.models.Room;

import java.util.ArrayList;
import java.util.List;

public class RoomsFragment extends Fragment {

    // =========================
    // UI COMPONENTS
    // =========================
    private RecyclerView rvRooms;
    private LinearLayout layoutFilter;
    private Button btnFilter;
    private RadioGroup rgDifficulty;
    private Spinner spCategory;
    private EditText etSearch;

    // =========================
    // DATA + ADAPTER
    // =========================
    private RoomAdapter roomAdapter;
    private final List<Room> allRooms = new ArrayList<>();
    private final List<Room> filteredRooms = new ArrayList<>();

    public RoomsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_rooms, container, false);
    }

    // =========================
    // BƯỚC 4 – FILTER + UI LOGIC
    // =========================
    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        // ===== Bind UI =====
        rvRooms = view.findViewById(R.id.rvRooms);
        layoutFilter = view.findViewById(R.id.layoutFilter);
        btnFilter = view.findViewById(R.id.btnFilter);
        rgDifficulty = view.findViewById(R.id.rgDifficulty);
        spCategory = view.findViewById(R.id.spCategory);
        etSearch = view.findViewById(R.id.etSearch);

        // ===== RecyclerView =====
        rvRooms.setLayoutManager(new LinearLayoutManager(requireContext()));

        // ===== Load data =====
        loadRooms();
        filteredRooms.addAll(allRooms);

        roomAdapter = new RoomAdapter(filteredRooms);
        rvRooms.setAdapter(roomAdapter);

        // ===== Spinner Category =====
        setupCategorySpinner();

        // ===== Toggle Filter =====
        btnFilter.setOnClickListener(v -> {
            if (layoutFilter.getVisibility() == View.GONE) {
                layoutFilter.setVisibility(View.VISIBLE);
            } else {
                layoutFilter.setVisibility(View.GONE);
            }
        });

        // ===== Difficulty Filter =====
        rgDifficulty.setOnCheckedChangeListener((group, checkedId) -> applyFilters());

        // ===== Category Filter =====
        spCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    android.widget.AdapterView<?> parent,
                    View view,
                    int position,
                    long id
            ) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // ===== Search =====
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // =========================
    // APPLY FILTER LOGIC
    // =========================
    private void applyFilters() {

        filteredRooms.clear();

        // Difficulty
        String selectedDifficulty = "All";
        int checkedId = rgDifficulty.getCheckedRadioButtonId();
        if (checkedId != -1) {
            RadioButton rb = requireView().findViewById(checkedId);
            selectedDifficulty = rb.getText().toString();
        }

        // Category
        String selectedCategory = spCategory.getSelectedItem().toString();

        // Search keyword
        String keyword = etSearch.getText().toString().toLowerCase();

        for (Room room : allRooms) {

            boolean matchDifficulty =
                    selectedDifficulty.equals("All") ||
                            room.difficulty.equalsIgnoreCase(selectedDifficulty);

            boolean matchCategory =
                    selectedCategory.equals("All") ||
                            room.category.equalsIgnoreCase(selectedCategory);

            boolean matchSearch =
                    room.title.toLowerCase().contains(keyword);

            if (matchDifficulty && matchCategory && matchSearch) {
                filteredRooms.add(room);
            }
        }

        roomAdapter.notifyDataSetChanged();
    }

    // =========================
    // CATEGORY SPINNER
    // =========================
    private void setupCategorySpinner() {

        List<String> categories = new ArrayList<>();
        categories.add("All");
        categories.add("Web");
        categories.add("Linux");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);
    }

    // =========================
    // FAKE DATA – ROOM LIST
    // =========================
    private void loadRooms() {

        allRooms.clear();

        allRooms.add(new Room(
                "web-01",
                "Web Application Security",
                "Learn to identify and exploit common web vulnerabilities.",
                "Medium",
                "Web",
                68,
                false,
                500
        ));

        allRooms.add(new Room(
                "linux-01",
                "Linux Privilege Escalation",
                "Master techniques for escalating privileges.",
                "Hard",
                "Linux",
                40,
                false,
                750
        ));

        allRooms.add(new Room(
                "web-02",
                "SQL Injection Basics",
                "Understand and exploit SQL injection vulnerabilities.",
                "Easy",
                "Web",
                100,
                true,
                300
        ));
    }
}
