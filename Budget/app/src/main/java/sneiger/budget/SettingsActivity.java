package sneiger.budget;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import sneiger.budget.databinding.ActivityDetailBinding;
import sneiger.budget.databinding.ActivityHomeBinding;
import sneiger.budget.databinding.ActivitySettingsBinding;

public class SettingsActivity extends MainActivity {

    ActivitySettingsBinding activitySettingsBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySettingsBinding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(activitySettingsBinding.getRoot());
        allocateActivityTitle("Settings");

    }

}