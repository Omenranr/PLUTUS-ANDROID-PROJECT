package com.rdmn.plutus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.rdmn.plutus.R;

public  class SliderAdapter extends PagerAdapter {

    private Context context;

    private int[] imageArray = {R.drawable.slider_pic_1,R.drawable.slider_pic_2};
    private String[] titleArray = {"Spread help culture", "Get a proof of your good\n" +
            "sense"};
    private String[] descArray = {"Request help and assistance from volunteers near your location", "Users can appreciate your help in the form of a score, as a proof of your sense of altruism"};



    public SliderAdapter(Context context){
        this.context = context;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == object);
    }

    @Override
    public int getCount() {
        return titleArray.length;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout)object);
    }



    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.slide, container, false);

        TextView slideTitle = (TextView) view.findViewById(R.id.slide_title);
        TextView slideDesc = (TextView) view.findViewById(R.id.slide_desc);
        ImageView slideImage = (ImageView) view.findViewById(R.id.slide_image);
        slideImage.setImageResource(imageArray[position]);
        slideTitle.setText(titleArray[position]);
        slideDesc.setText(descArray[position]);
        container.addView(view);
        return view;
    }
}
