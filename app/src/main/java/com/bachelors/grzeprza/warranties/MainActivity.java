package com.bachelors.grzeprza.warranties;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Toast;

import com.bachelors.grzeprza.warranties.data.ItemContract;
import com.bachelors.grzeprza.warranties.data.ItemContract.ItemEntry;
import com.bachelors.grzeprza.warranties.data.ItemDbHelper;

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


    }
    /**Toasts ({@link android.widget.Toast}) message about current number of rows in database*/
    private void displayInfo() {
        ItemDbHelper db = new ItemDbHelper(getApplicationContext());

        SQLiteDatabase sdb = db.getReadableDatabase();

        Cursor cursor = sdb.rawQuery("SELECT * FROM " + ItemContract.ItemEntry.TABLE_NAME, null);
        try{
            int number = cursor.getCount();

            Toast.makeText(this ,String.valueOf(number), Toast.LENGTH_LONG).show();
        }
        finally {
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

        switch (item.getItemId())
        {
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
                float price= random.nextFloat();
                int duration = random.nextInt(10)+1;

                ContentValues values = new ContentValues();
                values.put(ItemEntry.COLUMN_ITEM_NAME,"");
                values.put(ItemEntry.COLUMN_ITEM_PRICE,price);
                values.put(ItemEntry.COLUMN_BOUGHT_DATE,"28-11-16");
                values.put(ItemEntry.COLUMN_SHOP_NAME,"Sklep" + String.valueOf(random.nextInt()));
                values.put(ItemEntry.COLUMN_WARRANTY_DURATION,duration);
                values.put(ItemEntry.COLUMN_ITEM_PHOTO_URI,"zdje.jpg");
                values.put(ItemEntry.COLUMN_ITEM_RECEIPT_PHOTO_URI,"zdje.jpg");
                values.put(ItemEntry.COLUMN_ITEM_TYPE, ItemContract.ItemTypes.ELECTRONIC);

                db.insert(ItemEntry.TABLE_NAME,null,values);
                displayInfo();

                return true;

            case R.id.action_delete_data:
                return true;

            default: return super.onOptionsItemSelected(item);
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
}
