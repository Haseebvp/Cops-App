package com.rapidotask.rapidotask;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.rapidotask.rapidotask.models.Events;
import com.rapidotask.rapidotask.network.ApiCommunication;
import com.rapidotask.rapidotask.network.ApiService;
import com.rapidotask.rapidotask.utils.Constants;
import com.rapidotask.rapidotask.utils.HttpConnection;
import com.rapidotask.rapidotask.utils.NotifUtil;
import com.rapidotask.rapidotask.utils.PathJSONParser;
import com.rapidotask.rapidotask.utils.StorageUtil;
import com.rapidotask.rapidotask.utils.TaskUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, ApiCommunication {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final float DEFAULT_ZOOM = 15;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 100;
    private GoogleMap mMap;
    private Location mLastKnownLocation;
    private LatLng mDefault = new LatLng(12.9279, 77.6271);
    private LocationManager locationManager;
    private MarkerOptions s_markeroptions, d_markeroptions, c_markeroptions;
    private Marker s_marker = null, d_marker = null, c_marker = null;
    private TextView tv_dest, tv_src, tv_plan, tv_cancel;
    private Polyline polyline;
    private ArrayList<LatLng> pathWaypointsList = new ArrayList<>();
    private Boolean isPaused = false;

    private ProgressBar progressBar;
    private LinearLayout lv_triphandle, lv_alertlayout;
    boolean doubleBackToExitPressedOnce = false;
    ArrayList<JSONObject> pointsToServer = null;
    ArrayList<Marker> cop_markers = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        initialiseMarkerOptions();
        bindViews();


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            String msg = intent.getStringExtra("param");
            String eventKey = intent.getStringExtra("key");
            Vote(eventKey, msg);
            NotifUtil.CancelNotif(0, this);
            System.out.println(msg);

        } catch (Exception e) {

        }

        try {
            String msg = intent.getStringExtra("message");
            if (msg.length() > 0) {
                updateEvent(StorageUtil.getInstance(this).getEvents());
                NotifUtil.CancelNotif(0,this);
            }
        } catch (Exception e) {

        }
    }

    private void retainSavedData() {
        if (StorageUtil.getInstance(this).getTripStatus()) {
            s_marker = mMap.addMarker(s_markeroptions);
            s_marker.setPosition(StorageUtil.getInstance(this).getLocation(StorageUtil.SOURCE_LATLONG));

            d_marker = mMap.addMarker(d_markeroptions);
            d_marker.setPosition(StorageUtil.getInstance(this).getLocation(StorageUtil.DESTINATION_LATLONG));

            tv_dest.setText(StorageUtil.getInstance(this).getDestination_name());

            updateUi();
            updateEvent(StorageUtil.getInstance(this).getEvents());
        }
    }

    private void bindViews() {
        tv_dest = (TextView) findViewById(R.id.tv_destination);
        tv_src = (TextView) findViewById(R.id.tv_source);
        tv_plan = (TextView) findViewById(R.id.tv_plan);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        lv_triphandle = (LinearLayout) findViewById(R.id.lv_triphandle);
        lv_alertlayout = (LinearLayout) findViewById(R.id.lv_alertlayout);
        lv_triphandle.setVisibility(View.INVISIBLE);
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        lv_alertlayout.setVisibility(View.INVISIBLE);

        tv_dest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(MainActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });
    }

    private void initialiseMarkerOptions() {
        s_markeroptions = new MarkerOptions().position(mDefault).icon(TaskUtil.bitmapDescriptorFromVector(this, R.drawable.ic_pin_source));
        d_markeroptions = new MarkerOptions().position(mDefault).icon(TaskUtil.bitmapDescriptorFromVector(this, R.drawable.ic_pin_dest));
        c_markeroptions = new MarkerOptions().position(mDefault).icon(TaskUtil.bitmapDescriptorFromVector(this, R.drawable.ic_bike));
    }


    private void updateUi() {

        if (StorageUtil.getInstance(this).getTripStatus()) {
            tv_dest.setEnabled(false);
            tv_plan.setVisibility(View.GONE);
            lv_alertlayout.setVisibility(View.VISIBLE);
        } else {
            if (tv_plan.getVisibility() != View.VISIBLE) {
                tv_plan.setVisibility(View.VISIBLE);
            }
        }

        drawPath();
    }

    private void drawPath() {
        String url = TaskUtil.getDirectionsUrl(this);
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        isPaused = false;
        initMap();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter("Firebase"));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);


    }


    BroadcastReceiver mRegistrationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateEvent(StorageUtil.getInstance(getApplicationContext()).getEvents());
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            locationManager.removeUpdates(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMap() {
        MapFragment mapFragment =
                (MapFragment) getFragmentManager()
                        .findFragmentById(R.id.mapfragment);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            mMap = googleMap;
            if (TaskUtil.permissionCheck(Manifest.permission.ACCESS_COARSE_LOCATION, this) && TaskUtil.permissionCheck(Manifest.permission.ACCESS_FINE_LOCATION, this)) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);

            }
            retainSavedData();
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: ");
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                String placename = place.getName() + ", " + place.getAddress();
                StorageUtil.getInstance(this).putLocation(place.getLatLng(), StorageUtil.DESTINATION_LATLONG);
                StorageUtil.getInstance(this).putDestination(placename, StorageUtil.DESTINATION_NAME);
                tv_dest.setText(StorageUtil.getInstance(this).getDestination_name());
                if (d_marker == null) {
                    d_marker = mMap.addMarker(d_markeroptions);
                    d_marker.setPosition(place.getLatLng());
                } else {
                    d_marker.setPosition(place.getLatLng());
                }
                updateUi();

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
//                Status status = PlaceAutocomplete.getStatus(this, data);
//                // TODO: Handle the error.
//                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }


//    Location callbacks

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: " + location);
        mLastKnownLocation = location;
        LatLng latLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        if (!isPaused) {
            progressBar.setVisibility(View.GONE);
            lv_triphandle.setVisibility(View.VISIBLE);
            if (s_marker == null) {
                s_marker = mMap.addMarker(s_markeroptions);
                s_marker.setPosition(latLng);
                StorageUtil.getInstance(this).putLocation(latLng, StorageUtil.SOURCE_LATLONG);
            } else {
                if (StorageUtil.getInstance(this).getTripStatus()) {
                    if (c_marker == null) {
                        c_marker = mMap.addMarker(c_markeroptions);
                        c_marker.setPosition(latLng);
                    } else {
                        c_marker.setPosition(latLng);
                        checkpoints();
                    }
                    StorageUtil.getInstance(this).putLocation(latLng, StorageUtil.CURRENT_LATLONG);
                } else {
                    s_marker.setPosition(latLng);
                    StorageUtil.getInstance(this).putLocation(latLng, StorageUtil.SOURCE_LATLONG);
                }
            }
            if (d_marker == null) {
                AnimateCamera(latLng);
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    private void AnimateCamera(LatLng position) {
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                position, DEFAULT_ZOOM);
        mMap.animateCamera(location);
    }

    public void StartTrip(View view) {
        if (!StorageUtil.getInstance(this).getTripStatus()) {
            tv_dest.setEnabled(false);
            tv_plan.setVisibility(View.GONE);
            lv_alertlayout.setVisibility(View.VISIBLE);
            StorageUtil.getInstance(this).putTripStatus(true);
            registerTrip(pointsToServer);
        }

    }

    public void CancelTrip(View view) {
        if (StorageUtil.getInstance(this).getTripStatus()) {
            tv_dest.setEnabled(true);
            lv_alertlayout.setVisibility(View.INVISIBLE);
            StorageUtil.getInstance(this).putTripStatus(false);
            StorageUtil.getInstance(this).putDestination("", StorageUtil.DESTINATION_NAME);
            tv_dest.setText("Where to");
            s_marker.remove();
            d_marker.remove();
            if (c_marker != null)
                c_marker.remove();
            s_marker = null;
            d_marker = null;
            if (cop_markers.size() > 0) {
                for (Marker m : cop_markers) {
                    m.remove();
                }
            }
            if (polyline != null)
                polyline.remove();
        }
    }


    private void updateEvent(List<Events> events) {
        Log.d(TAG, "updateEvent: " + events);
        if (cop_markers.size() > 0) {
            for (Marker m : cop_markers) {
                m.remove();
            }
        }

        if (events != null) {
            for (int i = 0; i < events.size(); i++) {
                Events item = events.get(i);
                MarkerOptions temp = new MarkerOptions().
                        position(item.getLocation())
                        .title("Cops Alert")
                        .snippet(item.getPercentage() + " percent chance of presence of cops here.")
                        .icon(TaskUtil.bitmapDescriptorFromVector(this, R.drawable.ic_siren));

                Marker tempMarker = mMap.addMarker(temp);
                cop_markers.add(tempMarker);
            }
        }
    }

    public void CopsAlert(View view) {
        JSONObject data = new JSONObject();
        try {
            if (StorageUtil.getInstance(this).getLocation(StorageUtil.CURRENT_LATLONG) != null) {
                data.put("latitude", StorageUtil.getInstance(this).getLocation(StorageUtil.CURRENT_LATLONG).latitude);
                data.put("longitude", StorageUtil.getInstance(this).getLocation(StorageUtil.CURRENT_LATLONG).longitude);
            }
            else {
                data.put("latitude", StorageUtil.getInstance(this).getLocation(StorageUtil.SOURCE_LATLONG).latitude);
                data.put("longitude", StorageUtil.getInstance(this).getLocation(StorageUtil.SOURCE_LATLONG).longitude);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiService.getInstance(this).post(this, Constants.BASE_URL + "create/event/", data, "CREATEEVENT");
    }


    public void Vote(String key, String param) {
        JSONObject data = new JSONObject();
        try {
            data.put("eventKey", key);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (param.equals("upvote")) {
            ApiService.getInstance(this).post(this, Constants.BASE_URL + "upvote/event/", data, "UPVOTE");
        } else {
            ApiService.getInstance(this).post(this, Constants.BASE_URL + "downvote/event/", data, "DOWNVOTE");
        }
    }


//    Async tasks
//    ------------------------------------

    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }


    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;
            if (polyline != null)
                polyline.remove();

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                pointsToServer = new ArrayList<JSONObject>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    JSONObject toServer = new JSONObject();
                    try {
                        toServer.put("latitude", lat);
                        toServer.put("longitude", lng);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    points.add(position);
                    pointsToServer.add(toServer);
                }

                polyLineOptions.addAll(points);
                pathWaypointsList.addAll(points);
                polyLineOptions.width(10);
                polyLineOptions.color(Color.BLACK);

            }

            if (polyLineOptions != null) {
                polyline = mMap.addPolyline(polyLineOptions);
                AnimatePath();
            } else {
                drawPath();
            }

        }
    }

    private void AnimatePath() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(StorageUtil.getInstance(this).getLocation(StorageUtil.SOURCE_LATLONG));
        builder.include(StorageUtil.getInstance(this).getLocation(StorageUtil.DESTINATION_LATLONG));
        LatLngBounds bounds = builder.build();
        int padding = 200; // padding around start and end marker
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);

    }

    private void checkpoints() {
        Log.d(TAG, "checkpoints: ");
        LatLng current = StorageUtil.getInstance(this).getLocation(StorageUtil.SOURCE_LATLONG);
        List<Events> tempArray = StorageUtil.getInstance(this).getEvents();
        if (tempArray != null) {
            for (int i = 0; i < tempArray.size(); i++) {
                if (tempArray.get(i).getLocation().latitude == current.latitude &&
                        tempArray.get(i).getLocation().longitude == current.longitude) {
                    NotifUtil.NotifyForVote(tempArray.get(i).getEventKey(), this);
                    tempArray.remove(i);
                    StorageUtil.getInstance(this).putEvents(tempArray);
                    break;
                }
            }
        }
        updateEvent(StorageUtil.getInstance(this).getEvents());

    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


//    API CALLS
//    ---------------------------------------------------------

    public void registerTrip(ArrayList<JSONObject> pointsToServer) {
        Log.d(TAG, "registerTrip: " + StorageUtil.getInstance(this).getSession());
        JSONObject data = new JSONObject();
        try {
            data.put("path", pointsToServer);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiService.getInstance(this).post(this, Constants.BASE_URL + "start/trip/", data, "STARTTRIP");
    }


    private void CheckEvent() {
        JSONObject data = new JSONObject();
        try {
            data.put("tripKey", StorageUtil.getInstance(this).getTripId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiService.getInstance(this).post(this, Constants.BASE_URL + "check/event/", data, "CHECKTRIP");
    }


    private void storeEvents(JSONArray events) {
        List<Events> eventlist = new ArrayList<>();
        for (int i = 0; i < events.length(); i++) {
            Events event = new Events();
            try {
                event.setDownvote(events.getJSONObject(i).getInt("downvote"));
                event.setUpvote(events.getJSONObject(i).getInt("upvote"));
                event.setPercentage(events.getJSONObject(i).getInt("percentage"));
                event.setEventKey(events.getJSONObject(i).getString("eventKey"));
                event.setLocation(new LatLng(events.getJSONObject(i).getJSONArray("location").getDouble(0), events.getJSONObject(i).getJSONArray("location").getDouble(1)));
                eventlist.add(event);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        StorageUtil.getInstance(this).putEvents(eventlist);
        updateEvent(eventlist);
    }


    @Override
    public void onSuccess(JSONObject object, String flag) {
        Log.d(TAG, "onSuccess: " + object);
        if (flag.equals("STARTTRIP")) {
            String status = object.optString("status");
            if (status.equals("OK")) {
                String token = object.optString("tripKey");
                StorageUtil.getInstance(this).putTripid(token);
                CheckEvent();
            } else {
                Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
            }
        } else if (flag.equals("CHECKTRIP")) {
            String status = object.optString("status");
            if (status.equals("OK")) {
                JSONArray events = object.optJSONArray("events");
                if (events.length() > 0) {
                    storeEvents(events);

                }
            } else {
                Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
            }
        } else if (flag.equals("UPVOTE")) {
            String status = object.optString("status");
            if (status.equals("OK")) {
                Toast.makeText(getApplicationContext(), "Thanks, Your vote recorded...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();

            }
        } else if (flag.equals("DOWNVOTE")) {
            String status = object.optString("status");
            if (status.equals("OK")) {
                Toast.makeText(getApplicationContext(), "Thanks, Your vote recorded...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();

            }
        } else if (flag.equals("CREATEEVENT")) {
            String status = object.optString("status");
            if (status.equals("OK")) {
                Toast.makeText(getApplicationContext(), "Cops alert recorded...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();

            }
        }
    }


    @Override
    public void onError(VolleyError e, String flag) {
        Log.e(TAG, "onError: ", e);
        Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
    }


}
