package nfelis.budget;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;

import java.io.File;
import java.net.URISyntaxException;

public class SettingsFragment extends PreferenceFragment {
    private static final int REQUEST_CODE_FILE_CHOOSER = 1001;
    Preference folderPicker;
    Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // below line is used to add preference
        // fragment from our xml folder.
        addPreferencesFromResource(R.xml.preferences);
        initWidgets();
        fillWidgets();
        setOnClickListeners();
    }

    private void initWidgets() {
        folderPicker = (Preference) findPreference("folderPicker");
        context = getContext();
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
    }

    private void fillWidgets() {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String path = preferences.getString("storage_location", null);
        folderPicker.setSummary(path);
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
