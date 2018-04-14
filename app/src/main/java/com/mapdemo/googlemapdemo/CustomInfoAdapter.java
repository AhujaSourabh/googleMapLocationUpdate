package com.mapdemo.googlemapdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Sourabh on 14/04/18.
 */

public class CustomInfoAdapter implements GoogleMap.InfoWindowAdapter {

    private final View window;
    private Context mContext;

    public CustomInfoAdapter(Context context) {
        mContext = context;
        window = LayoutInflater.from(mContext).inflate(R.layout.snippet_layout,null);
    }

    private void renderWindowText(Marker marker, View view) {
        String title = marker.getTitle();
        TextView titleText = view.findViewById(R.id.title_window);
        if(!title.equals("")){
            titleText.setText(title);
        }

        String snippet = marker.getSnippet();
        TextView snippet_text = view.findViewById(R.id.snippet);
        if(!snippet.equals("")){
            snippet_text.setText(snippet);
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        renderWindowText(marker,window);
        return window;
    }

    @Override
    public View getInfoContents(Marker marker) {
        renderWindowText(marker,window);
        return window;
    }
}
