package kr.co.gubed.habit2good;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import kr.co.gubed.habit2good.gpoint.database.dataDB;
import kr.co.gubed.habit2good.gpoint.model.AlarmNoti;
import kr.co.gubed.habit2good.gpoint.model.GiftBoxModel;
import kr.co.gubed.habit2good.gpoint.model.NoticeModel;
import kr.co.gubed.habit2good.gpoint.util.CommonUtil;

public class HabitDbAdapter {
    public static final String DATABASE_NAME = "habit2good.db";
    private static final int DATABASE_VERSION = 27;     // 2.0 이전에 23이었음
    private final String tblHabitPreference = "tbl_habit_preference";
    private final String tblHabitExecResult = "tbl_habit_exec_result";
    private final String tblCategories = "tbl_categories";
    private final String tblHabitReminder = "tbl_habit_reminder";
    private final String tblMemo = "tbl_memo";
    private final String tblPlus1 = "tbl_plus1";

    private final int RESULT_DELETE = 3;

    private final Context context;
    private static HabitDatabaseOpenHelper mDbHelper;
    private static SQLiteDatabase mDb;

    public class HabitDatabaseOpenHelper extends SQLiteOpenHelper {
        HabitDatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTable(db);

            db.execSQL(dataDB.TB_NOTICE_CREATE);
            db.execSQL(dataDB.TB_NEW_NOTICE_CREATE);
            db.execSQL(dataDB.TB_CATEGORY_CREATE);
            db.execSQL(dataDB.TB_QUEUE_CREATE);
            db.execSQL(dataDB.TB_NOTICE_POPUP_CREATE);
            db.execSQL(dataDB.TB_PACKAGE_CHK_CREATE);
            db.execSQL(dataDB.TB_GIFT_BOX_CREATE);
            db.execSQL(dataDB.TB_ALARM_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            /*db.execSQL("DROP TABLE IF EXISTS " + tblHabitExecResult);
            db.execSQL("DROP TABLE IF EXISTS " + tblHabitPreference);
            db.execSQL("DROP TABLE IF EXISTS " + tblCategories);
            db.execSQL("DROP TABLE IF EXISTS " + tblHabitReminder);
            db.execSQL("DROP TABLE IF EXISTS " + tblMemo);
            db.execSQL("DROP TABLE IF EXISTS " + tblPlus1);

            db.execSQL("drop table if exists "+ dataDB.TB_NOTICE);
            db.execSQL("drop table if exists "+ dataDB.TB_NEW_NOTICE);
            db.execSQL("drop table if exists "+ dataDB.TB_CATEGORY);
            db.execSQL("drop table if exists "+ dataDB.TB_QUEUE);
            db.execSQL("drop table if exists "+ dataDB.TB_NOTICE_POPUP);
            db.execSQL("drop table if exists "+ dataDB.TB_PACKAGE_CHK);*/

            //this.onCreate(db);
            db.execSQL("DROP TABLE IF EXISTS " + tblPlus1);
            createTablePlus1(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            super.onDowngrade(db, oldVersion, newVersion);

            db.execSQL("DROP TABLE IF EXISTS " + tblHabitExecResult);
            db.execSQL("DROP TABLE IF EXISTS " + tblHabitPreference);
            db.execSQL("DROP TABLE IF EXISTS " + tblCategories);
            db.execSQL("DROP TABLE IF EXISTS " + tblHabitReminder);
            db.execSQL("DROP TABLE IF EXISTS " + tblMemo);
            db.execSQL("DROP TABLE IF EXISTS " + tblPlus1);

            db.execSQL("drop table if exists "+ dataDB.TB_NOTICE);
            db.execSQL("drop table if exists "+ dataDB.TB_NEW_NOTICE);
            db.execSQL("drop table if exists "+ dataDB.TB_CATEGORY);
            db.execSQL("drop table if exists "+ dataDB.TB_QUEUE);
            db.execSQL("drop table if exists "+ dataDB.TB_NOTICE_POPUP);
            db.execSQL("drop table if exists "+ dataDB.TB_PACKAGE_CHK);

            this.onCreate(db);
        }
    }

    public HabitDbAdapter(Context context) {
        this.context = context;
    }

    public void open() throws SQLException {
        Log.i(getClass().getName(), "db open");
        if (mDbHelper == null) {
            mDbHelper = new HabitDatabaseOpenHelper(context);
            mDb = mDbHelper.getWritableDatabase();
            Log.i(getClass().getName(), "db open OK!");
        }

    }

    public void close() {
        mDbHelper.close();
        mDbHelper = null;
        mDb.close();
        Log.i(getClass().getName(), "db close");
    }

    private void createTable(SQLiteDatabase db) {
        String sql1 = "CREATE TABLE " + tblHabitExecResult
                + " (habitid INTEGER NOT NULL, "
                + "selectedday TEXT NOT NULL, "
                + "result INTEGER NOT NULL, "
                + "PRIMARY KEY (habitid, selectedday))";
        try {
            db.execSQL(sql1);
        } catch (SQLException e) {
            Log.e(getClass().getName(), sql1);
        }

        String sql2 = "CREATE TABLE " + tblHabitPreference
                + " (habitid INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "position INTEGER, "
                + "hname TEXT NOT NULL, "
                + "goalimg TEXT NOT NULL, "
                + "goal TEXT NOT NULL, "
                + "signal TEXT, "
                + "reward TEXT, "
                + "category TEXT, "
                + "sdate TEXT NOT NULL, "
                + "edate TEXT NOT NULL, "
                + "cycle TEXT, "
                + "count INTEGER, "
                + "unit TEXT)";
        try {
            db.execSQL(sql2);
        } catch (SQLException e) {
            Log.e(getClass().getName(), sql2);
        }


        String sql3 = "CREATE TABLE " + tblCategories
                + " (cname TEXT PRIMARY KEY, "
                + "color TEXT NOT NULL)";
        try {
            db.execSQL(sql3);
        } catch (SQLiteException e) {
            Log.e(getClass().getName(), sql3);
        }

        String sql4 = "CREATE TABLE " + tblHabitReminder
                + " (habitid INTEGER NOT NULL,"
                + " alarm_time TEXT NOT NULL,"
                + " alarm_name TEXT,"
                + " alarm_state INTEGER NOT NULL,"
                + " resunday INTEGER NOT NULL,"
                + " remonday INTEGER NOT NULL,"
                + " retuesday INTEGER NOT NULL,"
                + " rewednesday INTEGER NOT NULL,"
                + " rethursday INTEGER NOT NULL,"
                + " refriday INTEGER NOT NULL,"
                + " resaturday INTEGER NOT NULL,"
                + " PRIMARY KEY (habitid, alarm_time))";
        try {
            db.execSQL(sql4);
        } catch (SQLiteException e) {
            Log.e(getClass().getName(), e.getMessage()+e.getCause()+sql4);
        }

        String sql5 = "CREATE TABLE " + tblMemo
                + " (habitid INTEGER NOT NULL, "
                + "selectedday TEXT NOT NULL, "
                + "memo TEXT NOT NULL, "
                + "PRIMARY KEY (habitid, selectedday))";
        try {
            db.execSQL(sql5);
        } catch (SQLException e) {
            Log.e(getClass().getName(), sql5);
        }

        String sql6 = "CREATE TABLE " + tblPlus1
                + " (day TEXT NOT NULL, "
                + "count INTEGER NOT NULL, "
                + "PRIMARY KEY (day))";
        try {
            db.execSQL(sql6);
        } catch (SQLException e) {
            Log.e(getClass().getName(), sql6);
        }
    }

    private void createTablePlus1(SQLiteDatabase db) {
        String sql6 = "CREATE TABLE " + tblPlus1
                + " (day TEXT NOT NULL, "
                + "count INTEGER NOT NULL, "
                + "PRIMARY KEY (day))";
        try {
            db.execSQL(sql6);
        } catch (SQLException e) {
            Log.e(getClass().getName(), sql6);
        }
    }

    public void initDatabase(){
        open();
        try {
            mDb.execSQL("DELETE FROM "+ dataDB.TB_NOTICE);
            mDb.execSQL("DELETE FROM "+ dataDB.TB_NEW_NOTICE);
            mDb.execSQL("DELETE FROM "+ dataDB.TB_NOTICE_POPUP);
            mDb.execSQL("DELETE FROM "+ dataDB.TB_GIFT_BOX);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    
    public void addNewHabit(Habit habit){
        ContentValues values = new ContentValues();

        values.put("position", habit.getPosition());
        values.put("hname", habit.getHname());
        values.put("goalimg", habit.getGoalimg());
        values.put("goal", habit.getGoal());
        values.put("signal", habit.getSignal());
        values.put("reward", habit.getReward());
        values.put("category", habit.getCategory());
        values.put("sdate", habit.getSdate());
        values.put("edate", habit.getEdate());
        values.put("cycle", habit.getCycle());
        values.put("count", habit.getCount());
        values.put("unit", habit.getUnit());

        mDb.beginTransaction();
        try {
            mDb.insert("tbl_habit_preference", null, values);
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
            //
        } finally {
            mDb.endTransaction();
        }
    }

    public void deleteHabit(Integer habitid) {
        mDb.beginTransaction();
        try {
            mDb.delete(tblHabitPreference, "habitid=?", new String[]{String.valueOf(habitid)});
            //mDb.delete(tblHabitReminder, "habitid=?", new String[]{String.valueOf(habitid)});
            deleteHabitReminder(habitid);
            deleteExecResult(habitid);
            deleteMemo(habitid);
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            mDb.endTransaction();
        }
    }

    public void updateHabit(Habit habit){
        ContentValues values = new ContentValues();

        values.put("hname", habit.getHname());
        values.put("goalimg", habit.getGoalimg());
        values.put("goal", habit.getGoal());
        values.put("signal", habit.getSignal());
        values.put("reward", habit.getReward());
        values.put("category", habit.getCategory());
        values.put("sdate", habit.getSdate());
        values.put("edate", habit.getEdate());
        values.put("cycle", habit.getCycle());
        values.put("count", habit.getCount());
        values.put("unit", habit.getUnit());

        mDb.beginTransaction();
        try {
            mDb.update(tblHabitPreference, values, "habitid=?", new String[]{String.valueOf(habit.getHabitid())});
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            mDb.endTransaction();
        }
    }

    public void setHabitItemPosition(Integer habitid, int position){
        ContentValues values = new ContentValues();

        values.put("position", position);

        mDb.beginTransaction();
        try {
            mDb.update(tblHabitPreference, values, "habitid=?", new String[]{String.valueOf(habitid)});
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            mDb.endTransaction();
        }
    }

    public List<Habit> getHabitList(String filter) {
        String query;
        Cursor cursor = null;

        if(filter.equals("")) {
            query = "SELECT * FROM " + tblHabitPreference ;
        } else {
            query = "SELECT * FROM " + tblHabitPreference + " ORDER BY " + filter;
        }

        List<Habit> habitLinkedList = new LinkedList<>();
        mDb.beginTransaction();
        try {
            cursor = mDb.rawQuery(query, null);

            Habit habit;

            if (cursor.moveToFirst()) {
                do {
                    habit = new Habit();

                    habit.setHabitid(cursor.getInt(cursor.getColumnIndex("habitid")));
                    habit.setPosition(cursor.getInt(cursor.getColumnIndex("position")));
                    habit.setHname(cursor.getString(cursor.getColumnIndex("hname")));
                    habit.setGoalimg(cursor.getString(cursor.getColumnIndex("goalimg")));
                    habit.setGoal(cursor.getString(cursor.getColumnIndex("goal")));
                    habit.setSignal(cursor.getString(cursor.getColumnIndex("signal")));
                    habit.setReward(cursor.getString(cursor.getColumnIndex("reward")));
                    habit.setCategory(cursor.getString(cursor.getColumnIndex("category")));
                    habit.setSdate(cursor.getString(cursor.getColumnIndex("sdate")));
                    habit.setEdate(cursor.getString(cursor.getColumnIndex("edate")));
                    habit.setCycle(cursor.getString(cursor.getColumnIndex("cycle")));
                    habit.setCount(cursor.getInt(cursor.getColumnIndex("count")));
                    habit.setUnit(cursor.getString(cursor.getColumnIndex("unit")));

                    habitLinkedList.add(habit);
                } while (cursor.moveToNext()) ;
            }
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            mDb.endTransaction();
        }

        return habitLinkedList;
    }

    /* 하나의 habit 만 가져오기 */
    public Habit getHabit(Integer habitid) {
        String query = "SELECT * FROM " + tblHabitPreference + " WHERE habitid=" + habitid;
        Habit habit = new Habit();

        Cursor cursor = mDb.rawQuery(query, null);
        mDb.beginTransaction();
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                habit.setHabitid(cursor.getInt(cursor.getColumnIndex("habitid")));
                habit.setPosition(cursor.getInt(cursor.getColumnIndex("position")));
                habit.setHname(cursor.getString(cursor.getColumnIndex("hname")));
                habit.setGoalimg(cursor.getString(cursor.getColumnIndex("goalimg")));
                habit.setGoal(cursor.getString(cursor.getColumnIndex("goal")));
                habit.setSignal(cursor.getString(cursor.getColumnIndex("signal")));
                habit.setReward(cursor.getString(cursor.getColumnIndex("reward")));
                habit.setCategory(cursor.getString(cursor.getColumnIndex("category")));
                habit.setSdate(cursor.getString(cursor.getColumnIndex("sdate")));
                habit.setEdate(cursor.getString(cursor.getColumnIndex("edate")));
                habit.setCycle(cursor.getString(cursor.getColumnIndex("cycle")));
                habit.setCount(cursor.getInt(cursor.getColumnIndex("count")));
                habit.setUnit(cursor.getString(cursor.getColumnIndex("unit")));

            }
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
            return null;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            mDb.endTransaction();
        }
        return habit;
    }

    public String getHabitNameFromDB(Integer habitid) {
        String habitName=null;
        String query = "SELECT hname FROM " + tblHabitPreference + " WHERE habitid=" + habitid;
        Cursor cursor = mDb.rawQuery(query, null);

        mDb.beginTransaction();
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                habitName = cursor.getString(cursor.getColumnIndex("hname"));
            }
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            mDb.endTransaction();
        }

        return habitName;
    }

    public void setGoalImage(Integer habitid, String goalImgPath){
        ContentValues values = new ContentValues();

        values.put("goalimg", goalImgPath);

        mDb.beginTransaction();
        try {
            mDb.update(tblHabitPreference, values, "habitid=?", new String[]{String.valueOf(habitid)});
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            mDb.endTransaction();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public int getExecResult(Integer habitid, HfDay day) {
        String selectedDay = day.getYear() + "." + String.format(Locale.getDefault(), "%02d", day.getMonth()+1) + "." + String.format(Locale.getDefault(), "%02d", day.getDay());

        String query = "SELECT * FROM " + tblHabitExecResult + " WHERE habitid="+habitid+" AND selectedday="+"'"+selectedDay+"'";
        Cursor cursor = mDb.rawQuery(query, null);
        int mResult=-1;

        mDb.beginTransaction();
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                mResult = cursor.getInt(cursor.getColumnIndex("result"));
            } else {
            }
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            mDb.endTransaction();
        }

        return mResult;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public int getExecResult(Integer habitid, String selectedDay) {
        String query = "SELECT * FROM " + tblHabitExecResult + " WHERE habitid="+habitid+" AND selectedday="+"'"+selectedDay+"'";
        Cursor cursor = mDb.rawQuery(query, null);
        int mResult=-1;

        mDb.beginTransaction();
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                mResult = cursor.getInt(cursor.getColumnIndex("result"));
            } else {
            }
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            mDb.endTransaction();
        }

        return mResult;
    }

    public void setExecResult(Integer habitid, HfDay day, int result){
        ContentValues values = new ContentValues();
        String selectedDay = day.getYear() + "." + String.format(Locale.getDefault(), "%02d", day.getMonth()+1) + "." + String.format(Locale.getDefault(),"%02d", day.getDay());

        if (getExecResult(habitid, day) > -1) {
            if (result == RESULT_DELETE) {
                deleteExecResult(habitid, day);
                deleteMemo(habitid, day); /* 메모까지 삭제할 것인지 따로 메모 삭제 UI를 추가할 것인지 추후 검토 */
            }
            values.put("result", result);

            mDb.beginTransaction();
            try {
                mDb.update(tblHabitExecResult, values, "habitid=? AND selectedDay=?", new String[]{habitid.toString(),
                        selectedDay});
                mDb.setTransactionSuccessful();
            } catch (SQLiteException e) {
                Log.e("DB Error", e.getCause() + e.getMessage());
            } finally {
                mDb.endTransaction();
            }

        } else {
            if (result == RESULT_DELETE) return;

            values.put("habitid", habitid);
            values.put("selectedday", selectedDay);
            values.put("result", result);

            mDb.beginTransaction();
            try {
                mDb.insert(tblHabitExecResult, null, values);
                mDb.setTransactionSuccessful();
            } catch (SQLiteException e) {
                Log.e("DB Error", e.getCause() + e.getMessage());
            } finally {
                mDb.endTransaction();
            }
        }
    }

    private void deleteExecResult(Integer habitid) {
        mDb.beginTransaction();
        try {
            mDb.delete(tblHabitExecResult, "habitid=?", new String[]{String.valueOf(habitid)});
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            mDb.endTransaction();
        }
    }

    private void deleteExecResult(Integer habitid, HfDay day) {
        String selectedDay = day.getYear() + "." + String.format(Locale.getDefault(),"%02d", day.getMonth()+1) + "." + String.format(Locale.getDefault(),"%02d", day.getDay());

        mDb.beginTransaction();
        try {
            mDb.delete(tblHabitExecResult, "habitid=? AND selectedDay=?", new String[]{habitid.toString(),
                    selectedDay});
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            mDb.endTransaction();
        }
    }

    public int getSuccessCountFromExecResult(Integer habitid) {
        String query = "SELECT * FROM " + tblHabitExecResult + " WHERE habitid="+habitid+" AND result=0";
        Cursor cursor=null;
        int mResult;

        mDb.beginTransaction();
        try {
            cursor = mDb.rawQuery(query, null);
            mResult = cursor.getColumnCount();
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
            mResult = 0;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            mDb.endTransaction();
        }
        return mResult;
    }

    public int getSuccessCountFromExecResultForPeriod(Integer habitid, String sdate, String edate) {
        Cursor cursor = null;
        int result = -1;
        String query;

        mDb.beginTransaction();
        try {
            Calendar today = Calendar.getInstance();
            Calendar calStartDate = Calendar.getInstance();
            Calendar calEndDate = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
            Date startDate = sdf.parse(sdate);
            Date endDate = sdf.parse(edate);
            calStartDate.setTime(startDate);
            calEndDate.setTime(endDate);
            String thisDay = today.get(Calendar.YEAR) + "." + String.format(Locale.getDefault(),"%02d", today.get(Calendar.MONTH)+1) + "." + String.format(Locale.getDefault(),"%02d", today.get(Calendar.DAY_OF_MONTH));

            if (today.getTimeInMillis() <= calEndDate.getTimeInMillis()) {
                query = "SELECT * FROM " + tblHabitExecResult + " WHERE habitid="+habitid
                        +" AND selectedday >= "+"'"+sdate+"'"
                        +" AND selectedday <= "+"'"+thisDay+"'"
                        +" AND result=0";
            } else {
                query = "SELECT * FROM " + tblHabitExecResult + " WHERE habitid="+habitid
                        +" AND selectedday >= "+"'"+sdate+"'"
                        +" AND selectedday <= "+"'"+edate+"'"
                        +" AND result=0";
            }
            cursor = mDb.rawQuery(query, null);
            result = cursor.getCount();
            mDb.setTransactionSuccessful();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            mDb.endTransaction();
        }
        return result;
    }

    public List<HabitReminder> getHabitReminderList(Integer habitid, String orderby) {
        String query;
        Cursor cursor = null;
        List<HabitReminder> mHabitReminderList = new LinkedList<>();

        if(orderby.equals("")) {
            query = "SELECT * FROM " + tblHabitReminder + " WHERE habitid=" + habitid ;
        } else {
            query = "SELECT * FROM " + tblHabitReminder + " WHERE habitid=" + habitid + " ORDER BY " + orderby;
        }

        mDb.beginTransaction();
        try {
            cursor = mDb.rawQuery(query, null);

            HabitReminder mHabitReminder;

            if (cursor.moveToFirst()) {
                do {
                    mHabitReminder = new HabitReminder();

                    mHabitReminder.mHabitId = cursor.getInt(cursor.getColumnIndex("habitid"));
                    mHabitReminder.mAlarmTime = cursor.getString(cursor.getColumnIndex("alarm_time"));
                    mHabitReminder.mAlarmName = cursor.getString(cursor.getColumnIndex("alarm_name"));
                    mHabitReminder.mAlarmState = (cursor.getInt((cursor.getColumnIndex("alarm_state"))) != 0);
                    mHabitReminder.mReSunday = (cursor.getInt((cursor.getColumnIndex("resunday"))) != 0);
                    mHabitReminder.mReMonday = (cursor.getInt((cursor.getColumnIndex("remonday"))) != 0);
                    mHabitReminder.mReTuesday = (cursor.getInt((cursor.getColumnIndex("retuesday"))) != 0);
                    mHabitReminder.mReWednesday = (cursor.getInt((cursor.getColumnIndex("rewednesday"))) != 0);
                    mHabitReminder.mReThursday = (cursor.getInt((cursor.getColumnIndex("rethursday"))) != 0);
                    mHabitReminder.mReFriday = (cursor.getInt((cursor.getColumnIndex("refriday"))) != 0);
                    mHabitReminder.mReSaturday = (cursor.getInt((cursor.getColumnIndex("resaturday"))) != 0);

                    mHabitReminderList.add(mHabitReminder);
                } while (cursor.moveToNext()) ;
            }
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(getClass().getName(), e.getCause() + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            mDb.endTransaction();
        }

        return mHabitReminderList;
    }

    public List<HabitReminder> getHabitReminderList() {
        String query;
        Cursor cursor = null;
        List<HabitReminder> mHabitReminderList = new LinkedList<>();

        query = "SELECT * FROM " + tblHabitReminder + " WHERE alarm_state=" + 1;

        mDb.beginTransaction();
        try {
            cursor = mDb.rawQuery(query, null);

            HabitReminder mHabitReminder;

            if (cursor.moveToFirst()) {
                do {
                    mHabitReminder = new HabitReminder();

                    mHabitReminder.mHabitId = cursor.getInt(cursor.getColumnIndex("habitid"));
                    mHabitReminder.mAlarmTime = cursor.getString(cursor.getColumnIndex("alarm_time"));
                    mHabitReminder.mAlarmName = cursor.getString(cursor.getColumnIndex("alarm_name"));
                    mHabitReminder.mAlarmState = (cursor.getInt((cursor.getColumnIndex("alarm_state"))) != 0);
                    mHabitReminder.mReSunday = (cursor.getInt((cursor.getColumnIndex("resunday"))) != 0);
                    mHabitReminder.mReMonday = (cursor.getInt((cursor.getColumnIndex("remonday"))) != 0);
                    mHabitReminder.mReTuesday = (cursor.getInt((cursor.getColumnIndex("retuesday"))) != 0);
                    mHabitReminder.mReWednesday = (cursor.getInt((cursor.getColumnIndex("rewednesday"))) != 0);
                    mHabitReminder.mReThursday = (cursor.getInt((cursor.getColumnIndex("rethursday"))) != 0);
                    mHabitReminder.mReFriday = (cursor.getInt((cursor.getColumnIndex("refriday"))) != 0);
                    mHabitReminder.mReSaturday = (cursor.getInt((cursor.getColumnIndex("resaturday"))) != 0);

                    mHabitReminderList.add(mHabitReminder);
                } while (cursor.moveToNext()) ;
            }
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(getClass().getName(), e.getCause() + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            mDb.endTransaction();
        }

        return mHabitReminderList;
    }

    public HabitReminder getHabitReminder(Integer habitid, String alarmTime) {
        String query;
        Cursor cursor = null;
        HabitReminder mHabitReminder = new HabitReminder();

        query = "SELECT * FROM " + tblHabitReminder + " WHERE habitid=" + habitid + " AND alarm_time='" + alarmTime+"'";

        mDb.beginTransaction();
        try {
            cursor = mDb.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    mHabitReminder.mHabitId = cursor.getInt(cursor.getColumnIndex("habitid"));
                    mHabitReminder.mAlarmTime = cursor.getString(cursor.getColumnIndex("alarm_time"));
                    mHabitReminder.mAlarmName = cursor.getString(cursor.getColumnIndex("alarm_name"));
                    mHabitReminder.mAlarmState = (cursor.getInt((cursor.getColumnIndex("alarm_state"))) != 0);
                    mHabitReminder.mReSunday = (cursor.getInt((cursor.getColumnIndex("resunday"))) != 0);
                    mHabitReminder.mReMonday = (cursor.getInt((cursor.getColumnIndex("remonday"))) != 0);
                    mHabitReminder.mReTuesday = (cursor.getInt((cursor.getColumnIndex("retuesday"))) != 0);
                    mHabitReminder.mReWednesday = (cursor.getInt((cursor.getColumnIndex("rewednesday"))) != 0);
                    mHabitReminder.mReThursday = (cursor.getInt((cursor.getColumnIndex("rethursday"))) != 0);
                    mHabitReminder.mReFriday = (cursor.getInt((cursor.getColumnIndex("refriday"))) != 0);
                    mHabitReminder.mReSaturday = (cursor.getInt((cursor.getColumnIndex("resaturday"))) != 0);
                } while (cursor.moveToNext()) ;
            }
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(getClass().getName(), e.getCause() + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            mDb.endTransaction();
        }

        return mHabitReminder;
    }

    public Integer getCountOfReminder(Integer habitid) {
        Cursor cursor = null;
        Integer result = 0;
        String query;

        mDb.beginTransaction();
        try {
            query = "SELECT * FROM " + tblHabitReminder + " WHERE habitid="+habitid+" AND alarm_state="+1;

            cursor = mDb.rawQuery(query, null);
            result = cursor.getCount();
            mDb.setTransactionSuccessful();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            mDb.endTransaction();
        }
        return result;
    }

    public void addNewHabitReminder(HabitReminder habitReminder){
        ContentValues values = new ContentValues();

        if (isDuplicatedAlarm(habitReminder.mHabitId, habitReminder.mAlarmTime)) {
            deleteHabitReminderItem(habitReminder.mHabitId, habitReminder.mAlarmTime);
        }

        values.put("habitid", habitReminder.mHabitId);
        values.put("alarm_time", habitReminder.mAlarmTime);
        values.put("alarm_name", habitReminder.mAlarmName);
        values.put("alarm_state", habitReminder.mAlarmState);
        values.put("resunday", habitReminder.mReSunday);
        values.put("remonday", habitReminder.mReMonday);
        values.put("retuesday", habitReminder.mReTuesday);
        values.put("rewednesday", habitReminder.mReWednesday);
        values.put("rethursday", habitReminder.mReThursday);
        values.put("refriday", habitReminder.mReFriday);
        values.put("resaturday", habitReminder.mReSaturday);

        mDb.beginTransaction();
        try {
            mDb.insert(tblHabitReminder, null, values);
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            mDb.endTransaction();
        }
    }

    private void deleteHabitReminder(Integer habitid) {
        String query;
        Cursor cursor = null;
        HabitReminder mHabitReminder = new HabitReminder();
        HabitReminderAdapter mHabitReminderAdapter = new HabitReminderAdapter(context);

        query = "SELECT alarm_time FROM " + tblHabitReminder + " WHERE habitid=" + habitid;

        mDb.beginTransaction();
        try {
            cursor = mDb.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    mHabitReminder.mAlarmTime = cursor.getString(cursor.getColumnIndex("alarm_time"));
                    mHabitReminderAdapter.setAlarmReminderItem(habitid, mHabitReminder.mAlarmTime, null, false);
                } while (cursor.moveToNext()) ;
            }
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(getClass().getName(), e.getCause() + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            mDb.endTransaction();
        }

        mDb.beginTransaction();
        try {
            mDb.delete(tblHabitReminder, "habitid=?", new String[]{String.valueOf(habitid)});
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(getClass().getName(), e.getCause()+e.getMessage());
        } finally {
            mDb.endTransaction();
        }
    }

    public void deleteHabitReminderItem(Integer habitid, String alarmTime) {
        mDb.beginTransaction();
        try {
            mDb.delete(tblHabitReminder, "habitid=? AND alarm_time=?", new String[]{String.valueOf(habitid), alarmTime});
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(getClass().getName(), e.getCause() + e.getMessage());
        } finally {
            mDb.endTransaction();
        }
    }

    public void updateHabitReminder(HabitReminder habitReminder, String oldAlarmTime){
        ContentValues values = new ContentValues();
        /* 1.변경된 알람의 중복 체크, 존재하면 삭제
         * 2.아니면 기존 데이터 업데이트
         */
        if (isDuplicatedAlarm(habitReminder.mHabitId, habitReminder.mAlarmTime) && !habitReminder.mAlarmTime.equals(oldAlarmTime)) {
            deleteHabitReminderItem(habitReminder.mHabitId, habitReminder.mAlarmTime);
        }

        values.put("habitid", habitReminder.mHabitId);
        values.put("alarm_time", habitReminder.mAlarmTime);
        values.put("alarm_name", habitReminder.mAlarmName);
        values.put("alarm_state", habitReminder.mAlarmState);
        values.put("resunday", habitReminder.mReSunday);
        values.put("remonday", habitReminder.mReMonday);
        values.put("retuesday", habitReminder.mReTuesday);
        values.put("rewednesday", habitReminder.mReWednesday);
        values.put("rethursday", habitReminder.mReThursday);
        values.put("refriday", habitReminder.mReFriday);
        values.put("resaturday", habitReminder.mReSaturday);

        mDb.beginTransaction();
        try {
            mDb.update(tblHabitReminder, values, "habitid=? AND alarm_time=?", new String[]{String.valueOf(habitReminder.mHabitId), oldAlarmTime});
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            mDb.endTransaction();
        }
    }

    public void setHabitReminderItemState(Integer habitid, String alarmTime, boolean isChecked) {
        ContentValues values = new ContentValues();

        values.put("alarm_state", isChecked);

        mDb.beginTransaction();
        try {
            mDb.update(tblHabitReminder, values, "habitid=? AND alarm_time=?", new String[]{String.valueOf(habitid), alarmTime});
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(getClass().getName(), e.getCause() + e.getMessage());
        } finally {
            mDb.endTransaction();
        }
    }

    private boolean isDuplicatedAlarm(Integer habitid, String alarmTime) {
        int result=0;
        Cursor cursor = null;
        String sql = "SELECT * FROM " + tblHabitReminder + " WHERE habitid=" + habitid + " AND alarm_time='" + alarmTime+"'";

        mDb.beginTransaction();
        try {
            cursor = mDb.rawQuery(sql, null);
            result = cursor.getCount();
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            mDb.endTransaction();
        }

        return result > 0;
    }

    public List<HabitMemo> getHabitMemoSet(Integer habitid, String filter) {
        String query;
        Cursor cursor = null;

        if(filter.equals("")) {
            query = "SELECT * FROM " + tblMemo + " WHERE habitid=" + habitid;
        } else {
            query = "SELECT * FROM " + tblMemo + " WHERE habitid=" + habitid + " ORDER BY " + filter;
        }

        List<HabitMemo> habitMemoList = new LinkedList<>();

        mDb.beginTransaction();
        try {
            cursor = mDb.rawQuery(query, null);

            HabitMemo habitMemo;

            if (cursor.moveToFirst()) {
                do {
                    habitMemo = new HabitMemo();

                    habitMemo.setHabitid(cursor.getInt(cursor.getColumnIndex("habitid")));
                    habitMemo.setSelectedday(cursor.getString(cursor.getColumnIndex("selectedday")));
                    habitMemo.setMemo(cursor.getString(cursor.getColumnIndex("memo")));

                    habitMemoList.add(habitMemo);
                } while (cursor.moveToNext()) ;
            }
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            mDb.endTransaction();
        }
        return habitMemoList;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public String getMemo(Integer habitid, HfDay day) {
        String selectedDay = day.getYear() + "." + String.format(Locale.getDefault(),"%02d", day.getMonth()+1) + "." + String.format(Locale.getDefault(),"%02d", day.getDay());

        String query = "SELECT * FROM " + tblMemo + " WHERE habitid="+habitid+" AND selectedday="+"'"+selectedDay+"'";
        Cursor cursor = mDb.rawQuery(query, null);
        String mResult=null;

        mDb.beginTransaction();
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                mResult = cursor.getString(cursor.getColumnIndex("memo"));
                //Log.d(getClass().getName(),"Found record: "+query);
            } else {
                //Log.d(getClass().getName(),"NotFound record: "+query);
            }
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            mDb.endTransaction();
        }

        return mResult;
    }

    public void setMemo(Integer habitid, HfDay day, String memo){
        ContentValues values = new ContentValues();
        String selectedDay = day.getYear() + "." + String.format(Locale.getDefault(),"%02d", day.getMonth()+1) + "." + String.format(Locale.getDefault(),"%02d", day.getDay());

        if (getMemo(habitid, day) != null) {
            values.put("memo", memo);

            mDb.beginTransaction();
            try {
                mDb.update(tblMemo, values, "habitid=? AND selectedday=?",
                        new String[]{habitid.toString(),
                                selectedDay});
                mDb.setTransactionSuccessful();
            } catch (SQLiteException e) {
                Log.e("DB Error", e.getCause() + e.getMessage());
            } finally {
                mDb.endTransaction();
            }

        } else {
            values.put("habitid", habitid);
            values.put("selectedday", selectedDay);
            values.put("memo", memo);

            mDb.beginTransaction();
            try {
                mDb.insert(tblMemo, null, values);
                mDb.setTransactionSuccessful();
            } catch (SQLiteException e) {
                Log.e("DB Error", e.getCause() + e.getMessage());
            } finally {
                mDb.endTransaction();
            }
        }
    }

    private void deleteMemo(Integer habitid) {
        mDb.beginTransaction();
        try {
            mDb.delete(tblMemo, "habitid=?", new String[]{String.valueOf(habitid)});
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            mDb.endTransaction();
        }
    }

    public void deleteMemo(Integer habitid, HfDay day) {
        String selectedDay = day.getYear() + "." + String.format(Locale.getDefault(),"%02d", day.getMonth()+1) + "." + String.format(Locale.getDefault(),"%02d", day.getDay());

        mDb.beginTransaction();
        try {
            mDb.delete(tblMemo, "habitid=? AND selectedday=?",
                    new String[]{habitid.toString(),
                            selectedDay});
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            mDb.endTransaction();
        }
    }

    public void deleteMemo(Integer habitid, String selectedDay) {
        mDb.beginTransaction();
        try {
            mDb.delete(tblMemo, "habitid=? AND selectedday=?",
                    new String[]{habitid.toString(),
                            selectedDay});
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            mDb.endTransaction();
        }
    }

    public List<Plus1> getPlus1List(String day, int period, int criteria) {
        String query;
        DateFormat dateFormat;
        Cursor cursor = null;
        //String from = currentDay - period;
        if (criteria == CommonUtil.CRITERIA_DAY) {
            dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        } else {
            dateFormat = new SimpleDateFormat("yyyy.MM");
        }

        Date date = null;
        try {
            date = dateFormat.parse(day);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (criteria == CommonUtil.CRITERIA_DAY) {
            calendar.add(Calendar.DATE, -(MainActivity.numberOfPoints - 1));
        } else {
            calendar.add(Calendar.MONTH, -(MainActivity.numberOfPoints - 1));
        }
        String from = dateFormat.format(calendar.getTime());

        //query = "SELECT day, sum(count) count FROM " + tblPlus1 + " WHERE day >= '" + from + "' AND day <= '" + day +"' GROUP BY day";
        if (criteria == CommonUtil.CRITERIA_DAY) {
            query = "SELECT day dday, count FROM " + tblPlus1 + " WHERE day >= '" + from + "' AND day <= '" + day + "'";
        } else {
            query = "SELECT substr(day, 0, 8) dday, sum(count) count FROM " + tblPlus1 + " WHERE day >= '" + from + ".??" + "' AND day <= '" + day + ".??" + "'" + " GROUP BY dday";
        }
        Log.i(getClass().getName(), "PLUS1 query="+query);

        List<Plus1> plus1LinkedList = new LinkedList<>();
        for (int i=0 ; i <= period ; i++) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            if (criteria == CommonUtil.CRITERIA_DAY) {
                cal.add(Calendar.DATE, -(period - i));
            } else {
                cal.add(Calendar.MONTH, -(period - i));
            }
            String dday = dateFormat.format(cal.getTime());
            Plus1 plus1 = new Plus1();

            plus1.setDay(dday);
            plus1.setCount(0);
            plus1LinkedList.add(plus1);
        }

        mDb.beginTransaction();
        try {
            cursor = mDb.rawQuery(query, null);

            Plus1 plus1;

            if (cursor.moveToFirst()) {
                do {
                    for (int i=0 ; i<=period ; i++) {
                        plus1 = plus1LinkedList.get(i);
                        //Log.i(getClass().getName(), "PLUS1 cursor.getColumnIndex="+cursor.getString(cursor.getColumnIndex("day")));
                        if (plus1.getDay().equals(cursor.getString(cursor.getColumnIndex("dday")))) {
                            plus1.setCount(cursor.getInt(cursor.getColumnIndex("count")));
                        }
                        ((LinkedList<Plus1>) plus1LinkedList).remove(i);
                        plus1LinkedList.add(i, plus1);

                        Log.i(getClass().getName(), "PLUS1 data list: "+i+" day="+plus1LinkedList.get(i).getDay()+" count="+plus1LinkedList.get(i).getCount());
                    }
                } while (cursor.moveToNext()) ;
            }
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            mDb.endTransaction();
        }

        return plus1LinkedList;
    }

    public void setPlus1(int count){
        ContentValues values = new ContentValues();

        List<Plus1> plus1LinkedList;
        String day;

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        day = sdf.format(date);

        /*plus1LinkedList = getPlus1List(day, 0);
        Plus1 plus1 = plus1LinkedList.get(0);
        Log.i(getClass().getName(), "PLUS1 today attempt="+ plus1.getCount());*/

        if (count > 1) {
            //Plus1 plus1 = plus1LinkedList.get(0);

            values.put("count", count);
            Log.i(getClass().getName(), "PLUS1 update day="+day+" count="+count);

            mDb.beginTransaction();
            try {
                mDb.update(tblPlus1, values, "day=?",
                        new String[]{day});
                mDb.setTransactionSuccessful();
            } catch (SQLiteException e) {
                Log.e("DB Error", e.getCause() + e.getMessage());
            } finally {
                mDb.endTransaction();
            }
        } else {
            values.put("day", day);
            values.put("count", 1);
            Log.i(getClass().getName(), "PLUS1 insert day="+day+" count=1");

            mDb.beginTransaction();
            try {
                mDb.insert(tblPlus1, null, values);
                mDb.setTransactionSuccessful();
            } catch (SQLiteException e) {
                Log.e("DB Error", e.getCause() + e.getMessage());
            } finally {
                mDb.endTransaction();
            }
        }
    }

    public void setPlus1(){
        ContentValues values = new ContentValues();

        List<Plus1> plus1LinkedList;
        String day;

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        day = sdf.format(date);
        int count;

        count = getPlus1(day);

        if (count > 0) {
            //Plus1 plus1 = plus1LinkedList.get(0);

            values.put("count", ++count);
            Log.i(getClass().getName(), "PLUS1 update day="+day+" count="+count);

            mDb.beginTransaction();
            try {
                mDb.update(tblPlus1, values, "day=?",
                        new String[]{day});
                mDb.setTransactionSuccessful();
            } catch (SQLiteException e) {
                Log.e("DB Error", e.getCause() + e.getMessage());
            } finally {
                mDb.endTransaction();
            }
        } else {
            values.put("day", day);
            values.put("count", 1);
            Log.i(getClass().getName(), "PLUS1 insert day="+day+" count=1");

            mDb.beginTransaction();
            try {
                mDb.insert(tblPlus1, null, values);
                mDb.setTransactionSuccessful();
            } catch (SQLiteException e) {
                Log.e("DB Error", e.getCause() + e.getMessage());
            } finally {
                mDb.endTransaction();
            }
        }
    }

    public int getPlus1(String day) {
        String query = "SELECT count FROM " + tblPlus1 + " WHERE day="+"'"+day+"'";
        Cursor cursor = mDb.rawQuery(query, null);
        int mResult=-1;

        mDb.beginTransaction();
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                mResult = cursor.getInt(cursor.getColumnIndex("count"));
            } else {
            }
            mDb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e("DB Error", e.getCause() + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            mDb.endTransaction();
        }

        return mResult;
    }

    /* Timecash db function */
    public void insertNewNotice(JSONArray jobArr){
        try{
            open();
            mDb.beginTransaction();
            for(int i=0;i<jobArr.length();i++){
                JSONObject job = jobArr.getJSONObject(i);
                Cursor nCursor = mDb.query(dataDB.TB_NEW_NOTICE, new String[]{dataDB.ID, dataDB.ISREAD}, dataDB.ID+"=?", new String[]{job.getString("i")}, null, null, null);
                ContentValues values = new ContentValues();
                values.put(dataDB.B_TYPE, job.getString("b"));
                values.put(dataDB.ISDEL, job.getString("d"));
                values.put(dataDB.UPDATE_DATE, job.getString("u"));
                values.put(dataDB.REGDATE, job.getString("t"));
                if( nCursor.getCount() > 0){
                    //update
                    if( !nCursor.getString(nCursor.getColumnIndex(dataDB.ISREAD)).equals("read")){
                        if( Long.parseLong(job.getString("u")) > CommonUtil.newDate()) {
                            values.put(dataDB.ISREAD, "new");
                        }else{
                            values.put(dataDB.ISREAD, "read");
                        }
                    }else{
                        values.put(dataDB.ISREAD, "read");
                    }
                    mDb.update(dataDB.TB_NEW_NOTICE, values, dataDB.ID+"=?", new String[]{job.getString("i")});
                }else{
                    //insert
                    values.put(dataDB.ID, job.getString("i"));
                    if( Long.parseLong(job.getString("u")) > CommonUtil.newDate()) {
                        values.put(dataDB.ISREAD, "new");
                    }else{
                        values.put(dataDB.ISREAD, "read");
                    }
                    mDb.insert(dataDB.TB_NEW_NOTICE, null, values);
                }
                nCursor.close();
            }
            mDb.setTransactionSuccessful();
        }catch (Exception ignore){
        }finally {
            mDb.endTransaction();
        }
    }

    public int getNoticeNewCnt(String boardtype){
        open();
        int cnt = 0;
        Cursor cursor = mDb.rawQuery("SELECT count(*) as cnt FROM "+ dataDB.TB_NEW_NOTICE+" WHERE "+ dataDB.B_TYPE+"='"+boardtype+"' AND "+ dataDB.REGDATE+">="+CommonUtil.newDate()+" AND "+ dataDB.ISREAD+"='new' AND "+ dataDB.ISDEL+"='N'", null);
        if( cursor.getCount() > 0){
            if( cursor.moveToFirst()) {
                cnt = cursor.getInt(cursor.getColumnIndex("cnt"));
            }
        }
//        Cursor ccursor = mDb.rawQuery("SELECT * FROM "+TimecashDB.TB_NEW_NOTICE+" WHERE "+TimecashDB.B_TYPE+"='"+boardtype+"' AND "+TimecashDB.REGDATE+">="+CommonUtil.newDate()+" AND "+TimecashDB.ISREAD+"='' AND "+TimecashDB.ISDEL+"='N'", null);
        cursor.close();
        return cnt;
    }

    public void readNewNotice(NoticeModel noticeModel){
        ContentValues values = new ContentValues();
        open();
        values.put(dataDB.ID, noticeModel.getId());
        values.put(dataDB.B_TYPE, noticeModel.getB_type());
        values.put(dataDB.ISREAD, "read");
        Cursor nCursor = mDb.query(dataDB.TB_NEW_NOTICE, new String[]{dataDB.ISREAD}, dataDB.ID+"=?", new String[]{noticeModel.getId()}, null, null, null);
        if( nCursor.getCount() > 0){
            mDb.update(dataDB.TB_NEW_NOTICE, values, dataDB.ID+"=?", new String[]{noticeModel.getId()});
        }else{
            mDb.insert(dataDB.TB_NEW_NOTICE, null, values);
        }
    }

    public String isNoticeRead(String id){
        Cursor nCursor = mDb.query(dataDB.TB_NEW_NOTICE, new String[]{dataDB.ISREAD}, dataDB.ID+"=?", new String[]{id}, null, null, null);
        if( nCursor.getCount() > 0){
            if( nCursor.moveToFirst()) {
                return nCursor.getString(nCursor.getColumnIndex(dataDB.ISREAD));
            }else{
                return "";
            }
        }else{
            return "";
        }
    }

    public boolean insertNotice(JSONObject job){
        ContentValues values = new ContentValues();
        open();
        try {
            values.put(dataDB.ID, job.getString("i"));
            values.put(dataDB.SUBJECT, job.getString("s"));
            values.put(dataDB.CONTENT, job.getString("c"));
            values.put(dataDB.B_TYPE, job.getString("b"));
            values.put(dataDB.ISREAD, "");
            values.put(dataDB.UPDATE_DATE, job.getString("u"));
            values.put(dataDB.REGDATE, job.getString("t"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mDb.insert(dataDB.TB_NOTICE, null, values) > 0;
    }

    public int getNoticeMax(){
        int maxId = 0;
        open();
        Cursor cursor = mDb.rawQuery("SELECT MAX("+ dataDB.ID+") as maxId FROM "+ dataDB.TB_NOTICE+" LIMIT 1", null);
        if( cursor.getCount() > 0){
            if( cursor.moveToFirst()) {
                maxId = cursor.getInt(cursor.getColumnIndex("maxId"));
            }
        }
        cursor.close();
        return maxId;
    }

    public int getNoticeCnt(String boardtype){
        open();
        int cnt = 0;
        Cursor cursor = mDb.rawQuery("SELECT count(*) as cnt FROM "+ dataDB.TB_NOTICE+" WHERE "+ dataDB.B_TYPE+"='"+boardtype+"' AND "+ dataDB.REGDATE+">="+CommonUtil.newDate()+" AND "+ dataDB.ISREAD+"=''", null);
        if( cursor.getCount() > 0){
            if( cursor.moveToFirst()) {
                cnt = cursor.getInt(cursor.getColumnIndex("cnt"));
            }
        }
        cursor.close();
        return cnt;
    }

    public ArrayList<NoticeModel> getNoticeList(String boardtype){
        ArrayList<NoticeModel> noticeList = new ArrayList<>();
        try {
            open();
            String columns[] = {dataDB.ID, dataDB.SUBJECT, dataDB.CONTENT, dataDB.B_TYPE, dataDB.ISREAD, dataDB.UPDATE_DATE, dataDB.REGDATE};
            Cursor cursor = mDb.query(dataDB.TB_NOTICE, columns, dataDB.B_TYPE+"='"+boardtype+"'", null, null, null, dataDB.REGDATE+" desc");
            if( cursor.getCount() > 0){
                while(cursor.moveToNext()){
                    NoticeModel notice = new NoticeModel(cursor.getString(cursor.getColumnIndex(dataDB.ID)), cursor.getString(cursor.getColumnIndex(dataDB.SUBJECT)),
                            cursor.getString(cursor.getColumnIndex(dataDB.CONTENT)), cursor.getString(cursor.getColumnIndex(dataDB.B_TYPE)),
                            cursor.getString(cursor.getColumnIndex(dataDB.ISREAD)), cursor.getString(cursor.getColumnIndex(dataDB.UPDATE_DATE)), cursor.getString(cursor.getColumnIndex(dataDB.REGDATE)));
                    noticeList.add(notice);
                }
            }
            cursor.close();
        } catch (Exception ignore) {

        }
        return noticeList;
    }

    public void readNotice(NoticeModel noticeModel){
        ContentValues values = new ContentValues();
        open();
        values.put(dataDB.ID, noticeModel.getId());
        values.put(dataDB.SUBJECT, noticeModel.getSubject());
        values.put(dataDB.CONTENT, noticeModel.getContent());
        values.put(dataDB.B_TYPE, noticeModel.getB_type());
        values.put(dataDB.ISREAD, "read");
        values.put(dataDB.UPDATE_DATE, noticeModel.getUpdateDate());
        values.put(dataDB.REGDATE, noticeModel.getRegDate());
        mDb.update(dataDB.TB_NOTICE, values, dataDB.ID+"=?", new String[]{noticeModel.getId()});
    }

    public void insertQueue(String reward_key, int reward_gold){
        try{
            open();
            mDb.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(dataDB.REWARD_KEY, reward_key);
            values.put(dataDB.REWARD_GOLD, reward_gold);
            mDb.insert(dataDB.TB_QUEUE, null, values);
            mDb.setTransactionSuccessful();
        }catch (Exception ignore){
        }finally {
            mDb.endTransaction();
        }
    }

    public String getNoticePop(String id){
        String expire = "";
        try{
            open();
            String columns[] = {dataDB.ID, dataDB.EXPIRE};
            Cursor cursor = mDb.query(dataDB.TB_NOTICE_POPUP, columns, dataDB.ID+"='"+id+"'", null, null, null, null);
            if( cursor.getCount() > 0){
                if( cursor.moveToFirst()){
                    expire = cursor.getString(cursor.getColumnIndex(dataDB.EXPIRE));
                }
            }
        }catch (Exception ignore){
        }
        return expire;
    }

    public void setNoticePop(String id, String expire){
        try{
            open();
            mDb.beginTransaction();
            String columns[] = {dataDB.ID, dataDB.EXPIRE};
            Cursor cursor = mDb.query(dataDB.TB_NOTICE_POPUP, columns, dataDB.ID+"='"+id+"'", null, null, null, null);
            ContentValues values = new ContentValues();
            values.put(dataDB.ID, id);
            values.put(dataDB.EXPIRE, expire);
            if( cursor.getCount() > 0){
                //update
                mDb.update(dataDB.TB_NOTICE_POPUP, values, dataDB.ID+"=?", new String[]{id});
            }else{
                //insert
                mDb.insert(dataDB.TB_NOTICE_POPUP, null, values);
            }
            cursor.close();
            mDb.setTransactionSuccessful();
        }catch (Exception ignore){
        }finally {
            mDb.endTransaction();
        }
    }

    public boolean insertCPIPackage(String packageName){
        ContentValues values = new ContentValues();
        open();
        try {
            values.put(dataDB.PACKAGE_NAME, packageName);
            values.put(dataDB.STATUS, "down");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mDb.insert(dataDB.TB_PACKAGE_CHK, null, values) > 0;
    }

    public boolean deleteCPIPackage(String packageName){
        try{
            open();
            return mDb.delete(dataDB.TB_PACKAGE_CHK, dataDB.PACKAGE_NAME+"='"+packageName+"'", null) > 0;
        }catch (Exception ignore){
            ignore.printStackTrace();
        }
        return false;
    }

    public boolean chkCPIPackage(String packageName){
        try {
            String columns[] = {dataDB.PACKAGE_NAME, dataDB.STATUS};
            open();
            Cursor cursor = mDb.query(dataDB.TB_PACKAGE_CHK, columns, dataDB.PACKAGE_NAME + "='"+packageName+"'", null, null, null, null);
            if( cursor.getCount() > 0) {
                return false;
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public boolean chkNewGiftbox(){
        try{
            String columns[] = {dataDB.ID};
            open();
            Cursor cursor = mDb.query(dataDB.TB_GIFT_BOX, columns, dataDB.ISREAD + "='new'", null, null, null, null);
            if( cursor.getCount() > 0){
                return true;
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public int getGiftBoxNewCnt(){
        int cnt = 0;
        try{
            open();
            Cursor cursor = mDb.rawQuery("SELECT count(*) as cnt FROM "+ dataDB.TB_GIFT_BOX+" WHERE "+ dataDB.ISREAD+"='new'", null);
            if( cursor.getCount() > 0){
                if( cursor.moveToFirst()) {
                    cnt = cursor.getInt(cursor.getColumnIndex("cnt"));
                }
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return cnt;
    }

    public int getGiftBoxLastId(){
        int maxId = 0;
        try {
            open();
            Cursor cursor = mDb.rawQuery("SELECT MAX(" + dataDB.ID + ") as maxId FROM " + dataDB.TB_GIFT_BOX + " LIMIT 1", null);
            if (cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    maxId = cursor.getInt(cursor.getColumnIndex("maxId"));
                }
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return maxId;
    }

    public void insertGiftBox(JSONArray jobArr){
        try{
            open();
            mDb.beginTransaction();
            for(int i=0;i<jobArr.length();i++){
                JSONObject job = jobArr.getJSONObject(i);
                Cursor nCursor = mDb.query(dataDB.TB_GIFT_BOX, new String[]{dataDB.ID, dataDB.ISREAD}, dataDB.ID+"=?", new String[]{job.getString("id")}, null, null, null);
                ContentValues values = new ContentValues();
                values.put(dataDB.REGDATE, job.getString("t"));
                if( nCursor.getCount() > 0){
                    //update
                    values.put(dataDB.REGDATE, job.getString("t"));
                    values.put(dataDB.GIFT_TITLE, job.getString("gtitle"));
                    values.put(dataDB.GIFT_CONTENT, job.getString("gcontent"));
                    mDb.update(dataDB.TB_GIFT_BOX, values, dataDB.ID+"=?", new String[]{job.getString("id")});
                }else{
                    //insert
                    values.put(dataDB.ID, job.getString("id"));
                    if( Long.parseLong(job.getString("t")) > CommonUtil.newDate()) {
                        values.put(dataDB.ISREAD, "new");
                    }else{
                        values.put(dataDB.ISREAD, "read");
                    }
                    values.put(dataDB.GIFT_TITLE, job.getString("gtitle"));
                    values.put(dataDB.GIFT_CONTENT, job.getString("gcontent"));
                    mDb.insert(dataDB.TB_GIFT_BOX, null, values);
                }
                nCursor.close();
            }
            mDb.setTransactionSuccessful();
        }catch (Exception ignore){
            ignore.printStackTrace();
        }finally {
            mDb.endTransaction();
        }
    }

    public ArrayList<GiftBoxModel> getGiftBoxList(){
        ArrayList<GiftBoxModel> giftboxList = new ArrayList<>();
        try {
            open();
            String columns[] = {dataDB.ID, dataDB.GIFT_TITLE, dataDB.GIFT_CONTENT, dataDB.ISREAD, dataDB.REGDATE};
            Cursor cursor = mDb.query(dataDB.TB_GIFT_BOX, columns, null, null, null, null, dataDB.ID+" desc");
            if( cursor.getCount() > 0){
                while(cursor.moveToNext()){
                    GiftBoxModel giftBoxModel = new GiftBoxModel();
                    giftBoxModel.setId(cursor.getString(cursor.getColumnIndex(dataDB.ID)));
                    giftBoxModel.setGiftTitle(cursor.getString(cursor.getColumnIndex(dataDB.GIFT_TITLE)));
                    giftBoxModel.setGiftContent(cursor.getString(cursor.getColumnIndex(dataDB.GIFT_CONTENT)));
                    giftBoxModel.setIsRead(cursor.getString(cursor.getColumnIndex(dataDB.ISREAD)));
                    giftBoxModel.setRegdate(cursor.getString(cursor.getColumnIndex(dataDB.REGDATE)));

                    giftboxList.add(giftBoxModel);
                }
            }
            cursor.close();
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        return giftboxList;
    }

    public void readNewGiftBox(GiftBoxModel giftBoxModel){
        ContentValues values = new ContentValues();
        open();
        values.put(dataDB.ID, giftBoxModel.getId());
        values.put(dataDB.ISREAD, "read");
        Cursor nCursor = mDb.query(dataDB.TB_GIFT_BOX, new String[]{dataDB.ISREAD}, dataDB.ID+"=?", new String[]{giftBoxModel.getId()}, null, null, null);
        if( nCursor.getCount() > 0){
            mDb.update(dataDB.TB_GIFT_BOX, values, dataDB.ID+"=?", new String[]{giftBoxModel.getId()});
        }
    }

    public ArrayList<AlarmNoti> getAlarmNoti(){
        ArrayList<AlarmNoti> alarmNotis = new ArrayList<>();
        try{
            open();
            String columns[] = {dataDB.NOTI_ID, dataDB.TITLE, dataDB.MESSAGE, dataDB.EXPIRE_TIMESTAMP};
            Cursor cursor = mDb.query(dataDB.TB_ALARM, columns, null, null, null, null, dataDB.NOTI_ID+" desc");
            if( cursor.getCount() > 0){
                while(cursor.moveToNext()){
                    AlarmNoti alarmNoti = new AlarmNoti();
                    alarmNoti.setNotiid(cursor.getString(cursor.getColumnIndex(dataDB.NOTI_ID)));
                    alarmNoti.setEx_time(cursor.getString(cursor.getColumnIndex(dataDB.EXPIRE_TIMESTAMP)));
                    alarmNoti.setTitle(cursor.getString(cursor.getColumnIndex(dataDB.TITLE)));
                    alarmNoti.setMessage(cursor.getString(cursor.getColumnIndex(dataDB.MESSAGE)));
                    alarmNotis.add(alarmNoti);
                }
            }
            cursor.close();
        }catch (Exception ignore){
            ignore.printStackTrace();
        }
        return alarmNotis;
    }

    public void setAlarmNoti(String notiid, String title, String message, String ex_time){
        ContentValues values = new ContentValues();
        open();
        try {
            values.put(dataDB.NOTI_ID, notiid);
            values.put(dataDB.EXPIRE_TIMESTAMP, ex_time);
            values.put(dataDB.TITLE, title);
            values.put(dataDB.MESSAGE, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Cursor nCursor = mDb.query(dataDB.TB_ALARM, new String[]{dataDB.NOTI_ID}, dataDB.NOTI_ID+"=?", new String[]{notiid}, null, null, null);
        if( nCursor.getCount() > 0){
            mDb.update(dataDB.TB_ALARM, values, dataDB.NOTI_ID+"=?", new String[]{notiid});
        }else{
            mDb.insert(dataDB.TB_ALARM, null, values);
        }
    }

    public void delAlarmNoti(String notiid){
        try{
            open();
            mDb.delete(dataDB.TB_ALARM, dataDB.NOTI_ID+" = '"+notiid+"'", null);
        }catch (Exception ignore){
            ignore.printStackTrace();
        }
    }
}