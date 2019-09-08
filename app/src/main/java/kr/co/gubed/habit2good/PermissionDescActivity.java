package kr.co.gubed.habit2good;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

import kr.co.gubed.habit2good.gpoint.activity.SignActivity;

public class PermissionDescActivity extends AppCompatActivity {
    private String TAG = getClass().getName();
    private Button btnConfirm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        btnConfirm = findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PermissionListener permissionListener = new PermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            //Toast.makeText(getApplicationContext(), "접근 권한 요청을 허용하셨습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), SignActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                            Toast.makeText(getApplicationContext(), "필수 권한 요청을 허용해 주세요.", Toast.LENGTH_SHORT).show();
                        }
                    };

                    TedPermission.with(PermissionDescActivity.this)
                            .setPermissionListener(permissionListener)
                            .setDeniedMessage("필수 권한 요청을 거부하시면\n서비스 사용이 불가합니다. \n\n[설정] 눌러서 [권한]을 변경해 주세요.")
                            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.READ_PHONE_STATE,
                                            Manifest.permission.READ_CONTACTS,
                                            Manifest.permission.GET_ACCOUNTS)
                            .check();
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        String[] permissionArr = getPermissionArray();
        if( permissionArr.length > 0){      // 권한 허가 필요
            //권한 설정 설명 화면 노출
        }else{                              // 권한 설정 완료
            Intent intent = new Intent(getApplicationContext(), SignActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityCompat.finishAffinity(this);
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
