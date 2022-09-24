package com.xfarrow.locatemydevice;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsHandler {
    /*
     * Messages:
     * finddevice 1234 locate
     * [command] [password] [option]
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    public void handleSms(String message, String sender, Context context) {
        Settings settings = new Settings(context);
        String password = settings.get(Settings.PASSWORD);
        String command = settings.get(Settings.SMS_COMMAND);
        String providedOption = "";
        String providedPassword = "";

        String regexToMatch = "^"
                + command
                + "\\s"
                + "[^\\s]*"
                + "\\s"
                + Utils.LOCATE_OPTION;
        Pattern pattern = Pattern.compile(regexToMatch);
        Matcher matcher = pattern.matcher(message);
        if (!matcher.find()) {
            return;
        }

        String[] splitMessage = message.split(" ");
        providedPassword = splitMessage[1];
        providedOption = splitMessage[2];

        if (!CipherUtils.get256Sha(providedPassword).equals(password)) {
            return;
        }

        SmsManager smsManager = SmsManager.getDefault();

        // locate
        if (providedOption.equals(Settings.LOCATE_OPTION)) {

            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // GPS is off
            if (!locationManager.isLocationEnabled()) {
                smsManager.sendTextMessage(sender, null, "GPS is off ", null, null);
                return;
            }

            // GPS permission not granted
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                smsManager.sendTextMessage(sender, null,
                        "GPS permission is not granted. " +
                        "Unable to serve request.",null, null);
                return;
            }

            // API 30 and above
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER, null, context.getMainExecutor(), new Consumer<Location>() {
                    @Override
                    public void accept(Location location) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        smsManager.sendTextMessage(sender, null,
                                "GPS coordinates are: " +
                                "\nLatitude: " + latitude +
                                "\nLongitude: " + longitude + "\n" +
                                Utils.buildOSMLink(latitude, longitude), null, null);
                    }
                });
            }

            // Legacy (API < 29)
            else{
                Criteria locationCriteria = new Criteria();
                locationCriteria.setAccuracy(Criteria.ACCURACY_FINE);
                locationManager.requestSingleUpdate(locationCriteria, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        smsManager.sendTextMessage(sender, null,
                                "GPS coordinates are:" +
                                        "\nLatitude: " + latitude +
                                        "\nLongitude: " + longitude + "\n" +
                                        Utils.buildOSMLink(latitude, longitude), null, null);
                    }
                }, null);
            }
        }
    }
}
