package com.xfarrow.locatemydevice;

import java.util.Arrays;
import java.util.Locale;

public class Utils {

    public static final int PERMISSION_MULTIPLE = 100;
    public static final int PERMISSION_ACCESS_BACKGROUND_LOCATION = 101;


    public static final String LOCATE_OPTION = "locate";
    public static final String CELLULAR_INFO_OPTION = "cellinfo";
    public static final String BATTERY_OPTION = "battery";

    public static String buildOSMLink(double latitude, double longitude){
        return "https://www.openstreetmap.org/?mlat=" + latitude + "&mlon=" + longitude;
    }

    public static String getCountryNameByIso(String iso){
        Locale locale = new Locale("", iso);
        return locale.getDisplayCountry();
    }

}
