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
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.security.cert.CertPathValidatorException;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsHandler {
    /*
     * Messages:
     * [command] [password] [option]
     *
     */
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
                + Utils.LOCATE_OPTION + "|" + Utils.CELLULAR_INFO_OPTION;
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
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
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

        // cellinfo
        else if(providedOption.equals(Utils.CELLULAR_INFO_OPTION)) {

            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            StringBuilder resultSms = new StringBuilder();

            resultSms.append("Network country: ");
            String country = telephony.getNetworkCountryIso();
            resultSms.append(Utils.getCountryNameByIso(country)).append("\n\n");

            List<CellInfo> availableTowersInRange = telephony.getAllCellInfo();
            resultSms.append("Towers in range: ");
            if(availableTowersInRange.size() == 0) {
                resultSms.append("none or location is off.");
            }

            for(CellInfo tower : availableTowersInRange){
                resultSms.append("\n\n");

                if(tower.isRegistered()){
                    resultSms.append("[Connected to this tower]\n");
                }
                if (tower instanceof CellInfoWcdma) {
                    resultSms.append("Radio Type: WCDMA\n");
                    resultSms.append("Strength: ");
                    resultSms.append(((CellInfoWcdma)tower).getCellSignalStrength().getLevel()).append("/4\n");
                    resultSms.append("CID: ").append(((CellInfoWcdma) tower).getCellIdentity().getCid()).append("\n");
                    resultSms.append("LAC: ").append(((CellInfoWcdma) tower).getCellIdentity().getLac()).append("\n");
                    resultSms.append("MCC: ").append(((CellInfoWcdma) tower).getCellIdentity().getMccString()).append("\n");
                    resultSms.append("MNC: ").append(((CellInfoWcdma) tower).getCellIdentity().getMncString()).append("\n");
                }
                else if (tower instanceof CellInfoGsm) {
                    resultSms.append("Radio Type: GSM\n");
                    resultSms.append("Strength: ");
                    resultSms.append(((CellInfoGsm)tower).getCellSignalStrength().getLevel()).append("/4\n");
                    resultSms.append("CID: ").append(((CellInfoGsm) tower).getCellIdentity().getCid()).append("\n");
                    resultSms.append("LAC: ").append(((CellInfoGsm) tower).getCellIdentity().getLac()).append("\n");
                    resultSms.append("MCC: ").append(((CellInfoGsm) tower).getCellIdentity().getMccString()).append("\n");
                    resultSms.append("MNC: ").append(((CellInfoGsm) tower).getCellIdentity().getMncString()).append("\n");
                }
                else if (tower instanceof CellInfoLte) {
                    resultSms.append("Radio Type: LTE\n");
                    resultSms.append("Strength: ");
                    resultSms.append(((CellInfoLte)tower).getCellSignalStrength().getLevel()).append("/4\n");
                    resultSms.append("CI: ").append(((CellInfoLte) tower).getCellIdentity().getCi()).append("\n");
                    resultSms.append("TAC: ").append(((CellInfoLte) tower).getCellIdentity().getTac()).append("\n");
                    resultSms.append("MCC: ").append(((CellInfoLte) tower).getCellIdentity().getMccString()).append("\n");
                    resultSms.append("MNC: ").append(((CellInfoLte) tower).getCellIdentity().getMncString()).append("\n");
                }
                else if (tower instanceof CellInfoCdma) {
                    resultSms.append("Radio Type: CDMA\n");
                    resultSms.append("Strength: ");
                    resultSms.append(((CellInfoCdma)tower).getCellSignalStrength().getLevel()).append("/4\n");
                    resultSms.append("Latitude: ").append(((CellInfoCdma) tower).getCellIdentity().getLatitude()).append("\n");
                    resultSms.append("Longitude: ").append(((CellInfoCdma) tower).getCellIdentity().getLongitude()).append("\n");
                    resultSms.append("Network ID: ").append(((CellInfoCdma) tower).getCellIdentity().getNetworkId()).append("\n");
                    resultSms.append("System ID: ").append(((CellInfoCdma) tower).getCellIdentity().getSystemId()).append("\n");
                }
            }
            smsManager.sendTextMessage(sender, null, resultSms.toString(),null, null);
        }
    }

    private void sendGpsCoordinates(SmsManager smsManager, String sendTo, double latitude, double longitude){
        smsManager.sendTextMessage(sendTo, null,
                "Coordinates are:" +
                        "\nLatitude: " + latitude +
                        "\nLongitude: " + longitude + "\n" +
                        Utils.buildOSMLink(latitude, longitude), null, null);
    }
}
