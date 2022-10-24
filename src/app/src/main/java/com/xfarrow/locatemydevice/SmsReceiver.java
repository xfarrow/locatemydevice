package com.xfarrow.locatemydevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
    // https://www.vogella.com/tutorials/AndroidBroadcastReceiver/article.html
    // This method gets fired as soon as it receives an SMS.
    @Override
    public void onReceive(Context context, Intent intent) {
        /* Consider using a WorkManager: https://developer.android.com/topic/performance/vitals/anr#slow_broadcast_receivers
        *  because the maximum BroadcastReceiver lifespan is 10seconds https://developer.android.com/training/articles/perf-anr.html
        */
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                // get sms objects
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus.length == 0) {
                    return;
                }
                String format = bundle.getString("format");
                // large message might be broken into many
                SmsMessage[] messages = new SmsMessage[pdus.length];
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                    sb.append(messages[i].getMessageBody());
                }
                // getOriginatingAddress() will always be the same in messages[X] for 0<=X<messages.length
                String sender = messages[0].getOriginatingAddress();
                String message = sb.toString().trim();

                // prevent any other broadcast receivers from receiving broadcast
                abortBroadcast();

                SmsHandler smshandler = new SmsHandler();
                smshandler.handleSms(message, sender, context);

            }
        }
    }
}

/*
NOTE:
On Android 3.1 and higher, when your app is first installed on the device, it is in a
“stopped” state. This has nothing to do with onStop() of any activity. While in the
stopped state, your manifest-registered BroadcastReceivers will not receive any
broadcasts.
To get out of the stopped state, something on the device, such as another app (that
itself is not in the stopped state), must use an explicit Intent to invoke one of your
components.
The most common way this happens is for the user to tap on a launcher icon
associated with your launcher activity. Under the covers, the home screen’s launcher
will create an explicit Intent, identifying your activity, and use that with
startActivity(). This moves you out of the stopped state.
As noted above, you start off in the stopped state. Once you are moved out of the
stopped state, via the explicit Intent, you will remain out of the stopped state until
one of two things happens:
1. The user uninstalls your app
2. The user “force-stops” your app

- The busy's guide to Android Development, Mark L. Murphy
 */