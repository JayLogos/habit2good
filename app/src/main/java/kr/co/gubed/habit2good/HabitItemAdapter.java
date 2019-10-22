package kr.co.gubed.habit2good;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import az.plainpie.PieView;
import az.plainpie.animation.PieAngleAnimation;
import kr.co.gubed.habit2good.gpoint.listener.AsyncTaskCompleteListener;
import kr.co.gubed.habit2good.gpoint.util.APICrypto;
import kr.co.gubed.habit2good.gpoint.util.Applications;
import kr.co.gubed.habit2good.gpoint.util.AsyncHTTPPost;
import kr.co.gubed.habit2good.gpoint.util.CommonUtil;
import kr.co.gubed.habit2good.gpoint.util.EPreference;
import kr.co.gubed.habit2good.gpoint.util.Preference;
import kr.co.gubed.habit2good.gpoint.view.LoadingDialog;

public class HabitItemAdapter extends RecyclerView.Adapter<HabitItemAdapter.ViewHolder>
        implements ItemTouchHelperAdapter, AsyncTaskCompleteListener<String> {
    private final Context context;
    private final HabitsActivity activity;
    private final ArrayList<HashMap<String, String>> habitList;
    private final SoundPool soundPool;
    private final int soundId;

    private static final String EXTRA_HABITID = "habitid";
    private static final String EXTRA_POSITION = "position";
    private static final String EXTRA_HNAME = "hname";
    private static final String EXTRA_GOALIMG = "goalimg";
    private static final String EXTRA_GOAL = "goal";
    private static final String EXTRA_SIGNAL = "signal";
    private static final String EXTRA_REWARD = "reward";
    private static final String EXTRA_CATEGORY = "category";
    private static final String EXTRA_SDATE = "sdate";
    private static final String EXTRA_EDATE = "edate";
    private static final String EXTRA_CYCLE = "cycle";
    private static final String EXTRA_COUNT = "count";
    private static final String EXTRA_UNIT = "unit";

    private final int RESULT_DONE=0;
    private final int RESULT_FAIL=1;
    private final int RESULT_SKIP=2;
    private final int RESULT_DELETE=3;
    private final int RESULT_NOTE=4;

    private final static int DOWN = 0;
    private final static int UP = 1;

    private ViewGroup viewGroup;


    private ImageView mIVUp;

    private ImageView mImageViewItemMenu;

    /* Habit Item Body */

    /* Habit Item Tail */

    /* Database */
    private final HabitDbAdapter dbAdapter;

    private LoadingDialog loadingDialog;

    public HabitItemAdapter(Context context, ArrayList<HashMap<String, String>> habitList, SoundPool soundPool, int soundId) {
        this.context = context;
        this.habitList = habitList;
        this.soundPool = soundPool;
        this.soundId = soundId;

        activity = (HabitsActivity) this.context;

        this.dbAdapter = new HabitDbAdapter(context);
        dbAdapter.open();
        loadingDialog = new LoadingDialog(context);
    }

    @NonNull
    @Override
    public HabitItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(getClass().getName(), "IMHERE start onCreateViewHolder");
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, null);
        viewGroup = parent;

        //ShowLoadingProgress();

        Log.i(getClass().getName(), "IMHERE end onCreateViewHolder");
        return new ViewHolder(v);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)

    /** 정보 및 이벤트 처리는 이 메소드에서 처리 **/

    @Override
    public void onBindViewHolder(@NonNull final HabitItemAdapter.ViewHolder holder, int mPosition) {
        Log.i(getClass().getName(), "IMHERE start onBindViewHolder");
        final int position = mPosition;
        final HashMap<String, String> habitItem = habitList.get(position);

        holder.habitid = Integer.parseInt(Objects.requireNonNull(habitItem.get(EXTRA_HABITID)));

        /* header */
        holder.goalImgPath = habitItem.get(EXTRA_GOALIMG);
        //Log.i(getClass().getName(), "habitid="+holder.habitid+" goalImgPath="+holder.goalImgPath);
        if (Objects.equals(holder.goalImgPath, "default")) {
            Glide.with(context).load(R.drawable.ic_habit2good_512)
                    .apply(new RequestOptions().circleCrop())
                    .into(holder.iv_goalimg);
        } else if (Objects.equals(holder.goalImgPath, "example")) {
            Glide.with(context).load(R.drawable.habit_example)
                    .apply(new RequestOptions().circleCrop())
                    .into(holder.iv_goalimg);
        } else {
            Glide.with(context).load(holder.goalImgPath)
                    .apply(new RequestOptions().circleCrop())
                    .into(holder.iv_goalimg);
        }

        holder.iv_goalimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageDialog(holder.goalImgPath);
            }
        });

        Log.i(getClass().getName(), "hname="+habitItem.get(EXTRA_HNAME));

        holder.tv_hname.setText(habitItem.get(EXTRA_HNAME)); //habit title
        holder.tv_sdate.setText(habitItem.get(EXTRA_SDATE));
        holder.tv_edate.setText(habitItem.get(EXTRA_EDATE));
        holder.tv_reminder.setText(habitItem.get("reminder"));

        /* Body */
        final int numOfOk = dbAdapter.getSuccessCountFromExecResultForPeriod(holder.habitid, holder.tv_sdate.getText().toString(), holder.tv_edate.getText().toString());
        holder.tv_numofok.setText(String.valueOf(numOfOk));
        final int successRatio = getSuccessRatio(holder.habitid, holder.tv_sdate.getText().toString(), holder.tv_edate.getText().toString(),
                                                habitItem.get(EXTRA_CYCLE), Integer.parseInt(habitItem.get(EXTRA_COUNT)));
        if ( successRatio < 60) {
            holder.tv_successratio.setTextColor(Color.parseColor("#DD2C00"));
        } else if ((60 <= successRatio) && (successRatio < 80)) {
            holder.tv_successratio.setTextColor(Color.parseColor("#FFAB00"));
        } else {
            holder.tv_successratio.setTextColor(Color.parseColor("#7CB342"));
        }
        String strSr = String.valueOf(successRatio)+"%";
        holder.tv_successratio.setText(strSr);

        holder.iv_successratio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSuccessRatioDiagramDialog(holder.habitid, holder.tv_hname.getText().toString(), habitItem.get(EXTRA_SDATE), habitItem.get(EXTRA_EDATE),
                                                habitItem.get(EXTRA_CYCLE), Integer.parseInt(habitItem.get(EXTRA_COUNT)));
            }
        });
        holder.tv_successratio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSuccessRatioDiagramDialog(holder.habitid, holder.tv_hname.getText().toString(), habitItem.get(EXTRA_SDATE), habitItem.get(EXTRA_EDATE),
                                                habitItem.get(EXTRA_CYCLE), Integer.parseInt(habitItem.get(EXTRA_COUNT)));
            }
        });

        holder.iv_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HabitReminderActivity.class);

                intent.putExtra("habitid", holder.habitid);
                //context.startActivity(intent);
                activity.startActivityForResult(intent, CommonUtil.REQUEST_CODE_HABIT_REMINDER);
                activity.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
            }
        });
        holder.tv_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HabitReminderActivity.class);

                intent.putExtra("habitid", holder.habitid);
                context.startActivity(intent);
                activity.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
            }
        });

        /* Expand */
        holder.tv_goal.setText(habitItem.get(EXTRA_GOAL));
        holder.tv_signal.setText(habitItem.get(EXTRA_SIGNAL));
        holder.tv_reward.setText(habitItem.get(EXTRA_REWARD));
//        holder.tv_category.setText(habitItem.get(EXTRA_CATEGORY));
        holder.tv_cycle.setText(habitItem.get(EXTRA_CYCLE));
        holder.tv_count.setText(habitItem.get(EXTRA_COUNT));
        holder.tv_unit.setText(habitItem.get(EXTRA_UNIT));


        /* Habit Item Header */
        ImageView mIVDown = holder.iv_down;
        mIVUp = holder.iv_up;
        mIVDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionManager.beginDelayedTransition(viewGroup, new ChangeBounds());
                holder.changeViewStateOfExpandItem(DOWN);
            }
        });

        mIVUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionManager.beginDelayedTransition(viewGroup, new ChangeBounds());
                holder.changeViewStateOfExpandItem(UP);
            }
        });

        mImageViewItemMenu = holder.iv_menu;

        mImageViewItemMenu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                MenuInflater menuInflater = popupMenu.getMenuInflater();
                Menu menu = popupMenu.getMenu();

                menuInflater.inflate(R.menu.menu_habit_item, menu);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_reminder:
                                Intent intent = new Intent(context, HabitReminderActivity.class);
                                intent.putExtra("habitid", holder.habitid);
                                context.startActivity(intent);
                                activity.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                                return true;
                            case R.id.action_success:
                                showSuccessRatioDiagramDialog(holder.habitid, holder.tv_hname.getText().toString(), habitItem.get(EXTRA_SDATE), habitItem.get(EXTRA_EDATE),
                                                                holder.tv_cycle.getText().toString(), Integer.parseInt(holder.tv_count.getText().toString()));
                                return true;
                            case R.id.action_memo:
                                Intent intentMemo = new Intent(context, HabitMemoActivity.class);
                                intentMemo.putExtra("habitid", holder.habitid);
                                context.startActivity(intentMemo);
                                activity.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                                return true;
                            case R.id.action_update:
                                updateHabit(holder.habitid, holder, position);
                                HabitItemAdapter.this.notifyItemChanged(holder.getAdapterPosition());
                                return true;
                            case R.id.action_delete:
                                /* db에 서 delete 실패 시 에러 처리 추가 필요
                                 * deleteHabit return value 를 boolean 으로 처리 하자
                                 * */
                                showDeleteItemDialog(holder);
                                return true;
                            case R.id.action_capture:
                                RecyclerView recyclerView = viewGroup.findViewById(R.id.rv);
                                holder.captureUtilities.captureMyRecyclerView(recyclerView, Color.WHITE, position, position);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });

        holder.collapsibleCalendar.setHabitid(holder.habitid);
        holder.collapsibleCalendar.reload();

        holder.collapsibleCalendar.setCalendarListener(new HfCollapsibleCalendar.CalendarListener() {

            @Override
            public void onDaySelect() {
                final HfDay day = holder.collapsibleCalendar.getSelectedDay();
                holder.collapsibleCalendar.expandIconView.setAnimationDuration(100);

                //Log.i(getClass().getName(), "Selected Day: "
                //        + day.getYear() + "/" + (day.getMonth() + 1) + "/" + day.getDay());

                if (isChangeable(day)) {
                    holder.circleMenu.setEventListener(new MyCircleMenuView.EventListener() {

                        @SuppressLint("RestrictedApi")
                        @Override
                        public void onMenuOpenAnimationStart(@NonNull MyCircleMenuView view) {
                            view.mMenuButton.setVisibility(View.VISIBLE);
                            //Log.d("D", "onMenuOpenAnimationStart, duration open="+view.getDurationOpen()+", close="+view.getDurationClose());
                        }

                        @Override
                        public void onMenuOpenAnimationEnd(@NonNull MyCircleMenuView view) {
                            view.setDurationOpen(0);
                            view.setDurationClose(400);
                            //Log.d("D", "onMenuOpenAnimationEnd, duration open="+view.getDurationOpen()+", close="+view.getDurationClose());
                        }

                        @SuppressLint("RestrictedApi")
                        @Override
                        public void onMenuCloseAnimationStart(@NonNull MyCircleMenuView view) {
                            view.mMenuButton.setVisibility(View.GONE);
                            //Log.d("D", "onMenuCloseAnimationStart, duration open="+view.getDurationOpen()+", close="+view.getDurationClose());
                        }

                        @Override
                        public void onMenuCloseAnimationEnd(@NonNull MyCircleMenuView view) {
                            view.setDurationOpen(0);
                            //Log.d("D", "onMenuCloseAnimationEnd, duration open="+view.getDurationOpen()+", close="+view.getDurationClose());
                            holder.circleMenu.setVisibility(View.GONE);
                        }

                        @SuppressLint("RestrictedApi")
                        @Override
                        public void onButtonClickAnimationStart(@NonNull MyCircleMenuView view, int index) {
                            //Log.d("D", "onButtonClickAnimationStart| index: " + index+", duration open="+view.getDurationOpen()+", close="+view.getDurationClose());
                            view.mMenuButton.setVisibility(View.GONE);
                            switch (index) {
                                case RESULT_DONE:
                                    Toast.makeText(context, "미션 성공!\n작은 성공이 쌓여 밝은 미래를 만듭니다.", Toast.LENGTH_LONG).show();
                                    rewardTrophy(holder.habitid, 1);
                                    if (soundPool != null) {
                                        soundPool.play(soundId, 1, 1, 0, 0, 1);
                                    } else {
                                        Log.e(getClass().getName(), "soundPool is null");
                                    }
                                    break;
                                case RESULT_FAIL:
                                    Toast.makeText(context, "미션 실패!\n낙담은 금물. 다시 시작해요!", Toast.LENGTH_LONG).show();
                                    break;
                                case RESULT_SKIP:
                                    Toast.makeText(context, "건너뛰기.\n더 이상 미루지 마세요. 바로 지금입니다!", Toast.LENGTH_LONG).show();
                                    break;
                                case RESULT_DELETE:
                                    Toast.makeText(context, "삭제 되었습니다.", Toast.LENGTH_LONG).show();
                                    break;
                                case RESULT_NOTE:
                                    Toast.makeText(context, "메모하기", Toast.LENGTH_LONG).show();
                                    showMemoInputDialog(holder, day);
                                    break;
                                default:
                                    break;
                            }

                            int position;  // 추후 검토 필요, holder.getAdapterPosition()으로 대체 검토
                            position = holder.collapsibleCalendar.getSelectedItemPosition();
                            if (index != RESULT_NOTE) {
                                holder.collapsibleCalendar.setSelectedDayWithResult(position, index);
                                dbAdapter.setExecResult(holder.habitid, day, index);
                                //Log.e("DB LEAK","dbHelper.setExecResult");
                                holder.collapsibleCalendar.select(day);
                            }

                            holder.tv_numofok.setText(String.valueOf(dbAdapter.getSuccessCountFromExecResultForPeriod(holder.habitid,
                                    holder.tv_sdate.getText().toString(), holder.tv_edate.getText().toString())));
                            //Log.e("DB LEAK","dbHelper.getSuccessCountFromExecResultForPeriod");

                            int successRatio = getSuccessRatio(holder.habitid, holder.tv_sdate.getText().toString(), holder.tv_edate.getText().toString(),
                                                                habitItem.get(EXTRA_CYCLE), Integer.parseInt(habitItem.get(EXTRA_COUNT)));
                            if (successRatio < 60) {
                                holder.tv_successratio.setTextColor(Color.parseColor("#DD2C00"));
                            } else if ((60 <= successRatio) && (successRatio < 80)) {
                                holder.tv_successratio.setTextColor(Color.parseColor("#FFAB00"));
                            } else {
                                holder.tv_successratio.setTextColor(Color.parseColor("#7CB342"));
                            }
                            String strSr = String.valueOf(successRatio) + "%";
                            holder.tv_successratio.setText(strSr);
                        }

                        @Override
                        public void onButtonClickAnimationEnd(@NonNull MyCircleMenuView view, int index) {
                            view.setDurationOpen(0);
                            //Log.d("D", "onButtonClickAnimationEnd| index: " + index+", duration open="+view.getDurationOpen()+", close="+view.getDurationClose());
                            holder.circleMenu.setVisibility(View.GONE);
                        }
                    });
                    holder.circleMenu.setVisibility(View.VISIBLE);
                    holder.circleMenu.open(true);
                }
            }

            @Override
            public void onItemClick(View var1) {
                //Log.i("Calendar onItemClick", "Clicked....");
            }

            @Override
            public void onDataUpdate() {
                //Log.i("Calendar onDataUpdate", "Clicked....");
            }

            @Override
            public void onMonthChange() {
                //Log.i("Calendar onMonthChange", "Clicked....");
            }

            @Override
            public void onWeekChange(int var1) {
                //Log.i("Calendar onWeekChange", "Clicked....");
            }
        });
        Log.i(getClass().getName(), "IMHERE end onBindViewHolder");
        try {
            //HideLoadingProgress();
        } catch (Exception ignore) {
        }
    }

    @Override
    public int getItemCount() {
        return this.habitList.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {

        dbAdapter.setHabitItemPosition(Integer.valueOf(Objects.requireNonNull(habitList.get(fromPosition).get(EXTRA_HABITID))), toPosition);
        dbAdapter.setHabitItemPosition(Integer.valueOf(Objects.requireNonNull(habitList.get(toPosition).get(EXTRA_HABITID))), fromPosition);

        //Collections.swap(habitList, fromPosition, toPosition);
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(habitList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(habitList, i, i - 1);
            }
        }

        HabitItemAdapter.this.notifyItemMoved(fromPosition, toPosition);

        //Log.d(getClass().getName(), "Exec onItemMove ,hname="+habitList.get(fromPosition).get(EXTRA_HNAME)+" from="+fromPosition+", to="+fromPosition);

        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    static class ViewHolder extends RecyclerView.ViewHolder
            implements ItemTouchHelperIndicator {
        Integer     habitid=0;
        final TextView    tv_hname;
        final TextView tv_sdate;
        final TextView tv_edate;
        final TextView tv_reminder;
        final TextView    tv_goal;
        final TextView tv_signal;
        final TextView tv_reward;
//        final TextView tv_category;
        final TextView tv_cycle;
        final TextView tv_count;
        final TextView tv_unit;
        final TextView    tv_numofok;
        final TextView tv_successratio;
        final LinearLayout ll_expand;
        final ImageView   iv_down;
        final ImageView iv_up;
        final ImageView iv_menu;
        final ImageView iv_goalimg;
        final ImageView iv_reminder;
        final ImageView iv_successratio;
        String goalImgPath;
        final HfCollapsibleCalendar collapsibleCalendar;
        final MyCircleMenuView circleMenu;
        final CaptureUtilities captureUtilities;

        final CardView    cv;

        ViewHolder(View itemView) {
            super(itemView);
            Log.i(getClass().getName(), "IMHERE start ViewHolder");

            iv_goalimg = itemView.findViewById(R.id.iv_goalimg);
            tv_hname = itemView.findViewById(R.id.tv_hname);
            tv_sdate = itemView.findViewById(R.id.tv_sdate);
            tv_edate = itemView.findViewById(R.id.tv_edate);
            iv_menu = itemView.findViewById(R.id.iv_menu);

            tv_numofok = itemView.findViewById(R.id.tv_numofok);
            iv_successratio = itemView.findViewById(R.id.iv_successratio);
            tv_successratio = itemView.findViewById(R.id.tv_successratio);
            iv_reminder = itemView.findViewById(R.id.iv_reminder);
            tv_reminder = itemView.findViewById(R.id.tv_reminder);
            collapsibleCalendar = itemView.findViewById(R.id.calendarView);

            iv_down = itemView.findViewById(R.id.iv_down);
            iv_up = itemView.findViewById(R.id.iv_up);

            ll_expand = itemView.findViewById(R.id.ll_expand);
            tv_goal = itemView.findViewById(R.id.tv_goal);
            tv_signal = itemView.findViewById(R.id.tv_signal);
            tv_reward = itemView.findViewById(R.id.tv_reward);
//            tv_category = itemView.findViewById(R.id.tv_category);
            tv_cycle = itemView.findViewById(R.id.tv_cycle);
            tv_count = itemView.findViewById(R.id.tv_count);
            tv_unit = itemView.findViewById(R.id.tv_unit);

            cv = itemView.findViewById(R.id.cv);
            circleMenu = itemView.findViewById(R.id.circle_menu);

            captureUtilities = new CaptureUtilities();

            goalImgPath = "";
            Log.i(getClass().getName(), "IMHERE end ViewHolder");
        }

        @Override
        public void onItemSelected() {
            cv.setCardElevation(getPixelsFromDPs(10));
        }

        @Override
        public void onItemClear() {
            cv.setCardElevation(getPixelsFromDPs(1));
        }

        void changeViewStateOfExpandItem(int state) {
            if (state == DOWN) {
                ll_expand.setVisibility(View.VISIBLE);
                iv_down.setVisibility(View.INVISIBLE);
                iv_up.setVisibility(View.VISIBLE);
            } else if (state == UP) {
                ll_expand.setVisibility(View.GONE);
                iv_down.setVisibility(View.VISIBLE);
                iv_up.setVisibility(View.INVISIBLE);
            }
        }

        private int getPixelsFromDPs(int dps) {
            //Resources r = context.getResources();
            Resources r = cv.getResources();
            int px = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps, r.getDisplayMetrics()));

            return px;
        }
    }


    private void deleteHabit(Integer habitid) {
        dbAdapter.deleteHabit(habitid);
    }

    private void updateHabit(Integer habitid, HabitItemAdapter.ViewHolder holder, int position) {
        Intent intent = new Intent(context, UpdateHabitItem.class);

        Log.i("updateHabit", "habitid="+habitid);

        intent.putExtra(EXTRA_HABITID, habitid);
        intent.putExtra(EXTRA_POSITION, position);
        intent.putExtra(EXTRA_HNAME, holder.tv_hname.getText());
        intent.putExtra(EXTRA_GOALIMG, holder.goalImgPath);
        intent.putExtra(EXTRA_GOAL, holder.tv_goal.getText());
        intent.putExtra(EXTRA_SIGNAL, holder.tv_signal.getText());
        intent.putExtra(EXTRA_REWARD, holder.tv_reward.getText());
//        intent.putExtra(EXTRA_CATEGORY, holder.tv_category.getText());
        intent.putExtra(EXTRA_SDATE, holder.tv_sdate.getText());
        intent.putExtra(EXTRA_EDATE, holder.tv_edate.getText());
        intent.putExtra(EXTRA_CYCLE, holder.tv_cycle.getText());
        intent.putExtra(EXTRA_COUNT, holder.tv_count.getText());
        intent.putExtra(EXTRA_UNIT, holder.tv_unit.getText());

        activity.startActivityForResult(intent, CommonUtil.REQUEST_CODE_HABIT_UPDATE);
        activity.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
    }

    private int getCountOfTries(int habitid, String sdate, String edate) {
        long calDate=0;
        long now = System.currentTimeMillis();

        try {
            Date today = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
            Date startDate = sdf.parse(sdate);
            Date endDate = sdf.parse(edate);

            if (endDate.getTime() > today.getTime()) {
                calDate = ((today.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000)) + 1;
            } else {
                calDate = ((endDate.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000)) + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (int)calDate;
    }

    private int getSuccessRatio(int habitid, String sdate, String edate, String cycle, int count) {
        double calDate, successRatio=0;
        double successCount;

        //Log.i(getClass().getName(), "getSuccessRatio cycle="+cycle);
        try {
            calDate = getCountOfTries(habitid, sdate, edate);
            successCount = dbAdapter.getSuccessCountFromExecResultForPeriod(habitid, sdate, edate);

            if (cycle.equals("매일")) {
                successRatio = (successCount / calDate) * 100;
                //Log.i(getClass().getName(), "day getSuccessRatio="+successRatio+" count="+count+" calDate="+calDate);
            } else if (cycle.equals("매주")) {
                successRatio = (successCount / (count*(calDate/7))) * 100 ;
                //Log.i(getClass().getName(), "week getSuccessRatio="+successRatio+" count="+count+" calDate="+calDate);
            } else if (cycle.equals("매월")) {
                successRatio = (successCount / (count*(calDate/30))) * 100 ;
                //Log.i(getClass().getName(), "month getSuccessRatio="+successRatio+" count="+count+" calDate="+calDate);
            } else if (cycle.equals("매년")) {
                successRatio = (successCount / (count*(calDate/365))) * 100 ;
                //Log.i(getClass().getName(), "year getSuccessRatio="+successRatio+" count="+count+" calDate="+calDate);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        if (successRatio > 100) {
            successRatio = 100;
        }
        return (int)successRatio;
    }

    private boolean isChangeable(HfDay day) {
        long now = System.currentTimeMillis();
        String selectedDay = day.getYear()+"."+(day.getMonth()+1)+"."+day.getDay();
        Log.i("DEBUG", "year="+day.getYear()+" month="+day.getMonth()+" day="+day.getDay());

        try {
            Date today = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
            Date chosenday = sdf.parse(selectedDay);

            if (chosenday.getTime() > today.getTime()) {
                Toast.makeText(context, "결과를 입력할 수 있는 날이 아닙니다.", Toast.LENGTH_LONG).show();
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void showDeleteItemDialog(final ViewHolder holder) {
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle("목표 삭제 확인");
        adb.setMessage("선택하신 목표를 삭제하시겠습니까?")
                .setPositiveButton("삭제",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteHabit(holder.habitid);
                                Toast.makeText(context, "삭제하기", Toast.LENGTH_LONG).show();
                                habitList.remove(holder.getAdapterPosition());
                                HabitItemAdapter.this.notifyItemRemoved(holder.getAdapterPosition());
                            }
                        })
                .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = adb.create();
        alertDialog.show();
    }

    private void showSuccessRatioDiagramDialog(Integer habitid, String hname, String sdate, String edate, String cycle, int count) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = Objects.requireNonNull(inflater).inflate(R.layout.pie_view, null);
        TextView tv_hname = view.findViewById(R.id.title);
        TextView tv_schedule = view.findViewById(R.id.schedule);
        TextView tv_result = view.findViewById(R.id.result);
        PieView pieView = view.findViewById(R.id.pieView);
        int successRatio;

        successRatio = getSuccessRatio(habitid, sdate, edate, cycle, count);
        pieView.setInnerText((String.valueOf(successRatio)+"%"));
        pieView.setPercentage(successRatio);

        PieAngleAnimation animation = new PieAngleAnimation(pieView);
        animation.setDuration(200);
        pieView.startAnimation(animation);

        tv_hname.setText(hname);
        String strSchedule = sdate+" ~ "+edate+" (총 "+getCountOfTries(habitid, sdate, edate)+"회)";
        tv_schedule.setText(strSchedule);
        String strResult = getCountOfTries(habitid, sdate, edate)+"회 시도 중 "+dbAdapter.getSuccessCountFromExecResultForPeriod(habitid, sdate, edate)+"회 성공";
        tv_result.setText(strResult);

        builder.setView(view);

        final AlertDialog dialog = builder.create();

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    private void showImageDialog(String imagePath) {
        Dialog builder = new Dialog(context);
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

        if (imagePath.equals("default")) {
            imageView.setImageResource(R.drawable.ic_habit2good_512);
        } else if (imagePath.equals("example")) {
            imageView.setImageResource(R.drawable.habit_example);
        } else {
            imageView.setImageURI(Uri.parse(imagePath));
        }
        builder.show();
    }

    private void showMemoInputDialog(final HabitItemAdapter.ViewHolder holder, final HfDay day) {
        final EditText editText = new EditText(context);
        final int mMaxLength = 1024;
        String storedMemo = dbAdapter.getMemo(holder.habitid, day);
        editText.setText(storedMemo);
        editText.setSelection(editText.length());
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength)});

        AlertDialog.Builder builder= new AlertDialog.Builder(context);
        builder.setTitle("메모를 입력하세요.");
        builder.setView(editText);
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editText.getText().length() > 0) {
                            dbAdapter.setMemo(holder.habitid, day, editText.getText().toString());
                            holder.collapsibleCalendar.setEventTag(day.getYear(), day.getMonth(), day.getDay(), 1);
                        } else {
                            dbAdapter.deleteMemo(holder.habitid, day);
                            holder.collapsibleCalendar.setEventTag(day.getYear(), day.getMonth(), day.getDay(), 0);
                        }
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*
                        if (editText.getText().length() > 0) {
                            dbAdapter.setMemo(holder.habitid, day, editText.getText().toString());
                            holder.collapsibleCalendar.setEventTag(day.getYear(), day.getMonth(), day.getDay(), 1);
                        } else {
                            dbAdapter.deleteMemo(holder.habitid, day);
                            holder.collapsibleCalendar.setEventTag(day.getYear(), day.getMonth(), day.getDay(), 0);
                        }
                        */
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    void rewardTrophy(Integer habitid, Integer rewardTrophy) {
        /*
        *   TBD: Trophy 정책
        *   하루 5개 제한
        *   당일 설정에만 부여
        *   성공/실행이 아니여도 트로피 부여, 왜냐하면 트로피는 해빗투굿 서비스 활성화에 기여도를 측정하기 위해 부여되기 때문
         */
        Applications.isHomeRefresh = false;
        HashMap<String, String> map = new HashMap<>();
        map.put(CommonUtil.KEY_USERID, Applications.preference.getValue(Preference.USER_ID, ""));
        map.put(CommonUtil.KEY_ADID, Applications.preference.getValue(Preference.AD_ID, ""));
        map.put(CommonUtil.KEY_DEVICE_TOKEN, Applications.preference.getValue(Preference.DEVICE_TOKEN, ""));
        map.put(CommonUtil.KEY_PHONE_NM, Applications.preference.getValue(Preference.PHONE_NM, ""));
        int version = CommonUtil.getVersionCode(context);
        map.put(CommonUtil.KEY_NAME, version + "");
        map.put(CommonUtil.KEY_ACTION, CommonUtil.ACTION_PUT_TROPHY);
        map.put(CommonUtil.KEY_REWARD_TROPHY, rewardTrophy.toString());
        map.put(CommonUtil.KEY_HABIT_ID, habitid.toString());
        String param = APICrypto.getParam(context, map, CommonUtil.SHARED_KEY);
        requestAsyncTask(param, CommonUtil.ACTION_PUT_TROPHY);
    }

    public void requestAsyncTask(String param, String action){
        if( Applications.getCountry(context).equals("KR") && !Applications.isRoaming(context)) {
            new AsyncHTTPPost(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, CommonUtil.SERVER_URL, param, action);
        }else{
            new AsyncHTTPPost(this).execute(CommonUtil.SERVER_URL, param, action);
        }
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
            Log.i(getClass().getName(), "onTaskComplete action="+action);

            if (error != null && error.isEmpty() && action != null && !action.isEmpty()) {
                Log.i(getClass().getName(), "onTaskComplete action="+action);
                switch (action) {
                    case CommonUtil.ACTION_PUT_TROPHY:
                        Integer trophy = Integer.parseInt(jo.getString(CommonUtil.RESULT_H2G_TROPHY));
                        Applications.ePreference.put(EPreference.N_TROPHY, trophy);
                        Toast.makeText(context, "트로피를 획득하셨습니다. 총 트로피 "+trophy+" 개", Toast.LENGTH_LONG).show();
                        Log.i(getClass().getName(), "ACTION_PUT_TROPHY get return, total trophy count="+trophy);

                        break;
                    default:
                        Log.e(getClass().getName(), "unknown action: "+action);
                        break;
                }
            } else {
                Log.e(getClass().getName(), "CommonUtil.ACTION fail action="+action);
                if (error.equals("excess")) {
                    Toast.makeText(context, "일일 보상 트로피를 모두 획득하셨습니다.", Toast.LENGTH_LONG).show();
                } else if (error.equals("dup")) {
                    Log.i(getClass().getName(), "duplicated reward trophy occured");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    @Override
    public void onTaskError(String param, String action, String result) {

    }

    public void ShowLoadingProgress() {
        //show loading

        try {
            if (loadingDialog == null) {
                loadingDialog = new LoadingDialog(context);
            }
            if (!loadingDialog.isShowing()) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            loadingDialog.show();
                            Log.i(getClass().getName(), "ShowLoadingProgress start");
                        } catch (Exception ignore) {
                            ignore.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }

    public void HideLoadingProgress() throws Exception {
        //hide loading

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                        Log.i(getClass().getName(), "HideLoadingProgress start");
                    }
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
            }
        });
    }
}
