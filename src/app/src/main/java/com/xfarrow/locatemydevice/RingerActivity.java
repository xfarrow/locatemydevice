package com.xfarrow.locatemydevice;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class RingerActivity extends AppCompatActivity {

    private Ringtone ringtoneManager;
    private Vibrator vibrator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringer);
        showOnLockscreen();
        setViews();
        startRinging();
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

    private void setViews(){
        Button stopButton = findViewById(R.id.stopButton);

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRinging();
            }
        });
    }

    private void startRinging(){
        Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtoneManager = RingtoneManager.getRingtone(this, ringtone);
        ringtoneManager.setVolume(1f);
        ringtoneManager.play();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Start without a delay
        // Vibrate for 500 milliseconds
        // Sleep for 500 milliseconds
        long[] pattern = {0, 500, 500};
        // The '0' here means to repeat indefinitely
        // '0' is actually the index at which the pattern keeps repeating from (the start)
        // To repeat the pattern from any other point, you could increase the index, e.g. '1'
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        }
        else{
            vibrator.vibrate(pattern,0);
        }
    }

    private void stopRinging(){
        ringtoneManager.stop();
        vibrator.cancel();
        finishAndRemoveTask();
    }

    @Override
    public void onBackPressed(){
        stopRinging();
    }
}