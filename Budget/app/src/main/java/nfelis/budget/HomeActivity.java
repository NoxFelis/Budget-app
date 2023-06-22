package nfelis.budget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.graphics.PathUtils;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import nfelis.budget.R;
import nfelis.budget.databinding.ActivityHomeBinding;

public class HomeActivity extends MainActivity {
    private static final int REQUEST_CODE_FILE_CHOOSER = 1001;
    private static boolean isFirstLaunch,visualCash;
    private static String PREFS_NAME,SUBSCRIPTION,CASHVISIBLE;
    private static final String PREF_FIRST_LAUNCH = "FirstLaunch";
    private PieChart pieChart;
    private LinkedHashMap<String,Integer> colors;
    private LinkedHashMap<String,PieEntry> pieEntryMap;
    ActivityHomeBinding activityHomeBinding;
    private String startDate,endDate;
    private TextView valueRest, textArgent,textCash,cashValue;
    private int maxDepense,subscriptionMonths,month;
    private ProgressBar resteBudget;
    private ListView budgetListView;
    private SQLiteManager sqLiteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityHomeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(activityHomeBinding.getRoot());
        allocateActivityTitle("Home");

        PREFS_NAME = getApplicationContext().getString(R.string.prefName);
        CASHVISIBLE = getApplicationContext().getString(R.string.cashVisible);
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isFirstLaunch = preferences.getBoolean(PREF_FIRST_LAUNCH, true);
        visualCash = preferences.getBoolean(CASHVISIBLE,false);

        if (isFirstLaunch) {
            // Show the storage location dialog
            showStorageLocationDialog();

            // Once the dialog is shown, update the flag to indicate it has been shown
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(PREF_FIRST_LAUNCH, false);
            editor.apply();
        } else {
            initWidgets();
            setVisibility();
            setDaysLimits();
            loadFromDBToMemory();
            getMaxDepense();
        }


        spendSubscriptions();

    }



    private void loadFromDBToMemory() {
        CategoryManager categoryManager = CategoryManager.instanceOfDatabase(this);
        categoryManager.populateCategorySet(false);

        sqLiteManager = SQLiteManager.instanceOfDatabase(this);
        sqLiteManager.populateExpenseListArray(startDate,endDate);
    }

    private void getMaxDepense() {
        maxDepense = Utils.getMaxDepense(getApplicationContext());
        resteBudget.setMax(maxDepense);
    }


    private void setDaysLimits() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH) +1;
        List<String> days = Utils.setDateLimits(month,year);
        startDate = days.get(0);
        endDate = days.get(1);
    }

    private void initWidgets() {
        pieChart = findViewById(R.id.chart);
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(false);
        pieChart.setDrawHoleEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.getDescription().setEnabled(false);

        colors = new LinkedHashMap<>();

        valueRest = findViewById(R.id.valueRest);
        resteBudget = findViewById(R.id.resteBudget);
        textArgent = findViewById(R.id.textArgent);
        budgetListView = findViewById(R.id.budgetListView);
        textCash = findViewById(R.id.textCash);
        cashValue = findViewById(R.id.cashValue);

    }

    private void setVisibility() {
        if (visualCash) {
            textCash.setVisibility(View.VISIBLE);
            cashValue.setVisibility(View.VISIBLE);
            cashValue.setText(Utils.getCash(getApplicationContext()));
        } else {
            textCash.setVisibility(View.GONE);
            cashValue.setVisibility(View.GONE);
        }
    }

    public void newExpense(View view)

    {
        Intent newExpenseIntent = new Intent(this, ExpenseEditActivity.class);
        startActivity(newExpenseIntent);
    }

    private void setValues() {
        pieEntryMap = new LinkedHashMap<>();
        for (Category category : Category.categoryMap.values()) {
            if (category.isVisible() && category.isInBudget()) {
                String name = category.getName();
                pieEntryMap.put(name,new PieEntry(0,name));
                colors.put(name,Color.parseColor(category.getColor()));

                String name_rest = name+"_rest";
                pieEntryMap.put(name_rest,new PieEntry(category.getAmount(),name_rest));
                colors.put(name_rest,Color.TRANSPARENT);

            }

        }
        for (Expense expense : Expense.expenseMap.values()) {
            float value = (float) expense.getAmount() / 100;
            Category category = Category.getCategoryForID(expense.getCategory());
            assert category != null;
            if (category.isVisible() && category.isInBudget() && !expense.isRetrait()) {
                String name = category.getName();
                PieEntry entry = pieEntryMap.get(name);
                assert entry != null;
                entry.setY(entry.getValue() + value);
                PieEntry entryRest = pieEntryMap.get(name + "_rest");
                assert entryRest != null;
                entryRest.setY(Math.max(0,entryRest.getValue() - value));
            }

        }
    }

    private void setUpChart() {
        PieDataSet pieDataSet = new PieDataSet(new ArrayList<>(pieEntryMap.values()),"Pie Chart");
        PieData pieData = new PieData(pieDataSet);
        pieDataSet.setValueTextColor(Color.TRANSPARENT);
        pieChart.setEntryLabelColor(Color.TRANSPARENT);
        pieDataSet.setColors(new ArrayList<>(colors.values()));
        pieData.setValueTextSize(12f);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    private void progressBarShow() {
        int reste = (maxDepense - Expense.getTotal());
        float total = (float) reste/100;
        String value = total + "€";
        valueRest.setText(value);

        resteBudget.setProgress(Math.abs(reste));

        if (reste < 0.0) {
            int color = getResources().getColor(R.color.teaRose);
            valueRest.setTextColor(color);
            resteBudget.setProgressTintList(ColorStateList.valueOf(color));
            textArgent.setTextColor(color);
        } else {
            int color = getResources().getColor(R.color.mintGreen);
            valueRest.setTextColor(color);
            resteBudget.setProgressTintList(ColorStateList.valueOf(color));
            textArgent.setTextColor(color);
        }

    }

    private void setBudgetAdapter() {
        //SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this);
        List<Category> visibleCategories = Category.getVisibleCategories();
        //List<Integer> progress = sqLiteManager.getProgress(visibleCategories,startDate,endDate);
        List<Integer> progress = Expense.getProgress(visibleCategories);
        BudgetAdapter budgetAdapter = new BudgetAdapter(getApplicationContext(), visibleCategories, progress,1);
        budgetListView.setAdapter(budgetAdapter);
    }

    private void showStorageLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Storage Location")
                .setMessage("Select a storage location for your data")
                .setPositiveButton("Default", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle default option
                        try {
                            Context context = getApplicationContext();
                            String defaultLocation =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()+"/nfelis.budget";
                            Utils.saveStorageLocation(context,"storage_location", null,defaultLocation);
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                        // Perform any necessary actions
                    }
                })
                .setNegativeButton("Custom", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        startActivityForResult(intent, REQUEST_CODE_FILE_CHOOSER);
                    }
                })
                .show();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_FILE_CHOOSER && resultCode == RESULT_OK) {
            Uri treeUri = data.getData();

            // Handle the selected location URI (treeUri) here
            try {
                Utils.saveStorageLocation(this,"storage_location",treeUri,"");
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

        }
        initWidgets();
        setVisibility();
        setDaysLimits();
        loadFromDBToMemory();
        getMaxDepense();
        onResume();
    }

    private void spendSubscriptions() {
        SubscriptionManager subscriptionManager = SubscriptionManager.instanceOfDatabase(this);
        subscriptionManager.populateSubscriptionListArray();

        Date date = new Date();
        for (Subscription subscription : Subscription.subscriptionMap.values()) {
            if (subscription.isActivated() && subscription.getMonth() != month) {
                Expense expense = new Expense(0,subscription.getTitle(),subscription.getCategory(),subscription.getAmount(),date,false,false,false,false);
                sqLiteManager.addExpenseToDatabase(expense);
                subscription.setMonth(month);
            }
        }

    }

    @Override
    protected void onResume() {
            super.onResume();
            if (!isFirstLaunch) {
                setValues();
                setUpChart();
                progressBarShow();
                setBudgetAdapter();
            }
    }
}