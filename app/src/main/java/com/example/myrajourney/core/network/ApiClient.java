package com.example.myrajourney.core.network;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.myrajourney.BuildConfig;

public class ApiClient {
    private static final String TAG = "ApiClient";

    // ✅ UPDATED: Pointing to your PC's Wi-Fi IP on Port 8000
    private static final String DEFAULT_LOCAL_URL = "http://10.58.163.149:8000/api/v1/";

    private static volatile Retrofit retrofit = null;
    private static volatile ApiService apiService = null;

    public static String getBaseUrl() {
        // Check if a custom URL is set in local.properties/gradle, otherwise use the IP above
        try {
            String gradleUrl = BuildConfig.API_BASE_URL;
            if (gradleUrl != null && !gradleUrl.isEmpty() && !gradleUrl.equals("null")) {
                Log.d(TAG, "Using Gradle/Configured URL: " + gradleUrl);
                return gradleUrl;
            }
        } catch (Exception e) {
            Log.w(TAG, "BuildConfig.API_BASE_URL not found, falling back to default.");
        }

        Log.d(TAG, "Using Physical Device IP: " + DEFAULT_LOCAL_URL);
        return DEFAULT_LOCAL_URL;
    }

    public static Retrofit getRetrofit(Context context) {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    Gson gson = new GsonBuilder().setLenient().create();

                    // Logging helps debug connection issues
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
                    if (context != null) {
                        httpClient.addInterceptor(new AuthInterceptor(context));
                    }
                    httpClient.addInterceptor(loggingInterceptor);

                    // Longer timeouts for mobile networks
                    httpClient.connectTimeout(60, TimeUnit.SECONDS);
                    httpClient.readTimeout(60, TimeUnit.SECONDS);
                    httpClient.writeTimeout(60, TimeUnit.SECONDS);

                    retrofit = new Retrofit.Builder()
                            .baseUrl(getBaseUrl())
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .client(httpClient.build())
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static ApiService getApiService(Context context) {
        if (apiService == null) {
            synchronized (ApiClient.class) {
                if (apiService == null) {
                    apiService = getRetrofit(context).create(ApiService.class);
                }
            }
        }
        return apiService;
    }
}