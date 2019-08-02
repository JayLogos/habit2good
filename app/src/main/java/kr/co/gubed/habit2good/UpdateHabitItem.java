package kr.co.gubed.habit2good;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import kr.co.gubed.habit2good.gpoint.util.CommonUtil;

public class UpdateHabitItem extends AppCompatActivity {
    private static final String EXTRA_HABITID = "habitid";
    private static final String EXTRA_POSITION = "position";
    private static final String EXTRA_HNAME = "hname";
    private static final String EXTRA_GOALIMG = "goalimg";
    private static final String EXTRA_GOAL = "goal";
    private static final String EXTRA_SIGNAL = "signal";
    private static final String EXTRA_REWARD = "reward";
    private static final String EXTRA_CATEGORY = "category";
    private static final String EXTRA_Sdate = "sdate";
    private static final String EXTRA_Edate = "edate";
    private static final String EXTRA_CYCLE = "cycle";
    private static final String EXTRA_COUNT = "count";
    private static final String EXTRA_UNIT = "unit";

    //Toolbar tb_updateHabit;

    private ImageView mGoalImg;
    private EditText mHname;
    private EditText mGoal;
    private EditText mSignal;
    private EditText mReward;
    private Button mSdate;
    private Button mEdate;
    private Spinner mCycle;
    private EditText mCount;
    private EditText mUnit;
    private int habitid;
    private int position;

    private String categoryName;
    private String cycleName;

    String mCurrentPhotoPath;
    File goalImage;

    private HabitDbAdapter dbAdapter;

    Date startDate, endDate;
    DateFormat dateFormat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_item_settings);
        Intent intent = getIntent();

        //tb_updateHabit = findViewById(R.id.toolbar);
        //tb_updateHabit.setTitle("좋은 습관 변경하기");
        //setSupportActionBar(tb_updateHabit);

        mGoalImg = findViewById(R.id.circle_image);
        mHname = findViewById(R.id.et_habitName);
        mGoal = findViewById(R.id.et_habitGoal);
        mSignal = findViewById(R.id.et_habitSignal);
        mReward = findViewById(R.id.et_habitReward);
        Spinner mCategory = findViewById(R.id.spn_category);
        mSdate = findViewById(R.id.btn_habitSdate);
        mEdate = findViewById(R.id.btn_habitEdate);
        mCycle = findViewById(R.id.spn_cycle);
        mCount = findViewById(R.id.et_habitCount);
        mUnit = findViewById(R.id.et_habitUnit);

        //Toolbar 의 왼쪽에 버튼을 추가하고 버튼의 아이콘을 바꾼다.
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.mipmap.outline_keyboard_arrow_left_white_18);
        actionBar.setTitle("좋은습관 수정하기");

        mGoalImg.setOnClickListener(onGoalImgSelectedListener);
        mSdate.setOnClickListener(onSelectDateListenerSdate);
        mEdate.setOnClickListener(onSelectDateListenerEdate);

        mCategory.setOnItemSelectedListener(onCategorySelectedListener);
        mCycle.setOnItemSelectedListener(onCycleSelectedListener);

        habitid = Objects.requireNonNull(intent.getExtras()).getInt(EXTRA_HABITID);
        position = intent.getExtras().getInt(EXTRA_POSITION);

        mCurrentPhotoPath = intent.getExtras().getString(EXTRA_GOALIMG);

        if (mCurrentPhotoPath == null || mCurrentPhotoPath.equals("default")) {
            Glide.with(this).load(R.drawable.ic_habit2good_512)
                    .apply(new RequestOptions().circleCrop())
                    .into(mGoalImg);
        } else {
            Glide.with(this).load(mCurrentPhotoPath)
                    .apply(new RequestOptions().circleCrop())
                    .into(mGoalImg);
        }

        mHname.setText(intent.getExtras().getString(EXTRA_HNAME));
        mGoal.setText(intent.getExtras().getString(EXTRA_GOAL));
        mSignal.setText(intent.getExtras().getString(EXTRA_SIGNAL));
        mReward.setText(intent.getExtras().getString(EXTRA_REWARD));
        //Log.i("카테고리 초기화", "category size="+mCategory.getAdapter().getCount());
        for (int i = 0; i< mCategory.getAdapter().getCount() ; i++) {
            //Log.i("카테고리 초기화", "category="+mCategory.getItemAtPosition(i).toString()+" old category="+intent.getExtras().getString(EXTRA_CATEGORY));
            if (mCategory.getItemAtPosition(i).toString().equals(intent.getExtras().getString(EXTRA_CATEGORY))) {
                //Log.i("카테고리 초기화", "position="+i+" name="+mCategory.getItemAtPosition(i));
                mCategory.setSelection(i);
                break;
            }
        }
        mSdate.setText(intent.getExtras().getString(EXTRA_Sdate));
        mEdate.setText(intent.getExtras().getString(EXTRA_Edate));
        dateFormat = new SimpleDateFormat("yyyy.MM.dd");

        for (int i=0; i< mCycle.getAdapter().getCount() ; i++) {
            if (mCycle.getItemAtPosition(i) == intent.getExtras().getString(EXTRA_CYCLE)) {
                mCycle.setSelection(i);
                break;
            }
        }
        String cycle = intent.getExtras().getString(EXTRA_CYCLE);
        if (cycle.equals("매일")) {
            mCycle.setSelection(0);
        } else if (cycle.equals("매주")) {
            mCycle.setSelection(1);
        } else if (cycle.equals("매월")) {
            mCycle.setSelection(2);
        } else if (cycle.equals("매년")) {
            mCycle.setSelection(3);
        }
        mCount.setText(intent.getExtras().getString(EXTRA_COUNT));
        mUnit.setText(intent.getExtras().getString(EXTRA_UNIT));


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_update_habit, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                return true;
            case R.id.action_save:
                updateHabit();
                setResult(CommonUtil.REQUEST_CODE_HABIT_UPDATE);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final View.OnClickListener onGoalImgSelectedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // dialog 를 통해서 갤러리, 카메라, 기본 이미지 선택
            showDialogForGoalImage();
        }
    };

    private void updateHabit() {
        int count = 0;

        String hname = mHname.getText().toString().trim();
        String goalimg = mCurrentPhotoPath;
        String goal = mGoal.getText().toString().trim();
        String signal = mSignal.getText().toString().trim();
        String reward = mReward.getText().toString().trim();
        String category = categoryName;
        String sdate = mSdate.getText().toString().trim();
        String edate = mEdate.getText().toString().trim();
        String cycle = cycleName;
        if (mCount.getText().length() > 0) {
            if (mCount.getText().toString().equals(".")) {
                count = 0;
            } else {
                count = (int) Double.parseDouble(mCount.getText().toString().trim());
            }
        }
        String unit = mUnit.getText().toString().trim();

        dbAdapter = new HabitDbAdapter(this);
        dbAdapter.open();

        Habit habit = new Habit(habitid, position, hname, goalimg, goal, signal, reward, category, sdate, edate, cycle, count, unit);

        dbAdapter.updateHabit(habit);

        // destroy 된 view 를 call 했기 때문에 Null point 참조 오류 발생
        //HabitTabFragment fragment = new HabitTabFragment();
        //fragment.addOneList(habit);
    }

    private final View.OnClickListener onSelectDateListenerSdate  = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                startDate = dateFormat.parse(mSdate.getText().toString().trim());
                endDate = dateFormat.parse(mEdate.getText().toString().trim());
            } catch (ParseException e){
                e.printStackTrace();
            }

            MyDatePicker picker = createDatePicker(startDate);
            picker.show(getSupportFragmentManager(), onDatePickListener);
        }

        final MyDatePicker.OnDatePickListener onDatePickListener = new MyDatePicker.OnDatePickListener() {
            @Override
            public void onDatePick(Calendar calendar) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
                mSdate.setText(format.format(calendar.getTime()));
            }
        };
    };

    private final View.OnClickListener onSelectDateListenerEdate  = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                startDate = dateFormat.parse(mSdate.getText().toString().trim());
                endDate = dateFormat.parse(mEdate.getText().toString().trim());
            } catch (ParseException e){
                e.printStackTrace();
            }

            MyDatePicker picker = createDatePicker(endDate);
            picker.show(getSupportFragmentManager(), onDatePickListener);
        }

        final MyDatePicker.OnDatePickListener onDatePickListener = new MyDatePicker.OnDatePickListener() {
            @Override
            public void onDatePick(Calendar calendar) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
                mEdate.setText(format.format(calendar.getTime()));
            }
        };
    };

    private MyDatePicker createDatePicker(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Log.i(getClass().getName(), "date picker year="+calendar.get(Calendar.YEAR)+" month="+calendar.get(Calendar.MONTH)+" day="+calendar.get(Calendar.DATE));
        MyDatePicker picker = new MyDatePicker.Builder(UpdateHabitItem.this)
                .setDate(calendar)
                //.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .create();
        return picker;
    }


    private final AdapterView.OnItemSelectedListener onCategorySelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            ((TextView) view).setTextSize(14);
            categoryName = parent.getItemAtPosition(position).toString();

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    private final AdapterView.OnItemSelectedListener onCycleSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            try {
                ((TextView) view).setTextSize(14);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            cycleName = parent.getItemAtPosition(position).toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void showDialogForGoalImage() {
        final List<String> ListItems = new ArrayList<>();

        ListItems.add("앨범에서 사진 선택");
        ListItems.add("사진 촬영");
        ListItems.add("기본 사진으로 변경");
        final CharSequence[] items = ListItems.toArray(new String[ListItems.size()]);

        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("목표 시각화 이미지 등록");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:     // 앨범
                        getImageFromAlbum();
                        break;
                    case 1:     // 촬영
                        //takePicture();
                        getImageFromCamera();
                        break;
                    case 2:     // 기본 사진
                        setGoalImageToDefault();
                        break;
                    default:
                        break;
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case AddHabit.REQUEST_PICK_ALBUM:
                Uri dataUri = data.getData();
                if (dataUri != null) {
                    cropImageFromAlbum(dataUri);
                }
                break;
            case AddHabit.REQUEST_TAKE_PHOTO:
                cropImageFromCamera();
                break;
            case AddHabit.REQUEST_CROP_IMAGE:
                Glide.with(this).load(mCurrentPhotoPath)
                        .apply(new RequestOptions().circleCrop())
                        .into(mGoalImg);

                galleryAddPic();
                break;
            default:
                break;
        }
    }

    private void getImageFromAlbum() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(intent, AddHabit.REQUEST_PICK_ALBUM);
        } else {
            Toast.makeText(getApplicationContext(), "갤러리 사용 권한을 허용해야 합니다.", Toast.LENGTH_LONG).show();
        }
    }

    private void getImageFromCamera() {

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try{
                goalImage = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Uri uri = FileProvider.getUriForFile(this, "kr.co.gubed.habit2good", goalImage);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(takePictureIntent, AddHabit.REQUEST_TAKE_PHOTO);
        } else {
            Toast.makeText(getApplicationContext(), "사진 사용 권한을 허용해야 합니다.", Toast.LENGTH_LONG).show();
        }
    }

    private void cropImageFromCamera() {
        Uri uri = FileProvider.getUriForFile(this, "kr.co.gubed.habit2good", goalImage);
        Intent intent = getCropIntent(uri, uri);
        startActivityForResult(intent, AddHabit.REQUEST_CROP_IMAGE);
    }

    private void cropImageFromAlbum(Uri inputUri) {
        try {
            goalImage = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri outputUri = Uri.fromFile(goalImage);
        Intent intent = getCropIntent(inputUri, outputUri);
        startActivityForResult(intent, AddHabit.REQUEST_CROP_IMAGE);
    }

    private Intent getCropIntent(Uri inputUri, Uri outputUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(inputUri, "image/*");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 500);
        intent.putExtra("outputY", 500);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

        return intent;
    }

    private void setGoalImageToDefault() {
        Glide.with(this).load(R.drawable.ic_habit2good_512)
                .apply(new RequestOptions().circleCrop())
                .into(mGoalImg);
        mCurrentPhotoPath = "default";
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "h2g_" + timeStamp + ".jpg";

        File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+"/"+imageFileName);

        mCurrentPhotoPath = storageDir.getAbsolutePath();
        Log.i(getClass().getName(), "mCurrentPhotoPath="+mCurrentPhotoPath);
        return storageDir;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    private void checkManifestPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                //Toast.makeText(getApplicationContext(), "접근 권한 요청을 허용하셨습니다.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(getApplicationContext(), "접근 권한 요청을 거부하셨습니다.", Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setDeniedMessage("권한 요청을 거절하시면 서비스 사용이 불가합니다. \n\n[설정] 눌러서 [권한]을 변경해 주세요.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

    }
}


