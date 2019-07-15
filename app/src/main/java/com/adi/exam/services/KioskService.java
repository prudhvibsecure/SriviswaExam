package com.adi.exam.services;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.adi.exam.SplashActivity;
import com.adi.exam.utils.PrefUtils;

import java.util.concurrent.TimeUnit;

public class KioskService extends Service {

    private static final long INTERVAL = TimeUnit.SECONDS.toMillis(2);
    private static final String TAG = KioskService.class.getSimpleName();

    private Thread t = null;
    private Context ctx = null;
    private boolean running = false;

    @Override
    public void onDestroy() {
        Log.i(TAG, "Stopping service 'KioskService'");
        startService(new Intent(this, KioskService.class));
        this.running = false;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting service 'KioskService'");
        running = true;
        ctx = this;

        // start a thread that periodically checks if your app is in the foreground
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    handleKioskMode();
                    try {
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                        Log.i(TAG, "Thread interrupted: 'KioskService'");
                    }
                }while(running);
                stopSelf();
            }
        });

        t.start();
        return Service.START_NOT_STICKY;
    }

    private void handleKioskMode() {
        // is Kiosk Mode active?
        if(PrefUtils.isKioskModeActive(ctx)&& !isForeground()) {
            // is App in background?
            //if(isInBackground()) {
                restoreApp(); // restore!
           // }
        }
    }
    private boolean isForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService("activity");
        if (Build.VERSION.SDK_INT <= 20) {
            return ((RunningTaskInfo) activityManager.getRunningTasks(1).get(0)).topActivity.getPackageName().equals(getPackageName());
        }
        boolean z = false;
        for (RunningAppProcessInfo runningAppProcessInfo : activityManager.getRunningAppProcesses()) {
            if (runningAppProcessInfo.importance == 100) {
                boolean z2 = z;
                for (String equals : runningAppProcessInfo.pkgList) {
                    if (equals.equals(getPackageName())) {
                        z2 = true;
                    }
                }
                z = z2;
            }
        }
        return z;
    }

    private boolean isInBackground() {
        return !this.ctx.getApplicationContext().getPackageName().equals(((RunningTaskInfo) ((ActivityManager) this.ctx.getSystemService("activity")).getRunningTasks(1).get(0)).topActivity.getPackageName());
    }
    private void restoreApp() {
        // Restart activity
        Intent i = new Intent(ctx, SplashActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onTaskRemoved(Intent intent) {
        Intent intent2 = new Intent(getApplicationContext(), getClass());
        intent2.setPackage(getPackageName());
        ((AlarmManager) getApplicationContext().getSystemService(NotificationCompat.CATEGORY_ALARM)).set(3, SystemClock.elapsedRealtime() + 1000, PendingIntent.getService(getApplicationContext(), 1, intent2, 1073741824));
        super.onTaskRemoved(intent);
    }
}