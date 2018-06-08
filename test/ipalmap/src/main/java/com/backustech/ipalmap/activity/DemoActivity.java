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
import com.backustech.ipalmap.utils.Constant;
import com.backustech.ipalmap.utils.FileUtilsTools;
import com.palmaplus.nagrand.core.Engine;

public class DemoActivity extends AppCompatActivity {

    /**
     * 申请读写文件权限返回标志字段
     */
    private static final int REQUEST_WRITE_STORAGE = 0x100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        findViewById(R.id.ipalmap_btn_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DemoActivity.this, IpalmapActivity.class);
                intent.putExtra("call_number", "K109/426/2015");
                intent.putExtra("book_name", "书目名字");
                startActivity(intent);
            }
        });

        boolean hasPermission = (ContextCompat.checkSelfPermission(DemoActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            //申请权限
            ActivityCompat.requestPermissions(DemoActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        } else {
            copyLuaToStorage();
            Engine engine = Engine.getInstance(); //初始化引擎
            engine.startWithLicense(Constant.APP_KEY, this);//设置验证license，可以通过开发者平台去查找自己的license
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(DemoActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Toast.makeText(DemoActivity.this,"申请读写权限被拒绝,Android6.0及以上地图渲染可能出现问题!",Toast.LENGTH_SHORT).show();
                        Log.d("permission: ", getResources().getString(R.string.permission_denied));
                    }
                } else {
                    copyLuaToStorage();
                    Engine engine = Engine.getInstance(); //初始化引擎
                    engine.startWithLicense(Constant.APP_KEY, this);//设置验证license，可以通过开发者平台去查找自己的license
                }
            }
            default:
                break;
        }
    }

    /**
     * 把Asset目录下的lua配置文件复制到sd卡内
     */
    public void copyLuaToStorage() {
        FileUtilsTools.copyDirToSDCardFromAsserts(this, "Nagrand/lua", "font");
        FileUtilsTools.copyDirToSDCardFromAsserts(this, "Nagrand/lua", "Nagrand/lua");
    }
}
