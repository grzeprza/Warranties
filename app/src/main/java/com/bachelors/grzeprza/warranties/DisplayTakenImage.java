package com.bachelors.grzeprza.warranties;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class DisplayTakenImage extends AppCompatActivity {

    private ImageView mainImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_taken_image);

        String path;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                path = null;
            } else {
                path = extras.getString("imageUri");
            }
        } else {
            path = (String) savedInstanceState.getSerializable("imageUri");
        }
        Log.i("DisplayTakenImage", path);
        Uri imageUri = Uri.parse(path);

        mainImageView = (ImageView) findViewById(R.id.imageView_takenImage);

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 2;
            //Log.i("onActivityResult",fileUri.getPath());
            final Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath(),
                    options);

            mainImageView.setImageBitmap(bitmap);
            //bitmap.recycle();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
