package com.backustech.ipalmap.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.backustech.ipalmap.R;
import com.backustech.ipalmap.utils.Constant;
import com.backustech.ipalmap.view.BookShelf;
import com.backustech.ipalmap.view.IpalmapHelpIndicatorView;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by tygzx on 2018/3/28.
 */

public class IpalmapShowMapActivity extends IpalmapActivity implements View.OnClickListener,Handler.Callback{

    private static final int MESSAGE_DISMSS_TIP=0x100;
    private static final int MESSAGE_REPORT=0x101;

    //图书加载状态
    private boolean bookLoaded=false;


    PopupWindow pw_help;

    TextView tv_address;
    TextView tv_bookshelf_guide;
    TextView tv_book_number;
    TextView tv_book_title;

    String address="";
    String bookshelfGuide="";
    String bookNumber="";
    String bookTitle="";
    String bookId="";


    PopupWindow mPopupWindow;
    ImageButton ib_help;

    LinearLayout ll_bookShelfWatch;

    //左下角橘黄色长条形的
    LinearLayout ll_bubbleError;

    //书架共几行几列
    int bookShelfRows=6;
    int bookShelfColumns=6;
    //书籍放在书架的几行几列
    int bookRow=1;
    int bookColumn=1;


    ImageButton ib_back;
    LinearLayout ll_nav;

    Handler mHandler;

    @Override
    protected void initLayout() {
        setContentView(R.layout.activity_ipalmap_show);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.ipalmap_header_bar_show);
    }

    @Override
    protected void initView() {
        ib_back= (ImageButton) findViewById(R.id.ipalmap_header_bar_back);
        ib_back.setOnClickListener(this);
        ll_nav= (LinearLayout) findViewById(R.id.ipalmap_header_bar_nav);
        ll_nav.setOnClickListener(this);

        tv_address= (TextView) findViewById(R.id.ipalmap_tv_address);
        tv_bookshelf_guide= (TextView) findViewById(R.id.ipalmap_tv_bookshelf_guide);
        tv_book_number= (TextView) findViewById(R.id.ipalmap_tv_book_number);
        tv_book_title= (TextView) findViewById(R.id.ipalmap_tv_book_title);

        ib_help= (ImageButton) findViewById(R.id.ipalmap_ib_help);
        ib_help.setOnClickListener(this);

        ll_bookShelfWatch= (LinearLayout) findViewById(R.id.ipalmap_ll_bookshelf_guide);
        ll_bookShelfWatch.setOnClickListener(this);

        ll_bubbleError= (LinearLayout) findViewById(R.id.ipalmap_ll_error_info);
        ll_bubbleError.setOnClickListener(this);

        View btnZoomOut=findViewById(R.id.ipalmap_show_zoom_out);
        btnZoomOut.setOnClickListener(this);
        View btnZoomIn=findViewById(R.id.ipalmap_show_zoom_in);
        btnZoomIn.setOnClickListener(this);

        ImageButton ib_ReportDialog= (ImageButton) findViewById(R.id.ipalmap_ib_error);
        ib_ReportDialog.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        mHandler=new Handler(this);

        getDataAsync();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case 1://后台请求到信息
                initTextView();
                //隔3s后消失左下角的提示块
                mHandler.sendEmptyMessageDelayed(MESSAGE_DISMSS_TIP,3000);
                break;
            case MESSAGE_DISMSS_TIP:
                ll_bubbleError.setVisibility(View.GONE);
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        if(id==R.id.ipalmap_ib_help){
            popupHelpView();
        }else if(id==R.id.ipalmap_ll_bookshelf_guide){
            //点击了查看书籍在书架哪个位置的按钮
            popupBookShelf();
        }else if(id==R.id.ipalmap_btn_show_bookshelf){
            //点击了报告书籍不在书架的按钮
            dismissPopup();
            report();
        }else if(id==R.id.ipalmap_ib_show_help_close){
            //点击了关闭弹出框的按钮
            dismissPopup();
        }else if(id==R.id.ipalmap_ll_error_info){
            //消失掉右下角橘黄色长条形的View
            ll_bubbleError.setVisibility(View.GONE);
        }else if(id==R.id.ipalmap_ib_error){
            showReportDialog();
        }else if(id==R.id.ipalmap_header_bar_back){
            finish();
        }else if(id==R.id.ipalmap_header_bar_nav){
            Intent intent=new Intent(this,IpalmapNavigationActivity.class);
            startActivity(intent);
        }else if(id==R.id.ipalmap_show_zoom_out){
            //地图放大
            mapView.zoomIn();
        }else if(id==R.id.ipalmap_show_zoom_in){
            //地图缩小
            mapView.zoomOut();
        }
    }

    private void getDataAsync() {

        String call_number="";
        String book_name="";
        Intent intent=getIntent();
        try{
            call_number=intent.getStringExtra("call_number");
            book_name=intent.getStringExtra("book_name");
        }catch (Exception e){
            Toast.makeText(this,"请确认是否传入正确参数call_number和book_name",Toast.LENGTH_SHORT);
            bookLoaded=false;
            return;
        }

        String url= Constant.SERVER_BACK_URL+Constant.BOOK_URL+"?call_number="+call_number+"&book_name="+book_name+"&LAB_JSON=1";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call,final IOException e) {
                IpalmapShowMapActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(IpalmapShowMapActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                    }
                });
                bookLoaded=false;
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){//回调的方法执行在子线程。
                    Log.d("ipalmap","获取数据成功了");
                    handleBookJSON(response.body().string());
                }
            }
        });
    }

    private void handleBookJSON(String json){

        try{
            JSONObject result=new  JSONObject(json);
            JSONObject data=result.getJSONObject("data");
            JSONObject bookshelf=data.getJSONObject("bookshelf");
            JSONObject room=data.getJSONObject("room");
            JSONObject index=data.getJSONObject("index");

            address=room.getString("library_title")+" "+room.getString("floor")+" "+room.getString("name");
            bookshelfGuide=bookshelf.getString("name")+index.getString("title");
            bookNumber=data.getString("callNumber");
            bookId=index.getString("id");
            bookTitle=data.getString("bookName");

            bookShelfRows=Integer.parseInt(bookshelf.getString("specifications_row_count"));
            bookShelfColumns=Integer.parseInt(bookshelf.getString("specifications_column_count"));
            bookRow=Integer.parseInt(index.getString("row"));
            bookColumn=Integer.parseInt(index.getString("column"));

            bookLoaded=true;

            mHandler.sendEmptyMessage(1);

        }catch (Exception e){
            e.printStackTrace();
            IpalmapShowMapActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(IpalmapShowMapActivity.this,"书籍信息处理失败",Toast.LENGTH_SHORT);
                }
            });
            bookLoaded=false;
        }

    }

    private void popupHelpView(){

        backgroundAlpha(0.4f);
        final View rootView=findViewById(R.id.ipalmap_fl_content);
        rootView.setBackgroundColor(getResources().getColor(R.color.half_transparent));
        IpalmapHelpIndicatorView view=new IpalmapHelpIndicatorView(this);
        view.setOnCloseClickListener(new IpalmapHelpIndicatorView.OnCloseClickListener() {
            @Override
            public void onClick() {
                dismissPopup();
            }
        });
        view.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT));

        mPopupWindow=new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);
                rootView.setBackgroundColor(getResources().getColor(R.color.transparent));
                mPopupWindow=null;
            }
        });
        mPopupWindow.showAtLocation(rootView, Gravity.BOTTOM|Gravity.LEFT,0,0);
    }

    private void popupBookShelf(){
        if(!bookLoaded){
            return;
        }
        backgroundAlpha(0.4f);
        final View rootView=findViewById(R.id.ipalmap_fl_content);
        rootView.setBackgroundColor(getResources().getColor(R.color.half_transparent));
        View contentView= LayoutInflater.from(IpalmapShowMapActivity.this).inflate(R.layout.ipalmap_show_book_shelf, null, false);
        BookShelf bookShelf= (BookShelf) contentView.findViewById(R.id.ipalmap_show_bookshelf);
        bookShelf.setData(bookShelfRows,bookShelfColumns,bookRow,bookColumn);
        Button btnReport= (Button) contentView.findViewById(R.id.ipalmap_btn_show_bookshelf);
        btnReport.setOnClickListener(this);
        ImageButton ibClose= (ImageButton) contentView.findViewById(R.id.ipalmap_ib_show_help_close);
        ibClose.setOnClickListener(this);

        mPopupWindow=new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                rootView.setBackgroundColor(getResources().getColor(R.color.transparent));
                backgroundAlpha(1f);
                mPopupWindow=null;
            }
        });
        mPopupWindow.showAtLocation(rootView, Gravity.BOTTOM|Gravity.LEFT,0,0);

    }

    private void initTextView(){
        tv_address.setText(address);
        tv_book_title.setText(bookTitle);
        tv_book_number.setText(bookNumber);
        tv_bookshelf_guide.setText(bookshelfGuide);
    }

    private void dismissPopup(){
        if(mPopupWindow!=null){
            mPopupWindow.dismiss();
            backgroundAlpha(1f);
            View rootView=findViewById(R.id.ipalmap_fl_content);
            rootView.setBackgroundColor(getResources().getColor(R.color.transparent));
        }
    }

    private void report(){
        showLoadingDialog();
        String url= Constant.SERVER_BACK_URL+Constant.BOOK_REPORT_MISS_URL+"?id="+bookId+"&book_name="+bookTitle+"&LAB_JSON=1";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call,final IOException e) {
                hideLoadingDialog();
                IpalmapShowMapActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(IpalmapShowMapActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                hideLoadingDialog();
                if(response.isSuccessful()){//回调的方法执行在子线程。
                    IpalmapShowMapActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(IpalmapShowMapActivity.this, "感谢您提供的宝贵信息",Toast.LENGTH_SHORT);
                        }
                    });
                }
            }
        });
    }

    private void showReportDialog(){

        if(!bookLoaded){
            return;
        }
        Dialog dialog=new AlertDialog.Builder(this)
                .setTitle("报告")
                .setMessage(R.string.iplmap_book_shelf_tip)
                .setNegativeButton("取消",null)
                .setPositiveButton("报告", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        report();
                    }
                }).show();

    }
}
