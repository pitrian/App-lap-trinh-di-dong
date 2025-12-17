package com.example.appattt.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.appattt.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class FilterDialogFragment extends DialogFragment {

    private RadioGroup radioGroupSort, radioGroupTime;
    private CheckBox cbSolved, cbPinned, cbNoReplies;
    private ChipGroup chipGroupDifficulties, chipGroupCategories;
    private Button btnApply, btnReset, btnCancel;

    private FilterListener listener;
    private String currentSort = "newest";
    private String currentTimeFilter = "all";
    private List<String> selectedDifficulties = new ArrayList<>();
    private List<String> selectedCategories = new ArrayList<>();
    private boolean showSolved = false;
    private boolean showPinned = false;
    private boolean showNoReplies = false;

    public interface FilterListener {
        void onFilterApplied(String sortBy, String timeFilter, List<String> difficulties,
                             List<String> categories, boolean showSolved,
                             boolean showPinned, boolean showNoReplies);
    }

    public static FilterDialogFragment newInstance(String currentSort, String currentTimeFilter) {
        FilterDialogFragment fragment = new FilterDialogFragment();
        Bundle args = new Bundle();
        args.putString("sort", currentSort);
        args.putString("time", currentTimeFilter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentSort = getArguments().getString("sort", "newest");
            currentTimeFilter = getArguments().getString("time", "all");
        }

        // Set dialog style
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_dialog, container, false);

        initViews(view);
        setupListeners();
        setupChips();
        setCurrentSelections();

        return view;
    }

    private void initViews(View view) {
        radioGroupSort = view.findViewById(R.id.radioGroupSort);
        radioGroupTime = view.findViewById(R.id.radioGroupTime);
        cbSolved = view.findViewById(R.id.cbSolved);
        cbPinned = view.findViewById(R.id.cbPinned);
        cbNoReplies = view.findViewById(R.id.cbNoReplies);
        chipGroupDifficulties = view.findViewById(R.id.chipGroupDifficulties);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        btnApply = view.findViewById(R.id.btnApply);
        btnReset = view.findViewById(R.id.btnReset);
        btnCancel = view.findViewById(R.id.btnCancel);
    }

    private void setupListeners() {
        btnApply.setOnClickListener(v -> applyFilters());
        btnReset.setOnClickListener(v -> resetFilters());
        btnCancel.setOnClickListener(v -> dismiss());

        // Difficulty chips selection
        for (int i = 0; i < chipGroupDifficulties.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupDifficulties.getChildAt(i);
            chip.setOnClickListener(v -> {
                String difficulty = chip.getText().toString();
                if (chip.isChecked()) {
                    selectedDifficulties.add(difficulty);
                } else {
                    selectedDifficulties.remove(difficulty);
                }
            });
        }

        // Category chips selection
        for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCategories.getChildAt(i);
            chip.setOnClickListener(v -> {
                String category = chip.getText().toString();
                if (chip.isChecked()) {
                    selectedCategories.add(category);
                } else {
                    selectedCategories.remove(category);
                }
            });
        }
    }

    private void setupChips() {
        // Add difficulty chips
        String[] difficulties = {"Beginner", "Intermediate", "Advanced", "Expert"};
        for (String difficulty : difficulties) {
            Chip chip = new Chip(requireContext());
            chip.setText(difficulty);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.cyber_card_background);
            chip.setCheckedIconResource(R.drawable.ic_check);
            chipGroupDifficulties.addView(chip);
        }

        // Add category chips
        String[] categories = {"Web Security", "Network", "Cryptography", "Forensics",
                "Reverse Engineering", "Binary", "Mobile", "Cloud", "IoT"};
        for (String category : categories) {
            Chip chip = new Chip(requireContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.cyber_card_background);
            chip.setCheckedIconResource(R.drawable.ic_check);
            chipGroupCategories.addView(chip);
        }
    }

    private void setCurrentSelections() {
        // Set sort radio button
        int sortId = getSortRadioId(currentSort);
        if (sortId != -1) {
            radioGroupSort.check(sortId);
        }

        // Set time filter radio button
        int timeId = getTimeRadioId(currentTimeFilter);
        if (timeId != -1) {
            radioGroupTime.check(timeId);
        }

        // Set checkboxes (default to false)
        cbSolved.setChecked(showSolved);
        cbPinned.setChecked(showPinned);
        cbNoReplies.setChecked(showNoReplies);
    }

    private int getSortRadioId(String sort) {
        switch (sort) {
            case "newest":
                return R.id.radioNewest;
            case "popular":
                return R.id.radioPopular;
            case "most_commented":
                return R.id.radioMostCommented;
            case "trending":
                return R.id.radioTrending;
            default:
                return -1;
        }
    }

    private int getTimeRadioId(String time) {
        switch (time) {
            case "today":
                return R.id.radioToday;
            case "week":
                return R.id.radioWeek;
            case "month":
                return R.id.radioMonth;
            case "year":
                return R.id.radioYear;
            case "all":
            default:
                return R.id.radioAllTime;
        }
    }

    private void applyFilters() {
        // Get sort selection
        int selectedSortId = radioGroupSort.getCheckedRadioButtonId();
        String sortBy = "newest";
        if (selectedSortId == R.id.radioNewest) {
            sortBy = "newest";
        } else if (selectedSortId == R.id.radioPopular) {
            sortBy = "popular";
        } else if (selectedSortId == R.id.radioMostCommented) {
            sortBy = "most_commented";
        } else if (selectedSortId == R.id.radioTrending) {
            sortBy = "trending";
        }

        // Get time filter
        int selectedTimeId = radioGroupTime.getCheckedRadioButtonId();
        String timeFilter = "all";
        if (selectedTimeId == R.id.radioToday) {
            timeFilter = "today";
        } else if (selectedTimeId == R.id.radioWeek) {
            timeFilter = "week";
        } else if (selectedTimeId == R.id.radioMonth) {
            timeFilter = "month";
        } else if (selectedTimeId == R.id.radioYear) {
            timeFilter = "year";
        } else if (selectedTimeId == R.id.radioAllTime) {
            timeFilter = "all";
        }

        // Get checkbox states
        showSolved = cbSolved.isChecked();
        showPinned = cbPinned.isChecked();
        showNoReplies = cbNoReplies.isChecked();

        // Apply filters through listener
        if (listener != null) {
            listener.onFilterApplied(sortBy, timeFilter, selectedDifficulties,
                    selectedCategories, showSolved, showPinned, showNoReplies);
        }

        dismiss();
    }

    private void resetFilters() {
        // Reset to defaults
        radioGroupSort.check(R.id.radioNewest);
        radioGroupTime.check(R.id.radioAllTime);
        cbSolved.setChecked(false);
        cbPinned.setChecked(false);
        cbNoReplies.setChecked(false);

        // Clear chip selections
        selectedDifficulties.clear();
        selectedCategories.clear();

        for (int i = 0; i < chipGroupDifficulties.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupDifficulties.getChildAt(i);
            chip.setChecked(false);
        }

        for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCategories.getChildAt(i);
            chip.setChecked(false);
        }

        Toast.makeText(getContext(), "Filters reset", Toast.LENGTH_SHORT).show();
    }

    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }
}