package com.adi.exam.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import com.adi.exam.SriVishwaApp;
import com.adi.exam.utils.PrefUtils;


public class OnScreenOffReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            SriVishwaApp sriVishwaApp = (SriVishwaApp) context.getApplicationContext();
            if (PrefUtils.isKioskModeActive(sriVishwaApp)) {
                wakeUpDevice(sriVishwaApp);
            }
        }
    }

    private void wakeUpDevice(SriVishwaApp sriVishwaApp) {
        PowerManager.WakeLock wakeLock = sriVishwaApp.getWakeLock();
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
        wakeLock.acquire();
        wakeLock.release();
    }

}