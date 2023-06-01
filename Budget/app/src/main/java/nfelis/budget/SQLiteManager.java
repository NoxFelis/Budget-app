package nfelis.budget;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class SQLiteManager extends SQLiteOpenHelper
{
    private static String path;
    private static SQLiteManager sqLiteManager;
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Expense";
    private static final String COUNTER = "Counter";
    private static final String ID_FIELD = "id";
    private static final String TITLE_FIELD = "title";
    private static final String AMOUNT_FIELD = "amount";
    private static final String DATE_FIELD = "date";
    private static final String CASH_FIELD = "cash";
    private static final String RETRAIT_FIELD = "retrait";
    private static final String CATEGORY_FIELD = "category";
    private static final String REMBOURSE_FIELD = "rembourse";
    private static final String REMBOURSE_CASH_FIELD = "rembourseCash";
    private static int number_expenses;

    @SuppressLint("SimpleDateFormat")
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public SQLiteManager(Context context)
    {
        super(context, path, null, DATABASE_VERSION);
    }
    public static SQLiteManager instanceOfDatabase(Context context)
    {
        if(sqLiteManager == null)
            sqLiteManager =  new SQLiteManager(context);
        return sqLiteManager;
    }

    public static SQLiteManager instanceOfDatabase(Context context, boolean custom) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", context.MODE_PRIVATE);
        path = preferences.getString("storage_expenses", null);
        if(sqLiteManager == null)
            sqLiteManager =  new SQLiteManager(context);
        return sqLiteManager;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
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
                .append(DATE_FIELD)
                .append(" TEXT, ")
                .append(AMOUNT_FIELD)
                .append(" INT, ")
                .append(CASH_FIELD)
                .append(" INT, ")
                .append(RETRAIT_FIELD)
                .append(" INT, ")
                .append(CATEGORY_FIELD)
                .append(" INT, ")
                .append(REMBOURSE_FIELD)
                .append(" INT, ")
                .append(REMBOURSE_CASH_FIELD)
                .append(" INT)");
        sqLiteDatabase.execSQL(sql.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {
//        switch (oldVersion)
//        {
//            case 1:
//                sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + NEW_COLUMN + " TEXT");
//            case 2:
//                sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + NEW_COLUMN + " TEXT");
//        }
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS expenses;");
        //onCreate(sqLiteDatabase);
    }
    public void addExpenseToDatabase(Expense expense)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        number_expenses++;
        expense.setId(number_expenses);
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID_FIELD, expense.getId());
        contentValues.put(TITLE_FIELD, expense.getTitle());
        contentValues.put(CASH_FIELD,expense.isCash());
        contentValues.put(RETRAIT_FIELD,expense.isRetrait());
        contentValues.put(CATEGORY_FIELD,expense.getCategory());
        contentValues.put(AMOUNT_FIELD,expense.getAmount());
        contentValues.put(DATE_FIELD,Utils.getStringFromDate(expense.getDate(),dateFormat));
        contentValues.put(REMBOURSE_FIELD,expense.isRembourse());
        contentValues.put(REMBOURSE_CASH_FIELD,expense.isRembourseCash());

        sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        sqLiteDatabase.close();
    }

    public void populateExpenseListArray(String startDate, String endDate)
    {
        Expense.expenseMap.clear();
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String dateCondition = (startDate != null && endDate != null) ? " WHERE " + DATE_FIELD + " BETWEEN '" + startDate + "' AND '" + endDate + "'" : "";

        try (Cursor result = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME +
                dateCondition + " ORDER BY " + DATE_FIELD + " DESC", null))
        {
            if(result.getCount() != 0)
            {
                while (result.moveToNext())
                {
                    int id = result.getInt(1);
                    String title = result.getString(2);
                    Date date = Utils.getDateFromString(result.getString(3),dateFormat);
                    int amount = result.getInt(4);
                    boolean cash = result.getInt(5) == 1;
                    boolean retrait = result.getInt(6) == 1;
                    int category = result.getInt(7);
                    boolean rembourse = result.getInt(8) == 1;
                    boolean rembourseCash = result.getInt(9) ==1;
                    number_expenses = Math.max(number_expenses,id);
                    Expense expense = new Expense(id,title,category,amount,date,cash,retrait,rembourse,rembourseCash);
                    Expense.expenseMap.put(id,expense);
                }
            }
        }
        sqLiteDatabase.close();
    }
    public void updateExpenseInDB(Expense expense)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(ID_FIELD, expense.getId());
        contentValues.put(TITLE_FIELD, expense.getTitle());
        contentValues.put(CASH_FIELD,expense.isCash());
        contentValues.put(RETRAIT_FIELD,expense.isRetrait());
        contentValues.put(CATEGORY_FIELD,expense.getCategory());
        contentValues.put(AMOUNT_FIELD,expense.getAmount());
        contentValues.put(DATE_FIELD,Utils.getStringFromDate(expense.getDate(),dateFormat));
        contentValues.put(REMBOURSE_FIELD,expense.isRembourse());
        contentValues.put(REMBOURSE_CASH_FIELD,expense.isRembourseCash());

        sqLiteDatabase.update(TABLE_NAME, contentValues, ID_FIELD + " =? ", new String[]{String.valueOf(expense.getId())});
        sqLiteDatabase.close();
    }


    public void deleteExpenseInDB(Expense selectedExpense) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, ID_FIELD +  "= ?", new String[]{String.valueOf(selectedExpense.getId())});
        db.close();
        Expense.expenseMap.remove(selectedExpense.getId());
    }

    public void deleteExpensesInDB(HashMap<Integer,Expense> expenses) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (Expense expense : expenses.values()) {
            db.delete(TABLE_NAME, ID_FIELD +  "= ?", new String[]{String.valueOf(expense.getId())});
            Expense.expenseMap.remove(expense.getId());
        }
        db.close();
    }

    @SuppressLint("Range")
    public int getTotal(String startDate, String endDate) {
        String dateCondition = (startDate != null && endDate != null) ? " WHERE " + DATE_FIELD + " BETWEEN '" + startDate + "' AND '" + endDate + "'" : "";
        String rawQuery = "SELECT SUM(" + AMOUNT_FIELD + ") AS total FROM " + TABLE_NAME + dateCondition;
        SQLiteDatabase db = sqLiteManager.getReadableDatabase();
        Cursor cursor = db.rawQuery(rawQuery, null);
        int totalExpense = 0;
        if (cursor.moveToFirst()) {
            totalExpense = cursor.getInt(cursor.getColumnIndex("total"));
        }
        cursor.close();
        db.close();
        return totalExpense;

    }

    @SuppressLint("Range")
    public List<Integer> getProgress(List<Category> visibleCategories, String startDate, String endDate) {
        List<Integer> progress = new ArrayList<>();
        SQLiteDatabase db = sqLiteManager.getReadableDatabase();
        String dateCondition = (startDate != null && endDate != null) ? " AND " + DATE_FIELD + " BETWEEN '" + startDate + "' AND '" + endDate + "'" : "";
        for (Category category : visibleCategories) {
            String rawQuery = "SELECT SUM (" + AMOUNT_FIELD + ") AS total FROM " + TABLE_NAME + " WHERE " + ID_FIELD + " = " + category.getId() + dateCondition;
            Cursor cursor = db.rawQuery(rawQuery, null);
            int totalExpense = 0;
            if (cursor.moveToFirst()) {
                totalExpense = cursor.getInt(cursor.getColumnIndex("total"));
            }
            progress.add(totalExpense);
            cursor.close();
        }
        db.close();
        return progress;
    }

    public void deleteCategoryInDB(Category selectedCategory) {
        int idAutre = Category.getIdAutre();
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        for (Expense expense : Expense.expenseMap.values()) {
            if (selectedCategory.getId() == expense.getCategory()) {
                expense.setCategory(idAutre);

                ContentValues contentValues = new ContentValues();
                contentValues.put(ID_FIELD, expense.getId());
                contentValues.put(TITLE_FIELD, expense.getTitle());
                contentValues.put(CASH_FIELD,expense.isCash());
                contentValues.put(RETRAIT_FIELD,expense.isRetrait());
                contentValues.put(CATEGORY_FIELD,expense.getCategory());
                contentValues.put(AMOUNT_FIELD,expense.getAmount());
                contentValues.put(DATE_FIELD,Utils.getStringFromDate(expense.getDate(),dateFormat));
                contentValues.put(REMBOURSE_FIELD,expense.isRembourse());
                contentValues.put(REMBOURSE_CASH_FIELD,expense.isRembourseCash());

                sqLiteDatabase.update(TABLE_NAME, contentValues, ID_FIELD + " =? ", new String[]{String.valueOf(expense.getId())});
            }
        }
    }

    public void updateExpensesInDB(HashMap<Integer, Expense> selectedItems,int id) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        for (Expense expense : selectedItems.values()){
            expense.setCategory(id);

            ContentValues contentValues = new ContentValues();
            contentValues.put(ID_FIELD, expense.getId());
            contentValues.put(TITLE_FIELD, expense.getTitle());
            contentValues.put(CASH_FIELD,expense.isCash());
            contentValues.put(RETRAIT_FIELD,expense.isRetrait());
            contentValues.put(CATEGORY_FIELD,expense.getCategory());
            contentValues.put(AMOUNT_FIELD,expense.getAmount());
            contentValues.put(DATE_FIELD,Utils.getStringFromDate(expense.getDate(),dateFormat));
            contentValues.put(REMBOURSE_FIELD,expense.isRembourse());
            contentValues.put(REMBOURSE_CASH_FIELD,expense.isRembourseCash());

            sqLiteDatabase.update(TABLE_NAME, contentValues, ID_FIELD + " =? ", new String[]{String.valueOf(expense.getId())});
        }
        sqLiteDatabase.close();
    }

    public void rembourseExpensesInDB(HashMap<Integer, Expense> selectedItems, boolean parCash) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        for (Expense expense : selectedItems.values()){
            expense.setRembourse(true);
            expense.setRembourseCash(parCash);

            ContentValues contentValues = new ContentValues();
            contentValues.put(ID_FIELD, expense.getId());
            contentValues.put(TITLE_FIELD, expense.getTitle());
            contentValues.put(CASH_FIELD,expense.isCash());
            contentValues.put(RETRAIT_FIELD,expense.isRetrait());
            contentValues.put(CATEGORY_FIELD,expense.getCategory());
            contentValues.put(AMOUNT_FIELD,expense.getAmount());
            contentValues.put(DATE_FIELD,Utils.getStringFromDate(expense.getDate(),dateFormat));
            contentValues.put(REMBOURSE_FIELD,expense.isRembourse());
            contentValues.put(REMBOURSE_CASH_FIELD,expense.isRembourseCash());

            sqLiteDatabase.update(TABLE_NAME, contentValues, ID_FIELD + " =? ", new String[]{String.valueOf(expense.getId())});
        }
        sqLiteDatabase.close();
    }

    public void fillDB() {

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        for (Expense expense : Expense.expenseMap.values()){
            ContentValues contentValues = new ContentValues();
            contentValues.put(ID_FIELD, expense.getId());
            contentValues.put(TITLE_FIELD, expense.getTitle());
            contentValues.put(CASH_FIELD,expense.isCash());
            contentValues.put(RETRAIT_FIELD,expense.isRetrait());
            contentValues.put(CATEGORY_FIELD,expense.getCategory());
            contentValues.put(AMOUNT_FIELD,expense.getAmount());
            contentValues.put(DATE_FIELD,Utils.getStringFromDate(expense.getDate(),dateFormat));
            contentValues.put(REMBOURSE_FIELD,expense.isRembourse());
            contentValues.put(REMBOURSE_CASH_FIELD,expense.isRembourseCash());

            sqLiteDatabase.update(TABLE_NAME, contentValues, ID_FIELD + " =? ", new String[]{String.valueOf(expense.getId())});
        }
        sqLiteDatabase.close();
    }

    public void deleteDB(Context context) {
        context.deleteDatabase(path);
        sqLiteManager =null;
    }
}