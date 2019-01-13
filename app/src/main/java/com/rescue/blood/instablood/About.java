package com.rescue.blood.instablood;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

/**
 * Created by sayan07 on 14/5/18.
 */

class About extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        /*Bitmap originalImage= Bitmap.createScaledBitmap (BitmapFactory.decodeResource(getResources(), R.drawable.sayan), 160, 160, true);
        ((ImageView)findViewById(R.id.dp)).setImageBitmap(originalImage);


        originalImage= Bitmap.createScaledBitmap (BitmapFactory.decodeResource(getResources(), R.drawable.sanket), 160, 160, true);
        ((ImageView)findViewById(R.id.dp2)).setImageBitmap(originalImage);

        originalImage= Bitmap.createScaledBitmap (BitmapFactory.decodeResource(getResources(), R.drawable.sanchari), 160, 160, true);
        ((ImageView)findViewById(R.id.dp3)).setImageBitmap(originalImage);

        originalImage= Bitmap.createScaledBitmap (BitmapFactory.decodeResource(getResources(), R.drawable.sayani), 160, 160, true);
        ((ImageView)findViewById(R.id.dp4)).setImageBitmap(originalImage);

        originalImage= Bitmap.createScaledBitmap (BitmapFactory.decodeResource(getResources(), R.drawable.shibu), 160, 160, true);
        ((ImageView)findViewById(R.id.dp5)).setImageBitmap(originalImage);*/

    }
}
