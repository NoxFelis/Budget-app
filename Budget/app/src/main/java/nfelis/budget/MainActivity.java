package nfelis.budget;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;


import com.google.android.material.navigation.NavigationView;

import nfelis.budget.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);
    }

    @Override
    public void setContentView(View view) {
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawer_base,null);
        FrameLayout container = drawerLayout.findViewById(R.id.activityContainer);
        container.addView(view);
        super.setContentView(drawerLayout);

        Toolbar toolbar =drawerLayout.findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = drawerLayout.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.menu_drawer_open,R.string.menu_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        if  (item.getItemId() == R.id.nav_home) {
            startActivity(new Intent(this, HomeActivity.class));
            overridePendingTransition(0, 0 );
        } else if (item.getItemId() == R.id.nav_detail) {
            startActivity(new Intent(this, DetailActivity.class));
            overridePendingTransition(0, 0 );
        } else if (item.getItemId() == R.id.nav_mois) {
            startActivity(new Intent(this, MoisActivity.class));
            overridePendingTransition(0, 0 );
        } else if (item.getItemId() == R.id.nav_periode) {
            startActivity(new Intent(this, PeriodeActivity.class));
            overridePendingTransition(0, 0 );
        } else if (item.getItemId() == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(0, 0);
        } else if (item.getItemId() == R.id.nav_category) {
            startActivity(new Intent(this, CategoryActivity.class));
            overridePendingTransition(0, 0);
        }
        return false;

    }

    protected void allocateActivityTitle(String titleString) {
        getSupportActionBar().setTitle(titleString);
    }

}