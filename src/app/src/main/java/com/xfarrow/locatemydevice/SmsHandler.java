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
        if (providedOption.equals(Utils.LOCATE_OPTION)) {

            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            if(!locationManager.isLocationEnabled()){
                // TODO: get last known location (requies google play services)
                smsManager.sendTextMessage(sender, null,
                        "Location is not enabled. " +
                                "Unable to serve request.",null, null);
                return;
            }

            // Location permission not granted
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    smsManager.sendTextMessage(sender, null,
                        "Location permission is not granted. " +
                        "Unable to serve request.",null, null);
                return;
            }

            // API 31 and above
            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                locationManager.getCurrentLocation(LocationManager.FUSED_PROVIDER, null, context.getMainExecutor(), new Consumer<Location>() {
                    @Override
                    public void accept(Location location) {
                        sendGpsCoordinates(smsManager, sender, location.getLatitude(), location.getLongitude());
                    }
                });
            }

            // Legacy (API < 31)
            else{
                Criteria locationCriteria = new Criteria();
                locationCriteria.setAccuracy(Criteria.ACCURACY_FINE);
                locationManager.requestSingleUpdate(locationCriteria, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        sendGpsCoordinates(smsManager, sender, location.getLatitude(), location.getLongitude());
                    }
                }, null);
            }
        }
    }

    private void sendGpsCoordinates(SmsManager smsManager, String sendTo, double latitude, double longitude){
        smsManager.sendTextMessage(sendTo, null,
                "GPS coordinates are:" +
                        "\nLatitude: " + latitude +
                        "\nLongitude: " + longitude + "\n" +
                        Utils.buildOSMLink(latitude, longitude), null, null);
    }
}
