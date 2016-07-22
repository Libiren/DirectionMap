package com.example.shmtzh.electriccircle.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.shmtzh.electriccircle.R;
import com.example.shmtzh.electriccircle.model.Leg;
import com.example.shmtzh.electriccircle.model.StartLocation_;
import com.example.shmtzh.electriccircle.model.Step;
import com.example.shmtzh.electriccircle.model.Waypoint;
import com.example.shmtzh.electriccircle.network.ApiCalls;
import com.example.shmtzh.electriccircle.network.NetworkManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends BaseActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Button mButtonFindPath;
    private EditText mEditTextOrigin;
    private EditText mEditTextDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mButtonFindPath = (Button) findViewById(R.id.buttonPath);
        mEditTextOrigin = (EditText) findViewById(R.id.editTextOrigin);
        mEditTextDestination = (EditText) findViewById(R.id.editTextDestination);
        mButtonFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findPath();
            }
        });

        String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (locationProviders == null || locationProviders.equals("")) {

            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle("Warning");
            adb.setMessage("Your geolocation is not included");
            adb.setIcon(android.R.drawable.ic_dialog_info);
            adb.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });

            adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            adb.show();
        }
    }

    private void findPath() {
        String origin = mEditTextOrigin.getText().toString();
        String destination = mEditTextDestination.getText().toString();
        origin.replaceAll(" ", "+");
        destination.replaceAll(" ", "+");
        origin.toUpperCase();
        destination.toUpperCase();

        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiCalls mApicalls = NetworkManager.getInstance().getApiCalls();
        Call<Waypoint> call = mApicalls.getProfileFeed(origin, destination, "AIzaSyAPfYisMShEQ4M1DISHUZZju3GwYmLfLGs");
        try {
            call.enqueue(new Callback<Waypoint>() {
                @Override
                public void onResponse(Call<Waypoint> call, Response<Waypoint> response) {
                    mMap.clear();
                try {
                    List<Leg> leg = response.body().getRoutes().get(0).getLegs();

                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(leg.get(0).getStartLocation().getLat(), leg.get(0).getStartLocation().getLng()))

                    );

                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(leg.get(0).getEndLocation().getLat(), leg.get(0).getEndLocation().getLng()))
                    );

                    Waypoint waypoint = response.body();
                    ArrayList<LatLng> routelist = new ArrayList<>();
                    if (waypoint.getRoutes().size() > 0) {
                        ArrayList<LatLng> decodelist;
                        com.example.shmtzh.electriccircle.model.Route routeA = waypoint.getRoutes().get(0);

                        if (routeA.getLegs().size() > 0) {
                            List<Step> steps = routeA.getLegs().get(0).getSteps();
                            Step step;
                            StartLocation_ location;
                            String polyline;
                            for (int i = 0; i < steps.size(); i++) {
                                step = steps.get(i);
                                location = step.getStartLocation();
                                routelist.add(new LatLng(location.getLat(), location.getLng()));
                                polyline = step.getPolyline().getPoints();
                                decodelist = RouteDecode.decodePoly(polyline);
                                routelist.addAll(decodelist);
                                location = step.getEndLocation();
                                routelist.add(new LatLng(location.getLat(), location.getLng()));
                            }
                        }
                    }

                    if (routelist.size() > 0) {
                        PolylineOptions rectLine = new PolylineOptions().width(10).color(
                                Color.RED);

                        for (int i = 0; i < routelist.size(); i++) {
                            rectLine.add(routelist.get(i));
                        }

                        mMap.addPolyline(rectLine);
                    }
                }
                catch (IndexOutOfBoundsException e)
                {
                    Toast.makeText(getApplicationContext(), "  This route does not exist  ", Toast.LENGTH_LONG).show();
                }
                }

                @Override
                public void onFailure(Call<Waypoint> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "  0  ", Toast.LENGTH_LONG).show();
                }
            });
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(getApplicationContext(), "  1  ", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        LatLng cityGarden = new LatLng(46.485141, 30.734745);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(cityGarden));
    }
}
