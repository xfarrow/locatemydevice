package com.xfarrow.locatemydevice;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class AppInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);

        TextView appVersionTextView = findViewById(R.id.appversionTextView);
        appVersionTextView.setText("App version: " + BuildConfig.VERSION_NAME);
    }
}