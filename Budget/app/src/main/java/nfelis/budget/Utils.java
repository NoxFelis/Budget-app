package nfelis.budget;

import static android.content.Context.MODE_PRIVATE;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Pair;
import android.widget.ListView;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.net.URISyntaxException;

public class Utils {
    public static List<String> setDateLimits(int month, int year) {
        int dayStart = 1;
        int dayEnd = 31;
        switch (month) {
            case 4:
            case 6:
            case 9:
            case 11:
                dayEnd = 30;
                break;
            case 2:
                dayEnd = (year%4==0 && (year%100 != 0 || year%400 == 0) ) ? 29 : 28;
                break;
        }
        return Arrays.asList(makeDateString(dayStart, month, year), makeDateString(dayEnd, month, year));
    }

    public static String makeDateString(int day, int month, int year) {
        String mon;
        if (month<10) {
            mon = "0" + Integer.toString(month);
        } else
        {
            mon = Integer.toString(month);
        }
        String da;
        if (day < 10) {
            da = "0" + Integer.toString(day);
        } else {
            da = Integer.toString(day);
        }
        return year + "-" + mon + "-" + da;
    }

    public static String getStringFromAmount(int amount) {
        int euro = amount/100;
        int cent = amount - euro*100;
        return Integer.toString(euro) + "." + Integer.toString(cent);
    }

    public static String getStringFromDate(Date date, DateFormat dateFormat)
    {
        if(date == null)
            return null;
        return dateFormat.format(date);
    }

    public static Date getDateFromString(String string,DateFormat dateFormat)
    {
        try
        {
            return dateFormat.parse(string);
        }
        catch (ParseException | NullPointerException e)
        {
            return null;
        }
    }

    public static String showDate(int month, int year) {
        String mois;
        switch(month) {
            case 1:
                mois = "Janvier ";
                break;
            case 2:
                mois = "Février ";
                break;
            case 3:
                mois = "Mars ";
                break;
            case 4:
                mois = "Avril ";
                break;
            case 5:
                mois = "Mai ";
                break;
            case 6:
                mois = "Juin ";
                break;
            case 7:
                mois = "Juillet ";
                break;
            case 8:
                mois = "Août ";
                break;
            case 9:
                mois = "Septembre ";
                break;
            case 10:
                mois = "Octobre ";
                break;
            case 11:
                mois = "Novembre ";
                break;
            case 12:
                mois = "Décembre ";
                break;
            default:
                mois = "Janvier ";
        }
        return mois + year;
    }

    public static String returnHEX(String color) {
        if (color.length() == 0) {
            return "#FFFFFF";
        } else {
            if (color.charAt(0) != Character.valueOf('#')) {
                return "#"+color;
            } else {
                return color;
            }
        }
    }

    public static List<String> setPeriodLimits(int monthEnd,int yearEnd) {

        int monthStart = monthEnd -5;
        int yearStart = yearEnd;
        if (monthStart <= 0) {
            monthStart = 12 - monthStart;
            yearStart--;
        }

        int dayStart = 1;
        int dayEnd = 31;
        switch (monthEnd) {
            case 4:
            case 6:
            case 9:
            case 11:
                dayEnd = 30;
                break;
            case 2:
                dayEnd = (yearEnd%4==0 && (yearEnd%100 != 0 || yearEnd%400 == 0) ) ? 29 : 28;
                break;
        }
        return Arrays.asList(makeDateString(dayStart, monthStart, yearStart), makeDateString(dayEnd, monthEnd, yearEnd));
    }
    public static String showPeriod(String startDate, String endDate) {
        String[] start = startDate.split("-");
        String[] end = endDate.split("-");
        int startMonth = Integer.parseInt(start[1]);
        int startYear = Integer.parseInt(start[0]);
        int endMonth = Integer.parseInt(end[1]);
        int endYear = Integer.parseInt(end[0]);

        return showDate(startMonth,startYear) + " - " + showDate(endMonth,endYear);
    }

    public static int getMaxDepense() {
        int maxDepense = 0;
        for (Category category : Category.categoryMap.values()) {
            if (category.isInBudget()) {
                maxDepense += category.getAmount()*100;
            }
        }
        return maxDepense;
    }

    @SuppressLint("NewApi")
    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        /*String selection = null;
        String[] selectionArgs = null;*/
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        //DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)
        if (uri != null && needToCheckUri ) {
            if (isExternalStorageDocument(uri)) {
                /*final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");*/
                final String[] split = uri.getPath().split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1]; // + "/" + split[1];
            } /*else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{ split[1] };
            }*/
        }
        /*if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }*/
        return "";
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

   /* public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }*/

    /*public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }*/

    public static void saveStorageLocation(Context context,String name, Uri uri,String file) throws URISyntaxException {
        String location;
        if (uri == null) {
            location = file;
        }else {
            location = Utils.getPath(context,uri) + "/nfelis.budget" + file;
        }
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.prefName), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(name, location);
        editor.apply();
    }

    public static void transferExpenses(Context context,String path) throws URISyntaxException {
        //upload all expenses
        SQLiteManager old = SQLiteManager.instanceOfDatabase(context,true);
        old.populateExpenseListArray(null,null);
        old.deleteDB(context);

        saveStorageLocation(context,"storage_expenses", null, path);
        SQLiteManager nouveau = SQLiteManager.instanceOfDatabase(context,true);
        nouveau.fillDB();
    }

    public static void transferCategories(Context context, String path) throws URISyntaxException {
        CategoryManager old = CategoryManager.instanceOfDatabase(context,true);
        old.populateCategorySet(false);
        old.deleteDB(context);

        saveStorageLocation(context,"storage_categories", null, path);
        CategoryManager nouveau = CategoryManager.instanceOfDatabase(context,true);
        nouveau.fillDB();
    }

    public static boolean comparePaths(Context context,String newPath) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String[] oldPath = preferences.getString("storage_expenses", null).split("/Expenses.db");
        return oldPath[0].equals(newPath);
    }

    public static int getResteDepense(Context context,int id) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.prefName), MODE_PRIVATE);
        int maxDepense = preferences.getInt(context.getString(R.string.total),Integer.parseInt(context.getString(R.string.default_total)));
        int occupied = 0;
        for (Category category : Category.categoryMap.values()) {
            if (id!=category.getId()) {
                int value = category.getAmount();
                occupied += Math.round( ((float)value / maxDepense) * 100);
            }
        }
        return 100-occupied;
    }

    public static int getPercentage(Context context,int amount) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.prefName), MODE_PRIVATE);
        int maxDepense = preferences.getInt(context.getString(R.string.total),Integer.parseInt(context.getString(R.string.default_total)));
        return Math.round(((float) amount/maxDepense)*100);
    }
}
