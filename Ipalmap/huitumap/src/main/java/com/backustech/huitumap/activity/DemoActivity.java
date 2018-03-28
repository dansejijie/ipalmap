package com.backustech.huitumap.activity;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.backustech.huitumap.R;
import com.backustech.huitumap.constants.Constant;
import com.backustech.huitumap.utils.FileUtils;
import com.backustech.huitumap.utils.ToastUtils;
import com.palmaplus.nagrand.core.Engine;



public class DemoActivity extends Activity {


    /**
     * 申请读写文件权限返回标志字段
     */
    private static final int REQUEST_WRITE_STORAGE = 112;

    private boolean hasPermission=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnShow= (Button) findViewById(R.id.btn_show);
        Button btnNavigation= (Button) findViewById(R.id.btn_navigation);
        Button btnTest=(Button) findViewById(R.id.btn_test);

        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!hasPermission){
                    ToastUtils.showToast("需要文件操作权限");
                    return;
                }

                Intent intent=new Intent(DemoActivity.this,IpalmapShowMapActivity.class);
                intent.putExtra("call_number","K109/426/2015");
                intent.putExtra("book_name","书目名字");
                startActivity(intent);
            }
        });

        btnNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DemoActivity.this,IpalmapNavigationActivity.class);
                startActivity(intent);
            }
        });

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DemoActivity.this,TestActivity.class);
                startActivity(intent);
            }
        });


        //是否拥有读写文件的权限,Android6.0及以上需开发者格外注意权限问题.
        hasPermission = (ContextCompat.checkSelfPermission(DemoActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            //申请权限
            ActivityCompat.requestPermissions(DemoActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
            return;
        } else {
            copyLuaToStorage();

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
        if (FileUtils.copyLuaFinished()) {
            return;
        }
        if (FileUtils.checkoutSDCard()) {
            Log.d("lua:", "开始复制!");
            FileUtils.copyDirToSDCardFromAsserts(this, Constant.LUR_NAME, "font");
            FileUtils.copyDirToSDCardFromAsserts(this, Constant.LUR_NAME, Constant.LUR_NAME);
            Log.d("lua:", "复制完成!");
        } else {
            ToastUtils.showToast(R.string.do_not_find_sdcard);
        }
    }


}
