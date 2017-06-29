package com.weikuo.elemenzhang.phonebookwk.controller;

import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;

import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;
import com.orhanobut.logger.Logger;
import com.weikuo.elemenzhang.phonebookwk.bean.RawContact;
import com.weikuo.elemenzhang.phonebookwk.utils.DatabaseHelper;

import org.greenrobot.eventbus.EventBus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by elemenzhang on 2017/6/28.
 */

public class ContactSyncService extends Service {

    public ContentObserver mObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            Logger.d("contact changed");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<Contact> contactList = Contacts.getQuery().find();
                        DatabaseHelper helper=DatabaseHelper.getHelper(ContactSyncService.this);
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
                        ContactChanged contactChanged=new ContactChanged();
                        contactChanged.setContactList(contactList);
                        EventBus.getDefault().post(contactChanged);

                    }
                }).start();
            }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, mObserver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class ContactChanged{
        private List<Contact> contactList;

        public List<Contact> getContactList() {
            return contactList;
        }

        public void setContactList(List<Contact> contactList) {
            this.contactList = contactList;
        }
    }
}
