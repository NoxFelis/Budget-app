package nfelis.budget;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import nfelis.budget.R;

public class ExpenseEditActivity extends AppCompatActivity {
    private EditText titreEditText,editTextNumber;
    private Spinner categorySpinner;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchCash, switchRetrait, switchRembourse,switchRembourseCash;
    private FloatingActionButton deleteButton;
    private Expense selectedExpense;
    private CategoryAdapter categoryAdapter;
    private DatePickerDialog datePickerDialog;
    private Button dateButton;
    private int day, month, year;
    @SuppressLint("SimpleDateFormat")
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_edit);

        initWidgets();
        setListeners();
        initDatePicker();
        getTodaysDate();
        dateButton.setText(Utils.makeDateString(day,month,year));
        loadFromDBToMemory();
        setAdapter();
        checkforEditExpense();
    }

    private void getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        month = month + 1;
        day = cal.get(Calendar.DAY_OF_MONTH);
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month +1;
                String date = Utils.makeDateString(day,month,year);
                dateButton.setText(date);
            }
        };
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_DARK;
        datePickerDialog = new DatePickerDialog(this, style, dateSetListener,year,month,day);
    }

    public void openDatePicker(View view) {
        datePickerDialog.show();
    }

    private void setListeners() {
        switchRetrait.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchCash.setChecked(false);  // Uncheck switch2
                }
            }
        });

        switchCash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchRetrait.setChecked(false);  // Uncheck switch2
                }
            }
        });

        switchRembourseCash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (switchRembourseCash.isChecked()) {
                    switchRembourse.setChecked(true);
                    switchRembourse.setEnabled(false);
                } else {
                    switchRembourse.setEnabled(true);
                }

            }
        });
    }

    private void checkforEditExpense() {
        Intent previousIntent = getIntent();

        int passedExpenseID = previousIntent.getIntExtra(Expense.EXPENSE_EDIT_EXTRA,-1);
        selectedExpense = Expense.getExpenseForID(passedExpenseID);

        if (selectedExpense != null) {
            titreEditText.setText(selectedExpense.getTitle());
            editTextNumber.setText(Utils.getStringFromAmount(selectedExpense.getAmount()));
            dateButton.setText(Utils.getStringFromDate(selectedExpense.getDate(),dateFormat));
            categorySpinner.setSelection(categoryAdapter.getPosition(Category.getCategoryForID(selectedExpense.getCategory())));
            switchCash.setChecked(selectedExpense.isCash());
            switchRetrait.setChecked(selectedExpense.isRetrait());
            switchRembourse.setChecked(selectedExpense.isRembourse());
            switchRembourseCash.setChecked(selectedExpense.isRembourseCash());

        } else {
            // idk
            deleteButton.setVisibility(View.INVISIBLE);
        }
    }

    private void initWidgets() {
        titreEditText = findViewById(R.id.titreEditText);
        dateButton = findViewById(R.id.dateButton);
        editTextNumber = findViewById(R.id.editTextNumber);
        categorySpinner = findViewById(R.id.categorySpinner);
        switchCash = findViewById(R.id.switchCash);
        switchRetrait = findViewById(R.id.switchRetrait);
        switchRembourse = findViewById(R.id.switchRembourse);
        switchRembourseCash = findViewById(R.id.switchRembourseCash);

        deleteButton = findViewById(R.id.deleteButton);
    }

    private void loadFromDBToMemory() {
        CategoryManager categoryManager = CategoryManager.instanceOfDatabase(this,true);
        categoryManager.populateCategorySet();
    }

    private void setAdapter() {
        categoryAdapter = new CategoryAdapter(getApplicationContext(), new ArrayList<Category>(Category.categoryMap.values()));
        categorySpinner.setAdapter(categoryAdapter);
    }
    public void saveExpense(View view)
    {
        Date date = Utils.getDateFromString(String.valueOf(dateButton.getText()),dateFormat);
        int datefull = (date.getYear()+1900)*100 + (date.getMonth()+1);
        int dateToday = year*100 + month;
        String title = String.valueOf(titreEditText.getText()).trim();
        if (datefull > dateToday) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create(); //Read Update
            alertDialog.setTitle("Date incorrecte");
            alertDialog.setMessage("Vous ne pouvez pas créer de dépense pour le mois prochain!");

            alertDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();  //<-- See This!
        } else if (title.compareTo("")==0) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create(); //Read Update
            alertDialog.setTitle("Nom vide");
            alertDialog.setMessage("Vous ne pouvez pas créer de dépense sans titre!");

            alertDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();  //<-- See This!
        } else {
            SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this,true);
            String value = String.valueOf(editTextNumber.getText());
            int amount = (value.length() == 0) ? 0 : Math.round(Float.parseFloat(value)*100) ;

            int category = ((Category) categorySpinner.getSelectedItem()).getId();
            boolean cash = switchCash.isChecked();
            boolean retrait = switchRetrait.isChecked();
            boolean rembourse = switchRembourse.isChecked();
            boolean rembourseCash = switchRembourseCash.isChecked();

            if(selectedExpense == null)
            {
                Expense newExpense = new Expense(0, title,category,amount,date,cash,retrait,rembourse,rembourseCash);

                sqLiteManager.addExpenseToDatabase(newExpense);
                if (datefull == dateToday) {
                    Expense.expenseMap.put(selectedExpense.getId(),selectedExpense);
                }
                //HomeActivity.updateValue(selectedExpense,0);
            }
            else
            {
                selectedExpense.setTitle(title);
                selectedExpense.setAmount(amount);
                selectedExpense.setCash(cash);
                selectedExpense.setRetrait(retrait);
                selectedExpense.setDate(date);
                selectedExpense.setCategory(category);
                selectedExpense.setRembourse(rembourse);
                selectedExpense.setRembourseCash(rembourseCash);
                sqLiteManager.updateExpenseInDB(selectedExpense);
                if (datefull == dateToday) {
                    Expense.expenseMap.put(selectedExpense.getId(),selectedExpense);
                }
               // HomeActivity.updateValue(selectedExpense,oldValue);
            }
            finish();
        }

    }
    public void deleteExpense(View view)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create(); //Read Update
        alertDialog.setTitle("Supprimer");
        alertDialog.setMessage("Voulez-vous supprimer cette dépense?");

        alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(getApplicationContext(),true);
                sqLiteManager.deleteExpenseInDB(selectedExpense);
                finish();
            }
        });
        alertDialog.show();  //<-- See This!
    }
    public void cancel(View view) {
        String title = String.valueOf(titreEditText.getText()).trim();
        String value = String.valueOf(editTextNumber.getText());
        int amount = (value.length() == 0) ? 0 : Math.round(Float.parseFloat(value)*100) ;
        int category = ((Category) categorySpinner.getSelectedItem()).getId();
        boolean cash = switchCash.isChecked();
        boolean retrait = switchRetrait.isChecked();
        boolean rembourse = switchRembourse.isChecked();
        boolean rembourseCash = switchRembourseCash.isChecked();

        if ((selectedExpense == null && (title.length() != 0 || value.length() !=0 || category != Category.getIdAutre() || cash || retrait || rembourse || rembourseCash))
                || (selectedExpense != null && (selectedExpense.getTitle().compareTo(title)!=0 || selectedExpense.getAmount() != amount
                    || selectedExpense.getCategory() != category || selectedExpense.isCash() != cash || selectedExpense.isRetrait() != retrait
                        || selectedExpense.isRembourse() != rembourse || selectedExpense.isRembourseCash() != rembourseCash))) {
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

