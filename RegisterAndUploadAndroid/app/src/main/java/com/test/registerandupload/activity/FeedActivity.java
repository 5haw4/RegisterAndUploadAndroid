package com.test.registerandupload.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.test.registerandupload.R;
import com.test.registerandupload.Utilities.General;
import com.test.registerandupload.adapter.FeedAdapter;
import com.test.registerandupload.api.RetrofitClient;
import com.test.registerandupload.model.Post;
import com.test.registerandupload.model.User;
import com.test.registerandupload.ui.SwipeToRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.test.registerandupload.Utilities.Constants.REFRESH_REQUEST_CODE;
import static com.test.registerandupload.Utilities.Constants.UPDATE_USER_OBJECT;

public class FeedActivity extends AppCompatActivity {

    private static final String TAG = FeedAdapter.class.getSimpleName();

    Context context;
    User user;

    SwipeToRefreshLayout swipeLayout;
    TextView messageTV;
    String messageStr = "";
    LinearLayout loadingLayout;
    ListView listView;

    FeedAdapter adapter;

    boolean isLoadingData = false;
    boolean didReachMax = false; //can the api give more items
    int offset = 0; //offset of feed
    int itemCount = 10; //item count of each page in the pagination of the api

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        context = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Feed");

        // Getting the user info from the bundle of the intent that opened this activity
        if(getIntent() != null && getIntent().getExtras() != null) {
            Gson gson = new GsonBuilder().create();
            user = gson.fromJson(getIntent().getExtras().getString("user"), User.class);
        }

        // finding the views from the activity's XML file
        swipeLayout = findViewById(R.id.swipe_layout);
        messageTV = findViewById(R.id.message_tv);
        loadingLayout = findViewById(R.id.loading_layout);
        listView = findViewById(R.id.list_view);

        /*
            setting scroll listener to detect when the user reaches the bottom of the feed list in
            order to load more content for the user to interact with
        */
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView lw, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(didReachMax)
                    return;

                if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0) {
                    if(!isLoadingData){
                        LoadFeed(false);
                    }
                }
            }
        });

        // setting the refresh action for the "swipe to refresh" library
        swipeLayout.setListViewChild(listView);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LoadFeed(true);
            }
        });

        //loading the first few posts of the feed
        LoadFeed(true);
    }

    /*
        handeling the callback intents that indicate if the feed should be refreshed,
        and if the user info has been updated
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK || data == null || data.getExtras() == null) return;

        switch (requestCode) {
            case REFRESH_REQUEST_CODE:
                if(data.getExtras().getBoolean("refresh_feed"))
                    LoadFeed(true);
                break;
            case UPDATE_USER_OBJECT:
                if(!data.getExtras().getString("user","").isEmpty()) {
                    Gson gson = new GsonBuilder().create();
                    user = gson.fromJson(data.getExtras().getString("user"),User.class);
                    LoadFeed(true);
                }
                break;
        }
    }

    /* inflating the custom options menu for the toolbar */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = ((Activity) context).getMenuInflater();
        inflater.inflate(R.menu.add_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* handeling the toolbar "create new post" button */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.create_post_btn) {
            GoToCreatePostActivity();
            return true;
        }
        return false;
    }


    /*
        - loading the next few posts of the feed
        - if "isRefreshing" is true reseting the offset back to 0 to start and load the posts from the start
    */
    public void LoadFeed(final boolean isRefreshing){
        if(isLoadingData)
            return;
        isLoadingData = true;

        if(isRefreshing) {
            if(adapter != null)
                adapter.clear();
            didReachMax = false;
            offset = 0;
            ChangeVisibilityOfLayouts(0, true);
        }
        else {
            offset += itemCount;
        }

        Call<ResponseBody> call = RetrofitClient.getInstance()
                .getPostsApi()
                .feed(General.GetUserKey(context), offset);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String body = response.body().string();
                    JSONObject jsonObject = new JSONObject(body);
                    boolean error = jsonObject.getBoolean("error");
                    boolean isLoggedIn = jsonObject.getBoolean("isLoggedIn");
                    String type = jsonObject.getString("type");
                    String message = jsonObject.getString("message");

                    if(!isLoggedIn) { //for when the user logs in from a different phone while the app is open in this one
                        user = null;
                    } else {
                        JSONObject userJson = jsonObject.getJSONObject("user");
                        user = new User(
                                userJson.getString("user_id"),
                                userJson.getString("username"),
                                userJson.getString("email")
                        );
                    }
                    if(!error){
                        JSONArray postsJson = jsonObject.getJSONArray("posts");
                        List posts = new ArrayList();
                        for (int i = 0; i < postsJson.length(); i++) {
                            JSONObject curPost = postsJson.getJSONObject(i);
                            posts.add(new Post(
                                    curPost.getString("post_id"),
                                    curPost.getString("image"),
                                    curPost.getString("description"),
                                    curPost.getString("creation_time"),
                                    curPost.getString("created_by_uid"),
                                    curPost.getString("username"),
                                    curPost.getBoolean("did_user_create_post")
                            ));
                        }
                        if(adapter == null || isRefreshing) {
                            adapter = new FeedAdapter(context, R.layout.adapter_post, posts);
                            listView.setAdapter(adapter);
                        } else {
                            adapter.addAll(posts);
                        }
                    }
                    if(type.equals("NO_POSTS")){
                        didReachMax = true;
                    }
                    if(!message.equals(""))
                        messageStr = message;

                    ChangeVisibilityOfLayouts(listView == null ? 0 : listView.getCount(), false);
                } catch (Exception e) {
                    Log.e(TAG, "onResponse: " + e.getMessage());
                    messageStr = "Something went wrong, please try again later.";
                    ChangeVisibilityOfLayouts(listView == null ? 0 : listView.getCount(), false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                messageStr = "Something went wrong, please check your internet connection.";
                ChangeVisibilityOfLayouts(listView == null ? 0 : listView.getCount(), false);
            }
        });

    }


    /**
     * - If loading and there are no items in the list view ->
     *          hiding the list view and showing the loading UI
     *
     * - If not loading and there are items in the list view ->
     *          showing the list view and hiding the loading UI
     *
     * - If not loading and there are no items in the list view (= an error occurred)->
     *          showing error in 'messageTV' and hiding both the list view and the loading UI
     */
    private void ChangeVisibilityOfLayouts(int listSize, boolean isRefreshing){
        if(isRefreshing) {
            messageTV.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
            loadingLayout.setVisibility(View.VISIBLE);
            return;
        }

        swipeLayout.setRefreshing(false);
        isLoadingData = false;

        loadingLayout.setVisibility(View.GONE);

        if(listSize>0){
            messageTV.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }else {
            messageTV.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
        if(messageStr.equals(""))
            messageTV.setText("Unknown error has occurred.");
        else
            messageTV.setText(messageStr);
    }


    /*
        opening the "create post" activityt and passing it the user info object,
        if the user isn't logged in, preventing them from going to the "create post" activity
        and promping them to register / login with a button that can direct thme to the login screen
    */
    private void GoToCreatePostActivity() {
        if (user == null) {
            new AlertDialog.Builder(context)
                    .setTitle("You are not logged in")
                    .setMessage("To create posts you must be logged in!")
                    .setPositiveButton("Login / Register", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.putExtra("show_back_arrow", true);
                            startActivityForResult(intent, UPDATE_USER_OBJECT);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            Intent intent = new Intent(context, CreatePostActivity.class);
            Gson gson = new GsonBuilder().create();
            String jsonStr = gson.toJson(user);
            intent.putExtra("user", jsonStr);
            startActivityForResult(intent, REFRESH_REQUEST_CODE);
        }
    }

}
