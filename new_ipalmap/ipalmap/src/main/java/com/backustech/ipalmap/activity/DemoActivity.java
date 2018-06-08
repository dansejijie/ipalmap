package com.backustech.ipalmap.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.backustech.ipalmap.R;

public class DemoActivity extends AppCompatActivity {


    String[] permissions = new String[]{Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION};
    boolean hasPermission = false;
    private static final int REQUEST_PERMISSION = 0x100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        findViewById(R.id.ipalmap_btn_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!hasPermission){
                    return;
                }
                Intent intent=new Intent(DemoActivity.this, ShowMapActivity.class);
                intent.putExtra("call_number", "K109/426/2015");
                intent.putExtra("book_name", "书目名字");
                startActivity(intent);
            }
        });

        boolean flag = true;

        for(int i = 0;i<permissions.length;i++){
            if(ContextCompat.checkSelfPermission(DemoActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED){
                flag=false;
            }
        }

        hasPermission=true;
        if (!flag) {
            //申请权限
            ActivityCompat.requestPermissions(DemoActivity.this, permissions, REQUEST_PERMISSION);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                for(int i=0;i<grantResults.length;i++){
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        if (ActivityCompat.shouldShowRequestPermissionRationale(DemoActivity.this,permissions[i])) {
                            Toast.makeText(DemoActivity.this,"权限被拒绝,地图渲染可能出现问题!",Toast.LENGTH_SHORT).show();
                            hasPermission=false;
                        }
                    }
                }
            }
            default:
                break;
        }
    }

}
