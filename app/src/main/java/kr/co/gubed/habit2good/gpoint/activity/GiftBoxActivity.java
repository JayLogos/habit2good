package kr.co.gubed.habit2good.gpoint.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapps.android.share.AdInfoKey;
import com.mapps.android.view.AdInterstitialView;
import com.mapps.android.view.AdView;
import com.mz.common.listener.AdListener;
import com.tnkfactory.ad.BannerAdListener;
import com.tnkfactory.ad.BannerAdType;
import com.tnkfactory.ad.BannerAdView;
import com.tnkfactory.ad.TnkAdListener;
import com.tnkfactory.ad.TnkSession;

import java.util.ArrayList;

import kr.co.gubed.habit2good.R;
import kr.co.gubed.habit2good.gpoint.listener.GiftBoxListener;
import kr.co.gubed.habit2good.gpoint.model.GiftBoxModel;
import kr.co.gubed.habit2good.gpoint.util.Applications;
import kr.co.gubed.habit2good.gpoint.util.CommonUtil;
import kr.co.gubed.habit2good.gpoint.util.GiftBoxAsyncTask;
import kr.co.gubed.habit2good.gpoint.view.LoadingDialog;
import kr.co.gubed.habit2good.gpoint.view.NetworkDialog;

public class GiftBoxActivity extends Activity implements View.OnClickListener, GiftBoxListener {
    private String TAG = this.getClass().toString();

    /* Ad */
    private LinearLayout type_admob;                // adMob
    private BannerAdView bannerAdView;              // TNK
    private RelativeLayout tnkBanner;
    private RelativeLayout manplusBanner;           // MANPLUS
    private AdView m_adView = null;
    private AdInterstitialView m_interView = null;
    private Handler handler = new Handler();

    private Button btn_back;
    private Button btn_info;
    private TextView tv_cnt;
    private ArrayList<GiftBoxModel> giftboxList;
    private WebView webView;

    private LoadingDialog loadingDialog;
    private NetworkDialog networkDialog;
    private StringBuilder str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gift_box);

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

        btn_back = (Button)findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);
        btn_info = (Button)findViewById(R.id.btn_info);
        btn_info.setOnClickListener(this);
        tv_cnt = (TextView)findViewById(R.id.tv_cnt);

        giftboxList = new ArrayList<>();

        webView = (WebView)findViewById(R.id.webView);
        webView.addJavascriptInterface(new GiftBoxActivity.CPIF(), "cpif");
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        webView.setWebViewClient(new GiftBoxActivity.CPWebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBlockNetworkLoads(false);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        int newCnt = Applications.dbHelper.getGiftBoxNewCnt();
        if( newCnt > 0){
            tv_cnt.setText(newCnt+"");
            tv_cnt.setVisibility(View.VISIBLE);
        }else{
            tv_cnt.setVisibility(View.GONE);
        }
        str = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        sb.append(this.getResources().getString(R.string.notice_top));
        sb.append(this.getResources().getString(R.string.notice_bottom));
        webView.loadDataWithBaseURL("file:///android_asset/", sb.toString(), "text/html", "utf-8", null);
    }

    @Override
    protected void onResume() {
        super.onResume();

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
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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
    public void onBackPressed() {
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
        }

        manPlusInterstitialView(1413, 31780, 803854);

        /*super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);*/
    }

    @Override
    protected void onDestroy() {
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
        }

        if (bannerAdView != null) {
            bannerAdView.onDestroy();
        }

        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_back:
                onBackPressed();
                break;
            case R.id.btn_info:
                CommonUtil.showSupport(GiftBoxActivity.this, true);
                break;
        }
    }

    @Override
    public void giftBoxListener(ArrayList<GiftBoxModel> giftBoxModels) {
        giftboxList = giftBoxModels;
        setList();
    }

    private class CPIF{
        @JavascriptInterface
        public void readGiftBox(final int idx){
            webView.post(new Runnable() {
                @Override
                public void run() {
                    if( giftboxList.get(idx).getIsRead().equals("new")){
                        Applications.isSettingNOticeRefresh = true;
                        giftboxList.get(idx).setIsRead("read");
                        Applications.dbHelper.readNewGiftBox(giftboxList.get(idx));
                    }
                    int newCnt = Applications.dbHelper.getGiftBoxNewCnt();
                    if( newCnt > 0){
                        tv_cnt.setText(newCnt+"");
                        tv_cnt.setVisibility(View.VISIBLE);
                    }else{
                        tv_cnt.setVisibility(View.GONE);
                    }

                    if( !giftboxList.get(idx).isSelect()){
                        for(int i=0;i<giftboxList.size();i++){
                            if( i != idx) {
                                giftboxList.get(i).setSelect(false);
                            }
                        }
                        String javascript = "javascript:$(\"#listview\").find(\"li\").removeClass(\"on\");$(\".content\").hide();$(\"#r_"+giftboxList.get(idx).getId()+"\").find(\".new\").hide();$(\"#r_"+giftboxList.get(idx).getId()+"\").addClass(\"on\");$(\"#r_"+giftboxList.get(idx).getId()+"\").find(\".content\").show();$('body').animate({scrollTop: $(\"#r_" + giftboxList.get(idx).getId() + "\").offset().top},300);";
                        Log.e("javascript",""+javascript);
                        webView.loadUrl(javascript);
                        giftboxList.get(idx).setSelect(true);
                    }else{
                        String javascript = "javascript:$(\"#listview\").find(\"li\").removeClass(\"on\");$(\".content\").hide();";
                        Log.e("javascript",""+javascript);
                        webView.loadUrl(javascript);
                        giftboxList.get(idx).setSelect(false);
                    }
                }
            });
        }
        @JavascriptInterface
        public void loadMore(){

        }
    }

    private class CPWebViewClient extends WebViewClient {
        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            if( uri.getScheme().equals("http") || uri.getScheme().equals("https")){
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                view.getContext().startActivity(intent);
                return true;
            }else{
                return super.shouldOverrideUrlLoading(view, request);
            }

        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri uri = Uri.parse(url);
            if( uri.getScheme().equals("http") || uri.getScheme().equals("https")){
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                view.getContext().startActivity(intent);
                return true;
            }else{
                return super.shouldOverrideUrlLoading(view, url);
            }
        }

        @Override
        public void onPageFinished(WebView view, final String url) {
            Log.e("onPageFinished","onPageFinished");
            super.onPageFinished(view, url);
            new GiftBoxAsyncTask(GiftBoxActivity.this, GiftBoxActivity.this).execute();
        }
    }

    public void setList(){
        if( str == null) {
            str = new StringBuilder();
        }else{
            str.setLength(0);
            str = new StringBuilder();
        }
        String newStr = this.getResources().getString(R.string.new_txt);
        String appendStr = "";
        if( !giftboxList.isEmpty() && giftboxList.size() > 0) {
            for (int i = 0; i < giftboxList.size(); i++) {
                appendStr = "<li id=r_" + giftboxList.get(i).getId() + " class=row>";
                appendStr += "<a href=\"javascript:window.cpif.readGiftBox(" + i + ");\">";
                appendStr += "<div class=title>";
                appendStr += giftboxList.get(i).getGiftTitle();
                appendStr += "</div>";

                appendStr += "<div class=date>";
                appendStr += CommonUtil.getDateTime(giftboxList.get(i).getRegdate());
                appendStr += "</div>";

                appendStr += "<div class=ico></div>";
                if (giftboxList.get(i).getIsRead().equals("new")) {
                    appendStr += "<div class=new>" + newStr + "</div>";
                } else {
                    appendStr += "<div class=new style=\"display:none;\">" + newStr + "</div>";
                }
                appendStr += "</a>";
                appendStr += "<div class=content>";
                appendStr += CommonUtil.addSlashes(giftboxList.get(i).getGiftContent());
                appendStr += "</div>";
                appendStr += "</li>";
                webView.loadUrl("javascript:if( $('#r_" + giftboxList.get(i).getId() + "').length == 0){ $('#listview').append('" + appendStr + "'); }");
            }
            webView.loadUrl("javascript:$(window).scroll(function(){if( $(window).scrollTop()==$(document).height()-$(window).height()){window.cpif.loadMore();}});$(window).ready(function(){if($('body').height()<$(window).height()){window.cpif.loadMore();}});");
        }else{
            appendStr = "<li class=row>";
            appendStr += "<div class=no_list>";
            appendStr += this.getResources().getString(R.string.no_gift);
            appendStr += "</div>";
            appendStr += "</li>";
            Log.e("appendStr",""+appendStr);
            webView.loadUrl("javascript:$('#listview').append('" + appendStr + "');");
        }

        HideLoadingProgress();
    }

    public void ShowLoadingProgress() {
        //show loading
        try {
            if( loadingDialog == null){
                loadingDialog = new LoadingDialog(GiftBoxActivity.this);
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
            m_interView = new AdInterstitialView(GiftBoxActivity.this);
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
                TnkSession.showInterstitialAd(GiftBoxActivity.this);
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
