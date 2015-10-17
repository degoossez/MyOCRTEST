package com.example.dries.myocrtest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.IOException;

public class MainActivity extends Activity {
    public int REQUEST_SAVE=1;
    public int REQUEST_LOAD=2;
    public String LOAD_PATH ="/sdcard/Pictures/Motivational Quote Wallpapers/wallpaper1.jpg";


    /*
     *RenderScript
     */
    RsScript RenderScriptObject;
    /*
    * END
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("OCR_DRIES","requestCode" + Integer.toString(requestCode));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings){
            //settings
        }
        else if (id == R.id.action_findText) {
            ocr();
            return true;
        }
        else if (id == R.id.action_selectFile) {
            //
        }

        return super.onOptionsItemSelected(item);
    }

    protected void ocr() {
        String LOG_TAG = "OCR_DRIES";
        String IMAGE_PATH = LOAD_PATH;
        String DATA_PATH= "/sdcard/tesseract/";
        String LANG = "eng";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;


        Bitmap bitmap = BitmapFactory.decodeFile(IMAGE_PATH, options);
/*
        RenderScriptObject = new RsScript(this,(ImageView)findViewById(R.id.imageView));
        RenderScriptObject.setInputBitmap(bitmap);
        bitmap=null;
        //Bitmap outBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false);
        RenderScriptObject.RenderScriptInverse();
        while(RenderScriptObject.Working==true) {
            //wait
        }
        bitmap = RenderScriptObject.getOutputBitmap();
        if(bitmap==null){
            Log.v(LOG_TAG, "bitmap==null" );
        }
        else{
            Log.v(LOG_TAG, "bitmap!=null" );

        }
*/


        try {
            ExifInterface exif = new ExifInterface(IMAGE_PATH);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Log.v(LOG_TAG, "Orient: " + exifOrientation);

            int rotate = 0;
            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            Log.v(LOG_TAG, "Rotation: " + rotate);

            if (rotate != 0) {

                // Getting width & height of the given image.
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();

                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);

                // Rotating Bitmap
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
                // tesseract req. ARGB_8888
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Rotate or coversion failed: " + e.toString());
        }

        ImageView iv = (ImageView) findViewById(R.id.imageView);
        iv.setImageBitmap(bitmap);
        iv.setVisibility(View.VISIBLE);

        Log.v(LOG_TAG, "Before baseApi");

        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATA_PATH, LANG);
        baseApi.setImage(bitmap);
        String recognizedText = baseApi.getUTF8Text();
        baseApi.end();

        Log.v(LOG_TAG, "OCR Result: " + recognizedText);

        // clean up and show
        if (LANG.equalsIgnoreCase("eng")) {
            recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
        }
        if (recognizedText.length() != 0) {
            ((TextView) findViewById(R.id.textView)).setText(recognizedText.trim());
        }
    }
}
