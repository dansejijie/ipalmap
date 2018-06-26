package com.backustech.ipalmap.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.backustech.ipalmap.R;
import com.backustech.ipalmap.other.PicassoImageLoader;
import com.backustech.ipalmap.utils.Constants;
import com.backustech.ipalmap.view.BookShelf;
import com.backustech.ipalmap.view.IpalmapHelpIndicatorView;
import com.palmap.gl.MapEngine;
import com.palmap.gl.geometry.Coordinate;
import com.palmap.gl.maps.Palmap;
import com.palmap.gl.maps.listener.OnMapReadyListener;
import com.palmap.gl.maps.listener.OnPalmapErrorListener;
import com.palmap.gl.model.Feature;
import com.palmap.gl.plugin.IBitmapLoader;
import com.palmap.gl.plugin.IBitmapLoaderProvider;
import com.palmap.gl.view.MapView;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by tygzx on 2018/6/1.
 */

public class ShowMapActivity extends Activity implements View.OnClickListener {

    //标题栏
    LinearLayout fl_header_back;
    LinearLayout ll_header_nav;

    FrameLayout palmapContainer;

    boolean bookLoaded = false;

    Bundle mData;

    Palmap palmap;
    MapView mapView;

    TextView tv_address;
    TextView tv_bookshelf_guide;
    TextView tv_book_number;
    TextView tv_book_title;

    PopupWindow mPopupWindow;
    ImageButton ib_help;

    LinearLayout ll_bookShelfWatch;
    //左下角橘黄色长条形的
    LinearLayout ll_bubbleError;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_ipalmap_show);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.ipalmap_header_bar);
        initView();
        getDataAsync();
    }

    private void initView() {
        initHeaderView();
        initControlView();
        //initMapView();
    }

    private void initControlView() {

        palmapContainer = findViewById(R.id.ipalmap_map_container);

        tv_address = (TextView) findViewById(R.id.ipalmap_tv_address);
        tv_bookshelf_guide = (TextView) findViewById(R.id.ipalmap_tv_bookshelf_guide);
        tv_book_number = (TextView) findViewById(R.id.ipalmap_tv_book_number);
        tv_book_title = (TextView) findViewById(R.id.ipalmap_tv_book_title);

        ib_help = (ImageButton) findViewById(R.id.ipalmap_ib_help);
        ib_help.setOnClickListener(this);

        ll_bookShelfWatch = (LinearLayout) findViewById(R.id.ipalmap_ll_bookshelf_guide);
        ll_bookShelfWatch.setOnClickListener(this);

        ll_bubbleError = (LinearLayout) findViewById(R.id.ipalmap_ll_error_info);
        ll_bubbleError.setOnClickListener(this);

        ImageButton ib_ReportDialog = (ImageButton) findViewById(R.id.ipalmap_ib_error);
        ib_ReportDialog.setOnClickListener(this);
    }

    private void initHeaderView() {
        fl_header_back = (LinearLayout) findViewById(R.id.ipalmap_header_bar_back);
        ll_header_nav = (LinearLayout) findViewById(R.id.ipalmap_header_bar_nav);
        findViewById(R.id.ipalmap_header_bar_title).setVisibility(View.GONE);
        fl_header_back.setOnClickListener(this);
        ll_header_nav.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (palmap != null) {
            palmap.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (palmap != null) {
            palmap.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (palmap != null) {
            palmap.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (palmap != null) {
            palmap.onLowMemory();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ipalmap_header_bar_back) {
            finish();
        } else if (id == R.id.ipalmap_header_bar_nav) {
            if (bookLoaded) {
                Intent intent = new Intent(ShowMapActivity.this, NavMapActivity.class);
                intent.putExtra("map_info", mData);
                startActivity(intent);
            }
        } else if (id == R.id.ipalmap_ib_help) {
            popupHelpView();
        } else if (id == R.id.ipalmap_ll_bookshelf_guide) {
            //点击了查看书籍在书架哪个位置的按钮
            popupBookShelf();
        } else if (id == R.id.ipalmap_btn_show_bookshelf) {
            //点击了报告书籍不在书架的按钮
            dismissPopup();
            report();
        } else if (id == R.id.ipalmap_ib_show_help_close) {
            //点击了关闭弹出框的按钮
            dismissPopup();
        } else if (id == R.id.ipalmap_ll_error_info) {
            //消失掉右下角橘黄色长条形的View
            ll_bubbleError.setVisibility(View.GONE);
        } else if (id == R.id.ipalmap_ib_error) {
            showReportDialog();
        }
    }

    private void popupHelpView() {

        backgroundAlpha(0.4f);
        final View rootView = findViewById(R.id.ipalmap_fl_content);
        rootView.setBackgroundColor(getResources().getColor(R.color.half_transparent));
        IpalmapHelpIndicatorView view = new IpalmapHelpIndicatorView(this);
        view.setOnCloseClickListener(new IpalmapHelpIndicatorView.OnCloseClickListener() {
            @Override
            public void onClick() {
                dismissPopup();
            }
        });
        view.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

        mPopupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);
                rootView.setBackgroundColor(getResources().getColor(R.color.transparent));
                mPopupWindow = null;
            }
        });
        mPopupWindow.showAtLocation(rootView, Gravity.BOTTOM | Gravity.LEFT, 0, 0);
    }

    private void popupBookShelf() {
        if (!bookLoaded) {
            return;
        }
        backgroundAlpha(0.4f);
        final View rootView = findViewById(R.id.ipalmap_fl_content);
        rootView.setBackgroundColor(getResources().getColor(R.color.half_transparent));
        View contentView = LayoutInflater.from(this).inflate(R.layout.ipalmap_show_book_shelf, null, false);
        BookShelf bookShelf = (BookShelf) contentView.findViewById(R.id.ipalmap_show_bookshelf);
        bookShelf.setData(mData.getInt("bookShelfRows"), mData.getInt("bookShelfColumns"), mData.getInt("bookRow"), mData.getInt("bookColumn"));
        Button btnReport = (Button) contentView.findViewById(R.id.ipalmap_btn_show_bookshelf);
        btnReport.setOnClickListener(this);
        ImageButton ibClose = (ImageButton) contentView.findViewById(R.id.ipalmap_ib_show_help_close);
        ibClose.setOnClickListener(this);

        mPopupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                rootView.setBackgroundColor(getResources().getColor(R.color.transparent));
                backgroundAlpha(1f);
                mPopupWindow = null;
            }
        });
        mPopupWindow.showAtLocation(rootView, Gravity.BOTTOM | Gravity.LEFT, 0, 0);

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

        String url = Constants.SERVER_BACK_URL + Constants.BOOK_URL + "?call_number=" + call_number + "&book_name=" + book_name + "&LAB_JSON=1";
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
                        Toast.makeText(ShowMapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
            mData.putString("appkey", room.getString("appkey"));
            mData.putString("mapid", room.getString("mapid"));
            mData.putString("port", room.getString("port"));
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
                    initMapReady();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ShowMapActivity.this, "书籍信息JSON处理失败", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void initMapReady(){
        //地图SDK初始化
        MapEngine.start(getApplicationContext(), mData.getString("appkey"));
        MapEngine.setBitmapLoaderProvider(new IBitmapLoaderProvider() {
            @Override
            public IBitmapLoader createBitmapLoader(Context context) {
                return new PicassoImageLoader(context);
            }
        });

        View palmapInnerContainer = LayoutInflater.from(this).inflate(R.layout.ipalmap_map_base,null);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        palmapInnerContainer.setLayoutParams(lp);
        palmapContainer.addView(palmapInnerContainer);

        palmap = palmapInnerContainer.findViewById(R.id.ipalmap_map);
        //设置地图引擎启动监听(MapEngine.start(Context var0, String var1)的监听)
        palmap.setOnMapReadyListener(new OnMapReadyListener() {
            @Override
            public void onMapReady(final MapView mapView) {
                palmap.loadMap(mData.getString("mapid"));
                palmap.postOnAnimationDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tv_address.setText(mData.getString("address"));
                        tv_book_title.setText(mData.getString("bookTitle"));
                        tv_book_number.setText(mData.getString("bookNumber"));
                        tv_bookshelf_guide.setText(mData.getString("bookshelfGuide"));

//                        ll_bubbleError.postOnAnimationDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                ll_bubbleError.setVisibility(View.GONE);
//                            }
//                        }, 3000);
                        ll_bubbleError.setVisibility(View.GONE);
                        final List<Feature> features = mapView.searchFeaturesByWorldCoordinate(
                                new Coordinate(mData.getDouble("map_x_value"), mData.getDouble("map_y_value")));
                        if (features == null || features.isEmpty()) {
                            Toast.makeText(ShowMapActivity.this, "当前层没有找到符合条件的POI", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mapView.resetAllRendererColor();
                        //修改目标Poi背景颜色
                        mapView.updateRendererColor(features.get(0).getId(), Color.BLUE);
                    }
                },2500);
            }
        });
        //设置地图加载错误回调
        palmap.setOnPalmapErrorListener(new OnPalmapErrorListener() {
            @Override
            public void onRequestMapDataFailure(Exception e) {
                Toast.makeText(ShowMapActivity.this, "Load map failure : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        mapView = palmap.getMapView();

    }


    private void dismissPopup() {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
            backgroundAlpha(1f);
            View rootView = findViewById(R.id.ipalmap_fl_content);
            rootView.setBackgroundColor(getResources().getColor(R.color.transparent));
        }
    }

    private void report() {
        String url = Constants.SERVER_BACK_URL + Constants.BOOK_REPORT_MISS_URL + "?id=" + mData.getString("bookId") + "&book_name=" + mData.getString("bookTitle") + "&LAB_JSON=1";

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
                        Toast.makeText(ShowMapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {//回调的方法执行在子线程
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ShowMapActivity.this, "感谢您提供的宝贵信息", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void showReportDialog() {

        if (!bookLoaded) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("报告")
                .setMessage(R.string.iplmap_book_shelf_tip)
                .setNegativeButton("取消", null)
                .setPositiveButton("报告", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        report();
                    }
                }).show();
    }

    protected void backgroundAlpha(float alpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = alpha; //0.0-1.0
        getWindow().setAttributes(lp);
    }
}
