package com.test.registerandupload.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.test.registerandupload.R;
import com.test.registerandupload.Utilities.General;
import com.test.registerandupload.api.RetrofitClient;
import com.test.registerandupload.model.User;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    Context context;

    boolean onLoginGoToPrevActivity = false;

    ScrollView mainLoginLayout;
    LinearLayout loadingLayout, scrollViewMainChild;

    boolean isLoginState = true; //true for login / false for register
    boolean isLoadingData = false;

    TextInputLayout unameTextInputLayout, emailTextInputLayout, passTextInputLayout;
    EditText unameET, emailET, passET;
    Button actionBtn;
    TextView switchBtn, titleTV, skipTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Login / Register");

        if(getIntent() != null && getIntent().getExtras() != null) {
            onLoginGoToPrevActivity = getIntent().getExtras().getBoolean("show_back_arrow");
            if(onLoginGoToPrevActivity) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        mainLoginLayout = findViewById(R.id.main_login_layout);
        loadingLayout = findViewById(R.id.loading_layout);

        unameTextInputLayout = findViewById(R.id.input_layout_username);
        emailTextInputLayout = findViewById(R.id.input_layout_email);
        passTextInputLayout = findViewById(R.id.input_layout_password);

        unameET = findViewById(R.id.username_et);
        emailET = findViewById(R.id.email_et);
        passET = findViewById(R.id.password_et);
        switchBtn = findViewById(R.id.switch_btn);
        actionBtn = findViewById(R.id.action_btn);

        titleTV = findViewById(R.id.title_tv);
        skipTV = findViewById(R.id.skip_btn);

        scrollViewMainChild = findViewById(R.id.scroll_view_main_child);
        scrollViewMainChild.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                General.hideSoftKeyboard((Activity) context);
                return true;
            }
        });
        passET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                PerformAction();
                return true;
            }
        });
        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailTextInputLayout.setErrorEnabled(false);
                passTextInputLayout.setErrorEnabled(false);
                unameTextInputLayout.setErrorEnabled(false);
                ChangeVisibilityOfLayouts(true);
            }
        });
        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PerformAction();
            }
        });

        skipTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoToFeedActivity(null);
            }
        });

        ValidateUserKey();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void ValidateUserKey(){
        String userKey = General.GetUserKey(context);

        isLoadingData = true;
        ChangeVisibilityOfLayouts(false);

        Call<ResponseBody> call = RetrofitClient.getInstance()
                .getUserApi()
                .validateUserKey(userKey);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String body = response.body().string();
                    JSONObject jsonObject = new JSONObject(body);
                    boolean isLoggedIn = jsonObject.getBoolean("isLoggedIn");
                    if(isLoggedIn) {
                        JSONObject userJson = jsonObject.getJSONObject("user");
                        User user = new User(
                                userJson.getString("user_id"),
                                userJson.getString("username"),
                                userJson.getString("email")
                        );
                        GoToFeedActivity(user);
                    } else {
                        isLoadingData = false;
                        ChangeVisibilityOfLayouts(false);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onResponse: " + e.getMessage());
                    Log.e(TAG, "onResponse: " + e.getStackTrace()[0].getLineNumber() + " ~ " + response.code() + " ~ " + response.errorBody());
                    isLoadingData = false;
                    ChangeVisibilityOfLayouts(false);
                    General.CreateAlertDialog(context,"","Something went wrong, please try again later.");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                Log.e(TAG, "onFailure: " + t.getStackTrace()[0].getLineNumber());
                isLoadingData = false;
                ChangeVisibilityOfLayouts(false);
                General.CreateAlertDialog(context,"","Something went wrong, please check your internet connection.");
            }
        });
    }

    private void PerformAction(){
        General.hideSoftKeyboard((Activity) context);

        String email = emailET.getText().toString();
        String uname = unameET.getText().toString();
        String password = passET.getText().toString();

        Call<ResponseBody> call;

        if(isLoginState) {
            if(email.isEmpty() || password.isEmpty()) {
                General.CreateAlertDialog(context,"","Please enter all the required fields.");
                return;
            }
            call = RetrofitClient.getInstance()
                    .getUserApi()
                    .login(email, password);
        } else {
            if(uname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                General.CreateAlertDialog(context,"","Please enter all the required fields.");
                return;
            }
            call = RetrofitClient.getInstance()
                    .getUserApi()
                    .register(uname, email, password);
        }

        RegisterLogin(call);
    }

    private void RegisterLogin(Call<ResponseBody> call){
        isLoadingData = true;
        ChangeVisibilityOfLayouts(false);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String body = response.body().string();
                    JSONObject jsonObject = new JSONObject(body);
                    boolean error = jsonObject.getBoolean("error");
                    String message = jsonObject.getString("message");
                    if (!error) {
                        String userKey = jsonObject.getString("user_key");
                        General.SetUserKey(context, userKey);
                        ValidateUserKey();
                    } else {
                        if (!message.isEmpty())
                            General.CreateAlertDialog(context, "", message);
                        isLoadingData = false;
                        ChangeVisibilityOfLayouts(false);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onResponse: " + e.getMessage());
                    General.CreateAlertDialog(context,"","Something went wrong, please try again later.");
                    isLoadingData = false;
                    ChangeVisibilityOfLayouts(false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                General.CreateAlertDialog(context,"","Something went wrong, please check your internet connection.");
                isLoadingData = false;
                ChangeVisibilityOfLayouts(false);
            }
        });
    }

    private void GoToFeedActivity(User user){
        Intent intent = new Intent(context, FeedActivity.class);
        if(user != null) {
            Gson gson = new GsonBuilder().create();
            String jsonStr = gson.toJson(user);
            intent.putExtra("user", jsonStr);

            if(onLoginGoToPrevActivity) {
                setResult(RESULT_OK,intent);
                finish();
                return; //just in case ;)
            }
        }
        startActivity(intent);
        finish();
    }

    private void ChangeVisibilityOfLayouts(boolean switchingLayout){
        if(switchingLayout) {
            int newVisibitily = isLoginState ? View.VISIBLE : View.GONE;
            unameTextInputLayout.setVisibility(newVisibitily);

            if(isLoginState){
                actionBtn.setText("Register");
                titleTV.setText("Register:");
                switchBtn.setText("Already have an account? Login!");
            } else {
                actionBtn.setText("Login");
                titleTV.setText("Login:");
                switchBtn.setText("Don't have an account yet? Register!");
            }

            isLoginState = !isLoginState;
        }
        if(isLoadingData) {
            mainLoginLayout.setVisibility(View.GONE);
            loadingLayout.setVisibility(View.VISIBLE);
        } else {
            mainLoginLayout.setVisibility(View.VISIBLE);
            loadingLayout.setVisibility(View.GONE);
        }
    }


}
