package nfelis.budget;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_edit);

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

        deleteButton = findViewById(R.id.deleteButton);
    }

    private void checkForEditCategory() {
        Intent previousIntent = getIntent();

        int passedCategoryID = previousIntent.getIntExtra(Category.CATEGORY_EDIT_EXTRA,-1);
        selectedCategory = Category.getCategoryForID(passedCategoryID);

        if (selectedCategory != null) {
            nameEdit.setVisibility(View.INVISIBLE);

            nameText.setText(selectedCategory.getName());
            amountEdit.setText(String.valueOf(selectedCategory.getAmount()));
            switchVisible.setChecked(selectedCategory.isVisible());
            switchInBudget.setChecked(selectedCategory.isInBudget());
            colorEdit.setText(selectedCategory.getColor());
            colorView.setBackgroundColor(Color.parseColor(selectedCategory.getColor()));
            if (0 == selectedCategory.getName().compareTo("Autre")) {
                deleteButton.setVisibility(View.INVISIBLE);
            }
        } else {
            nameText.setVisibility(View.INVISIBLE);
            deleteButton.setVisibility(View.INVISIBLE);
        }
    }

    public void saveCategory(View view) {
        CategoryManager categoryManager = CategoryManager.instanceOfDatabase(this,true);
        String name = String.valueOf(nameEdit.getText()).trim();
        String value = String.valueOf(amountEdit.getText());
        int amount = (value.length() == 0) ? 0 : Integer.parseInt(value);
        boolean visible = switchVisible.isChecked();
        boolean inBudget = switchInBudget.isChecked();
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
                    Category category = new Category(0, name, amount, color, visible, inBudget);
                    categoryManager.addCategoryToDatabase(category);
                    Category.categoryMap.put(category.getId(), category);
                }
            } else {
                selectedCategory.setAmount(amount);
                selectedCategory.setColor(color);
                selectedCategory.setVisible(visible);
                selectedCategory.setInBudget(inBudget);
                categoryManager.updateCategoryInDB(selectedCategory);
                Category.categoryMap.put(selectedCategory.getId(),selectedCategory);
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
        int amount = (value.length() == 0) ? 0 : Math.round(Float.parseFloat(value)*100) ;
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
}