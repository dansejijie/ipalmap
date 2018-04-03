package com.backustech.ipalmap.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backustech.ipalmap.R;
import com.backustech.ipalmap.utils.Constant;
import com.backustech.ipalmap.view.BookShelf;
import com.backustech.ipalmap.view.IpalmapHelpIndicatorView;
import com.palmaplus.nagrand.core.Types;
import com.palmaplus.nagrand.data.Feature;
import com.palmaplus.nagrand.data.LocationModel;
import com.palmaplus.nagrand.data.PlanarGraph;
import com.palmaplus.nagrand.geos.Coordinate;
import com.palmaplus.nagrand.tools.ViewUtils;
import com.palmaplus.nagrand.view.MapView;
import com.palmaplus.nagrand.view.widgets.Switch;

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

public class ShowMapFragemnt extends BaseMapFragment implements View.OnClickListener, Handler.Callback {


    private static final int MESSAGE_DISMSS_TIP = 0x100;
    private static final int MESSAGE_REPORT = 0x101;

    //图书加载状态
    private boolean bookLoaded = false;


    PopupWindow pw_help;

    TextView tv_address;
    TextView tv_bookshelf_guide;
    TextView tv_book_number;
    TextView tv_book_title;

    String address = "";
    String bookshelfGuide = "";
    String bookNumber = "";
    String bookTitle = "";
    String bookId = "";


    PopupWindow mPopupWindow;
    ImageButton ib_help;

    LinearLayout ll_bookShelfWatch;

    //左下角橘黄色长条形的
    LinearLayout ll_bubbleError;

    //书架共几行几列
    int bookShelfRows = 6;
    int bookShelfColumns = 6;
    //书籍放在书架的几行几列
    int bookRow = 1;
    int bookColumn = 1;

    double map_x_value = 0;
    double map_y_value = 0;

    Handler mHandler;



    @Override
    protected View getView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.activity_ipalmap_show, container, false);
    }

    @Override
    public void onInitFragment(Bundle savedInstanceState) {
        super.onInitFragment(savedInstanceState);

        RelativeLayout rl_control_container= (RelativeLayout) view.findViewById(R.id.ipalmap_map_show_control_container);
        //添加指南针
        map.setDefaultWidgetContrainer(rl_control_container);
        // 隐藏比例尺
        map.getScale().setVisibility(View.GONE);
        // 隐藏2D/3D
        map.getCompass().setVisibility(View.GONE);
        // 隐藏楼层切换控件
        map.getFloorLayout().setVisibility(View.GONE);

        Switch swicth=map.getSwitch();
        swicth.setTextSize(12);
        swicth.setTextColor(Color.BLACK);
        swicth.setBackgroundColor(Color.WHITE);
        swicth.setBackground(getResources().getDrawable(R.drawable.ipalmap_border));
        rl_control_container.removeView(swicth);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewUtils.dip2px(getContext(), 30.0F), ViewUtils.dip2px(getContext(), 30.0F));

        layoutParams.setMargins(ViewUtils.dip2px(getContext(), 12.0F), ViewUtils.dip2px(getContext(), 12.0F),0 , 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        rl_control_container.addView(swicth, layoutParams);

        tv_address = (TextView) view.findViewById(R.id.ipalmap_tv_address);
        tv_bookshelf_guide = (TextView) view.findViewById(R.id.ipalmap_tv_bookshelf_guide);
        tv_book_number = (TextView) view.findViewById(R.id.ipalmap_tv_book_number);
        tv_book_title = (TextView) view.findViewById(R.id.ipalmap_tv_book_title);

        ib_help = (ImageButton) view.findViewById(R.id.ipalmap_ib_help);
        ib_help.setOnClickListener(this);

        ll_bookShelfWatch = (LinearLayout) view.findViewById(R.id.ipalmap_ll_bookshelf_guide);
        ll_bookShelfWatch.setOnClickListener(this);

        ll_bubbleError = (LinearLayout) view.findViewById(R.id.ipalmap_ll_error_info);
        ll_bubbleError.setOnClickListener(this);

        View btnZoomOut = view.findViewById(R.id.ipalmap_show_zoom_out);
        btnZoomOut.setOnClickListener(this);
        View btnZoomIn = view.findViewById(R.id.ipalmap_show_zoom_in);
        btnZoomIn.setOnClickListener(this);

        ImageButton ib_ReportDialog = (ImageButton) view.findViewById(R.id.ipalmap_ib_error);
        ib_ReportDialog.setOnClickListener(this);



    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandler = new Handler(this);

        map.addOnChangePlanarGraph(new MapView.OnChangePlanarGraph() {
            @Override
            public void onChangePlanarGraph(PlanarGraph planarGraph, PlanarGraph planarGraph1, long l, long l1) {
                if(bookLoaded){
                    //选中书架标亮
                    Types.Point point=mapView.converToScreenCoordinate(map_x_value,map_y_value);
                    Feature feature= mapView.selectFeature((float) point.x,(float) point.y);
                    if(feature!=null){
                        long featureId = LocationModel.id.get(feature);
                        mapView.setRenderableColor("Area",featureId, Color.BLUE);
                    }
                }
            }
        });

        Bundle data=getArguments();
        //setData会有两种情况，一种是fragment加载好了，等待网络加载data,一种是data好了，等待加载fragment
        setData(data);
    }


    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_DISMSS_TIP:
                ll_bubbleError.setVisibility(View.GONE);
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ipalmap_ib_help) {
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
        } else if (id == R.id.ipalmap_show_zoom_out) {
            //地图放大
            mapView.zoomIn();
        } else if (id == R.id.ipalmap_show_zoom_in) {
            //地图缩小
            mapView.zoomOut();
        }
    }

    public void setData(Bundle bundle) {

        try{
            address = bundle.getString("address");
            bookshelfGuide = bundle.getString("bookshelfGuide");
            bookNumber = bundle.getString("bookNumber");
            bookId = bundle.getString("bookId");
            bookTitle = bundle.getString("bookTitle");
            bookShelfRows = bundle.getInt("bookShelfRows");
            bookShelfColumns = bundle.getInt("bookShelfColumns");
            bookRow = bundle.getInt("bookRow");
            bookColumn = bundle.getInt("bookColumn");
            map_x_value = bundle.getDouble("map_x_value");
            map_y_value = bundle.getDouble("map_y_value");
            initTextView();
            if(bundle.getBoolean("showBookShelf",false)){
                popupBookShelf();
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void popupHelpView() {

        backgroundAlpha(0.4f);
        final View rootView = view.findViewById(R.id.ipalmap_fl_content);
        rootView.setBackgroundColor(getResources().getColor(R.color.half_transparent));
        IpalmapHelpIndicatorView view = new IpalmapHelpIndicatorView(getContext());
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
        final View rootView = view.findViewById(R.id.ipalmap_fl_content);
        rootView.setBackgroundColor(getResources().getColor(R.color.half_transparent));
        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.ipalmap_show_book_shelf, null, false);
        BookShelf bookShelf = (BookShelf) contentView.findViewById(R.id.ipalmap_show_bookshelf);
        bookShelf.setData(bookShelfRows, bookShelfColumns, bookRow, bookColumn);
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

    private void initTextView() {
        bookLoaded=true;
        tv_address.setText(address);
        tv_book_title.setText(bookTitle);
        tv_book_number.setText(bookNumber);
        tv_bookshelf_guide.setText(bookshelfGuide);

        mHandler.sendEmptyMessageDelayed(MESSAGE_DISMSS_TIP, 3000);

        if(map.checkMapLoaded()){
            Types.Point point=mapView.converToScreenCoordinate(map_x_value,map_y_value);
            Feature feature= mapView.selectFeature((float) point.x,(float) point.y);
            if(feature!=null){
                long featureId = LocationModel.id.get(feature);
                mapView.setRenderableColor("Area",featureId, Color.BLUE);
            }
        }


    }

    private void dismissPopup() {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
            backgroundAlpha(1f);
            View rootView = view.findViewById(R.id.ipalmap_fl_content);
            rootView.setBackgroundColor(getResources().getColor(R.color.transparent));
        }
    }

    private void report() {
        String url = Constant.SERVER_BACK_URL + Constant.BOOK_REPORT_MISS_URL + "?id=" + bookId + "&book_name=" + bookTitle + "&LAB_JSON=1";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {//回调的方法执行在子线程
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "感谢您提供的宝贵信息", Toast.LENGTH_SHORT).show();
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
        Dialog dialog = new AlertDialog.Builder(getActivity())
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
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = alpha; //0.0-1.0
        getActivity().getWindow().setAttributes(lp);
    }
}
