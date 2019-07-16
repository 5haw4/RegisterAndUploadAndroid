package com.test.registerandupload.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.test.registerandupload.R;
import com.test.registerandupload.Utilities.ConvertBitmapToString;
import com.test.registerandupload.Utilities.General;
import com.test.registerandupload.api.RetrofitClient;
import com.test.registerandupload.interfaces.BitmapToStringCallback;
import com.test.registerandupload.model.User;

import org.json.JSONObject;

import java.io.FileNotFoundException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.test.registerandupload.Utilities.Constants.CAPTURE_IMAGE_REQUEST_CODE;
import static com.test.registerandupload.Utilities.Constants.IMAGE_UPLOAD_QUALITY;
import static com.test.registerandupload.Utilities.Constants.PICK_IMAGE_REQUEST_CODE;
import static com.test.registerandupload.Utilities.Constants.REFRESH_REQUEST_CODE;

public class CreatePostActivity extends AppCompatActivity {

    private static final String TAG = CreatePostActivity.class.getSimpleName();

    Context context;
    User user;

    TextInputLayout inputLayoutDescription;
    EditText descET;
    ImageView imagePreview;
    Button addImageBtn, removeImageBtn;

    Bitmap postImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        context = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Create New Post");

        if(getIntent() != null && getIntent().getExtras() != null) {
            Gson gson = new GsonBuilder().create();
            user = gson.fromJson(getIntent().getExtras().getString("user"), User.class);
        }

        inputLayoutDescription = findViewById(R.id.input_layout_description);
        descET = findViewById(R.id.desc_et);
        imagePreview = findViewById(R.id.image_preview);
        addImageBtn = findViewById(R.id.add_image_btn);
        removeImageBtn = findViewById(R.id.remove_image_btn);
        removeImageBtn.setEnabled(false);
        General.ChangeButtonDrawbleColor(context, removeImageBtn, android.R.color.darker_gray);

        addImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenAddImageAlert();
            }
        });

        removeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePreview.setImageBitmap(null);
                removeImageBtn.setEnabled(false);
                General.ChangeButtonDrawbleColor(context, removeImageBtn, android.R.color.darker_gray);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == CAPTURE_IMAGE_REQUEST_CODE && data.getExtras() != null) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    imagePreview.setImageBitmap(bitmap);
                    postImage = bitmap;
                    removeImageBtn.setEnabled(true);
                    General.ChangeButtonDrawbleColor(context, removeImageBtn, android.R.color.black);
                }
            } else if (requestCode == PICK_IMAGE_REQUEST_CODE && data.getData() != null) {
                Uri targetUri = data.getData();
                Bitmap bitmap;
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                    imagePreview.setImageBitmap(bitmap);
                    postImage = bitmap;
                    removeImageBtn.setEnabled(true);
                    General.ChangeButtonDrawbleColor(context, removeImageBtn, android.R.color.black);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = ((Activity) context).getMenuInflater();
        inflater.inflate(R.menu.check_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ok_btn:
                CreatePost();
                break;
            case android.R.id.home:
                FinishActivity(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        FinishActivity(false);
        super.onBackPressed();
    }

    private void CreatePost(){
        final ProgressDialog dialog = ProgressDialog.show(context, "Creating post","Please Wait...", true);

        //using an array instead of regular string so that the inner callback classes can access and modify its value
        final String[] imgStr = {""};

        //running this runnable after the conversion of the bitmap is finished
        final Runnable createPostRunnable = new Runnable() {
            @Override
            public void run() {
                Call<ResponseBody> call = RetrofitClient.getInstance()
                        .getPostsApi()
                        .createPost(General.GetUserKey(context), imgStr[0], descET.getText().toString());

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            String body = response.body().string();
                            JSONObject jsonObject = new JSONObject(body);
                            boolean error = jsonObject.getBoolean("error");
                            String message = jsonObject.getString("message");
                            if (!error) {
                                FinishActivity(true);
                            } else {
                                if (!message.equals(""))
                                    General.CreateAlertDialog(context, "", message);
                            }
                            dialog.dismiss();
                        } catch (Exception e) {
                            Log.e(TAG, "onResponse: " + e.getMessage());
                            General.CreateAlertDialog(context, "", "Something went wrong, please try again later.");
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                        General.CreateAlertDialog(context, "", "Something went wrong, please check your internet connection.");
                        dialog.dismiss();
                    }
                });
            }
        };

        if (postImage != null) {
            ConvertBitmapToString convertBitmapToString = new ConvertBitmapToString();
            convertBitmapToString.setBitmapToStringCallback(postImage, IMAGE_UPLOAD_QUALITY, new BitmapToStringCallback() {
                @Override
                public void onConversionFinished(String image) {
                    imgStr[0] = image;
                    /*
                        using AsyncTask to prevent the UI from lagging when creating
                        Retrofit's "Call" object (which may contain few mbs of an
                        image in a string base64 format)
                     */
                    AsyncTask.execute(createPostRunnable);
                }
            });
        } else {
            /*
                using AsyncTask to prevent the UI from lagging when creating
                Retrofit's "Call" object (which may contain few mbs of an
                image in a string base64 format)
            */
            AsyncTask.execute(createPostRunnable);
        }

    }

    private void OpenAddImageAlert(){

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.alert_dialog_photo_selector);

        Button CancelBTN = dialog.findViewById(R.id.cancel_btn);
        CancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        RelativeLayout PhotoGalleryLayout = dialog.findViewById(R.id.album_layout);
        PhotoGalleryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //gallery
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
                dialog.dismiss();
            }
        });

        RelativeLayout CameraLayout = dialog.findViewById(R.id.camera_layout);
        CameraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //camera
                Intent imageIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(imageIntent, CAPTURE_IMAGE_REQUEST_CODE);
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void FinishActivity(boolean refreshFeed){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("refresh_feed", refreshFeed);
        setResult(RESULT_OK,returnIntent);
        finish();
    }

}
