package org.hcilab.circog;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Util {

    private static final String	TAG	= Util.class.getSimpleName();
    private static final SimpleDateFormat sdfEn					= new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private static final SimpleDateFormat sdfMySql				= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static long start_time				                = System.currentTimeMillis();

    public static double calculateAverage(List<Long> marks) {
        long sum = 0;
        for (Long mark : marks) {
            sum += mark;
        }
        return marks.isEmpty()? 0: 1.0*sum/marks.size();
    }

    public static String getLocale() {
        return "" + Locale.getDefault().toString();
    }

    public static String getTimezone() {
        return "" + Calendar.getInstance().get(Calendar.ZONE_OFFSET)
                / (60 * 60 * 1000);
    }

    /**
     * Returns a pseudo-random number between min and max.
     */
    public static int randInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    /**
     * parses a timestamp into a Date object, if not parseable, yesterday's Date is returned
     */
    public static Date getDateFromTimestamp(long ts) {
        Date date;
        try {
            date = new Date(ts);
        } catch (Exception pe) {
            pe.printStackTrace();
            //return date one day ago
            long DAY_IN_MS = 1000 * 60 * 60 * 24;
            date = new Date(System.currentTimeMillis() - (1 * DAY_IN_MS));
        }
        return date;
    }

//    SHARED PREFERENCES
    public static void putString(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(CircogPrefs.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS); //PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(key, value);
        edit.commit();
    }

    public static String getString(Context context, String key, String defValue) {
        SharedPreferences prefs = context.getSharedPreferences(CircogPrefs.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS); //PreferenceManager.getDefaultSharedPreferences(context);
        String result = prefs.getString(key, defValue);
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "getString for key: " + key + " (result: " + result + ", default: " + defValue + ")");
        }
        return result;
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences prefs = context.getSharedPreferences(CircogPrefs.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS); //PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences prefs = context.getSharedPreferences(CircogPrefs.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS); //PreferenceManager.getDefaultSharedPreferences(context);
        int result = prefs.getInt(key, defValue);
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "getInt for key: " + key + " (result: " + result + ", default: " + defValue + ")");
        }
        return result;
    }

    public static void putBool(Context context, String key, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(CircogPrefs.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS); //PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    public static boolean getBool(Context context, String key, boolean defValue) {
        SharedPreferences prefs = context.getSharedPreferences(CircogPrefs.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS); //PreferenceManager.getDefaultSharedPreferences(context);
        boolean result = prefs.getBoolean(key, defValue);
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "getBool for key: " + key + " (result: " + result + ", default: " + defValue + ")");
        }
        return result;
    }

    public static long getLong(Context context, String key, long defValue) {
        SharedPreferences prefs = context.getSharedPreferences(CircogPrefs.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS); //PreferenceManager.getDefaultSharedPreferences(context);
        long result = prefs.getLong(key, defValue);
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "getLong for key: " + key + " (result: " + result + ", default: " + defValue + ")");
        }
        return result;
    }

    public static void putLong(Context context, String key, long value) {
        SharedPreferences prefs = context.getSharedPreferences(CircogPrefs.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS); //PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putLong(key, value);
        edit.commit();
    }

    public static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "isSameDay: " + cal1.get(Calendar.DAY_OF_YEAR) + ", " + cal2.get(Calendar.DAY_OF_YEAR) + ": " + sameDay);
        }

        return sameDay;
    }

    public static int getRating(RadioGroup rg) {
        int rbId = rg.getCheckedRadioButtonId();
        if(rbId == -1) {
            return rbId;
        } else {
            View rb = rg.findViewById(rbId);
            return rg.indexOfChild(rb) + 1;
        }
    }

    public static long getUpTimeSec() {
        final long now = System.currentTimeMillis();
        final long upTimeSec = (now - start_time) / 1000;
        return upTimeSec;
    }

    public static String format(double d) {
        return format(d, 100.0);
    }

    public static String format(double d, double formatConstant) {
        int i = (int) (d * formatConstant);
        return "" + i / formatConstant;
    }

    public static String format(Location l) {
        try {
            return format(l.getLatitude(), 10000) + "/" + format(l.getLongitude(), 10000) + " " + l.getProvider();
        } catch (Exception e) {
            return "" + l;
        }
    }

    public static String dateEn() {
        final long now = System.currentTimeMillis();
        final String date = getTimeStringEn(now);
        return date;
    }

    public static String getTimeStringEn(long now) {
        Date d = new Date(now);
        return sdfEn.format(d);
    }

    public static String getTimeStringMySql(long now) {
        Date d = new Date(now);
        return sdfMySql.format(d);
    }

    public static boolean recursiveDelete(File file) {
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "recursiveDelete "+file);
        }
        if(file.isDirectory()) {
            boolean success = true;
            File [] files = file.listFiles();
            if(files != null) {
                for(File f : files) {
                    success |= recursiveDelete(f);
                }
            }
            return success;
        }else{
            return file.delete();
        }
    }

    public static boolean studyCompleted(Context context) {

        long dateRegistered = getLong(context, CircogPrefs.PREF_REGISTRATION_TIMESTAMP, System.currentTimeMillis());
        long now = System.currentTimeMillis();
        long diff = now - dateRegistered;
        boolean completed = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)>=CircogPrefs.MIN_STUDY_DAYS;

        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "studyCompleted: date registered: " + new Date(dateRegistered).toString() + ", now: " + new Date(now).toString() + ", days since: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + ", completed: " + completed);
        }

        return (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)>=CircogPrefs.MIN_STUDY_DAYS);
    }
}
