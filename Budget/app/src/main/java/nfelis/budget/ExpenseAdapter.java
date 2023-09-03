package nfelis.budget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.prefs.Preferences;

import nfelis.budget.R;

public class ExpenseAdapter extends ArrayAdapter<Expense> {
    private int gray,mint;
    private boolean cents;
    @SuppressLint("SimpleDateFormat")
    private static final DateFormat dateFormat = new SimpleDateFormat("dd-MM");
    public ExpenseAdapter(Context context, List<Expense> expenses)
    {
        super(context, 0, expenses);
        gray = context.getResources().getColor(R.color.battleshipGray);
        mint = context.getResources().getColor(R.color.mintGreen);

        String CENTS = context.getString(R.string.cents);
        String PREFS_NAME = context.getString(R.string.prefName);
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        cents = preferences.getBoolean(CENTS,true);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        Expense expense = getItem(position);
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.expense_cell, parent, false);

        TextView title = convertView.findViewById(R.id.cellTitle);
        TextView amount = convertView.findViewById(R.id.cellAmount);
        TextView cat = convertView.findViewById(R.id.cellCat);
        TextView date = convertView.findViewById(R.id.cellDate);

        title.setText(expense.getTitle());
        amount.setText(Utils.getStringFromAmount(expense.getAmount(),cents)+"€");
        cat.setText(Category.getCategoryForID(expense.getCategory()).getName());
        date.setText(Utils.getStringFromDate(expense.getDate(),dateFormat));
        if (expense.isRembourse()) {
            title.setTextColor(gray);
            amount.setTextColor(gray);
            cat.setTextColor(gray);
            date.setTextColor(gray);
        } else if (expense.getAmount()< 0) {
            title.setTextColor(mint);
            amount.setTextColor(mint);
            cat.setTextColor(mint);
            date.setTextColor(mint);
        }
        return convertView;
    }


}
