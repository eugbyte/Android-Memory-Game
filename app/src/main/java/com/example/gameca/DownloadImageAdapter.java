package com.example.gameca;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DownloadImageAdapter extends BaseAdapter {

    private final Context providedContext;
    private List<Bitmap> bitmaps;

    //Constructor to instantiate a BooksAdapter.
    public DownloadImageAdapter(Context context, List<Bitmap> bitmaps){
        this.providedContext = context;
        this.bitmaps = bitmaps;
    }

    //return the number of cells to render here.
    @Override
    public int getCount() {
        return bitmaps.size();
    }

    //return the cell id
    @Override
    public long getItemId(int position) {
        return position;
    }

    //return the Object bound to the cell
    public Object getItem(int position) {
        return bitmaps.get(position);
    }

    //Get a View that displays the data at the specified position in the data set
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView textView;

        ImageView imageView;

        //convert view is the old view. Adapter uses old view to "recycle" instead of instantiating new objects
        if (convertView == null) {
            imageView = new ImageView(providedContext);

        } else {
            //textView = (TextView)convertView;
            imageView = (ImageView)convertView;
        }
        imageView.setImageBitmap(bitmaps.get(position));
        return imageView;
    }


}
