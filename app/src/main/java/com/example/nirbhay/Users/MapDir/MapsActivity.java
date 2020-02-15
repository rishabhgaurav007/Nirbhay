package com.example.nirbhay.Users.MapDir;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nirbhay.R;
import com.example.nirbhay.Users.CompleteSignUp;
import com.example.nirbhay.Users.CreatePost;
import com.example.nirbhay.Users.GPSTracker;
import com.example.nirbhay.Users.PostActivity;
import com.example.nirbhay.Users.PostDetails;
import com.example.nirbhay.Users.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE_AUTOCOMPLETE_1 = 1;
    private static final int REQUEST_CODE_AUTOCOMPLETE_2 = 2;

    private GoogleMap mMap;
    private TextView back;
    private Button findRoute, safeNow;
    private Marker currentLocationMarker;
    ArrayList markerPoints= new ArrayList();
    GeoJsonLayer layer = null;
    private CarmenFeature home;
    private CarmenFeature work;
    private CarmenFeature sourceCarmen = null;
    private DatabaseReference databaseReference;
    private CarmenFeature destCarmen = null;
    private TextView sourceTextView, destTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        back = (TextView) findViewById(R.id.back);
        findRoute = (Button) findViewById(R.id.findRoute);
        safeNow = (Button) findViewById(R.id.safeNow);

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("Pref", 0);
        if(preferences!=null){
            if(!preferences.getBoolean("safeNow", false)){
                safeNow.setVisibility(View.VISIBLE);
            }
            else{
                safeNow.setVisibility(View.INVISIBLE);
            }
        }
        final FirebaseAuth  firebaseAuth = FirebaseAuth.getInstance();
        safeNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = getApplicationContext().getSharedPreferences("Pref", 0);
                if(preferences!=null){
                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putBoolean("safeNow", true);
                    edit.apply();
                    edit.commit();
                    String pincode = preferences.getString("pincode", null);
                    databaseReference.child("emergency").child(pincode).child(firebaseAuth.getCurrentUser().getUid()).removeValue();
                    safeNow.setVisibility(View.INVISIBLE);
                }

            }
        });
        Mapbox.getInstance(this, getString(R.string.access_token));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        databaseReference = FirebaseDatabase.getInstance().getReference();

        FloatingActionButton sosTrigger = (FloatingActionButton) findViewById(R.id.sos_trigger);
        sosTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng latLng = getCurrentLocation();
                if(latLng!=null){
                    GetPincode getPincode = new GetPincode();
                    String url = getDirectionsUrlPincode(latLng);
                    getPincode.execute(url);
                }
                else{
                    Toast.makeText(MapsActivity.this, "Unable to get the location", Toast.LENGTH_SHORT).show();
                }
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public LatLng getCurrentLocation(){
        GPSTracker gps = new GPSTracker(MapsActivity.this);

        if (gps.canGetLocation()) {
            LatLng latLng = new LatLng(gps.getLatitude(), gps.getLongitude());

            //PanicData.latitude = latLng.latitude;
            Toast.makeText(MapsActivity.this, "current Location" + latLng, Toast.LENGTH_SHORT).show();
            return latLng;
            //PanicData.longitude = latLng.longitude;
        } else {
            gps.showSettingsAlert();
        }
        return null;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initSearchFab();
        /*// Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

    }

    public static BitmapDescriptor generateBitmapDescriptorFromRes(
            Context context, int resId) {
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        drawable.setBounds(
                0,
                0,
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void initSearchFab() {
        findViewById(R.id.fab_location_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LatLng currentLocation = getCurrentLocation();
                if(currentLocation!=null) {
                    currentLocationMarker = mMap.addMarker(new MarkerOptions()
                            .position(currentLocation)
                            .title("Current location")
                            .snippet("Current location")
                            .icon(generateBitmapDescriptorFromRes(MapsActivity.this, R.drawable.current_location))
                    );
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13));
                    // Zoom in, animating the camera.
                    mMap.animateCamera(CameraUpdateFactory.zoomIn());
                    // Zoom out to zoom level 10, animating with a duration of 2 seconds.
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
                }else{
                    Toast.makeText(MapsActivity.this, "Unable to get your location", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.source_textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(MapsActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE_1);
            }
        });

        findViewById(R.id.dest_textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(MapsActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE_2);
            }
        });
        findRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sourceCarmen != null && destCarmen != null) {
                    LatLng sourceLocation = new LatLng(((Point) sourceCarmen.geometry()).latitude(),
                            ((Point) sourceCarmen.geometry()).longitude());
                    LatLng destLocation = new LatLng(((Point) destCarmen.geometry()).latitude(),
                            ((Point) destCarmen.geometry()).longitude());

                    mMap.addMarker(new MarkerOptions().position(sourceLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    mMap.addMarker(new MarkerOptions().position(destLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sourceLocation, 13));
                    // Zoom in, animating the camera.
                    mMap.animateCamera(CameraUpdateFactory.zoomIn());
                    // Zoom out to zoom level 10, animating with a duration of 2 seconds.
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
                    String url = getDirectionsUrlMap(sourceLocation, destLocation);
                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                } else {
                    Toast.makeText(MapsActivity.this, "Please enter source and destination first", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE_1) {

            // Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);
            LatLng currentLocation = new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                    ((Point) selectedCarmenFeature.geometry()).longitude());
            sourceCarmen = selectedCarmenFeature;
            sourceTextView.setText(selectedCarmenFeature.placeName());

        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE_2) {

            // Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);
            LatLng currentLocation = new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                    ((Point) selectedCarmenFeature.geometry()).longitude());
            destCarmen = selectedCarmenFeature;
            destTextView.setText(selectedCarmenFeature.placeName());

        }
    }
    private class DownloadTask extends AsyncTask<String, Void, String> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MapsActivity.this);
            progressDialog.setMessage("Loading");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.hide();
            JSONObject jsonObject = null;
            Log.i("info", result);
            Toast.makeText(MapsActivity.this, "res: " + result, Toast.LENGTH_LONG).show();
            try {
                jsonObject = new JSONObject(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                JSONObject obj = jsonArray.getJSONObject(0);
                JSONObject geometryObject = obj.getJSONObject("geometry");

                //Toast.makeText(MapsActivity.this, "res: " + geometryObject.toString(), Toast.LENGTH_LONG).show();
                layer = new GeoJsonLayer(mMap, geometryObject);
                layer.addLayerToMap();

            } catch (Exception e) {
                e.printStackTrace();
            }
            //Toast.makeText(MapsActivity.this, "result: " + result, Toast.LENGTH_LONG).show();
        }

    }

    private String getDirectionsUrlMap (LatLng origin, LatLng dest){

        // Origin of route
        String str_origin = origin.longitude + "," + origin.latitude;

        // Destination of route
        String str_dest = dest.longitude + "," + dest.latitude;

        String url = "http://router.project-osrm.org/route/v1/driving/" + str_origin + ";" + str_dest + "?steps=true&geometries=geojson&continue_straight=false";

        Log.i("info", url);

        return url;
    }

    private class GetPincode extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog = new ProgressDialog(MapsActivity.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Sending SOS Message!!");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.hide();
            JSONObject jsonObject = null;
            Log.i("info", result);

            try{
                jsonObject = new JSONObject(result);
                int responseCode = jsonObject.getInt("responseCode");
                if(responseCode !=200){
                    Toast.makeText(MapsActivity.this, "Error accessing your location", Toast.LENGTH_SHORT).show();
                }
                else{
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    JSONObject resultObject =jsonArray.getJSONObject(0);
                    final String pincode = resultObject.getString("pincode");
                    Log.i("info", pincode);

                    Toast.makeText(MapsActivity.this, pincode, Toast.LENGTH_SHORT).show();

                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    final String ownerUserId = firebaseUser.getUid();

                    databaseReference.child("users").child(ownerUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            LatLng currLoc = getCurrentLocation();

                            PanicDetails panicDetails = new PanicDetails(
                                    user.getName(),
                                    user.getPhoneNumber(),
                                    currLoc.latitude,
                                    currLoc.longitude,
                                    user.getAddress(),
                                    pincode
                            );
                            final SharedPreferences pref = getApplicationContext().getSharedPreferences("Pref", 0);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putBoolean("safeNow", false);
                            editor.putString("pincode", pincode);
                            editor.apply();
                            editor.commit();

                            final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            if(currentUser!=null)
                            databaseReference.child("emergency").child(pincode).child(currentUser.getUid()).setValue(panicDetails);
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
                            // Setting Dialog Title
                            alertDialog.setTitle("!!!SOS!!!");
                            // Setting Dialog Message
                            alertDialog.setCancelable(false);
                            alertDialog.setMessage("Your details has been sent. Kindly look for safer place nearby");
                            // On pressing Settings button
                            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int which) {
                                    safeNow.setVisibility(View.VISIBLE);

                                    dialog.cancel();
                                }
                            });
                            // on pressing cancel button
                            // Showing Alert Message
                            alertDialog.show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(MapsActivity.this, "Error fetching database", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    private String getDirectionsUrlPincode(LatLng origin) {
        String url = "https://apis.mapmyindia.com/advancedmaps/v1/zdhlcatxe8tt5mr6c9w3wrhrdfbbr8h3/rev_geocode?lat=" +origin.latitude+"&lng="+origin.longitude;
        Log.i("info", url);

        return url;
    }
        private String downloadUrl (String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);

                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.connect();

                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();

                br.close();

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }

    }

