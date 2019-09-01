package kr.co.gubed.habit2good;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mapps.android.share.AdInfoKey;
import com.mapps.android.view.AdInterstitialView;
import com.mz.common.listener.AdListener;
import com.tnkfactory.ad.TnkAdListener;
import com.tnkfactory.ad.TnkSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import kr.co.gubed.habit2good.gpoint.listener.AsyncTaskCompleteListener;
import kr.co.gubed.habit2good.gpoint.util.APICrypto;
import kr.co.gubed.habit2good.gpoint.util.Applications;
import kr.co.gubed.habit2good.gpoint.util.AsyncHTTPPost;
import kr.co.gubed.habit2good.gpoint.util.CommonUtil;
import kr.co.gubed.habit2good.gpoint.util.Preference;
import kr.co.gubed.habit2good.gpoint.view.LoadingDialog;

public class GpointRankingBoardActivity extends AppCompatActivity implements AsyncTaskCompleteListener<String> {
    private GpointRankingBoardAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<JSONObject> mRankingSet;
    private ArrayList<JSONObject> mTableSet;
    private static ItemTouchHelper touchHelper;
    private String mCpId;
    private final int mSerchingRange = 50;

    private ImageView mIvMyRanking;
    private ImageView mIvTopRanking;
    private EditText mEtUserId;
    private ImageView mIvSerching;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean swipeRefreshFlag = false;

    private LoadingDialog loadingDialog;

    private SearchView searchView;
    private ArrayList<String> tableList;
    private ArrayList<String> tableDisplay;
    private String rankingTable;
    TextView tvTableName;
    ArrayAdapter adapter;

    Toolbar toolbar;
    ActionBar actionbar;

    private AdInterstitialView m_interView = null;
    private Handler handler = new Handler();

    public GpointRankingBoardActivity() {
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpoint_ranking_board);

        RecyclerView mRecyclerView = findViewById(R.id.recyclerView);

        mRankingSet = new ArrayList<>();
        mTableSet = new ArrayList<>();
        mCpId = Applications.preference.getValue(Preference.CPID, "");

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new GpointRankingBoardAdapter(this, mRankingSet);
        mRecyclerView.setAdapter(mAdapter);

        /*ItemTouchHelper.Callback callback = new ItemTouchHelperCallbackForReminder(mAdapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);*/

        //Toolbar 의 왼쪽에 버튼을 추가하고 버튼의 아이콘을 바꾼다.
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.mipmap.outline_keyboard_arrow_left_white_18);
        actionbar.setTitle(R.string.activity_ranking_board);


        /*ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.mipmap.outline_keyboard_arrow_left_white_18);
        actionBar.setTitle("랭킹 보드");*/

        init();
        requestRankingInfo(0, 0, mCpId);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    void init() {
        Spinner spinTableList = findViewById(R.id.spinner_table_list);

        tableList = new ArrayList<>();
        tableDisplay = new ArrayList<>();
        rankingTable = "";

        adapter = new ArrayAdapter(getApplicationContext(), R.layout.spin, tableDisplay);
        adapter.setDropDownViewResource(R.layout.spin_dropdown);
        spinTableList.setAdapter(adapter);

        tvTableName = findViewById(R.id.tv_table_name);

        spinTableList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //rankingTable = parent.getSelectedItem().toString();
                rankingTable = tableList.get(position);
                //Log.i(getClass().getName(), "onItemSelected, rankingTable="+rankingTable);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                rankingTable = "";
            }
        });

        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    if (mRankingSet.size() == 0) {
                        return;
                    }
                    JSONObject gpointRankingItem = mRankingSet.get(0);
                    Integer mRanking = gpointRankingItem.getInt("r");
                    Integer startRanking = mRanking - mSerchingRange;
                    if (startRanking < 1) startRanking = 1;
                    Integer endRanking = mRanking - 1;
                    if (startRanking < 1) {
                        return;
                    }
                    requestRankingInfo(startRanking, endRanking, "");
                    swipeRefreshFlag = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public synchronized void requestRankingInfo(Integer start, Integer end, String cpId) {
        HashMap<String, String> map = new HashMap<>();
        map.put(CommonUtil.KEY_USERID, Applications.preference.getValue(Preference.USER_ID, ""));
        map.put(CommonUtil.KEY_RANKING_START, start.toString());
        map.put(CommonUtil.KEY_RANKING_END, end.toString());
        map.put(CommonUtil.KEY_CPID, cpId);
        map.put(CommonUtil.KEY_TABLE_NAME, rankingTable);
        int version = CommonUtil.getVersionCode(this);
        map.put(CommonUtil.KEY_NAME, version + "");
        map.put(CommonUtil.KEY_ACTION, CommonUtil.ACTION_H2G_GET_GPOINT_RANKING);
        String param = APICrypto.getParam(this, map, CommonUtil.SHARED_KEY);
        requestAsyncTask(param, CommonUtil.ACTION_H2G_GET_GPOINT_RANKING);

        Log.i(getClass().getName(), "requestRankingInfo cpId="+cpId);
    }

    public void requestAsyncTask(String param, String action) {
        Log.i(getClass().getName(), "requestAsyncTask action="+action);
        if (action.equals(CommonUtil.ACTION_POP_LINKED)) {
            new AsyncHTTPPost(this).execute(CommonUtil.SERVER_URL + "?_z=" + Math.random(), param, action);
        } else {
            if (Applications.getCountry(this).equals("KR") && !Applications.isRoaming(this)) {
                ShowLoadingProgress();
                new AsyncHTTPPost(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, CommonUtil.SERVER_URL, param, action);
            } else {
                new AsyncHTTPPost(this).execute(CommonUtil.SERVER_URL, param, action);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ranking_board, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.hint_userid));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                requestRankingInfo(0, 0, s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                /*finish();
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);*/
                onBackPressed();
                return true;
            case R.id.action_my_position:
                mCpId = Applications.preference.getValue(Preference.CPID, "");
                requestRankingInfo(0, 0, mCpId);
                return true;
            case R.id.action_top:
                requestRankingInfo(1, mSerchingRange, "");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        /*TNK
        TnkSession.showInterstitialAd(this, 1000, new TnkAdListener() {
            @Override
            public void onClose(int i) {
                finish();
            }

            @Override
            public void onShow() {

            }

            @Override
            public void onFailure(int i) {
                Log.e(getClass().getName(), "TNK interstitial showing fail: "+i);
                finish();
            }

            @Override
            public void onLoad() {

            }
        });*/
        /* ManPlus */
        manPlusInterstitialView(1413, 31780, 803854);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
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
            Log.i(getClass().getName(), "onTaskComplete action=" + action);

            if (error != null && error.isEmpty() && action != null && !action.isEmpty()) {
                switch (action) {
                    case CommonUtil.ACTION_H2G_GET_GPOINT_RANKING:
                        JSONArray rankingArray = jo.getJSONArray("rank");
                        if (rankingArray != null && rankingArray.length() > 0) {
                            ArrayList<JSONObject> mTmpRankingSet = new ArrayList<>();
                            if (swipeRefreshFlag == false) {
                                mRankingSet.clear();
                            }
                            for (int i = 0; i < rankingArray.length(); i++) {
                                JSONObject item = rankingArray.getJSONObject(i);
                                if (swipeRefreshFlag == true) {
                                    mTmpRankingSet.add(item);
                                } else {
                                    mRankingSet.add(item);
                                }
                            }
                            if (swipeRefreshFlag == true) {
                                mTmpRankingSet.addAll(mRankingSet);
                                mRankingSet.clear();
                                mRankingSet.addAll(mTmpRankingSet);
                                mTmpRankingSet.clear();
                            }
                            mAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "데이터가 없습니다", Toast.LENGTH_LONG).show();
                        }
                        // spinner(ranking table) 값 초기화
                        JSONArray tableArray = jo.getJSONArray("tl");
                        if (tableArray != null && tableArray.length() > 0) {
                            tableList.clear();
                            tableDisplay.clear();
                            for (int i=0; i<tableArray.length(); i++) {
                                JSONObject tableName = tableArray.getJSONObject(i);
                                String tname = tableName.getString("t");
                                if (!tname.equals("0")) {
                                    tableList.add(tname);
                                    String displayName = convertDisplayName(tname);
                                    tableDisplay.add(displayName);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                        break;
                    default:
                        break;
                }
            } else  if (error.equals("no_user")) {
                Toast.makeText(this, "없는 가입자입니다. 다시 입력해 주세요.", Toast.LENGTH_LONG).show();
            } else if (error.equals("no_data")) {
                //Toast.makeText(this, "데이터가 없습니다. 확인 바랍니다.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "랭킹 조회를 실패했습니다.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            try {
                //hideRefreshIconAnimation();
            } catch (Exception ignore) {
            }
            e.printStackTrace();
        } finally {
            try {
                mSwipeRefreshLayout.setRefreshing(false);
                swipeRefreshFlag = false;
                HideLoadingProgress();;
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void onTaskError(String param, String action, String result) {
        try {
            mSwipeRefreshLayout.setRefreshing(false);
            swipeRefreshFlag = false;
            HideLoadingProgress();;
        } catch (Exception ignore) {
        }
    }

    String convertDisplayName(String name) {
        String displayName="종합 순위";

        if (name.equals("ranking_board")) {
            displayName = "종합 순위";
        } else if (name.equals("ranking_board_month")) {
            displayName = "월간 순위";
        } else if (name.equals("ranking_board_week")) {
            displayName = "주간 순위";
        } else {
            Log.d(getClass().getName(), "unknown ranking table="+name);
        }
        return displayName;
    }

    public void ShowLoadingProgress() {
        //show loading
        Log.i(getClass().getName(), "ShowLoadingProgress start");

        try {
            if (loadingDialog == null) {
                loadingDialog = new LoadingDialog(this);
            }
            this.runOnUiThread(new Runnable() {
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
        this.runOnUiThread(new Runnable() {
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

    private void manPlusInterstitialView(int p, int m, int s) {
        if (m_interView == null) {
            m_interView = new AdInterstitialView(GpointRankingBoardActivity.this);
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
