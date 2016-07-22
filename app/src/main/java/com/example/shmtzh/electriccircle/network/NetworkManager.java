package com.example.shmtzh.electriccircle.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.shmtzh.electriccircle.BuildConfig;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;

import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by shmtzh on 5/27/16.
 */
public class NetworkManager {

    private static final String TAG = "WebApiManager";
    private static NetworkManager sInstance;
    private CookieManager cookieManager;
    private TokenInterceptor mTokenInterceptor;
    private final Context mContext;
    private ApiCalls mApiCalls;
    private OkHttpClient okHttpClient;
    private Handler mHandler;

    private NetworkManager(Context context) {
        mTokenInterceptor = new TokenInterceptor();
        mContext = context;
        initApi();
    }

    public static void create(Context context) {
        if (sInstance != null) {
            throw new IllegalStateException("Already created!");
        }
        sInstance = new NetworkManager(context);
    }

    public static synchronized NetworkManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Call create() first");
        }
        return sInstance;
    }

    private void initApi() {
        final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Response response = chain.proceed(request);

                boolean showToast = (response.code() == 400);
                if (showToast) {
                    String txt = "intercept: error in request" + request.method() + " " + request.url(); //+ " response: " + response.body().string();
                    Log.e(TAG, txt);
                    Message message = mHandler.obtainMessage(0, txt);
                    message.sendToTarget();
                }
                return response;
            }
        };
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        okHttpClient = new OkHttpClient().newBuilder()
                .followRedirects(true)
                .followSslRedirects(true)
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .addInterceptor(logging)
                .addInterceptor(interceptor)
                .addNetworkInterceptor(mTokenInterceptor)
                .build();


        Retrofit apiRestAdapter = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_END_POINT)
                .client(okHttpClient)
                .addConverterFactory(new ToStringConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mApiCalls = apiRestAdapter.create(ApiCalls.class);

        initLooper();
    }

    private void initLooper() {
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                Log.d(TAG, "handleMessage: ");
                Toast.makeText(mContext, message.obj.toString(), Toast.LENGTH_SHORT).show();
            }
        };

    }


    public void clearCookies() {
        mTokenInterceptor.clear();
        cookieManager.getCookieStore().removeAll();
        initApi();
    }

    public ApiCalls getApiCalls() {
        return mApiCalls;
    }

}
