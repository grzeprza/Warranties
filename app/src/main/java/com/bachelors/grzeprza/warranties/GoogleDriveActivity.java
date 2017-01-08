package com.bachelors.grzeprza.warranties;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.bachelors.grzeprza.warranties.MainActivity;
import com.bachelors.grzeprza.warranties.data.ItemContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import static com.bachelors.grzeprza.warranties.EditorActivity.IMAGE_DIRECTORY_NAME;
import static com.bachelors.grzeprza.warranties.data.ItemContract.ItemEntry.CONTENT_URI;
import static com.bachelors.grzeprza.warranties.data.ItemDbHelper.DATABASE_NAME;

/**
 * Created by grzeprza on 08.01.2017.
 */

public abstract class GoogleDriveActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GoogleDriveActivity";

    /**Directory used to create temporary local file for import/export purposes to GoogleDrive*/
    protected final String DIRECTORY_TEMP_EXPORT_FILE = Environment.getExternalStorageDirectory()+ File.separator + IMAGE_DIRECTORY_NAME + File.separator + "warrantyFile.txt";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Called when activity gets visible. A connection to Drive services need to
     * be initiated as soon as the activity is visible. Registers
     * {@code ConnectionCallbacks} and {@code OnConnectionFailedListener} on the
     * activities itself.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    /**
     * Handles resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Called when activity gets invisible. Connection to Drive service needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onPause() {

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");
    }

    /**
     * Called when {@code mGoogleApiClient} is disconnected.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution is
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    /**
     * Getter for the {@code GoogleApiClient}.
     */
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }


    /**Exports Database to TextFile - textfile is uploaded later to Google Drive*/
    protected File exportDBtoText() throws IOException {
        File txtFile = new File(DIRECTORY_TEMP_EXPORT_FILE);
        PrintWriter printWriter = null;
        Cursor cursor = getContentResolver().query(CONTENT_URI,null,null,null,null);
        try
        {
            printWriter = new PrintWriter(new FileWriter(txtFile));
            String resultRow = "";
            while(cursor.moveToNext())
            {
                String name = cursor.getString(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME));
                String price = cursor.getString(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PRICE));
                String boughtDate = cursor.getString(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_BOUGHT_DATE));
                String shopName = cursor.getString(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_SHOP_NAME));
                int warrantyDuration = cursor.getInt(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_WARRANTY_DURATION));
                String photoUriString = cursor.getString(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PHOTO_URI));
                String receiptUriString = cursor.getString(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_RECEIPT_PHOTO_URI));
                int type = cursor.getInt(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_TYPE));

                resultRow = name +"|"+ price +"|"+ boughtDate +"|"+ shopName  +"|"+String.valueOf(warrantyDuration) +"|"+ photoUriString +"|"+ receiptUriString  +"|"+String.valueOf(type)+"\r\n";
                printWriter.print(resultRow);
                System.out.println(resultRow);
            }
        }finally {
            cursor.close();
            printWriter.close();
            return txtFile;
        }
    }

    /**Creates file in AppFolder in GoogleDrive based on provided {@param mimeType} and reference to file {@param fileReference}.
     * Method retrieves database information to text file which is saved on GoogleDrive*/
    protected void saveToGoogleDrive(final DriveFolder driveFolder, final String fileName,
                           final String mimeType, final java.io.File fileReference) {
        if (getGoogleApiClient() != null && driveFolder != null && fileName != null && mimeType != null && fileReference != null) try {
            Drive.DriveApi.newDriveContents(getGoogleApiClient()).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                    DriveContents driveContents = driveContentsResult != null && driveContentsResult.getStatus().isSuccess() ?
                            driveContentsResult.getDriveContents() : null;

                    if (driveContents != null) try {
                        OutputStream outputStream = driveContents.getOutputStream();
                        if (outputStream != null) try {
                            InputStream inputStream = new FileInputStream(fileReference);
                            byte[] buffer = new byte[4096];
                            int bytesToSend;
                            while ((bytesToSend = inputStream.read(buffer, 0, buffer.length)) > 0) {
                                outputStream.write(buffer, 0, bytesToSend);
                                outputStream.flush();
                            }
                        }
                        finally { outputStream.close();}

                        MetadataChangeSet meta = new MetadataChangeSet.Builder().setTitle(fileName).setMimeType(mimeType).build();

                        driveFolder.createFile(getGoogleApiClient(), meta, driveContents).setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                            @Override
                            public void onResult(DriveFolder.DriveFileResult driveFileResult) {
                                if (driveFileResult != null && driveFileResult.getStatus().isSuccess()) {
                                    DriveFile driveFile = driveFileResult != null && driveFileResult.getStatus().isSuccess() ?
                                            driveFileResult.getDriveFile() : null;
                                    if (driveFile != null) {
                                        driveFile.getMetadata(getGoogleApiClient()).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
                                            @Override
                                            public void onResult(DriveResource.MetadataResult metadataResult) {
                                                if (metadataResult != null && metadataResult.getStatus().isSuccess()) {
                                                    //when uploaded successfully save DriveID to SharedPreferences
                                                    final DriveId mDriveId = metadataResult.getMetadata().getDriveId();
                                                    SharedPreferences sharedPreferences = getSharedPreferences(DATABASE_NAME,MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                    editor.putString(DATABASE_NAME,mDriveId.encodeToString());
                                                    editor.commit();
                                                    Log.i("UPLOAD FILE","SUCCESFULLY EXPORTED");
                                                }
                                            }
                                        });
                                    }
                                } else { /* report error */     }
                            }
                        });
                    } catch (Exception e) { e.printStackTrace(); }
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**Retrieves data from GoogleDrive based on key in {@Link SharedPreferences} under {@Link DATABASE_NAME}.
     * Method clears database and inserts rows based on retrieved data.*/
    protected void readDataFromGoogleDrive() {
        byte[] buf = null;
        if (getGoogleApiClient() != null && getGoogleApiClient().isConnected()) try {

            SharedPreferences sharedPref = getSharedPreferences(DATABASE_NAME, Context.MODE_PRIVATE);
            String exitingDatabaseFile = sharedPref.getString(DATABASE_NAME, "");
            //System.out.println();

            //System.out.println("STRING: " + exitingDatabaseFile);
            //System.out.println("mDriveID" + mDriveId.encodeToString());

            DriveFile df = Drive.DriveApi.getFile(getGoogleApiClient(), DriveId.decodeFromString(exitingDatabaseFile));
            df.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                    .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                        @Override
                        public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                            if ((driveContentsResult != null) && driveContentsResult.getStatus().isSuccess()) {

                                //delete database using content resolver
                                getContentResolver().delete(CONTENT_URI,null,null);

                                DriveContents driveContents = driveContentsResult.getDriveContents();
                                BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(driveContents.getInputStream()));
                                String currLine;
                                ContentValues values;
                                try
                                {
                                    while ((currLine = reader.readLine()) != null)
                                    {
                                        String [] splittedLine = currLine.split("\\|");
                                        //System.out.println(Arrays.toString(splittedLine));

                                        //Retrieve values from string to variables
                                        String itemName = splittedLine[0];
                                        float itemPrice = Float.valueOf(splittedLine[1]);
                                        String boughtData = splittedLine[2];
                                        String shopName = splittedLine[3];
                                        int warrantyDuration = Integer.valueOf(splittedLine[4]);
                                        String itemPhotUri = splittedLine[5];
                                        String receiptPhotoUri = splittedLine[6];
                                        int itemType = Integer.valueOf(splittedLine[7]);

                                        values = new ContentValues();
                                        values.put(ItemContract.ItemEntry.COLUMN_ITEM_NAME,itemName);
                                        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PRICE,itemPrice);
                                        values.put(ItemContract.ItemEntry.COLUMN_BOUGHT_DATE,boughtData);
                                        values.put(ItemContract.ItemEntry.COLUMN_SHOP_NAME,shopName);
                                        values.put(ItemContract.ItemEntry.COLUMN_WARRANTY_DURATION,warrantyDuration);
                                        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PHOTO_URI,itemPhotUri);
                                        values.put(ItemContract.ItemEntry.COLUMN_ITEM_RECEIPT_PHOTO_URI,receiptPhotoUri);
                                        values.put(ItemContract.ItemEntry.COLUMN_ITEM_TYPE, itemType);

                                        getContentResolver().insert(ItemContract.ItemEntry.CONTENT_URI, values);
                                        values.clear();
                                    }

                                    reader.close();
                                }
                                catch (FileNotFoundException e)
                                {
                                    e.printStackTrace();
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                                finally {
                                    Log.i("","SUCCESSFULLY UPDATED");
                                }
                                driveContents.discard(getGoogleApiClient());
                            }
                        }
                    });
        } catch (Exception e) { e.printStackTrace(); }
    }


}
