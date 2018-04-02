package com.backustech.ipalmap.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backustech.ipalmap.R;
import com.backustech.ipalmap.fragments.NavMapFragemnt;
import com.backustech.ipalmap.fragments.ShowMapFragemnt;
import com.backustech.ipalmap.utils.Constant;
import com.backustech.ipalmap.utils.FragmentUtils;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by tygzx on 2018/3/29.
 */

public class IpalmapActivity extends AppCompatActivity implements View.OnClickListener {

    //标题栏
    LinearLayout fl_header_back;
    TextView tv_header_title;
    LinearLayout ll_header_nav;
    //当前页面是展示页 //"navMap";
    String currentPage = "showMap";

    ShowMapFragemnt showFragment;
    NavMapFragemnt navFragment;
    Bundle mData;
    //图书加载状态
    private boolean bookLoaded = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_ipalmap_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.ipalmap_header_bar);
        initHeaderView();
        showFragment = new ShowMapFragemnt();
        FragmentUtils.replaceFragment(getSupportFragmentManager(), showFragment, R.id.ipalmap_fragment_container, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDataAsync();
    }

    private void initHeaderView() {
        fl_header_back = (LinearLayout) findViewById(R.id.ipalmap_header_bar_back);
        tv_header_title = (TextView) findViewById(R.id.ipalmap_header_bar_title);
        ll_header_nav = (LinearLayout) findViewById(R.id.ipalmap_header_bar_nav);
        fl_header_back.setOnClickListener(this);
        ll_header_nav.setOnClickListener(this);
        showShowHeaderBarMap();
    }

    private void showShowHeaderBarMap() {
        currentPage = "showMap";
        ll_header_nav.setVisibility(View.VISIBLE);
        tv_header_title.setVisibility(View.GONE);
    }

    private void showNavHeaderBarMap() {
        currentPage = "navMap";
        ll_header_nav.setVisibility(View.GONE);
        tv_header_title.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ipalmap_header_bar_back) {
            if (currentPage == "showMap") {
                finish();
            } else {
                showShowHeaderBarMap();
                showFragment = new ShowMapFragemnt();
                showFragment.setArguments(mData);
                FragmentUtils.replaceFragment(getSupportFragmentManager(), showFragment, R.id.ipalmap_fragment_container, false);
            }
        } else if (id == R.id.ipalmap_header_bar_nav) {
            if (!bookLoaded) {
                return;
            }
            currentPage = "navMap";
            showNavHeaderBarMap();
            navFragment = new NavMapFragemnt();
            navFragment.setArguments(mData);
            FragmentUtils.replaceFragment(getSupportFragmentManager(), navFragment, R.id.ipalmap_fragment_container, false);
        }
    }

    private void getDataAsync() {

        String call_number = "";
        String book_name = "";
        Intent intent = getIntent();
        try {
            call_number = intent.getStringExtra("call_number");
            book_name = intent.getStringExtra("book_name");
        } catch (Exception e) {
            Toast.makeText(this, "请确认是否传入正确参数call_number和book_name", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Constant.SERVER_BACK_URL + Constant.BOOK_URL + "?call_number=" + call_number + "&book_name=" + book_name + "&LAB_JSON=1";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(IpalmapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {//回调的方法执行在子线程。
                    Log.d("ipalmap", "获取数据成功了");
                    handleBookJSON(response.body().string());
                }
            }
        });
    }

    private void handleBookJSON(String json) {

        mData = new Bundle();
        try {
            JSONObject result = new JSONObject(json);
            JSONObject data = result.getJSONObject("data");
            JSONObject bookshelf = data.getJSONObject("bookshelf");
            JSONObject room = data.getJSONObject("room");
            JSONObject index = data.getJSONObject("index");

            mData.putString("address", room.getString("library_title") + " " + room.getString("floor") + " " + room.getString("name"));
            mData.putString("bookshelfGuide", bookshelf.getString("name") + index.getString("title"));
            mData.putString("bookNumber", data.getString("callNumber"));
            mData.putString("bookId", index.getString("id"));
            mData.putString("bookTitle", data.getString("bookName"));
            mData.putInt("bookShelfRows", Integer.parseInt(bookshelf.getString("specifications_row_count")));
            mData.putInt("bookShelfColumns", Integer.parseInt(bookshelf.getString("specifications_column_count")));
            mData.putInt("bookRow", Integer.parseInt(index.getString("row")));
            mData.putInt("bookColumn", Integer.parseInt(index.getString("column")));
            mData.putDouble("map_x_value", Double.parseDouble(bookshelf.getString("map_x_value")));
            mData.putDouble("map_y_value", Double.parseDouble(bookshelf.getString("map_y_value")));
            bookLoaded = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showFragment.setData(mData);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(IpalmapActivity.this, "书籍信息处理失败", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}
