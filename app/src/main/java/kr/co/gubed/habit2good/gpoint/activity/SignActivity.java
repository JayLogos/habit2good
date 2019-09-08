package kr.co.gubed.habit2good.gpoint.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tnkfactory.ad.TnkSession;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import kr.co.gubed.habit2good.MainActivity;
import kr.co.gubed.habit2good.R;
import kr.co.gubed.habit2good.gpoint.filecache.FileCache;
import kr.co.gubed.habit2good.gpoint.filecache.FileCacheFactory;
import kr.co.gubed.habit2good.gpoint.listener.AdidListener;
import kr.co.gubed.habit2good.gpoint.listener.AsyncTaskCompleteListener;
import kr.co.gubed.habit2good.gpoint.util.APICrypto;
import kr.co.gubed.habit2good.gpoint.util.Applications;
import kr.co.gubed.habit2good.gpoint.util.AsyncHTTPPost;
import kr.co.gubed.habit2good.gpoint.util.CommonUtil;
import kr.co.gubed.habit2good.gpoint.util.EPreference;
import kr.co.gubed.habit2good.gpoint.util.EmulatorDetector;
import kr.co.gubed.habit2good.gpoint.util.Preference;
import kr.co.gubed.habit2good.gpoint.view.CashPopDialog;
import kr.co.gubed.habit2good.gpoint.view.LoadingDialog;
import kr.co.gubed.habit2good.gpoint.view.NetworkDialog;

public class SignActivity extends Activity implements View.OnClickListener, AdidListener, AsyncTaskCompleteListener<String> {

    private String TAG = this.getClass().toString();

    private TextView tv_login;
    private ImageView loadingIv;
    private AnimationDrawable loadingViewAnim;
    private TextView tv_init;
    private Button btn_join;
    private LinearLayout btns;

    private Applications applications;
    private Tracker tracker;
    private FileCache fileCache;

    private LoadingDialog loadingDialog;
    private CashPopDialog cashPopDialog;
    private NetworkDialog networkDialog;

    private String analiticsCategory = "/gpoint_signing";
    private String btn_type = "";

    private boolean isSplash = false;
    private boolean isVersion = false;
    private boolean isVersionUp = false;

    private static int request_permission = 0;
    private static boolean isFirst = false;

    private boolean isGoMain = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        if( !isFirst) {
            Log.e(TAG,"onCreate2");
            isFirst = true;
            String[] permissionArr = getPermissionArray();
            request_permission++;
            if( permissionArr.length > 0){
                ActivityCompat.requestPermissions(this, permissionArr, request_permission);
                //Log.i(TAG,"asdasd");
            }else{
                firstInit();
            }
        }else{
            firstInit();
        }
    }

    @Override
    public void finish() {
        super.finish();
        if( isGoMain){
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        isFirst = false;
    }

    public void firstInit(){
        //FirebaseApp.initializeApp(SignActivity.this);

        TnkSession.applicationStarted(this);

        cashPopDialog = new CashPopDialog(SignActivity.this);
        networkDialog = new NetworkDialog(SignActivity.this);

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        long subscriberId = 0;
        // 폰 넘버 구하기. 특별한 용도로 사용되고 있진 않음.
        try {

            if (tm.getSubscriberId() != null && Pattern.matches("^[0-9]+$", tm.getSubscriberId())) {
                subscriberId = Long.parseLong(tm.getSubscriberId()) - 402;
            }
        }catch (Exception ignore){
            String[] permissionArr = getPermissionArray();
            request_permission++;
            if( permissionArr.length > 0 || isPermissionNotShow()){
                settingPermission();
            }
            return;
        }
        Log.i(TAG,""+subscriberId);
        if( (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT && subscriberId == 0) || subscriberId == 0){
            //No USIM or subscriberId is null or subscriberId is not number.
            cashPopDialog.setCpTitle(this.getResources().getString(R.string.usim_title));
            cashPopDialog.setCpDesc(this.getResources().getString(R.string.usim_detail));
            cashPopDialog.setCpOkButton(this.getResources().getString(R.string.ok), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.finishAffinity(SignActivity.this);
                }
            });
            cashPopDialog.setCpCancel(false);
            cashPopDialog.show();
            return;
        }else{
            Applications.preference.put(Preference.AD_ID, Long.toString(subscriberId));
        }

        fileCache = getFileCache();

//        if( Applications.preference.getValue(Preference.DEVICE_TOKEN,"").equals("")){
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    MyFirebaseInstanceIDService mfs = new MyFirebaseInstanceIDService();
//                    mfs.onTokenRefresh();
//                }
//            });
//        }

        Log.i(TAG, "device token : "+Applications.preference.getValue(Preference.DEVICE_TOKEN,""));
        int version = CommonUtil.getVersionCode(this);
        if( version > Applications.preference.getVersionCode(version)){
            isVersionUp = true;
            Applications.preference.versionUp();
            getFileCache().clear();
            getFileNoticeCache().clear();
            getFileInviteCache().clear();
        }

        Applications.preference.setVersionCode(version);
        if( !Applications.getCountry(this).equals(Applications.preference.getValue(Preference.COUNTRY_CODE, Applications.getCountry(this)))){
            getFileCache().clear();
            getFileNoticeCache().clear();
            getFileInviteCache().clear();
        }

        Applications.preference.put(Preference.COUNTRY_CODE, Applications.getCountry(this));

        ShowLoadingProgress();
        EmulatorDetector.with(this)
                .setCheckTelephony(true)
                .addPackageName("com.bluestacks")
                .setDebug(false)
                .detect(new EmulatorDetector.OnEmulatorDetectorListener() {
                    @Override
                    public void onResult(boolean isEmulator) {
                        HideLoadingProgress();
                        Log.e(TAG, "onResult = "+isEmulator);
                        if( isEmulator) {
                            SignActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //No USIM or subscriberId is null or subscriberId is not number.
                                    cashPopDialog = new CashPopDialog(SignActivity.this);
//                                        cashPopDialog.setCpTitle(SignActivity.this.getResources().getString(R.string.usim_title));
                                    cashPopDialog.setCpDesc(SignActivity.this.getResources().getString(R.string.rooting));
                                    cashPopDialog.setCpOkButton(SignActivity.this.getResources().getString(R.string.ok), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            ActivityCompat.finishAffinity(SignActivity.this);
                                        }
                                    });
                                    cashPopDialog.setCpCancel(false);
                                    cashPopDialog.show();
                                }
                            });
                            Applications.preference.put(Preference.EMULATE_CHK, false);
                        } else {
                            Applications.preference.put(Preference.EMULATE_CHK, true);
                            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if( ContextCompat.checkSelfPermission(SignActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED ||
                                        ContextCompat.checkSelfPermission(SignActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                                    if( shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){

                                    }
                                    requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                                }else{
                                    SignActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            init();
                                        }
                                    });

                                }
                            }else{
                                SignActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        init();
                                    }
                                });
                            }
                        }
                    }
                });
    }

    public FileCache getFileCache(){
        if( fileCache == null){
            FileCacheFactory.initialize(this);
            if( !FileCacheFactory.getInstance().has(CommonUtil.cacheName)){
                FileCacheFactory.getInstance().create(CommonUtil.cacheName, 1024);
            }
            fileCache = FileCacheFactory.getInstance().get(CommonUtil.cacheName);
        }
        return fileCache;
    }

    public FileCache getFileNoticeCache(){
        if( fileCache == null){
            FileCacheFactory.initialize(this);
            if( !FileCacheFactory.getInstance().has(CommonUtil.cacheNameNotice)){
                FileCacheFactory.getInstance().create(CommonUtil.cacheNameNotice, 1024);
            }
            fileCache = FileCacheFactory.getInstance().get(CommonUtil.cacheNameNotice);
        }
        return fileCache;
    }

    public FileCache getFileInviteCache(){
        if( fileCache == null){
            FileCacheFactory.initialize(this);
            if( !FileCacheFactory.getInstance().has(CommonUtil.cacheNameInvite)){
                FileCacheFactory.getInstance().create(CommonUtil.cacheNameInvite, 1024);
            }
            fileCache = FileCacheFactory.getInstance().get(CommonUtil.cacheNameInvite);
        }
        return fileCache;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG,"onActivityResult");
        if( requestCode == request_permission){
            String[] permissionArr = getPermissionArray();
            request_permission++;
            if( permissionArr.length > 0){
                //ActivityCompat.requestPermissions(this, permissionArr, request_permission);
                settingPermission();
            }else{
                if( isPermissionNotShow()){
                    settingPermission();
                }else{
                    firstInit();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e(TAG,"onRequestPermissionsResult : 1 - "+requestCode+" | "+request_permission);
        if( requestCode == request_permission) {
            Log.e(TAG,"onRequestPermissionsResult : 2 - "+requestCode+" | "+request_permission);
            boolean isPermission = false;
            boolean isGoPermission = false;
            for(int i=0;i<permissions.length;i++){
                Log.e(TAG, "permissions : "+permissions[i]);
            }
            for(int i=0;i<grantResults.length;i++){
                Log.e(TAG, "granted : "+grantResults[i]);
                if( grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    isPermission = true;
                    if( ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])){
                        isGoPermission = true;
                    }
                }
            }

            if( isPermission || isGoPermission){
                settingPermission();
            }else{
                firstInit();
            }
            /*
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                firstInit();
            }else{
                finish();
            }
            */
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void init(){
        loadingDialog = new LoadingDialog(SignActivity.this);

        applications = (Applications)this.getApplication();

        tracker = applications.getDefaultTracker();
        tracker.setScreenName(analiticsCategory);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        loadingIv = (ImageView)findViewById(R.id.loadingIv);
        tv_init = (TextView)findViewById(R.id.tv_init);

        Random generator = new Random();            // random 변수의 역할?
        int ran = generator.nextInt(2);
        Log.i(TAG, "ran = "+ran);
        if( ran == 0){
            tv_init.setText(this.getResources().getString(R.string.init1));
        }else{
            tv_init.setText(this.getResources().getString(R.string.init2));
        }

        tv_login = (TextView)findViewById(R.id.tv_login);
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            tv_login.setText(Html.fromHtml("<u>"+this.getResources().getString(R.string.login_tv).replaceAll("\n", "<br>")+"</u>", Html.FROM_HTML_MODE_LEGACY));
        }else{
            tv_login.setText(Html.fromHtml("<u>"+this.getResources().getString(R.string.login_tv).replaceAll("\n", "<br>")+"</u>"));
        }

        tv_login.setOnClickListener(this);
        btn_join = (Button)findViewById(R.id.btn_join);
        btn_join.setOnClickListener(this);

        btns = (LinearLayout)findViewById(R.id.btns);

        versionChk();

        loadingIv.setVisibility(View.VISIBLE);
        tv_init.setVisibility(View.VISIBLE);
        //tv_init.setVisibility(View.GONE);

        loadingViewAnim = (AnimationDrawable) loadingIv.getBackground();
        loadingViewAnim.start();

        Applications.getAdid(this, this);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                isSplash = true;
                if( !Applications.preference.getValue(Preference.USER_ID, "").equals("")) {
                    if( isVersion){
                        GoMain();
                    }
                }else{
                    SignActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if( isVersion && !Applications.preference.getValue(Preference.AD_ID, "").equals("")) {
                                complete();
                            }
                        }
                    });
                }
            }
        };
        Timer timer = new Timer();
        long splashDelay = 2000;
        timer.schedule(task, splashDelay);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        try{
            if( loadingDialog != null && loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
            if( cashPopDialog != null && cashPopDialog.isShowing()){
                cashPopDialog.dismiss();
            }
            if( networkDialog != null && networkDialog.isShowing()){
                networkDialog.dismiss();
            }
        }catch (Exception ignore){
        }
        ActivityCompat.finishAffinity(SignActivity.this);
    }

    @Override
    protected void onDestroy() {
        try{
            if( loadingDialog != null && loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
            if( cashPopDialog != null && cashPopDialog.isShowing()){
                cashPopDialog.dismiss();
            }
            if( networkDialog != null && networkDialog.isShowing()){
                networkDialog.dismiss();
            }
        }catch (Exception ignore){
        }
        super.onDestroy();
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
//                                cashPopDialog.dismiss();
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
            }else{
                isVersion = true;
                if( Applications.preference.getValue(Preference.USER_ID, "").equals("")){
                    complete();
                }else{
                    if( isSplash){
                        GoMain();
                    }
                }
            }
        }
    }

    public void Login() {
        btn_type = "login";
        HashMap<String, String> map = new HashMap<>();
        map.put(CommonUtil.KEY_ACTION, CommonUtil.ACTION_LOGIN);
        map.put(CommonUtil.KEY_ADID, Applications.preference.getValue(Preference.AD_ID, ""));
        map.put(CommonUtil.KEY_DEVICE_TOKEN, Applications.preference.getValue(Preference.DEVICE_TOKEN,""));
        String param = APICrypto.getParam(this, map, CommonUtil.SHARED_KEY);
        requestAsyncTask(param, CommonUtil.ACTION_LOGIN);
        ShowLoadingProgress();
    }

    public void Join() {
        btn_type = "join";
        HashMap<String, String> map = new HashMap<>();
        map.put(CommonUtil.KEY_ACTION, CommonUtil.ACTION_LOGIN);
        map.put(CommonUtil.KEY_ADID, Applications.preference.getValue(Preference.AD_ID, ""));
        map.put(CommonUtil.KEY_DEVICE_TOKEN, Applications.preference.getValue(Preference.DEVICE_TOKEN,""));
        String param = APICrypto.getParam(this, map, CommonUtil.SHARED_KEY);
        requestAsyncTask(param, CommonUtil.ACTION_LOGIN);
        ShowLoadingProgress();
    }

    public void GoMain() {
        if( isVersionUp){
            Login();
        }else{
            isGoMain = true;
            Intent intent = new Intent(SignActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    public void ShowLoadingProgress() {
        //show loading
        try {
            if( loadingDialog == null){
                loadingDialog = new LoadingDialog(SignActivity.this);
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

    @Override
    public void onTaskComplete(String result) {
        if( result == null){
            cashPopDialog = new CashPopDialog(this);
            cashPopDialog.setCpTitle(this.getResources().getString(R.string.login_error));
            cashPopDialog.setCpDesc(this.getResources().getString(R.string.login_error_desc));
            cashPopDialog.setCpOkButton(this.getResources().getString(R.string.ok), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cashPopDialog.dismiss();
                }
            });
            cashPopDialog.show();
            HideLoadingProgress();
        }else{
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
                    if( action.equals(CommonUtil.ACTION_LOGIN)){
                        String uid = jo.getString(CommonUtil.RESULT_USERID);
                        String cpid = jo.getString(CommonUtil.RESULT_CPID);
                        String invite = jo.getString(CommonUtil.RESULT_INVITE);
                        Integer trophy = Integer.parseInt(jo.getString(CommonUtil.RESULT_H2G_TROPHY));
                        String p1Timer = jo.getString(CommonUtil.RESULT_PLUS1_TIMER);

                        Log.i(getClass().getName(), "cpid="+cpid);
                        Applications.preference.put(Preference.USER_ID, uid);
                        Applications.preference.put(Preference.INVITE, invite);
                        Applications.preference.put(Preference.REDEEMCODE, Long.toString(Long.parseLong(Applications.preference.getValue(Preference.USER_ID, "")), 36));
                        Applications.preference.put(Preference.CPID, cpid);
                        Applications.ePreference.put(EPreference.N_TROPHY, trophy);
                        Applications.preference.put(Preference.PROFILE_IMAGE, "default");
                        Applications.preference.put(Preference.PLUS1_TIMER, p1Timer);

                        if( btn_type.equals("join")) {
                            Toast.makeText(this, "이미 가입한 아이디로 로그인합니다.", Toast.LENGTH_LONG).show();
                        }

                        isVersionUp = false;
                        GoMain();
                    }else if( action.equals(CommonUtil.ACTION_VERSION)){
                        final String v_n = jo.getString("v_n");
                        final String v_p = jo.getString("v_p");
                        final int v_c = Integer.parseInt(jo.getString("v_c"));
                        Applications.ePreference.put(EPreference.VERSION_N, v_n+"");
                        Applications.ePreference.put(EPreference.VERSION_P, v_p+"");
                        Applications.ePreference.put(EPreference.VERSION_C, v_c+"");
                        int version = CommonUtil.getVersionCode(this);
                        if( version < v_c){
                            cashPopDialog = new CashPopDialog(SignActivity.this);
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
                                    cashPopDialog.dismiss();
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
                        }else{
                            isVersion = true;
                            if( Applications.preference.getValue(Preference.USER_ID, "").equals("")){
                                complete();
                            }else{
                                if( isSplash){
                                    GoMain();
                                }
                            }
                        }
                    }
                }else if( error != null && error.equals(CommonUtil.ERROR_NO_USER)){
                    if( btn_type.equals("login")){
                        startActivity(new Intent(SignActivity.this, SignLoginActivity.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }else if( btn_type.equals("join")){
                        startActivity(new Intent(SignActivity.this, SignJoinActivity.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                    btn_type = "";
                }else if( error != null && error.equals(CommonUtil.ERROR_USIM)){
                    if( btn_type.equals("login")){
                        cashPopDialog = new CashPopDialog(SignActivity.this);
                        cashPopDialog.setCpTitle(SignActivity.this.getResources().getString(R.string.user_error));
                        cashPopDialog.setCpDesc(SignActivity.this.getResources().getString(R.string.user_error_desc));
                        cashPopDialog.setCpOkButton(SignActivity.this.getResources().getString(R.string.signup), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(SignActivity.this, SignJoinActivity.class));
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                cashPopDialog.dismiss();
                            }
                        });
                        cashPopDialog.setCpCancelButton(SignActivity.this.getResources().getString(R.string.cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cashPopDialog.dismiss();
                            }
                        });
                        cashPopDialog.show();
                    }else if( btn_type.equals("join")){
                        startActivity(new Intent(SignActivity.this, SignJoinActivity.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                isVersion = true;
                if( Applications.preference.getValue(Preference.USER_ID, "").equals("")){
                    complete();
                }else{
                    if( isSplash){
                        isVersionUp = false;
                        GoMain();
                    }
                }
            } finally {
                HideLoadingProgress();
            }
        }
    }

    @Override
    public void onTaskError(String param, String action, String result) {
        HideLoadingProgress();
        try {
            Log.e(TAG, action);
            if( action.equals(CommonUtil.ACTION_VERSION)){
                isVersion = true;
                if( Applications.preference.getValue(Preference.USER_ID, "").equals("")){
                    complete();
                }else{
                    if( isSplash) {
                        isVersionUp = false;
                        GoMain();
                    }
                }
            }else if( action.equals(CommonUtil.ACTION_LOGIN)){
                showErrorNetwork(param, action);
            }
        }catch (Exception ignore){
            ignore.printStackTrace();
        }
    }

    public void showErrorNetwork(final String param, final String action){
        if( networkDialog == null){
            networkDialog = new NetworkDialog(SignActivity.this);
        }
        if( !networkDialog.isShowing()) {
            networkDialog.setCancelClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HideLoadingProgress();
                    networkDialog.dismiss();
                    ActivityCompat.finishAffinity(SignActivity.this);
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
            case R.id.tv_login:
                tracker.send(new HitBuilders.EventBuilder().setCategory(analiticsCategory).setAction("/log_in_click").build());
                Login();
                break;
            case R.id.btn_join:
                tracker.send(new HitBuilders.EventBuilder().setCategory(analiticsCategory).setAction("/sign_up_click").build());
                Join();
                break;
        }
    }

    @Override
    public void complete() {
        if( isSplash && isVersion) {
            isSplash = false;
            Applications.isStart = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingViewAnim.stop();
                    tv_init.setVisibility(View.INVISIBLE);
                    //loadingIv.setVisibility(View.INVISIBLE);
                    //Login();
                    btns.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public void requestAsyncTask(String param, String action){
        if( Applications.getCountry(this).equals("KR") && !Applications.isRoaming(this)) {
            new AsyncHTTPPost(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, CommonUtil.SERVER_URL, param, action);
        }else{
            new AsyncHTTPPost(this).execute(CommonUtil.SERVER_URL, param, action);
        }
    }

    public void settingPermission(){
        cashPopDialog = new CashPopDialog(this);
        cashPopDialog.setCpTitle(getResources().getString(R.string.permission_title));
        cashPopDialog.setCpDesc(getResources().getString(R.string.permission_go_desc));
        cashPopDialog.setCpOkButton(getResources().getString(R.string.permission_go), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, request_permission);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    startActivityForResult(intent, request_permission);
                }
                cashPopDialog.dismiss();
            }
        });
        cashPopDialog.setCpCancelButton(getResources().getString(R.string.exit), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cashPopDialog.dismiss();
                ActivityCompat.finishAffinity(SignActivity.this);
            }
        });
        cashPopDialog.setCpCancel(false);
        cashPopDialog.setCancelable(false);
        cashPopDialog.show();
    }

    public String[] getPermissionArray(){
        boolean get_account = this.checkPermissionPoint(Manifest.permission.GET_ACCOUNTS);
        boolean write_es = this.checkPermissionPoint(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        boolean read_es = this.checkPermissionPoint(Manifest.permission.READ_EXTERNAL_STORAGE);
        boolean read_ps = this.checkPermissionPoint(Manifest.permission.READ_PHONE_STATE);
        boolean read_ct = this.checkPermissionPoint(Manifest.permission.READ_CONTACTS);
        List<String> requestPermisionArr = new ArrayList<>();
        requestPermisionArr.clear();
        if( !get_account){
            Log.e(TAG,"GET_ACCOUNTS");
            requestPermisionArr.add(Manifest.permission.GET_ACCOUNTS);
        }
        if( !write_es){
            Log.e(TAG,"WRITE_EXTERNAL_STORAGE");
            requestPermisionArr.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if( !read_es){
            Log.e(TAG,"READ_EXTERNAL_STORAGE");
            requestPermisionArr.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if( !read_ps){
            Log.e(TAG,"READ_PHONE_STATE");
            requestPermisionArr.add(Manifest.permission.READ_PHONE_STATE);
        }

        if( !read_ct){
            Log.e(TAG,"READ_CONTACTS");
            requestPermisionArr.add(Manifest.permission.READ_CONTACTS);
        }

        if( requestPermisionArr.size() > 0){
            Log.e(TAG,"requestPermissions");
            String[] permissionArr = requestPermisionArr.toArray(new String[requestPermisionArr.size()]);
            return permissionArr;
        }else{
            return new String[]{};
        }
    }

    public boolean isPermissionNotShow(){

        boolean isNotShow = false;

        if( !this.checkPermissionPoint(Manifest.permission.GET_ACCOUNTS) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.GET_ACCOUNTS)){
            isNotShow = true;
        }
        if( !this.checkPermissionPoint(Manifest.permission.WRITE_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            isNotShow = true;
        }
        if( !this.checkPermissionPoint(Manifest.permission.READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            isNotShow = true;
        }
        if( !this.checkPermissionPoint(Manifest.permission.READ_PHONE_STATE) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)){
            isNotShow = true;
        }

        if( !this.checkPermissionPoint(Manifest.permission.READ_CONTACTS) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)){
            isNotShow = true;
        }

        return isNotShow;
    }

    public boolean checkPermissionPoint(String permission){
        int permissionResult = ContextCompat.checkSelfPermission(this, permission);
        Log.i(TAG, "permission = "+permission+", permissionResult = "+permissionResult);
        if( permissionResult == PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            if( ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){
                return false;
            }
        }
        return false;
    }

}
