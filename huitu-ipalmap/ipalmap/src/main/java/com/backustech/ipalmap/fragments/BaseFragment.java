package com.backustech.ipalmap.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by jian.feng on 2017/4/24.
 */

public abstract class BaseFragment extends Fragment {

    protected View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = provideView(inflater, container, savedInstanceState);
            this.onInitFragment(savedInstanceState);
        } else if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        return view;
    }

    public abstract View provideView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState);

    public void onInitFragment(Bundle savedInstanceState) {
    }
}