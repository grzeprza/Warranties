package com.bachelors.grzeprza.warranties;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bachelors.grzeprza.warranties.data.ItemContract;
import com.bachelors.grzeprza.warranties.data.ItemContract.ItemTypes;
import com.bachelors.grzeprza.warranties.data.ItemDbHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import static com.bachelors.grzeprza.warranties.data.ItemContract.ItemEntry.CONTENT_URI;

public class EditorActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    /**Tag for Log messages*/
    private final String LOG_TAG = getClass().getSimpleName();

    /**Spinner with ItemContract.ItemTypes options as simple dropdown list.*/
    private Spinner spinnerItemType;

    /**Interface for opening DataPickerDialog after clicking on EditText*/
    private DatePickerDialog.OnDateSetListener editTextListenerForDatePicker;
    /**Shows DatePickerDialog to pick 'Bought Date'*/
    private EditText editTextDatePickerDialog;
    /**Enables API for managing Date Picker Dialog*/
    private DatePickerDialog datePickerDialog;
    /**Formats date to appropriate figure*/
    private SimpleDateFormat simpleDateFormatter;
    /**Holds calendar instance*/
    private Calendar calendar;

    /**Easy access to selected spinner option with item types.*/
    private int mItemType = -1;

    /**Variable corresponging to request capture value. Makes code more readable.*/
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_CAPTURE_IMAGE_CODE = 100;
    /**Folder directory name where images will be saved*/
    static final String IMAGE_DIRECTORY_NAME = "Warranties";

    /**Variable is resposible for saying which image file will be used.
     *Variable is set when user clicks camera button to capture image.
     *Value TRUE means itemImageFile
     *Value FALSE means itemReceiptImageFile*/
    private boolean chooseItemCapture;

    /**Future icon to take item picture*/
    private Button buttonTakeItemPhoto;
    /**Taken item photo URI*/
    private Uri takenItemFileUri;
    /**Taken receipt photo URI*/
    private Uri takenReceiptFileUri;
    /**Future icon to take receipt picture*/
    private Button buttonTakeReceiptPhoto;
    /**Temporary button to show stored item picture*/
    private Button btnShowItem;
    /**Temporart button to show stored receipt picture*/
    private Button btnShowReceipt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

       Intent intent = getIntent();
        Uri currentItemUri = intent.getData();
        if(currentItemUri != null)
        {
            this.setTitle("Warranties \\ Edit");
        }
        else this.setTitle("Warranties \\ Add Item");

        configureOptionSpinnerForItemtypes();

        configureDatePickerDialogForBoughtDate();

        //Pinned button to take item picture
        buttonTakeItemPhoto = (Button) findViewById(R.id.button_item_photo);
        buttonTakeItemPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseItemCapture = true;
                dispatchTakePictureIntent();
            }
        });

        //pinned button to take receipt item picture
        buttonTakeReceiptPhoto = (Button) findViewById(R.id.button_item_receipt_photo);
        buttonTakeReceiptPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseItemCapture = false;
                dispatchTakePictureIntent();
            }
        });

        //pinned partly temporary files to show taken pictures in new intent.
        //at the beggining invisible.
        btnShowItem = (Button) findViewById(R.id.button_showPhoto);
        btnShowItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent in = new Intent(getApplicationContext(),DisplayTakenImage.class);
                    in.putExtra("imageUri",takenItemFileUri.getPath());
                    startActivity(in);
            }
        });


        //pinned partly temporary files to show receipt taken pictures in new intent.
        //at the beggining invisible.
        btnShowReceipt = (Button) findViewById(R.id.button_showReceiptPhoto);
        btnShowReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getApplicationContext(), DisplayTakenImage.class);
                in.putExtra("imageUri",takenReceiptFileUri.getPath());
                startActivity(in);
            }
        });

        addToolbarAndConfigure();
    }

    /**Adds toolbar and sets back arrow button*/
    private void addToolbarAndConfigure()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_editor);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**Adds menu_editor (save button) to Toolbar*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**Invokes an intent to capture a photo*/
    private void dispatchTakePictureIntent()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //Ensure that there is camera activity to handle the intent
        if(takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            //chooses which item or receipt user wants to capture. Action done for later saving to database.
            if(chooseItemCapture)
            {
                takenItemFileUri = getOutputMediaFileUri(REQUEST_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, takenItemFileUri);
            }
            else
            {
                takenReceiptFileUri = getOutputMediaFileUri(REQUEST_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, takenReceiptFileUri);
            }
            startActivityForResult(takePictureIntent, REQUEST_CAPTURE_IMAGE_CODE);
        }
    }

    /**Helper method that gets Uri for given file. Type may be used when capturing image to divide code(DRY)*/
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**Method creates and returns file for image. Type may be used when capturing image to divide code(DRY)*/
    private static File getOutputMediaFile(int type) {

        // External location
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;

            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "ITEM_IMG_" + timeStamp + ".jpg");
        return mediaFile;
    }

    /**Handles result of startActivityForResult( Bitmap, REQUEST TYPE)*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //if everything is all right, set approriate button to enable seeing taken image.
        if(requestCode == REQUEST_CAPTURE_IMAGE_CODE && resultCode == RESULT_OK)
        {
           Log.i("onActivityResult", "Taking picture: successful");
            if(chooseItemCapture) {
                Log.i("onActivityResult", takenItemFileUri.getPath());
                btnShowItem.setVisibility(View.VISIBLE);
                btnShowItem.setEnabled(true);
            }else {
                Log.i("onActivityResult", takenReceiptFileUri.getPath());
                btnShowReceipt.setVisibility(View.VISIBLE);
                btnShowReceipt.setEnabled(true);
            }
        }
        else
        {
            Log.i("onActivityResult", "Taking picture: failed");
        }
    }

    /**Method handles toolbar actions - save, cancel*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.button_ok_save:

                //if all field are filled properly then move on to main else inform user
                if(insertItem())
                {
                    //ends activity
                    finish();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please fill apropriate data upon",Toast.LENGTH_LONG).show();
                }

                return true;

            case R.id.button_cancel:
                finish();
                return true;

            //if user action was not recognized.
            default: return super.onOptionsItemSelected(item);
        }
    }

    /**Inserts inputted data to database. User has to fill all informations.*/
    private boolean insertItem() {
        //Get Item Name from EditText and checks whether it was filled
        EditText editText_itemName = (EditText) findViewById(R.id.edit_text_item_name);
        String itemName = editText_itemName.getText().toString().trim();
        if(itemName.isEmpty()) return false;

        //Gets Shop Name from EditText and checks whether it was filled
        EditText editText_shopName = (EditText) findViewById(R.id.edit_text_shop_name);
        String shopName = editText_shopName.getText().toString().trim();
        if(shopName.isEmpty()) return false;

        //Gets Item Price and checks whether it was filled correctly
        EditText editText_itemPrice = (EditText) findViewById(R.id.edit_text_item_price);
        String itemPriceString = editText_itemPrice.getText().toString().trim();
        if(itemPriceString.isEmpty()) return false;
        float itemPrice = Float.valueOf(itemPriceString);
        if(itemPrice <= 0.0) return false;

        //Check whether item photo was taken
        if(takenItemFileUri== null) return false;
        String itemPhotoUri = takenItemFileUri.toString();

        //checks whether item receipt photo was taken
        if(takenReceiptFileUri==null) return false;
        String receiptPhotoUri = takenReceiptFileUri.toString();

        //Gets item type selected by user
        if(mItemType<0) return false;

        //Get bought date
        EditText editText_boughtDate = (EditText) findViewById(R.id.edit_text_bought_date);
        String boughtDate = editText_boughtDate.getText().toString().trim();
        if(boughtDate.isEmpty()) return false;

        //Get warranty duration in weeks
        EditText editText_warrantyDuration = (EditText) findViewById(R.id.edit_text_warranty_duration);
        String warrantyDurationString = editText_warrantyDuration.getText().toString().trim();
        if(warrantyDurationString.isEmpty()) return false;
        int warrantyDuration = Integer.valueOf(warrantyDurationString);

        ContentValues values = new ContentValues();
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_NAME,itemName);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PRICE,itemPrice);
        values.put(ItemContract.ItemEntry.COLUMN_BOUGHT_DATE,boughtDate);
        values.put(ItemContract.ItemEntry.COLUMN_SHOP_NAME,shopName);
        values.put(ItemContract.ItemEntry.COLUMN_WARRANTY_DURATION,warrantyDuration);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PHOTO_URI,itemPhotoUri);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_RECEIPT_PHOTO_URI,receiptPhotoUri);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_TYPE, mItemType);

        Uri addedRowId = getContentResolver().insert(ItemContract.ItemEntry.CONTENT_URI,values);

        if(addedRowId == null)
            Toast.makeText(getApplicationContext(),"Failed to save, try again.",Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(),"Item saved",Toast.LENGTH_SHORT).show();

        return true;
    }

    /**Create DatePickerDialog for Bought Date*/
    private void configureDatePickerDialogForBoughtDate()
    {
        calendar = Calendar.getInstance();

        //sets formating after user chooses date with DatePickerDialog
        editTextListenerForDatePicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                //Formats date to f.e. 20-11-2016
                simpleDateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.UK);

                editTextDatePickerDialog. setText(simpleDateFormatter.format(calendar.getTime()));
            }
        };

        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int month = calendar.get(Calendar.MONTH);
        final int year = calendar.get(Calendar.YEAR);

        //refers to EditText with date and add onClickListener which pops up DatePickerDialog
        editTextDatePickerDialog = (EditText) findViewById(R.id.edit_text_bought_date);
        editTextDatePickerDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(EditorActivity.this, editTextListenerForDatePicker, year, month, day).show();
            }
        });
    }

    /**Create Option Spinner for ItemTypes*/
    private void configureOptionSpinnerForItemtypes()
    {
        spinnerItemType = (Spinner) findViewById(R.id.spinner_item_type);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,R.array.item_types,android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerItemType.setAdapter(spinnerAdapter);
        spinnerItemType.setOnItemSelectedListener(this);
    }

    /**AdapterView.OnItemSelectedListener method implementation.
     * User selects an option which is saved to mItemType variable and later on to database*/
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //User selects one of few options
        String selection = (String) parent.getItemAtPosition(position);
        if(!TextUtils.isEmpty(selection))
        {
            switch (selection)
            {
                case "Electronic": mItemType = ItemTypes.ELECTRONIC; break;
                case "Fashion": mItemType = ItemTypes.FASHION; break;
                case "House": mItemType = ItemTypes.HOUSE; break;
                case "Sport": mItemType = ItemTypes.SPORT; break;
                case "Motorization": mItemType = ItemTypes.MOTORIZATION; break;
                case "Garden": mItemType = ItemTypes.GARDEN; break;
            }
        }
       // Toast.makeText(this, selection + " + " + Integer.toString(mItemType), Toast.LENGTH_SHORT).show();
    }

    /**AdapterView.OnItemSelectedListener method.
     * When user forgets to choose any option then mItemType variable will have value -1 which will be checked before saving to database.
     * User has to choose at least 'Other' option*/
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        mItemType = -1;
    }
}
