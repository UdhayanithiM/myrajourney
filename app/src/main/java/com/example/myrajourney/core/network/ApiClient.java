package com.example.myrajourney.core.network;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String TAG = "ApiClient";

    // 1. Emulator URL (Standard)
    private static final String BASE_URL_EMULATOR = "http://10.0.2.2/backend/public/api/v1/";

    // 2. Physical Device URL (UPDATE THIS when your IP changes)
    // Run 'ipconfig' (Windows) or 'ifconfig' (Mac/Linux) to find your PC's IP
    private static final String BASE_URL_PHYSICAL = "http://192.168.29.162/backend/public/api/v1/";

    private static volatile Retrofit retrofit = null;
    private static volatile ApiService apiService = null;

    // Auto-detect emulator
    private static boolean isEmulator() {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || "google_sdk".equals(Build.PRODUCT);
    }

    public static String getBaseUrl() {
        if (isEmulator()) {
            Log.d(TAG, "Device is Emulator. Using: " + BASE_URL_EMULATOR);
            return BASE_URL_EMULATOR;
        }
        Log.d(TAG, "Device is Physical. Using: " + BASE_URL_PHYSICAL);
        return BASE_URL_PHYSICAL;
    }

    public static Retrofit getRetrofit(Context context) {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();

                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

                    // Add Auth Interceptor
                    if (context != null) {
                        httpClient.addInterceptor(new AuthInterceptor(context));
                    }

                    httpClient.addInterceptor(loggingInterceptor);

                    // Timeouts (60s is safer for slow PHP backends)
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






