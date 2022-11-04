package com.xfarrow.locatemydevice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SmsManager;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import java.util.ArrayList;
import java.util.Locale;

public class Utils {

    public static final int PERMISSION_MULTIPLE = 100;
    public static final int PERMISSION_ACCESS_BACKGROUND_LOCATION = 101;
    public static final int PERMISSION_CALLS = 103;


    public static final String LOCATE_OPTION = "locate";
    public static final String CELLULAR_INFO_OPTION = "cellinfo";
    public static final String BATTERY_OPTION = "battery";
    public static final String CALL_ME_OPTION = "callme";
    public static final String WIFI_OPTION = "wifi";
    public static final String LOCK_OPTION = "lock";
    public static final String SHOW_MESSAGE_OPTION = "show";
    public static final String RING_OPTION = "ring";
    public static final String ON_SUBOPTION = "-on";
    public static final String OFF_SUBOPTION = "-off";

    public static String getCountryNameByIso(String iso){
        Locale locale = new Locale("", iso);
        return locale.getDisplayCountry();
    }

    public static void sendSms(SmsManager smsManager, String text, String sendTo){
        ArrayList<String> parts = smsManager.divideMessage(text);
        smsManager.sendMultipartTextMessage (sendTo, null, parts,null, null);
    }

    public static String buildCoordinatesResponse(double latitude, double longitude){
        return new StringBuilder().append("Coordinates are:")
                .append("\nLatitude: ")
                .append(latitude)
                .append("\nLongitude: ")
                .append(longitude)
                .append("\n")
                .append("https://www.openstreetmap.org/?mlat=").append(latitude).append("&mlon=").append(longitude).toString();
    }

    // returns the country code from a phone number. For example +393340000000 will return "39"
    @Nullable
    public static String extractCountryCodeFromPhoneNumber(String phoneNumber){
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber numberProto;
        try {
            numberProto = phoneUtil.parse(phoneNumber, "");
        } catch (NumberParseException e) {
            return null;
        }
        return String.valueOf(numberProto.getCountryCode());
    }

    // We'll remove parenthesis, dashes and whitespaces
    public static String normalizePhoneNumber(String phoneNo){
        return phoneNo.replaceAll("[-()\\s]", "");
    }

    // forcefully toggle location services on (secure setting - needs permission to be granted via adb)
    @SuppressLint("ObsoleteSdkInt")
    public static boolean toggleLocationOn(Context context){

        if (Build.VERSION.SDK_INT < 19) {
            return false;
        }

        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED){
            return false;
        }

        return android.provider.Settings.Secure.putInt(context.getContentResolver(),
                    android.provider.Settings.Secure.LOCATION_MODE,
                    android.provider.Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);

    }

    // forcefully toggle location services off (secure setting - needs permission to be granted via adb)
    @SuppressLint("ObsoleteSdkInt")
    public static boolean toggleLocationOff(Context context){

        if (Build.VERSION.SDK_INT < 19) {
            return false;
        }

        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED){
            return false;
        }
        
        return android.provider.Settings.Secure.putInt(context.getContentResolver(),
                    android.provider.Settings.Secure.LOCATION_MODE,
                    android.provider.Settings.Secure.LOCATION_MODE_OFF);
    }
}
