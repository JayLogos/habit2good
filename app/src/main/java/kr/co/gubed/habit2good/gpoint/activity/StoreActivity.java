package kr.co.gubed.habit2good.gpoint.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mapps.android.share.AdInfoKey;
import com.mapps.android.view.AdInterstitialView;
import com.mapps.android.view.AdView;
import com.mz.common.listener.AdListener;
import com.tnkfactory.ad.BannerAdListener;
import com.tnkfactory.ad.BannerAdType;
import com.tnkfactory.ad.BannerAdView;
import com.tnkfactory.ad.TnkAdListener;
import com.tnkfactory.ad.TnkSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import kr.co.gubed.habit2good.R;
import kr.co.gubed.habit2good.gpoint.adapter.StoreAdapter;
import kr.co.gubed.habit2good.gpoint.adapter.StoreCategoryAdapter;
import kr.co.gubed.habit2good.gpoint.filecache.ByteProviderUtil;
import kr.co.gubed.habit2good.gpoint.filecache.FileCache;
import kr.co.gubed.habit2good.gpoint.filecache.FileCacheFactory;
import kr.co.gubed.habit2good.gpoint.listener.AsyncTaskCompleteListener;
import kr.co.gubed.habit2good.gpoint.listener.BuyListener;
import kr.co.gubed.habit2good.gpoint.model.NetworkErrorModel;
import kr.co.gubed.habit2good.gpoint.util.APICrypto;
import kr.co.gubed.habit2good.gpoint.util.Applications;
import kr.co.gubed.habit2good.gpoint.util.AsyncHTTPPost;
import kr.co.gubed.habit2good.gpoint.util.CommonUtil;
import kr.co.gubed.habit2good.gpoint.util.EPreference;
import kr.co.gubed.habit2good.gpoint.util.Preference;
import kr.co.gubed.habit2good.gpoint.view.CashPopDialog;
import kr.co.gubed.habit2good.gpoint.view.LoadingDialog;
import kr.co.gubed.habit2good.gpoint.view.NetworkDialog;
import kr.co.gubed.habit2good.gpoint.view.StoreNewPurchaseDialog;

public class StoreActivity extends Activity implements View.OnClickListener, AsyncTaskCompleteListener<String>, AdapterView.OnItemClickListener, BuyListener {

    private String TAG = this.getClass().toString();

    private TextView tv_title;

    /* Ad */
    private LinearLayout type_admob;        // adMob
    private BannerAdView bannerAdView;      // TNK
    private RelativeLayout tnkBanner;
    private RelativeLayout manplusBanner;
    private AdView m_adView = null;                 // MANPLUS
    private AdInterstitialView m_interView = null;
    private Handler handler = new Handler();

    private Button btn_back;
    private Button btn_info;

    private LinearLayout btn_history;

    private GridView listViewCategory;
    private StoreCategoryAdapter storeCategoryAdapter;
    private GridView listViewStore;
    private StoreAdapter storeAdapter;

    private ImageView iv_my_gpoint;
    private TextView tv_my_gpoint;

    private boolean isPurchase = false;

    private Applications applications;
    private Tracker tracker;

    private String analiticsCategory = "/store";
    private FileCache fileCache;

    private ArrayList<JSONObject> storeCategoryList;
    private ArrayList<JSONObject> storeProductList;
    private HashMap<Integer, JSONObject> template;

    private LoadingDialog loadingDialog;
    private NetworkDialog networkDialog;
    private CashPopDialog cashPopDialog;
    private StoreNewPurchaseDialog storeNewPurchaseDialog;

    private HashMap<String, NetworkErrorModel> networkErrorHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tstore);

        applications = (Applications)getApplication();
        tracker = applications.getDefaultTracker();
        tracker.setScreenName(analiticsCategory);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        getFileCache();

        this.init();

    }

    @Override
    protected void onResume() {
        super.onResume();
        versionChk();
        applications.setRefreshActivity(this);

        if (bannerAdView != null) {
            bannerAdView.onResume();
        }

        if (m_adView != null)
            m_adView.StartService();

        TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
            @Override
            public void onClose(int i) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }

            @Override
            public void onShow() {

            }

            @Override
            public void onFailure(int i) {
                Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
            }

            @Override
            public void onLoad() {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bannerAdView != null) {
            bannerAdView.onPause();
        }

        if (m_adView != null)
            m_adView.StopService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bannerAdView != null) {
            bannerAdView.onDestroy();
        }
    }

    public void init(){
        tnkBanner = findViewById(R.id.tnk_banner);
        manplusBanner = findViewById(R.id.manplus_banner);
        bannerAdView = (BannerAdView) findViewById(R.id.banner_ad);
        bannerAdView.setBannerAdListener(new BannerAdListener() {

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
        //bannerAdView.loadAd(TnkSession.CPC, BannerAdType.LANDSCAPE); // or bannerAdView.loadAd(TnkSession.CPC, BannerAdType.LANDSCAPE)
        manPlusBannerAdcreateBannerXMLMode();

        /*type_admob = (LinearLayout)findViewById(R.id.type_admob);

        try {
            if ((Applications.adView.getParent()) != null) {
                ((ViewGroup) Applications.adView.getParent()).removeAllViews();
                type_admob.addView(Applications.adView);
            }else{
                type_admob.addView(Applications.adView);
            }
        }catch (Exception ignore){
            ignore.printStackTrace();
        }*/

        tv_title = (TextView)findViewById(R.id.tv_title);
        tv_title.setText(getResources().getString(R.string.title_store));
        btn_back = (Button)findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);
        btn_info = (Button)findViewById(R.id.btn_info);
        btn_info.setOnClickListener(this);

        btn_history = (LinearLayout)findViewById(R.id.btn_history);
        btn_history.setOnClickListener(this);

        template = new HashMap();

        storeCategoryList = new ArrayList<>();

        listViewCategory = (GridView) findViewById(R.id.listViewCategory);
        storeCategoryAdapter = new StoreCategoryAdapter(this, R.layout.row_store_category, storeCategoryList);
        listViewCategory.setAdapter(storeCategoryAdapter);
        listViewCategory.setOnItemClickListener(this);

        storeProductList = new ArrayList<>();
        listViewStore = (GridView) findViewById(R.id.listViewStore);
        listViewStore.setOnItemClickListener(this);

        iv_my_gpoint = (ImageView)findViewById(R.id.iv_my_gpoint);

        tv_my_gpoint = (TextView)findViewById(R.id.tv_my_gpoint);
        tv_my_gpoint.setText(CommonUtil.setComma(Applications.ePreference.getBalanceGpoint() + "", true, false));

        Applications.isStoreRefresh = true;

        storeNewPurchaseDialog = new StoreNewPurchaseDialog(this);

        refresh();
    }

    @Override
    public void onBackPressed() {
        if( isPurchase){
            closePurchase();
            return;
        }
        try{
            if( loadingDialog != null && loadingDialog.isShowing()){
                loadingDialog.dismiss();
                loadingDialog = null;
            }
            if( networkDialog != null && networkDialog.isShowing()){
                networkDialog.dismiss();
                networkDialog = null;
            }
        }catch (Exception ignore){
            ignore.printStackTrace();
        }

        /*TnkSession.showInterstitialAd(this, 1000, new TnkAdListener() {
            @Override
            public void onClose(int i) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }

            @Override
            public void onShow() {

            }

            @Override
            public void onFailure(int i) {
                Log.e(getClass().getName(), "TNK interstitial showing fail: "+i);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }

            @Override
            public void onLoad() {

            }
        });*/
        manPlusInterstitialView(1413, 31780, 803854);

        //super.onBackPressed();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_back:
                onBackPressed();
                break;
            case R.id.btn_info:
                CommonUtil.showSupport(StoreActivity.this, true);
                break;
            case R.id.btn_history:
                goHistory();
                break;
        }
    }

    public FileCache getFileCache(){
        if( fileCache == null){
            FileCacheFactory.initialize(this);
            if( !FileCacheFactory.getInstance().has(CommonUtil.storeNewCache)){
                FileCacheFactory.getInstance().create(CommonUtil.storeNewCache, 1024*4);
            }
            fileCache = FileCacheFactory.getInstance().get(CommonUtil.storeNewCache);
        }
        return fileCache;
    }

    public void versionChk(){
        long now = System.currentTimeMillis();
        String versionChk = Applications.preference.getValue(Preference.VERSION_CHK_TIMESTAMP, now+"");
        long versionTime;
        if( versionChk != null && !versionChk.equals("") && versionChk.matches("^[0-9]+$")){
            try {
                versionTime = Long.parseLong(versionChk);
            }catch (Exception ignore){
                versionTime = now;
            }
        }else{
            versionTime = now;
        }
        if( versionTime <= now) {
            Applications.preference.put(Preference.VERSION_CHK_TIMESTAMP, (now+(3600000*12))+"");
            HashMap<String, String> map = new HashMap<>();
            map.put(CommonUtil.KEY_ACTION, CommonUtil.ACTION_VERSION);
            String param = APICrypto.getParam(this, map, CommonUtil.SHARED_KEY);
            requestAsyncTask(param, CommonUtil.ACTION_VERSION);
        }else{
            int version = CommonUtil.getVersionCode(this);
            int nowVersion = version;
            try {
                nowVersion = Integer.parseInt(Applications.ePreference.getValue(EPreference.VERSION_C, ""));
            }catch (Exception ignore){}
            final String v_p = Applications.ePreference.getValue(EPreference.VERSION_P, "");
            if( version < nowVersion){
                cashPopDialog = new CashPopDialog(this);
                cashPopDialog.setCpTitle(getResources().getString(R.string.habit2good_update));
                cashPopDialog.setCpDesc(getResources().getString(R.string.habit2good_update_desc));
                cashPopDialog.setCpBOkButton(getResources().getString(R.string.update_now), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        if( v_p != null && !v_p.equals("")) {
                            intent.setData(Uri.parse("market://details?id=" + v_p));
                        }else{
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

    public void requestAsyncTask(String param, String action){
        if( Applications.getCountry(this).equals("KR") && !Applications.isRoaming(this)) {
            new AsyncHTTPPost(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, CommonUtil.SERVER_URL, param, action);
        }else{
            new AsyncHTTPPost(this).execute(CommonUtil.SERVER_URL, param, action);
        }
    }

    public void ShowLoadingProgress() {
        //show loading
        try {
            if( loadingDialog == null){
                loadingDialog = new LoadingDialog(this);
            }
            this.runOnUiThread(new Runnable() {
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
    }

    public void HideLoadingProgress() {
        //hide loading
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    loadingDialog.dismiss();
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
            }
        });
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
        Applications.ePreference.putBalanceGpoint(0);
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

        FileCacheFactory.initialize(StoreActivity.this);
        if( FileCacheFactory.getInstance().has(CommonUtil.cacheNameInvite)){
            FileCacheFactory.getInstance().get(CommonUtil.cacheNameInvite).clear();
        }
        if( FileCacheFactory.getInstance().has(CommonUtil.cacheName)){
            FileCacheFactory.getInstance().get(CommonUtil.cacheName).clear();
        }
        if( FileCacheFactory.getInstance().has(CommonUtil.cacheNameNotice)){
            FileCacheFactory.getInstance().get(CommonUtil.cacheNameNotice).clear();
        }
        if( FileCacheFactory.getInstance().has(CommonUtil.cacheNameHistory)){
            FileCacheFactory.getInstance().get(CommonUtil.cacheNameHistory).clear();
        }
        if( FileCacheFactory.getInstance().has(CommonUtil.noticeCache)){
            FileCacheFactory.getInstance().get(CommonUtil.noticeCache).clear();
        }
        if( FileCacheFactory.getInstance().has(CommonUtil.inviteCache)){
            FileCacheFactory.getInstance().get(CommonUtil.inviteCache).clear();
        }
        if( FileCacheFactory.getInstance().has(CommonUtil.adCache)){
            FileCacheFactory.getInstance().get(CommonUtil.adCache).clear();
        }
        if( FileCacheFactory.getInstance().has(CommonUtil.eventCache)){
            FileCacheFactory.getInstance().get(CommonUtil.eventCache).clear();
        }
        if( FileCacheFactory.getInstance().has(CommonUtil.storeCache)){
            FileCacheFactory.getInstance().get(CommonUtil.storeCache).clear();
        }
        if( FileCacheFactory.getInstance().has(CommonUtil.storeNewCache)){
            FileCacheFactory.getInstance().get(CommonUtil.storeNewCache).clear();
        }
        if( FileCacheFactory.getInstance().has(CommonUtil.noticePopCache)){
            FileCacheFactory.getInstance().get(CommonUtil.noticePopCache).clear();
        }

        Intent intent = new Intent(StoreActivity.this, SignActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    @Override
    public void onTaskComplete(String result) {
        String rst;
        try {
            rst = APICrypto.decrypt(CommonUtil.SHARED_KEY, result);
        } catch (Exception e) {
            rst = result;
            e.printStackTrace();
        }
        try {
            JSONObject jo = new JSONObject(rst);
            String error = jo.getString(CommonUtil.RESULT_ERROR);
            String action = jo.getString(CommonUtil.RESULT_ACTION);
            if( error != null && error.isEmpty() && action != null && !action.isEmpty()) {
                if( action.equals(CommonUtil.ACTION_GET_INFO)){
                    String gold = jo.getString(CommonUtil.RESULT_BUDGET);
                    String coin = jo.getString(CommonUtil.RESULT_COIN);
                    final double nowGold = Applications.ePreference.getBalanceGpoint();
                    final int nowCoin = Applications.ePreference.getValue(EPreference.N_TROPHY, 0);
                    if( nowGold < Double.parseDouble(gold) || nowCoin < Integer.parseInt(coin)) {
                        setGoldCoin(Double.parseDouble(gold), Integer.parseInt(coin), "reward");
                    }else{
                        setGoldCoin(Double.parseDouble(gold), Integer.parseInt(coin), "");
                    }

                    String linked_gold = jo.getString(CommonUtil.RESULT_LINKED_GOLD);
                    setLinkedGold(Double.parseDouble(linked_gold));
                    String normal_gold = jo.getString(CommonUtil.RESULT_BALANCE_GPOINT);
                    setNormalGold(Double.parseDouble(normal_gold));
/*
                    String invited_partners = jo.getString(CommonUtil.RESULT_INVITED_PARTNERS);

                    if( !invited_partners.equals(Applications.preference.getValue(Preference.PARTNERS, "0"))){
                        FileCacheFactory.initialize(StoreActivity.this);
                        if( FileCacheFactory.getInstance().has(CommonUtil.cacheNameInvite)){
                            FileCacheFactory.getInstance().get(CommonUtil.cacheNameInvite).clear();
                        }
                    }
                    String partner_gpoint = jo.getString(CommonUtil.RESULT_PARTNER_GPOINT);

                    Applications.preference.put(Preference.REDEEMCODE, Long.toString(Long.parseLong(Applications.preference.getValue(Preference.USER_ID, "")), 36));

                    Applications.preference.put(Preference.PARTNERS, invited_partners);
                    Applications.preference.put(Preference.PARTNER_GPOINT, partner_gpoint);
*/
                    Applications.preference.put(Preference.CPID, jo.getString("id"));

                    Applications.preference.put(Preference.BIRTH, jo.getString(CommonUtil.RESULT_YEAR));
                    Applications.preference.put(Preference.LOCATION, jo.getString(CommonUtil.RESULT_LOCATION));
                    Applications.preference.put(Preference.GENDER, jo.getString(CommonUtil.RESULT_GENDER));
                    Applications.preference.put(Preference.MARRIAGE, jo.getString(CommonUtil.RESULT_MARRIAGE));
                    Applications.preference.put(Preference.PARTNERCDOE, jo.getString(CommonUtil.RESULT_PARTNERCODE));
                    Applications.preference.put(Preference.REVIEW, jo.getString(CommonUtil.RESULT_REVIEW));
                    Applications.preference.put(Preference.MISSION, jo.getString(CommonUtil.RESULT_MISSION));

                    if( jo.getString(CommonUtil.RESULT_ALARM).equals("1") || jo.getString(CommonUtil.RESULT_ALARM).equals("")){
                        Applications.preference.put(Preference.CASH_POP_ALARM, true);
                        FirebaseMessaging.getInstance().subscribeToTopic(Applications.getTopicId(StoreActivity.this));
                    }else{
                        Applications.preference.put(Preference.CASH_POP_ALARM, false);
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(Applications.getTopicId(StoreActivity.this));
                    }

                    HideLoadingProgress();

                }else if (action.equals(CommonUtil.ACTION_VERSION)) {
                    final String v_n = jo.getString("v_n");
                    final String v_p = jo.getString("v_p");
                    final int v_c = Integer.parseInt(jo.getString("v_c"));
                    Applications.ePreference.put(EPreference.VERSION_N, v_n+"");
                    Applications.ePreference.put(EPreference.VERSION_P, v_p+"");
                    Applications.ePreference.put(EPreference.VERSION_C, v_c+"");
                    int version = CommonUtil.getVersionCode(this);
                    if( version < v_c){
                        cashPopDialog = new CashPopDialog(this);
                        cashPopDialog.setCpTitle(getResources().getString(R.string.habit2good_update));
                        cashPopDialog.setCpDesc(getResources().getString(R.string.habit2good_update_desc));
                        cashPopDialog.setCpBOkButton(getResources().getString(R.string.update_now), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                if( v_p != null && !v_p.equals("")) {
                                    intent.setData(Uri.parse("market://details?id=" + v_p));
                                }else{
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
                        this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cashPopDialog.show();
                            }
                        });
                    }
                }else if( action.equals(CommonUtil.ACTION_GET_STORELIST)){
                    storeCategoryList.clear();
                    template.clear();
                    String purchase = jo.getString(CommonUtil.RESULT_PURCHASE);
                    Applications.ePreference.putNPurchaseGold(Integer.parseInt(purchase));
                    Log.e("l",""+jo.getString("l"));
                    JSONArray jobjArr = new JSONArray(jo.getString("l"));
                    ArrayList<JSONObject> jsonObjList = new ArrayList<>();
                    for (int i = 0; i < jobjArr.length(); i++) {
                        jsonObjList.add(jobjArr.getJSONObject(i));
                    }
                    storeCategoryList.addAll(jsonObjList);
                    if( storeCategoryAdapter != null) {
                        listViewCategory.setVisibility(View.VISIBLE);
                        storeCategoryAdapter.notifyDataSetChanged();
                    }
                    JSONObject cacheRst = new JSONObject();
                    cacheRst.put(CommonUtil.KEY_TIMESTAMP, System.currentTimeMillis());
                    cacheRst.put(CommonUtil.KEY_RST, rst);
                    fileCache.put(CommonUtil.storeNewCache, ByteProviderUtil.create(cacheRst.toString()));

                    JSONArray templateArr = new JSONArray(jo.getString("tl"));
                    for(int i=0;i<templateArr.length();i++){
                        template.put(templateArr.getJSONObject(i).getInt("id"), templateArr.getJSONObject(i));
                    }
                    for(int key : template.keySet()){
                        System.out.println( String.format("키 : %s, 값 : %s", key, template.get(key)));
                    }

                }else if( action.equals(CommonUtil.ACTION_STORE_REQUEST)){
                    String gold = jo.getString(CommonUtil.RESULT_BUDGET);
                    String purchase = jo.getString(CommonUtil.RESULT_PURCHASE);
                    String completeTitle = jo.getString(CommonUtil.RESULT_COMPLETE_TITLE);
                    String completeDesc = jo.getString(CommonUtil.RESULT_COMPLETE_DESC);
                    String mileage_back = jo.getString(CommonUtil.RESULT_MILEAGE_BACK);
                    String mileage_back_per = jo.getString(CommonUtil.RESULT_MILEAGE_BACK_PER);
                    String coin = jo.getString(CommonUtil.RESULT_COIN);
                    setGoldCoin(Double.parseDouble(gold), Integer.parseInt(coin), "");
                    Applications.ePreference.putNPurchaseGold(Double.parseDouble(purchase));
                    cashPopDialog = new CashPopDialog(this);
                    cashPopDialog.setCpTitle(completeTitle);
                    cashPopDialog.setCpDesc(completeDesc);
                    cashPopDialog.setCpOkButton(getResources().getString(R.string.btn_confirm), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            cashPopDialog.dismiss();
                        }
                    });
                    cashPopDialog.show();
                    Applications.isHomeRefresh = true;
                    refresh();
                    listViewCategory.setVisibility(View.GONE);
                    storeAdapter.notifyDataSetChanged();
                    if( !mileage_back.equals("0") && !mileage_back.equals("") && !mileage_back_per.equals("0") && !mileage_back_per.equals("")) {
                        Toast toast = Toast.makeText(this, getResources().getString(R.string.mileage_toast, CommonUtil.setComma(mileage_back, true, false), mileage_back_per + "%"), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM, 0, CommonUtil.TOAST_YOFFSET);
                        toast.show();
                    }
                    Applications.isGiftBoxRe = true;

                    try{
                        FileCacheFactory.initialize(this);
                        if (FileCacheFactory.getInstance().has(CommonUtil.giftboxCache)) {
                            FileCacheFactory.getInstance().get(CommonUtil.giftboxCache).clear();
                        }
                    }catch (Exception ignore){}

                }
            }else if( error != null && error.equals(CommonUtil.ERROR_NO_USER)) {
                cashPopDialog = new CashPopDialog(this);
                cashPopDialog.setCpTitle(this.getResources().getString(R.string.logout));
                cashPopDialog.setCpDesc(this.getResources().getString(R.string.auto_logout_no_user));
                cashPopDialog.setCpCancel(false);
                cashPopDialog.setCpOkButton(this.getResources().getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Logout();
                    }
                });
                cashPopDialog.show();
            }else if( error != null && error.equals(CommonUtil.ERROR_NO_AD)){
                Toast toast = Toast.makeText(this, this.getResources().getString(R.string.cpi_end), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM, 0, CommonUtil.TOAST_YOFFSET);
                toast.show();
            }else if(  error != null && error.equals(CommonUtil.ERROR_LIMIT)){
                cashPopDialog = new CashPopDialog(this);
                cashPopDialog.setCpTitle(this.getResources().getString(R.string.error_occurrerd));
                cashPopDialog.setCpDesc(this.getResources().getString(R.string.limit_text));
                cashPopDialog.setCpOkButton(this.getResources().getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cashPopDialog.dismiss();
                    }
                });
                cashPopDialog.show();
            }else if( error != null && error.equals(CommonUtil.ERROR_VERSION)){
                cashPopDialog = new CashPopDialog(this);
                cashPopDialog.setCpTitle(getResources().getString(R.string.habit2good_update));
                cashPopDialog.setCpDesc(getResources().getString(R.string.habit2good_update_desc));
                cashPopDialog.setCpBOkButton(getResources().getString(R.string.update_now), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=" + getPackageName()));
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
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cashPopDialog.show();
                    }
                });
            }else if( error != null && error.equals(CommonUtil.ERROR_ERROR_PURCHASE)){
                cashPopDialog = new CashPopDialog(this);
                cashPopDialog.setCpTitle(this.getResources().getString(R.string.error_store_fail));
                cashPopDialog.setCpDesc(this.getResources().getString(R.string.error_store));
                cashPopDialog.setCpCancel(false);
                cashPopDialog.setCpOkButton(this.getResources().getString(R.string.confirm), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cashPopDialog.dismiss();
                    }
                });
                cashPopDialog.show();
            } else if(  error != null && error.equals(CommonUtil.ERROR_NO_BUDGET)){
                cashPopDialog = new CashPopDialog(this);
                cashPopDialog.setCpTitle(getResources().getString(R.string.not_enough_gold_title));
                cashPopDialog.setCpDesc(getResources().getString(R.string.not_enough_gold));
                cashPopDialog.setCpCancelButton(getResources().getString(R.string.hold), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cashPopDialog.dismiss();
                    }
                });
                cashPopDialog.setCpOkButton(getResources().getString(R.string.go_to_gold), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        goToMission();
                        cashPopDialog.dismiss();
                    }
                });
                cashPopDialog.show();
            } else if(  error != null && error.equals(CommonUtil.ERROR_NO_PURCHASE)){
                cashPopDialog = new CashPopDialog(this);
                cashPopDialog.setCpTitle(getResources().getString(R.string.no_purchase_title));
                cashPopDialog.setCpDesc(getResources().getString(R.string.no_purchase_desc));
                cashPopDialog.setCpCancelButton(getResources().getString(R.string.no_purchase_cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cashPopDialog.dismiss();
                    }
                });
                cashPopDialog.setCpOkButton(getResources().getString(R.string.no_purchase_question), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CommonUtil.showSupport(StoreActivity.this, true);
                        cashPopDialog.dismiss();
                    }
                });
                cashPopDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            HideLoadingProgress();
        } finally {
            HideLoadingProgress();
        }
    }

    @Override
    public void onTaskError(String param, String action, String result) {
        HideLoadingProgress();
        try{
            Log.e(TAG, action);
            if( action.equals(CommonUtil.ACTION_GET_STORELIST)){
                String cacheRst;
                if( fileCache.get(CommonUtil.storeNewCache) != null){
                    try{
                        InputStream is = fileCache.get(CommonUtil.storeNewCache).getInputStream();
                        int size = is.available();
                        byte[] buffer = new byte[size];
                        is.read(buffer);
                        is.close();
                        cacheRst = new String(buffer);
                        Log.e(TAG, cacheRst);
                        storeCategoryList.clear();
                        template.clear();
                        JSONObject job = new JSONObject(cacheRst);
                        JSONObject jo = new JSONObject(job.getString(CommonUtil.KEY_RST));
                        JSONArray jobjArr = new JSONArray(jo.getString("l"));
                        ArrayList<JSONObject> jsonObjList = new ArrayList<>();
                        for (int i = 0; i < jobjArr.length(); i++) {
                            jsonObjList.add(jobjArr.getJSONObject(i));
                        }
                        storeCategoryList.addAll(jsonObjList);
                        if( storeCategoryAdapter != null) {
                            listViewStore.setVisibility(View.GONE);
                            listViewCategory.setVisibility(View.VISIBLE);
                            storeCategoryAdapter.notifyDataSetChanged();
                        }
                        JSONArray templateArr = new JSONArray(jo.getString("tl"));
                        for(int i=0;i<templateArr.length();i++){
                            template.put(templateArr.getJSONObject(i).getInt("id"), templateArr.getJSONObject(i));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }else{
                    listViewStore.setVisibility(View.GONE);
                    listViewCategory.setVisibility(View.GONE);
                }

                showErrorNetwork(param, action, "store");
            }else if( action.equals(CommonUtil.ACTION_STORE_REQUEST)){
                showErrorNetwork(param, action, "store");
            }
        }catch (Exception ignore){
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()){
            case R.id.listViewCategory:
                String invite_partner = Applications.preference.getValue(Preference.PARTNERS, "0");
                int partner = 0;
                try{
                    partner = Integer.parseInt(invite_partner);
                }catch (Exception ignore){
                    partner = 0;
                    ignore.printStackTrace();
                }
                int fcnt = 0;
                try{
                    String fcnt_str = storeCategoryAdapter.getItem(i).getString("fcnt");
                    fcnt = Integer.parseInt(fcnt_str);
                }catch (Exception ignore){
                    fcnt = 0;
                    ignore.printStackTrace();
                }
                if( partner >= fcnt) {
                    setListView(storeCategoryAdapter.getItem(i));
                }else{
                    cashPopDialog = new CashPopDialog(this);
                    cashPopDialog.setCpTitle(getResources().getString(R.string.fcnt_chk));
                    cashPopDialog.setCpDesc(getResources().getString(R.string.fcnt_chk_desc, (fcnt-partner)+""));
                    cashPopDialog.setCpCancelButton(getResources().getString(R.string.fcnt_cancel), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            cashPopDialog.dismiss();
                        }
                    });
                    cashPopDialog.setCpOkButton(getResources().getString(R.string.fcnt_invite), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            goToInvitePartner();
                            cashPopDialog.dismiss();
                        }
                    });
                    cashPopDialog.show();
                }
                break;
            case R.id.listViewStore:
                try {

                    final JSONObject jobj = storeAdapter.getItem(i);
                    int gold = Integer.parseInt(jobj.getString("gold"));
                    Log.e("budget",""+Applications.ePreference.getBalanceGpoint());
                    Log.e("gold",""+gold);
                    Log.e("getPurchase",""+Applications.ePreference.getNPurchaseGold());
                    if( Applications.ePreference.getBalanceGpoint() > gold) {
                        int version = CommonUtil.getVersionCode(this);
                        if( Applications.preference.getValue(Preference.VERSION_SERVER, version) > version){
                            Toast toast = Toast.makeText(this, this.getResources().getString(R.string.habit2good_update_plz), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.BOTTOM, 0, CommonUtil.TOAST_YOFFSET);
                            toast.show();
                            return;
                        }
                        if( storeNewPurchaseDialog == null){
                            storeNewPurchaseDialog = new StoreNewPurchaseDialog(this);
                        }
                        storeNewPurchaseDialog.open(jobj, template, StoreActivity.this);
                    }else{
                        cashPopDialog = new CashPopDialog(this);
                        cashPopDialog.setCpTitle(getResources().getString(R.string.not_enough_gold_title));
                        cashPopDialog.setCpDesc(getResources().getString(R.string.not_enough_gold));
                        cashPopDialog.setCpCancelButton(getResources().getString(R.string.hold), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cashPopDialog.dismiss();
                            }
                        });
                        cashPopDialog.setCpOkButton(getResources().getString(R.string.go_to_gold), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                goToMission();
                                cashPopDialog.dismiss();
                            }
                        });
                        cashPopDialog.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void refresh(){
        Log.e(TAG,"refresh");
        if( Applications.isStoreRefresh){
            ShowLoadingProgress();
            requestPurchase();
            Applications.isStoreRefresh = false;
        }
    }

    private void requestPurchase(){
        closePurchase();
        boolean isCache = false;
        String cacheRst = "";
        tv_title.setText(this.getResources().getString(R.string.title_store));
        if( fileCache.get(CommonUtil.storeNewCache) != null){
            try{
                InputStream is = fileCache.get(CommonUtil.storeNewCache).getInputStream();
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                cacheRst = new String(buffer);
                Log.e(TAG, cacheRst);
                JSONObject job = new JSONObject(cacheRst);
                isCache = System.currentTimeMillis() - Long.parseLong(job.getString(CommonUtil.KEY_TIMESTAMP)) < 60 * 60 * 1000 * 2;
                JSONObject jo = new JSONObject(job.getString(CommonUtil.KEY_RST));
                JSONArray jobjArr = new JSONArray(jo.getString("l"));
                if( jobjArr.length() == 0){
                    isCache = false;
                }

            }catch (Exception e){
                isCache = false;
                e.printStackTrace();
            }
        }
        if( isCache){
            try{
                HideLoadingProgress();
                storeCategoryList.clear();
                template.clear();
                JSONObject job = new JSONObject(cacheRst);
                JSONObject jo = new JSONObject(job.getString(CommonUtil.KEY_RST));
                JSONArray jobjArr = new JSONArray(jo.getString("l"));
                ArrayList<JSONObject> jsonObjList = new ArrayList<>();
                for (int i = 0; i < jobjArr.length(); i++) {
                    jsonObjList.add(jobjArr.getJSONObject(i));
                }
                storeCategoryList.addAll(jsonObjList);
                if( storeCategoryAdapter != null) {
                    listViewStore.setVisibility(View.GONE);
                    listViewCategory.setVisibility(View.VISIBLE);
                    storeCategoryAdapter.notifyDataSetChanged();
                }
                JSONArray templateArr = new JSONArray(jo.getString("tl"));
                for(int i=0;i<templateArr.length();i++){
                    template.put(templateArr.getJSONObject(i).getInt("id"), templateArr.getJSONObject(i));
                }
                for(int key : template.keySet()){
                    System.out.println( String.format("키 : %s, 값 : %s", key, template.get(key)));
                }
            }catch (Exception ignore){
                ignore.printStackTrace();
            }
        }else {
            HashMap<String, String> map = new HashMap<>();
            map.put(CommonUtil.KEY_USERID, Applications.preference.getValue(Preference.USER_ID, ""));
            map.put(CommonUtil.KEY_ACTION, CommonUtil.ACTION_GET_STORELIST);
            String param = APICrypto.getParam(this, map, CommonUtil.SHARED_KEY);
            requestAsyncTask(param, CommonUtil.ACTION_GET_STORELIST);
        }
    }

    private void setListView(JSONObject job){
        try {
            String image = job.getString("image");
            storeProductList.clear();
            tv_title.setText(job.getString("category"));
            JSONArray jobjArr = new JSONArray(job.getString("products"));
            ArrayList<JSONObject> jsonObjList = new ArrayList<>();
            for (int i = 0; i < jobjArr.length(); i++) {
                jsonObjList.add(jobjArr.getJSONObject(i));
            }
            storeProductList.addAll(jsonObjList);
            storeAdapter = new StoreAdapter(this, R.layout.row_store, storeProductList, template);
            listViewStore.setAdapter(storeAdapter);
            listViewStore.setVisibility(View.VISIBLE);
            listViewCategory.setVisibility(View.GONE);
            storeAdapter.notifyDataSetChanged();
            isPurchase = true;
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToInvitePartner(){
        startActivity(new Intent(StoreActivity.this, InviteActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    public void goToMission(){
        //startActivity(new Intent(TstoreActivity.this, TmissionActivity.class));
        startActivity(new Intent(StoreActivity.this, MissionActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    public void setGoldCoin(final double gold, final int coin, final String type) {
        Applications.isCashpopPopup = false;
        final double nowGold = Applications.ePreference.getBalanceGpoint();
        final int nowCoin = Applications.ePreference.getValue(EPreference.N_TROPHY, 0);
        //final int nowCoin = Applications.preference.getValue(Preference.COIN, 0);
        if( nowGold != gold || nowCoin != coin){
            if( FileCacheFactory.getInstance().has(CommonUtil.cacheNameHistory)){
                FileCacheFactory.getInstance().get(CommonUtil.cacheNameHistory).clear();
            }
        }
        double reward = gold - nowGold;
        int reward_coin = coin - nowCoin;
        if( reward != 0 || reward_coin != 0){
            try {
                if (FileCacheFactory.getInstance().has(CommonUtil.cacheNameHistory)) {
                    FileCacheFactory.getInstance().get(CommonUtil.cacheNameHistory).clear();
                }
            }catch (Exception ignore){
                ignore.printStackTrace();
            }
        }
        if( type.equals("") || (reward == 0 && reward_coin == 0) || nowGold == 0) {
            try {
                goldRefresh(nowGold, gold);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if( type.equals("reward")) {
            if( reward > 0 || reward_coin > 0){
                HideLoadingProgress();
                try {
                    goldRefresh(nowGold, gold);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    goldRefresh(nowGold, gold);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Applications.ePreference.putBalanceGpoint(gold);
        //Applications.preference.put(Preference.COIN, coin);
    }

    public void goldRefresh(final double nowGold, final double refreshGold) throws Exception{
        if( refreshGold - nowGold > 0){
            final int dtime = 40;
            new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what){
                        case  0:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(StoreActivity.this, R.drawable.cash2_1));
                            this.sendEmptyMessageDelayed(msg.what+1, dtime);
                            break;
                        case 1:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(StoreActivity.this, R.drawable.cash2_2));
                            this.sendEmptyMessageDelayed(msg.what+1, dtime);
                            break;
                        case 2:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(StoreActivity.this, R.drawable.cash2_1));
                            this.sendEmptyMessageDelayed(msg.what+1, dtime);
                            break;
                        case 3:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(StoreActivity.this, R.drawable.cash2));
                            this.sendEmptyMessageDelayed(msg.what+1, dtime);
                            break;
                        case 4:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(StoreActivity.this, R.drawable.cash2_1));
                            this.sendEmptyMessageDelayed(msg.what+1, dtime);
                            break;
                        case 5:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(StoreActivity.this, R.drawable.cash2_2));
                            this.sendEmptyMessageDelayed(msg.what+1, dtime);
                            break;
                        case 6:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(StoreActivity.this, R.drawable.cash2_1));
                            this.sendEmptyMessageDelayed(msg.what+1, dtime);
                            break;
                        case 7:
                            iv_my_gpoint.setImageDrawable(ContextCompat.getDrawable(StoreActivity.this, R.drawable.cash2));
                            this.sendEmptyMessageDelayed(msg.what+1, dtime);
                            break;
                        case 8:
                            ValueAnimator va = ValueAnimator.ofInt((int) nowGold, (int) refreshGold);
                            va.setDuration(1000);
                            va.setInterpolator(AnimationUtils.loadInterpolator(StoreActivity.this, android.R.anim.decelerate_interpolator));
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
                                public void onAnimationStart(Animator animator) {}
                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    Toast toast = Toast.makeText(StoreActivity.this, getResources().getString(R.string.reward_gpoint_toast, ""+(int)(refreshGold-nowGold)), Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.BOTTOM, 0, CommonUtil.TOAST_YOFFSET);
                                    toast.show();
                                }
                                @Override
                                public void onAnimationCancel(Animator animator) {}
                                @Override
                                public void onAnimationRepeat(Animator animator) {}
                            });
                            break;
                    }
                }
            }.sendEmptyMessageDelayed(0, 200);
        }else {
            ValueAnimator va = ValueAnimator.ofInt((int) nowGold, (int) refreshGold);
            va.setDuration(1000);
            va.setInterpolator(AnimationUtils.loadInterpolator(StoreActivity.this, android.R.anim.decelerate_interpolator));
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

    @Override
    public void buyProc(JSONObject jobj, String h, String n, String a, String b) {

    }

    @Override
    public void buyNewProc(final JSONObject jobj, final String input1, final String input2, final String input3, final String input4) {
        try{

            String product = jobj.getString("product");

            cashPopDialog = new CashPopDialog(this);
            cashPopDialog.setCpTitle(product);
            String description = "";
            final JSONArray inputArr = new JSONArray(template.get(jobj.getInt("inputNo")).getString("text"));


            if( inputArr.getJSONObject(0) != null && !inputArr.getJSONObject(0).getString("key").equals("") && !inputArr.getJSONObject(0).getString("val").equals("")) {
                description += inputArr.getJSONObject(0).getString("key")+" : "+input1+"\n";
            }
            if( inputArr.getJSONObject(1) != null && !inputArr.getJSONObject(1).getString("key").equals("") && !inputArr.getJSONObject(1).getString("val").equals("")) {
                description += inputArr.getJSONObject(1).getString("key")+" : "+input2+"\n";
            }
            if( inputArr.getJSONObject(2) != null && !inputArr.getJSONObject(2).getString("key").equals("") && !inputArr.getJSONObject(2).getString("val").equals("")) {
                description += inputArr.getJSONObject(2).getString("key")+" : "+input3+"\n";
            }
            if( inputArr.getJSONObject(3) != null && !inputArr.getJSONObject(3).getString("key").equals("") && !inputArr.getJSONObject(3).getString("val").equals("")) {
                description += inputArr.getJSONObject(3).getString("key")+" : "+input4+"\n";
            }
            description += "\n"+template.get(jobj.getInt("confirmNo")).getString("text");
            cashPopDialog.setCpDesc(description);
            cashPopDialog.setCpBOkButton(getResources().getString(R.string.btn_yes), new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    requestCashGift(jobj, input1, input2, input3, input4);
                    storeNewPurchaseDialog.dismiss();
                    cashPopDialog.dismiss();
                }
            });
            cashPopDialog.setCpBCancelButton(getResources().getString(R.string.btn_modify), new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    cashPopDialog.dismiss();
                }
            });
            cashPopDialog.show();
        }catch (Exception ignore){
            ignore.printStackTrace();
        }
    }

    private void requestCashGift(final JSONObject jobj, final String input1, final String input2, final String input3, final String input4){
        try{
            ShowLoadingProgress();
            HashMap<String, String> map = new HashMap<>();
            map.put(CommonUtil.KEY_USERID, Applications.preference.getValue(Preference.USER_ID, ""));
            map.put(CommonUtil.KEY_ACTION, CommonUtil.ACTION_STORE_REQUEST);
            map.put(CommonUtil.KEY_STORE_ID, jobj.getString("id"));
            map.put(CommonUtil.KEY_INPUT1, input1);
            map.put(CommonUtil.KEY_INPUT2, input2);
            map.put(CommonUtil.KEY_INPUT3, input3);
            map.put(CommonUtil.KEY_INPUT4, input4);
            String param = APICrypto.getParam(this, map, CommonUtil.SHARED_KEY);
            requestAsyncTask(param, CommonUtil.ACTION_STORE_REQUEST);
        }catch (Exception ignore){
            ignore.printStackTrace();
        }
    }

    private void closePurchase(){
        tv_title.setText(getResources().getString(R.string.title_store));
        listViewStore.setVisibility(View.GONE);
        listViewCategory.setVisibility(View.VISIBLE);
        storeProductList.clear();
        isPurchase = false;
    }

    public void showErrorNetwork(final String param, final String action, final String type){
        System.out.println("action : "+action+", type : "+type+", param : "+param);
        HideLoadingProgress();
        if( networkErrorHash == null){
            networkErrorHash = new HashMap<>();
        }
        if( networkErrorHash.get(param+action+type) == null){
            NetworkErrorModel networkErrorModel = new NetworkErrorModel();
            networkErrorModel.setAction(action);
            networkErrorModel.setParam(param);
            networkErrorModel.setType(type);
            networkErrorHash.put(param+action+type, networkErrorModel);
        }

        if( networkDialog == null){
            networkDialog = new NetworkDialog(StoreActivity.this);
        }
        if( !networkDialog.isShowing()) {
            networkDialog.setCancelClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HideLoadingProgress();
                    networkErrorHash.clear();
                    networkDialog.dismiss();
                    ActivityCompat.finishAffinity(StoreActivity.this);
                }
            });
            networkDialog.setOkClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowLoadingProgress();
                    System.out.println(networkErrorHash.size());
                    synchronized (networkErrorHash) {
                        try {
                            for(String key : networkErrorHash.keySet()) {
                                NetworkErrorModel networkErrorModel = networkErrorHash.get(key);
                                System.out.println(String.format("action : %s,  키 : %s, 값 : %s", networkErrorModel.getAction(), key, networkErrorHash.get(key)));
                                requestAsyncTask(networkErrorModel.getParam(), networkErrorModel.getAction());
                                //networkErrorHash.remove(key);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }finally {
                            networkErrorHash.clear();
                        }
                    }
                    HideLoadingProgress();
                    networkDialog.dismiss();
                }
            });
            networkDialog.show();
        }
    }

    public void setLinkedGold(double linkedGold) {
        Applications.ePreference.putNLinkedGold(linkedGold);
    }


    public void setNormalGold(double normalGold) {
        Applications.ePreference.putBalanceGpoint(normalGold);
    }

    public synchronized void requestInfo(){
        ShowLoadingProgress();
        Applications.isMissionRefresh = false;
        HashMap<String, String> map = new HashMap<>();
        map.put(CommonUtil.KEY_USERID, Applications.preference.getValue(Preference.USER_ID,""));
        map.put(CommonUtil.KEY_ADID, Applications.preference.getValue(Preference.AD_ID,""));
        map.put(CommonUtil.KEY_DEVICE_TOKEN, Applications.preference.getValue(Preference.DEVICE_TOKEN,""));
        map.put(CommonUtil.KEY_PHONE_NM, Applications.preference.getValue(Preference.PHONE_NM, ""));
        int version = CommonUtil.getVersionCode(StoreActivity.this);
        map.put(CommonUtil.KEY_NAME, version+"");
        map.put(CommonUtil.KEY_ACTION, CommonUtil.ACTION_GET_INFO);
        String param = APICrypto.getParam(StoreActivity.this, map, CommonUtil.SHARED_KEY);
        requestAsyncTask(param, CommonUtil.ACTION_GET_INFO);
    }

    public void goHistory(){
        startActivity(new Intent(StoreActivity.this, HistoryActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
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
                Log.e(getClass().getName(), "AD fail code: "+i);
                if (i != AdInfoKey.AD_SUCCESS) {
                    tnkBanner.setVisibility(View.VISIBLE);
                    manplusBanner.setVisibility(View.GONE);
                    bannerAdView.loadAd(TnkSession.CPC, BannerAdType.LANDSCAPE);
                }
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

    private void manPlusInterstitialView(int p, int m, int s) {
        if (m_interView == null) {
            m_interView = new AdInterstitialView(StoreActivity.this);
            m_interView.setAdListener(new AdListener() {


                @Override
                public void onChargeableBannerType(View view, boolean b) {
                    handler.post(new Runnable() {
                        public void run() {
                            Log.i(getClass().getName(), (b) ? "no chargebale" : "chargeble");
                        }
                    });
                }

                @Override
                public void onFailedToReceive(View view, int i) {
                    final int errcode = i;
                    handler.post(new Runnable() {
                        public void run() {
                            showErrorMsg(errcode);
                        }

                    });
                }

                @Override
                public void onInterClose(View view) {
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                }

                @Override
                public void onAdClick(View view) {
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                }
            });
            m_interView.setViewStyle(AdInterstitialView.VIEWSTYLE.NONE);
			/*m_interView.setUserAge("22");
			m_interView.setUserGender("1");
			m_interView.setLoaction(true);
			m_interView.setAccount("test");
			m_interView.setEmail("test@mezzomediaco.kr");*/
        }
        m_interView.setAdViewCode(String.valueOf(p), String.valueOf(m), String.valueOf(s));
        m_interView.ShowInterstitialView();
    }

    private void showErrorMsg(int errorCode) {
        String log;
        switch (errorCode) {
            case AdInfoKey.AD_SUCCESS:
                log = "[ " + errorCode + " ] " + "광고 성공";
                break;
            case AdInfoKey.AD_ID_NO_AD:
                log = "[ " + errorCode + " ] " + "광고 소진";
                TnkSession.showInterstitialAd(StoreActivity.this);
                break;
            case AdInfoKey.NETWORK_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)네트워크";
                break;
            case AdInfoKey.AD_SERVER_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)서버";
                break;
            case AdInfoKey.AD_API_TYPE_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)API 형식 오류";
                break;
            case AdInfoKey.AD_APP_ID_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)ID 오류";
                break;
            case AdInfoKey.AD_WINDOW_ID_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)ID 오류";
                break;
            case AdInfoKey.AD_ID_BAD:
                log = "[ " + errorCode + " ] " + "(ERROR)ID 오류";
                break;
            case AdInfoKey.AD_CREATIVE_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)광고 생성 불가";
                break;
            case AdInfoKey.AD_ETC_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)예외 오류";
                break;
            case AdInfoKey.CREATIVE_FILE_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)파일 형식";
                break;
            case AdInfoKey.AD_INTERVAL:
                log = "[ " + errorCode + " ] " + "광고 요청 어뷰징";
                break;
            case AdInfoKey.AD_TIMEOUT:
                log = "[ " + errorCode + " ] " + "광고 API TIME OUT";
                break;
            case AdInfoKey.AD_ADCLICK:
                log = "[ " + errorCode + " ] " + "광고 클릭";
                break;
            default:
                log = "[ " + errorCode + " ] " + "etc";
                break;
        }
        Log.e(getClass().getName(), log);
    }
}
