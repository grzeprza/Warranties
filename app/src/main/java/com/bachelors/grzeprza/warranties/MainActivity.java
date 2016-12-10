package com.bachelors.grzeprza.warranties;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bachelors.grzeprza.warranties.data.ItemContract;
import com.bachelors.grzeprza.warranties.data.ItemContract.ItemEntry;
import com.bachelors.grzeprza.warranties.data.ItemDbHelper;
import com.bachelors.grzeprza.warranties.data.ItemProvider;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Random;

import static com.bachelors.grzeprza.warranties.EditorActivity.IMAGE_DIRECTORY_NAME;
import static com.bachelors.grzeprza.warranties.data.ItemDbHelper.DATABASE_NAME;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
                startActivity(intent);

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    @Override
    protected void onStart() {
        super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        displayInfo();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    /**
     * Toasts ({@link Toast}) message about current number of rows in database
     */
    private void displayInfo() {

        String [] projection = {ItemEntry._ID, ItemEntry.COLUMN_ITEM_NAME, ItemEntry.COLUMN_BOUGHT_DATE};

       /// Cursor cursor = sdb.query(ItemEntry.TABLE_NAME, projection, null, null, null, null, null);
        Cursor cursor = getContentResolver().query(ItemEntry.CONTENT_URI,projection,null,null,null);
        try {
            int itemNameIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int boughtDate = cursor.getColumnIndex(ItemEntry.COLUMN_BOUGHT_DATE);

            while (cursor.moveToNext()) {
                String itemName = cursor.getString(itemNameIndex);
                String date = cursor.getString(boughtDate);

                Toast.makeText(getApplicationContext(),itemName + " bought on " + date + "\n",Toast.LENGTH_SHORT).show();
            }

        } finally {
            cursor.close();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                //goes directly to setting
                return true;

            case R.id.action_search_main:
                //looks for item in list
                //for test purposes diplays number of rows in database
                return true;

            case R.id.action_insert_dummy_data:

                ItemDbHelper dbHelper = new ItemDbHelper(getApplicationContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                Random random = new Random();
                float price = random.nextFloat();
                int duration = random.nextInt(10) + 1;

                ContentValues values = new ContentValues();
                values.put(ItemEntry.COLUMN_ITEM_NAME, "");
                values.put(ItemEntry.COLUMN_ITEM_PRICE, price);
                values.put(ItemEntry.COLUMN_BOUGHT_DATE, "28-11-16");
                values.put(ItemEntry.COLUMN_SHOP_NAME, "Sklep" + String.valueOf(random.nextInt()));
                values.put(ItemEntry.COLUMN_WARRANTY_DURATION, duration);
                values.put(ItemEntry.COLUMN_ITEM_PHOTO_URI, "zdje.jpg");
                values.put(ItemEntry.COLUMN_ITEM_RECEIPT_PHOTO_URI, "zdje.jpg");
                values.put(ItemEntry.COLUMN_ITEM_TYPE, ItemContract.ItemTypes.ELECTRONIC);

                db.insert(ItemEntry.TABLE_NAME, null, values);
                displayInfo();

                return true;

            case R.id.action_delete_data:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
