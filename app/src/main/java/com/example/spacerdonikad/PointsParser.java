package com.example.spacerdonikad;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class PointsParser extends AsyncTask<String, Integer, PolylineOptions> {
    TaskLoadedCallback taskCallback;

    public PointsParser(TaskLoadedCallback taskCallback) {
        this.taskCallback = taskCallback;
    }

    @Override
    protected PolylineOptions doInBackground(String... jsonData) {
        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try {
            jObject = new JSONObject(jsonData[0]);
            DirectionsJSONParser parser = new DirectionsJSONParser();
            routes = parser.parse(jObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getPolylineOptions(routes);
    }

    @Override
    protected void onPostExecute(PolylineOptions polylineOptions) {
        taskCallback.onTaskDone(polylineOptions);
    }

    private PolylineOptions getPolylineOptions(List<List<HashMap<String, String>>> routes) {
        PolylineOptions polylineOptions = new PolylineOptions();

        for (int i = 0; i < routes.size(); i++) {
            List<HashMap<String, String>> path = routes.get(i);

            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);
                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);
                polylineOptions.add(position);
            }
        }
        return polylineOptions;
    }
}
