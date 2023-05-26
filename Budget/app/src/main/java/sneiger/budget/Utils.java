package sneiger.budget;

import android.util.Pair;
import android.widget.ListView;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

}
