package com.bachelors.grzeprza.warranties;

import android.app.Activity;

import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.bachelors.grzeprza.warranties.data.ItemContract.ItemTypes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditorActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

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

    private Button buttonTakeItemPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        configureOptionSpinnerForItemtypes();

        configureDatePickerDialogForBoughtDate();

        //Pinned button to take item picture
        buttonTakeItemPhoto = (Button) findViewById(R.id.button_item_photo);
        buttonTakeItemPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"TOOK A PICTURE", Toast.LENGTH_SHORT).show();
            }
        });



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_editor);
        setSupportActionBar(toolbar);
    }

    /**Create DatePickerDialog for Bought Date*/
    public void configureDatePickerDialogForBoughtDate()
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
    public void configureOptionSpinnerForItemtypes()
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
