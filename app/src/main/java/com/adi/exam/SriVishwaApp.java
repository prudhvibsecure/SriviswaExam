package com.adi.exam;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.adi.exam.utils.TraceUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;


public class SriVishwaApp extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {

    private static boolean isInterestingActivityVisible;

    private static SriVishwaApp instance;

    public SriVishwaApp() {
        super();
        instance = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {

            instance = this;

            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();

            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).defaultDisplayImageOptions(defaultOptions).build();
            ImageLoader.getInstance().init(config);

        } catch (Exception e) {
            TraceUtils.logException(e);
        }

        try {
            new WebView(this).destroy();
        } catch (Exception e) {
            TraceUtils.logException(e);
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof SriVishwa) {
            isInterestingActivityVisible = true;
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity instanceof SriVishwa) {
            isInterestingActivityVisible = false;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public static boolean isInterestingActivityVisible() {
        return isInterestingActivityVisible;
    }

    public static SriVishwaApp get() {
        return instance;
    } //TODO:/// check how to avoid this call


}
