package com.test.registerandupload.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface UsersApi {

    /**
     * ENDPOINTS FOR THE USERS API
     * */

    String API_PATH = "users.php/";

    @FormUrlEncoded
    @POST(API_PATH + "register")
    Call<ResponseBody> register(
            @Field("username") String username,
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST(API_PATH + "login")
    Call<ResponseBody> login(
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST(API_PATH + "validate_user_key")
    Call<ResponseBody> validateUserKey(
            @Field("user_key") String userKey
    );

}
