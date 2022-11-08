package com.xfarrow.locatemydevice;

import static android.content.Context.DEVICE_POLICY_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SmsHandler {

    /*
    * TODO When Location is automatically enabled, it is not fast enough to lock on to the satellite.
    *  This causes:
    *   1. On Android >= API 31 a location = null
    *   2. On Android < API 31 to never fire onLocationChanged()
    *   We should wait for the signal to stabilize before calling these methods but a prefixed sleep
    *  time might be wrong and/or inefficient. Help wanted!
    * https://stackoverflow.com/questions/74367356/problem-with-locationmanager-and-broadcastreceiver-in-android
     */

    /*
     * Messages:
     * [command] [password] [option]
     *
     */
    @SuppressLint("ObsoleteSdkInt")
    public void handleSms(String message, String sender, Context context) {
        Settings settings = new Settings(context);
        SmsManager smsManager = SmsManager.getDefault();
        String password = settings.getString(Settings.PASSWORD);
        String command = settings.getString(Settings.SMS_COMMAND);
        String providedOption = "";
        String providedPassword = "";

        // Deny communication to those not in the whitelist, if enabled.
        // Deny communication if the message does not start with "LMD "
        WhitelistDbHandler whitelistDbHandler = new WhitelistDbHandler(context);
        if(!message.startsWith(command + " ") || (settings.getBoolean(Settings.WHITELIST_ENABLED) && !whitelistDbHandler.isContactPresent(sender))){
            return;
        }

        String[] splitMessage = message.split(" ");
        if(Arrays.stream(splitMessage).count() < 3){
            return;
        }
        providedPassword = splitMessage[1];
        providedOption = splitMessage[2].toLowerCase();

        // Deny communication if the password is not valid
        if (!CipherUtils.get256Sha(providedPassword).equals(password)) {
            return;
        }

        // locate
        if (providedOption.equals(Utils.LOCATE_OPTION)) {

            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // location not enabled and unable to turn it on
            if(!locationManager.isLocationEnabled() && !Utils.toggleLocationOn(context)){
                String response = "Location is not enabled. Unable to serve request.";
                Utils.sendSms(smsManager, response, sender);
                return;
            }

            // Location permission not granted
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String response = "Location permission is not granted. Unable to serve request.";
                Utils.sendSms(smsManager, response, sender);
                return;
            }

            // API 31 and above
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                locationManager.getCurrentLocation(LocationManager.FUSED_PROVIDER, null, context.getMainExecutor(), new Consumer<Location>() {
                    @Override
                    public void accept(Location location) {
                        if(location == null)
                            return;

                        String response = Utils.buildCoordinatesResponse(location.getLatitude(), location.getLongitude());
                        Utils.sendSms(smsManager, response , sender);
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
                        Log.d("SmsHandler", "Going to send coordinates: " + location.getLatitude() + " " + location.getLongitude());
                        String response = Utils.buildCoordinatesResponse(location.getLatitude(), location.getLongitude());
                        Utils.sendSms(smsManager, response, sender);
                    }

                    @Override
                    public void onProviderEnabled(@NonNull String provider) {
                        Log.d("SmsHandler", "PROVIDER ENABLED");
                    }

                    @Override
                    public void onProviderDisabled(@NonNull String provider) {
                        Log.d("SmsHandler", "PROVIDER DISABLED");
                    }
                }, null);
            }
        }

        // cellinfo
        else if(providedOption.equals(Utils.CELLULAR_INFO_OPTION)) {

            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            StringBuilder responseSms = new StringBuilder();

            responseSms.append("Network country: ");
            String country = telephony.getNetworkCountryIso();
            responseSms.append(Utils.getCountryNameByIso(country)).append("\n\n");

            // getAllCellInfo() requires Location services to work
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isLocationOn;
            isLocationOn = locationManager.isLocationEnabled() || Utils.toggleLocationOn(context);

            List<CellInfo> availableTowersInRange = telephony.getAllCellInfo();
            responseSms.append("Towers in range: ");
            if(availableTowersInRange.size() == 0) {
                if(isLocationOn){
                    responseSms.append("none");
                }
                else{
                    responseSms.append("unable to serve request, location is off");
                }
            }

            for(CellInfo tower : availableTowersInRange){
                responseSms.append("\n\n");

                if(tower.isRegistered()){
                    responseSms.append("[Connected to this tower]\n");
                }
                if (tower instanceof CellInfoWcdma) {
                    responseSms.append("Radio Type: WCDMA\n");
                    responseSms.append("Strength: ");
                    responseSms.append(((CellInfoWcdma)tower).getCellSignalStrength().getLevel()).append("/4\n");
                    responseSms.append("CID: ").append(((CellInfoWcdma) tower).getCellIdentity().getCid()).append("\n");
                    responseSms.append("LAC: ").append(((CellInfoWcdma) tower).getCellIdentity().getLac()).append("\n");
                    responseSms.append("MCC: ").append(((CellInfoWcdma) tower).getCellIdentity().getMccString()).append("\n");
                    responseSms.append("MNC: ").append(((CellInfoWcdma) tower).getCellIdentity().getMncString()).append("\n");
                }
                else if (tower instanceof CellInfoGsm) {
                    responseSms.append("Radio Type: GSM\n");
                    responseSms.append("Strength: ");
                    responseSms.append(((CellInfoGsm)tower).getCellSignalStrength().getLevel()).append("/4\n");
                    responseSms.append("CID: ").append(((CellInfoGsm) tower).getCellIdentity().getCid()).append("\n");
                    responseSms.append("LAC: ").append(((CellInfoGsm) tower).getCellIdentity().getLac()).append("\n");
                    responseSms.append("MCC: ").append(((CellInfoGsm) tower).getCellIdentity().getMccString()).append("\n");
                    responseSms.append("MNC: ").append(((CellInfoGsm) tower).getCellIdentity().getMncString()).append("\n");
                }
                else if (tower instanceof CellInfoLte) {
                    responseSms.append("Radio Type: LTE\n");
                    responseSms.append("Strength: ");
                    responseSms.append(((CellInfoLte)tower).getCellSignalStrength().getLevel()).append("/4\n");
                    responseSms.append("CI: ").append(((CellInfoLte) tower).getCellIdentity().getCi()).append("\n");
                    responseSms.append("TAC: ").append(((CellInfoLte) tower).getCellIdentity().getTac()).append("\n");
                    responseSms.append("MCC: ").append(((CellInfoLte) tower).getCellIdentity().getMccString()).append("\n");
                    responseSms.append("MNC: ").append(((CellInfoLte) tower).getCellIdentity().getMncString()).append("\n");
                }
                else if (tower instanceof CellInfoCdma) {
                    responseSms.append("Radio Type: CDMA\n");
                    responseSms.append("Strength: ");
                    responseSms.append(((CellInfoCdma)tower).getCellSignalStrength().getLevel()).append("/4\n");
                    responseSms.append("Latitude: ").append(((CellInfoCdma) tower).getCellIdentity().getLatitude()).append("\n");
                    responseSms.append("Longitude: ").append(((CellInfoCdma) tower).getCellIdentity().getLongitude()).append("\n");
                    responseSms.append("Network ID: ").append(((CellInfoCdma) tower).getCellIdentity().getNetworkId()).append("\n");
                    responseSms.append("System ID: ").append(((CellInfoCdma) tower).getCellIdentity().getSystemId()).append("\n");
                }
            }
            Utils.sendSms(smsManager, responseSms.toString(), sender);
        }

        // battery
        else if(providedOption.equals(Utils.BATTERY_OPTION)){
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);

            StringBuilder responseSms = new StringBuilder();

            // Battery level
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = level * 100 / (float)scale;
            responseSms.append("Battery level: ").append(Math.round(batteryPct)).append("%\n");

            // Are we charging / charged?
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL;
            responseSms.append("Charging: ");
            if(isCharging) responseSms.append("Yes\n");
            else responseSms.append("No");

            // How are we charging?
            if(isCharging) {
                int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                responseSms.append("Charging through: ");
                if(chargePlug == BatteryManager.BATTERY_PLUGGED_USB)
                    responseSms.append("USB");
                else if(chargePlug == BatteryManager.BATTERY_PLUGGED_AC)
                    responseSms.append("AC (wall)");
                else if( chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS)
                    responseSms.append("Wireless");
                else
                    responseSms.append("Unknown");
            }
            Utils.sendSms(smsManager, responseSms.toString(), sender);
        }

        //callme
        // Permission restrictions: https://developer.android.com/guide/components/activities/background-starts
        // So we ask to grant the SYSTEM_ALERT_WINDOW permission
        else if(providedOption.equals(Utils.CALL_ME_OPTION)){
            // if canDrawOverlays() returns false, not necessarily it will be impossible to
            // launch a call.
            if(!android.provider.Settings.canDrawOverlays(context)){
                Utils.sendSms(smsManager, "\"Display over other app\" permission was not " +
                        "granted, thus it might be impossible to initiate a call.", sender);
            }
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + sender));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        // wifi
        else if(providedOption.equals(Utils.WIFI_OPTION)){
            StringBuilder responseSms = new StringBuilder();

            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && !locationManager.isLocationEnabled() && !Utils.toggleLocationOn(context)) {
                responseSms.append("Location is off. Unable to execute command.");
                Utils.sendSms(smsManager, responseSms.toString(), sender);
                return;
            }

            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            responseSms.append("Wifi enabled: ");

            if (!wifiManager.isWifiEnabled()) {
                responseSms.append("No");
                Utils.sendSms(smsManager, responseSms.toString(), sender);
                return;
            }

            responseSms.append("Yes\n");
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = (wifiInfo.getSSID().equals(WifiManager.UNKNOWN_SSID)) ?
                    "Not connected or unknown" : wifiInfo.getSSID();
            String bssid = (wifiInfo.getBSSID().equals("02:00:00:00:00:00")) ?
                    "Not connected or unknown" : wifiInfo.getBSSID();
            responseSms.append("SSID: ").append(ssid).append("\n");
            responseSms.append("BSSID: ").append(bssid).append("\n");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                responseSms.append("Strength: ").append(wifiManager.calculateSignalLevel(wifiInfo.getRssi()))
                        .append("/").append(wifiManager.getMaxSignalLevel()).append("\n");
            }
            responseSms.append("\nNearby networks:");
            List<ScanResult> scanResults = wifiManager.getScanResults();
            for (ScanResult scanResult : scanResults) {
                responseSms.append("\n");
                responseSms.append("SSID: ").append(scanResult.SSID).append("\n");
                responseSms.append("BSSID: ").append(scanResult.BSSID).append("\n");
                responseSms.append("Security: ").append(scanResult.capabilities).append("\n");
            }
            Utils.sendSms(smsManager, responseSms.toString(), sender);
        }

        // wifi-on OR wifi-off
        else if(providedOption.contains(Utils.WIFI_OPTION) && (providedOption.contains(Utils.ON_SUBOPTION)
        || providedOption.contains(Utils.OFF_SUBOPTION))){

            StringBuilder responseSms = new StringBuilder();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                responseSms.append("Your device does not support this option.");
                Utils.sendSms(smsManager, responseSms.toString(), sender);
                return;
            }

            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(providedOption.contains(Utils.ON_SUBOPTION));
            responseSms.append("Command executed");
            Utils.sendSms(smsManager, responseSms.toString(), sender);
        }

        // lock
        else if(providedOption.equals(Utils.LOCK_OPTION)){
            StringBuilder responseSms = new StringBuilder();

            DevicePolicyManager mgr = (DevicePolicyManager)context.getSystemService(DEVICE_POLICY_SERVICE);
            if(!mgr.isAdminActive(new ComponentName(context, AdminReceiver.class))){
                responseSms.append("No admin permission. Aborted");
                Utils.sendSms(smsManager, responseSms.toString(), sender);
                return;
            }
            mgr.lockNow();
            responseSms.append("Locked");
            Utils.sendSms(smsManager, responseSms.toString(), sender);
        }

        // show
        else if(providedOption.contains(Utils.SHOW_MESSAGE_OPTION)){
            String messageToDisplay;
            try {
                 messageToDisplay = message.substring(message.indexOf("\"") + 1,
                        message.lastIndexOf("\""));
            }
            catch(StringIndexOutOfBoundsException ex){
                Utils.sendSms(smsManager, "Wrong usage", sender);
                return;
            }
            Intent lockScreenMessage = new Intent(context, ShowMessageActivity.class);
            lockScreenMessage.putExtra(Utils.SHOW_MESSAGE_OPTION, messageToDisplay);
            lockScreenMessage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(lockScreenMessage);
            Utils.sendSms(smsManager, "Displayed on screen", sender);
        }

        // ring
        else if(providedOption.equals(Utils.RING_OPTION)){

            // In this way the smartphone will ring only if the overlay on screen permission
            // has been granted. This is not something inherently related to ringing,
            // rather on the Activity used to show the button "Stop". We could make the phone ring
            // without the overlay permission, but there would be no way to stop it except for
            // powering it off or closing it from the recent applications (impossible if the phone
            // is lost and locked).
            if(!android.provider.Settings.canDrawOverlays(context)) {
                Utils.sendSms(smsManager, "Unable to ring. Overlay permission not granted.",
                        sender);
                return;
            }
            Intent ringerActivityIntent = new Intent(context, RingerActivity.class);
            ringerActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(ringerActivityIntent);

            Utils.sendSms(smsManager, "Ringing", sender);
        }

        else{
            StringBuilder responseSms = new StringBuilder("This option is not valid. Available options:\n\n");

            responseSms.append(Utils.LOCATE_OPTION + ": Will return the most accurate set of coordinates possible " +
                        "and a link to them pinpointed to OpenStreetMap.\n\n");

            responseSms.append(Utils.CELLULAR_INFO_OPTION + ": Will return a set of uniquely identifiable information" +
                    " about cell towers near the phone. You can then put this information" +
                    " on OpenCellId to individuate the smartphone's approximate location.\n\n");

            responseSms.append(Utils.BATTERY_OPTION + ": Will return battery info.\n\n");

            responseSms.append(Utils.LOCK_OPTION + ": Will lock down the smartphone.\n\n");

            responseSms.append(Utils.SHOW_MESSAGE_OPTION + " \"message\": Will show a message on the screen, even when it's locked.\n\n");

            responseSms.append(Utils.CALL_ME_OPTION + ": You will receive a call from the lost smartphone\n\n");

            responseSms.append(Utils.WIFI_OPTION + ": Will return Wi-Fi infos.\n\n");

            responseSms.append(Utils.WIFI_OPTION + Utils.ON_SUBOPTION + ": Will enable Wi-Fi (Only API < 29).\n\n");

            responseSms.append(Utils.WIFI_OPTION + Utils.OFF_SUBOPTION + ": Will disable Wi-Fi (Only API < 29).\n\n");

            responseSms.append(Utils.RING_OPTION + ": Will make the smartphone ring.\n\n");

            Utils.sendSms(smsManager, responseSms.toString(), sender);
        }
    }
}
