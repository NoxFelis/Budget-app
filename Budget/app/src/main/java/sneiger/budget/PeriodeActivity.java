package sneiger.budget;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import sneiger.budget.databinding.ActivityPeriodeBinding;

public class PeriodeActivity extends MainActivity {

    ActivityPeriodeBinding activityPeriodeBinding;
    private TextView dateText,textArgent,valueRest;
    private ListView budgetListView;
    private PieChart pieChart;
    private ProgressBar resteBudget;
    private LinkedHashMap<String,Integer> colors;
    private String startDate,endDate;
    private int maxDepense;
    private LinkedHashMap<String, PieEntry> pieEntryMap;
    private int thisMonth, thisYear, month,year;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityPeriodeBinding = ActivityPeriodeBinding.inflate(getLayoutInflater());
        setContentView(activityPeriodeBinding.getRoot());
        allocateActivityTitle("Periode");

        initWidgets();
        getTodaysDate();
        List<String> days = Utils.setPeriodLimits(month,year);
        startDate = days.get(0);
        endDate = days.get(1);
        loadFromDBToMemory();
        getMaxDepense();
    }

    private void initWidgets() {
        dateText = findViewById(R.id.dateText);
        textArgent = findViewById(R.id.textArgent);
        valueRest = findViewById(R.id.valueRest);
        budgetListView = findViewById(R.id.budgetListView);
        resteBudget = findViewById(R.id.resteBudget);
        nextButton = findViewById(R.id.next_periode);

        pieChart = findViewById(R.id.chart);
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(false);
        pieChart.setDrawHoleEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.getDescription().setEnabled(false);

        colors = new LinkedHashMap<>();
    }

    private void getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        thisYear = cal.get(Calendar.YEAR);
        thisMonth = cal.get(Calendar.MONTH) +1;
        month = thisMonth;
        year = thisYear;
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
        maxDepense*=6;
        resteBudget.setMax(maxDepense); // car on va compter sur 6 mmois
    }

    private void setValues() {
        pieEntryMap = new LinkedHashMap<>();
        for (Category category : Category.categoryMap.values()) {
            if (category.isVisible() && category.isInBudget()) {
                String name = category.getName();
                pieEntryMap.put(name,new PieEntry(0,name));
                colors.put(name, Color.parseColor(category.getColor()));

                String name_rest = name+"_rest";
                pieEntryMap.put(name_rest,new PieEntry(category.getAmount()*6,name_rest));
                colors.put(name_rest,Color.TRANSPARENT);

            }

        }
        for (Expense expense : Expense.expenseMap.values()) {
            float value = (float) expense.getAmount() / 100;
            Category category = Category.getCategoryForID(expense.getCategory());
            assert category != null;
            if (category.isVisible() && category.isInBudget()) {
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
        List<Category> visibleCategories = Category.getVisibleCategories();
        List<Integer> progress = Expense.getProgress(visibleCategories);
        BudgetAdapter budgetAdapter = new BudgetAdapter(getApplicationContext(), visibleCategories, progress,6);
        budgetListView.setAdapter(budgetAdapter);
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

    public void nextPeriod(View view) {
        if (!(year == thisYear && month == thisMonth)) {
            month+=6;
            if (month > 12) {
                year ++;
                month-=12;
            }
            List<String> days = Utils.setPeriodLimits(month,year);
            startDate = days.get(0);
            endDate = days.get(1);
            dateText.setText(Utils.showPeriod(startDate,endDate));
            loadFromDBToMemory();
            onResume();
        }
    }

    public void previousPeriod(View view) {
        month-=6;
        if (month<=0) {
            month = 12 + month;
            year--;
        }
        List<String> days = Utils.setPeriodLimits(month,year);
        startDate = days.get(0);
        endDate = days.get(1);
        loadFromDBToMemory();
        dateText.setText(Utils.showPeriod(startDate,endDate));
        loadFromDBToMemory();
        onResume();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setValues();
        setUpChart();
        progressBarShow();
        setBudgetAdapter();
        enableButton(!(month==thisMonth && year==thisYear));
    }
}