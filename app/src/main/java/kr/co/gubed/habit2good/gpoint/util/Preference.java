package kr.co.gubed.habit2good.gpoint.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Preference {

    private final String PREF_NAME = "com.habit.pref";
    private final String VER_NAME = "com.habit.ver";

    private static Context context;

    public final static String AD_ID = "adId";
    public final static String ADVERTISE_ID = "advertiseId";
    public final static String DEVICE_TOKEN = "device_token";
    public final static String USER_ID = "userId";
    public final static String PHONE_NM = "phoneNm";
    public final static String PROFILE_IMAGE = "profile_image";
    public final static String CPID = "cpid";
    public final static String NICKNAME = "nickname";
    public final static String PARTNERCDOE = "partnercode";
    public final static String REDEEMCODE = "redeemcode";
    public final static String BIRTH = "birth";
    public final static String GENDER = "gender";
    public final static String LOCATION = "location";
    public final static String MARRIAGE = "marriage";
    public final static String REVIEW = "review";
    public final static String MISSION = "mission";
    public final static String PARTNERS = "partners";
    public final static String PARTNER_GPOINT = "partner_gpoint";
    public final static String CASH_POP_ALARM = "cash_pop_alarm";
    public final static String APP_GUIDE = "app_guide";
    public final static String HISTORY_GUIDE = "history_guide";
    public final static String LINKED_APPS = "linked_apps";
    public final static String INVITE_PARTNER = "invite_partner";
    public final static String INVITE = "invite";
    public final static String COIN = "coin";
    public final static String REVIEW_POP = "review_pop";
    public final static String COUNTRY_CODE = "country_code";
    public final static String NOTICE_POP_DATE = "notice_pop_date";

    public final static String VERSION_SERVER = "version_server";
    public final static String VERSION_CHK_TIMESTAMP = "version_chk_timestamp";

    public final static String EMULATE_CHK = "emulate_chk";
    public final static String TROPHY_VIEW = "trophy_view";

    public final static String NOTI_SLIDE_1_NAME = "s1_name";
    public final static String NOTI_SLIDE_2_NAME = "s2_name";
    public final static String NOTI_SLIDE_3_NAME = "s3_name";
    public final static String NOTI_SLIDE_4_NAME = "s4_name";
    public final static String NOTI_SLIDE_1_IMAGE = "s1_image";
    public final static String NOTI_SLIDE_2_IMAGE = "s2_image";
    public final static String NOTI_SLIDE_3_IMAGE = "s3_image";
    public final static String NOTI_SLIDE_4_IMAGE = "s4_image";
    public final static String NOTI_SLIDE_1_LINK = "s1_link";
    public final static String NOTI_SLIDE_2_LINK = "s2_link";
    public final static String NOTI_SLIDE_3_LINK = "s3_link";
    public final static String NOTI_SLIDE_4_LINK = "s4_link";

    private final static String VERSION = "";

    public final static String PLUS1_TIMER = "plus1_timer";
    public final static String PLUS1_AD_FLAG = "plus1_ad_flag";

    public final static String RATING_VISIBILITY = "rating_visibility";

    public Preference(Context context) {
        Preference.context = context;
    }

    public void put(String key, String value) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void put(String key, boolean value) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void put(String key, int value) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public void put(String key, float value) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public String getValue(String key, String dftValue) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        try {
            String returnValue = pref.getString(key, dftValue);
            return returnValue;
        } catch (Exception e) {
            e.printStackTrace();
            return dftValue;
        }
    }

    public int getValue(String key, int dftValue) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        try {
            int returnValue = pref.getInt(key, dftValue);
            return returnValue;
        } catch (Exception e) {
            e.printStackTrace();
            return dftValue;
        }
    }

    public float getValue(String key, float dftValue) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        try {
            return pref.getFloat(key, dftValue);
        } catch (Exception e) {
            e.printStackTrace();
            return dftValue;
        }
    }

    public boolean getValue(String key, boolean dftValue) {
            SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
            try {
                return pref.getBoolean(key, dftValue);
            } catch (Exception e) {
                return dftValue;
            }
    }

    public int getVersionCode(int dftValue) {
        SharedPreferences pref = context.getSharedPreferences(VER_NAME, Activity.MODE_PRIVATE);
        try {
            return pref.getInt(VERSION, dftValue);
        } catch (Exception e) {
            return dftValue;
        }
    }

    public void setVersionCode(int value) {
//        synchronized (syncObj) {
            SharedPreferences pref = context.getSharedPreferences(VER_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(VERSION, value);
            editor.commit();
//        }
    }

    public void pclear() {
//        synchronized (syncObj) {
            SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.commit();
//        }
    }

    public void versionUp() {
        put(CPID, "");
        put(GENDER, "");
        put(BIRTH, "");
        put(LOCATION, "");
        put(MARRIAGE, "");
        put(PARTNERCDOE, "");
        put(CASH_POP_ALARM, "");
        put(VERSION_CHK_TIMESTAMP, "");
    }

}
