package com.bachelors.grzeprza.warranties.data;

/**
 * Created by grzeprza on 03.11.2016.
 */

import android.provider.BaseColumns;

/**
 * API Contract for the Warranties app.
 **/
public final class ItemContract {

    private ItemContract(){}

    /**Represents ITEMS table*/
    static final class ItemEntry implements BaseColumns
    {
        public final static String TABLE_NAME = "items";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_ITEM_NAME = "item_name";
        public final static String COLUMN_ITEM_PRICE = "item_price";
        public final static String COLUMN_ITEM_PHOTO = "item_photo";
        public final static String COLUMN_ITEM_RECEIPT_PHOTO = "item_receipt_photo";
        public final static String COLUMN_BOUGHT_DATE = "bought_date";
        public final static String COLUMN_ITEM_TYPE = "item_type";
        public final static String COLUMN_WARRANTY_DURATION = "warranty_duration"; //in weeks
        public final static String COLUMN_SHOP_ID = "shop_id";
    }

    static final class ItemTypes
    {
        public final static int OTHER = 0;
        public final static int ELECTRONIC = 1;
        public final static int FASHION = 2;
        public final static int HOUSE = 3;
        public final static int SPORT = 4;
        public final static int MOTORIZATION = 5;
        public final static int GARDEN = 6;
    }

    /**Represents SHOP table*/
    static final class ShopEntry implements BaseColumns
    {
        public final static String TABLE_NAME = "shop";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_SHOP_NAME = "shop_name";
        public final static String COLUMN_ITEM_COUNT = "item_count";
    }
}
