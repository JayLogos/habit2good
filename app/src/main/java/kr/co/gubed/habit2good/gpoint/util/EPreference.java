package kr.co.gubed.habit2good.gpoint.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class EPreference {
    private final String PREF_NAME = "com.habit2good.epref";
    private static Context context;

    public static final String GOLD_PER_E = "gold_per_e";
    public static final String BACKUP_GOLD_E = "backup_gold_e";

    public static final String OFF_TIME = "s_off_time";
    public static final String ON_TIME = "s_on_time";

    public static final String ADFRS = "adfrs";
    public static final String ADFRE = "adfre";
    public static final String ADFl = "adfl";
    public static final String ADFS = "adfs";

    public static final String NMFRS = "nmfrs";
    public static final String NMFRE = "nmfre";
    public static final String NMFl = "nmfl";
    public static final String NMFS = "nmfs";

    public static final String FINAL_TIME_POP_KEY = "final_time_pop_key";

    public static final String N_BUDGET = "n_budget";
    public static final String N_BALANCE_GPOINT = "n_balance_gpoint";  // 2018.12.20 h2g
    public static final String N_LINKED_GOLD = "n_linked_gold";
    public static final String N_PURCHASE_GOLD = "n_purchase_gold";
    public static final String N_LASTTIME_PLUS1 = "n_lasttime_plus1";  // 2019.03.19 h2g
    public static final String N_BOOSTER_TIME_PLUS1 = "n_booster_time_plus1";  // 2019.10.24 h2g

    // habit2good 2018.11.08
    public static final String N_TROPHY = "n_trophy";
    public static final String N_TOTAL_GPOINT = "n_total_gpoint";
    public static final String N_MY_GPOINT = "n_my_gpoint";
    public static final String N_PARTNER_1_POINT = "n_p1p";
    public static final String N_PARTNER_2_POINT = "n_p2p";
    public static final String N_PARTNER_3_POINT = "n_p3p";
    public static final String N_PARTNER_4_POINT = "n_p4p";
    public static final String N_PARTNER_5_POINT = "n_p5p";
    public static final String N_PARTNER_6_POINT = "n_p6p";
    public static final String N_PARTNER_1_COUNT = "n_p1c";
    public static final String N_PARTNER_2_COUNT = "n_p2c";
    public static final String N_PARTNER_3_COUNT = "n_p3c";
    public static final String N_PARTNER_4_COUNT = "n_p4c";
    public static final String N_PARTNER_5_COUNT = "n_p5c";
    public static final String N_PARTNER_6_COUNT = "n_p6c";

    public static final String LASTPOP_TIMESTAMP = "lastpop_timestamp";

    public static final String LASTOFFPOP_TIMESTAMP = "lastoffpop_timestamp";

    public static final String BONUSLAST_TIMESTAMP = "bonuslast_timestamp";

    public static final String VERSION_N = "version_n";
    public static final String VERSION_P = "version_p";
    public static final String VERSION_C = "version_c";

    public EPreference(Context context) {
        EPreference.context = context;
    }

    public void put(String key, String value) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = pref.edit();
        try {
            editor.putString(key, APICrypto.encrypt(CommonUtil.SHARED_KEY, value));
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getValue(String key, String dftValue) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        try {
            String returnValue = pref.getString(key, APICrypto.encrypt(CommonUtil.SHARED_KEY, dftValue));
            if( returnValue == null || returnValue.equals("")){
                return "";
            }
            return APICrypto.decrypt(CommonUtil.SHARED_KEY, returnValue);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void put(String key, Integer value) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = pref.edit();
        try {
            editor.putString(key, APICrypto.encrypt(CommonUtil.SHARED_KEY, value.toString()));
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Integer getValue(String key, Integer dftValue) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        try {
            String returnValue = pref.getString(key, APICrypto.encrypt(CommonUtil.SHARED_KEY, "0"));
            if( returnValue == null || returnValue.equals("")){
                return 0;
            }
            return Integer.parseInt(APICrypto.decrypt(CommonUtil.SHARED_KEY, returnValue));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void putTotalGpoint(Double gpoint){
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = pref.edit();
        try {
            editor.putString(N_TOTAL_GPOINT, APICrypto.encrypt(CommonUtil.SHARED_KEY, gpoint+""));
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getTotalGpoint(){
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        try {
            String returnValue = pref.getString(N_TOTAL_GPOINT, "");
            if( returnValue == null || returnValue.equals("")){
                return 0;
            }
            String strBudget = APICrypto.decrypt(CommonUtil.SHARED_KEY, returnValue);
            return Double.parseDouble(strBudget);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void putBalanceGpoint(double gold){
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = pref.edit();
        try {
            editor.putString(N_BALANCE_GPOINT, APICrypto.encrypt(CommonUtil.SHARED_KEY, gold+""));
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getBalanceGpoint(){
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        try {
            String returnValue = pref.getString(N_BALANCE_GPOINT, "");
            if( returnValue == null || returnValue.equals("")){
                return 0;
            }
            String strBudget = APICrypto.decrypt(CommonUtil.SHARED_KEY, returnValue);
            return Double.parseDouble(strBudget);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void putNLinkedGold(double gold){
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = pref.edit();
        try {
            editor.putString(N_LINKED_GOLD, APICrypto.encrypt(CommonUtil.SHARED_KEY, gold+""));
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getNLinkedGold(){
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        try {
            String returnValue = pref.getString(N_LINKED_GOLD, "");
            if( returnValue == null || returnValue.equals("")){
                return 0;
            }
            String strBudget = APICrypto.decrypt(CommonUtil.SHARED_KEY, returnValue);
            return Double.parseDouble(strBudget);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void putNPurchaseGold(double gold){
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = pref.edit();
        try {
            editor.putString(N_PURCHASE_GOLD, APICrypto.encrypt(CommonUtil.SHARED_KEY, gold+""));
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getNPurchaseGold(){
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        try {
            String returnValue = pref.getString(N_PURCHASE_GOLD, "");
            if( returnValue == null || returnValue.equals("")){
                return 0;
            }
            String strBudget = APICrypto.decrypt(CommonUtil.SHARED_KEY, returnValue);
            return Double.parseDouble(strBudget);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void putLastTimeForPlus1(long time){
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = pref.edit();
        try {
            editor.putString(N_LASTTIME_PLUS1, APICrypto.encrypt(CommonUtil.SHARED_KEY, time+""));
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getLastTimeForPlus1(){
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        try {
            String returnValue = pref.getString(N_LASTTIME_PLUS1, "");
            if( returnValue == null || returnValue.equals("")){
                return 0;
            }
            String strLastTIme = APICrypto.decrypt(CommonUtil.SHARED_KEY, returnValue);
            return Long.parseLong(strLastTIme);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void putBoosterTimeForPlus1(long time){
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = pref.edit();
        try {
            editor.putString(N_BOOSTER_TIME_PLUS1, APICrypto.encrypt(CommonUtil.SHARED_KEY, time+""));
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getBoosterTimeForPlus1(){
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        try {
            String returnValue = pref.getString(N_BOOSTER_TIME_PLUS1, "");
            if( returnValue == null || returnValue.equals("")){
                return 0;
            }
            String strLastTIme = APICrypto.decrypt(CommonUtil.SHARED_KEY, returnValue);
            return Long.parseLong(strLastTIme);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
