package com.example.gameca;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;


public class GameImageAdapter extends BaseAdapter {
    private Context context;

    public GameImageAdapter(Context context) {
        this.context = context;
        //ADDED
        for (int i=0; i<12; i++) {
            this.itemClickable.add(true);
        } //ADDED
    }

    @Override
    public int getCount() {
        return 12;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }
    @Override
    public long getItemId(int position) {
        return 0;
    }
    //ADDED
    public static ArrayList<Boolean> itemClickable=new ArrayList<Boolean>(); //ADDED


    @Override
    public boolean isEnabled(int position) {
        if (GameActivity.matchedStatusPositions.get(position)==1) return false;
        else {return true;}
    }


    public boolean setItemClickable(int position, Boolean typeValue) {
        itemClickable.set(position, typeValue);
        Log.v("position", String.valueOf(position));
        Log.v("isEnabled", String.valueOf(itemClickable.get(position)));
        return typeValue;

    } //ADDED

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if(convertView == null) {
            imageView= new ImageView(this.context);
            imageView.setLayoutParams((new ViewGroup.LayoutParams(350,350)));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }
        else {
            imageView=(ImageView) convertView;
        }
        imageView.setImageResource(R.drawable.hidden);
        return imageView;
    }

}
