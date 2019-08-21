package kr.co.gubed.habit2good;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mapps.android.view.AdView;
import com.mz.common.listener.AdListener;
import com.tnkfactory.ad.BannerAdListener;
import com.tnkfactory.ad.BannerAdType;
import com.tnkfactory.ad.BannerAdView;
import com.tnkfactory.ad.TnkSession;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.regex.Pattern;

import kr.co.gubed.habit2good.gpoint.activity.ProfileActivity;
import kr.co.gubed.habit2good.gpoint.activity.SettingActivity;
import kr.co.gubed.habit2good.gpoint.activity.SignActivity;
import kr.co.gubed.habit2good.gpoint.listener.AsyncTaskCompleteListener;
import kr.co.gubed.habit2good.gpoint.model.NetworkErrorModel;
import kr.co.gubed.habit2good.gpoint.util.APICrypto;
import kr.co.gubed.habit2good.gpoint.util.Applications;
import kr.co.gubed.habit2good.gpoint.util.AsyncHTTPPost;
import kr.co.gubed.habit2good.gpoint.util.CommonUtil;
import kr.co.gubed.habit2good.gpoint.util.EPreference;
import kr.co.gubed.habit2good.gpoint.util.Preference;
import kr.co.gubed.habit2good.gpoint.view.CashPopDialog;
import kr.co.gubed.habit2good.gpoint.view.LoadingDialog;


public class DashboardActivity extends BaseActivity implements AsyncTaskCompleteListener<String>,
                                                                BaseSliderView.OnSliderClickListener,
                                                                ViewPagerEx.OnPageChangeListener {
    private TextView tv_id, tv_nickname;

    private ImageView iv_profile_image;
    private ImageView iv_edit_profile;
    private ImageView iv_ic_gpoint, iv_ic_trophy;
    private TextView tv_gpoint, tv_balance;
    private TextView tv_trophy;
    private TextView tv_my_gpoint, tv_partner_gpoint;
    private TextView tv_level1count, tv_level1point;
    private TextView tv_level2count, tv_level2point;
    private TextView tv_level3count, tv_level3point;
    private TextView tv_level4count, tv_level4point;
    private TextView tv_level5count, tv_level5point;
    private TextView tv_level6count, tv_level6point;
    /*ADMOB private AdView adViewWallet;*/
    /*TNK private BannerAdView bannerAdView;*/
    private AdView m_adView = null;                 // MANPLUS
    private Handler handler = new Handler();
    private EndingDialog mEndingDialog;

    private LinearLayout ll_demoDisplay;
    private LinearLayout ll_rating;
    private TextView tv_demo;

    private LoadingDialog loadingDialog;

    private String TAG = this.getClass().toString();
    private CashPopDialog cashPopDialog;
    private Applications applications;
    private Tracker tracker;

    private String noticeContents;

    Toolbar toolbar;
    ActionBar actionbar;
    private SliderLayout mNotificationSlider;

    @Override
    public void onSliderClick(BaseSliderView slider) {
        String slideName = slider.getBundle().getString("extra");
        Toast.makeText(this,slideName + "",Toast.LENGTH_SHORT).show();
        Intent intent = null;

        if (slideName.equals(Applications.preference.getValue(Preference.NOTI_SLIDE_1_NAME, getResources().getString(R.string.notislide1_name)))) {
            showGoodsayingDialog();
        } else if (slideName.equals(Applications.preference.getValue(Preference.NOTI_SLIDE_2_NAME,getResources().getString(R.string.notislide2_name)))){
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Applications.preference.getValue(Preference.NOTI_SLIDE_2_LINK, "https://blog.naver.com/habit2good/221453138961")));
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (slideName.equals(Applications.preference.getValue(Preference.NOTI_SLIDE_3_NAME,getResources().getString(R.string.notislide3_name)))){
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Applications.preference.getValue(Preference.NOTI_SLIDE_3_LINK, "https://blog.naver.com/habit2good/221454959364")));
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (slideName.equals(Applications.preference.getValue(Preference.NOTI_SLIDE_4_NAME,getResources().getString(R.string.notislide4_name)))){
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Applications.preference.getValue(Preference.NOTI_SLIDE_4_LINK, "https://blog.naver.com/habit2good/221458078758")));
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            return;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        //Toast.makeText(this,"slide position="+position,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    int getContentViewId() {
        if (Applications.preference.getValue(Preference.USER_ID, "").equals("")) {
            return R.layout.intro_gtrophy_and_gpoint;
        } else {
            return R.layout.activity_dashboard;
        }
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.btn_nav_dashboard;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(getClass().getName(), "IMHERE start onCreate");
        super.onCreate(savedInstanceState);
        onNewIntent(getIntent());

        View view;

        if (Applications.preference.getValue(Preference.USER_ID, "").equals("")) {
            Intent intent = new Intent(getApplicationContext(), SignActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.i(TAG, "user_id"+Applications.preference.getValue(Preference.USER_ID, ""));
        }
        if (Applications.preference.getValue(Preference.PHONE_NM, "").equals("")) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
            String phoneNum = telephonyManager.getLine1Number();
            Applications.preference.put(Preference.PHONE_NM, phoneNum);
        }
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
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

//        pointTabfragment.getNoticePopFileCache();
//        pointTabfragment.getGiftBoxFileCache();
//        pointTabfragment.getNoticeFileCache();

        int version = CommonUtil.getVersionCode(this);
        applications = (Applications) getApplication();
        tracker = applications.getDefaultTracker();

        //Crashlytics.setUserIdentifier(Applications.preference.getValue(Preference.USER_ID, ""));

        //activity = (MainActivity)getActivity();

        toolbar = findViewById(R.id.habits_toolbar);
        setSupportActionBar(toolbar);
        actionbar = getSupportActionBar();
        try {
            actionbar.setTitle(R.string.btn_nav_dashboard);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        setHorizontalSlide();

        this.init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            noticeContents = extras.getString(CommonUtil.EXTRA_NOTICE_CONTENTS);
            if (noticeContents != null) {
                Log.i(getClass().getName(), "noticeContents=" + noticeContents);

                Intent intentNotification = new Intent(Intent.ACTION_VIEW, Uri.parse(noticeContents));
                startActivity(intentNotification);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Applications.preference.getValue(Preference.USER_ID, "").equals("")) {
            return;
        }

        /*TNK
        if (bannerAdView != null) {
            bannerAdView.onResume();
        }*/
        if (m_adView != null)
            m_adView.StartService();

        String profileImage = Applications.preference.getValue(Preference.PROFILE_IMAGE, "");
        String profileImageUrl = CommonUtil.PROFILE_SERVER_IMAGE_URL + Applications.preference.getValue(Preference.USER_ID, "") + "/my_profile.jpg";
        if (profileImage.equals("default")) {
            Glide.with(this).load(profileImageUrl)
                    .apply(new RequestOptions()
                            .error(R.drawable.profile_default_image)
                            .circleCrop())
                    .into(iv_profile_image);
        } else {
            File file = new File(profileImage);
            if (!file.exists()) {

                Log.i(getClass().getName(), "profileImageUrl="+profileImageUrl);

                Glide.with(this).load(profileImageUrl)
                        .apply(new RequestOptions()
                                .error(R.drawable.profile_default_image)
                                .circleCrop())
                        .into(iv_profile_image);
            } else {
                Glide.with(this).load(profileImage)
                        .apply(new RequestOptions()
                                .error(R.drawable.profile_default_image)
                                .circleCrop())
                        .into(iv_profile_image);
            }
        }

        String cpid = Applications.preference.getValue(Preference.CPID, "");
        String nickname = Applications.preference.getValue(Preference.NICKNAME, "");

        double totalGpoint = Applications.ePreference.getTotalGpoint();
        double balanceGpoint = Applications.ePreference.getBalanceGpoint();
        Integer trophy = Applications.ePreference.getValue(EPreference.N_TROPHY, 0);
        Integer myGpoint = Applications.ePreference.getValue(EPreference.N_MY_GPOINT, 0);
        Integer partner1Count = Applications.ePreference.getValue(EPreference.N_PARTNER_1_COUNT, 0);
        Integer partner1Gpoint = Applications.ePreference.getValue(EPreference.N_PARTNER_1_POINT, 0);
        Integer partner2Count = Applications.ePreference.getValue(EPreference.N_PARTNER_2_COUNT, 0);
        Integer partner2Gpoint = Applications.ePreference.getValue(EPreference.N_PARTNER_2_POINT, 0);
        Integer partner3Count = Applications.ePreference.getValue(EPreference.N_PARTNER_3_COUNT, 0);
        Integer partner3Gpoint = Applications.ePreference.getValue(EPreference.N_PARTNER_3_POINT, 0);
        Integer partner4Count = Applications.ePreference.getValue(EPreference.N_PARTNER_4_COUNT, 0);
        Integer partner4Gpoint = Applications.ePreference.getValue(EPreference.N_PARTNER_4_POINT, 0);
        Integer partner5Count = Applications.ePreference.getValue(EPreference.N_PARTNER_5_COUNT, 0);
        Integer partner5Gpoint = Applications.ePreference.getValue(EPreference.N_PARTNER_5_POINT, 0);
        Integer partner6Count = Applications.ePreference.getValue(EPreference.N_PARTNER_6_COUNT, 0);
        Integer partner6Gpoint = Applications.ePreference.getValue(EPreference.N_PARTNER_6_POINT, 0);

        Integer numberOfPartner = partner1Count+partner2Count+partner3Count+partner4Count+partner5Count+partner6Count;
        Integer sumPartnerPoint = partner1Gpoint+partner2Gpoint+partner3Gpoint+partner4Gpoint+partner5Gpoint+partner6Gpoint;
        Applications.preference.put(Preference.PARTNERS, numberOfPartner.toString());
        Applications.preference.put(Preference.PARTNER_GPOINT, sumPartnerPoint.toString());

        Log.i(TAG,"numberOfpartner="+Applications.preference.getValue(Preference.PARTNERS, "0")+
                " sumPartner="+Applications.preference.getValue(Preference.PARTNER_GPOINT, "0"));

        tv_id.setText(cpid);
        Log.i(getClass().getName(), "cpid="+cpid);
        if (nickname.equals("")) {
            tv_nickname.setText(getResources().getString(R.string.my_nickname));
        } else {
            tv_nickname.setText(nickname);
        }
        tv_gpoint.setText(String.format("%,.0f", totalGpoint));
        tv_balance.setText(String.format("%,.0f", balanceGpoint));
        tv_trophy.setText(String.format("%,d", trophy));
        tv_my_gpoint.setText(String.format("%,d", myGpoint)+" GP");
        tv_partner_gpoint.setText(String.format("%,d", (partner1Gpoint+partner2Gpoint+partner3Gpoint+partner4Gpoint+partner5Gpoint+partner6Gpoint))+" GP");
        tv_level1count.setText(String.format("%,d", partner1Count)+" 명");
        tv_level1point.setText(String.format("%,d", partner1Gpoint)+" GP");
        tv_level2count.setText(String.format("%,d", partner2Count)+" 명");
        tv_level2point.setText(String.format("%,d", partner2Gpoint)+" GP");
        tv_level3count.setText(String.format("%,d", partner3Count)+" 명");
        tv_level3point.setText(String.format("%,d", partner3Gpoint)+" GP");
        tv_level4count.setText(String.format("%,d", partner4Count)+" 명");
        tv_level4point.setText(String.format("%,d", partner4Gpoint)+" GP");
        tv_level5count.setText(String.format("%,d", partner5Count)+" 명");
        tv_level5point.setText(String.format("%,d", partner5Gpoint)+" GP");
        tv_level6count.setText(String.format("%,d", partner6Count)+" 명");
        tv_level6point.setText(String.format("%,d", partner6Gpoint)+" GP");

        int toValue = (int) totalGpoint;
        ValueAnimator va = ValueAnimator.ofInt((int) 0, toValue);
        va.setDuration(1000);
        va.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.anim.decelerate_interpolator));
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                try {
                    Integer value = (Integer) valueAnimator.getAnimatedValue();
                    tv_gpoint.setText(CommonUtil.setComma(value + "", false, false));
                } catch (Exception ignore) {
                }
            }
        });
        va.start();

        mNotificationSlider.startAutoCycle();
    }

    @Override
    public void onPause() {
        super.onPause();
        /*TNK
        if (bannerAdView != null) {
            bannerAdView.onPause();
        }*/
        if (m_adView != null)
            m_adView.StopService();
    }

    @Override
    protected void onStop() {
        if (mNotificationSlider != null) {
            mNotificationSlider.stopAutoCycle();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*TNK
        if (bannerAdView != null) {
            bannerAdView.onDestroy();
        }*/
        if (mEndingDialog != null) {
            mEndingDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        manPlusEndingView(1413, 31780, 803889, AdView.TYPE_HTML);
        //super.onBackPressed();
    }

    private void init() {

        tv_id = findViewById(R.id.tv_id);
        tv_nickname = findViewById(R.id.tv_nickname);
        iv_profile_image = findViewById(R.id.profile_image);
        iv_edit_profile = findViewById(R.id.iv_edit_profile);
        iv_ic_gpoint = findViewById(R.id.iv_ic_gpoint);
        tv_gpoint = findViewById(R.id.tv_gpoint);
        tv_balance = findViewById(R.id.tv_balance);
        iv_ic_trophy = findViewById(R.id.iv_ic_trophy);
        tv_trophy = findViewById(R.id.tv_trophy);
        tv_my_gpoint = findViewById(R.id.tv_my_gpoint);
        tv_partner_gpoint = findViewById(R.id.tv_partner_gpoint);
        tv_level1count = findViewById(R.id.tv_level1count);
        tv_level1point = findViewById(R.id.tv_level1point);
        tv_level2count = findViewById(R.id.tv_level2count);
        tv_level2point = findViewById(R.id.tv_level2point);
        tv_level3count = findViewById(R.id.tv_level3count);
        tv_level3point = findViewById(R.id.tv_level3point);
        tv_level4count = findViewById(R.id.tv_level4count);
        tv_level4point = findViewById(R.id.tv_level4point);
        tv_level5count = findViewById(R.id.tv_level5count);
        tv_level5point = findViewById(R.id.tv_level5point);
        tv_level6count = findViewById(R.id.tv_level6count);
        tv_level6point = findViewById(R.id.tv_level6point);

        iv_profile_image.setOnClickListener(onProfileImageSelectedListener);
        iv_edit_profile.setOnClickListener(onEditProfileSelectedListener);

        LinearLayout ll_gpoint = findViewById(R.id.ll_gpoint);
        ll_gpoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GpointRankingBoardActivity.class);
                startActivity(intent);
            }
        });

        ll_demoDisplay = findViewById(R.id.ll_demoDisplay);
        ll_demoDisplay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showInputDialogForDemo();
                return false;
            }
        });

        tv_demo = findViewById(R.id.tv_demo);
        tv_demo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialogForDemo();
                return;
            }
        });

        getUserInfo();
        requestWalletInfo();

        /*TNK
        bannerAdView = (BannerAdView) findViewById(R.id.banner_ad);
        bannerAdView.setBannerAdListener(new BannerAdListener() {
            @Override
            public void onFailure(int errCode) {

            }

            @Override
            public void onShow() {

            }

            @Override
            public void onClick() {

            }
        });
        bannerAdView.loadAd(TnkSession.CPC, BannerAdType.LANDSCAPE); // or bannerAdView.loadAd(TnkSession.CPC, BannerAdType.LANDSCAPE)*/
        /*adViewWallet = (AdView) findViewById(R.id.adViewWallet);
        AdRequest adRequest = new AdRequest.Builder().build();
        adViewWallet.loadAd(adRequest);*/

        manPlusBannerAdcreateBannerXMLMode();

        ll_rating = findViewById(R.id.rating);
        ll_rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.habit2good_uri)));
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
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
            HideLoadingProgress();
            //hideRefreshIconAnimation();
        } catch (Exception ignore) {
        }

        try {
            JSONObject jo = new JSONObject(rst);
            String error = jo.getString(CommonUtil.RESULT_ERROR);
            String action = jo.getString(CommonUtil.RESULT_ACTION);
            Log.i(getClass().getName(), "onTaskComplete action="+action);

            if (error != null && error.isEmpty() && action != null && !action.isEmpty()) {
                switch (action) {
                    case CommonUtil.ACTION_H2G_GET_INFO:
                        String budget = jo.getString(CommonUtil.RESULT_BUDGET);
                        Double totalGpoint = Double.parseDouble(budget);
                        String balance = jo.getString(CommonUtil.RESULT_BALANCE_GPOINT);
                        Double balanceGpoint = Double.parseDouble(balance);
                        Integer myGpoint = Integer.parseInt(jo.getString(CommonUtil.RESULT_H2G_MYPOINT));
                        Integer trophy = Integer.parseInt(jo.getString(CommonUtil.RESULT_H2G_TROPHY));
                        Integer partner1Count = Integer.parseInt(jo.getString(CommonUtil.RESULT_H2G_PARTNER_1_COUNT));
                        Integer partner2Count = Integer.parseInt(jo.getString(CommonUtil.RESULT_H2G_PARTNER_2_COUNT));
                        Integer partner3Count = Integer.parseInt(jo.getString(CommonUtil.RESULT_H2G_PARTNER_3_COUNT));
                        Integer partner4Count = Integer.parseInt(jo.getString(CommonUtil.RESULT_H2G_PARTNER_4_COUNT));
                        Integer partner5Count = Integer.parseInt(jo.getString(CommonUtil.RESULT_H2G_PARTNER_5_COUNT));
                        Integer partner6Count = Integer.parseInt(jo.getString(CommonUtil.RESULT_H2G_PARTNER_6_COUNT));
                        Integer partner1Gpoint = Integer.parseInt(jo.getString(CommonUtil.RESULT_H2G_PARTNER_1_POINT));
                        Integer partner2Gpoint = Integer.parseInt(jo.getString(CommonUtil.RESULT_H2G_PARTNER_2_POINT));
                        Integer partner3Gpoint = Integer.parseInt(jo.getString(CommonUtil.RESULT_H2G_PARTNER_3_POINT));
                        Integer partner4Gpoint = Integer.parseInt(jo.getString(CommonUtil.RESULT_H2G_PARTNER_4_POINT));
                        Integer partner5Gpoint = Integer.parseInt(jo.getString(CommonUtil.RESULT_H2G_PARTNER_5_POINT));
                        Integer partner6Gpoint = Integer.parseInt(jo.getString(CommonUtil.RESULT_H2G_PARTNER_6_POINT));
                        String slide1Name = jo.getString(CommonUtil.RESULT_H2G_NOTI_SLIDE_1_NAME);
                        String slide1Image = jo.getString(CommonUtil.RESULT_H2G_NOTI_SLIDE_1_IMAGE_URL);
                        String slide1Link = jo.getString(CommonUtil.RESULT_H2G_NOTI_SLIDE_1_LINK_URL);
                        String slide2Name = jo.getString(CommonUtil.RESULT_H2G_NOTI_SLIDE_2_NAME);
                        String slide2Image = jo.getString(CommonUtil.RESULT_H2G_NOTI_SLIDE_2_IMAGE_URL);
                        String slide2Link = jo.getString(CommonUtil.RESULT_H2G_NOTI_SLIDE_2_LINK_URL);
                        String slide3Name = jo.getString(CommonUtil.RESULT_H2G_NOTI_SLIDE_3_NAME);
                        String slide3Image = jo.getString(CommonUtil.RESULT_H2G_NOTI_SLIDE_3_IMAGE_URL);
                        String slide3Link = jo.getString(CommonUtil.RESULT_H2G_NOTI_SLIDE_3_LINK_URL);
                        String slide4Name = jo.getString(CommonUtil.RESULT_H2G_NOTI_SLIDE_4_NAME);
                        String slide4Image = jo.getString(CommonUtil.RESULT_H2G_NOTI_SLIDE_4_IMAGE_URL);
                        String slide4Link = jo.getString(CommonUtil.RESULT_H2G_NOTI_SLIDE_4_LINK_URL);
                        String plus1Timer = jo.getString(CommonUtil.RESULT_PLUS1_TIMER);

                        //final double storedTotalGpoint = Applications.ePreference.getTotalGpoint();
                        //final Integer storedMyGpoint = Applications.ePreference.getValue(EPreference.N_MY_GPOINT, 0);
                        tv_gpoint.setText(String.format("%,.0f", totalGpoint));
                        tv_balance.setText(String.format("%,.0f", balanceGpoint));
                        tv_trophy.setText(String.format("%,d", trophy));
                        tv_my_gpoint.setText(String.format("%,d", myGpoint)+" GP");
                        tv_partner_gpoint.setText(String.format("%,d", (partner1Gpoint+partner2Gpoint+partner3Gpoint+partner4Gpoint+partner5Gpoint+partner6Gpoint))+" GP");
                        tv_level1count.setText(String.format("%,d", partner1Count)+" 명");
                        tv_level1point.setText(String.format("%,d", partner1Gpoint)+" GP");
                        tv_level2count.setText(String.format("%,d", partner2Count)+" 명");
                        tv_level2point.setText(String.format("%,d", partner2Gpoint)+" GP");
                        tv_level3count.setText(String.format("%,d", partner3Count)+" 명");
                        tv_level3point.setText(String.format("%,d", partner3Gpoint)+" GP");
                        tv_level4count.setText(String.format("%,d", partner4Count)+" 명");
                        tv_level4point.setText(String.format("%,d", partner4Gpoint)+" GP");
                        tv_level5count.setText(String.format("%,d", partner5Count)+" 명");
                        tv_level5point.setText(String.format("%,d", partner5Gpoint)+" GP");
                        tv_level6count.setText(String.format("%,d", partner6Count)+" 명");
                        tv_level6point.setText(String.format("%,d", partner6Gpoint)+" GP");

                        /*
                         * habit2good 20181108
                         */
                        Applications.ePreference.putTotalGpoint(totalGpoint);
                        Applications.ePreference.putBalanceGpoint(balanceGpoint);
                        Applications.ePreference.put(EPreference.N_MY_GPOINT, myGpoint);
                        Applications.ePreference.put(EPreference.N_TROPHY, trophy);
                        Applications.ePreference.put(EPreference.N_PARTNER_1_COUNT, partner1Count);
                        Applications.ePreference.put(EPreference.N_PARTNER_2_COUNT, partner2Count);
                        Applications.ePreference.put(EPreference.N_PARTNER_3_COUNT, partner3Count);
                        Applications.ePreference.put(EPreference.N_PARTNER_4_COUNT, partner4Count);
                        Applications.ePreference.put(EPreference.N_PARTNER_5_COUNT, partner5Count);
                        Applications.ePreference.put(EPreference.N_PARTNER_6_COUNT, partner6Count);
                        Applications.ePreference.put(EPreference.N_PARTNER_1_POINT, partner1Gpoint);
                        Applications.ePreference.put(EPreference.N_PARTNER_2_POINT, partner2Gpoint);
                        Applications.ePreference.put(EPreference.N_PARTNER_3_POINT, partner3Gpoint);
                        Applications.ePreference.put(EPreference.N_PARTNER_4_POINT, partner4Gpoint);
                        Applications.ePreference.put(EPreference.N_PARTNER_5_POINT, partner5Gpoint);
                        Applications.ePreference.put(EPreference.N_PARTNER_6_POINT, partner6Gpoint);

                        Integer numberOfPartner = partner1Count+partner2Count+partner3Count+partner4Count+partner5Count+partner6Count;
                        Integer sumPartnerPoint = partner1Gpoint+partner2Gpoint+partner3Gpoint+partner4Gpoint+partner5Gpoint+partner6Gpoint;
                        Applications.preference.put(Preference.PARTNERS, numberOfPartner.toString());
                        Applications.preference.put(Preference.PARTNER_GPOINT, sumPartnerPoint.toString());

                        Applications.preference.put(Preference.CPID, jo.getString("id"));

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

                        Applications.preference.put(Preference.NOTI_SLIDE_1_NAME, slide1Name);
                        Applications.preference.put(Preference.NOTI_SLIDE_1_IMAGE, slide1Image);
                        Applications.preference.put(Preference.NOTI_SLIDE_1_LINK, slide1Link);
                        Applications.preference.put(Preference.NOTI_SLIDE_2_NAME, slide2Name);
                        Applications.preference.put(Preference.NOTI_SLIDE_2_IMAGE, slide2Image);
                        Applications.preference.put(Preference.NOTI_SLIDE_2_LINK, slide2Link);
                        Applications.preference.put(Preference.NOTI_SLIDE_3_NAME, slide3Name);
                        Applications.preference.put(Preference.NOTI_SLIDE_3_IMAGE, slide3Image);
                        Applications.preference.put(Preference.NOTI_SLIDE_3_LINK, slide3Link);
                        Applications.preference.put(Preference.NOTI_SLIDE_4_NAME, slide4Name);
                        Applications.preference.put(Preference.NOTI_SLIDE_4_IMAGE, slide4Image);
                        Applications.preference.put(Preference.NOTI_SLIDE_4_LINK, slide4Link);

                        setHorizontalSlide();
                        mNotificationSlider.movePrevPosition(false);

                        Applications.preference.put(Preference.PLUS1_TIMER, plus1Timer);
                        break;
                    case CommonUtil.ACTION_GET_USER:
                        String nickname = jo.getString(CommonUtil.RESULT_NICKNAME);
                        Applications.preference.put(Preference.NICKNAME, nickname);
                        tv_nickname.setText(nickname);
                        break;
                    default:
                        break;
                }
            } else {
                Log.e(getClass().getName(), "CommonUtil.ACTION fail action="+action);
            }
        } catch (Exception e) {
            try {
                //hideRefreshIconAnimation();
            } catch (Exception ignore) {
            }
            e.printStackTrace();
        } finally {
            try {
                //hideRefreshIconAnimation();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void onTaskError(String param, String action, String result) {
        try {
            HideLoadingProgress();
            //hideRefreshIconAnimation();
        } catch (Exception ignore) {
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_refresh:
                /*double totalGpoint = Applications.ePreference.getTotalGpoint();
                tv_gpoint.setText(String.format("%,.0f", totalGpoint));*/
                tv_gpoint.setTextColor(Color.BLACK);
                tv_gpoint.clearAnimation();
                init();
                break;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingActivity.class);
                this.startActivity(intent);
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                break;
            case R.id.action_homepage:
                Intent intentBlog = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_uri)));
                startActivity(intentBlog);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.action_help:
                CommonUtil.showSupport(this, true);
                break;
            case R.id.action_info:
                Intent intentInfo = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.dashboard_about_uri)));
                startActivity(intentInfo);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public synchronized void requestWalletInfo() {
        Applications.isHomeRefresh = false;
        HashMap<String, String> map = new HashMap<>();
        map.put(CommonUtil.KEY_USERID, Applications.preference.getValue(Preference.USER_ID, ""));
        map.put(CommonUtil.KEY_ADID, Applications.preference.getValue(Preference.AD_ID, ""));
        map.put(CommonUtil.KEY_DEVICE_TOKEN, Applications.preference.getValue(Preference.DEVICE_TOKEN, ""));
        map.put(CommonUtil.KEY_PHONE_NM, Applications.preference.getValue(Preference.PHONE_NM, ""));
        int version = CommonUtil.getVersionCode(this);
        map.put(CommonUtil.KEY_NAME, version + "");
        map.put(CommonUtil.KEY_ACTION, CommonUtil.ACTION_H2G_GET_INFO);
        String param = APICrypto.getParam(this, map, CommonUtil.SHARED_KEY);
        requestAsyncTask(param, CommonUtil.ACTION_H2G_GET_INFO);
    }

    public void requestAsyncTask(String param, String action) {
        Log.i(getClass().getName(), "requestAsyncTask action="+action);
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

    void setDemoDisplay(Integer member, Integer averageIncome, Integer tr) {
        long totalGpoint;
        long trophy = tr;
        long myGpoint = (int)(averageIncome * 1);
        long partner1Count = (long)Math.pow(member, 1);
        long partner2Count = (long)Math.pow(member, 2);
        long partner3Count = (long)Math.pow(member, 3);
        long partner4Count = (long)Math.pow(member, 4);
        long partner5Count = (long)Math.pow(member, 5);
        long partner6Count = (long)Math.pow(member, 6);
        long partner1Gpoint = partner1Count * (long)(averageIncome * 0.1);
        long partner2Gpoint = partner2Count * (long)(averageIncome * 0.1);
        long partner3Gpoint = partner3Count * (long)(averageIncome * 0.1);
        long partner4Gpoint = partner4Count * (long)(averageIncome * 0.1);
        long partner5Gpoint = partner5Count * (long)(averageIncome * 0.1);
        long partner6Gpoint = partner6Count * (long)(averageIncome * 0.1);
        totalGpoint = myGpoint + partner1Gpoint + partner2Gpoint + partner3Gpoint + partner4Gpoint + partner5Gpoint + partner6Gpoint;

        tv_gpoint.setText("월 예상 수익 "+String.format("%,d", totalGpoint));
        tv_gpoint.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        tv_gpoint.startAnimation(anim);

        tv_trophy.setText(String.format("%,d", trophy));
        tv_my_gpoint.setText(String.format("%,d", myGpoint)+" GP");
        tv_partner_gpoint.setText(String.format("%,d", (partner1Gpoint+partner2Gpoint+partner3Gpoint+partner4Gpoint+partner5Gpoint+partner6Gpoint))+" GP");
        tv_level1count.setText(String.format("%,d", partner1Count)+" 명");
        tv_level1point.setText(String.format("%,d", partner1Gpoint)+" GP");
        tv_level2count.setText(String.format("%,d", partner2Count)+" 명");
        tv_level2point.setText(String.format("%,d", partner2Gpoint)+" GP");
        tv_level3count.setText(String.format("%,d", partner3Count)+" 명");
        tv_level3point.setText(String.format("%,d", partner3Gpoint)+" GP");
        tv_level4count.setText(String.format("%,d", partner4Count)+" 명");
        tv_level4point.setText(String.format("%,d", partner4Gpoint)+" GP");
        tv_level5count.setText(String.format("%,d", partner5Count)+" 명");
        tv_level5point.setText(String.format("%,d", partner5Gpoint)+" GP");
        tv_level6count.setText(String.format("%,d", partner6Count)+" 명");
        tv_level6point.setText(String.format("%,d", partner6Gpoint)+" GP");
    }

    public void showInputDialogForDemo() {
        final EditText partnerEditText = new EditText(this);
        final EditText incomeEditText = new EditText(this);
        final EditText trophyEditText = new EditText(this);
        LinearLayout layout = new LinearLayout(this);

        layout.setOrientation(LinearLayout.VERTICAL);

        InputFilter[] filters = new InputFilter[] {
                new InputFilter.LengthFilter(9)
        };
        partnerEditText.setHint("파트너 수");
        partnerEditText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        partnerEditText.setFilters(filters);
        layout.addView(partnerEditText);
        incomeEditText.setHint("일인당 한달 평균 수익");
        incomeEditText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        incomeEditText.setFilters(filters);
        layout.addView(incomeEditText);
        trophyEditText.setHint("트로피 수");
        trophyEditText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        trophyEditText.setFilters(filters);
        layout.addView(trophyEditText);

        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("데모를 위한 값을 입력하세요.");
        builder.setView(layout);
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (partnerEditText.getText().length() == 0) {
                            partnerEditText.setText("5");
                        }
                        if (incomeEditText.getText().length() == 0) {
                            incomeEditText.setText("1000");
                        }
                        if (trophyEditText.getText().length() == 0) {
                            trophyEditText.setText("1000");
                        }
                        if (partnerEditText.getText().toString().equals(".") || incomeEditText.getText().toString().equals(".") || trophyEditText.getText().toString().equals(".")) {
                            Toast.makeText(getApplicationContext(), "잘못 입력하셨습니다. 입력 값을 확인 하시기 바랍니다.", Toast.LENGTH_LONG).show();
                        } else {
                            setDemoDisplay((int)Double.parseDouble(partnerEditText.getText().toString()),
                                    (int)Double.parseDouble(incomeEditText.getText().toString()),
                                    (int)Double.parseDouble(trophyEditText.getText().toString()));
                        }
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    private void showRefreshIconAnimation() {
        iv_ic_gpoint.clearAnimation();
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
        anim.setRepeatCount(Animation.INFINITE);
        iv_ic_gpoint.startAnimation(anim);
    }

    private void hideRefreshIconAnimation() {
        iv_ic_gpoint.clearAnimation();
    }

    public void ShowLoadingProgress() {
        //show loading
        Log.i(getClass().getName(), "ShowLoadingProgress start");

        try {
            if (loadingDialog == null) {
                loadingDialog = new LoadingDialog(this);
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        loadingDialog.show();
                        Log.i(getClass().getName(), "loadingDialog.show");
                    } catch (Exception ignore) {
                        ignore.printStackTrace();
                    }
                }
            });
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }

    public void HideLoadingProgress() throws Exception {
        //hide loading
        Log.i(getClass().getName(), "HideLoadingProgress start");
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

    private void setHorizontalSlide() {
        mNotificationSlider = (SliderLayout)findViewById(R.id.slider);
        LinkedHashMap<String,String> url_maps = new LinkedHashMap<String, String>();
        url_maps.put(Applications.preference.getValue(Preference.NOTI_SLIDE_1_NAME, getResources().getString(R.string.notislide1_name)),
                Applications.preference.getValue(Preference.NOTI_SLIDE_1_IMAGE, "http://a.habit2good.com/image/share/notislide_1.png"));
        url_maps.put(Applications.preference.getValue(Preference.NOTI_SLIDE_2_NAME, getResources().getString(R.string.notislide2_name)),
                Applications.preference.getValue(Preference.NOTI_SLIDE_2_IMAGE, "http://a.habit2good.com/image/share/notislide_2.png"));
        url_maps.put(Applications.preference.getValue(Preference.NOTI_SLIDE_3_NAME, getResources().getString(R.string.notislide3_name)),
                Applications.preference.getValue(Preference.NOTI_SLIDE_3_IMAGE, "http://a.habit2good.com/image/share/notislide_3.png"));
        url_maps.put(Applications.preference.getValue(Preference.NOTI_SLIDE_4_NAME, getResources().getString(R.string.notislide4_name)),
                Applications.preference.getValue(Preference.NOTI_SLIDE_4_IMAGE, "http://a.habit2good.com/image/share/notislide_4.png"));

        /*LinkedHashMap<String,Integer> file_maps = new LinkedHashMap<String, Integer>();

        file_maps.put("해빗투굿 생각",R.drawable.notislide_1);
        file_maps.put("알리타",R.drawable.alita);
        file_maps.put("데드풀2",R.drawable.deadpool2);
        file_maps.put("캡틴마블", R.drawable.mavel);*/


        //for(String name : file_maps.keySet()){
        mNotificationSlider.removeAllSliders();
        for(String name : url_maps.keySet()){
            TextSliderView textSliderView = new TextSliderView(this);
            // initialize a SliderLayout
            textSliderView
                    .description(name)
                    .image(url_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                    .setOnSliderClickListener(this);

            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra",name);

            mNotificationSlider.addSlider(textSliderView);
        }
        mNotificationSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
        mNotificationSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mNotificationSlider.setCustomAnimation(new DescriptionAnimation());
        mNotificationSlider.setDuration(4000);
        mNotificationSlider.addOnPageChangeListener(this);
    }

    private final View.OnClickListener onProfileImageSelectedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showImageDialog();
        }
    };

    private void showImageDialog() {
        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(builder.getWindow()).setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });
        builder.setContentView(R.layout.dialog_photo_viewer);

        ImageView imageView = builder.findViewById(R.id.iv_photo);

        String profileImage = Applications.preference.getValue(Preference.PROFILE_IMAGE, "");
        String profileImageUrl = CommonUtil.PROFILE_SERVER_IMAGE_URL + Applications.preference.getValue(Preference.USER_ID, "") + "/my_profile.jpg";
        if (profileImage.equals("default")) {
            Glide.with(this).load(profileImageUrl)
                    .apply(new RequestOptions()
                            .error(R.drawable.profile_default_image))
                    .into(imageView);
        } else {
            File file = new File(profileImage);
            if (!file.exists()) {

                Log.i(getClass().getName(), "profileImageUrl="+profileImageUrl);

                Glide.with(this).load(profileImageUrl)
                        .apply(new RequestOptions()
                                .error(R.drawable.profile_default_image))
                        .into(imageView);
            } else {
                Glide.with(this).load(profileImage)
                        .apply(new RequestOptions()
                                .error(R.drawable.profile_default_image))
                        .into(imageView);
            }
        }
        builder.show();
    }

    private final View.OnClickListener onEditProfileSelectedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);

            //intent.putExtra("habitid", holder.habitid);
            //context.startActivity(intent);
            startActivityForResult(intent, CommonUtil.REQUEST_CODE_HABIT_REMINDER);
            overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
        }
    };

    public void getUserInfo(){
        ShowLoadingProgress();
        HashMap<String, String> map = new HashMap<>();
        map.put(CommonUtil.KEY_USERID, Applications.preference.getValue(Preference.USER_ID,""));
        map.put(CommonUtil.KEY_ACTION, CommonUtil.ACTION_GET_USER);
        String param = APICrypto.getParam(this, map, CommonUtil.SHARED_KEY);
        requestAsyncTask(param, CommonUtil.ACTION_GET_USER);
    }

    private void showGoodsayingDialog() {
        //setContentView(R.layout.dialog_good_saying);
        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.setContentView(R.layout.dialog_good_saying);

        //builder.setTitle(getResources().getString(R.string.quote_title));
        /*Objects.requireNonNull(builder.getWindow()).setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));*/

        /*builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });*/

        TextView tv_contents = builder.findViewById(R.id.dialog_contents);
        //TextView tv_author = findViewById(R.id.dialog_author);
        //TextView tv_desc = findViewById(R.id.dialog_desc);

        tv_contents.setText(Applications.preference.getValue(CommonUtil.INTENT_TYPE_GOOD_SAYING, getResources().getString(R.string.ht_contents)));
        WindowManager.LayoutParams params = builder.getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        builder.getWindow().setAttributes(params);

        builder.show();
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
        mEndingDialog = new EndingDialog(DashboardActivity.this, getResources().getString(R.string.ending_dialog_title), leftClickListener2, rightClickListener2);
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
