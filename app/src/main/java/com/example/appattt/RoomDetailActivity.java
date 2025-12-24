package com.example.appattt;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.appattt.adapters.RoomDetailPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class RoomDetailActivity extends AppCompatActivity {

    private String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        // ========================
        // 1. LẤY ROOM ID
        // ========================
        roomId = getIntent().getStringExtra("ROOM_ID");
        if (roomId == null) {
            finish(); // không có room thì thoát
            return;
        }

        // ========================
        // 2. TOOLBAR (GIỐNG WEB)
        // ========================
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Room Detail");

        // ========================
        // 3. TAB + VIEWPAGER
        // ========================
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        RoomDetailPagerAdapter adapter =
                new RoomDetailPagerAdapter(this, roomId);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Overview");
                    } else {
                        tab.setText("Tasks");
                    }
                }
        ).attach();
    }

    // ========================
    // 4. BACK BUTTON
    // ========================
    @Override
    public boolean onOptionsItemSelected(
            @NonNull MenuItem item
    ) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
