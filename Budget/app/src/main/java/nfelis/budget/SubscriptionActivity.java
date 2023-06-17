package nfelis.budget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

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
    private FloatingActionButton deleteButton,changeButton,deactivateButton,activateButton;
    private int currentMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySubcriptionBinding = ActivitySubscriptionBinding.inflate(getLayoutInflater());
        setContentView(activitySubcriptionBinding.getRoot());
        allocateActivityTitle("Subscription");

        initWidgets();
        getTodaysMonth();
    }

    private void initWidgets() {
        subscriptionListView = findViewById(R.id.subscriptionListView);
	    subscriptionListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        checkBox = findViewById(R.id.checkBox);
        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setClickable(false);
        changeButton = findViewById(R.id.changeButton);
        changeButton.setClickable(false);
        activateButton = findViewById(R.id.activateButton);
        activateButton.setClickable(false);
        deactivateButton = findViewById(R.id.deactivateButton);
        deactivateButton.setClickable(false);
        multipleChoiceButtons = findViewById(R.id.multipleChoiceButtons);
        isMultipleMode=false;
        multipleChoiceButtons.setVisibility(View.GONE);
    }

    private void getTodaysMonth() {
        Calendar cal = Calendar.getInstance();
        currentMonth = cal.get(Calendar.MONTH) +1;
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
                    //subscriptionAdapter.notifyDataSetChanged();
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
        List<Subscription> listSubs = new ArrayList<Subscription>(Subscription.subscriptionMap.values());
        subscriptionAdapter = new SubscriptionAdapter(getApplicationContext(), listSubs);
        subscriptionListView.setAdapter(subscriptionAdapter);
    }

    public void setMultiple(View view) {
        setMultiple(checkBox.isChecked());
    }

    private void setMultiple(boolean multiple) {
        /*if (multiple) {
            isMultipleMode= true;
            subscriptionListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            multipleChoiceButtons.setVisibility(View.VISIBLE);
            deleteButton.setClickable(true);
            changeButton.setClickable(true);
            deactivateButton.setClickable(true);
            activateButton.setClickable(true);
            selectedItems = new HashMap<>();
        } else {
            isMultipleMode=false;
            subscriptionListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
            subscriptionListView.clearChoices();
            setAllItemsChecked(false);
            multipleChoiceButtons.setVisibility(View.GONE);
            deleteButton.setClickable(false);
            changeButton.setClickable(false);
            deactivateButton.setClickable(false);
            activateButton.setClickable(false);
            selectedItems.clear();
        }*/
        isMultipleMode= multiple;
        checkBox.setChecked(multiple);
        deleteButton.setClickable(multiple);
        changeButton.setClickable(multiple);
        deactivateButton.setClickable(multiple);
        activateButton.setClickable(multiple);
        if (multiple) {
            multipleChoiceButtons.setVisibility(View.VISIBLE);
            selectedItems = new HashMap<>();
            subscriptionListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        } else {
            subscriptionListView.clearChoices();
            setAllItemsChecked(false);
            subscriptionListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            multipleChoiceButtons.setVisibility(View.GONE);
            selectedItems.clear();
        }
    }

    private void setAllItemsChecked(boolean checked) {
        int itemCount = subscriptionAdapter.getCount();
        for (int i = 0; i < itemCount; i++) {
            subscriptionListView.setItemChecked(i, checked);
        }
        //subscriptionAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFromDBToMemory();
        Log.d("SubscriptionMap", String.valueOf(Subscription.subscriptionMap));
        setSubscriptionAdapter();
        setListener();
    }

    public void deleteSubscriptions(View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create(); //Read Update
        alertDialog.setTitle("Supprimer");
        alertDialog.setMessage("Voulez-vous supprimer tous ces abonements?");

        alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SubscriptionManager subscriptionManager = SubscriptionManager.instanceOfDatabase(view.getContext());
                subscriptionManager.deleteSubscriptionsInDB(selectedItems);
                selectedItems.clear();
                alertDialog.dismiss();
                setMultiple(false);
                setSubscriptionAdapter();
                onResume();
            }
        });
        alertDialog.show();  //<-- See This!
    }

    public void changeCategory(View view) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.change_dialog);

        Spinner spinner = dialog.findViewById(R.id.categorySpinner);
        CategoryAdapter categoryAdapter = new CategoryAdapter(getApplicationContext(), new ArrayList<Category>(Category.categoryMap.values()));
        spinner.setAdapter(categoryAdapter);

        Button cancel = dialog.findViewById(R.id.button_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        Button ok = dialog.findViewById(R.id.button_ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubscriptionManager subscriptionManager = SubscriptionManager.instanceOfDatabase(getApplicationContext());
                int category = ((Category)spinner.getSelectedItem()).getId();
                subscriptionManager.updateSubscriptionsInDB(selectedItems,category);
                dialog.dismiss();
                subscriptionAdapter.notifyDataSetChanged();
                setMultiple(false);
                //onResume();
            }
        });
        dialog.show();
    }

    public void activate(View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create(); //Read Update
        alertDialog.setTitle("Activer");
        alertDialog.setMessage("Voulez-vous activer tous ces abonnements?");

        alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SubscriptionManager subscriptionManager = SubscriptionManager.instanceOfDatabase(view.getContext());
                subscriptionManager.activationSubscriptionsInDB(selectedItems,true,getApplicationContext(),currentMonth);
                selectedItems.clear();
                alertDialog.dismiss();
                setMultiple(false);
                setSubscriptionAdapter();
                //subscriptionAdapter.notifyDataSetChanged();
                //onResume();
            }
        });
        alertDialog.show();  //<-- See This!
    }

    public void deactivate(View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create(); //Read Update
        alertDialog.setTitle("Désactiver");
        alertDialog.setMessage("Voulez-vous désactiver tous ces abonnements?");

        alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SubscriptionManager subscriptionManager = SubscriptionManager.instanceOfDatabase(view.getContext());
                subscriptionManager.activationSubscriptionsInDB(selectedItems,false,getApplicationContext(),currentMonth);
                selectedItems.clear();
                alertDialog.dismiss();
                setMultiple(false);
                setSubscriptionAdapter();
                //subscriptionAdapter.notifyDataSetChanged();
                //onResume();
            }
        });
        alertDialog.show();  //<-- See This!
    }

    public void newSubscription(View view) {
        Intent newSubscriptionIntent = new Intent(this, SubscriptionEditActivity.class);
        startActivity(newSubscriptionIntent);
    }

}
