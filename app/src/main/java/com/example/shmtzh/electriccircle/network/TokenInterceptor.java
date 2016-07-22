package com.example.shmtzh.electriccircle.network;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by shmtzh on 5/27/16.
 */
public class TokenInterceptor implements Interceptor {
    public static String mToken = null;

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {

        Request originalRequest = chain.request();
        if (mToken == null || !originalRequest.url().host().contains("rolr.net")) {
            return chain.proceed(originalRequest);
        } else {
            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + mToken)
                    .build();

            Log.w("TokenInterceptor", "Calling: " + newRequest.method() + " :: " + newRequest.url().url().toString());
            return chain.proceed(newRequest);
        }
    }

    public void clear() {
        mToken = null;
    }
}
