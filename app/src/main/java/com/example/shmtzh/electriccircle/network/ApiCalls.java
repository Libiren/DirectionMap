package com.example.shmtzh.electriccircle.network;

import com.example.shmtzh.electriccircle.model.Waypoint;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by shmtzh on 5/28/16.
 */
public interface ApiCalls {
    @GET("https://maps.googleapis.com/maps/api/directions/json")
    Call<Waypoint> getProfileFeed(@Query("origin") String origin, @Query("destination") String destination, @Query("key") String key);
}
