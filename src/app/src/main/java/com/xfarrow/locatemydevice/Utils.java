package com.xfarrow.locatemydevice;

import android.telephony.SmsManager;

import androidx.annotation.Nullable;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import java.util.ArrayList;
import java.util.Locale;

public class Utils {

    public static final int PERMISSION_MULTIPLE = 100;
    public static final int PERMISSION_ACCESS_BACKGROUND_LOCATION = 101;


    public static final String LOCATE_OPTION = "locate";
    public static final String CELLULAR_INFO_OPTION = "cellinfo";
    public static final String BATTERY_OPTION = "battery";
    public static final String CALL_ME_OPTION = "callme";
    public static final String WIFI_OPTION = "wifi";
    public static final String LOCK_OPTION = "lock";
    public static final String SHOW_MESSAGE_OPTION = "show";
    public static final String RING_OPTION = "ring";
    public static final String WIFI_ENABLE_SUBOPTION = "-enable";
    public static final String WIFI_DISABLE_SUBOPTION = "-disable";

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

}
