package com.test.registerandupload.api;

import android.util.Base64;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String uName = "<ENTER_YOUR_BASIC_AUTH_USERNAME_HERE>";
    private static final String pass = "<ENTER_YOUR_BASIC_AUTH_PASSWORD_HERE>";
    private static final String AUTH = "Basic " + Base64.encodeToString((uName + ":" + pass).getBytes(), Base64.NO_WRAP);

    public static final String BASE_URL = "http://YOU_DOMAIN.com/";
    private static final String BASE_API_URL = BASE_URL + "api/";

    private static RetrofitClient Instance;
    private Retrofit retrofit;

    //handeling the initilizition of the retrofit object
    private RetrofitClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(
                        new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                Request original = chain.request();

                                Request.Builder builder = original.newBuilder()
                                        .addHeader("Authorization", AUTH)
                                        .method(original.method(), original.body());

                                Request request = builder.build();
                                return chain.proceed(request);
                            }
                        }
                ).build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (Instance == null) {
            Instance = new RetrofitClient();
        }
        return Instance;
    }


    public PostsApi getPostsApi() {
        return retrofit.create(PostsApi.class);
    }

    public UsersApi getUserApi() {
        return retrofit.create(UsersApi.class);
    }

}