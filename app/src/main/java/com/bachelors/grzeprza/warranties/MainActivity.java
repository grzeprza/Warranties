package com.bachelors.grzeprza.warranties;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.bachelors.grzeprza.warranties.data.ItemContract;
import com.bachelors.grzeprza.warranties.data.ItemContract.ItemEntry;
import com.bachelors.grzeprza.warranties.data.ItemCursorAdapter;
import com.bachelors.grzeprza.warranties.notification.NotificationsManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.drive.Drive;

import java.io.IOException;
import java.util.Random;

import static com.bachelors.grzeprza.warranties.data.ItemContract.ItemEntry.CONTENT_URI;
import static com.bachelors.grzeprza.warranties.data.ItemDbHelper.DATABASE_NAME;

public class MainActivity extends GoogleDriveActivity
        implements NavigationView.OnNavigationItemSelectedListener, android.app.LoaderManager.LoaderCallbacks<Cursor>{

      /**Identifies loader being used to load items into listView rows*/
    private static final int ITEM_LOADER = 0;

    /**Instance of {@link ItemCursorAdapter}*/
    private ItemCursorAdapter itemCursorAdapter;

    /**AdMob full screen commercial*/
    private InterstitialAd mInterstitialAd;

    /**Variable responsible for app flow before/after commercial appeared.*/
    public static boolean loaded = false;

    /**Enables to use search at top bar*/
    private SearchManager searchManager;

    /**Reference to our item where we place query string*/
    private SearchView searchView;

    /**Initializes whole world.*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Init AdMob FullScreen Commercial
        mInterstitialAd = new InterstitialAd(this);
        // set the ad unit ID
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                loaded = true;
                showInterstitial();
                Toast.makeText(getApplicationContext(), "Click add", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdClosed() {
                if(loaded){
                Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
                startActivity(intent);}
               // Toast.makeText(getApplicationContext(), "Ad is closed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                loaded = false;
                Toast.makeText(getApplicationContext(), "Check internet!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLeftApplication() {
                //Toast.makeText(getApplicationContext(), "Ad left application!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdOpened() {
                //Toast.makeText(getApplicationContext(), "Ad is opened!", Toast.LENGTH_SHORT).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Creates query to database to get all items
        Cursor itemCursor = getContentResolver().query(ItemEntry.CONTENT_URI,null,null,null,null);
        //gets list view to put items form query result
        final ListView itemListView = (ListView) findViewById(R.id.listView_items);
        //gets view which is set when there is no data in the database
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.empty_view);
        itemCursorAdapter = new ItemCursorAdapter(getApplicationContext(),itemCursor);
        itemListView.setEmptyView(relativeLayout);
        itemListView.setAdapter(itemCursorAdapter);
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
                Intent displayItemInDetail = new Intent(getApplicationContext(), EditorActivity.class);
                displayItemInDetail.setData(ContentUris.withAppendedId(CONTENT_URI,id));
                startActivity(displayItemInDetail);
            }
        });

        itemListView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                getContentResolver().delete(ContentUris.withAppendedId(CONTENT_URI,id),null,null);
                NotificationsManager.deleteNotification(getApplicationContext(), Integer.parseInt(Long.toString(id)));
                Toast.makeText(MainActivity.this, "Item removed.", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        itemCursor.close();

        //Floating button on the bottom right - f: adding new item
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(itemListView.getCount() >= 3)
                {
                    AdRequest adRequest = new AdRequest.Builder()
                            //TODO: on production - comment addTestDevice
                            //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                            //.addTestDevice("40CE65C47616AFC7701238C8828E1E1F")
                            //TODO: get last category from database
                            //.addKeyword()
                            .build();
                    // Load ads into Interstitial Ads
                    mInterstitialAd.loadAd(adRequest);

                }
                else {
                    Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
                    startActivity(intent);
                }
            }
        });

        searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        handleIntent(getIntent());
        getLoaderManager().initLoader(ITEM_LOADER, null, this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    protected  void handleIntent(Intent intent)
    {
        if(Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Cursor c = getItemsByCompanyOrDescr(query);
            itemCursorAdapter.swapCursor(c);
        }
    }
    /***/
    private Cursor getItemsByCompanyOrDescr(String companyOrDescr)
    {
        String selection = ItemEntry.COLUMN_SHOP_NAME + " LIKE ? OR "+ ItemEntry.COLUMN_ITEM_NAME + " LIKE ? OR " + ItemEntry.COLUMN_ITEM_NAME + " LIKE ?";
        String [] selectionArgs = new String[]{String.valueOf("%"+companyOrDescr+"%"),String.valueOf("%"+companyOrDescr+"%"),String.valueOf("%"+companyOrDescr+"%")};
        return getContentResolver().query(ItemEntry.CONTENT_URI,null,selection,selectionArgs,null);
    }

    /**Methods displays commercial, when it is loaded*/
    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
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
        MenuInflater inflater =getMenuInflater();
                inflater.inflate(R.menu.main, menu);

        searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search_main).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                itemCursorAdapter.changeCursor(getItemsByCompanyOrDescr(query));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                itemCursorAdapter.changeCursor(getItemsByCompanyOrDescr(newText));
                return false;
            }
        });
      /*  searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                Toast.makeText(getApplicationContext(),"SELECT",Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Toast.makeText(getApplicationContext(), "CLICK", Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/

        return true;
    }

    /**Describes actions on pressing button in app menu*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:

                return true;

            case R.id.action_search_main:
                return true;

            case R.id.action_export:
                new AsyncTaskSaveDataToGoogleDrive().execute();
                return true;

            case R.id.action_import:
                new AsyncTaskReadDataFromGoogleDrive().execute();

                return true;
/*
            case R.id.action_insert_dummy_data:

                Random random = new Random();
                float price = random.nextFloat();
                int duration = random.nextInt(10) + 1;

                ContentValues values = new ContentValues();
                values.put(ItemEntry.COLUMN_ITEM_NAME, "TESLA");
                values.put(ItemEntry.COLUMN_ITEM_PRICE, price);
                values.put(ItemEntry.COLUMN_BOUGHT_DATE, "28-11-16");
                values.put(ItemEntry.COLUMN_SHOP_NAME, "Sklep" + String.valueOf(random.nextInt(9999)));
                values.put(ItemEntry.COLUMN_WARRANTY_DURATION, duration);
                values.put(ItemEntry.COLUMN_ITEM_PHOTO_URI, "zdje.jpg");
                values.put(ItemEntry.COLUMN_ITEM_RECEIPT_PHOTO_URI, "zdje.jpg");
                values.put(ItemEntry.COLUMN_ITEM_TYPE, ItemContract.ItemTypes.MOTORIZATION);

                getContentResolver().insert(CONTENT_URI,values);

                return true;

            case R.id.action_delete_data:
                getContentResolver().delete(CONTENT_URI,null,null);
                return true;
*/
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

    /**Describes what query has to be done at the LoaderManager call*/
    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

                return new android.content.CursorLoader(
                        this,
                        CONTENT_URI,
                        null,
                        null,
                        null,
                        null);
    }

    /**Changes old cursor to new one*/
    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        itemCursorAdapter.swapCursor(data);
    }
    /**Closes cursor*/
    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        itemCursorAdapter.swapCursor(null);
    }

    /**Information progress dialog informing about importing/exporting database*/
    private ProgressDialog pDialog;

    /**Inner class to Import data from GoogleDrive. Working in separate thread.*/
    class AsyncTaskSaveDataToGoogleDrive extends AsyncTask<Void,Void,Void>
    {
        /**
         * Before starting background thread
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Starting download");

            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading... Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected Void doInBackground(Void... params) {
            try {
                saveToGoogleDrive(
                        Drive.DriveApi.getAppFolder(getGoogleApiClient()),
                        DATABASE_NAME,
                        "text/plain",
                        exportDBtoText());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        /**
         * After completing background task
         * **/
        @Override
        protected void onPostExecute(Void aVoid) {
            pDialog.dismiss();
            super.onPostExecute(aVoid);
        }


    }

    /**Inner class to Import data from GoogleDrive. Working in separate thread.*/
    class AsyncTaskReadDataFromGoogleDrive extends AsyncTask<Void,Void,Void>
    {

        /**
         * Before starting background thread
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Starting download");

            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading... Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected Void doInBackground(Void... params) {
            readDataFromGoogleDrive();
            return null;
        }


        @Override
        protected void onCancelled() {

            super.onCancelled();
        }

        /**
         * After completing background task
         * **/

        @Override
        protected void onPostExecute(Void aVoid) {
            pDialog.dismiss();
            super.onPostExecute(aVoid);
        }
    }

}
