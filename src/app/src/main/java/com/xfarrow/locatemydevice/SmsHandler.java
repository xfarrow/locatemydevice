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
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
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
    @SuppressLint("ObsoleteSdkInt")
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
                + Utils.LOCATE_OPTION
                + "|"
                + Utils.CELLULAR_INFO_OPTION
                + "|"
                + Utils.BATTERY_OPTION
                + "|"
                + Utils.CALL_ME_OPTION
                + "|"
                + Utils.WIFI_OPTION + "((" + Utils.WIFI_ENABLE_SUBOPTION + ")|(" + Utils.WIFI_DISABLE_SUBOPTION + "))?"
                + "|"
                + Utils.LOCK_OPTION
                + "|"
                + Utils.SHOW_MESSAGE_OPTION + "\\s+\"[\\w\\W]*[\"]$";

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

            // location not enabled
            if(!locationManager.isLocationEnabled()){
                // TODO: get last known location (requies google play services)
                String response ="Location is not enabled. Unable to serve request.";
                Utils.sendSms(smsManager, response, sender);
                return;
            }

            // Location permission not granted
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String response ="Location permission is not granted. Unable to serve request.";
                Utils.sendSms(smsManager, response, sender);
                return;
            }

            // API 31 and above
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                locationManager.getCurrentLocation(LocationManager.FUSED_PROVIDER, null, context.getMainExecutor(), new Consumer<Location>() {
                    @Override
                    public void accept(Location location) {
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
                        String response = Utils.buildCoordinatesResponse(location.getLatitude(), location.getLongitude());
                        Utils.sendSms(smsManager, response, sender);
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

            List<CellInfo> availableTowersInRange = telephony.getAllCellInfo();
            responseSms.append("Towers in range: ");
            if(availableTowersInRange.size() == 0) {
                responseSms.append("none or location is off.");
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

            if(!locationManager.isLocationEnabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
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

        // wifi-enabled OR wifi-disabled
        else if(providedOption.contains(Utils.WIFI_OPTION) && (providedOption.contains(Utils.WIFI_ENABLE_SUBOPTION)
        || providedOption.contains(Utils.WIFI_DISABLE_SUBOPTION))){

            StringBuilder responseSms = new StringBuilder();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                responseSms.append("Your device does not support this option.");
                Utils.sendSms(smsManager, responseSms.toString(), sender);
                return;
            }

            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(providedOption.contains(Utils.WIFI_ENABLE_SUBOPTION));
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

        //show
        else if(providedOption.contains(Utils.SHOW_MESSAGE_OPTION)){
            String messageToDisplay = message.substring(message.indexOf("\"") + 1,
                    message.lastIndexOf("\""));

            Intent lockScreenMessage = new Intent(context, ShowMessageActivity.class);
            lockScreenMessage.putExtra(Utils.SHOW_MESSAGE_OPTION, messageToDisplay);
            lockScreenMessage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(lockScreenMessage);
            Utils.sendSms(smsManager, "Displayed on screen", sender);
        }

    }
}
