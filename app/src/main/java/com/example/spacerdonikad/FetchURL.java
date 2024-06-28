package com.example.spacerdonikad;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class FetchURL extends AsyncTask<String, Void, PolylineOptions> {
    TaskLoadedCallback taskCallback;

    public FetchURL(TaskLoadedCallback taskCallback) {
        this.taskCallback = taskCallback;
    }

    @Override
    protected PolylineOptions doInBackground(String... strings) {
        String data = "";
        PolylineOptions polylineOptions = null;
        try {
            data = downloadUrl(strings[0]);
            JSONObject jsonObject = new JSONObject(data);
            DirectionsJSONParser parser = new DirectionsJSONParser();
            List<List<HashMap<String, String>>> routes = parser.parse(jsonObject);
            polylineOptions = getPolylineOptions(routes);
        } catch (Exception e) {
            Log.d("FetchURL", e.toString());
        }
        return polylineOptions;
    }

    @Override
    protected void onPostExecute(PolylineOptions polylineOptions) {
        taskCallback.onTaskDone(polylineOptions);
    }

    private String downloadUrl(String strUrl) throws Exception {
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
