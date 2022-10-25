package com.xfarrow.locatemydevice;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
    public static final int PASSWORD = 0;
    public static final int SMS_COMMAND = 1;
    public static final int WHITELIST_ENABLED = 2;

    private final SharedPreferences sharedPreferences;

    public Settings(Context context){
        sharedPreferences = context.getSharedPreferences("locatemydevice.settings", Context.MODE_PRIVATE);
    }

    public void set(int setting, String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(String.valueOf(setting), value);
        editor.apply();
    }

    public String get(int setting){
        return sharedPreferences.getString(String.valueOf(setting), defaultValues(setting));
    }

    public String defaultValues(int setting){
        switch (setting){
            case PASSWORD:
                return CipherUtils.get256Sha("0000");
            case SMS_COMMAND:
                return "LMD";
            case WHITELIST_ENABLED:
                return "false";
        }
        return null;
    }
}
