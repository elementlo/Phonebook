package com.weikuo.elemenzhang.phonebookwk.utils;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.orhanobut.logger.Logger;
import com.weikuo.elemenzhang.phonebookwk.bean.RawContact;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ezvcard.VCard;
import ezvcard.property.Email;
import ezvcard.property.Organization;
import ezvcard.property.Telephone;

/**
 * Created by ma on 2016/4/1.
 */
public class ContactInfo {
    /**
     * MUST exist
     */
    private String name; // 姓名

    /**
     * 联系人电话信息
     */
    public static class PhoneInfo {
        /**
         * 联系电话类型
         */
        public int type;
        /**
         * 联系电话
         */
        public String number;
    }

    /**
     * 联系人邮箱信息
     */
    public static class EmailInfo {
        /**
         * 邮箱类型
         */
        public int type;
        /**
         * 邮箱
         */
        public String email;
    }

    private List<PhoneInfo> phoneList = new ArrayList<PhoneInfo>(); // 联系号码
    private List<EmailInfo> email = new ArrayList<EmailInfo>(); // Email

    /**
     * 构造联系人信息
     *
     * @param name 联系人姓名
     */
    public ContactInfo(String name) {
        this.name = name;
    }

    /**
     * 姓名
     */
    public String getName() {
        return name;
    }

    /**
     * 姓名
     */
    public ContactInfo setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * 联系电话信息
     */
    public List<PhoneInfo> getPhoneList() {
        return phoneList;
    }

    /**
     * 联系电话信息
     */
    public ContactInfo setPhoneList(List<PhoneInfo> phoneList) {
        this.phoneList = phoneList;
        return this;
    }

    /**
     * 邮箱信息
     */
    public List<EmailInfo> getEmail() {
        return email;
    }

    /**
     * 邮箱信息
     */
    public ContactInfo setEmail(List<EmailInfo> email) {
        this.email = email;
        return this;
    }

    @Override
    public String toString() {
        return "{name: " + name + ", number: " + phoneList + ", email: " + email + "}";
    }

    /**
     * 联系人
     * 备份/还原操作
     *
     * @author LW
     */
    public static class ContactHandler {

        private static ContactHandler instance_ = new ContactHandler();

        /**
         * 获取实例
         */
        public static ContactHandler getInstance() {
            return instance_;
        }

        /**
         * 获取联系人指定信息
         *
         * @param projection 指定要获取的列数组, 获取全部列则设置为null
         * @return
         * @throws Exception
         */
        public Cursor queryContact(Activity context, String[] projection) {
            // 获取联系人的所需信息
            Cursor cur = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, null);
            return cur;
        }


        /**
         * 向手机中录入联系人信息
         *
         * @param info 要录入的联系人信息
         */
        public boolean addContacts(Activity context, VCard info) {
            List<Telephone> phoneList = info.getTelephoneNumbers();
            List<Email> emailList = info.getEmails();
            List<ezvcard.property.Address> addresseList = info.getAddresses();
            Organization company = info.getOrganization();
            DatabaseHelper helper = DatabaseHelper.getHelper(context);
            Map<String, Object> sqlMap = new HashMap<>();
            if (info.getStructuredName().getGiven() != null) {
                sqlMap.put("name", info.getStructuredName().getGiven());
            }
            if (info.getStructuredName().getFamily() != null) {
                sqlMap.put("family", info.getStructuredName().getFamily());
            }
            if (phoneList != null && phoneList.size() > 0) {
                sqlMap.put("phone", phoneList.get(0).getText());
            }
            if (emailList != null && emailList.size() > 0) {
                sqlMap.put("email", emailList.get(0).getValue());
            }
            if (company != null && company.getValues() != null&&company.getValues().size()>0) {
                sqlMap.put("company", company.getValues().get(0));
            }
            if (addresseList != null && addresseList.size() > 0) {
                sqlMap.put("address", addresseList.get(0).getExtendedAddressFull());
            }
            if (info.getNotes() != null && info.getNotes().size() > 0) {
                sqlMap.put("note", info.getNotes().get(0).getValue());
            }

            try {
                List<RawContact> rawContacts = helper.getUserDao().queryForFieldValues(sqlMap);
                if (rawContacts.size() > 0) {
                    Logger.d("there is a same one");
                    return false;
                }
                Logger.d("not a same one");

            } catch (SQLException e) {
                e.printStackTrace();
            }

            ContentValues values = new ContentValues();
            //首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
            Uri rawContactUri = context.getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
            long rawContactId = ContentUris.parseId(rawContactUri);

            //往data表入姓名数据
            values.clear();
            values.put(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactId);
            values.put(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, info.getStructuredName().getGiven());
            if (info.getStructuredName().getFamily() != null) {
                values.put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, info.getStructuredName().getFamily());
            }
            if (info.getFormattedName() != null) {
                values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, info.getFormattedName().getValue());
            }
            context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

            /** 录入联系电话 */
            if (phoneList != null && phoneList.size() > 0) {
                for (Telephone phoneInfo : phoneList) {
                    values.clear();
                    values.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
                    values.put(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                    values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneInfo.getText());
                    if (phoneInfo.getTypes() != null && phoneInfo.getTypes().size() > 0) {
                        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, phoneInfo.getTypes().get(0).getValue());
                    }
                    context.getContentResolver().insert(
                            ContactsContract.Data.CONTENT_URI, values);
                }
            }

            /** 录入联系人邮箱信息 */
            if (emailList != null && emailList.size() > 0) {
                for (Email email : emailList) {
                    values.clear();
                    values.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
                    values.put(ContactsContract.RawContacts.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
                    // 设置录入的邮箱信息
                    values.put(ContactsContract.CommonDataKinds.Email.DATA, email.getValue());
                    if (email.getTypes() != null && email.getTypes().size() > 0) {
                        values.put(ContactsContract.CommonDataKinds.Email.TYPE, email.getTypes().get(0).getValue());
                    }
                    // 往data表入Email数据
                    context.getContentResolver().insert(
                            ContactsContract.Data.CONTENT_URI, values);
                }
            }

            if (company != null && company.getValues() != null&&company.getValues().size()>0) {
                values.clear();
                values.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.RawContacts.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.Organization.DATA, company.getValues().get(0));
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
            }

            if (addresseList != null && addresseList.size() > 0) {
                values.clear();
                values.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, addresseList.get(0).getExtendedAddressFull());
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
            }

            if (info.getNotes() != null && info.getNotes().size() > 0) {
                values.clear();
                values.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE);
                values.put(ContactsContract.CommonDataKinds.Note.DATA1, info.getNotes().get(0).getValue());
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
            }

            return true;
        }

    }
}