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
    // This method gets fired as soon as it receives an SMS
    @Override
    public void onReceive(Context context, Intent intent) {

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