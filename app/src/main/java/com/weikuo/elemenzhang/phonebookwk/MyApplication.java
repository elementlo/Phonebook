package com.weikuo.elemenzhang.phonebookwk;

import android.app.Application;

import com.github.tamir7.contacts.Contacts;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

/**
 * Created by elemenzhang on 2017/6/12.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Contacts.initialize(this);
        Logger.addLogAdapter(new AndroidLogAdapter());
    }
}
