package com.example.shmtzh.electriccircle;

import android.app.Application;
import android.os.SystemClock;

import com.example.shmtzh.electriccircle.network.NetworkManager;

import java.util.concurrent.TimeUnit;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/*
 * Created by shmtzh on 5/28/16.
 */
public class EC extends Application {

    @Override

    public void onCreate() {
        super.onCreate();
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2));

        NetworkManager.create(getApplicationContext());
        initFonts();

    }

    private void initFonts() {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Lato-Medium.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }


}
