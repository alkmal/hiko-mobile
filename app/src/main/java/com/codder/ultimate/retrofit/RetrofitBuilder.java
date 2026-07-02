package com.codder.ultimate.retrofit;

import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.worker.uploadingprogress.CountingRequestBody;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitBuilder {


    public static RetrofitService create() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder client = new OkHttpClient.Builder().addInterceptor(interceptor);
        client.connectTimeout(100, TimeUnit.SECONDS);
        client.writeTimeout(30, TimeUnit.SECONDS);
        client.readTimeout(100, TimeUnit.SECONDS).retryOnConnectionFailure(true).build();
        client.addInterceptor(chain -> {
            Request request = chain.request().newBuilder().addHeader("key", Const.KEY).build();
            return chain.proceed(request);
        });


        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(RetrofitService.class);

    }

    public static RetrofitService createUploadFile(CountingRequestBody.Listener progressListener) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(interceptor);
        client.connectTimeout(100, TimeUnit.SECONDS);
        client.readTimeout(100, TimeUnit.SECONDS);

        client.addNetworkInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();

                if (originalRequest.body() == null) {
                    return chain.proceed(originalRequest);
                }

                Request progressRequest = originalRequest.newBuilder()
                        .method(originalRequest.method(),
                                new CountingRequestBody(originalRequest.body(), progressListener))
                        .build();

                return chain.proceed(progressRequest);

            }
        });

        client.addInterceptor(chain -> {
            Request request = chain.request().newBuilder().addHeader("key", Const.KEY).build();
            return chain.proceed(request);
        });

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(RetrofitService.class);
    }

    public static RetrofitService getIp() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();


        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        return new Retrofit.Builder()
                .baseUrl("http://ip-api.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(RetrofitService.class);


    }

    public static RetrofitService createStoryUploadFile(CountingRequestBody.Listener progressListener) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(interceptor);
        client.connectTimeout(100, TimeUnit.SECONDS);
        client.readTimeout(100, TimeUnit.SECONDS);

        client.addNetworkInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();

                if (originalRequest.body() == null) {
                    return chain.proceed(originalRequest);
                }
                Request progressRequest = originalRequest.newBuilder()
                        .method(originalRequest.method(),
                                new CountingRequestBody(originalRequest.body(), progressListener))
                        .build();

                return chain.proceed(progressRequest);
            }
        });

        client.addInterceptor(chain -> {
            Request request = chain.request().newBuilder().addHeader("key", Const.KEY).build();
            return chain.proceed(request);
        });

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(RetrofitService.class);
    }

    public static RetrofitService getLocation() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        return new Retrofit.Builder()
                .baseUrl("http://api.positionstack.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(RetrofitService.class);


    }
}
