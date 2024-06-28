package com.example.spacerdonikad;

import com.google.android.gms.maps.model.PolylineOptions;

public interface TaskLoadedCallback {
    void onTaskDone(PolylineOptions polylineOptions);
}
