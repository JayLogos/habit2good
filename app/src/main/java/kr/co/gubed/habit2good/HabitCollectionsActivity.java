package kr.co.gubed.habit2good;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import kr.co.gubed.habit2good.gpoint.activity.SettingActivity;
import kr.co.gubed.habit2good.gpoint.activity.SignActivity;
import kr.co.gubed.habit2good.gpoint.util.Applications;
import kr.co.gubed.habit2good.gpoint.util.CommonUtil;
import kr.co.gubed.habit2good.gpoint.util.Preference;
import kr.co.gubed.habit2good.gpoint.view.LoadingDialog;


public class HabitCollectionsActivity extends AppCompatActivity {
    public static ArrayList<HashMap<String, String>> habitList;
    public static HabitItemAdapter adapter;
    private BackupDbOnGoogleDrive dbBackup = new BackupDbOnGoogleDrive(this);

    private static final String TAG_HABITID="habitid";
    private static final String TAG_POSITION="position";
    private static final String TAG_HNAME="hname";
    private static final String TAG_GOALIMG="goalimg";
    private static final String TAG_SDATE="sdate";
    private static final String TAG_EDATE="edate";
    private static final String TAG_GOAL="goal";
    private static final String TAG_SIGNAL="signal";
    private static final String TAG_REWARD="reward";
    private static final String TAG_CATEGORY="category";
    private static final String TAG_CYCLE="cycle";
    private static final String TAG_COUNT="count";
    private static final String TAG_UNIT="unit";
    private static final String TAG_REMINDER="reminder";

    private HabitDbAdapter dbAdapter;

    private FloatingActionButton fab;

    private static ItemTouchHelper touchHelper;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private LoadingDialog loadingDialog;

    Toolbar toolbar;
    ActionBar actionbar;

    private SoundPool soundPool;
    private int soundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_collections);

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();

        adapter.notifyDataSetChanged();

        try {
            HideLoadingProgress();
        } catch (Exception ignore) {
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_habits, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_refresh:
                habitList.removeAll(habitList);
                makeListFromDB();
                adapter.notifyDataSetChanged();
                initReminder();
                break;
            case R.id.action_info:
                Intent intentInfo = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.habit_about_uri)));
                startActivity(intentInfo);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.action_backup:
                signInAndBackup(CommonUtil.REQUEST_CODE_SIGN_IN_FOR_BACKUP);
                break;
            case R.id.action_restore:
                signInAndBackup(CommonUtil.REQUEST_CODE_SIGN_IN_FOR_RESTORE);
                break;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingActivity.class);
                this.startActivity(intent);
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                break;
            case R.id.nav_all:
                habitList.removeAll(habitList);
                makeListFromDB();
                adapter.notifyDataSetChanged();
                item.setChecked(true);
                break;
            case R.id.nav_ongoing:
                habitList.removeAll(habitList);
                makeListFromDB();
                notifyDataSetChangedOngoing();
                adapter.notifyDataSetChanged();
                item.setChecked(true);
                break;
            case R.id.nav_expired:
                habitList.removeAll(habitList);
                makeListFromDB();
                notifyDataSetChangedExpired();
                adapter.notifyDataSetChanged();
                item.setChecked(true);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CommonUtil.REQUEST_CODE_SIGN_IN_FOR_BACKUP:
            case CommonUtil.REQUEST_CODE_SIGN_IN_FOR_RESTORE:
                Task<GoogleSignInAccount> getAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
                Log.i(getClass().getName(), "SIGNLOG H2G A");
                try {
                    Log.i(getClass().getName(), "SIGNLOG H2G A-1");
                    GoogleSignInAccount account = getAccountTask.getResult(ApiException.class);
                    Log.i(getClass().getName(), "SIGNLOG H2G B");
                    firebaseAuthWithGoogle(account, requestCode);
                } catch (ApiException e){
                    Toast.makeText(this, "구글 인증을 실패했습니다.", Toast.LENGTH_LONG).show();
                    Log.e(getClass().getName(), "SIGNLOG Google sign in failed: "+e.getMessage()+"-"+e.getStatusCode());
                }
                break;
            case CommonUtil.REQUEST_CODE_HABIT_ADD:
            case CommonUtil.REQUEST_CODE_HABIT_UPDATE:
                Log.i(getClass().getName(), "recv REQUEST_CODE_HABIT_ADD or REQUEST_CODE_HABIT_UPDATE");
                habitList.removeAll(habitList);
                makeListFromDB();
                adapter.notifyDataSetChanged();
                break;
            case CommonUtil.REQUEST_CODE_HABIT_REMINDER:
                habitList.removeAll(habitList);
                makeListFromDB();
                adapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    void init() {

        /*
         * Firebase 인증 초기화
         */
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.my_default_web_client_id))
                        .requestScopes(Drive.SCOPE_FILE)
                        .requestEmail()
                        .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.habits_toolbar);
        setSupportActionBar(toolbar);
        actionbar = getSupportActionBar();
        actionbar.setTitle(R.string.btn_nav_habits);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(8).build();
            soundId = soundPool.load(this, R.raw.habits_signal3, 1);
        } else {
            soundPool = new SoundPool(8, AudioManager.STREAM_MUSIC, 1);
            soundId = soundPool.load(this, R.raw.habits_signal3, 1);
        }

        habitList = new ArrayList<HashMap<String, String>>();
        adapter = new HabitItemAdapter(this, habitList, soundPool, soundId);

        RecyclerView recyclerView = findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
        //callback.makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        ItemTouchHelper.Callback.makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                makeListFromDB();

                //adapter.notifyDataSetChanged(); // TBD 필요한 상황인지 확인 하자
                //adapter.notifyItemRangeChanged(0, adapter.getItemCount());
            }
        }).start();


        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddHabit.class);
                startActivityForResult(intent, CommonUtil.REQUEST_CODE_HABIT_ADD);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            /*
                if (newState == rv.SCROLL_STATE_IDLE) {
                    fab.show(); fab 때문에 Item 확장 버튼을 선택하기 어렵기 때문에 삭제
                }
                */
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0 && !fab.isShown()) {
                    fab.show();
                } else if (dy > 0 && fab.isShown()) {
                    fab.hide();
                }
            }
        });

        LinearLayout ll_habit_collections = findViewById(R.id.ll_habit_collections);
        ll_habit_collections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TBD: Habit collections Activity 실행
                Intent intent = new Intent(getApplicationContext(), AddHabit.class);
                startActivityForResult(intent, CommonUtil.REQUEST_CODE_HABIT_ADD);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
            }
        });
    }

    private void signInAndBackup(int requestCode) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, requestCode);
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct, final int requestCode) {
        Log.d(getClass().getName(), "SIGNLOG firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(getClass().getName(), "signInWithCredential:success");
                            //FirebaseUser user = mAuth.getCurrentUser();
                            dbBackup.initializeDriveClient(requestCode);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log. e(getClass().getName(), "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "구글 인증을 실패했습니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void makeListFromDB() {
        List<Habit> habitLinkedList;

        dbAdapter = new HabitDbAdapter(this);
        dbAdapter.open();

        habitLinkedList = dbAdapter.getHabitList("position");
        Log.i("makeListFromDB", "Habit count = " + habitLinkedList.size());

        for (int i=0; i<habitLinkedList.size() ; i++) {
            HashMap<String, String> items = new HashMap<String, String>();
            Habit habit = habitLinkedList.get(i);

            //items.put(TAG_TITLE, String.format("Habit %d", i));
            items.put(TAG_HABITID, habit.getHabitid().toString());
            items.put(TAG_POSITION, habit.getPosition().toString());
            items.put(TAG_HNAME, habit.getHname());
            items.put(TAG_GOALIMG, habit.getGoalimg());
            items.put(TAG_SDATE, habit.getSdate());
            items.put(TAG_EDATE, habit.getEdate());
            items.put(TAG_GOAL, habit.getGoal());
            items.put(TAG_SIGNAL, habit.getSignal());
            items.put(TAG_REWARD, habit.getReward());
            items.put(TAG_CATEGORY, habit.getCategory());
            items.put(TAG_CYCLE, habit.getCycle());
            items.put(TAG_COUNT, habit.getCount().toString());
            items.put(TAG_UNIT, habit.getUnit());
            items.put(TAG_REMINDER, dbAdapter.getCountOfReminder(habit.getHabitid()).toString());
            Log.i("makeListFromDB", "title = " + items.get(TAG_HNAME) + ", habitid="+items.get(TAG_HABITID));

            habitList.add(items);

            dbAdapter.setHabitItemPosition(habit.getHabitid(), i);

            //Log.i("After makeListFromDB", "title = " + items.get(TAG_HNAME) + ", habitid="+items.get(TAG_HABITID));
        }

        /*
            habitList 를 position 기준으로 Sorting
            ArrayList<HashMap<String, String>>
         */
        Collections.sort(habitList, new Comparator<HashMap<String, String>>() {
            @Override
            public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
                if (Integer.parseInt(Objects.requireNonNull(o1.get("position"))) > Integer.parseInt(Objects.requireNonNull(o2.get("position")))) {
                    return 1;
                } else if (Integer.parseInt(Objects.requireNonNull(o1.get("position"))) < Integer.parseInt(Objects.requireNonNull(o2.get("position")))) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
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

    void initReminder() {
        HabitReminderAdapter reminderAdapter;

        HabitDbAdapter dbAdapter = new HabitDbAdapter(getApplicationContext());
        List<HabitReminder> habitReminderLinkedList;
        reminderAdapter = new HabitReminderAdapter(getApplicationContext());

        dbAdapter.open();
        habitReminderLinkedList = dbAdapter.getHabitReminderList();

        for (int i = 0; i < habitReminderLinkedList.size(); i++) {
            HabitReminder habitReminder = habitReminderLinkedList.get(i);
            reminderAdapter.setAlarmReminderItem(habitReminder.mHabitId, habitReminder.mAlarmTime, habitReminder.mAlarmName, habitReminder.mAlarmState);
        }
        Log.i(getClass().getName(), "initReminder, reminder registration was completed");
    }

    private void notifyDataSetChangedOngoing() {
        HashMap<String, String> habitItem;
        Calendar today = Calendar.getInstance();
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");

        for (int i = 0; i < habitList.size(); i++) {
            habitItem = habitList.get(i);

            String sdate = habitItem.get("sdate");
            String edate = habitItem.get("edate");
            String thisDay = today.get(Calendar.YEAR) + "."
                    + String.format(Locale.getDefault(),"%02d", today.get(Calendar.MONTH) + 1)
                    + "." + String.format(Locale.getDefault(),"%02d", today.get(Calendar.DAY_OF_MONTH));
            Log.i(getClass().getName(), "sdate="+sdate+" edate="+edate+" today="+thisDay+" i="+i+" size="+habitList.size());
            if (thisDay.compareTo(Objects.requireNonNull(sdate)) >= 0 && thisDay.compareTo(Objects.requireNonNull(edate)) <= 0) {

            } else {
                habitList.remove(i);
                adapter.notifyItemRemoved(i);
                --i;
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void notifyDataSetChangedExpired() {
        HashMap<String, String> habitItem;
        Calendar today = Calendar.getInstance();
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");

        for (int i=0; i<habitList.size(); i++) {
            habitItem = habitList.get(i);

            String sdate = habitItem.get("sdate");
            String edate = habitItem.get("edate");
            String thisDay = today.get(Calendar.YEAR) + "."
                    + String.format(Locale.getDefault(),"%02d", today.get(Calendar.MONTH)+1)
                    + "." + String.format(Locale.getDefault(),"%02d", today.get(Calendar.DAY_OF_MONTH));
            Log.i(getClass().getName(), "sdate="+sdate+" edate="+edate+" today="+thisDay+" i="+i+" size="+habitList.size());
            if (thisDay.compareTo(Objects.requireNonNull(sdate)) >= 0 && thisDay.compareTo(Objects.requireNonNull(edate)) <= 0) {
                habitList.remove(i);
                adapter.notifyItemRemoved(i);
                --i;
            } else {

            }
        }
    }
}
