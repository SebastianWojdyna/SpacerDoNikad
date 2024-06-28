package com.example.spacerdonikad;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;
    private TextView locationTextView;
    private EditText distanceInput;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private LatLng currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationTextView = findViewById(R.id.locationTextView);
        distanceInput = findViewById(R.id.distanceInput);
        Button generateRouteButton = findViewById(R.id.generateRouteButton);
        Button generateSpecificRouteButton = findViewById(R.id.generateSpecificRouteButton);

        generateRouteButton.setOnClickListener(v -> generateRandomRoute());
        generateSpecificRouteButton.setOnClickListener(v -> generateRouteWithDistance());

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                getCurrentLocation();
            } else {
                locationTextView.setText("Permission denied");
            }
        });

        getLocationPermission();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            locationTextView.setText("Lat: " + location.getLatitude() + " Lon: " + location.getLongitude());
                        } else {
                            Log.w("MainActivity", "getLastLocation:exception", task.getException());
                        }
                    }
                });
    }

    private void generateRandomRoute() {
        if (currentLocation != null) {
            Random random = new Random();
            double randomDistance = 1 + (5 - 1) * random.nextDouble(); // Generuje losową odległość między 1 km a 5 km
            generateRoute(randomDistance);
        } else {
            locationTextView.setText("Current location is not available");
        }
    }

    private void generateRouteWithDistance() {
        if (currentLocation != null) {
            String distanceStr = distanceInput.getText().toString();
            if (!distanceStr.isEmpty()) {
                double distance = Double.parseDouble(distanceStr);
                generateRoute(distance);
            } else {
                locationTextView.setText("Please enter a distance");
            }
        } else {
            locationTextView.setText("Current location is not available");
        }
    }

    private void generateRoute(double distance) {
        double randomBearing = new Random().nextDouble() * 360;
        double earthRadius = 6371.01; // Radius of the Earth in km

        double lat1 = Math.toRadians(currentLocation.latitude);
        double lon1 = Math.toRadians(currentLocation.longitude);

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance / earthRadius) +
                Math.cos(lat1) * Math.sin(distance / earthRadius) * Math.cos(Math.toRadians(randomBearing)));
        double lon2 = lon1 + Math.atan2(Math.sin(Math.toRadians(randomBearing)) * Math.sin(distance / earthRadius) * Math.cos(lat1),
                Math.cos(distance / earthRadius) - Math.sin(lat1) * Math.sin(lat2));

        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);

        LatLng targetLocation = new LatLng(lat2, lon2);

        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        intent.putExtra("currentLocation", currentLocation);
        intent.putExtra("targetLocation", targetLocation);
        startActivity(intent);
    }
}
