package com.xfarrow.locatemydevice;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.TextView;

public class ShowMessageActivity extends AppCompatActivity {

    private TextView textToDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_message);
        showOnLockscreen();
        setViews();
        String messageToDisplay = getData();
        textToDisplay.setText(messageToDisplay);
    }

    // https://stackoverflow.com/questions/35356848/android-how-to-launch-activity-over-lock-screen
    @SuppressLint("ObsoleteSdkInt")
    private void showOnLockscreen(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
        {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        else
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    private String getData(){
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            return extras.getString(Utils.SHOW_MESSAGE_OPTION);
        }
        return null;
    }

    private void setViews(){
        textToDisplay = findViewById(R.id.text_to_display);
    }

}