package com.rdmn.plutus.blindapp.Helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.rdmn.plutus.blindapp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter{
    View view0;
    public CustomInfoWindow(Context context){
        view0= LayoutInflater.from(context).inflate(R.layout.custom_blind_info_window, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        TextView tvPickupTitle=view0.findViewById(R.id.tvPickupInfo);
        tvPickupTitle.setText(marker.getTitle());

        TextView tvPicupSnippet=view0.findViewById(R.id.tvPickupSnippet);
        tvPicupSnippet.setText("0624867340");
        return view0;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
