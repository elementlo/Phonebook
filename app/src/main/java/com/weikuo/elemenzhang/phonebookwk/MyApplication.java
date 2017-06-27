package com.weikuo.elemenzhang.phonebookwk;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.github.tamir7.contacts.Contacts;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import io.fabric.sdk.android.Fabric;

/**
 * Created by elemenzhang on 2017/6/12.
 */

public class MyApplication extends Application {

    private FirebaseAnalytics mFirebaseAnalytics;
    @Override
    public void onCreate() {
        super.onCreate();
        Contacts.initialize(this);
        Logger.addLogAdapter(new AndroidLogAdapter());
        Fabric.with(this, new Crashlytics());


        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }
}
