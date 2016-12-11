package com.bachelors.grzeprza.warranties.data;

import android.content.Context;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bachelors.grzeprza.warranties.R;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by grzeprza on 11.12.2016.
 */
public class ItemCursorAdapter extends CursorAdapter {

    /**Needed to count time left till the end of warranty, appropriate format is */
    private java.text.SimpleDateFormat simpleDateFormatter;

    /**Initilizes an instance of th {@link ItemCursorAdapter}*/
    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    /**Gets access to ListView and enables creation of new row*/
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_row_layout, parent, false);
    }

    /**Binds item_row_layout fields to adequate database columns*/
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView itemIcon = (ImageView) view.findViewById(R.id.ic_item);
        TextView itemDesciption = (TextView) view.findViewById(R.id.item_description);
        TextView shopName = (TextView) view.findViewById(R.id.shop_name);
        TextView numberTimeLeft = (TextView) view.findViewById(R.id.number_timeLeft);
        TextView measureTimeLeft = (TextView) view.findViewById(R.id.measure_timeLeft);

        //Get URI value for Image and scale to icon;
        Uri imageURI = Uri.parse(cursor.getString(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PHOTO_URI)));
        //TODO:SCALE DOWN IMAGE WITH GIVES URI to 96x96 pixels and set as image[round] or background [square]
        //Get value for ITEM NAME
        String itemDescriptionString = cursor.getString(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME));
        //GET value for SHOP NAME
        String shopNameString = cursor.getString(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_SHOP_NAME));
        //GET value for BOUGHT_DATE, WARRANTY_DURATION and count numberTimeLeftInteger - time left in weeks
        String boughtDate = cursor.getString(cursor.getColumnIndex((ItemContract.ItemEntry.COLUMN_BOUGHT_DATE)));
        int duration = cursor.getInt(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_WARRANTY_DURATION));
        String numberTimeLeftInteger = countTimeLeft(boughtDate,duration);

        //Assign value to layout fields
        //itemIcon.setImageURI(imageURI);
        itemIcon.setBackgroundResource(android.R.mipmap.sym_def_app_icon);
        itemDesciption.setText(itemDescriptionString);
        shopName.setText(shopNameString);
        numberTimeLeft.setText(numberTimeLeftInteger);
        measureTimeLeft.setText("weeks");
    }

    private String countTimeLeft(String boughtDate, int duration)
    {
        //TODO: Implement this method -> timeLeft = (bought_date + duration) - currentTime
        return "10";
    }
}
