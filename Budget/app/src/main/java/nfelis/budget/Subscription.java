package nfelis.budget;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.LinkedHashMap;

/*  Dates will be set once the subscription is turned into an expense
    It is decided all subscription are paid by card
    and that they are not reimbursed naturally
    Subscriptions are specifically for monthly expenses*/
public class Subscription {
    public static LinkedHashMap<Integer,Subscription> subscriptionMap = new LinkedHashMap<>();
    public static String SUBSCRIPTION_EDIT_EXTRA =  "subscriptionEdit";
    private int id;             // id de reconnaissance de la dépense
    private String title;       // titre/descriptif de la dépense
    private int category;    // id de la catégorie de la dépense (santé, loisir, alimentaire, etc.)
    private int amount;         // valeur de la dépense, elle sera stockée en entiers
    private boolean activated;  // si on utilise cet abonnement
    public Subscription(int id, String title, int category, int amount,boolean activated) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.activated = activated;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isActivated() {return activated;}
    public void setActivated(boolean activated) {this.activated = activated;}

    public static Subscription getSubscriptionForID(int passedSubscriptionID) {
        for (Subscription subscription : subscriptionMap.values())
        {
            if(subscription.getId() == passedSubscriptionID)
                return subscription;
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return this.title + " " + this.activated;
    }
}
