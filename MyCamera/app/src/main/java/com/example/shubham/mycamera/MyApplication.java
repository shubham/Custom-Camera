package com.example.shubham.mycamera;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Application Class For Getting the Application Context Throughout Application
 * <p>
 * Created by shubham.
 */

public class MyApplication extends Application {

    private static final String TAG = MyApplication.class.getSimpleName();
    private static MyApplication sInstance;

    /**
     * Method For getting Application Context
     *
     * @return : Context
     */
    public static Context getAppContext() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() called");
        sInstance = this;
    }
}