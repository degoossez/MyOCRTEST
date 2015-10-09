package com.example.dries.myocrtest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.googlecode.tesseract.android.TessBaseAPI;
import com.lamerman.FileDialog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public int REQUEST_SAVE=1;
    public int REQUEST_LOAD=2;
    public String LOAD_PATH ="/sdcard/Pictures/Motivational Quote Wallpapers/wallpaper1.jpg";
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
        if(requestCode==REQUEST_SAVE){
            String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
            try{
                File CodeFile =new File(filePath);
//if file doesnt exists, then create it
                if(!CodeFile.exists()) CodeFile.createNewFile();
                FileWriter fileWritter = new FileWriter(CodeFile,true);
                BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                bufferWritter.write("Your new text.");
                bufferWritter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(requestCode == REQUEST_LOAD)
        {
            String PathLoadFile = data.getStringExtra(FileDialog.RESULT_PATH);
            LOAD_PATH = PathLoadFile;
            Log.v("PATH_DRIES",PathLoadFile);
        }
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
            getPath();
        }

        return super.onOptionsItemSelected(item);
    }

    private void getPath() {
        Intent intent = new Intent(getBaseContext(), FileDialog.class);
        intent.putExtra(FileDialog.START_PATH, "/storage/emulated/0/Pictures/");
//can user select directories or not
        intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
//alternatively you can set file filter
        //Intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "png" ,"jpg"});

        startActivityForResult(intent, REQUEST_LOAD);
        Log.v("OCR_DRIES","EINDE");
        return;
    }

    protected void ocr() {
        String LOG_TAG = "OCR_DRIES";
        String IMAGE_PATH = LOAD_PATH;
        String DATA_PATH= "/sdcard/tesseract/";
        String LANG = "eng";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;

        Bitmap bitmap = BitmapFactory.decodeFile(IMAGE_PATH, options);

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
