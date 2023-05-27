package nfelis.budget;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import nfelis.budget.R;
import nfelis.budget.databinding.ActivityHomeBinding;

public class HomeActivity extends MainActivity {
    private PieChart pieChart;
    private LinkedHashMap<String,Integer> colors;
    private LinkedHashMap<String,PieEntry> pieEntryMap;
    ActivityHomeBinding activityHomeBinding;
    private String startDate,endDate;
    private TextView valueRest, textArgent;
    private int maxDepense;
    private ProgressBar resteBudget;
    private ListView budgetListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityHomeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(activityHomeBinding.getRoot());
        allocateActivityTitle("Home");

        initWidgets();
        setDaysLimits();
        loadFromDBToMemory();
        getMaxDepense();
    }

    private void loadFromDBToMemory() {
        CategoryManager categoryManager = CategoryManager.instanceOfDatabase(this,true);
        categoryManager.populateCategorySet();

        SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this,true);
        sqLiteManager.populateExpenseListArray(startDate,endDate);
    }

    private void getMaxDepense() {
        maxDepense = 0;
        for (Category category : Category.categoryMap.values()) {
            if (category.isInBudget()) {
                maxDepense += category.getAmount()*100;
            }
        }
        resteBudget.setMax(maxDepense);
    }

    private void setDaysLimits() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) +1;
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

    @Override
    protected void onResume() {
            super.onResume();
        setValues();
        setUpChart();
        progressBarShow();
        setBudgetAdapter();
    }
}