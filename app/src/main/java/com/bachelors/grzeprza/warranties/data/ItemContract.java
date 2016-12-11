package com.bachelors.grzeprza.warranties.data;

/**
 * Created by grzeprza on 03.11.2016.
 */

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Warranties app.
 **/
public final class ItemContract {

    /**Unables to create instance of Item Contract*/
    private ItemContract(){}

    /**Represents authority needed for provider in Manifest file.*/
    public static final String CONTENT_AUTHORITY = "com.bachelors.grzeprza.warranties";

    /**Represents base URI for Warranties app.*/
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**Represents table name. If more tables needed then access would be easier.*/
    public static final String PATH_ITEM = "items";

    /**Represents ITEMS table*/
    public static final class ItemEntry implements BaseColumns
    {
        /**Content URI for Item table*/
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEM);

        /**The MIME type of the {@link #CONTENT_URI} for a list of items.*/
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEM;

        /**The MIME type of the {@link #CONTENT_URI} for a single item.*/
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEM;

        /**Name of the database*/
        public final static String TABLE_NAME = "items";

        //Below you can see columns in table items
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_ITEM_NAME = "item_name";
        public final static String COLUMN_ITEM_PRICE = "item_price";
        public final static String COLUMN_ITEM_PHOTO_URI = "item_photo_uri";
        public final static String COLUMN_ITEM_RECEIPT_PHOTO_URI = "item_receipt_photo_uri";
        public final static String COLUMN_BOUGHT_DATE = "bought_date";
        public final static String COLUMN_ITEM_TYPE = "item_type";
        public final static String COLUMN_WARRANTY_DURATION = "warranty_duration"; //in weeks
        public final static String COLUMN_SHOP_NAME = "shop_name";
    }

    /**Represents ITEM TYPES*/
    public static final class ItemTypes
    {
        public final static int OTHER = 0;
        public final static int ELECTRONIC = 1;
        public final static int FASHION = 2;
        public final static int HOUSE = 3;
        public final static int SPORT = 4;
        public final static int MOTORIZATION = 5;
        public final static int GARDEN = 6;
    }
}
