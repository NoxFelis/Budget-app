package nfelis.budget;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SubscriptionEditActivity extends AppCompatActivity {
    private EditText titreEditText,editTextNumber;
    private Spinner categorySpinner;
    private Switch switchActivated;
    private FloatingActionButton deleteButton;
    private Subscription selectedSubscription;
    private CategoryAdapter categoryAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_edit);

        initWidgets();
        setListeners();
        loadFromDBToMemory();
        setAdapter();
        checkForEditSubscription();
    }

    private void initWidgets() {
        titreEditText = findViewById(R.id.titreEditText);
        editTextNumber = findViewById(R.id.editTextNumber);
        categorySpinner = findViewById(R.id.categorySpinner);
        switchActivated = findViewById(R.id.switchActivated);

        deleteButton = findViewById(R.id.deleteButton);
    }

    private void setListeners() {
        //empty
    }

    private void loadFromDBToMemory() {
        CategoryManager categoryManager = CategoryManager.instanceOfDatabase(this);
        categoryManager.populateCategorySet(false);
    }

    private void setAdapter() {
        categoryAdapter = new CategoryAdapter(getApplicationContext(),new ArrayList<Category>(Category.categoryMap.values()));
        categorySpinner.setAdapter(categoryAdapter);
    }

    private void checkForEditSubscription() {
        Intent previousIntent = getIntent();

        int passedSubscriptionID = previousIntent.getIntExtra(Subscription.SUBSCRIPTION_EDIT_EXTRA,-1);
        selectedSubscription = Subscription.getSubscriptionForID(passedSubscriptionID);

        if (selectedSubscription != null) {
            titreEditText.setText(selectedSubscription.getTitle());
            editTextNumber.setText(Utils.getStringFromAmount(selectedSubscription.getAmount()));
            categorySpinner.setSelection(categoryAdapter.getPosition(Category.getCategoryForID(selectedSubscription.getCategory())));
            switchActivated.setChecked(selectedSubscription.isActivated());

        } else {
            // idk
            switchActivated.setChecked(true);
            deleteButton.setVisibility(View.INVISIBLE);
        }
    }

    public void saveSubscription(View view)
    {
        String title = String.valueOf(titreEditText.getText()).trim();
        if (title.compareTo("")==0) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create(); //Read Update
            alertDialog.setTitle("Nom vide");
            alertDialog.setMessage("Vous ne pouvez pas créer de dépense sans titre!");

            alertDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();  //<-- See This!
        } else {
            SubscriptionManager subscriptionManager = SubscriptionManager.instanceOfDatabase(this);
            String value = String.valueOf(editTextNumber.getText());
            int amount = (value.length() == 0) ? 0 : Math.round(Float.parseFloat(value)*100) ;

            int category = ((Category) categorySpinner.getSelectedItem()).getId();
            boolean activated = switchActivated.isChecked();

            if(selectedSubscription == null)
            {
                Subscription newSubscription = new Subscription(0, title,category,amount,activated);
                subscriptionManager.addSubscriptionToDatabase(newSubscription);
                Subscription.subscriptionMap.put(newSubscription.getId(),newSubscription);

                //because it's a new subscription
                if (activated) {
                    Date date = new Date();
                    Expense newExpense = new Expense(0,title,category,amount,date,false,false,false,false);
                    SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this);
                    sqLiteManager.addExpenseToDatabase(newExpense);
                }
            }
            else
            {
                boolean previous = selectedSubscription.isActivated();

                selectedSubscription.setTitle(title);
                selectedSubscription.setAmount(amount);
                selectedSubscription.setCategory(category);
                selectedSubscription.setActivated(activated);
                subscriptionManager.updateSubscriptionInDB(selectedSubscription);
                Subscription.subscriptionMap.put(selectedSubscription.getId(),selectedSubscription);
                // HomeActivity.updateValue(selectedExpense,oldValue);
                if (!previous && activated) {
                    Date date = new Date();
                    Expense newExpense = new Expense(0,title,category,amount,date,false,false,false,false);
                    SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this);
                    sqLiteManager.addExpenseToDatabase(newExpense);
                }
            }
            finish();
        }
    }

    public void deleteSubscription(View view)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create(); //Read Update
        alertDialog.setTitle("Supprimer");
        alertDialog.setMessage("Voulez-vous supprimer cet abonnement?");

        alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SubscriptionManager subscriptionManager = SubscriptionManager.instanceOfDatabase(getApplicationContext());
                subscriptionManager.deleteSubscriptionInDB(selectedSubscription);
                finish();
            }
        });
        alertDialog.show();  //<-- See This!
    }

    public void cancel(View view) {
        String title = String.valueOf(titreEditText.getText()).trim();
        String value = String.valueOf(editTextNumber.getText());
        int amount = (value.length() == 0) ? 0 : Math.round(Float.parseFloat(value)*100) ;
        int category = ((Category) categorySpinner.getSelectedItem()).getId();
        boolean activated = switchActivated.isChecked();

        if ((selectedSubscription == null && (title.length() != 0 || value.length() !=0 || category != Category.getIdAutre() || !activated ))
                || (selectedSubscription != null && (selectedSubscription.getTitle().compareTo(title)!=0 || selectedSubscription.getAmount() != amount
                || selectedSubscription.getCategory() != category || selectedSubscription.isActivated() != activated ))) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create(); //Read Update
            alertDialog.setTitle("Retour");
            alertDialog.setMessage("Voulez-vous retourner sans sauvegarder??");

            alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.setButton("Oui", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    onBackPressed();
                }
            });
            alertDialog.show();  //<-- See This!
        } else {
            onBackPressed();
        }
    }
}