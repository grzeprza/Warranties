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

import static com.bachelors.grzeprza.warranties.data.ItemContract.ItemEntry.CONTENT_ITEM_TYPE;
import static com.bachelors.grzeprza.warranties.data.ItemContract.ItemEntry.CONTENT_URI;
import static com.bachelors.grzeprza.warranties.data.ItemContract.ItemEntry.TABLE_NAME;
import static com.bachelors.grzeprza.warranties.data.ItemContract.ItemEntry._ID;

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

        cursor.setNotificationUri(getContext().getContentResolver(),uri);

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
            {
                Uri resultUri = insertItem(uri,values);
                //automatically updates ListView in MainActivity
                getContext().getContentResolver().notifyChange(resultUri, null);
                return resultUri;
            }
            //Otherwise throw exception
            default:
                throw new IllegalArgumentException("INSERT - Wrong URI : " + uri);
        }
    }

    /**Gains access to database and inserts values.*/
    private Uri insertItem(Uri uri, ContentValues values) {
        SQLiteDatabase db = itemDbHelper.getWritableDatabase();

        long index = db.insert(TABLE_NAME,null,values);

        if(index == -1) {
            Log.i(LOG_TAG, "Failed to insert " + uri);
            return null;
        }
        return ContentUris.withAppendedId(CONTENT_URI,index);
    }

    /**Deletes given rows matching selection and selectionArgs
     * Deletes row with given item _ID*/
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        SQLiteDatabase db = itemDbHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);

        int rowsDeleted;
        switch (match)
        {
            case ITEMS: rowsDeleted =  db.delete(TABLE_NAME, selection, selectionArgs); break;
            case ITEM_ID:
            {
                selection = _ID +  "=?";
                selectionArgs = new String []{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(TABLE_NAME, selection,selectionArgs);
            }break;
            default:
                throw new IllegalArgumentException("DELETE - Wrong uri: "+uri);
        }

        if(rowsDeleted != 0) getContext().getContentResolver().notifyChange(uri,null);

        return rowsDeleted;
    }

    /**Updates the database selection fields with new values from {@link ContentValues}*/
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int match = uriMatcher.match(uri);

        int rowsUpdated;
        switch (match)
        {
            case ITEM_ID:{
                selection = _ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsUpdated= updateItem(uri, values, selection,selectionArgs);}break;
            //updating based on whole table is unavailable
            default:
                throw new IllegalArgumentException("UPDATE - Wrong Uri: "+uri);
        }
        if(rowsUpdated !=0) getContext().getContentResolver().notifyChange(uri,null);
        return rowsUpdated;
    }

    /**Method used in update method. Makes code easier to read.*/
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = itemDbHelper.getWritableDatabase();

        int numberOfRowsAffected = db.update(TABLE_NAME, values,selection,selectionArgs);

        if(numberOfRowsAffected == 0) Log.i(LOG_TAG, "NO ROW AFFECTED");
        else Log.i(LOG_TAG, "NUMBER OF ROWS AFFECTED: " + numberOfRowsAffected);

        return numberOfRowsAffected;
    }

    /**Get the type from the URI (table name)*/
    @Nullable
    @Override
    public String getType(Uri uri) {

        int match = uriMatcher.match(uri);

        switch (match)
        {
            case ITEMS:
                return CONTENT_ITEM_TYPE;
            case ITEM_ID:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("GETTYPE - Wrong uri: "+ uri);
        }

    }

}
