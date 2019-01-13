package com.rescue.blood.instablood.common;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.rescue.blood.instablood.R;

/**
 * Created by sayan07 on 24/2/18.
 */

public class MyCustomInfoWindow implements GoogleMap.InfoWindowAdapter {

    Context mContext;

    public MyCustomInfoWindow(Context context){
        mContext=context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.custom_info_window,null);
        TextView title=view.findViewById(R.id.uname);
        title.setText(marker.getTitle());

        TextView bg=view.findViewById(R.id.bloodgrp);
        bg.setText(marker.getSnippet());

        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
