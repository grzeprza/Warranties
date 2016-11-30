package com.bachelors.grzeprza.warranties.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.bachelors.grzeprza.warranties.data.ItemContract.ItemEntry;

/**
 * Created by grzeprza on 28.11.2016.
 */

public class ItemDbHelper extends SQLiteOpenHelper {

    static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "warranties.db";

    public ItemDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**Creates String that contains SQL statement to createm Item table based on {@link ItemContract}*/
    @Override
    public void onCreate(SQLiteDatabase db) {
    /*
    CREATE TABLE `item` (
	`id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`item_name`	TEXT NOT NULL,
	`item_price`	REAL NOT NULL,
	`item_photo_uri`	TEXT NOT NULL,
	`item_receipt_photo_uri`	TEXT NOT NULL,
	`bought_date`	TEXT NOT NULL,
	`item_type`	INTEGER NOT NULL,
	`warranty_duration`	INTEGER NOT NULL,
	`shop_name`	TEXT NOT NULL UNIQUE);
    * */
        String SQL_CREATE_ITEM_TABLE = "CREATE TABLE "+ ItemEntry.TABLE_NAME + " ( "
                + ItemEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ItemEntry.COLUMN_ITEM_NAME +" TEXT NOT NULL, "
                + ItemEntry.COLUMN_ITEM_PRICE +" REAL NOT NULL, "
                + ItemEntry.COLUMN_ITEM_PHOTO_URI+" TEXT NOT NULL, "
                + ItemEntry.COLUMN_ITEM_RECEIPT_PHOTO_URI +" TEXT NOT NULL, "
                + ItemEntry.COLUMN_BOUGHT_DATE +" TEXT NOT NULL, "
                + ItemEntry.COLUMN_ITEM_TYPE +" INTEGER NOT NULL, "
                + ItemEntry.COLUMN_WARRANTY_DURATION +" INTEGER NOT NULL, "
                + ItemEntry.COLUMN_SHOP_NAME+" TEXT NOT NULL UNIQUE);";

        db.execSQL(SQL_CREATE_ITEM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
