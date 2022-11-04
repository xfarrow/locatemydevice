package com.xfarrow.locatemydevice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

public class PermissionsActivity extends AppCompatActivity {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch locationSwitch;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch overlaySwitch;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch deviceAdminSwitch;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch callsSwitch;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch autoEnablingLocationSwitch;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch smsSwitch;
    private Button openPermissionScreenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        setTitle(R.string.permissions);
        setViews();
        setValues();
        setListeners();
    }

    private void setViews(){
        locationSwitch = findViewById(R.id.location_permission_switch);
        overlaySwitch = findViewById(R.id.overlay_permission_switch);
        deviceAdminSwitch = findViewById(R.id.device_administrator_permission_switch);
        callsSwitch = findViewById(R.id.calls_permission_switch);
        autoEnablingLocationSwitch = findViewById(R.id.autoEnablingLocationSwitch);
        smsSwitch = findViewById(R.id.sms_permission_switch);
        openPermissionScreenButton = findViewById(R.id.open_permission_screen_button);
    }

    @SuppressLint("ObsoleteSdkInt")
    private void setValues(){

        // sms
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
        || ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            smsSwitch.setChecked(false);
        }
        else{
            smsSwitch.setChecked(true);
        }
        smsSwitch.setClickable(false);

        // location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationSwitch.setChecked(false);
        }
        else{
            locationSwitch.setChecked(true);
        }
        locationSwitch.setClickable(false);

        // overlay
        if(!android.provider.Settings.canDrawOverlays(this)) {
            overlaySwitch.setChecked(false);
        }
        else{
            overlaySwitch.setChecked(true);
        }
        overlaySwitch.setClickable(false);


        // device administrator
        ComponentName cn = new ComponentName(this, AdminReceiver.class);
        DevicePolicyManager mgr = (DevicePolicyManager)getSystemService(DEVICE_POLICY_SERVICE);
        if(!mgr.isAdminActive(cn)){
            deviceAdminSwitch.setChecked(false);
        }
        else{
            deviceAdminSwitch.setChecked(true);
        }
        deviceAdminSwitch.setClickable(false);

        // calls
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            callsSwitch.setChecked(false);
        }
        else{
            callsSwitch.setChecked(true);
        }
        callsSwitch.setClickable(false);

        // auto-enabling location
        if (Build.VERSION.SDK_INT < 19 || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            autoEnablingLocationSwitch.setChecked(false);
        }
        else{
            autoEnablingLocationSwitch.setChecked(true);
        }
        autoEnablingLocationSwitch.setClickable(false);
    }

    private void setListeners(){
        openPermissionScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
    }

}