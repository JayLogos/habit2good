package kr.co.gubed.habit2good.gpoint.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mapps.android.share.AdInfoKey;
import com.mapps.android.view.AdInterstitialView;
import com.mz.common.listener.AdListener;
import com.tnkfactory.ad.TnkAdListener;
import com.tnkfactory.ad.TnkSession;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import kr.co.gubed.habit2good.R;
import kr.co.gubed.habit2good.gpoint.adapter.JObjListAdapter_history;
import kr.co.gubed.habit2good.gpoint.filecache.ByteProviderUtil;
import kr.co.gubed.habit2good.gpoint.filecache.FileCache;
import kr.co.gubed.habit2good.gpoint.filecache.FileCacheFactory;
import kr.co.gubed.habit2good.gpoint.listener.AsyncTaskCompleteListener;
import kr.co.gubed.habit2good.gpoint.util.APICrypto;
import kr.co.gubed.habit2good.gpoint.util.Applications;
import kr.co.gubed.habit2good.gpoint.util.AsyncHTTPPost;
import kr.co.gubed.habit2good.gpoint.util.CommonUtil;
import kr.co.gubed.habit2good.gpoint.util.EPreference;
import kr.co.gubed.habit2good.gpoint.util.Preference;
import kr.co.gubed.habit2good.gpoint.view.CashPopDialog;
import kr.co.gubed.habit2good.gpoint.view.LoadingDialog;
import kr.co.gubed.habit2good.gpoint.view.NetworkDialog;

public class HistoryActivity extends Activity implements AsyncTaskCompleteListener<String>, View.OnClickListener{

    private String TAG = this.getClass().toString();

    private Button btn_back;
    private Button btn_info;

    private ScrollView listLayer;
    private TextView tv_no_history;
    private ListView listView;
    private TextView tv_balance;

    private LinearLayout list_linked_gpoint;
    private TextView tv_linked_gpoint;

    private LinearLayout list_normal_gpoint;
    private TextView tv_total;

    private LinearLayout list_store;
    private TextView tv_purchase;

    private TextView tv_trophy;

    private LinearLayout listDetailLayer;
    private LinearLayout listDetailClose;
    private TextView tv_detail_title;

    private ArrayList<JSONObject> linkedList;
    private ArrayList<JSONObject> rewardList;
    private ArrayList<JSONObject> storeList;
    private JObjListAdapter_history adaptor;

    private LoadingDialog loadingDialog;
    private CashPopDialog cashPopDialog;
    private NetworkDialog networkDialog;

    private Applications applications;
    private Tracker tracker;

    private String analiticsCategory = "/my_cash_history";

    private boolean isDetail = false;
    View footerView;

    private FileCache fileCache;

    private LinearLayout layer_trophy;

    private TextView tv_desc;

    private AdInterstitialView m_interView = null;
    private Handler handler = new Handler();

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        fileCache = getFileCache();

        footerView = ((LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_history, null, false);
        applications = (Applications)getApplication();
        tracker = applications.getDefaultTracker();
        tracker.setScreenName(analiticsCategory);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);
        btn_info = (Button)findViewById(R.id.btn_info);
        btn_info.setOnClickListener(this);

        tv_balance = (TextView)findViewById(R.id.tv_balance);

        listLayer = (ScrollView)findViewById(R.id.listLayer);

        list_linked_gpoint = (LinearLayout)findViewById(R.id.list_linked);
        tv_linked_gpoint = (TextView)findViewById(R.id.tv_linked_gold);
        list_linked_gpoint.setOnClickListener(this);

        list_normal_gpoint = (LinearLayout)findViewById(R.id.list_normal);
        tv_total = (TextView)findViewById(R.id.tv_total);
        list_normal_gpoint.setOnClickListener(this);

        list_store = (LinearLayout)findViewById(R.id.list_store);
        tv_purchase = (TextView)findViewById(R.id.tv_purchase);
        list_store.setOnClickListener(this);

        tv_trophy = findViewById(R.id.tv_trophy);
        Integer trophy = Applications.ePreference.getValue(EPreference.N_TROPHY, 0);
        tv_trophy.setText(String.format("%,d", trophy));

        listDetailLayer = (LinearLayout)findViewById(R.id.listDetailLayer);
        listDetailClose = (LinearLayout)findViewById(R.id.listDetailClose);
        listDetailClose.setOnClickListener(this);
        tv_detail_title = (TextView)findViewById(R.id.tv_detail_title);

        listView = (ListView) findViewById(R.id.listView);
        linkedList = new ArrayList<>();
        rewardList = new ArrayList<>();
        storeList = new ArrayList<>();
        listView.setFooterDividersEnabled(true);

        tv_no_history = (TextView) findViewById(R.id.tv_no_history);

        loadingDialog = new LoadingDialog(this);
        cashPopDialog = new CashPopDialog(this);
        networkDialog = new NetworkDialog(this);

        layer_trophy = (LinearLayout)findViewById(R.id.layer_trophy);

        tv_desc = (TextView)findViewById(R.id.tv_desc);
        tv_desc.setVisibility(View.GONE);
        getInfo();
        //admobInterstitialReqeust();

        if( !Applications.preference.getValue(Preference.HISTORY_GUIDE, false)){
            Applications.preference.put(Preference.HISTORY_GUIDE, true);
        }
    }

    public void ShowLoadingProgress() {
        //show loading
        try {
            if( loadingDialog == null){
                loadingDialog = new LoadingDialog(HistoryActivity.this);
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    try{
                        loadingDialog.show();
                    }catch (Exception ignore){}
                }
            });
        } catch (Exception ignore) {

        }
    }

    public void HideLoadingProgress() {
        //hide loading
        try {
            loadingDialog.dismiss();
        } catch (Exception ignore) {

        }
    }

    public FileCache getFileCache(){
        if( fileCache == null){
            FileCacheFactory.initialize(this);
            if( !FileCacheFactory.getInstance().has(CommonUtil.cacheNameHistory)){
                FileCacheFactory.getInstance().create(CommonUtil.cacheNameHistory, 1024);
            }
            fileCache = FileCacheFactory.getInstance().get(CommonUtil.cacheNameHistory);
        }
        return fileCache;
    }

    public void getInfo (){
        try{
            boolean isCache = false;
            String cacheRst="";
            if( getFileCache().get(CommonUtil.cacheNameHistory) != null){
                try{
                    InputStream is = getFileCache().get(CommonUtil.cacheNameHistory).getInputStream();
                    int size = is.available();
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    is.close();
                    cacheRst = new String(buffer);
                    Log.e(TAG, cacheRst);
                    JSONObject job = new JSONObject(cacheRst);
                    isCache = System.currentTimeMillis() - Long.parseLong(job.getString(CommonUtil.KEY_TIMESTAMP)) < 60 * 60 * 1000 * 2;
                }catch (Exception e){
                    isCache = false;
                }
            }isCache = false;
            if( isCache) {
                try{
                    JSONObject job = new JSONObject(cacheRst);
                    JSONObject jo = new JSONObject(job.getString(CommonUtil.KEY_RST));
                    double nowGold = Applications.ePreference.getTotalGpoint();
                    int nowTrophy = Applications.ePreference.getValue(EPreference.N_TROPHY, 0);
                    String gold = jo.getString(CommonUtil.RESULT_BUDGET);
                    String trophy = jo.getString(CommonUtil.RESULT_H2G_TROPHY);
                    if( !gold.equals(""+nowGold) || !trophy.equals(""+nowTrophy)){
                        requestInfo();
                    }else{
                        setHistory(jo);
                    }
                }catch (Exception ignore){}
            }else{
                requestInfo();
            }
        }catch (Exception e){

        }
    }

    public void requestInfo(){
        ShowLoadingProgress();
        HashMap<String, String> map = new HashMap<>();
        map.put(CommonUtil.KEY_USERID, Applications.preference.getValue(Preference.USER_ID,""));
        map.put(CommonUtil.KEY_ACTION, CommonUtil.ACTION_GET_HISTORYLIST);
        String param = APICrypto.getParam(this, map, CommonUtil.SHARED_KEY);
        requestAsyncTask(param, CommonUtil.ACTION_GET_HISTORYLIST);
    }

    public void ShowInfo(){
        if( cashPopDialog == null){
            cashPopDialog = new CashPopDialog(this);
        }
        cashPopDialog.setCpTitle(this.getResources().getString(R.string.history_info_title));
        cashPopDialog.setCpDesc(this.getResources().getString(R.string.history_info_detail));
        cashPopDialog.setCpOkButton(this.getResources().getString(R.string.ok), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cashPopDialog.dismiss();
            }
        });
        cashPopDialog.setCpCancelButton(this.getResources().getString(R.string.support), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cashPopDialog.dismiss();
                CommonUtil.showSupport(HistoryActivity.this, true);
            }
        });
        cashPopDialog.show();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        if( isDetail){
            listLayer.setVisibility(View.VISIBLE);
            listDetailLayer.setVisibility(View.GONE);
            tv_no_history.setVisibility(View.GONE);
            isDetail = false;
            layer_trophy.setVisibility(View.VISIBLE);
            tv_desc.setVisibility(View.GONE);
        }else {
            try {
                if( loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }
                if( cashPopDialog != null && cashPopDialog.isShowing()) {
                    cashPopDialog.dismiss();
                    cashPopDialog = null;
                }
                if( networkDialog != null && networkDialog.isShowing()) {
                    networkDialog.dismiss();
                    networkDialog = null;
                }
            } catch (Exception ignore) {
            }
            Applications.mobAdCnt++;
            if( Applications.mobAdCnt > 10){
                Applications.mobAdCnt = 0;
            }
            /*TnkSession.showInterstitialAd(this, 1000, new TnkAdListener() {
                @Override
                public void onClose(int i) {
                    aBack();
                }

                @Override
                public void onShow() {

                }

                @Override
                public void onFailure(int i) {
                    Log.e(getClass().getName(), "TNK interstitial showing fail: "+i);
                    aBack();
                }

                @Override
                public void onLoad() {

                }
            });*/
            manPlusInterstitialView(1413, 31780, 803854);

            //showExitInterstitial();
            /*if( Applications.mInterstitialAd.isLoaded() && Applications.mobAdCnt%2==1 ){
                Applications.mInterstitialAd.show();
                aBack();
            }else{
                aBack();
            }*/
        }
    }

    public void aBack(){
        setResult(CommonUtil.ACTIVITY_RESULT_HISTORY);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

/* ADMOB
    public void admobInterstitialReqeust() {
        if( Applications.mInterstitialAd == null) {
            Applications.mInterstitialAd = new InterstitialAd(getApplicationContext());
            Applications.mInterstitialAd.setAdUnitId(getApplicationContext().getResources().getString(R.string.interstitial_ad_unit_id_test));
        }
        Applications.mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                try {
                    aBack();
                }catch (Exception ignore){

                }
            }
        });
        requestNewInterstitial();
    }

    private void requestNewInterstitial() {
        if( !Applications.mInterstitialAd.isLoaded()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            Applications.mInterstitialAd.loadAd(adRequest);
        }
    }
*/

    @Override
    protected void onDestroy() {
        try{
            if( loadingDialog != null && loadingDialog.isShowing()){
                loadingDialog.dismiss();
                loadingDialog = null;
            }
            if( cashPopDialog != null && cashPopDialog.isShowing()){
                cashPopDialog.dismiss();
                cashPopDialog = null;
            }
            if( networkDialog != null && networkDialog.isShowing()){
                networkDialog.dismiss();
                networkDialog = null;
            }
        }catch (Exception ignore){
        }
        super.onDestroy();
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
            if( error != null && error.isEmpty() && action != null && !action.isEmpty()){
                if( action.equals(CommonUtil.ACTION_GET_HISTORYLIST)){
                    setHistory(jo);
                    JSONObject cacheRst = new JSONObject();
                    cacheRst.put(CommonUtil.KEY_TIMESTAMP, System.currentTimeMillis());
                    cacheRst.put(CommonUtil.KEY_RST, rst);
                    getFileCache().put(CommonUtil.cacheNameHistory, ByteProviderUtil.create(cacheRst.toString()));

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        HideLoadingProgress();
    }

    public void setHistory(JSONObject jo){
        try {
            Applications.isHomeRefresh = false;
            double totalGpoint = Applications.ePreference.getTotalGpoint();
            int nowTrophy = Applications.ePreference.getValue(EPreference.N_TROPHY, 0);
            String total = jo.getString(CommonUtil.RESULT_BUDGET);
            if( !total.equals(""+total)){
                tv_balance.setText(CommonUtil.setComma(total, true, false));
                Applications.ePreference.putTotalGpoint(Double.parseDouble(total));
                Applications.isHomeRefresh = true;
            }else{
                tv_total.setText(CommonUtil.setComma(totalGpoint+"", true, false));
            }
            /*String trophy = jo.getString(CommonUtil.RESULT_H2G_TROPHY);
            if( !trophy.equals(""+nowTrophy)){
                Applications.ePreference.put(EPreference.N_TROPHY, trophy);
                Applications.isHomeRefresh = true;
            } else {
                tv_trophy.setText(CommonUtil.setComma(trophy+"", true, false));
            }*/
            tv_trophy.setText(CommonUtil.setComma(nowTrophy+"", true, false));

            Applications.ePreference.putTotalGpoint(Double.parseDouble(total));
            String linked_gold = jo.getString(CommonUtil.RESULT_LINKED_GOLD);
            String balance = jo.getString(CommonUtil.RESULT_BALANCE_GPOINT);
            tv_balance.setText(CommonUtil.setComma(balance, true, false));
            String purchase = jo.getString(CommonUtil.RESULT_STORE_GOLD);
            tv_purchase.setText(CommonUtil.setComma(purchase, true, true));

            JSONArray rewards = jo.getJSONArray("rewards");
            JSONArray purchases = jo.getJSONArray("purchases");

            if( rewards != null && rewards.length() > 0) {
                rewardList.clear();
                for (int i = 0; i < rewards.length(); i++) {
                    JSONObject item = rewards.getJSONObject(i);
                    rewardList.add(item);
                }
            }
            if( purchases != null && purchases.length() > 0) {
                storeList.clear();
                for (int i = 0; i < purchases.length(); i++) {
                    JSONObject item = purchases.getJSONObject(i);
                    storeList.add(item);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskError(String param, String action, String result) {
        HideLoadingProgress();
        try{
            Log.e(TAG, action);
            if( action.equals(CommonUtil.ACTION_GET_HISTORYLIST)){
                showErrorNetwork(param, action);
            }
        }catch (Exception ignore){
        }
    }

    public void showErrorNetwork(final String param, final String action){
        if( networkDialog == null){
            networkDialog = new NetworkDialog(HistoryActivity.this);
        }
        if( !networkDialog.isShowing()) {
            networkDialog.setCancelClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HideLoadingProgress();
                    networkDialog.dismiss();
                    ActivityCompat.finishAffinity(HistoryActivity.this);
                }
            });
            networkDialog.setOkClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowLoadingProgress();
                    requestAsyncTask(param, action);
                    networkDialog.dismiss();
                }
            });
            networkDialog.show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_back:
                onBackPressed();
                break;
            case R.id.btn_info:
                CommonUtil.showSupport(HistoryActivity.this, true);
                tracker.send(new HitBuilders.EventBuilder().setCategory(analiticsCategory).setAction("/history_guide_button_click").build());
                break;
            case R.id.listDetailClose:
                listLayer.setVisibility(View.VISIBLE);
                listDetailLayer.setVisibility(View.GONE);
                tv_no_history.setVisibility(View.GONE);
                isDetail = false;
                layer_trophy.setVisibility(View.VISIBLE);
                tv_desc.setVisibility(View.GONE);
                break;
            case R.id.list_linked:
                isDetail = true;
                listLayer.setVisibility(View.GONE);
                listDetailLayer.setVisibility(View.VISIBLE);
                tv_detail_title.setText(getResources().getString(R.string.list_linked));
                layer_trophy.setVisibility(View.GONE);
                if( !linkedList.isEmpty() && linkedList.size() > 0) {
                    adaptor = new JObjListAdapter_history(this, R.layout.row_history, linkedList);
                    listView.removeFooterView(footerView);
                    footerView = ((LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_history_oneday, null, false);
                    listView.addFooterView(footerView);
                    listView.setAdapter(adaptor);
                    tv_no_history.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }else{
                    tv_no_history.setText(this.getResources().getString(R.string.no_history));
                    tv_no_history.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                }
                tv_desc.setVisibility(View.VISIBLE);
                break;
            case R.id.list_normal:
                isDetail = true;
                listLayer.setVisibility(View.GONE);
                listDetailLayer.setVisibility(View.VISIBLE);
                tv_detail_title.setText(getResources().getString(R.string.list_total));
                layer_trophy.setVisibility(View.GONE);
                if( !rewardList.isEmpty() && rewardList.size() > 0) {
                    adaptor = new JObjListAdapter_history(this, R.layout.row_history, rewardList);
                    listView.removeFooterView(footerView);
                    footerView = ((LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_history, null, false);
                    listView.addFooterView(footerView);
                    listView.setAdapter(adaptor);
                    tv_no_history.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }else{
                    tv_no_history.setText(this.getResources().getString(R.string.no_history));
                    tv_no_history.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                }
                tv_desc.setVisibility(View.VISIBLE);
                break;
            case R.id.list_store:
                isDetail = true;
                listLayer.setVisibility(View.GONE);
                listDetailLayer.setVisibility(View.VISIBLE);
                tv_detail_title.setText(getResources().getString(R.string.list_store));
                layer_trophy.setVisibility(View.GONE);
                if( !storeList.isEmpty() && storeList.size() > 0) {
                    adaptor = new JObjListAdapter_history(this, R.layout.row_history, storeList);
                    listView.removeFooterView(footerView);
                    footerView = ((LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_history, null, false);
                    listView.addFooterView(footerView);
                    listView.setAdapter(adaptor);
                    tv_no_history.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }else{
                    tv_no_history.setText(this.getResources().getString(R.string.no_history_use));
                    tv_no_history.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                }
                tv_desc.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void requestAsyncTask(String param, String action) {
        if (Applications.getCountry(this).equals("KR") && !Applications.isRoaming(this)) {
            new AsyncHTTPPost(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, CommonUtil.SERVER_URL, param, action);
        } else {
            new AsyncHTTPPost(this).execute(CommonUtil.SERVER_URL, param, action);
        }
    }

    public void goToMission() {
        Intent intent = new Intent(HistoryActivity.this, MissionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private void manPlusInterstitialView(int p, int m, int s) {
        if (m_interView == null) {
            m_interView = new AdInterstitialView(HistoryActivity.this);
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
                }

                @Override
                public void onAdClick(View view) {
                    finish();
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
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {
                        finish();
                    }

                    @Override
                    public void onShow() {
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                        finish();
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(this);
                break;
            case AdInfoKey.NETWORK_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)네트워크";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {
                        finish();
                    }

                    @Override
                    public void onShow() {
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                        finish();
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(this);
                break;
            case AdInfoKey.AD_SERVER_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)서버";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {
                        finish();
                    }

                    @Override
                    public void onShow() {
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                        finish();
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(this);
                break;
            case AdInfoKey.AD_API_TYPE_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)API 형식 오류";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {
                        finish();
                    }

                    @Override
                    public void onShow() {
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                        finish();
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(this);
                break;
            case AdInfoKey.AD_APP_ID_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)APP_ID 오류";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {
                        finish();
                    }

                    @Override
                    public void onShow() {
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                        finish();
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(this);
                break;
            case AdInfoKey.AD_WINDOW_ID_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)WINDOW_ID 오류";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {
                        finish();
                    }

                    @Override
                    public void onShow() {
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                        finish();
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(this);
                break;
            case AdInfoKey.AD_ID_BAD:
                log = "[ " + errorCode + " ] " + "(ERROR)ID 오류";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {
                        finish();
                    }

                    @Override
                    public void onShow() {
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                        finish();
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(this);
                break;
            case AdInfoKey.AD_CREATIVE_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)광고 생성 불가";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {
                        finish();
                    }

                    @Override
                    public void onShow() {
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                        finish();
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(this);
                break;
            case AdInfoKey.AD_ETC_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)예외 오류";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {
                        finish();
                    }

                    @Override
                    public void onShow() {
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                        finish();
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(this);
                break;
            case AdInfoKey.CREATIVE_FILE_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)파일 형식";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {
                        finish();
                    }

                    @Override
                    public void onShow() {
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                        finish();
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(this);
                break;
            case AdInfoKey.AD_INTERVAL:
                log = "[ " + errorCode + " ] " + "광고 요청 어뷰징";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {
                        finish();
                    }

                    @Override
                    public void onShow() {
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                        finish();
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(this);
                break;
            case AdInfoKey.AD_TIMEOUT:
                log = "[ " + errorCode + " ] " + "광고 API TIME OUT";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {
                        finish();
                    }

                    @Override
                    public void onShow() {
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                        finish();
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(this);
                break;
            case AdInfoKey.NOT_SUPPORT_VERSION:
                log = "[ " + errorCode + " ] " + "광고 NOT SUPPORT VERSION";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {
                        finish();
                    }

                    @Override
                    public void onShow() {
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                        finish();
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(this);
                break;
            case AdInfoKey.NOT_UNZIP_TIMEOUT:
                log = "[ " + errorCode + " ] " + "광고 NOT UNZIP TIMEOUT";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {
                        finish();
                    }

                    @Override
                    public void onShow() {
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                        finish();
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(this);
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
