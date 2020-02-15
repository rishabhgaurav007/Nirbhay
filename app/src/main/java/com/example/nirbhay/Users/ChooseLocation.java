package com.example.nirbhay.Users;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nirbhay.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

public class ChooseLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private boolean locationSelected = false;
    private CarmenFeature sourceCarmen = null;
    private Marker chooseMarker;
    private TextView choosenLocation;
    private Button selectLocation;
    private LatLng choosenLatLng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_location);
        locationSelected = false;
        choosenLocation = (TextView) findViewById(R.id.chooseLocation);
        selectLocation = (Button) findViewById(R.id.selectLocation);
        TextView back = (TextView) findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Mapbox.getInstance(this, getString(R.string.access_token));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                choosenLatLng = latLng;
                if(chooseMarker!=null)
                    chooseMarker.remove();
                chooseMarker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                choosenLocation.setText("Lat: " + latLng.latitude + " Lng: "+ latLng.longitude);
                locationSelected = true;
            }
        });
    }

    private void initSearchFab() {

        findViewById(R.id.chooseLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(ChooseLocation.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });

        selectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(locationSelected){
                    Toast.makeText(ChooseLocation.this, choosenLatLng.toString(), Toast.LENGTH_LONG).show();
                    Intent returnIntent = new Intent();

                    returnIntent.putExtra("latitude", Double.toString(choosenLatLng.latitude));
                    returnIntent.putExtra("longitude", Double.toString(choosenLatLng.longitude));
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
                else{
                    Toast.makeText(ChooseLocation.this, "Choose Location!!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            locationSelected = true;
            // Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);
            LatLng currentLocation = new LatLng(((Point)selectedCarmenFeature.geometry()).latitude(),
                    ((Point) selectedCarmenFeature.geometry()).longitude());
            choosenLatLng = currentLocation;
            sourceCarmen = selectedCarmenFeature;
            choosenLocation.setText(selectedCarmenFeature.placeName());
            chooseMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,10));
            // Zoom in, animating the camera.
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
            // Zoom out to zoom level 10, animating with a duration of 2 seconds.
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

        }
    }
}
