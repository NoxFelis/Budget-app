package nfelis.budget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import nfelis.budget.R;
import nfelis.budget.databinding.ActivityCategoryBinding;

public class CategoryActivity extends MainActivity {
    private ListView categoryListView,categoryNonListView;  //List views of the category activity
    private TextView total;     // text view to show total of budget
    private boolean percent;    // boolean to know if in percentage mode or not
    private String PREFS_NAME,PERCENTAGE;       // names of shared preference and access to element
    private int maxDepense;                     // value of total in budget
    private CategoryAdapter adapter, adapterNon;
    ActivityCategoryBinding activityCategoryBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityCategoryBinding = ActivityCategoryBinding.inflate(getLayoutInflater());
        setContentView(activityCategoryBinding.getRoot());
        allocateActivityTitle("Catégories");

        Context context = getApplicationContext();
        PREFS_NAME = context.getString(R.string.prefName);
        PERCENTAGE = context.getString(R.string.percentage);


        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        percent = preferences.getBoolean(PERCENTAGE, false);
        if (percent) {
            String value = context.getString(R.string.total);

            // TODO
            maxDepense = preferences.getInt(value,Integer.parseInt(context.getString(R.string.default_total)));
        }

        initWidgets();
        setVisibility();

    }

    private void initWidgets() {
        categoryListView = findViewById(R.id.categoryListView);
        categoryNonListView = findViewById(R.id.categoryNonListView);
        total = findViewById(R.id.total);
    }

    private void setVisibility() {
        if (percent) {
            categoryNonListView.setVisibility(View.VISIBLE);
            total.setVisibility(View.VISIBLE);
            String text = "Total " + maxDepense + "€";
            total.setText(text);
        } else {
            categoryNonListView.setVisibility(View.GONE);
            total.setVisibility(View.GONE);
        }
    }

    private void loadFromDBToMemory() {
        CategoryManager categoryManager = CategoryManager.instanceOfDatabase(this);
        categoryManager.populateCategorySet(percent);
    }

    private void setCategoryAdapter() {
        adapter = new CategoryAdapter(getApplicationContext(), new ArrayList<Category>(Category.categoryMap.values()));
        categoryListView.setAdapter(adapter);
        if (percent) {
            adapterNon = new CategoryAdapter(getApplicationContext(), new ArrayList<Category>(Category.categoryNonMap.values()));
            categoryNonListView.setAdapter(adapterNon);
        }

    }

    private void setOnClickListener() {
        categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long column) {
                Category selectedCategory = (Category) categoryListView.getItemAtPosition(position);
                Intent editCategoryIntent = new Intent(getApplicationContext(), CategoryEditActivity.class);
                editCategoryIntent.putExtra(Category.CATEGORY_EDIT_EXTRA,selectedCategory.getId());
                startActivity(editCategoryIntent);
            }
        });
        categoryNonListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long column) {
                Category selectedCategory = (Category) categoryNonListView.getItemAtPosition(position);
                Intent editCategoryIntent = new Intent(getApplicationContext(), CategoryEditActivity.class);
                editCategoryIntent.putExtra(Category.CATEGORY_EDIT_EXTRA,selectedCategory.getId());
                startActivity(editCategoryIntent);
            }
        });
    }


    public void newCategory(View view){
        Intent newCategoryIntent = new Intent(getApplicationContext(), CategoryEditActivity.class);
        startActivity(newCategoryIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFromDBToMemory();
        setCategoryAdapter();
        setOnClickListener();
    }
}