package com.test.registerandupload.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.test.registerandupload.R;

import java.io.ByteArrayOutputStream;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class General {

    public static void hideSoftKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                if (activity.getCurrentFocus() != null)
                    if (activity.getCurrentFocus().getWindowToken() != null)
                        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    public static void SetUserKey(Context context, String userKey){
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.user_key), MODE_PRIVATE).edit();
        editor.putString(context.getString(R.string.user_key), userKey);
        editor.apply();
    }

    public static String GetUserKey(Context context){
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.user_key), MODE_PRIVATE);
        return prefs.getString(context.getString(R.string.user_key), "");
    }

    public static void CreateAlertDialog(Context context, String title, String msg){
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setNegativeButton("Done", null)
                .show();
    }

    public static void ChangeButtonDrawbleColor(Context context, Button btn, int newColor) {
        Drawable[] removeBtnDrawables = btn.getCompoundDrawables();
        removeBtnDrawables[0].setColorFilter(context.getResources().getColor(newColor), PorterDuff.Mode.SRC_ATOP);
    }

}
