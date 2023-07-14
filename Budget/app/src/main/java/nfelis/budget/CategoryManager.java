package nfelis.budget;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;

public class CategoryManager extends SQLiteOpenHelper {
    private static String PREFS_NAME;
    public static CategoryManager categoryManager;
    public static String DATABASE_CATEGORIES;
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Category";
    private static final String COUNTER = "Counter";
    private static final String ID_FIELD = "id";
    private static final String CATEGORY_FIELD = "category";
    private static final String COLOR_FIELD = "color";
    private static final String AMOUNT_FIELD = "amount";
    private static final String VISIBLE_FIELD = "visible";
    private static final String BUDGET_FIELD = "inBudget";
    private static int number_categories;

    /** Constructor of the categoryManager
     * */
    public CategoryManager(Context context)
    {
        super(context, DATABASE_CATEGORIES, null, DATABASE_VERSION);

    }

    /** this is actually called instead of the constructor. It avoids creating multiple instances of the manager
     *
     * @param context context of the application
     * @return returns a new instance of the categoryManager unless one is already existing and then returns that one
     */
    public static CategoryManager instanceOfDatabase(Context context)
    {
        PREFS_NAME = context.getString(R.string.prefName);
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, context.MODE_PRIVATE);
        DATABASE_CATEGORIES = preferences.getString("storage_location", null) + "/Category.db";
        if(categoryManager == null)
            categoryManager =  new CategoryManager(context);
        return categoryManager;
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
                .append(CATEGORY_FIELD)
                .append(" TEXT, ")
                .append(COLOR_FIELD)
                .append(" TEXT, ")
                .append(AMOUNT_FIELD)
                .append(" INT, ")
                .append(VISIBLE_FIELD)
                .append(" INT, ")
                .append(BUDGET_FIELD)
                .append(" INT)");
        sqLiteDatabase.execSQL(sql.toString());

        //initial value
        ContentValues autre = new ContentValues();
        number_categories++;
        autre.put(ID_FIELD,number_categories);
        autre.put(CATEGORY_FIELD,"Autre");
        autre.put(COLOR_FIELD,"#FFFFFF");
        autre.put(AMOUNT_FIELD,0);
        autre.put(VISIBLE_FIELD,false);
        autre.put(BUDGET_FIELD,false);

        sqLiteDatabase.insert(TABLE_NAME, null, autre);
        Category.categoryMap.put(number_categories,new Category(number_categories,"Autre",0,"#FFFFFF",false,false));


        /*File databaseFile = new File(DATABASE_CATEGORIES);

        if (databaseFile.exists()) {
            // Set the file permissions to readable and writable
            boolean success = databaseFile.setReadable(true,false);
            databaseFile.setWritable(true,false);
        }

        databaseFile = new File(DATABASE_CATEGORIES+"-jounal");
        if (databaseFile.exists()) {
            boolean success = databaseFile.setReadable(true,false);
            databaseFile.setWritable(true,false);
        }*/


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


    /** Adds a single category to the database, based on a Category object
     * @param newCategory the new category we wish to add to the database
     * */
    public void addCategoryToDatabase(Category newCategory) {
        //il faudra check c'est la bonne db
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        number_categories++;
        newCategory.setId(number_categories);
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID_FIELD,newCategory.getId());
        contentValues.put(CATEGORY_FIELD,newCategory.getName());
        contentValues.put(COLOR_FIELD,newCategory.getColor());
        contentValues.put(AMOUNT_FIELD,newCategory.getAmount());
        contentValues.put(VISIBLE_FIELD,newCategory.isVisible());
        contentValues.put(BUDGET_FIELD,newCategory.isInBudget());

        sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        sqLiteDatabase.close();

    }

    /** Fills the category maps depending on if we are working in percentage mode or not
     *
     * @param percent true if we are in percentage mode. In which case, two maps are used depending
     *                if the category is in the budget or not
     */
    public void populateCategorySet(boolean percent) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Category.categoryMap.clear();
        Category.categoryNonMap.clear();


        try (Cursor result = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null))
        {
            if(result.getCount() != 0)
            {
                while (result.moveToNext())
                {
                    int id = result.getInt(1);
                    String name = result.getString(2);
                    String color = result.getString(3);
                    int amount = result.getInt(4);
                    boolean visible = result.getInt(5) == 1;
                    boolean inBudget = result.getInt(6)==1;
                    number_categories = Math.max(number_categories,id);
                    Category category = new Category(id,name,amount,color,visible,inBudget);
                    if (percent && !inBudget) {
                        Category.categoryNonMap.put(id,category);
                    } else {
                        Category.categoryMap.put(id,category);
                    }
                }
            }
        }
        sqLiteDatabase.close();
    }

    /** Deletes a category in the database and the category map
     * @param category the object we wish to delete*/
    public void deleteCategoryInDB(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, ID_FIELD +  "= ?", new String[]{String.valueOf(category.getId())});
        db.close();
        Category.categoryMap.remove(category.getId());
        Category.categoryNonMap.remove(category.getId());
    }

    /** Updates the category in the database based on the id
     *
     * @param category the category with its new parameters
     */
    public void updateCategoryInDB(Category category) {
        SQLiteDatabase categoryDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(CATEGORY_FIELD,category.getName());
        contentValues.put(COLOR_FIELD,category.getColor());
        contentValues.put(AMOUNT_FIELD,category.getAmount());
        contentValues.put(VISIBLE_FIELD,category.isVisible());
        contentValues.put(ID_FIELD,category.getId());
        contentValues.put(BUDGET_FIELD,category.isInBudget());

        categoryDatabase.update(TABLE_NAME,contentValues,ID_FIELD + " =? ", new String[]{String.valueOf(category.getId())});
        categoryDatabase.close();
    }

    /** deletes the database
     *
     * @param context context of the application
     */
    public void deleteDB(Context context) {
        context.deleteDatabase(DATABASE_CATEGORIES);
        categoryManager =null;
    }

    public void fillDB() {
        SQLiteDatabase categoryDatabase = this.getWritableDatabase();
        for (Category category : Category.categoryMap.values()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CATEGORY_FIELD,category.getName());
            contentValues.put(COLOR_FIELD,category.getColor());
            contentValues.put(AMOUNT_FIELD,category.getAmount());
            contentValues.put(VISIBLE_FIELD,category.isVisible());
            contentValues.put(ID_FIELD,category.getId());
            contentValues.put(BUDGET_FIELD,category.isInBudget());

            categoryDatabase.update(TABLE_NAME,contentValues,ID_FIELD + " =? ", new String[]{String.valueOf(category.getId())});
        }
        categoryDatabase.close();
    }

    public void setBudget0() {
        SQLiteDatabase categoryDatabase = this.getWritableDatabase();

        // Construct the SQL update statement
        String sql = "UPDATE " + TABLE_NAME + " SET " + AMOUNT_FIELD + " = 0 WHERE " + BUDGET_FIELD + " = 1";

        // Execute the update statement
        categoryDatabase.execSQL(sql);
    }
}
