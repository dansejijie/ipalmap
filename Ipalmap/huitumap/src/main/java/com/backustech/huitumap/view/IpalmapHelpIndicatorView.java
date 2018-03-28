package com.backustech.huitumap.view;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.backustech.huitumap.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tygzx on 2018/3/13.
 */

public class IpalmapHelpIndicatorView extends FrameLayout implements ViewPager.OnPageChangeListener ,View.OnClickListener{

    ViewPager mViewPager;
    ImageButton mImageButton;
    List<ImageView>mImageViews;
    List<ImageView>mDots;
    OnCloseClickListener mOnCloseClickListener;

    public IpalmapHelpIndicatorView(@NonNull Context context) {
        super(context);
        init();
    }

    public IpalmapHelpIndicatorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IpalmapHelpIndicatorView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init(){
        LinearLayout container= (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.ipalmap_show_help_indicator,null);
        addView(container);

        //图片
        mImageViews=new ArrayList<>();
        ImageView iv1=new ImageView(getContext());
        iv1.setImageResource(R.drawable.ipalmap_show_help_guide01);
        mImageViews.add(iv1);
        ImageView iv2=new ImageView(getContext());
        iv2.setImageResource(R.drawable.ipalmap_show_help_guide02);
        mImageViews.add(iv2);

        //圆点
        mDots=new ArrayList<>();
        ImageView dot1= (ImageView) container.findViewById(R.id.ipalmap_iv_show_help_dot1);
        dot1.setActivated(true);
        ImageView dot2= (ImageView) container.findViewById(R.id.ipalmap_iv_show_help_dot2);
        dot2.setActivated(false);
        mDots.add(dot1);
        mDots.add(dot2);

        mImageButton= (ImageButton) container.findViewById(R.id.ipalmap_ib_show_help_close);
        mImageButton.setOnClickListener(this);

        mViewPager= (ViewPager) container.findViewById(R.id.ipalmap_vp_show_help);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return mImageViews.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mImageViews.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(mImageViews.get(position));
                return mImageViews.get(position);
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view==object;
            }
        });



    }



    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for(int i=0;i<mDots.size();i++){
            if(i==position){
                mDots.get(i).setActivated(true);
            }else {
                mDots.get(i).setActivated(false);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        if(id==R.id.ipalmap_ib_show_help_close){
            if(mOnCloseClickListener!=null){
                mOnCloseClickListener.onClick();
            }
        }
    }

    public void setOnCloseClickListener(OnCloseClickListener listener){
        mOnCloseClickListener=listener;
    }


    public interface OnCloseClickListener{
        void onClick();
    }
}
