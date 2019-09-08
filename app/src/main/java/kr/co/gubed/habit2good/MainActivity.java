package kr.co.gubed.habit2good;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
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

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import io.fabric.sdk.android.Fabric;
import kr.co.gubed.habit2good.gpoint.activity.SignActivity;
import kr.co.gubed.habit2good.gpoint.filecache.FileCache;
import kr.co.gubed.habit2good.gpoint.filecache.FileCacheFactory;
import kr.co.gubed.habit2good.gpoint.listener.AsyncTaskCompleteListener;
import kr.co.gubed.habit2good.gpoint.service.MyFirebaseInstanceIDService;
import kr.co.gubed.habit2good.gpoint.util.APICrypto;
import kr.co.gubed.habit2good.gpoint.util.Applications;
import kr.co.gubed.habit2good.gpoint.util.AsyncHTTPPost;
import kr.co.gubed.habit2good.gpoint.util.CommonUtil;
import kr.co.gubed.habit2good.gpoint.util.Preference;
import kr.co.gubed.habit2good.gpoint.view.AppGuideDialog;
import kr.co.gubed.habit2good.gpoint.view.CashPopDialog;
import kr.co.gubed.habit2good.gpoint.view.NetworkDialog;
import kr.co.gubed.habit2good.gpoint.view.NoticeDialog;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;


public class MainActivity extends BaseActivity implements AsyncTaskCompleteListener<String> {
    private HabitDbAdapter dbAdapter;

    private TextView tv_attempt;
    private ImageView iv_plus1;
    private TextView tv_remain;
    private TranslateAnimation anim;
    private ScaleAnimation scaleAnimationanim;
    private TextView tv_selected_day, tv_count;
    private Switch adSwitch;
    /* ADMOB private AdView adView;*/               // Google admob
    private BannerAdView bannerAdView;              // TNK
    private RelativeLayout tnkBanner;
    private RelativeLayout manplusBanner;
    private AdView m_adView = null;                 // MANPLUS
    private RadioGroup radioGroup;

    private LineChartView chart;
    private LineChartData data;
    private int numberOfLines = 1;
    private int maxNumberOfLines = 1;
    public static final int numberOfPoints = 16;

    private Plus1[][] randomNumbersTab = new Plus1[maxNumberOfLines][numberOfPoints];

    private boolean hasAxes = true;
    private boolean hasAxesNames = true;
    private boolean hasLines = true;
    private boolean hasPoints = true;
    private ValueShape shape = ValueShape.CIRCLE;
    private boolean isFilled = false;
    private boolean hasLabels = false;
    private boolean isCubic = false;
    private boolean hasLabelForSelected = false;
    private boolean pointsHaveDifferentColor = true;
    private boolean hasGradientToTransparent = false;

    private long plus1Timer = Integer.parseInt(Applications.preference.getValue(Preference.PLUS1_TIMER, "60"));
    private long TIMER =  (plus1Timer < 60)? (60 * 1000) : (plus1Timer * 1000);  // default 1분
    private long lastTime;
    private long remainTime;
    private Timer timer;
    private TimerTask timerTask;

    private ArrayList<JSONObject> npMap;
    private Applications applications;
    private Tracker tracker;

    private CashPopDialog cashPopDialog;
    private AppGuideDialog appGuideDialog;
    private NetworkDialog networkDialog;
    private NoticeDialog noticeDialog;

    Toolbar toolbar;
    ActionBar actionbar;

    private FileCache noticePopFileCache;
    private FileCache giftboxFileCache;
    private FileCache noticeFileCache;

    private FirebaseAnalytics mFirebaseAnalytics;

    int [] imageArray = {R.drawable.thumbsup0, R.drawable.thumbsup1, R.drawable.thumbsup2, R.drawable.thumbsup3};

    private int graphPeriod = CommonUtil.CRITERIA_DAY;
    private SoundPool soundPool;
    private int soundId;

    private AdInterstitialView m_interView = null;
    private Handler handler = new Handler();
    private EndingDialog mEndingDialog=null;

    @Override
    int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.btn_nav_plus1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Applications.preference.getValue(Preference.USER_ID, "").equals("")) {
            Intent intent = new Intent(getApplicationContext(), PermissionDescActivity.class);
            startActivity(intent);

            /*getNoticePopFileCache();
            getGiftBoxFileCache();
            getNoticeFileCache();
*/
            npMap = new ArrayList<>();
/*
            requestNotice();
            AppGuidePopupShow();
*/

            finish();
        } else {
            Log.i(getClass().getName(), "user_id"+Applications.preference.getValue(Preference.USER_ID, ""));
            //view = getLayoutInflater().inflate(R.layout.activity_pointmall, null);
        }
        if (Applications.preference.getValue(Preference.PHONE_NM, "").equals("")) {
            String phoneNum=null;
            try {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
                phoneNum = telephonyManager.getLine1Number();

            } catch (SecurityException e) {
                e.printStackTrace();
                Intent intent = new Intent(getApplicationContext(), PermissionDescActivity.class);
                startActivity(intent);
                finish();
            }
            Applications.preference.put(Preference.PHONE_NM, phoneNum);
        }

        try {
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
        } catch (SecurityException e) {
            e.printStackTrace();
            Intent intent = new Intent(getApplicationContext(), PermissionDescActivity.class);
            startActivity(intent);
            finish();
        }

/*
        getNoticePopFileCache();
        getGiftBoxFileCache();
        getNoticeFileCache();
*/

        int version = CommonUtil.getVersionCode(this);
        applications = (Applications) getApplication();
        tracker = applications.getDefaultTracker();

        init();

/*
        requestNotice();
        AppGuidePopupShow();
*/

        generateValues();
        generateData();

        iv_plus1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adSwitch.isChecked() && ((int)(remainTime/1000) != 0)) {
                    //iv_plus1.startAnimation(anim);
                    Toast.makeText(getApplicationContext(), (int)(remainTime/1000)+" 초 후에 다시 시도하세요.", Toast.LENGTH_LONG).show();
                } else {
                    setPlus1UIData();
                }
            }
        });

        //admobInterstitialReqeust();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Applications.preference.getValue(Preference.USER_ID, "").equals("")) {
            final String ZERO = "0";

            lastTime = Applications.ePreference.getLastTimeForPlus1();
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastTime) < TIMER) {
                setPlus1Timer();
            } else {    // 하트 이미지, remain time 초기화
                iv_plus1.setImageResource(R.drawable.heart);
                tv_remain.setText(ZERO);
                remainTime = 0;
            }
        }

        if (bannerAdView != null) {
            bannerAdView.onResume();
        }

        if (m_adView != null)
            m_adView.StartService();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (timer != null)
            timer.cancel();

        if (bannerAdView != null) {
            bannerAdView.onPause();
        }
        if (m_adView != null)
            m_adView.StopService();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(8).build();
            soundId = soundPool.load(this, R.raw.plus1_signal, 1);
        } else {
            soundPool = new SoundPool(8, AudioManager.STREAM_NOTIFICATION, 0);
            soundId = soundPool.load(this, R.raw.plus1_signal, 1);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timer != null)
            timer.cancel();

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mEndingDialog != null) {
            mEndingDialog.dismiss();
        }
        //timer.cancel();
    }

    @Override
    public void onBackPressed() {
        /*Fill Rate이 10%이하로 나와 의미가 없음, 일단 현재 버전에서는 사용하지 않음
        manPlusEndingView(1413, 31780, 803889, AdView.TYPE_HTML);*/

        super.onBackPressed();
    }

    void init() {
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        //MobileAds.initialize(this, "ca-app-pub-7935254117249497~3267261242"); // 2018.12.04

        Fabric.with(this, new Crashlytics());
        Fabric.with(this, new Answers());
        FirebaseApp.initializeApp(this);
        //FirebaseApp.initializeApp(MainActivityTimeCash.this);
        if( Applications.preference.getValue(Preference.DEVICE_TOKEN,"").equals("")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MyFirebaseInstanceIDService mfs = new MyFirebaseInstanceIDService();
                    mfs.onTokenRefresh();
                }
            });
        }
        /*
         * Firebase Cloud Messaging 주제 정의
         * 주제를 정의하면 그룹 메시지 전송 가능
         * good_saying: 오늘의 명언(해빗투굿 생각)
         * reward: 현재 특별히 사용하지 않음.
         */
        FirebaseMessaging.getInstance().subscribeToTopic("good_saying");
        FirebaseMessaging.getInstance().subscribeToTopic("reward");
        FirebaseMessaging.getInstance().subscribeToTopic("notice");
        initAlarmChannel();

        dbAdapter = new HabitDbAdapter(this);

        toolbar = findViewById(R.id.habits_toolbar);
        setSupportActionBar(toolbar);
        actionbar = getSupportActionBar();
        actionbar.setTitle(R.string.btn_nav_plus1);

        tv_attempt = findViewById(R.id.tv_attempt);
        iv_plus1 = findViewById(R.id.iv_plus1);
        tv_remain = findViewById(R.id.tv_remain);
        /*lastTime = Applications.ePreference.getLastTimeForPlus1();
        Log.i(getClass().getName(), "PLUS1 lastTime 초기값: "+lastTime+" currentTime"+System.currentTimeMillis());
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastTime) < TIMER) {
            setPlus1Timer();
        }*/

        tv_selected_day = findViewById(R.id.tv_selected_day);
        tv_count = findViewById(R.id.tv_count);
        chart = findViewById(R.id.linechart);
        chart.setOnValueTouchListener(new ValueTouchListener());

        anim = new TranslateAnimation(0, 10, 0 ,10);
        anim.setDuration(100);
        scaleAnimationanim = new ScaleAnimation(0.1f, 1f, 0.1f, 1f);
        scaleAnimationanim.setDuration(200);

        adSwitch = findViewById(R.id.adSwitch);
        adSwitch.setChecked(Applications.preference.getValue(Preference.PLUS1_AD_FLAG, true));
        adSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Applications.preference.put(Preference.PLUS1_AD_FLAG, isChecked);
            }
        });


        tnkBanner = findViewById(R.id.tnk_banner);
        manplusBanner = findViewById(R.id.manplus_banner);
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
        //bannerAdView.loadAd(TnkSession.CPC, BannerAdType.LANDSCAPE); // or bannerAdView.loadAd(TnkSession.CPC, BannerAdType.LANDSCAPE)

        manPlusBannerAdcreateBannerXMLMode();

        radioGroup = findViewById(R.id.graph_period);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButton_day) {
                    graphPeriod = CommonUtil.CRITERIA_DAY;
                } else if (checkedId == R.id.radioButton_month) {
                    graphPeriod = CommonUtil.CRITERIA_MONTH;
                }

                chart.startDataAnimation();
                generateValues();
                generateData();

            }
        });

        /*adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_plus1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_help:
                CommonUtil.showSupport(this, true);
                break;
            case R.id.action_info:
                Intent intentInfo = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.plus1_about_uri)));
                startActivity(intentInfo);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void setPlus1UIData() {
        //iv_plus1.startAnimation(anim);

        int attempt = Integer.parseInt(tv_attempt.getText().toString());
        attempt++;
        tv_attempt.setText(String.valueOf(attempt));

        dbAdapter.setPlus1();
        try {
            soundPool.play(soundId, 1, 1, 0, 0, 1);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        Plus1 plus1 = randomNumbersTab[0][numberOfPoints-1];
        int count = plus1.getCount();
        plus1.setCount(++count);
        //Log.i(getClass().getName(), "PLUS1 onClick day="+plus1.getDay()+" count="+plus1.getCount());
        randomNumbersTab[0][numberOfPoints-1] = plus1;
        generateData();
        tv_count.setText(String.valueOf(count));

        chart.startDataAnimation();

        //requestNewInterstitial();


        showImageDialog();
    }

    private void setPlus1Timer() {

        Log.i(getClass().getName(), "PLUS1 lastTime="+lastTime+" TIMER="+TIMER+", plus1Timer="+Applications.preference.getValue(Preference.PLUS1_TIMER, "60"));

        iv_plus1.setImageResource(R.drawable.heart_grey);

        timer = new Timer(true);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();

                Log.i(getClass().getName(), "PLUS1 in timer");

                remainTime = TIMER - (currentTime - lastTime);


                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                iv_plus1.setImageResource(R.drawable.heart_grey);
                                tv_remain.setText(String.valueOf((int)(remainTime/1000)));
                            }
                        });
                    }
                }).start();*/

                if ((currentTime - lastTime) > TIMER) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(getClass().getName(), "PLUS1 change heart image");
                                    iv_plus1.setImageResource(R.drawable.heart);
                                    tv_remain.setText("0");

                                    timer.cancel();
                                    Log.i(getClass().getName(), "PLUS1 call timer.cancel()");
                                }
                            });
                        }
                    }).start();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_remain.setText(String.valueOf((int)(remainTime/1000)));
                                }
                            });
                        }
                    }).start();
                }
            }

            @Override
            public boolean cancel() {
                // cancel이 call되지 않고 있음, 나중에 검토
                return super.cancel();
            }
        };
        timer.schedule(timerTask, 0, 1000);

        /*if (timer == null) {

        } else {
            timer.schedule(timerTask, 0, 1000);
        }*/
    }

    private void generateValues() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf;
        if (graphPeriod == CommonUtil.CRITERIA_DAY) {
            sdf = new SimpleDateFormat("yyyy.MM.dd");
        } else {
            sdf = new SimpleDateFormat("yyyy.MM");
        }
        String day = sdf.format(date);

        List<Plus1> plus1LinkedList = dbAdapter.getPlus1List(day, numberOfPoints-1, graphPeriod);
        Plus1 todayPlus1;
        todayPlus1 = plus1LinkedList.get(numberOfPoints-1);
        tv_selected_day.setText(day);
        tv_attempt.setText(String.valueOf(todayPlus1.getCount()));
        tv_count.setText(String.valueOf(todayPlus1.getCount()));

        for (int i = 0; i < maxNumberOfLines; ++i) {
            for (int j = 0; j < numberOfPoints; ++j) {
                //Log.i(getClass().getName(), "PLUS1 data list: "+i+" "+j+" day="+plus1LinkedList.get(j).getDay()+" count="+plus1LinkedList.get(j).getCount());
                Plus1 plus1 = new Plus1();
                plus1.setDay(plus1LinkedList.get(j).getDay());
                plus1.setCount(plus1LinkedList.get(j).getCount());
                randomNumbersTab[i][j] = plus1;
            }
        }
    }

    private void generateData() {

        List<Line> lines = new ArrayList<Line>();
        Plus1 plus1;

        for (int i = 0; i < numberOfLines; ++i) {

            List<PointValue> values = new ArrayList<PointValue>();
            for (int j = 0; j < numberOfPoints; ++j) {
                plus1 = randomNumbersTab[i][j];
                /*String date = plus1.getDay().substring(8);
                Log.i(getClass().getName(), "PLUS1 date="+date+" (float)date="+Float.valueOf(date));*/
                values.add(new PointValue(j, plus1.getCount()));
                //values.add(new PointValue(Float.valueOf(date), plus1.getCount()));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLORS[i]);
            line.setShape(shape);
            line.setCubic(isCubic);
            line.setFilled(isFilled);
            line.setHasLabels(hasLabels);
            line.setHasLabelsOnlyForSelected(hasLabelForSelected);
            line.setHasLines(hasLines);
            line.setHasPoints(hasPoints);
            line.setStrokeWidth(1);
            line.setPointRadius(3);
            //line.setHasGradientToTransparent(hasGradientToTransparent);
            if (pointsHaveDifferentColor){
                //line.setPointColor(ChartUtils.COLORS[(i + 1) % ChartUtils.COLORS.length]);
                int color = getApplicationContext().getResources().getColor(R.color.colorAccent);
                line.setColor(color);
            }
            lines.add(line);
        }

        data = new LineChartData(lines);

        if (hasAxes) {
            Axis axisX = new Axis();
            Axis axisY = new Axis().setHasLines(true);
            if (hasAxesNames) {
                axisX.setName("");
                axisY.setName("횟수");
            }
            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);
        } else {
            data.setAxisXBottom(null);
            data.setAxisYLeft(null);
        }

        data.setBaseValue(Float.NEGATIVE_INFINITY);
        chart.setLineChartData(data);
    }

    private void toggleLabelForSelected() {
        hasLabelForSelected = !hasLabelForSelected;

        chart.setValueSelectionEnabled(hasLabelForSelected);

        if (hasLabelForSelected) {
            hasLabels = false;
        }

        generateData();
    }

    private void prepareDataAnimation() {
        for (Line line : data.getLines()) {
            for (PointValue value : line.getValues()) {
                // Here I modify target only for Y values but it is OK to modify X targets as well.
                value.setTarget(value.getX(), (float) 0);
            }
        }
    }

    private class ValueTouchListener implements LineChartOnValueSelectListener {

        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
            //Toast.makeText(getApplicationContext(), "Selected: " + value, Toast.LENGTH_SHORT).show();
            tv_selected_day.setText(randomNumbersTab[lineIndex][pointIndex].getDay());
            tv_count.setText(String.valueOf((int)value.getY()));
        }

        @Override
        public void onValueDeselected() {
            // TODO Auto-generated method stub

        }

    }

    private void showImageDialog() {

        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(builder.getWindow()).setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (adSwitch.isChecked()) {
                    //lastTime = Applications.ePreference.getLastTimeForPlus1();

                    Applications.mobAdCnt++;
                    if( Applications.mobAdCnt > 10){
                        Applications.mobAdCnt = 0;
                    }
                    /*if (Applications.mInterstitialAd.isLoaded() *//*&& Applications.mobAdCnt%2==1*//*) {
                        Applications.mInterstitialAd.show();
                        aBack();
                    } else {
                        aBack();
                    }*/

                    manPlusInterstitialView(1413, 31780, 803854);

                    //requestPutPlus1GP();      // 전면 광고가 정상적으로 출력되었을 루틴에서 처리했으나 여러번 호출됨.

                    lastTime = System.currentTimeMillis();
                    Applications.ePreference.putLastTimeForPlus1(lastTime);

                    setPlus1Timer();
                }
            }
        });
        ImageView imageView = new ImageView(this);
        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        //imageView.setImageResource(R.drawable.thumbsup2);
        int imageId = new Random().nextInt(4);
        Glide.with(this).load(imageArray[imageId])
                .apply(new RequestOptions().placeholder(R.drawable.thumbsup2)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true))
                .into(imageView);

        //imageView.startAnimation(scaleAnimationanim);


        builder.show();

        /*AlertDialog.Builder alertadd = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.dialog_confirm_imageview, null);
        alertadd.setView(view);
        alertadd.setNeutralButton("확인!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int sumthin) {

            }
        });

        alertadd.show();*/
    }

    public void aBack(){
        /*setResult(CommonUtil.ACTIVITY_RESULT_HISTORY);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);*/
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
        } else {
            Log.e(getClass().getName(), "admob intersitial ad wasn't loaded yet");
        }
    }
*/

    @Override
    public void onTaskComplete(String result) {
        Log.i(getClass().getName(), "start onTaskComplete action");
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
            Log.i(getClass().getName(), "onTaskComplete action="+action);

            if (error != null && error.isEmpty() && action != null && !action.isEmpty()) {
                switch (action) {
                    case CommonUtil.ACTION_H2G_PUT_PLUS1_GP:
                        /*String budget = jo.getString(CommonUtil.RESULT_BUDGET);
                        Double totalGpoint = Double.parseDouble(budget);
                        String balance = jo.getString(CommonUtil.RESULT_BALANCE_GPOINT);
                        Double balanceGpoint = Double.parseDouble(balance);
                        Integer myGpoint = Integer.parseInt(jo.getString(CommonUtil.RESULT_H2G_MYPOINT));

                        Applications.ePreference.putTotalGpoint(totalGpoint);
                        Applications.ePreference.putBalanceGpoint(balanceGpoint);
                        Applications.ePreference.put(EPreference.N_MY_GPOINT, myGpoint);*/
                        String plus1Timer = jo.getString((CommonUtil.RESULT_PLUS1_TIMER));
                        Applications.preference.put(Preference.PLUS1_TIMER, plus1Timer);

                        TIMER =  (Integer.parseInt(plus1Timer) < 60)? (60 * 1000) : (Integer.parseInt(plus1Timer) * 1000);

                        Toast.makeText(getApplicationContext(), R.string.gp_1plus_toast_msg, Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
            } else if( error != null && error.equals(CommonUtil.ERROR_NO_USER)){
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
            //HideLoadingProgress();
            //hideRefreshIconAnimation();
        } catch (Exception ignore) {
        }

    }

    public synchronized void requestPutPlus1GP() {
        Applications.isHomeRefresh = false;
        HashMap<String, String> map = new HashMap<>();
        map.put(CommonUtil.KEY_USERID, Applications.preference.getValue(Preference.USER_ID, ""));
        map.put(CommonUtil.KEY_ADID, Applications.preference.getValue(Preference.AD_ID, ""));
        map.put(CommonUtil.KEY_DEVICE_TOKEN, Applications.preference.getValue(Preference.DEVICE_TOKEN, ""));
        map.put(CommonUtil.KEY_PHONE_NM, Applications.preference.getValue(Preference.PHONE_NM, ""));
        int version = CommonUtil.getVersionCode(this);
        map.put(CommonUtil.KEY_NAME, version + "");
        map.put(CommonUtil.KEY_ACTION, CommonUtil.ACTION_H2G_PUT_PLUS1_GP);
        String param = APICrypto.getParam(this, map, CommonUtil.SHARED_KEY);
        requestAsyncTask(param, CommonUtil.ACTION_H2G_PUT_PLUS1_GP);
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

        FileCacheFactory.initialize(this);
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

        Intent intent = new Intent(this, SignActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    void initAlarmChannel() {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            Objects.requireNonNull(notificationManager).createNotificationChannelGroup(new NotificationChannelGroup(CommonUtil.CHANNEL_GROUP_HABIT_ID, CommonUtil.CHANNEL_GROUP_HABIT));
            Objects.requireNonNull(notificationManager).createNotificationChannelGroup(new NotificationChannelGroup(CommonUtil.CHANNEL_GROUP_GPOINT_ID, CommonUtil.CHANNEL_GROUP_GPOINT));

            NotificationChannel mChannelHabit = new NotificationChannel(CommonUtil.CHANNEL_ID_HABIT, CommonUtil.CHANNEL_NAME_HABIT, importance);
            mChannelHabit.setGroup(CommonUtil.CHANNEL_GROUP_HABIT_ID);
            Objects.requireNonNull(notificationManager).createNotificationChannel(mChannelHabit);

            NotificationChannel mChannelGoodsaying = new NotificationChannel(CommonUtil.CHANNEL_ID_GOOD_SAYING, CommonUtil.CHANNEL_NAME_GOOD_SAYING, importance);
            mChannelGoodsaying.setGroup(CommonUtil.CHANNEL_GROUP_HABIT_ID);
            Objects.requireNonNull(notificationManager).createNotificationChannel(mChannelGoodsaying);

            NotificationChannel mChannelNotice = new NotificationChannel(CommonUtil.CHANNEL_ID_NOTICE, CommonUtil.CHANNEL_NAME_NOTICE, importance);
            mChannelHabit.setGroup(CommonUtil.CHANNEL_GROUP_HABIT_ID);
            Objects.requireNonNull(notificationManager).createNotificationChannel(mChannelNotice);

            NotificationChannel mChannelGpoint = new NotificationChannel(CommonUtil.CHANNEL_ID_REWARD_GPOINT, CommonUtil.CHANNEL_NAME_REWARD_GPOINT, importance);
            mChannelGpoint.setGroup(CommonUtil.CHANNEL_GROUP_GPOINT_ID);
            Objects.requireNonNull(notificationManager).createNotificationChannel(mChannelGpoint);

            NotificationChannel mChannelTrophy = new NotificationChannel(CommonUtil.CHANNEL_ID_REWARD_TROPHY, CommonUtil.CHANNEL_NAME_REWARD_TROPHY, importance);
            mChannelTrophy.setGroup(CommonUtil.CHANNEL_GROUP_GPOINT_ID);
            Objects.requireNonNull(notificationManager).createNotificationChannel(mChannelTrophy);

            NotificationChannel mChannelPurchase = new NotificationChannel(CommonUtil.CHANNEL_ID_PURCHASE, CommonUtil.CHANNEL_NAME_PURCHASE, importance);
            mChannelPurchase.setGroup(CommonUtil.CHANNEL_GROUP_GPOINT_ID);
            Objects.requireNonNull(notificationManager).createNotificationChannel(mChannelPurchase);
        }
    }

    private void manPlusInterstitialView(int p, int m, int s) {
        if (m_interView == null) {
            m_interView = new AdInterstitialView(MainActivity.this);
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

                }

                @Override
                public void onAdClick(View view) {

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
                requestPutPlus1GP();
                break;
            case AdInfoKey.AD_ID_NO_AD:
                log = "[ " + errorCode + " ] " + "광고 소진";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {

                    }

                    @Override
                    public void onShow() {
                        requestPutPlus1GP();
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(MainActivity.this);
                break;
            case AdInfoKey.NETWORK_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)네트워크";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {

                    }

                    @Override
                    public void onShow() {
                        requestPutPlus1GP();
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(MainActivity.this);
                break;
            case AdInfoKey.AD_SERVER_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)서버";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {

                    }

                    @Override
                    public void onShow() {
                        requestPutPlus1GP();
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(MainActivity.this);
                break;
            case AdInfoKey.AD_API_TYPE_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)API 형식 오류";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {

                    }

                    @Override
                    public void onShow() {
                        requestPutPlus1GP();
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(MainActivity.this);
                break;
            case AdInfoKey.AD_APP_ID_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)APP_ID 오류";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {

                    }

                    @Override
                    public void onShow() {
                        requestPutPlus1GP();
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(MainActivity.this);
                break;
            case AdInfoKey.AD_WINDOW_ID_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)WINDOW_ID 오류";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {

                    }

                    @Override
                    public void onShow() {
                        requestPutPlus1GP();
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(MainActivity.this);
                break;
            case AdInfoKey.AD_ID_BAD:
                log = "[ " + errorCode + " ] " + "(ERROR)ID 오류";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {

                    }

                    @Override
                    public void onShow() {
                        requestPutPlus1GP();
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(MainActivity.this);
                break;
            case AdInfoKey.AD_CREATIVE_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)광고 생성 불가";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {

                    }

                    @Override
                    public void onShow() {
                        requestPutPlus1GP();
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(MainActivity.this);
                break;
            case AdInfoKey.AD_ETC_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)예외 오류";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {

                    }

                    @Override
                    public void onShow() {
                        requestPutPlus1GP();
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(MainActivity.this);
                break;
            case AdInfoKey.CREATIVE_FILE_ERROR:
                log = "[ " + errorCode + " ] " + "(ERROR)파일 형식";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {

                    }

                    @Override
                    public void onShow() {
                        requestPutPlus1GP();
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(MainActivity.this);
                break;
            case AdInfoKey.AD_INTERVAL:
                log = "[ " + errorCode + " ] " + "광고 요청 어뷰징";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {

                    }

                    @Override
                    public void onShow() {
                        requestPutPlus1GP();
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(MainActivity.this);
                break;
            case AdInfoKey.AD_TIMEOUT:
                log = "[ " + errorCode + " ] " + "광고 API TIME OUT";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {

                    }

                    @Override
                    public void onShow() {
                        requestPutPlus1GP();
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(MainActivity.this);
                break;
            case AdInfoKey.NOT_SUPPORT_VERSION:
                log = "[ " + errorCode + " ] " + "광고 NOT SUPPORT VERSION";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {

                    }

                    @Override
                    public void onShow() {
                        requestPutPlus1GP();
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(MainActivity.this);
                break;
            case AdInfoKey.NOT_UNZIP_TIMEOUT:
                log = "[ " + errorCode + " ] " + "광고 NOT UNZIP TIMEOUT";
                TnkSession.prepareInterstitialAd(this, TnkSession.CPC, new TnkAdListener() {
                    @Override
                    public void onClose(int i) {

                    }

                    @Override
                    public void onShow() {
                        requestPutPlus1GP();
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(getClass().getName(), "TNK interstitial Ad fail: " + i);
                    }

                    @Override
                    public void onLoad() {

                    }
                });
                TnkSession.showInterstitialAd(MainActivity.this);
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

    private void manPlusEndingView(final int p, final int m, final int s, int media) {
        mEndingDialog = new EndingDialog(MainActivity.this, getResources().getString(R.string.ending_dialog_title), leftClickListener2, rightClickListener2);
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
