package com.test.registerandupload.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.test.registerandupload.R;
import com.test.registerandupload.Utilities.General;
import com.test.registerandupload.activity.FeedActivity;
import com.test.registerandupload.api.RetrofitClient;
import com.test.registerandupload.model.Post;

import org.json.JSONObject;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedAdapter extends ArrayAdapter<Post> {

    private Context context;
    private int resource;
    private List<Post> posts;
    private Picasso picasso;

    @Nullable
    @Override
    public Post getItem(int position) {
        return posts.get(position);
    }

    public FeedAdapter(Context context, int resource, List<Post> posts) {
        super(context, resource, posts);
        this.context = context;
        this.resource = resource;
        this.posts = posts;
        this.picasso = Picasso.get();
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final Post post = getItem(position);

        ViewHolder vh;

        //initializing the view if there's no cached view
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);

            vh = new ViewHolder();
            vh.usernameTV = convertView.findViewById(R.id.username_tv);
            vh.timeAgoTV = convertView.findViewById(R.id.time_ago_tv);
            vh.descTV = convertView.findViewById(R.id.desc_tv);
            vh.deleteBtn = convertView.findViewById(R.id.delete_btn);
            vh.postImage = convertView.findViewById(R.id.post_image);
            vh.imageProgressBar = convertView.findViewById(R.id.image_progress_bar);

            convertView.setTag(vh);
        } else { //if there's cached view - getting it
            vh = (ViewHolder) convertView.getTag();
        }

        // setting the texts from the current post object
        vh.usernameTV.setText(Html.fromHtml(post.getCreatorUsername()));
        vh.timeAgoTV.setText(post.getCreationTime());
        vh.descTV.setVisibility(post.getDescription().isEmpty() ? View.GONE : View.VISIBLE);
        vh.descTV.setText(Html.fromHtml(post.getDescription()));

        // setting the click listener for deleting the post
        vh.deleteBtn.setVisibility(post.didUserCreatePost() ? View.VISIBLE : View.GONE);
        vh.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenDeleteAlert(post.getPostId(), position);
            }
        });

        //loading the current post's image with the picasso library
        final ViewHolder finalVH = vh;
        if(!post.getImageUrl().isEmpty()) {
            picasso.load(post.getImageUrl()).error(R.drawable.ic_broken_image_black_64dp)
                    .into(vh.postImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            finalVH.imageProgressBar.setVisibility(View.GONE);
                            finalVH.postImage.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {
                            finalVH.imageProgressBar.setVisibility(View.GONE);
                            finalVH.postImage.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            finalVH.imageProgressBar.setVisibility(View.GONE);
            finalVH.postImage.setVisibility(View.GONE);
        }

        return convertView;
    }

    static class ViewHolder{
        TextView usernameTV, timeAgoTV, descTV;
        ImageButton deleteBtn;
        ImageView postImage;
        ProgressBar imageProgressBar;
    }

    //opening the alert dialog for when the user tries to delete the current post
    private void OpenDeleteAlert(final String postId, final int position){
        new AlertDialog.Builder(context)
                .setTitle("Are you sure?")
                .setMessage("Deleting this post will delete any and all info related to it, such as its image and description.")
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DeletePost(postId, position);
                    }
                })
                .setPositiveButton("Cancel",null)
                .show();
    }

    //performing the delete action of the current post
    private void DeletePost(String postId, final int position){
        final ProgressDialog deleteDialog = ProgressDialog.show(context, "Deleting post","Please Wait...", true);

        Call<ResponseBody> call = RetrofitClient.getInstance()
                .getPostsApi()
                .deletePost(General.GetUserKey(context), postId);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    boolean error = jsonObject.getBoolean("error");
                    String message = jsonObject.getString("message");
                    if(!error){
                        posts.remove(position);
                        notifyDataSetChanged();
                    }
                    if(!message.isEmpty())
                        General.CreateAlertDialog(context,"", message);
                    deleteDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                    General.CreateAlertDialog(context,"An Error Occurred","Something went wrong, please try again later.");
                    deleteDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                General.CreateAlertDialog(context,"An Error Occurred","Something went wrong, please check your internet connection.");
                deleteDialog.dismiss();
            }
        });
    }

}
