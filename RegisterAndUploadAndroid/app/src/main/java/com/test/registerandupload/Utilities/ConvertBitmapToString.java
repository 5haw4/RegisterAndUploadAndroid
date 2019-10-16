package com.test.registerandupload.Utilities;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.test.registerandupload.interfaces.BitmapToStringCallback;

import java.io.ByteArrayOutputStream;


/*
    Handeling the convertion of the bitmap to a string
*/

public class ConvertBitmapToString {

    public void setBitmapToStringCallback(Bitmap bitmap, int quality, BitmapToStringCallback bitmapToStringCallback){
        new ConversionAsync(bitmapToStringCallback, bitmap, quality).execute();
    }

    private static class ConversionAsync extends AsyncTask<Void, Void, Void>
    {

        private BitmapToStringCallback bitmapToStringCallback;
        private String imgStr;
        private Bitmap bitmap;
        private int quality;

        private ConversionAsync(BitmapToStringCallback bitmapToStringCallback, Bitmap bitmap, int quality) {
            this.bitmapToStringCallback = bitmapToStringCallback;
            this.bitmap = bitmap;
            this.quality = quality;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(Void... params) {
            imgStr = null;
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, quality, byteArrayOutputStream);
                byte[] bytes = byteArrayOutputStream.toByteArray();
                imgStr = Base64.encodeToString(bytes,Base64.DEFAULT);
            } catch (Exception e){
                Log.e("ConvertBitmapToString","Exception: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (bitmapToStringCallback != null)
                bitmapToStringCallback.onConversionFinished(imgStr);
        }

    }

}
