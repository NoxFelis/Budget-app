package nfelis.budget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class SubscriptionAdapter extends ArrayAdapter<Subscription> {
    private int gray;
    public SubscriptionAdapter(Context context, List<Subscription> subscriptions) {
        super(context, 0, subscriptions);
        gray = context.getResources().getColor(R.color.battleshipGray);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        Subscription subscription = getItem(position);
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.subscription_cell, parent, false);

        TextView title = convertView.findViewById(R.id.cellTitle);
        TextView amount = convertView.findViewById(R.id.cellAmount);
        TextView cat = convertView.findViewById(R.id.cellCat);

        title.setText(subscription.getTitle());
        amount.setText(Utils.getStringFromAmount(subscription.getAmount())+"€");
        cat.setText(Category.getCategoryForID(subscription.getCategory()).getName());
        if (subscription.isActivated()) {
            title.setTextColor(gray);
            amount.setTextColor(gray);
            cat.setTextColor(gray);
        }
        return convertView;
    }

}
