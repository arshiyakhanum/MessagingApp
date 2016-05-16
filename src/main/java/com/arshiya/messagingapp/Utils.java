package com.arshiya.messagingapp;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by arshiya on 8/5/16.
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();


    public String getDate(Date date){
        Calendar input = Calendar.getInstance();
        input.setTime(date);

        int iYear = input.get(Calendar.YEAR);
        int iDayOfMonth = input.get(Calendar.DAY_OF_MONTH);
        int iMonth = input.get(Calendar.MONTH);
        int idayOfWeek = input.get(Calendar.DAY_OF_WEEK);

        Calendar current = Calendar.getInstance();

        int cYear = current.get(Calendar.YEAR);
        int cDayOfMonth = current.get(Calendar.DAY_OF_MONTH);
        int cMonth = current.get(Calendar.MONTH);
        int cdayOfWeek = current.get(Calendar.DAY_OF_WEEK);

        String dateRes = "";

        if (iYear == cYear){
            if (iMonth == cMonth && iDayOfMonth == cDayOfMonth){
                int hour = input.get(Calendar.HOUR);
                if (0 == hour){
                    hour = 12;
                }
                dateRes = hour + ":" + String.format("%02d",input.get(Calendar.MINUTE)) + " " + ((input.get(Calendar.AM_PM) == 1)? "PM" : "AM");
            } else {
                dateRes = getMonth(iMonth) + " " + iDayOfMonth;
            }

        }else {
            //else return Month date year
            dateRes = getMonth(iMonth) + " " + iDayOfMonth + " " + iYear;
        }

        Log.d(TAG, "res : " + dateRes);
        return dateRes;
    }

    private String getMonth(int month){
        String[] monthsArray = new String[] {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return monthsArray[month];
    }

    private String getDayOfTheWeek(int day){
        String[] daysArray = new String[] {"sunday","monday","tuesday","wednesday","thursday","friday", "saturday"};
        return daysArray[day - 1];
    }
}
