package com.example.spacerdonikad;

import android.net.Uri;

import androidx.fragment.app.FragmentActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng currentLocation;
    private LatLng targetLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        currentLocation = intent.getParcelableExtra("currentLocation");
        targetLocation = intent.getParcelableExtra("targetLocation");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button startHikeButton = findViewById(R.id.startHikeButton);
        startHikeButton.setOnClickListener(v -> startNavigation());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (currentLocation != null && targetLocation != null) {
            mMap.addMarker(new MarkerOptions().position(currentLocation).title("Obecne położenie"));
            mMap.addMarker(new MarkerOptions().position(targetLocation).title("Docelowe położenie"));

            String url = getDirectionsUrl(currentLocation, targetLocation);
            new FetchURL(this::drawRoute).execute(url);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        }
    }

    private void drawRoute(PolylineOptions polylineOptions) {
        if (polylineOptions != null) {
            mMap.addPolyline(polylineOptions);
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String strOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        String strDest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=walking";
        String parameters = strOrigin + "&" + strDest + "&" + mode;
        String output = "json";
        String apiKey = getString(R.string.MAPS_API_KEY);
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + apiKey;
    }

    private void startNavigation() {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + targetLocation.latitude + "," + targetLocation.longitude + "&mode=w");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
}
