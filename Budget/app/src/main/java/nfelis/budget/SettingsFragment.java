package nfelis.budget;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;


import java.io.File;
import java.net.URISyntaxException;

public class SettingsFragment extends PreferenceFragment {
    private static final int REQUEST_CODE_FILE_CHOOSER = 1001;
    private static String PREFS_NAME, PERCENTAGE;
    Preference folderPicker;
    SwitchPreference percentage;
    EditTextPreference total;
    Context context;
    private static SharedPreferences preferences;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        initWidgets();
        fillWidgets();
        setOnClickListeners();
    }

    private void initWidgets() {
        context = getContext();
        PREFS_NAME = context.getString(R.string.prefName);
        PERCENTAGE = context.getString(R.string.percentage);

        folderPicker = (Preference) findPreference("folderPicker");
        percentage = (SwitchPreference) findPreference(PERCENTAGE);
        total = (EditTextPreference) findPreference(context.getString(R.string.total));

        preferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void setOnClickListeners() {
        folderPicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(intent, REQUEST_CODE_FILE_CHOOSER);
                return true;
            }
        });

        percentage.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                boolean checked = (boolean) newValue;
                if (checked) {
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create(); //Read Update
                    alertDialog.setTitle("Passer en pourcentage");
                    alertDialog.setMessage("Si vous continuez, les catégories comptabilisées seront toutes mises à zéros");

                    alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.setButton("Continuer", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            CategoryManager categoryManager = CategoryManager.instanceOfDatabase(context,true);
                            categoryManager.setBudget0();
                        }
                    });
                    alertDialog.show();  //<-- See This!
                }
                total.setEnabled(checked);

                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PERCENTAGE,checked);
                editor.apply();
                return true;
            }
        });

        total.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                int value = Integer.parseInt(((String) newValue));
                String TOTAL = context.getString(R.string.total);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(TOTAL,value);
                editor.apply();
                return true;
            }
        });
    }

    private void fillWidgets() {
        String path = preferences.getString("storage_location", null);
        folderPicker.setSummary(path);

        boolean checked = preferences.getBoolean(PERCENTAGE,false);
        percentage.setChecked(checked);
        total.setEnabled(checked);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_FILE_CHOOSER && resultCode == RESULT_OK) {
            Uri treeUri = data.getData();
            // Handle the selected location URI (treeUri) here
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(false);  // Disable dismiss behavior
                builder.setMessage("Select a storage location for your data");

                LayoutInflater inflater = LayoutInflater.from(context);
                /*View dialogView = inflater.inflate(R.layout.custom_dialog_layout, null);  // Replace with your custom layout
                // Initialize your UI components and set up any necessary listeners
                builder.setView(dialogView);*/
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                // Perform your long-running task here
                String path = Utils.getPath(context,treeUri);
                if (!Utils.comparePaths(context,path)) {
                    Utils.transferExpenses(context,path+"/Expenses.db");
                    Utils.transferCategories(context,path+"/Categories.db");
                    Utils.saveStorageLocation(context,"storage_location",null,path);
                    File file = new File(path);
                    file.delete();
                }

                alertDialog.dismiss();
                /*Utils.saveStorageLocation(getContext(),"storage_expenses", treeUri, "/Expenses.db");
                Utils.saveStorageLocation(getContext(),"storage_categories", treeUri,"/Categories.db");*/
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
