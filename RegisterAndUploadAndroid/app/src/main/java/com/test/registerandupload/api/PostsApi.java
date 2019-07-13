package com.test.registerandupload.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface PostsApi {

    String API_PATH = "posts.php/";

    @FormUrlEncoded
    @POST(API_PATH + "feed")
    Call<ResponseBody> feed(
            @Field("user_key") String userKey,
            @Field("offset") int offset
    );

    @FormUrlEncoded
    @POST(API_PATH + "create_post")
    Call<ResponseBody> createPost(
            @Field("user_key") String userKey,
            @Field("image") String image,
            @Field("description") String description
    );

    @FormUrlEncoded
    @POST(API_PATH + "delete_post")
    Call<ResponseBody> deletePost(
            @Field("user_key") String userKey,
            @Field("post_id") String postId
    );


}
