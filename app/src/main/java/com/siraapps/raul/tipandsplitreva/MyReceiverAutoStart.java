package com.siraapps.raul.tipandsplitreva;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiverAutoStart extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent wakeUpService = new Intent(context, MyService.class);
        context.startService(wakeUpService);
    }
}