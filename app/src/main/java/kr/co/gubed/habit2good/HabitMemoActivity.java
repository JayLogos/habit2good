package kr.co.gubed.habit2good;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class HabitMemoActivity extends AppCompatActivity {
    private HabitMemoAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<HashMap<String, String>> mMemoSet;
    private Integer habitid;
    private static ItemTouchHelper touchHelper;
    //Toolbar tb_memo;

    private static final String TAG_HABITID = "habitid";
    private static final String TAG_SELECTEDDAY = "selectedday";
    private static final String TAG_MEMO = "memo";

    private HabitDbAdapter dbAdapter;


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);
        dbAdapter = new HabitDbAdapter(this);
        dbAdapter.open();
        Habit habit;
        Intent intent = getIntent();
        habitid = Objects.requireNonNull(intent.getExtras()).getInt("habitid");
        RecyclerView mRecyclerView = findViewById(R.id.recyclerView);

        mMemoSet = new ArrayList<HashMap<String, String>>();

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new HabitMemoAdapter(this, mMemoSet, dbAdapter);
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallbackForReminder(mAdapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);

        //Toolbar 의 왼쪽에 버튼을 추가하고 버튼의 아이콘을 바꾼다.
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.mipmap.outline_keyboard_arrow_left_white_18);
        actionBar.setTitle("메모");

        //tb_memo = findViewById(R.id.toolbar);
        //tb_memo.setTitle("메모");
        //tb_memo.setTitleTextColor(Color.WHITE);
        //setSupportActionBar(tb_memo);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.mipmap.outline_keyboard_arrow_left_white_18);

        Log.i(getClass().getName(), "habitid="+habitid);

        /* Header */
        habit = dbAdapter.getHabit(habitid);
        if (habit != null) {
            ImageView mIvGoalImage;
            TextView mHname, mTvSdate, mTvEdate;
            mIvGoalImage = findViewById(R.id.iv_goalimg);
            mHname = findViewById(R.id.tv_hname);
            mTvSdate = findViewById(R.id.tv_sdate);
            mTvEdate = findViewById(R.id.tv_edate);
            if (habit.getGoalimg().equals("default")) {
                Glide.with(this).load(R.drawable.ic_habit2good_512)
                        .apply(new RequestOptions().circleCrop())
                        .into(mIvGoalImage);
            } else if (habit.getGoalimg().equals("example")) {
                Glide.with(this).load(R.drawable.habit_example)
                        .apply(new RequestOptions().circleCrop())
                        .into(mIvGoalImage);
            } else {
                Glide.with(this).load(habit.getGoalimg())
                        .apply(new RequestOptions().circleCrop())
                        .into(mIvGoalImage);
            }
            mHname.setText(habit.getHname());
            mTvSdate.setText(habit.getSdate());
            mTvEdate.setText(habit.getEdate());
        } else {
            //view 를 invisible 로 감추자
        }

        /* Body */
        makeMemoSet(habitid);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void makeMemoSet(Integer habitid) {
        List<HabitMemo> habitMemoList;

        habitMemoList = dbAdapter.getHabitMemoSet(habitid, "");

        for (int i=0; i<habitMemoList.size() ; i++) {
            HashMap<String, String> items = new HashMap<String, String>();
            HabitMemo habitMemo = habitMemoList.get(i);

            items.put(TAG_HABITID, habitMemo.getHabitid().toString());
            items.put(TAG_SELECTEDDAY, habitMemo.getSelectedday());
            items.put(TAG_MEMO, habitMemo.getMemo());

            mMemoSet.add(items);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(getClass().getName(), "onActivityResult code="+resultCode);

        mMemoSet.clear();
        makeMemoSet(habitid);
        mAdapter.notifyDataSetChanged();

        /*if (resultCode == CommonUtil.REQUEST_CODE_UPDATE_MEMO) {
            makeMemoSet(habitid);
            mAdapter.notifyDataSetChanged();
        }*/
    }
}
