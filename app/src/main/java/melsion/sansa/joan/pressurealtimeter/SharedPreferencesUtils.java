package melsion.sansa.joan.pressurealtimeter;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by joan.sansa.melsion on 14/05/2018.
 * https://github.com/joansansam/PressureAltimeter
 */

public class SharedPreferencesUtils {

    public static void setString(Context context, String key, String value){
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key,value);
        editor.commit();
    }

    public static String getString(Context context, String key, String defaultValue){
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return preferences.getString(key,defaultValue);
    }

    public static void setInt(Context context, String key, int value){
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key,value);
        editor.commit();
    }

    public static int getInt(Context context, String key, int defaultValue){
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return preferences.getInt(key,defaultValue);
    }

    public static void setBoolean(Context context, String key, boolean value){
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key,value);
        editor.commit();
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue){
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return preferences.getBoolean(key,defaultValue);
    }

    public static void setFloat(Context context, String key, float value){
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(key,value);
        editor.commit();
    }

    public static float getFloat(Context context, String key, float defaultValue){
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return preferences.getFloat(key,defaultValue);
    }

    public static void setLong(Context context, String key, long value){
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key,value);
        editor.commit();
    }

    public static long getLong(Context context, String key, long defaultValue){
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return preferences.getLong(key,defaultValue);
    }

    public static void clearSharedPreferences(Context context){
        int serviceCalls = getInt(context, Constants.SERVICE_CALLS, 0);
        long firstServiceCall = getLong(context, Constants.FIRST_SERVICE_CALL, System.currentTimeMillis());

        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();

        setInt(context, Constants.SERVICE_CALLS, serviceCalls);
        setLong(context,Constants.FIRST_SERVICE_CALL, firstServiceCall);
    }

}
