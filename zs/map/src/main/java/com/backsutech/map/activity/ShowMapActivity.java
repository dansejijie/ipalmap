package com.backsutech.map.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import com.backsutech.map.R;
import com.backsutech.map.utils.Constants;
import com.backsutech.map.view.BookShelf;
import com.backsutech.map.view.IpalmapHelpIndicatorView;
import com.brtbeacon.map.map3d.BRTMapEnvironment;
import com.brtbeacon.map.map3d.BRTMapView;
import com.brtbeacon.map.map3d.entity.BRTFloorInfo;
import com.brtbeacon.map.map3d.entity.BRTPoi;
import com.brtbeacon.map.map3d.entity.BRTPoiEntity;
import com.brtbeacon.map.map3d.entity.BRTPoint;
import com.brtbeacon.map.map3d.utils.BRTConvert;
import com.brtbeacon.map.map3d.utils.BRTSearchAdapter;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONObject;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ShowMapActivity extends Activity implements View.OnClickListener , BRTMapView.BRTMapViewListener{

    private static final int BRTMAP_PERMISSION_CODE = 9999;
    //标题栏
    LinearLayout fl_header_back;
    LinearLayout ll_header_nav;

    TextView tv_address;
    TextView tv_bookshelf_guide;
    TextView tv_book_number;
    TextView tv_book_title;

    PopupWindow mPopupWindow;
    ImageButton ib_help;

    LinearLayout ll_bookShelfWatch;
    LinearLayout ll_bubbleError;        //左下角橘黄色长条形的

    boolean bookLoaded = false;

    Bundle mData;
    BRTMapView mapView;
    BRTSearchAdapter searchAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BRTMapEnvironment.initMapEnvironment(this);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_map_show);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.ipalmap_header_bar);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.addMapListener(this);

        initView();
        getDataAsync();
    }

    private void initView() {
        initHeaderView();
        initControlView();
    }

    private void initControlView() {

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
        String org_id="";
        String location="";
        String group="";

        Intent intent = getIntent();
        try {
            call_number = intent.getStringExtra("call_number");
            book_name = intent.getStringExtra("book_name");
            org_id = intent.getStringExtra("org_id");
            location = intent.getStringExtra("location");
            group = intent.getStringExtra("group");
        } catch (Exception e) {
            Toast.makeText(this, "请确认是否传入正确参数call_number和book_name", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Constants.SERVER_BACK_URL + Constants.BOOK_URL + "?call_number=" + call_number + "&book_name=" + book_name + "&org_id=" + org_id +"&location=" + location + "&group=" + group + "&LAB_JSON=1";
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
                    //Log.d("ipalmap", "获取数据成功了");
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
//            mData.putString("appkey", "d4a6d090d3b04542bcf91e80f933c8e1");
//            mData.putString("mapid", "00230029");

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

//            mData.putDouble("map_x_value", 29.401475984752352);
//            mData.putDouble("map_y_value", 106.56109829922548);
            bookLoaded = true;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!checkNeedPermission()){
                        initMapReady();
                    }
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
        mapView.init(mData.getString("mapid"), mData.getString("appkey"), BRTMapView.MAP_LOAD_MODE_OFFLINE);
        searchAdapter = new BRTSearchAdapter(mData.getString("mapid"));
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

    @Override
    public void mapViewDidLoad(BRTMapView brtMapView, Error error) {
        if(error!=null){
            Toast.makeText(ShowMapActivity.this, "Load map failure : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        mapView.setFloor(mapView.getFloorList().get(0));
    }

    @Override
    public void onFinishLoadingFloor(BRTMapView brtMapView, final BRTFloorInfo brtFloorInfo) {
        brtMapView.postOnAnimationDelayed(new Runnable() {
            @Override
            public void run() {
                tv_address.setText(mData.getString("address"));
                tv_book_title.setText(mData.getString("bookTitle"));
                tv_book_number.setText(mData.getString("bookNumber"));
                tv_bookshelf_guide.setText(mData.getString("bookshelfGuide"));
                ll_bubbleError.setVisibility(View.GONE);

                List<BRTPoiEntity> lists = searchAdapter.queryPoiByRadius(new LatLng(mData.getDouble("map_x_value"),mData.getDouble("map_y_value")),4,brtFloorInfo.getFloorNumber());
                if(lists!=null&&lists.size()>0) {
                    BRTPoiEntity entity = lists.get(0);
                    BRTPoi brtPoi = new BRTPoi();
                    brtPoi.setFloorNumber(entity.getFloorNumber());
                    brtPoi.setPoint(new BRTPoint(entity.getFloorNumber(),entity.getLatLng().getLatitude(),entity.getLatLng().getLongitude()));
                    brtPoi.setBuildingID(mapView.getBuilding().getBuildingID());
                    brtPoi.setLayer(entity.getLayer());
                    brtPoi.setPoiID(entity.getPoiId());
                    mapView.highlightPoi(brtPoi);
                }
            }
        },2500);
    }

    @Override
    public void onClickAtPoint(BRTMapView brtMapView, BRTPoint brtPoint) {

    }

    @Override
    public void onPoiSelected(BRTMapView brtMapView, List<BRTPoi> list) {
        Log.d("TAG","xx");
    }



    private boolean checkNeedPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//判断当前系统的SDK版本是否大于23
            List<String> permissionNeedRequest = new LinkedList<>();
            for (String permssion: permissionsNeedCheck) {
                if(ActivityCompat.checkSelfPermission(this, permssion) != PackageManager.PERMISSION_GRANTED) {
                    permissionNeedRequest.add(permssion);
                }
            }
            if (!permissionNeedRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionNeedRequest.toArray(new String[0]), BRTMAP_PERMISSION_CODE);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // requestCode即所声明的权限获取码，在requestPermissions时传入
            case BRTMAP_PERMISSION_CODE:
                boolean isAllGrant = true;
                for (int grantResult: grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        isAllGrant = false;
                        break;
                    }
                }
                if (!isAllGrant) {
                    Toast.makeText(getApplicationContext(), "获取位置权限失败，请手动前往设置开启", Toast.LENGTH_SHORT).show();
                    return;
                }

                initMapReady();

                break;
            default:
                break;
        }
    }

    private static final List<String> permissionsNeedCheck;
    static {
        permissionsNeedCheck = new LinkedList<>();
        permissionsNeedCheck.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        permissionsNeedCheck.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsNeedCheck.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    static {
        System.loadLibrary("BRTMapSDK");
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
