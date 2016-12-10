package com.bachelors.grzeprza.warranties.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.bachelors.grzeprza.warranties.data.ItemContract.ItemEntry.CONTENT_URI;
import static com.bachelors.grzeprza.warranties.data.ItemContract.ItemEntry.TABLE_NAME;

/**
 * Created by grzeprza on 10.12.2016.
 * {@link ContentProvider} for Warranties app.
 */
public class ItemProvider extends ContentProvider {

    /**{@link ItemDbHelper} instance. Direct access to database.*/
    private ItemDbHelper itemDbHelper;

    /**Variable handles dealing with different query type - to whole table and specific item*/
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**Help code to gain access to whole table*/
    private static final int ITEMS = 300;

    /**Help code to gain access to specific item*/
    private static final int ITEM_ID = 303;

    /**Added static dependencies between UriMatcher and ItemProvider. Later on it can differ query with only switch statement*/
    static
    {
        uriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEM, ITEMS);
        uriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEM + "/#", ITEM_ID);
    }

    /**Tag for logging messages*/
    public static final String LOG_TAG = ItemProvider.class.getSimpleName();

    /**Initializes the provider and the database helper .*/
    @Override
    public boolean onCreate() {

        this.itemDbHelper = new ItemDbHelper(getContext());
        return true;
    }

    /**Enables quering the Item database*/
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //get readable access to database
        SQLiteDatabase db = itemDbHelper.getReadableDatabase();
        //set holder for the result
        Cursor cursor;

        int match = uriMatcher.match(uri);
        switch (match)
        {
            case ITEMS:
                cursor = db.query(ItemContract.ItemEntry.TABLE_NAME, projection,selection,selectionArgs,null,null,sortOrder);
                break;

            case ITEM_ID:
                selection = ItemContract.ItemEntry._ID + "=?";
                selectionArgs = new String []{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(ItemContract.ItemEntry.TABLE_NAME, projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                cursor = null;
                Log.e(LOG_TAG, "Wrong centent URI");
        }
        return cursor;
    }

    /**Inserts new row into database.*/
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = uriMatcher.match(uri);

        switch (match)
        {
            //Item can be added only when asking for CONTENT_URI
            case ITEMS:
                return insertItem(uri,values);
            //Otherwise throw exception
            default:
                throw new IllegalArgumentException("Wrong URI : " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {
        SQLiteDatabase db = itemDbHelper.getWritableDatabase();

        long index = db.insert(TABLE_NAME,null,values);

        if(index == -1) {
            Log.i(LOG_TAG, "Failed to insert " + uri);
            return null;
        }

        return ContentUris.withAppendedId(CONTENT_URI,index);
    }

    /**Deletes given rows matching selection and selectionArgs*/
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    /**Updates the database selection fields with new values from {@link ContentValues}*/
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    /**Get the type from the URI (table name)*/
    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

}
