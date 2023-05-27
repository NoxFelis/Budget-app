package nfelis.budget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.List;

import nfelis.budget.R;

public class CategoryAdapter extends ArrayAdapter<Category> {

    private Context context;
    private List<Category> categories;
    int platinum;
    int delftBlue_2;

    public CategoryAdapter(Context context, List<Category> categories)
    {
        super(context,0,categories);
        this.context = context;
        this.categories = categories;
        delftBlue_2 = ContextCompat.getColor(context, R.color.delftBlue_2);
        platinum = ContextCompat.getColor(context,R.color.platinum);
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Category getItem(int position) {
        return categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.category_cell, parent, false);
        }
        TextView categoryTextView = convertView.findViewById(R.id.cellText);
        Category category = categories.get(position);
        categoryTextView.setText(category.getName());
        categoryTextView.setTextColor(platinum);
        View color = convertView.findViewById(R.id.color);
        color.setBackgroundColor(Color.parseColor(category.getColor()));
        TextView amount = convertView.findViewById(R.id.cellAmount);
        amount.setText(category.getAmount() + "€");
        //categoryButton.setBackgroundTintList(Color.parseColor(Expense.categoryMap.get(text)));
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            textView = new TextView(context);
        } else {
            textView = (TextView) convertView;
        }
        textView.setTextColor(platinum);
        textView.setBackgroundColor(delftBlue_2);
        textView.setTextSize(18);
        textView.setPadding(8, 8, 8, 8);
        textView.setText(categories.get(position).getName());
        return textView;
    }
}
