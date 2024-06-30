package com.example.spacerdonikad;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String LANGUAGE_KEY = "language_key";
    private static final String THEME_KEY = "theme_key";
    private FusedLocationProviderClient fusedLocationClient;
    private EditText distanceInput;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private LatLng currentLocation;
    private boolean isPolish;
    private boolean isDarkTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Odczytaj zapisany język i motyw z SharedPreferences przed ustawieniem układu
        isPolish = getSharedPreferences("AppSettings", MODE_PRIVATE).getBoolean(LANGUAGE_KEY, true);
        isDarkTheme = getSharedPreferences("AppSettings", MODE_PRIVATE).getBoolean(THEME_KEY, false);

        Log.d(TAG, "onCreate: Setting initial locale to " + (isPolish ? "pl" : "en"));
        LocaleHelper.setLocale(this, isPolish ? "pl" : "en");

        // Ustawienie motywu
        AppCompatDelegate.setDefaultNightMode(isDarkTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        distanceInput = findViewById(R.id.distanceInput);
        Button generateRouteButton = findViewById(R.id.generateRouteButton);
        Button generateSpecificRouteButton = findViewById(R.id.generateSpecificRouteButton);
        Switch themeSwitch = findViewById(R.id.themeSwitch);
        Switch languageSwitch = findViewById(R.id.languageSwitch);

        // Ustawienie przełączników w odpowiedniej pozycji
        themeSwitch.setChecked(isDarkTheme);
        languageSwitch.setChecked(isPolish);

        generateRouteButton.setOnClickListener(v -> generateRandomRoute());
        generateSpecificRouteButton.setOnClickListener(v -> generateRouteWithDistance());
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> toggleTheme(isChecked));
        languageSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> toggleLanguage(isChecked));

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Log.d(TAG, "Location permission granted.");
                getCurrentLocation();
            } else {
                Log.w(TAG, "Location permission denied.");
            }
        });

        Log.d(TAG, "Requesting location permission");
        getLocationPermission();
    }

    private void getLocationPermission() {
        Log.d(TAG, "Requesting location permission...");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission already granted.");
            getCurrentLocation();
        } else {
            Log.d(TAG, "Location permission not granted, requesting...");
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getCurrentLocation() {
        Log.d(TAG, "Getting current location...");
        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            Log.d(TAG, "Current location obtained: " + currentLocation);
                        } else {
                            Log.w(TAG, "Failed to get current location", task.getException());
                        }
                    }
                });
    }

    private void generateRandomRoute() {
        Log.d(TAG, "Generating random route...");
        if (currentLocation != null) {
            new Thread(() -> {
                Random random = new Random();
                double randomDistance = 1 + (5 - 1) * random.nextDouble();
                Log.d(TAG, "Random distance: " + randomDistance);
                generateRoute(randomDistance);
            }).start();
        } else {
            Log.w(TAG, "Current location is null, cannot generate route.");
        }
    }

    private void generateRouteWithDistance() {
        Log.d(TAG, "Generating route with specified distance...");
        if (currentLocation != null) {
            String distanceStr = distanceInput.getText().toString();
            if (!distanceStr.isEmpty()) {
                new Thread(() -> {
                    double distance = Double.parseDouble(distanceStr);
                    Log.d(TAG, "Specified distance: " + distance);
                    generateRoute(distance);
                }).start();
            } else {
                Log.w(TAG, "Distance input is empty.");
            }
        } else {
            Log.w(TAG, "Current location is null, cannot generate route.");
        }
    }

    private void generateRoute(double distance) {
        Log.d(TAG, "Generating route with distance: " + distance);
        double randomBearing = new Random().nextDouble() * 360;
        double earthRadius = 6371.01;

        double lat1 = Math.toRadians(currentLocation.latitude);
        double lon1 = Math.toRadians(currentLocation.longitude);

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance / earthRadius) +
                Math.cos(lat1) * Math.sin(distance / earthRadius) * Math.cos(Math.toRadians(randomBearing)));
        double lon2 = lon1 + Math.atan2(Math.sin(Math.toRadians(randomBearing)) * Math.sin(distance / earthRadius) * Math.cos(lat1),
                Math.cos(distance / earthRadius) - Math.sin(lat1) * Math.sin(lat2));

        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);

        LatLng targetLocation = new LatLng(lat2, lon2);
        Log.d(TAG, "Target location: " + targetLocation);

        new Handler(Looper.getMainLooper()).post(() -> {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            intent.putExtra("currentLocation", currentLocation);
            intent.putExtra("targetLocation", targetLocation);
            startActivity(intent);
        });
    }

    private void toggleTheme(boolean isDark) {
        Log.d(TAG, "Toggling theme to " + (isDark ? "dark" : "light"));
        isDarkTheme = isDark;
        getSharedPreferences("AppSettings", MODE_PRIVATE).edit().putBoolean(THEME_KEY, isDarkTheme).apply();
        AppCompatDelegate.setDefaultNightMode(isDarkTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        resetActivity(); // Reset the activity to apply the new theme
    }

    private void toggleLanguage(boolean isPolishSelected) {
        Log.d(TAG, "Toggling language to " + (isPolishSelected ? "pl" : "en"));
        isPolish = isPolishSelected;
        getSharedPreferences("AppSettings", MODE_PRIVATE).edit().putBoolean(LANGUAGE_KEY, isPolish).apply();
        LocaleHelper.setLocale(this, isPolish ? "pl" : "en");
        resetActivity(); // Reset the activity to apply the new language
    }

    private void resetActivity() {
        Log.d(TAG, "Resetting activity to apply new settings");
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
