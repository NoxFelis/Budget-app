package nfelis.budget;

import android.os.Bundle;

import nfelis.budget.databinding.ActivitySettingsBinding;

public class SettingsActivity extends MainActivity {

    ActivitySettingsBinding activitySettingsBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySettingsBinding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(activitySettingsBinding.getRoot());
        allocateActivityTitle("Settings");

        if (findViewById(R.id.preferences) != null) {
            if (savedInstanceState != null) {
                return;
            }
            // below line is to inflate our fragment.
            getFragmentManager().beginTransaction().add(R.id.preferences, new SettingsFragment()).commit();
        }

    }

}