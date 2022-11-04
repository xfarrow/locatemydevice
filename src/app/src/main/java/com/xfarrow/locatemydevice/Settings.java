package com.xfarrow.locatemydevice;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
    public static final int PASSWORD = 0;
    public static final int SMS_COMMAND = 1;
    /* integer 2 was used for WHITE_LIST_ENABLED till 1.0.4 as string, now it's a boolean
    ** so using WHITELIST_ENABLED = 2 might make devices crash.
     */
    public static final int WHITELIST_ENABLED = 3;
    public static final int DO_NOT_SHOW_OVERLAY_PERMISSION_AGAIN = 4;
    public static final int DO_NOT_SHOW_DEVICE_ADMIN_PERMISSION_AGAIN = 5;

    private final SharedPreferences sharedPreferences;

    public Settings(Context context){
        sharedPreferences = context.getSharedPreferences("locatemydevice.settings", Context.MODE_PRIVATE);
    }

    public void setString(int setting, String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(String.valueOf(setting), value);
        editor.apply();
    }

    public void setBoolean(int setting, boolean value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(String.valueOf(setting), value);
        editor.apply();
    }

    public String getString(int setting){
        return sharedPreferences.getString(String.valueOf(setting), (String)defaultValues(setting));
    }

    public boolean getBoolean(int setting){
        return sharedPreferences.getBoolean(String.valueOf(setting), (Boolean)defaultValues(setting));
    }

    public Object defaultValues(int setting){
        switch (setting){
            case PASSWORD:
                return CipherUtils.get256Sha("0000");
            case SMS_COMMAND:
                return "LMD";
            case WHITELIST_ENABLED:
            case DO_NOT_SHOW_OVERLAY_PERMISSION_AGAIN:
            case DO_NOT_SHOW_DEVICE_ADMIN_PERMISSION_AGAIN:
                return false;
        }
        return null;
    }
}
