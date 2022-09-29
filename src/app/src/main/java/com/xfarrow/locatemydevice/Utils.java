package com.xfarrow.locatemydevice;

import android.telephony.SmsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class Utils {

    public static final int PERMISSION_MULTIPLE = 100;
    public static final int PERMISSION_ACCESS_BACKGROUND_LOCATION = 101;


    public static final String LOCATE_OPTION = "locate";
    public static final String CELLULAR_INFO_OPTION = "cellinfo";
    public static final String BATTERY_OPTION = "battery";
    public static final String CALL_ME_OPTION = "callme";
    public static final String WIFI_OPTION = "wifi";

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

}
