package nfelis.budget;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;

import nfelis.budget.R;
import nfelis.budget.databinding.ActivitySubscriptionBinding;


public class SubscriptionActivity extends MainActivity {
    private ActivitySubscriptionBinding activitySubcriptionBinding;
    private ListView subscriptionListView;
    private CheckBox checkBox;
    private ConstraintLayout multipleChoiceButtons;
    private boolean isMultipleMode;
    private HashMap<Integer,Subscription> selectedItems;
    private SubscriptionAdapter subscriptionAdapter;
    private FloatingActionButton deleteButton,changeButton,deactivate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySubcriptionBinding = ActivitySubscriptionBinding.inflate(getLayoutInflater());
        setContentView(activitySubcriptionBinding.getRoot());
        allocateActivityTitle("Subscription");

        initWidgets();
        setListener();
    }

    private void initWidgets() {
        subscriptionListView = findViewById(R.id.subscriptionListView);
        checkBox = findViewById(R.id.checkBox);
        multipleChoiceButtons = findViewById(R.id.multipleChoiceButtons);
        isMultipleMode=false;
    }

    private void setListener() {
        subscriptionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Subscription selectedSubscription = (Subscription) subscriptionListView.getItemAtPosition(position);
                if (!isMultipleMode) {
                    Intent editSubscriptionIntent = new Intent(getApplicationContext(), SubscriptionEditActivity.class);
                    editSubscriptionIntent.putExtra(Subscription.SUBSCRIPTION_EDIT_EXTRA, selectedSubscription.getId());
                    startActivity(editSubscriptionIntent);
                } else {
                    if (subscriptionListView.isItemChecked(position)) {
                        selectedItems.put(position,selectedSubscription);
                    } else {
                        selectedItems.remove(position);
                    }

                }
            }
        });

    }

    private void loadFromDBToMemory() {
        SubscriptionManager subscriptionManager = SubscriptionManager.instanceOfDatabase(this);
        subscriptionManager.populateSubscriptionListArray();
    }

    private void setSubscriptionAdapter() {
        subscriptionAdapter = new SubscriptionAdapter(getApplicationContext(), new ArrayList<Subscription>(Subscription.subscriptionMap.values()));
        subscriptionListView.setAdapter(subscriptionAdapter);
    }

    public void setMultiple(View view) {
        boolean checked = checkBox.isChecked();
        isMultipleMode= checked;
        subscriptionListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        deleteButton.setClickable(checked);
        changeButton.setClickable(checked);
        deactivate.setClickable(checked);
        if (checked) {
            multipleChoiceButtons.setVisibility(View.VISIBLE);
            selectedItems = new HashMap<>();
        } else {
            multipleChoiceButtons.setVisibility(View.GONE);
            selectedItems.clear();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFromDBToMemory();
        setSubscriptionAdapter();
    }
}