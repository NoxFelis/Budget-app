package sneiger.budget;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import sneiger.budget.databinding.ActivityCategoryBinding;
import sneiger.budget.databinding.ActivitySettingsBinding;

public class CategoryActivity extends MainActivity {
    private ListView categoryListView;
    ActivityCategoryBinding activityCategoryBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityCategoryBinding = ActivityCategoryBinding.inflate(getLayoutInflater());
        setContentView(activityCategoryBinding.getRoot());
        allocateActivityTitle("Catégories");

        initWidgets();
        loadFromDBToMemory();
        setOnClickListener();

    }

    private void initWidgets() {
        categoryListView = findViewById(R.id.categoryListView);
    }

    private void loadFromDBToMemory() {
        CategoryManager categoryManager = CategoryManager.instanceOfDatabase(this,true);
        categoryManager.populateCategorySet();
    }

    private void setCategoryAdapter() {
        CategoryAdapter adapter = new CategoryAdapter(getApplicationContext(), new ArrayList<Category>(Category.categoryMap.values()));
        categoryListView.setAdapter(adapter);
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
    }


    public void newCategory(View view){
        Intent newCategoryIntent = new Intent(this, CategoryEditActivity.class);
        startActivity(newCategoryIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCategoryAdapter();
    }
}