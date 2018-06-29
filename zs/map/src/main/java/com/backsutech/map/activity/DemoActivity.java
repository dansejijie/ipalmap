package com.backsutech.map.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.backsutech.map.R;

public class DemoActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        findViewById(R.id.ipalmap_btn_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(DemoActivity.this, ShowMapActivity.class);
                intent.putExtra("call_number", "00001084");
                intent.putExtra("book_name", "书目名字");
                intent.putExtra("org_id","144");
                intent.putExtra("location","1");
                intent.putExtra("group","1");

                startActivity(intent);
            }
        });
    }
}
