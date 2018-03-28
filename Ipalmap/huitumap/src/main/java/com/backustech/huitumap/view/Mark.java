package com.backustech.huitumap.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.backustech.huitumap.R;
import com.palmaplus.nagrand.view.overlay.OverlayCell;

/**
 * Created by lchad on 2016/11/1.
 * Github: https://github.com/lchad
 */

public class Mark extends LinearLayout implements OverlayCell {

    private ImageView mIconView;
    private double[] mGeoCoordinate;
    private int mId;

    /**
     * 此Mark所属的楼层id.
     */
    public long mFloorId;


    public Mark(Context context, int id,int resId) {
        super(context);
        this.mId = id;
        LinearLayout contentView= (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.ipalmap_mark_layout, this);
        mIconView = (ImageView) contentView.findViewById(R.id.ipalmap_mark_image);
        mIconView.setBackgroundResource(resId);
    }


    @Override
    public void init(double[] doubles) {
        mGeoCoordinate = doubles;
    }

    @Override
    public double[] getGeoCoordinate() {
        return mGeoCoordinate;
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void position(double[] doubles) {
        setX((float) doubles[0] - getWidth() / 2);
        setY((float) doubles[1] - getHeight() / 2);
    }

    @Override
    public long getFloorId() {
        return mFloorId;
    }

    /**
     * 设置FloorId
     * @param floorId
     */
    public void setFloorId(long floorId) {
        mFloorId = floorId;
    }
}
