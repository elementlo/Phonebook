package com.weikuo.elemenzhang.phonebookwk;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.weikuo.elemenzhang.phonebookwk.bean.RawContact;
import com.weikuo.elemenzhang.phonebookwk.controller.ContactSyncService;
import com.weikuo.elemenzhang.phonebookwk.utils.BaseTaskSwitch;
import com.weikuo.elemenzhang.phonebookwk.utils.DatabaseHelper;

import org.greenrobot.eventbus.EventBus;

import java.sql.SQLException;
import java.util.List;

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

        BaseTaskSwitch.init(this).setOnTaskSwitchListener(new BaseTaskSwitch.OnTaskSwitchListener() {
            @Override
            public void onTaskSwitchToForeground() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        refreshContactList();
                    }
                }).start();
            }

            @Override
            public void onTaskSwitchToBackground() {

            }
        });


    }

    private void refreshContactList() {
        Logger.d("onRefreshContacts");
        List<Contact> contactList = Contacts.getQuery().find();
        DatabaseHelper helper=DatabaseHelper.getHelper(this);
        helper.onDelete();
        try {
            if (!(helper.getUserDao().isTableExists())){
                helper.reCreateTable();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        RawContact rawContact;
        for (int i = 0; i < contactList.size(); i++) {
            rawContact=new RawContact();
            if (contactList.get(i).getGivenName()!=null){
                rawContact.setName(contactList.get(i).getGivenName());
            }else {
                rawContact.setName("");
            }
            if (contactList.get(i).getEmails()!=null&&contactList.get(i).getEmails().size()>0){
                rawContact.setEmail(contactList.get(i).getEmails().get(0).getAddress());
            }
            if (contactList.get(i).getFamilyName()!=null){
                rawContact.setFamily(contactList.get(i).getFamilyName());
            }else {
                rawContact.setFamily("");
            }
            if (contactList.get(i).getPhoneNumbers()!=null&&contactList.get(i).getPhoneNumbers().size()>0){
                rawContact.setPhone(contactList.get(i).getPhoneNumbers().get(0).getNumber());
            }else {
                rawContact.setPhone("");
            }
            if (contactList.get(i).getAddresses()!=null&&contactList.get(i).getAddresses().size()>0){
                rawContact.setAddress(contactList.get(i).getAddresses().get(0).getFormattedAddress());
            }else {
                rawContact.setAddress("");
            }
            if (contactList.get(i).getCompanyName()!=null){
                rawContact.setCompany(contactList.get(i).getCompanyName());
            }else {
                rawContact.setCompany("");
            }
            if (contactList.get(i).getNote()!=null){
                rawContact.setNote(contactList.get(i).getNote());
            }else {
                rawContact.setNote("");
            }
            try {
                helper.getUserDao().create(rawContact);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        ContactSyncService.ContactChanged contactChanged=new ContactSyncService.ContactChanged();
        contactChanged.setContactList(contactList);
        EventBus.getDefault().post(contactChanged);

    }
}
