package nfelis.budget;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import nfelis.budget.R;

public class CategoryEditActivity extends AppCompatActivity {

    private TextView nameText;
    private EditText amountEdit,colorEdit,nameEdit;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchVisible,switchInBudget;
    private View colorView;
    private FloatingActionButton deleteButton;
    private Category selectedCategory;
    private boolean percent;
    private Button amountButton;
    private int maxDepense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_edit);

        Context context = getApplicationContext();
        SharedPreferences preferences = getSharedPreferences(context.getString(R.string.prefName), MODE_PRIVATE);
        percent = preferences.getBoolean(context.getString(R.string.percentage), false);
        maxDepense = preferences.getInt(context.getString(R.string.total),Integer.parseInt(context.getString(R.string.default_total)));

        initWidgets();
        checkForEditCategory();
    }

    private void initWidgets() {
        nameText = findViewById(R.id.nameText);
        nameEdit = findViewById(R.id.nameEdit);
        amountEdit = findViewById(R.id.amountEdit);
        switchVisible = findViewById(R.id.switchVisible);
        switchInBudget = findViewById(R.id.switchInBudget);
        colorEdit = findViewById(R.id.colorEdit);
        colorView = findViewById(R.id.colorView);
        amountButton = findViewById(R.id.amountButton);

        deleteButton = findViewById(R.id.deleteButton);
    }

    private void setVisibility(Boolean inBudget) {
        if (percent && inBudget) {
            amountButton.setVisibility(View.VISIBLE);
            amountEdit.setVisibility(View.GONE);
        } else {
            amountButton.setVisibility(View.GONE);
            amountEdit.setVisibility(View.VISIBLE);
        }
    }

    private void checkForEditCategory() {
        Intent previousIntent = getIntent();

        int passedCategoryID = previousIntent.getIntExtra(Category.CATEGORY_EDIT_EXTRA,-1);
        selectedCategory = Category.getCategoryForID(passedCategoryID);


        if (selectedCategory != null) {
            boolean inBudget = selectedCategory.isInBudget();
            setVisibility(inBudget);
            nameEdit.setVisibility(View.GONE);

            nameText.setText(selectedCategory.getName());
            if (percent && inBudget) {
                int value = Math.round((float) selectedCategory.getAmount()*100/maxDepense);
                amountButton.setText(String.valueOf(value));
            } else {
                amountEdit.setText(String.valueOf(selectedCategory.getAmount()));
            }
            switchVisible.setChecked(selectedCategory.isVisible());
            switchInBudget.setChecked(selectedCategory.isInBudget());
            colorEdit.setText(selectedCategory.getColor());
            colorView.setBackgroundColor(Color.parseColor(selectedCategory.getColor()));
            if (0 == selectedCategory.getName().compareTo("Autre")) {
                deleteButton.setVisibility(View.GONE);
            }
        } else {
            setVisibility(false);
            nameText.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        }
    }

    public void saveCategory(View view) {
        CategoryManager categoryManager = CategoryManager.instanceOfDatabase(this,true);
        String name = String.valueOf(nameEdit.getText()).trim();
        boolean checked = switchInBudget.isChecked();

        int amount;
        if (percent && checked) {
            int value = Integer.parseInt(String.valueOf(amountButton.getText()));
            amount = Math.round((float) value*maxDepense/100);
        } else {
            String value = String.valueOf(amountEdit.getText());
            amount = (value.length() == 0) ? 0 : Integer.parseInt(value);
        }
        boolean visible = switchVisible.isChecked();
        String color = Utils.returnHEX(String.valueOf(colorEdit.getText()));

        try{
            Color.parseColor(color);
            if (selectedCategory == null) {
                if (name.compareTo("") == 0) {
                    AlertDialog alertDialog = new AlertDialog.Builder(this).create(); //Read Update
                    alertDialog.setTitle("Nom vide");
                    alertDialog.setMessage("Vous ne pouvez pas créer de catégorie sans titre!");

                    alertDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();  //<-- See This
                } else {
                    Category category = new Category(0, name, amount, color, visible, checked);
                    categoryManager.addCategoryToDatabase(category);
                    if (percent && !checked) {
                        Category.categoryNonMap.put(category.getId(),category);
                    } else {
                        Category.categoryMap.put(category.getId(), category);
                    }
                }
            } else {
                boolean previous = selectedCategory.isInBudget();
                selectedCategory.setAmount(amount);
                selectedCategory.setColor(color);
                selectedCategory.setVisible(visible);
                selectedCategory.setInBudget(checked);
                int id = selectedCategory.getId();
                if (percent && !checked) {
                    categoryManager.updateCategoryInDB(selectedCategory);
                    Category.categoryNonMap.put(id,selectedCategory);
                    if (previous) {
                        Category.categoryMap.remove(id);
                    }
                } else {
                    Category.categoryMap.put(id,selectedCategory);
                    if (percent && !previous) {
                        selectedCategory.setAmount(0);
                        Category.categoryNonMap.remove(id);
                    }
                    categoryManager.updateCategoryInDB(selectedCategory);
                }

            }
            finish();
        } catch (IllegalArgumentException e) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create(); //Read Update
            alertDialog.setTitle("Couleur invalide");
            alertDialog.setMessage("Vous devez entrer une couleur en HEX valide!");
            alertDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();  //<-- See This
        }
    }


    public void deleteCategory(View view) {
        CategoryManager categoryManager = CategoryManager.instanceOfDatabase(this,true);
        categoryManager.deleteCategoryInDB(selectedCategory);

        SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this,true);
        sqLiteManager.deleteCategoryInDB(selectedCategory);
        finish();
    }

    public void cancel(View view) {
        String title = String.valueOf(nameEdit.getText()).trim();
        String value = String.valueOf(amountEdit.getText());
        String color = Utils.returnHEX(String.valueOf(colorEdit.getText()));
        int amount;
        if (percent) {
            amount = Math.round(Integer.parseInt(String.valueOf(amountButton.getText()))*maxDepense/100);
        } else {
            amount = (value.length() == 0) ? 0 : Math.round(Float.parseFloat(value)*100) ;
        }
        boolean visible = switchVisible.isChecked();
        boolean inBudget = switchInBudget.isChecked();

        if ((selectedCategory == null && (title.length() != 0 || value.length() !=0 || inBudget || visible || color.length() != 0))
                || (selectedCategory != null && ( selectedCategory.getAmount() != amount || selectedCategory.isInBudget() != inBudget || selectedCategory.isVisible() != visible || selectedCategory.getColor().compareTo(color) != 0))) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create(); //Read Update
            alertDialog.setTitle("Retour");
            alertDialog.setMessage("Voulez-vous retourner sans sauvegarder??");

            alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.setButton("Oui", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    onBackPressed();
                }
            });
            alertDialog.show();  //<-- See This!
        } else {
            onBackPressed();
        }

    }

    public void openPercentagePicker(View view) {
        Context context = getApplicationContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialogue_percentage_picker, null);
        NumberPicker percentagePicker = dialogView.findViewById(R.id.percentagePicker);
        percentagePicker.setMinValue(0);
        percentagePicker.setMaxValue(Utils.getResteDepense(context,selectedCategory.getId()));
        percentagePicker.setValue(Utils.getPercentage(context,selectedCategory.getAmount()));
        percentagePicker.setWrapSelectorWheel(false); // To block scrolling beyond min/max values

        builder.setTitle("Select Percentage")
                .setView(dialogView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedPercentage = percentagePicker.getValue();
                        amountButton.setText(String.valueOf(selectedPercentage));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}