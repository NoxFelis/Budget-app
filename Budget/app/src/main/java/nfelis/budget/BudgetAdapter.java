package nfelis.budget;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import nfelis.budget.R;

public class BudgetAdapter extends ArrayAdapter<Category> {
    private Context context;
    private List<Category> categories;
    private List<Integer> progress;
    private int periode;

    /** constructor of the BudgetAdapter
     * @param context       context of the app
     * @param categories    list of categories to upload in the listview
     * @param progress      list of value spent in each category to view
     * @param periode       periode (in months) so as to multiply the allocated amount for each category
     * */
    public BudgetAdapter(Context context,List<Category> categories,List<Integer> progress,int periode) {
        super(context,0,categories);
        this.context = context;
        this.categories = categories;
        this.progress = progress;
        this.periode = periode;
    }

    @Override
    public int getCount() {return categories.size();}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.budget_cell, parent, false);
        }
        //get the category to view at the position
        Category category = categories.get(position);

        // set the name in the given color of the category
        TextView cellName = convertView.findViewById(R.id.cellName);
        cellName.setText(category.getName());
        cellName.setTextColor(Color.parseColor(category.getColor()));

        // set the progress of the category (how much money has been spent in the category) in the given color
        TextView cellProgress = convertView.findViewById(R.id.cellProgress);
        String rest = Utils.getStringFromAmount(progress.get(position)) + "/" + category.getAmount()*periode + ".0";
        cellProgress.setText(rest);
        cellProgress.setTextColor(Color.parseColor(category.getColor()));

        return convertView;
    }
}
