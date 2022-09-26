package com.xfarrow.locatemydevice;

public class Utils {

    public static final int PERMISSION_SMS_RECEIVE = 100;
    public static final int PERMISSION_SMS_SEND = 101;
    public static final int PERMISSION_ACCESS_FINE_LOCATION = 102;


    public static final String LOCATE_OPTION = "locate";
    public static final String CELLULAR_INFO_OPTION = "cellinfo";

    public static String buildOSMLink(double latitude, double longitude){
        return "https://www.openstreetmap.org/?mlat=" + latitude + "&mlon=" + longitude;
    }

}
