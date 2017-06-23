package com.weikuo.elemenzhang.phonebookwk.utils;

/**
 * Created by alvin on 16/4/13.
 */
public interface ISharedPreference {

     void putSetting(String key, String value) ;

     void putSetting(String key, int value) ;

     void putSetting(String key, long value) ;

     void putSetting(String key, boolean value) ;

     void putSetting(String key, float value);

     String getSetting(String key, String defValue) ;

     boolean getSetting(String key, boolean defValue) ;

     long getSetting(String key, long defValue) ;

     double getSetting(String key, float defValue) ;

     int getSetting(String key, int defValue);

     boolean contains(String key);




}
