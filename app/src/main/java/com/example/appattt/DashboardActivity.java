package com.example.appattt;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.appattt.forum.ForumHomeActivity;
import com.example.appattt.fragments.RoomsFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);

        ImageView menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        setupNavigationListener();
    }

    // ===============================
    // MENU DRAWER HANDLING
    // ===============================
    private void setupNavigationListener() {
        navView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {

                showDashboard();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            } else if (id == R.id.nav_rooms) {

                openFragment(new RoomsFragment());
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            } else if (id == R.id.nav_forum) {

                startActivity(new Intent(this, ForumHomeActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            } else if (id == R.id.nav_writeup) {

                Toast.makeText(this,
                        "Chức năng Writeup đang được phát triển!",
                        Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            } else if (id == R.id.nav_logout) {

                doLogout();
                return true;
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    // ===============================
    // BƯỚC 3 – HÀM MỞ FRAGMENT (QUAN TRỌNG NHẤT)
    // ===============================
    public void openFragment(Fragment fragment) {

        // 1. Ẩn dashboard (ScrollView)
        View dashboardContent = findViewById(R.id.dashboard_content);
        if (dashboardContent != null) {
            dashboardContent.setVisibility(View.GONE);
        }

        // 2. Hiện fragment container
        View fragmentContainer = findViewById(R.id.fragment_container);
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
        }

        // 3. Replace Fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    // ===============================
    // QUAY VỀ DASHBOARD
    // ===============================
    private void showDashboard() {

        View dashboardContent = findViewById(R.id.dashboard_content);
        View fragmentContainer = findViewById(R.id.fragment_container);

        if (dashboardContent != null) {
            dashboardContent.setVisibility(View.VISIBLE);
        }

        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.GONE);
        }

        getSupportFragmentManager().popBackStack();
    }

    // ===============================
    // LOGOUT
    // ===============================
    private void doLogout() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ===============================
    // BACK BUTTON
    // ===============================
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            showDashboard();
            return;
        }

        super.onBackPressed();
    }
}
