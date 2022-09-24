package com.xfarrow.locatemydevice;

public class Utils {
    public static final String LOCATE_OPTION = "locate";
    public static String buildOSMLink(double latitude, double longitude){
        return "https://www.openstreetmap.org/?mlat=" + latitude + "&mlon=" + longitude;
    }
}
