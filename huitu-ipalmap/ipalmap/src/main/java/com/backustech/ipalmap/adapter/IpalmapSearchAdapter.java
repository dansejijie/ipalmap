package com.backustech.ipalmap.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.backustech.ipalmap.R;
import com.palmaplus.nagrand.data.LocationModel;
import com.palmaplus.nagrand.data.LocationPagingList;


/**
 * Created by tygzx on 2018/3/23.
 */

public class IpalmapSearchAdapter extends BaseAdapter {

    private Context mContent;
    private LocationPagingList mData;

    public IpalmapSearchAdapter(Context context, LocationPagingList data){
        mContent=context;
        mData=data;
    }

    public void setData(LocationPagingList data){
        mData=data;
    }


    @Override
    public int getCount() {
        return mData.getSize();
    }

    @Override
    public Object getItem(int position) {
        return mData.getPOI(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if(convertView==null){
            viewHolder=new ViewHolder();
            convertView= LayoutInflater.from(mContent).inflate(R.layout.ipalmap_search_list_item,null);
            viewHolder.tvTitle= (TextView) convertView.findViewById(R.id.ipalmap_nav_tv_list_item_name);
            convertView.setTag(viewHolder);
        }else {
            viewHolder= (ViewHolder) convertView.getTag();
        }

        LocationModel locationModel=mData.getPOI(position);
        viewHolder.tvTitle.setText(LocationModel.display.get(locationModel));
        return convertView;
    }


    class ViewHolder {
        TextView tvTitle;
    }
}
