package com.example.nirbhay.Police;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nirbhay.MainActivity;
import com.example.nirbhay.R;
import com.example.nirbhay.Users.GPSTracker;
import com.example.nirbhay.Users.MapDir.MapsActivity;
import com.example.nirbhay.Users.MapDir.PanicDetails;
import com.example.nirbhay.Users.PostActivity;
import com.example.nirbhay.Users.PostDetails;
import com.example.nirbhay.Users.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.geometry.LatLngSpan;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.example.nirbhay.Users.MapDir.MapsActivity.generateBitmapDescriptorFromRes;

public class Survillance extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker currentLocationMarker;
    private Button addCrime, plotCrime, signOut;
    private DatabaseReference databaseReference;
    ArrayList markerPoints= new ArrayList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survillance);


        addCrime = (Button) findViewById(R.id.addCrime);
        plotCrime = (Button) findViewById(R.id.plotCrime);
        signOut = (Button) findViewById(R.id.signOut);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        addCrime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Survillance.this);

                LinearLayout layout = new LinearLayout(Survillance.this);
                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setLayoutParams(parms);

                layout.setGravity(Gravity.CLIP_VERTICAL);
                layout.setPadding(2, 2, 2, 2);

                TextView tv = new TextView(Survillance.this);
                tv.setText("Enter ThreatLevel: (0, 1, 2)");
                tv.setPadding(40, 40, 40, 40);
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(20);

                final EditText et = new EditText(Survillance.this);

                TextView tv1 = new TextView(Survillance.this);
                tv1.setText("Click Ok to select location");
                tv1.setPadding(20, 20, 20, 20);

                LinearLayout.LayoutParams tv1Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tv1Params.bottomMargin = 5;
                layout.addView(et, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                alertDialogBuilder.setView(layout);
                alertDialogBuilder.setCustomTitle(tv);

                alertDialogBuilder.setCancelable(false);

                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });

                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final String etStr = et.getText().toString();
                        if(etStr.isEmpty()){
                            Toast.makeText(Survillance.this, "Add threatLevel!!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    if (markerPoints.size() > 0) {
                                        markerPoints.clear();
                                        mMap.clear();
                                    }

                                    // Adding new item to the ArrayList
                                    markerPoints.add(latLng);

                                    // Creating MarkerOptions
                                    MarkerOptions options = new MarkerOptions();

                                    // Setting the position of the marker
                                    options.position(latLng);

                                    if (markerPoints.size() == 1) {
                                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                    }

                                    mMap.addMarker(options);

                                    if (markerPoints.size() == 1) {


                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Survillance.this);
                                        alertDialog.setTitle("Add Crime");
                                        alertDialog.setCancelable(false);
                                        alertDialog.setMessage("Do You want to add crime to this location?");
                                        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                LatLng origin = (LatLng) markerPoints.get(0);
                                                CrimeRecords crimeRecords = new CrimeRecords();
                                                crimeRecords.setLatitude(origin.latitude);
                                                crimeRecords.setLongitude(origin.longitude);
                                                crimeRecords.setThreatLevel(Integer.parseInt(etStr));
                                                final String pushKey = databaseReference.push().getKey();
                                                crimeRecords.setCrimeNo(pushKey);
                                                databaseReference.child("crimes").child(pushKey).setValue(crimeRecords);
                                                Toast.makeText(Survillance.this, "Crime Added", Toast.LENGTH_SHORT).show();
                                                dialog.cancel();
                                            }
                                        });
                                        alertDialog.show();

                                    }

                                }
                            });
                        }
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();

                try {
                    alertDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
            }
        });

        plotCrime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.setOnMapClickListener(null);
                mMap.clear();
                mMap.addMarker(new MarkerOptions()
                        .position(currentLocationMarker.getPosition())
                        .title("Current location")
                        .snippet("Current location")
                        .icon(generateBitmapDescriptorFromRes(Survillance.this, R.drawable.current_location))
                );
                LatLng currentLoc = getCurrentLocation();
                if(currentLoc!=null){
                    GetPincode getPincode = new GetPincode();
                    String url = getDirectionsUrl(currentLoc);
                    getPincode.execute(url);
                }else{
                    Toast.makeText(Survillance.this, "Unable to access your location", Toast.LENGTH_SHORT).show();
                }

            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                SharedPreferences pref = getApplicationContext().getSharedPreferences("Pref", 0); //Mode_private
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("finishedSignUp", true);
                editor.putBoolean("signedIn", false);
                editor.apply();
                editor.commit();
                Intent intent = new Intent(Survillance.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng currentLocation = getCurrentLocation();
        if(currentLocation!=null) {
            currentLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .title("Current location")
                    .snippet("Current location")
                    .icon(generateBitmapDescriptorFromRes(Survillance.this, R.drawable.current_location))
            );
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13));
            // Zoom in, animating the camera.
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
            // Zoom out to zoom level 10, animating with a duration of 2 seconds.
            mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
        }else{
            Toast.makeText(Survillance.this, "Unable to get your location", Toast.LENGTH_SHORT).show();
        }
    }

    public LatLng getCurrentLocation(){
        GPSTracker gps = new GPSTracker(Survillance.this);

        if (gps.canGetLocation()) {
            LatLng latLng = new LatLng(gps.getLatitude(), gps.getLongitude());

            Toast.makeText(Survillance.this, "current Location" + latLng, Toast.LENGTH_SHORT).show();
            return latLng;
        } else {
            gps.showSettingsAlert();
        }
        return null;
    }

    private class GetPincode extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog = new ProgressDialog(Survillance.this);
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
                    Toast.makeText(Survillance.this, "Error accessing your location", Toast.LENGTH_SHORT).show();
                }
                else{
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    JSONObject resultObject =jsonArray.getJSONObject(0);
                    final String pincode = resultObject.getString("pincode");

                    databaseReference.child("emergency").child(pincode).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //progressBar.setVisibility(View.INVISIBLE);
                            if(dataSnapshot.getValue() == null){
                                Toast.makeText(Survillance.this, "No records found!!", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(Survillance.this, "" + dataSnapshot.getChildrenCount(), Toast.LENGTH_LONG).show();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    PanicDetails panicDetails = snapshot.getValue(PanicDetails.class);
                                    LatLng loc = new LatLng(panicDetails.getLatitude(), panicDetails.getLongitude());
                                    mMap.addMarker(new MarkerOptions()
                                            .position(loc)
                                            .title(panicDetails.getName())
                                            .snippet("Mob No: "+ panicDetails.getPhoneNumber())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                    );
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
    private String getDirectionsUrl(LatLng origin) {
        String url = "https://apis.mapmyindia.com/advancedmaps/v1/zdhlcatxe8tt5mr6c9w3wrhrdfbbr8h3/rev_geocode?lat=" +origin.latitude+"&lng="+origin.longitude;
        Log.i("info", url);

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
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
