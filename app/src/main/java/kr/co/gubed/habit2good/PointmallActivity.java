package kr.co.gubed.habit2good;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mapps.android.view.AdView;
import com.mz.common.listener.AdListener;
import com.tnkfactory.ad.BannerAdListener;
import com.tnkfactory.ad.BannerAdType;
import com.tnkfactory.ad.BannerAdView;
import com.tnkfactory.ad.TnkSession;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import kr.co.gubed.habit2good.gpoint.activity.GiftBoxActivity;
import kr.co.gubed.habit2good.gpoint.activity.HistoryActivity;
import kr.co.gubed.habit2good.gpoint.activity.InviteActivity;
import kr.co.gubed.habit2good.gpoint.activity.MissionActivity;
import kr.co.gubed.habit2good.gpoint.activity.SettingActivity;
import kr.co.gubed.habit2good.gpoint.activity.SignActivity;
import kr.co.gubed.habit2good.gpoint.activity.StoreActivity;
import kr.co.gubed.habit2good.gpoint.filecache.ByteProviderUtil;
import kr.co.gubed.habit2good.gpoint.filecache.FileCache;
import kr.co.gubed.habit2good.gpoint.filecache.FileCacheFactory;
import kr.co.gubed.habit2good.gpoint.listener.AsyncTaskCompleteListener;
import kr.co.gubed.habit2good.gpoint.listener.GuideListener;
import kr.co.gubed.habit2good.gpoint.model.AlarmNoti;
import kr.co.gubed.habit2good.gpoint.model.NetworkErrorModel;
import kr.co.gubed.habit2good.gpoint.util.APICrypto;
import kr.co.gubed.habit2good.gpoint.util.Applications;
import kr.co.gubed.habit2good.gpoint.util.AsyncHTTPPost;
import kr.co.gubed.habit2good.gpoint.util.CommonUtil;
import kr.co.gubed.habit2good.gpoint.util.EPreference;
import kr.co.gubed.habit2good.gpoint.util.Preference;
import kr.co.gubed.habit2good.gpoint.view.AppGuideDialog;
import kr.co.gubed.habit2good.gpoint.view.CashPopDialog;
import kr.co.gubed.habit2good.gpoint.view.LoadingDialog;
import kr.co.gubed.habit2good.gpoint.view.NetworkDialog;
import kr.co.gubed.habit2good.gpoint.view.NoticeDialog;
import kr.co.gubed.habit2good.gpoint.view.ReviewDialog;


public class PointmallActivity extends BaseActivity
        implements GuideListener, AsyncTaskCompleteListener<String>, View.OnClickListener{

    private String TAG = this.getClass().toString();

    /*ADMOB private AdView adView;*/
    /*TNK private BannerAdView bannerAdView;*/
    private AdView m_adView = null;                 // MANPLUS
    private Handler handler = new Handler();
    private EndingDialog mEndingDialog;

    private LinearLayout layer_my_gpoint;

    private TextView tv_user;
    private ImageView iv_my_gpoint;
    private TextView tv_my_gpoint;
    private TextView tv_my_dgpoint_title;
    private TextView tv_my_dgpoint;
    private TextView tv_my_coin;

    private RelativeLayout btn_get_gold;
    private RelativeLayout btn_partner;
    private RelativeLayout btn_store;
    private RelativeLayout btn_giftbox;
    private ImageView iv_gift_box_new;
    private RelativeLayout btn_setting;
    private ImageView iv_setting;

//    private RelativeLayout btn_access;

    private Applications applications;
    private Tracker tracker;

    private String analiticsCategory = "/Initialization";
    private boolean appFlag = false;
    private double gold = 0;
    private int coin = 0;
    private double linked_gold = 0;
    private double normal_gold = 0;
    private double purchase = 0;

    private boolean isGoMission = false;

    private FileCache noticePopFileCache;
    private FileCache giftboxFileCache;
    private FileCache noticeFileCache;

    private CashPopDialog cashPopDialog;
    private AppGuideDialog appGuideDialog;
    private NetworkDialog networkDialog;
    private NoticeDialog noticeDialog;
    private ReviewDialog reviewDialog;
    private LoadingDialog loadingDialog;

    private ArrayList<JSONObject> npMap;
    private HashMap<String, NetworkErrorModel> networkErrorHash;

    private boolean isVisibleTab;

    private HashMap<String, String> homeList;

    Toolbar toolbar;
    ActionBar actionbar;

    @Override
    int getContentViewId() {
        if (Applications.preference.getValue(Preference.USER_ID, "").equals("")) {
            return R.layout.intro_gtrophy_and_gpoint;
        } else {
            return R.layout.activity_pointmall;
        }
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.btn_nav_point;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Applications.preference.getValue(Preference.USER_ID, "").equals("")) {
            Intent intent = new Intent(getApplicationContext(), SignActivity.class);
            startActivity(intent);

            /*getNoticePopFileCache();
            getGiftBoxFileCache();
            getNoticeFileCache();*/

            npMap = new ArrayList<>();
            /*requestNotice();*/
            //AppGuidePopupShow();

            finish();
        } else {
            Log.i(TAG, "user_id"+Applications.preference.getValue(Preference.USER_ID, ""));
            //view = getLayoutInflater().inflate(R.layout.activity_pointmall, null);
        }
        if (Applications.preference.getValue(Preference.PHONE_NM, "").equals("")) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
            String phoneNum = telephonyManager.getLine1Number();
            Applications.preference.put(Preference.PHONE_NM, phoneNum);
        }
        TelephonyManager tm = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
        long subscriberId = 0;
        if (tm.getSubscriberId() != null && Pattern.matches("^[0-9]+$", tm.getSubscriberId())) {
            subscriberId = Long.parseLong(tm.getSubscriberId()) - 402;
        }
        if ((tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT && subscriberId == 0) || subscriberId == 0) {
            //No USIM or subscriberId is null or subscriberId is not number.
            if (cashPopDialog == null) {
                cashPopDialog = new CashPopDialog(this);
            }
            cashPopDialog.setCpTitle(this.getResources().getString(R.string.usim_title));
            cashPopDialog.setCpDesc(this.getResources().getString(R.string.usim_detail));
            cashPopDialog.setCpOkButton(this.getResources().getString(R.string.ok), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            cashPopDialog.setCpCancel(false);
            cashPopDialog.show();
        } else {
            Applications.preference.put(Preference.AD_ID, Long.toString(subscriberId));
        }

        getGiftBoxFileCache();
        /*getNoticePopFileCache();
        getGiftBoxFileCache();
        getNoticeFileCache();*/

        int version = CommonUtil.getVersionCode(this);
        applications = (Applications) getApplication();
        tracker = applications.getDefaultTracker();

        //Crashlytics.setUserIdentifier(Applications.preference.getValue(Preference.USER_ID, ""));

        init();

        getLauncher();

        /*ADMOB Applications.admobRequest();*/

        /*requestMyGift();
        requestNotice();
        AppGuidePopupShow();*/

        checkAlarm();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Applications.applicationDestroy();
        /*if (bannerAdView != null) {
            bannerAdView.onDestroy();
        }*/
        if (mEndingDialog != null) {
            mEndingDialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        if (Applications.preference.getValue(Preference.USER_ID, "").equals("")) {
            super.onResume();
            return;
        }

        try {
            Log.i(TAG, "onResume");

            /*TNK
            if (bannerAdView != null) {
                bannerAdView.onResume();
            }*/

            /* ManPlus */
            if (m_adView != null)
                m_adView.StartService();

            try {
                //HideLoadingProgress();
            } catch (Exception ignore) {
            }
            onResumeCheker();
            if (iv_gift_box_new != null) {
                if (Applications.dbHelper.chkNewGiftbox()) {
                    iv_gift_box_new.setVisibility(View.VISIBLE);
                } else {
                    iv_gift_box_new.setVisibility(View.GONE);
                }
            }
            int noticeNewCnt = Applications.dbHelper.getNoticeNewCnt("1");
            int faqNewCnt = Applications.dbHelper.getNoticeNewCnt("2");
            if (iv_setting != null) {
                if (noticeNewCnt + faqNewCnt > 0) {
                    iv_setting.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.setting1_new));
                } else {
                    iv_setting.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.setting1));
                }
            }
            applications.setRefreshActivity(this);
            versionChk();

            final double nowGold = Applications.ePreference.getBalanceGpoint();
            final int nowCoin = Applications.preference.getValue(Preference.COIN, 0);
            //setGoldCoin(nowGold, nowCoin, "");
            if (Applications.isGiftBoxRe) {
                Applications.isGiftBoxRe = false;
                requestMyGift(true);
            }
        } catch (Exception ignore) {

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        /* TNK
        if (bannerAdView != null) {
            bannerAdView.onPause();
        }*/
        if (m_adView != null)
            m_adView.StopService();
    }

    @Override
    public void onBackPressed() {
        manPlusEndingView(1413, 31780, 803889, AdView.TYPE_HTML);
        //super.onBackPressed();
    }

    public void init() {
        toolbar = findViewById(R.id.habits_toolbar);
        setSupportActionBar(toolbar);
        actionbar = getSupportActionBar();
        try {
            actionbar.setTitle(R.string.btn_nav_point);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        layer_my_gpoint = (LinearLayout) findViewById(R.id.layer_my_gpoint);
        layer_my_gpoint.setOnClickListener(this);

        tv_user = (TextView) findViewById(R.id.tv_user);
        iv_my_gpoint = (ImageView) findViewById(R.id.iv_my_gpoint);
        tv_my_gpoint = (TextView) findViewById(R.id.tv_my_gpoint);
        tv_my_gpoint.setText(CommonUtil.setComma(getGold() + "", true, false));

        tv_my_coin = (TextView) findViewById(R.id.tv_my_coin);
        tv_my_coin.setText(CommonUtil.setComma(getCoin() + "", false, false) + getResources().getString(R.string.trophy_cnt));

        btn_get_gold = (RelativeLayout) findViewById(R.id.btn_get_gold);
        btn_get_gold.setOnClickListener(this);
        btn_partner = (RelativeLayout) findViewById(R.id.btn_partner);
        btn_partner.setOnClickListener(this);
        btn_store = (RelativeLayout) findViewById(R.id.btn_store);
        btn_store.setOnClickListener(this);
        btn_giftbox = (RelativeLayout) findViewById(R.id.btn_giftbox);
        btn_giftbox.setOnClickListener(this);

        iv_gift_box_new = (ImageView) findViewById(R.id.iv_gift_box_new);

        btn_setting = (RelativeLayout) findViewById(R.id.btn_setting);
        btn_setting.setOnClickListener(this);

        iv_setting = (ImageView) findViewById(R.id.iv_setting);

        this.gold = getGold();
        this.coin = Applications.preference.getValue(Preference.COIN, 0);
        this.linked_gold = getLinkedGold();
        this.normal_gold = getNormalGold();

        npMap = new ArrayList<>();

        /*TNK
        bannerAdView = (BannerAdView) findViewById(R.id.banner_ad);
        bannerAdView.setBannerAdListener(new BannerAdListener() {
            public static final int FAIL_NO_AD = -1;  // no ad available
            public static final int FAIL_CANCELED = -4; // ad frequency settings
            public static final int FAIL_SYSTEM = -9;

            @Override
            public void onFailure(int errCode) {
                Log.e(getClass().getName(), "TNK bannerAd loading fail: "+errCode);
            }

            @Override
            public void onShow() {

            }

            @Override
            public void onClick() {

            }
        });
        bannerAdView.loadAd(TnkSession.CPC, BannerAdType.LANDSCAPE); // or bannerAdView.loadAd(TnkSession.CPC, BannerAdType.LANDSCAPE)*/
        /*adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);*/

        manPlusBannerAdcreateBannerXMLMode();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_pointmall, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_my_wallet:
                goToHistory();
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                break;
            case R.id.action_refresh:
                double balanceGpoint = Applications.ePreference.getBalanceGpoint();
                tv_my_gpoint.setText(String.format("%,.0f", balanceGpoint));

                requestMyGift(false);
                break;
            case R.id.action_help:
                CommonUtil.showSupport(this, true);
                break;
            case R.id.action_info:
                Intent intentInfo = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.pointmall_about_uri)));
                startActivity(intentInfo);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void Logout() {
        Applications.preference.put(Preference.USER_ID, "");
        Applications.preference.put(Preference.CPID, "");
        Applications.preference.put(Preference.COIN, 0);
        Applications.preference.put(Preference.GENDER, "");
        Applications.preference.put(Preference.BIRTH, "");
        Applications.preference.put(Preference.LOCATION, "");
        Applications.preference.put(Preference.MARRIAGE, "");
        Applications.preference.put(Preference.PARTNERCDOE, "");
        Applications.preference.put(Preference.CASH_POP_ALARM, false);
        Applications.preference.put(Preference.LINKED_APPS, "");
        Applications.preference.put(Preference.INVITE_PARTNER, "");
        Applications.preference.put(Preference.NOTICE_POP_DATE, "");
        Applications.preference.put(Preference.VERSION_CHK_TIMESTAMP, "");

        Applications.ePreference.putTotalGpoint((double)0);
        Applications.ePreference.putBalanceGpoint((double)0);
        Applications.ePreference.putNLinkedGold(0);
        Applications.ePreference.putNPurchaseGold(0);

        Applications.preference.pclear();

        Applications.dbHelper.initDatabase();

        Applications.isStart = false;
        Applications.isPopup = false;
        Applications.isHomeRefresh = true;
        Applications.isCashPopRefresh = true;
        Applications.isEventRefresh = true;
        Applications.isStoreRefresh = true;
        Applications.isSettingRefresh = true;
        Applications.isSettingNOticeRefresh = true;

        FileCacheFactory.initialize(this);
        if (FileCacheFactory.getInstance().has(CommonUtil.cacheNameInvite)) {
            FileCacheFactory.getInstance().get(CommonUtil.cacheNameInvite).clear();
        }
        if (FileCacheFactory.getInstance().has(CommonUtil.cacheName)) {
            FileCacheFactory.getInstance().get(CommonUtil.cacheName).clear();
        }
        if (FileCacheFactory.getInstance().has(CommonUtil.cacheNameNotice)) {
            FileCacheFactory.getInstance().get(CommonUtil.cacheNameNotice).clear();
        }
        if (FileCacheFactory.getInstance().has(CommonUtil.cacheNameHistory)) {
            FileCacheFactory.getInstance().get(CommonUtil.cacheNameHistory).clear();
        }
        if (FileCacheFactory.getInstance().has(CommonUtil.noticeCache)) {
            FileCacheFactory.getInstance().get(CommonUtil.noticeCache).clear();
        }
        if (FileCacheFactory.getInstance().has(CommonUtil.inviteCache)) {
            FileCacheFactory.getInstance().get(CommonUtil.inviteCache).clear();
        }
        if (FileCacheFactory.getInstance().has(CommonUtil.adCache)) {
            FileCacheFactory.getInstance().get(CommonUtil.adCache).clear();
        }
        if (FileCacheFactory.getInstance().has(CommonUtil.eventCache)) {
            FileCacheFactory.getInstance().get(CommonUtil.eventCache).clear();
        }
        if (FileCacheFactory.getInstance().has(CommonUtil.storeCache)) {
            FileCacheFactory.getInstance().get(CommonUtil.storeCache).clear();
        }
        if (FileCacheFactory.getInstance().has(CommonUtil.storeNewCache)) {
            FileCacheFactory.getInstance().get(CommonUtil.storeNewCache).clear();
        }
        if (FileCacheFactory.getInstance().has(CommonUtil.noticePopCache)) {
            FileCacheFactory.getInstance().get(CommonUtil.noticePopCache).clear();
        }
        Intent intent = new Intent(this, SignActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    public FileCache getNoticePopFileCache() {
        if (noticePopFileCache == null) {
            FileCacheFactory.initialize(this);
            if (!FileCacheFactory.getInstance().has(CommonUtil.noticePopCache)) {
                FileCacheFactory.getInstance().create(CommonUtil.noticePopCache, 1024 * 4);
            }
            noticePopFileCache = FileCacheFactory.getInstance().get(CommonUtil.noticePopCache);
        }
        return noticePopFileCache;
    }

    public FileCache getGiftBoxFileCache() {
        if (giftboxFileCache == null) {
            FileCacheFactory.initialize(this);
            if (!FileCacheFactory.getInstance().has(CommonUtil.giftboxCache)) {
                FileCacheFactory.getInstance().create(CommonUtil.giftboxCache, 1024 * 4);
            }
            giftboxFileCache = FileCacheFactory.getInstance().get(CommonUtil.giftboxCache);
        }
        return giftboxFileCache;
    }

    public FileCache getNoticeFileCache() {
        if (noticeFileCache == null) {
            FileCacheFactory.initialize(this);
            if (!FileCacheFactory.getInstance().has(CommonUtil.noticeCache)) {
                FileCacheFactory.getInstance().create(CommonUtil.noticeCache, 1024 * 4);
            }
            noticeFileCache = FileCacheFactory.getInstance().get(CommonUtil.noticeCache);
        }
        return noticeFileCache;
    }

    public void requestAsyncTask(String param, String action) {
        if (action.equals(CommonUtil.ACTION_POP_LINKED)) {
            new AsyncHTTPPost(this).execute(CommonUtil.SERVER_URL + "?_z=" + Math.random(), param, action);
        } else {
            if (Applications.getCountry(this).equals("KR") && !Applications.isRoaming(this)) {
                new AsyncHTTPPost(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, CommonUtil.SERVER_URL, param, action);
            } else {
                new AsyncHTTPPost(this).execute(CommonUtil.SERVER_URL, param, action);
            }
        }
    }

    public void AppGuidePopupShow() {
        if (!Applications.preference.getValue(Preference.APP_GUIDE, false)) {
            if (appGuideDialog == null) {
                appGuideDialog = new AppGuideDialog();
            }
            appGuideDialog.setCancelable(false);
            appGuideDialog.setGuideListener(this);
            appGuideDialog.show(getSupportFragmentManager(), "guide");
        } else {
            InvitePopupShow();
        }
    }

    public void InvitePopupShow() {
        //if (!Applications.preference.getValue(Preference.INVITE_PARTNER, false) && Applications.preference.getValue(Preference.INVITE, "1").equals("1")) {
        if (!Applications.preference.getValue(Preference.USER_ID, "").equals("") && !Applications.preference.getValue(Preference.INVITE_PARTNER, false) && Applications.preference.getValue(Preference.INVITE, "1").equals("1")) {
            cashPopDialog = new CashPopDialog(this);
            cashPopDialog.setCpTitle(getResources().getString(R.string.congratulation));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cashPopDialog.setCpDesc(Html.fromHtml(getResources().getString(R.string.invite_partners_desc), Html.FROM_HTML_MODE_LEGACY));
            } else {
                cashPopDialog.setCpDesc(Html.fromHtml(getResources().getString(R.string.invite_partners_desc)));
            }
            cashPopDialog.setCpBOkButton(getResources().getString(R.string.invite_partners), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tracker.send(new HitBuilders.EventBuilder().setCategory(analiticsCategory).setAction("/conguratulation_pop_up/invite_partners_click").build());
                    goToInvitePartner();
                    Applications.preference.put(Preference.INVITE_PARTNER, true);
                    cashPopDialog.dismiss();
                }
            });
            cashPopDialog.setCpBCancelButton(getResources().getString(R.string.later), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tracker.send(new HitBuilders.EventBuilder().setCategory(analiticsCategory).setAction("/conguratulation_pop_up/later_click").build());
                    Applications.preference.put(Preference.INVITE_PARTNER, true);
                    cashPopDialog.dismiss();
                }
            });
            cashPopDialog.setCpCancel(false);
            cashPopDialog.show();
        } else {
            noticeRequest();
        }
    }

    public void noticeRequest() {
        boolean isCache = false;
        String cacheRst = "";
        if (noticePopFileCache.get(CommonUtil.noticePopCache) != null) {
            try {
                InputStream is = noticePopFileCache.get(CommonUtil.noticePopCache).getInputStream();
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                cacheRst = new String(buffer);
                Log.e(TAG, cacheRst);
                JSONObject job = new JSONObject(cacheRst);
                isCache = System.currentTimeMillis() - Long.parseLong(job.getString(CommonUtil.KEY_TIMESTAMP)) < 60 * 60 * 1000 * 12;
            } catch (Exception e) {
                isCache = false;
                e.printStackTrace();
            }
        }
        if (isCache) {
            try {
                JSONObject job = new JSONObject(cacheRst);
                JSONObject jo = new JSONObject(job.getString(CommonUtil.KEY_RST));
                JSONArray npArr = new JSONArray(jo.getString("n"));
                npMap.clear();
                for (int i = 0; i < npArr.length(); i++) {
                    npMap.add(npArr.getJSONObject(i));
                }

                showNoticePop();

                JSONArray noticeArr = new JSONArray(jo.getString("l"));
                if (Applications.noticeMap == null) {
                    Applications.noticeMap = new HashMap();
                }
                Applications.noticeMap.clear();
                for (int i = 0; i < noticeArr.length(); i++) {
                    JSONObject noticeObj = noticeArr.getJSONObject(i);
                    Applications.noticeMap.put(noticeObj.getString("id"), noticeObj);
                }
            } catch (Exception ignore) {
            }
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put(CommonUtil.KEY_USERID, Applications.preference.getValue(Preference.USER_ID, ""));
            map.put(CommonUtil.KEY_ACTION, CommonUtil.ACTION_NOTICE_POPUP);
            String param = APICrypto.getParam(this, map, CommonUtil.SHARED_KEY);
            requestAsyncTask(param, CommonUtil.ACTION_NOTICE_POPUP);
        }
    }

    public void showNoticePop() {
        if (!npMap.isEmpty()) {
            try {
                final JSONObject np = npMap.get(0);
                final String id = np.getString("id");
                long currentTime = System.currentTimeMillis();
                long lTime;
                String lastTime;
                lastTime = Applications.dbHelper.getNoticePop(id);
                Log.e(" id :" + id, " lastTime : " + lastTime);
                if (lastTime.equals("")) {
                    lTime = currentTime - 86400000;
                } else {
                    lTime = Long.parseLong(lastTime);
                }
                if (CommonUtil.getDateTime(currentTime) > CommonUtil.getDateTime(lTime)) {
                    String notice_title = np.getString("subject");
                    String notice_content = np.getString("content");
                    String companyStr = np.getString("companys");
                    final String isToday = np.getString("isToday");
                    boolean isPop = false;
                    if (companyStr.equals("")) {
                        isPop = true;
                    } else {
                        String[] companys = companyStr.split(",");
                        for (int i = 0; i < companys.length; i++) {
                            String company = companys[i].toLowerCase().trim();
                            String manufacturer = Build.MANUFACTURER.toLowerCase().trim();
                            Log.e("company", "" + company);
                            Log.e("manufacturer", "" + manufacturer);
                            if (manufacturer.contains(company)) {
                                isPop = true;
                                break;
                            }
                        }
                    }
                    if (isPop) {
                        noticeDialog = new NoticeDialog(this);
                        noticeDialog.setNpTitle(notice_title);
                        noticeDialog.setNpHtml(notice_content);
                        noticeDialog.setCancelable(true);
                        noticeDialog.setTodayTxt(isToday);
                        DisplayMetrics dm = this.getResources().getDisplayMetrics();
                        int width = dm.widthPixels;
                        int height = dm.heightPixels;
                        noticeDialog.setSize(width, height);
                        noticeDialog.show();
                        noticeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                Log.e("onDismiss", "onDismiss : " + noticeDialog.getBtn_check().isSelected());
                                if (noticeDialog.getBtn_check().isSelected()) {
                                    if (isToday.equals("Y")) {
                                        Applications.dbHelper.setNoticePop(id, (System.currentTimeMillis() + (86400000 * 365)) + "");
                                    } else {
                                        Applications.dbHelper.setNoticePop(id, System.currentTimeMillis() + "");
                                    }
                                }
                                try {
                                    npMap.remove(0);
                                } catch (Exception ignore) {
                                }
                                showNoticePop();
                            }
                        });
                    } else {
                        try {
                            npMap.remove(0);
                        } catch (Exception ignore) {
                        }
                        showNoticePop();
                    }
                } else {
                    try {
                        npMap.remove(0);
                    } catch (Exception ignore) {
                    }
                    showNoticePop();
                }
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }
    }

    public void requestMyGift(boolean isCondition) {
        boolean isCache = false;
        String cacheRst;
        if (giftboxFileCache.get(CommonUtil.giftboxCache) != null) {
            try {
                InputStream is = giftboxFileCache.get(CommonUtil.giftboxCache).getInputStream();
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                cacheRst = new String(buffer);
                Log.e(TAG, cacheRst);
                JSONObject job = new JSONObject(cacheRst);
                if (isCondition) {
                    isCache = System.currentTimeMillis() - Long.parseLong(job.getString(CommonUtil.KEY_TIMESTAMP)) < 60 * 60 * 1000 * 2;
                }
            } catch (Exception e) {
                isCache = false;
                e.printStackTrace();
            }

        }
        if (!isCache || Applications.isGiftBoxGo) {
            HashMap<String, String> map = new HashMap<>();
            map.put(CommonUtil.KEY_USERID, Applications.preference.getValue(Preference.USER_ID, ""));
            map.put(CommonUtil.KEY_GIFT_MAX_ID, Applications.dbHelper.getGiftBoxLastId() + "");
//        map.put(CommonUtil.KEY_GIFT_MAX_ID, "0");
            map.put(CommonUtil.KEY_ACTION, CommonUtil.ACTION_GET_MYGIFT);
            String param = APICrypto.getParam(this, map, CommonUtil.SHARED_KEY);
            requestAsyncTask(param, CommonUtil.ACTION_GET_MYGIFT);
        }
    }

    private void requestNotice() {
        boolean isCache = false;
        String cacheRst;
        if (noticeFileCache.get(CommonUtil.noticeCache) != null) {
            try {
                InputStream is = noticeFileCache.get(CommonUtil.noticeCache).getInputStream();
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                cacheRst = new String(buffer);
                Log.e(TAG, cacheRst);
                JSONObject job = new JSONObject(cacheRst);
                isCache = System.currentTimeMillis() - Long.parseLong(job.getString(CommonUtil.KEY_TIMESTAMP)) < 60 * 60 * 1000;
            } catch (Exception e) {
                isCache = false;
                e.printStackTrace();
            }

        }
        if (!isCache) {
            //ShowLoadingProgress();
            HashMap<String, String> map = new HashMap<>();
            map.put(CommonUtil.KEY_USERID, Applications.preference.getValue(Preference.USER_ID, ""));
            map.put(CommonUtil.KEY_ACTION, CommonUtil.ACTION_GET_NOTICE);
            String param = APICrypto.getParam(this, map, CommonUtil.SHARED_KEY);
            requestAsyncTask(param, CommonUtil.ACTION_GET_NOTICE);
        }
    }

    public void setGoldCoin(final double gold, final int coin, final String type) {
        Applications.isCashpopPopup = false;
        final double nowGold = getGold();
        final int nowCoin = Applications.preference.getValue(Preference.COIN, 0);
        if (nowGold != gold || nowCoin != coin) {
            if (FileCacheFactory.getInstance().has(CommonUtil.cacheNameHistory)) {
                FileCacheFactory.getInstance().get(CommonUtil.cacheNameHistory).clear();
            }
        }
        double reward = gold - this.gold;
        int reward_coin = coin - this.coin;
        this.gold = gold;
        this.coin = coin;
        if (reward != 0 || reward_coin != 0) {
            try {
                if (FileCacheFactory.getInstance().has(CommonUtil.cacheNameHistory)) {
                    FileCacheFactory.getInstance().get(CommonUtil.cacheNameHistory).clear();
                }
            } catch (Exception ignore) {
            }
        }
        if (type.equals("") || (reward == 0 && reward_coin == 0) || nowGold == 0) {
            try {
//                goldRefresh(nowGold, gold);
//                coinRefresh(nowCoin, coin);
                goldCoinRefresh(nowGold, gold, nowCoin, coin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (type.equals("reward")) {
            if (reward > 0 || reward_coin > 0) {
                try {
                    //HideLoadingProgress();
                } catch (Exception ignore) {
                }
                if (Applications.isReward) {
                    Applications.isReward = true;
                    tracker.send(new HitBuilders.EventBuilder().setCategory(analiticsCategory).setAction("/show_pointapp_gold_pop").build());
                } else {
                    Applications.isReward = false;
                    tracker.send(new HitBuilders.EventBuilder().setCategory(analiticsCategory).setAction("/show_normal_gold_pop").build());
                }
                try {
//                    goldRefresh(nowGold, gold);
//                    coinRefresh(nowCoin, coin);
                    goldCoinRefresh(nowGold, gold, nowCoin, coin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
//                    goldRefresh(nowGold, gold);
//                    coinRefresh(nowCoin, coin);
                    goldCoinRefresh(nowGold, gold, nowCoin, coin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
//                goldRefresh(nowGold, gold);
//                coinRefresh(nowCoin, coin);
                goldCoinRefresh(nowGold, gold, nowCoin, coin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Applications.ePreference.putBalanceGpoint(gold);
        Applications.preference.put(Preference.COIN, coin);
    }

    public double getGold() {
        return Applications.ePreference.getBalanceGpoint();
    }

    public int getCoin() {
        return Applications.preference.getValue(Preference.COIN, coin);
    }

    public void setLinkedGold(double linkedGold) {
        Applications.ePreference.putNLinkedGold(linkedGold);
    }

    public double getLinkedGold() {
        return Applications.ePreference.getNLinkedGold();
    }

    public void setNormalGold(double normalGold) {
        Applications.ePreference.putBalanceGpoint(normalGold);
    }

    public double getNormalGold() {
        return Applications.ePreference.getBalanceGpoint();
    }

    public void setPurchase(double purchase) {
        this.purchase = purchase;
        Applications.ePreference.putNPurchaseGold(purchase);
    }

    public double getPurchase() {
        return Applications.ePreference.getNPurchaseGold();
    }

    public void ShowLoadingProgress() {
        //show loading
        /*
        try {
            if (loadingDialog == null) {
                loadingDialog = new LoadingDialog(getContext());
            }
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        loadingDialog.show();
                    } catch (Exception ignore) {
                        ignore.printStackTrace();
                    }
                }
            });
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        */
    }

    public void HideLoadingProgress() throws Exception {
        //hide loading
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
            }
        });
    }

    public synchronized void requestInfo() {
        //ShowLoadingProgress();
        Applications.isHomeRefresh = false;
        HashMap<String, String> map = new HashMap<>();
        map.put(CommonUtil.KEY_USERID, Applications.preference.getValue(Preference.USER_ID, ""));
        map.put(CommonUtil.KEY_ADID, Applications.preference.getValue(Preference.AD_ID, ""));
        map.put(CommonUtil.KEY_DEVICE_TOKEN, Applications.preference.getValue(Preference.DEVICE_TOKEN, ""));
        map.put(CommonUtil.KEY_PHONE_NM, Applications.preference.getValue(Preference.PHONE_NM, ""));
        int version = CommonUtil.getVersionCode(this);
        map.put(CommonUtil.KEY_NAME, version + "");
        map.put(CommonUtil.KEY_ACTION, CommonUtil.ACTION_GET_INFO);
        String param = APICrypto.getParam(this, map, CommonUtil.SHARED_KEY);
        requestAsyncTask(param, CommonUtil.ACTION_GET_INFO);
    }

    @Override
    public void dialogDismiss() {
        appGuideDialog.dismiss();
        tracker.send(new HitBuilders.EventBuilder().setCategory(analiticsCategory).setAction("/Alloc app guide_pop up/skip click").build());
        Applications.preference.put(Preference.APP_GUIDE, true);
        InvitePopupShow();
    }

    @Override
    public void onTaskComplete(String result) {
        String rst;
        try {
            rst = APICrypto.decrypt(CommonUtil.SHARED_KEY, result);
        } catch (Exception e) {
            rst = result;
        }
        try {
            JSONObject jo = new JSONObject(rst);
            String error = jo.getString(CommonUtil.RESULT_ERROR);
            String action = jo.getString(CommonUtil.RESULT_ACTION);
            if (error != null && error.isEmpty() && action != null && !action.isEmpty()) {
                if (action.equals(CommonUtil.ACTION_GET_INFO)) {
                    String gold = jo.getString(CommonUtil.RESULT_BUDGET);
                    String balance = jo.getString(CommonUtil.RESULT_BALANCE_GPOINT);
                    String coin = jo.getString(CommonUtil.RESULT_COIN);
                    final double nowGold = Applications.ePreference.getBalanceGpoint();
                    final int nowCoin = Applications.preference.getValue(Preference.COIN, 0);
                    if (nowGold < Double.parseDouble(balance) || nowCoin < Integer.parseInt(coin)) {
                        setGoldCoin(Double.parseDouble(balance), Integer.parseInt(coin), "reward");
                    } else {
                        setGoldCoin(Double.parseDouble(balance), Integer.parseInt(coin), "");
                    }
                    String linked_gold = jo.getString(CommonUtil.RESULT_LINKED_GOLD);
                    setLinkedGold(Double.parseDouble(linked_gold));
                    String normal_gold = jo.getString(CommonUtil.RESULT_BALANCE_GPOINT);
                    setNormalGold(Double.parseDouble(normal_gold));
                    String plus1Timer = jo.getString(CommonUtil.RESULT_PLUS1_TIMER);
                    Log.i(getClass().getName(), "jo.getString(CommonUtil.RESULT_PLUS1_TIMER)="+plus1Timer);

                    tv_my_gpoint.setText(CommonUtil.setComma(getGold() + "", true, false));
/* h2g 2018.11.16 Friend에서 Partner 6 Level 개념으로 변경되면서 필요없어짐.
                    String invited_partners = jo.getString(CommonUtil.RESULT_INVITED_PARTNERS);

                    if (!invited_partners.equals(Applications.preference.getValue(Preference.PARTNERS, "0"))) {
                        FileCacheFactory.initialize(getContext());
                        if (FileCacheFactory.getInstance().has(CommonUtil.cacheNameInvite)) {
                            FileCacheFactory.getInstance().get(CommonUtil.cacheNameInvite).clear();
                        }
                    }
                    String partner_gpoint = jo.getString(CommonUtil.RESULT_PARTNER_GPOINT);

                    Applications.preference.put(Preference.REDEEMCODE, Long.toString(Long.parseLong(Applications.preference.getValue(Preference.USER_ID, "")), 36));
*/
                    //Applications.preference.put(Preference.PARTNERS, invited_partners);
                    //Applications.preference.put(Preference.PARTNER_GPOINT, partner_gpoint);

                    Applications.preference.put(Preference.PLUS1_TIMER, plus1Timer);

                    Applications.preference.put(Preference.CPID, jo.getString("id"));

                    tv_user.setText(getResources().getString(R.string.username, jo.getString("id")));

                    Applications.preference.put(Preference.BIRTH, jo.getString(CommonUtil.RESULT_YEAR));
                    Applications.preference.put(Preference.LOCATION, jo.getString(CommonUtil.RESULT_LOCATION));
                    Applications.preference.put(Preference.GENDER, jo.getString(CommonUtil.RESULT_GENDER));
                    Applications.preference.put(Preference.MARRIAGE, jo.getString(CommonUtil.RESULT_MARRIAGE));
                    Applications.preference.put(Preference.PARTNERCDOE, jo.getString(CommonUtil.RESULT_PARTNERCODE));
                    Applications.preference.put(Preference.REVIEW, jo.getString(CommonUtil.RESULT_REVIEW));
                    Applications.preference.put(Preference.MISSION, jo.getString(CommonUtil.RESULT_MISSION));

                    if (jo.getString(CommonUtil.RESULT_ALARM).equals("1") || jo.getString(CommonUtil.RESULT_ALARM).equals("")) {
                        Applications.preference.put(Preference.CASH_POP_ALARM, true);
                        FirebaseMessaging.getInstance().subscribeToTopic(Applications.getTopicId(this));
                    } else {
                        Applications.preference.put(Preference.CASH_POP_ALARM, false);
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(Applications.getTopicId(this));
                    }

                } else if (action.equals(CommonUtil.ACTION_NOTICE_POPUP)) {
                    JSONArray npArr = new JSONArray(jo.getString("n"));
                    npMap.clear();
                    for (int i = 0; i < npArr.length(); i++) {
                        npMap.add(npArr.getJSONObject(i));
                    }

                    showNoticePop();

                    JSONArray noticeArr = new JSONArray(jo.getString("l"));
                    if (Applications.noticeMap == null) {
                        Applications.noticeMap = new HashMap();
                    }
                    Applications.noticeMap.clear();
                    for (int i = 0; i < noticeArr.length(); i++) {
                        JSONObject noticeObj = noticeArr.getJSONObject(i);
                        Applications.noticeMap.put(noticeObj.getString("id"), noticeObj);
                    }

                    JSONObject cacheRst = new JSONObject();
                    cacheRst.put(CommonUtil.KEY_TIMESTAMP, System.currentTimeMillis());
                    cacheRst.put(CommonUtil.KEY_RST, rst);
                    noticePopFileCache.put(CommonUtil.noticePopCache, ByteProviderUtil.create(cacheRst.toString()));
                } else if (action.equals(CommonUtil.ACTION_GET_MYGIFT)) {
                    JSONArray gifts = new JSONArray(jo.getString("gifts"));
                    if (gifts != null && gifts.length() > 0) {
                        Applications.dbHelper.insertGiftBox(gifts);
                    }

                    JSONObject cacheGiftRst = new JSONObject();
                    cacheGiftRst.put(CommonUtil.KEY_TIMESTAMP, System.currentTimeMillis());
                    cacheGiftRst.put(CommonUtil.KEY_RST, rst);
                    giftboxFileCache.put(CommonUtil.giftboxCache, ByteProviderUtil.create(cacheGiftRst.toString()));

                    if (Applications.dbHelper.chkNewGiftbox()) {
                        iv_gift_box_new.setVisibility(View.VISIBLE);
                    } else {
                        iv_gift_box_new.setVisibility(View.GONE);
                    }

                    if (Applications.isGiftBoxGo) {
                        Applications.isGiftBoxGo = false;
                        goToGiftBox();
                    }
                } else if (action.equals(CommonUtil.ACTION_GET_NOTICE)) {
                    JSONArray jArr = new JSONArray(jo.getString("n"));
                    Applications.dbHelper.insertNewNotice(jArr);
                    int noticeNewCnt = Applications.dbHelper.getNoticeNewCnt("1");
                    int faqNewCnt = Applications.dbHelper.getNoticeNewCnt("2");
                    if (noticeNewCnt + faqNewCnt > 0) {
                        iv_setting.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.setting1_new));
                    } else {
                        iv_setting.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.setting1));
                    }
                    JSONObject cacheNewRst = new JSONObject();
                    cacheNewRst.put(CommonUtil.KEY_TIMESTAMP, System.currentTimeMillis());
                    cacheNewRst.put(CommonUtil.KEY_RST, rst);
                    noticeFileCache.put(CommonUtil.noticeCache, ByteProviderUtil.create(cacheNewRst.toString()));
                } else if (action.equals(CommonUtil.ACTION_VERSION)) {
                    final String v_n = jo.getString("v_n");
                    final String v_p = jo.getString("v_p");
                    final int v_c = Integer.parseInt(jo.getString("v_c"));
                    Applications.ePreference.put(EPreference.VERSION_N, v_n + "");
                    Applications.ePreference.put(EPreference.VERSION_P, v_p + "");
                    Applications.ePreference.put(EPreference.VERSION_C, v_c + "");
                    int version = CommonUtil.getVersionCode(this);
                    if (version < v_c) {
                        cashPopDialog = new CashPopDialog(this);
                        cashPopDialog.setCpTitle(getResources().getString(R.string.habit2good_update));
                        cashPopDialog.setCpDesc(getResources().getString(R.string.habit2good_update_desc));
                        cashPopDialog.setCpBOkButton(getResources().getString(R.string.update_now), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                if (v_p != null && !v_p.equals("")) {
                                    intent.setData(Uri.parse("market://details?id=" + v_p));
                                } else {
                                    intent.setData(Uri.parse("market://details?id=" + getPackageName()));
                                }
                                startActivity(intent);
                            }
                        });
                        cashPopDialog.setCpBCancelButton(getResources().getString(R.string.exit), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                System.exit(0);
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            }
                        });
                        cashPopDialog.setCpCancel(false);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cashPopDialog.show();
                            }
                        });
                    }
                }
            } else {
                if (error != null && error.equals(CommonUtil.ERROR_OTHER_ADID)) {
                    cashPopDialog = new CashPopDialog(this);
                    cashPopDialog.setCpTitle(getResources().getString(R.string.logout));
                    cashPopDialog.setCpDesc(getResources().getString(R.string.auto_logout));
                    cashPopDialog.setCpCancel(false);
                    cashPopDialog.setCpOkButton(getResources().getString(R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Logout();
                        }
                    });
                    cashPopDialog.show();
                } else if (error != null && error.equals(CommonUtil.ERROR_NO_USER)) {
                    cashPopDialog = new CashPopDialog(this);
                    cashPopDialog.setCpTitle(getResources().getString(R.string.logout));
                    cashPopDialog.setCpDesc(getResources().getString(R.string.auto_logout_no_user));
                    cashPopDialog.setCpCancel(false);
                    cashPopDialog.setCpOkButton(getResources().getString(R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Logout();
                        }
                    });
                    cashPopDialog.show();
                } else if (error != null && error.equals(CommonUtil.ERROR_VERSION)) {
                    cashPopDialog = new CashPopDialog(this);
                    cashPopDialog.setCpTitle(getResources().getString(R.string.habit2good_update));
                    cashPopDialog.setCpDesc(getResources().getString(R.string.habit2good_update_desc));
                    cashPopDialog.setCpBOkButton(getResources().getString(R.string.update_now), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("market://details?id=" +getPackageName()));
                            startActivity(intent);
                        }
                    });
                    cashPopDialog.setCpBCancelButton(getResources().getString(R.string.exit), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            cashPopDialog.dismiss();
                            finish();
                        }
                    });
                    cashPopDialog.setCpCancel(false);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cashPopDialog.show();
                        }
                    });
                    Applications.preference.put(Preference.VERSION_CHK_TIMESTAMP, "");
                }
            }
        } catch (Exception e) {
            try {
                //HideLoadingProgress();
            } catch (Exception ignore) {
            }
            e.printStackTrace();
        } finally {
            try {
                //HideLoadingProgress();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void onTaskError(String param, String action, String result) {
        try {
            Log.e(TAG, action);
            if (action.equals(CommonUtil.ACTION_GET_INFO)) {
                //showErrorNetwork(param, action, "home");
            }
        } catch (Exception ignore) {
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult");
        if (requestCode == CommonUtil.ACTIVITY_RESULT_INVITE_PARTNER) {
        } else if (requestCode == CommonUtil.ACTIVITY_RESULT_HISTORY) {

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layer_my_gpoint:
                goToHistory();
                break;
            case R.id.btn_get_gold:
                goToMission();
                break;
            case R.id.btn_partner:
                goToInvitePartner();
                break;
            case R.id.btn_store:
                goToStore();
                break;
            case R.id.btn_giftbox:
                goToGiftBox();
                break;
            case R.id.btn_setting:
                goToSetting();
                break;
//            case R.id.btn_access:
//                goToAccess();
//                break;
        }
    }

    public void goldCoinRefresh(final double nowGold, final double refreshGold, final int nowCoin, final int refreshCoin) throws Exception {
        boolean isGold = false;
        boolean isCoin = false;
        if (refreshGold - nowGold != 0) {
            isGold = true;
        }
        if (refreshCoin - nowCoin != 0) {
            isCoin = true;
        }

        if (isGold && isCoin) {
            final int dtime = 40;
/*            new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case 0:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cash3_1));
                            this.sendEmptyMessageDelayed(msg.what + 1, dtime);
                            break;
                        case 1:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cash3_2));
                            this.sendEmptyMessageDelayed(msg.what + 1, dtime);
                            break;
                        case 2:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cash3_1));
                            this.sendEmptyMessageDelayed(msg.what + 1, dtime);
                            break;
                        case 3:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cash3));
                            this.sendEmptyMessageDelayed(msg.what + 1, dtime);
                            break;
                        case 4:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cash3_1));
                            this.sendEmptyMessageDelayed(msg.what + 1, dtime);
                            break;
                        case 5:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cash3_2));
                            this.sendEmptyMessageDelayed(msg.what + 1, dtime);
                            break;
                        case 6:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cash3_1));
                            this.sendEmptyMessageDelayed(msg.what + 1, dtime);
                            break;
                        case 7:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cash3));
                            this.sendEmptyMessageDelayed(msg.what + 1, dtime);
                            break;
                        case 8:
                            ValueAnimator vaG = ValueAnimator.ofInt((int) nowGold, (int) refreshGold);
                            vaG.setDuration(1000);
                            vaG.setInterpolator(AnimationUtils.loadInterpolator(getContext(), android.R.anim.decelerate_interpolator));
                            vaG.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    try {
                                        Integer value = (Integer) valueAnimator.getAnimatedValue();
                                        tv_my_gpoint.setText(CommonUtil.setComma(value + "", false, false));
                                    } catch (Exception ignore) {
                                    }
                                }
                            });
                            vaG.start();
                            ValueAnimator vaC = ValueAnimator.ofInt(nowCoin, refreshCoin);
                            vaC.setDuration(1000);
                            vaC.setInterpolator(AnimationUtils.loadInterpolator(getContext(), android.R.anim.decelerate_interpolator));
                            vaC.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    try {
                                        Integer value = (Integer) valueAnimator.getAnimatedValue();
                                        tv_my_coin.setText(CommonUtil.setComma(value + "", false, false));
                                    } catch (Exception ignore) {
                                    }
                                }
                            });
                            vaC.start();
                            this.sendEmptyMessageDelayed(msg.what + 1, 1000);
                            break;
                        case 9:
                            if (!Applications.isReward) {
                                String tmsg = "";
                                if ((refreshGold - nowGold) > 0 && (refreshCoin - nowCoin) > 0) {
                                    tmsg = getResources().getString(R.string.reward_toast, "" + (int) (refreshGold - nowGold), "" + (int) (refreshCoin - nowCoin));
                                } else if ((refreshGold - nowGold) > 0 && (refreshCoin - nowCoin) <= 0) {
                                    tmsg = getResources().getString(R.string.reward_gpoint_toast, "" + (int) (refreshGold - nowGold));
                                } else if ((refreshGold - nowGold) <= 0 && (refreshCoin - nowCoin) > 0) {
                                    tmsg = getResources().getString(R.string.reward_trophy_toast, "" + (int) (refreshCoin - nowCoin));
                                }
                                if (!tmsg.equals("")) {
                                    Toast toast = Toast.makeText(getContext(), tmsg, Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.BOTTOM, 0, CommonUtil.TOAST_YOFFSET);
                                    toast.show();
                                }
                            }
                            Applications.isReward = false;
                            break;
                    }
                }
            }.sendEmptyMessageDelayed(0, 200);*/
        } else {
            try {
                goldRefresh(nowGold, refreshGold);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                coinRefresh(nowCoin, refreshCoin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void goldRefresh(final double nowGold, final double refreshGold) throws Exception {
        if (refreshGold - nowGold > 0) {
            final int dtime = 40;
/*            new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case 0:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cash3_1));
                            this.sendEmptyMessageDelayed(msg.what + 1, dtime);
                            break;
                        case 1:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cash3_2));
                            this.sendEmptyMessageDelayed(msg.what + 1, dtime);
                            break;
                        case 2:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cash3_1));
                            this.sendEmptyMessageDelayed(msg.what + 1, dtime);
                            break;
                        case 3:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cash3));
                            this.sendEmptyMessageDelayed(msg.what + 1, dtime);
                            break;
                        case 4:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cash3_1));
                            this.sendEmptyMessageDelayed(msg.what + 1, dtime);
                            break;
                        case 5:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cash3_2));
                            this.sendEmptyMessageDelayed(msg.what + 1, dtime);
                            break;
                        case 6:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cash3_1));
                            this.sendEmptyMessageDelayed(msg.what + 1, dtime);
                            break;
                        case 7:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cash3));
                            this.sendEmptyMessageDelayed(msg.what + 1, dtime);
                            break;
                        case 8:
                            ValueAnimator va = ValueAnimator.ofInt((int) nowGold, (int) refreshGold);
                            va.setDuration(1000);
                            va.setInterpolator(AnimationUtils.loadInterpolator(getContext(), android.R.anim.decelerate_interpolator));
                            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    try {
                                        Integer value = (Integer) valueAnimator.getAnimatedValue();
                                        tv_my_gpoint.setText(CommonUtil.setComma(value + "", false, false));
                                    } catch (Exception ignore) {
                                    }
                                }
                            });
                            va.start();
                            va.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    if (!Applications.isReward) {
                                        Toast toast = Toast.makeText(getContext(), getResources().getString(R.string.reward_gpoint_toast, "" + (int) (refreshGold - nowGold)), Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.BOTTOM, 0, CommonUtil.TOAST_YOFFSET);
                                        toast.show();
                                    }
                                    Applications.isReward = false;
                                }

                                @Override
                                public void onAnimationCancel(Animator animator) {
                                }

                                @Override
                                public void onAnimationRepeat(Animator animator) {
                                }
                            });
                            break;
                    }
                }
            }.sendEmptyMessageDelayed(0, 200);*/
        } else {
            ValueAnimator va = ValueAnimator.ofInt((int) nowGold, (int) refreshGold);
            va.setDuration(1000);
            va.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.anim.decelerate_interpolator));
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    try {
                        Integer value = (Integer) valueAnimator.getAnimatedValue();
                        tv_my_gpoint.setText(CommonUtil.setComma(value + "", false, false));
                    } catch (Exception ignore) {
                    }
                }
            });
            va.start();
        }
    }

    public void coinRefresh(final int nowCoin, final int refreshCoin) throws Exception {

        if (refreshCoin - nowCoin > 0) {
            ValueAnimator va = ValueAnimator.ofInt(nowCoin, refreshCoin);
            va.setDuration(1000);
            va.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.anim.decelerate_interpolator));
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    try {
                        Integer value = (Integer) valueAnimator.getAnimatedValue();
                        tv_my_coin.setText(CommonUtil.setComma(value + "", false, false) + getResources().getString(R.string.trophy_cnt));
                    } catch (Exception ignore) {
                    }
                }
            });
            va.start();
            va.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (!Applications.isReward) {
                        Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.reward_trophy_toast, "" + (int) (refreshCoin - nowCoin)), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM, 0, CommonUtil.TOAST_YOFFSET);
                        toast.show();
                    }
                    Applications.isReward = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
        } else {
            ValueAnimator va = ValueAnimator.ofInt(nowCoin, refreshCoin);
            va.setDuration(1000);
            va.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.anim.decelerate_interpolator));
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    try {
                        Integer value = (Integer) valueAnimator.getAnimatedValue();
                        tv_my_coin.setText(CommonUtil.setComma(value + "", false, false) + getResources().getString(R.string.trophy_cnt));
                    } catch (Exception ignore) {
                    }
                }
            });
            va.start();
        }
    }

    public void showErrorNetwork(final String param, final String action, final String type) {
        System.out.println("action : " + action + ", type : " + type + ", param : " + param);
        try {
            //HideLoadingProgress();
        } catch (Exception ignore) {
        }
        if (networkErrorHash == null) {
            networkErrorHash = new HashMap<>();
        }
        if (networkErrorHash.get(param + action + type) == null) {
            NetworkErrorModel networkErrorModel = new NetworkErrorModel();
            networkErrorModel.setAction(action);
            networkErrorModel.setParam(param);
            networkErrorModel.setType(type);
            networkErrorHash.put(param + action + type, networkErrorModel);
        }

        if (networkDialog == null) {
            networkDialog = new NetworkDialog(this);
        }
        if (!networkDialog.isShowing()) {
            networkDialog.setCancelClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        //HideLoadingProgress();
                        networkErrorHash.clear();
                        networkDialog.dismiss();
                        //ActivityCompat.finishAffinity();
                        finish();
                    } catch (Exception ignore) {
                    }
                }
            });
            networkDialog.setOkClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //ShowLoadingProgress();
                    System.out.println(networkErrorHash.size());
                    synchronized (networkErrorHash) {
                        try {
                            for (String key : networkErrorHash.keySet()) {
                                NetworkErrorModel networkErrorModel = networkErrorHash.get(key);
                                System.out.println(String.format("action : %s,  키 : %s, 값 : %s", networkErrorModel.getAction(), key, networkErrorHash.get(key)));
                                requestAsyncTask(networkErrorModel.getParam(), networkErrorModel.getAction());
                                //networkErrorHash.remove(key);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            networkErrorHash.clear();
                        }
                    }
                    try {
                        //HideLoadingProgress();
                    } catch (Exception ignore) {
                    }
                    networkDialog.dismiss();
                }
            });
            networkDialog.show();
        }
    }

    private void getLauncher() {
        if (homeList == null) {
            homeList = new HashMap<>();
        }
        homeList.clear();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> lst = getPackageManager().queryIntentActivities(intent, 0);
        if (!lst.isEmpty()) {
            for (ResolveInfo resolveInfo : lst) {
                Log.d("Test", "New Launcher Found: " + resolveInfo.activityInfo.packageName);
                homeList.put(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.packageName);
            }
        }
    }

    public void checkAlarm() {
        ArrayList<AlarmNoti> alarmNotis = Applications.dbHelper.getAlarmNoti();
        //Log.e("alarmNotis size", ""+alarmNotis.size());
        if (alarmNotis != null && !alarmNotis.isEmpty() && alarmNotis.size() > 0) {
            for (int i = 0; i < alarmNotis.size(); i++) {
                AlarmNoti alarmNoti = alarmNotis.get(i);
                int notiid = Integer.parseInt(alarmNoti.getNotiid());
                long ex_time = Long.parseLong(alarmNoti.getEx_time());
                Intent intent = new Intent("EVENTNOTI");
                intent.putExtra("ltitle", alarmNoti.getTitle());
                intent.putExtra("msg", alarmNoti.getMessage());
                intent.putExtra("notiid", notiid);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notiid, intent, 0);
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, ex_time, (60 * 1000), pendingIntent);
            }
        }
    }

    public void onResumeCheker() {
        try {
            Intent getIntent = getIntent();
            Log.e(TAG, "getaction : " + getIntent.getAction());
            if (getIntent != null && getIntent.getAction() != null && (getIntent.getAction().equals("HABIT_REWARD") || getIntent.getAction().equals("HABIT_REWARD_POPUP") || getIntent.getAction().equals("HABIT_INTENT"))) {
                Applications.isReward = false;
                if ((getIntent.getAction().equals("HABIT_REWARD") || getIntent.getAction().equals("HABIT_REWARD_POPUP")) && getIntent.getStringExtra("autocash").equals("N")) {
                    isGoMission = false;
                } else {
                    isGoMission = true;
                }

                Applications.isOfferWall = true;
                Applications.isHomeRefresh = false;
                try {
                    if (getIntent.getExtras().getString("type") != null) {
                        String type = getIntent.getExtras().getString("type");
                        if (type.equals("partner")) {
                            FileCacheFactory.initialize(this);
                            if (FileCacheFactory.getInstance().has(CommonUtil.cacheNameInvite)) {
                                FileCacheFactory.getInstance().get(CommonUtil.cacheNameInvite).clear();
                            }
                        }
                    }
                } catch (Exception ignore) {
                }
                if (getIntent.getAction().equals("HABIT_REWARD")) {
                    Applications.isOfferWall = true;
                    Applications.isHomeRefresh = false;
                    Applications.isMissionRefresh = true;
                    if (isGoMission) {
                        goToMissionNoAnim();
                    }
                    //tracker.send(new HitBuilders.EventBuilder().setCategory(analiticsCategory).setAction("/HABIT_REWARD").build());
                } else if (getIntent.getAction().equals("HABIT_REWARD_POPUP")) {
                    Applications.isReward = true;
                    Applications.isOfferWall = true;
                    Applications.isHomeRefresh = false;
                    Applications.isMissionRefresh = true;
                    if (isGoMission) {
                        goToMissionNoAnim();
                    }
                    tracker.send(new HitBuilders.EventBuilder().setCategory(analiticsCategory).setAction("/HABIT_REWARD_POPUP").build());
                } else if (getIntent.getAction().equals("HABIT_INTENT")) {
                    //Applications.isReward = false;
                    tracker.send(new HitBuilders.EventBuilder().setCategory(analiticsCategory).setAction("/HABIT_INTENT").build());
                }
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();

                if (getIntent.getAction().equals("HABIT_REWARD") || getIntent.getAction().equals("HABIT_REWARD_POPUP")) {
                    Applications.isReward = true;
                    //Applications.isHomeRefresh = true;
                    Applications.isMissionRefresh = false;
                    if (Applications.isHomeRefresh) {
                        requestInfo();
                    }
                }

                setIntent(new Intent().setAction(null));
            } else if (getIntent != null && getIntent.getScheme() != null && getIntent.getScheme().equals("habit")) {
                setIntent(new Intent().setAction(null));
                goToMissionNoAnim();
                Applications.isOfferWall = true;
                //Applications.isReward = false;
            } else if (getIntent != null && getIntent.getAction() != null && (getIntent.getAction().equals("HABIT_GIFT") || getIntent.getAction().equals(CommonUtil.CLICK_ACTION_PURCHASE))) {
                //Applications.isReward = false;
                try {
                    FileCacheFactory.initialize(this);
                    if (FileCacheFactory.getInstance().has(CommonUtil.giftboxCache)) {
                        FileCacheFactory.getInstance().get(CommonUtil.giftboxCache).clear();
                        Log.e("clear", "clear giftboxCache2");
                    }
                } catch (Exception ignore) {
                }
                Applications.isGiftBoxGo = true;
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
                setIntent(new Intent().setAction(null));

                requestMyGift(false);
            } else {
                //Applications.isReward = false;
                if (!Applications.isStart) {
                    Applications.isOfferWall = true;
                    Applications.isHomeRefresh = true;
                    Applications.isStart = true;
                }
                Applications.isCashpopPopup = false;
                tracker.send(new HitBuilders.EventBuilder().setCategory(analiticsCategory).setAction("/HABIT_START").build());
                if (Applications.isHomeRefresh) {
                    requestInfo();
                }
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }

    public void goToHistory() {
        startActivity(new Intent(this, HistoryActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void goToMission() {
        isGoMission = false;
        startActivity(new Intent(this, MissionActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void goToMissionNoAnim() {
        isGoMission = false;
        startActivity(new Intent(this, MissionActivity.class));
        overridePendingTransition(0, 0);
    }

    public void goToInvitePartner() {
        startActivity(new Intent(this, InviteActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void goToStore() {
        startActivity(new Intent(this, StoreActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void goToGiftBox() {
        startActivity(new Intent(this, GiftBoxActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void goToSetting() {
        startActivity(new Intent(this, SettingActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void versionChk() {
        long now = System.currentTimeMillis();
        String versionChk = Applications.preference.getValue(Preference.VERSION_CHK_TIMESTAMP, now + "");
        long versionTime;
        if (versionChk != null && !versionChk.equals("") && versionChk.matches("^[0-9]+$")) {
            try {
                versionTime = Long.parseLong(versionChk);
            } catch (Exception ignore) {
                versionTime = now;
            }
        } else {
            versionTime = now;
        }
        if (versionTime <= now) {
            Applications.preference.put(Preference.VERSION_CHK_TIMESTAMP, (now + (3600000 * 12)) + "");
            HashMap<String, String> map = new HashMap<>();
            map.put(CommonUtil.KEY_ACTION, CommonUtil.ACTION_VERSION);
            String param = APICrypto.getParam(this, map, CommonUtil.SHARED_KEY);
            requestAsyncTask(param, CommonUtil.ACTION_VERSION);
        } else {
            int version = CommonUtil.getVersionCode(this);
            int nowVersion = version;
            try {
                nowVersion = Integer.parseInt(Applications.ePreference.getValue(EPreference.VERSION_C, ""));
            } catch (Exception ignore) {
            }
            final String v_p = Applications.ePreference.getValue(EPreference.VERSION_P, "");
            if (version < nowVersion) {
                cashPopDialog = new CashPopDialog(this);
                cashPopDialog.setCpTitle(getResources().getString(R.string.habit2good_update));
                cashPopDialog.setCpDesc(getResources().getString(R.string.habit2good_update_desc));
                cashPopDialog.setCpBOkButton(getResources().getString(R.string.update_now), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        if (v_p != null && !v_p.equals("")) {
                            intent.setData(Uri.parse("market://details?id=" + v_p));
                        } else {
                            intent.setData(Uri.parse("market://details?id=" + getPackageName()));
                        }
                        startActivity(intent);
                    }
                });
                cashPopDialog.setCpBCancelButton(getResources().getString(R.string.exit), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.exit(0);
                    }
                });
                cashPopDialog.setCpCancel(false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cashPopDialog.show();
                    }
                });
            }
        }
    }

    private void manPlusBannerAdcreateBannerXMLMode() {
        m_adView = (AdView) findViewById(R.id.ad);
        /*m_adView.setUserAge("1");
        m_adView.setUserGender("3");
        m_adView.setEmail("few.com");
        m_adView.setAccount("id");*/
        m_adView.setAdListener(new AdListener() {
            @Override
            public void onChargeableBannerType(View view, boolean b) {

            }

            @Override
            public void onFailedToReceive(View view, int i) {

            }

            @Override
            public void onInterClose(View view) {

            }

            @Override
            public void onAdClick(View view) {

            }
        });
        m_adView.isAnimateImageBanner(true);
    }

    private void manPlusEndingView(final int p, final int m, final int s, int media) {
        mEndingDialog = new EndingDialog(PointmallActivity.this, getResources().getString(R.string.ending_dialog_title), leftClickListener2, rightClickListener2);
        mEndingDialog.setCode(p, m, s, media);
        mEndingDialog.show();
    }

    private View.OnClickListener leftClickListener2 = new View.OnClickListener() {
        public void onClick(View v) {
            if (mEndingDialog != null) {
                mEndingDialog.dismiss();
                mEndingDialog = null;
            }
            finish();
        }

    };
    private View.OnClickListener rightClickListener2 = new View.OnClickListener() {
        public void onClick(View v) {
            if (mEndingDialog != null) {
                mEndingDialog.dismiss();
                mEndingDialog = null;
            }
        }
    };
}
