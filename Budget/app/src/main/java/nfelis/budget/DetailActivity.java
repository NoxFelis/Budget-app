package nfelis.budget;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import nfelis.budget.R;
import nfelis.budget.databinding.ActivityDetailBinding;

public class DetailActivity extends MainActivity {
    private ListView expenseListView;
    ActivityDetailBinding activityDetailBinding;
    private TextView date;
    private int thisMonth,thisYear,month,year;
    private String startDate,endDate;
    private Button nextButton;
    private CheckBox checkBox;
    private boolean isMultipleMode;
    private ConstraintLayout multipleChoiceButtons;
    private ExpenseAdapter expenseAdapter;
    private FloatingActionButton deleteButton,changeButton,rembourseButton;
    private HashMap<Integer,Expense> selectedItems;     // with the integer the position in the list view
    @SuppressLint("SimpleDateFormat")
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityDetailBinding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(activityDetailBinding.getRoot());
        allocateActivityTitle("Detail");

        initWidgets();
        getTodaysDate();
        List<String> days = Utils.setDateLimits(month,year);
        startDate = days.get(0);
        endDate = days.get(1);
    }
    private void initWidgets()
    {
        expenseListView = findViewById(R.id.expenseListView);
        expenseListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        date = findViewById(R.id.dateText);
        nextButton = findViewById(R.id.next_month);
        checkBox = findViewById(R.id.checkBox);
        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setClickable(false);
        changeButton = findViewById(R.id.changeButton);
        changeButton.setClickable(false);
        multipleChoiceButtons = findViewById(R.id.multipleChoiceButtons);
        isMultipleMode=false;
        multipleChoiceButtons.setVisibility(View.GONE);

    }
    private void setExpenseAdapter()
    {
        expenseAdapter = new ExpenseAdapter(getApplicationContext(), new ArrayList<Expense>(Expense.expenseMap.values()));
        expenseListView.setAdapter(expenseAdapter);
    }
    private void setListener()
    {
        expenseListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                Expense selectedExpense = (Expense) expenseListView.getItemAtPosition(position);
                if (!isMultipleMode) {
                    Intent editExpenseIntent = new Intent(getApplicationContext(), ExpenseEditActivity.class);
                    editExpenseIntent.putExtra(Expense.EXPENSE_EDIT_EXTRA, selectedExpense.getId());
                    startActivity(editExpenseIntent);
                } else {
                    if (expenseListView.isItemChecked(position)) {
                        selectedItems.put(position,selectedExpense);
                    } else {
                        selectedItems.remove(position);
                    }

                }
            }
        });
    }
    private void loadFromDBToMemory()
    {
        SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this);
        sqLiteManager.populateExpenseListArray(startDate,endDate);
    }
    private void getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        thisYear = cal.get(Calendar.YEAR);
        thisMonth = cal.get(Calendar.MONTH) +1;
        month = thisMonth;
        year = thisYear;
    }

    private void enableButton(boolean enabled) {
        if (enabled) {
            nextButton.setTextColor(getResources().getColor(R.color.platinum));
            nextButton.setClickable(true);
        } else {
            nextButton.setTextColor(getResources().getColor(R.color.battleshipGray));
            nextButton.setClickable(false);
        }
    }
    public void nextMonth(View view) {
        if (!(year == thisYear && month == thisMonth)) {
            if (month == 12) {
                year ++;
                month = 1;
            } else {
                month ++;
            }
            List<String> days = Utils.setDateLimits(month,year);
            startDate = days.get(0);
            endDate = days.get(1);
            date.setText(Utils.showDate(month,year));
            onResume();
        }

    }
    public void previousMonth(View view) {
        if (month == 1) {
            year --;
            month = 12;
        } else {
            month --;
        }
        List<String> days = Utils.setDateLimits(month,year);
        startDate = days.get(0);
        endDate = days.get(1);
        date.setText(Utils.showDate(month,year));
        onResume();

    }

    public void setMultiple(View view) {
        if (checkBox.isChecked()) {
            isMultipleMode= true;
            expenseListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            multipleChoiceButtons.setVisibility(View.VISIBLE);
            deleteButton.setClickable(true);
            changeButton.setClickable(true);
            selectedItems = new HashMap<>();
        } else {
            isMultipleMode=false;
            expenseListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
            expenseListView.clearChoices();
            setAllItemsChecked(false);
            multipleChoiceButtons.setVisibility(View.GONE);
            deleteButton.setClickable(false);
            changeButton.setClickable(false);
            selectedItems.clear();
        }
    }

    private void setMultiple(boolean multiple) {
        if (multiple) {
            checkBox.setChecked(true);
            isMultipleMode= true;
            expenseListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            multipleChoiceButtons.setVisibility(View.VISIBLE);
            deleteButton.setClickable(true);
            changeButton.setClickable(true);
            selectedItems = new HashMap<>();
        } else {
            checkBox.setChecked(false);
            isMultipleMode=false;
            expenseListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
            expenseListView.clearChoices();
            setAllItemsChecked(false);
            multipleChoiceButtons.setVisibility(View.GONE);
            deleteButton.setClickable(false);
            changeButton.setClickable(false);
            selectedItems.clear();
        }
    }

    public void deleteExpenses(View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create(); //Read Update
        alertDialog.setTitle("Supprimer");
        alertDialog.setMessage("Voulez-vous supprimer toutes ces dépenses?");

        alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(view.getContext());
                sqLiteManager.deleteExpensesInDB(selectedItems);
                selectedItems.clear();
                alertDialog.dismiss();
                setMultiple(false);
                setExpenseAdapter();
                onResume();
            }
        });
        alertDialog.show();  //<-- See This!
    }

    public void changeCategory(View view) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.change_dialog);

        Spinner spinner = dialog.findViewById(R.id.categorySpinner);
        CategoryAdapter categoryAdapter = new CategoryAdapter(getApplicationContext(), new ArrayList<Category>(Category.categoryMap.values()));
        spinner.setAdapter(categoryAdapter);

        Button cancel = dialog.findViewById(R.id.button_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        Button ok = dialog.findViewById(R.id.button_ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteManager expenseManager = SQLiteManager.instanceOfDatabase(getApplicationContext());
                int category = ((Category)spinner.getSelectedItem()).getId();
                expenseManager.updateExpensesInDB(selectedItems,category);
                dialog.dismiss();
                expenseAdapter.notifyDataSetChanged();
                setMultiple(false);
                onResume();
            }
        });
        dialog.show();
    }

    public void rembourse(View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create(); //Read Update
        alertDialog.setTitle("Rembourser");
        alertDialog.setMessage("Comment êtes-vous remboursé.e?");

        alertDialog.setButton3("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog.setButton2("Par cash", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SQLiteManager expenseManager = SQLiteManager.instanceOfDatabase(getApplicationContext());
                expenseManager.rembourseExpensesInDB(selectedItems,true);
                alertDialog.dismiss();
                expenseAdapter.notifyDataSetChanged();
                setMultiple(false);
                onResume();
            }
        });

        alertDialog.setButton("Par virement", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SQLiteManager expenseManager = SQLiteManager.instanceOfDatabase(getApplicationContext());
                expenseManager.rembourseExpensesInDB(selectedItems,false);
                alertDialog.dismiss();
                expenseAdapter.notifyDataSetChanged();
                setMultiple(false);
                onResume();
            }
        });
        alertDialog.show();
    }

    private void setAllItemsChecked(boolean checked) {
        int itemCount = expenseAdapter.getCount();
        for (int i = 0; i < itemCount; i++) {
            expenseListView.setItemChecked(i, checked);
        }
        expenseAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFromDBToMemory();
        setExpenseAdapter();
        enableButton(!(month==thisMonth && year==thisYear));
        setListener();
    }


}