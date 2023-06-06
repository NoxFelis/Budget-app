package nfelis.budget;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;
import java.util.HashMap;

public class SubscriptionManager extends SQLiteOpenHelper  {

    private static String PREFS_NAME;
    private static String path;
    private static SubscriptionManager subscriptionManager;
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Subscription";
    private static final String COUNTER = "Counter";
    private static final String ID_FIELD = "id";
    private static final String TITLE_FIELD = "title";
    private static final String AMOUNT_FIELD = "amount";
    private static final String CATEGORY_FIELD = "category";
    private static final String ACTIVATED_FIELD = "activated";
    private static int number_subscription;

    public SubscriptionManager(Context context)
    {
        super(context, path, null, DATABASE_VERSION);
    }

    public static SubscriptionManager instanceOfDatabase(Context context)
    {
        PREFS_NAME = context.getString(R.string.prefName);
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, context.MODE_PRIVATE);
        path = preferences.getString("storage_location", null)+"/Subscription.db";
        if(subscriptionManager == null)
            subscriptionManager =  new SubscriptionManager(context);
        return subscriptionManager;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        StringBuilder sql;
        sql = new StringBuilder()
                .append("CREATE TABLE ")
                .append(TABLE_NAME)
                .append("(")
                .append(COUNTER)
                .append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(ID_FIELD)
                .append(" INT, ")
                .append(TITLE_FIELD)
                .append(" TEXT, ")
                .append(AMOUNT_FIELD)
                .append(" INT, ")
                .append(CATEGORY_FIELD)
                .append(" INT, ")
                .append(ACTIVATED_FIELD)
                .append(" INT)");
        sqLiteDatabase.execSQL(sql.toString());

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void addSubscriptionToDatabase(Subscription subscription)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        number_subscription++;
        subscription.setId(number_subscription);
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID_FIELD, subscription.getId());
        contentValues.put(TITLE_FIELD, subscription.getTitle());
        contentValues.put(CATEGORY_FIELD,subscription.getCategory());
        contentValues.put(AMOUNT_FIELD,subscription.getAmount());
        contentValues.put(ACTIVATED_FIELD,subscription.isActivated());

        sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        sqLiteDatabase.close();
    }

    public void populateSubscriptionListArray()
    {
        Subscription.subscriptionMap.clear();
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        try (Cursor result = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null))
        {
            if(result.getCount() != 0)
            {
                while (result.moveToNext())
                {
                    int id = result.getInt(1);
                    String title = result.getString(2);
                    int amount = result.getInt(3);
                    int category = result.getInt(4);
                    boolean activated = result.getInt(5) == 1;
                    number_subscription = Math.max(number_subscription,id);
                    Subscription subscription = new Subscription(id,title,category,amount,activated);
                    Subscription.subscriptionMap.put(id,subscription);
                }
            }
        }
        sqLiteDatabase.close();
    }

    public void updateSubscriptionInDB(Subscription subscription)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(ID_FIELD, subscription.getId());
        contentValues.put(TITLE_FIELD, subscription.getTitle());
        contentValues.put(CATEGORY_FIELD,subscription.getCategory());
        contentValues.put(AMOUNT_FIELD,subscription.getAmount());
        contentValues.put(ACTIVATED_FIELD,subscription.isActivated());

        sqLiteDatabase.update(TABLE_NAME, contentValues, ID_FIELD + " =? ", new String[]{String.valueOf(subscription.getId())});
        sqLiteDatabase.close();
    }

    public void deleteSubscriptionInDB(Subscription selectedSubscription) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, ID_FIELD +  "= ?", new String[]{String.valueOf(selectedSubscription.getId())});
        db.close();
        Subscription.subscriptionMap.remove(selectedSubscription.getId());
    }

    public void deleteSubscriptionsInDB(HashMap<Integer,Subscription> subscriptions) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (Subscription subscription : subscriptions.values()) {
            db.delete(TABLE_NAME, ID_FIELD +  "= ?", new String[]{String.valueOf(subscription.getId())});
            Subscription.subscriptionMap.remove(subscription.getId());
        }
        db.close();
    }

    public void deleteCategoryInDB(Category selectedCategory) {
        int idAutre = Category.getIdAutre();
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        subscriptionManager.populateSubscriptionListArray();

        for (Subscription subscription : Subscription.subscriptionMap.values()) {
            if (selectedCategory.getId() == subscription.getCategory()) {
                subscription.setCategory(idAutre);

                ContentValues contentValues = new ContentValues();
                contentValues.put(ID_FIELD, subscription.getId());
                contentValues.put(TITLE_FIELD, subscription.getTitle());
                contentValues.put(CATEGORY_FIELD,subscription.getCategory());
                contentValues.put(AMOUNT_FIELD,subscription.getAmount());
                contentValues.put(ACTIVATED_FIELD,subscription.isActivated());

                sqLiteDatabase.update(TABLE_NAME, contentValues, ID_FIELD + " =? ", new String[]{String.valueOf(subscription.getId())});
            }
        }
    }

    public void updateSubscriptionsInDB(HashMap<Integer, Subscription> selectedItems,int id) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        for (Subscription subscription : selectedItems.values()){
            subscription.setCategory(id);

            ContentValues contentValues = new ContentValues();
            contentValues.put(ID_FIELD, subscription.getId());
            contentValues.put(TITLE_FIELD, subscription.getTitle());
            contentValues.put(CATEGORY_FIELD,subscription.getCategory());
            contentValues.put(AMOUNT_FIELD,subscription.getAmount());
            contentValues.put(ACTIVATED_FIELD,subscription.isActivated());

            sqLiteDatabase.update(TABLE_NAME, contentValues, ID_FIELD + " =? ", new String[]{String.valueOf(subscription.getId())});
        }
        sqLiteDatabase.close();
    }

    public void fillDB() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        for (Subscription subscription : Subscription.subscriptionMap.values()){
            ContentValues contentValues = new ContentValues();
            contentValues.put(ID_FIELD, subscription.getId());
            contentValues.put(TITLE_FIELD, subscription.getTitle());
            contentValues.put(CATEGORY_FIELD,subscription.getCategory());
            contentValues.put(AMOUNT_FIELD,subscription.getAmount());
            contentValues.put(ACTIVATED_FIELD,subscription.isActivated());

            sqLiteDatabase.update(TABLE_NAME, contentValues, ID_FIELD + " =? ", new String[]{String.valueOf(subscription.getId())});
        }
        sqLiteDatabase.close();
    }

    public void deleteDB(Context context) {
        context.deleteDatabase(path);
        subscriptionManager =null;
    }
}
