package com.owambo.jvamcas.drivemehome.ui.driverpair;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.owambo.jvamcas.drivemehome.utils.callback.Callback;

import java.io.IOException;
import java.util.List;

/***CLass to fetch polylines for a given route
 * @author Petrus Kambala
 */
public class GoogleMapRoute {

    private String api_key;
    private LatLng origin;
    private LatLng end;


    public GoogleMapRoute(String api_key, LatLng origin, LatLng end) {
        this.api_key = api_key;
        this.origin = origin;
        this.end = end;
    }

    public void path(Callback<List<LatLng>> pathPolyline) {
        new RoutePath(pathPolyline, api_key, origin, end).execute();
    }

    private static class RoutePath extends AsyncTask<Void, Void, DirectionsResult> {

        private Callback<List<LatLng>> callback;
        private String api_key;
        private LatLng origin;
        private LatLng end;


        public RoutePath(Callback<List<LatLng>> callback, String api_key, LatLng origin, LatLng end) {
            this.callback = callback;
            this.api_key = api_key;
            this.origin = origin;
            this.end = end;
        }

        @Override
        protected DirectionsResult doInBackground(Void... voids) {

            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey(api_key)
                    .build();
            DirectionsResult result = null;

            try {
                result = DirectionsApi.newRequest(context)
                        .mode(TravelMode.DRIVING)
                        .origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                        .destination(new com.google.maps.model.LatLng(end.latitude, end.longitude))
                        .await();
            } catch (ApiException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(DirectionsResult response) {
            if (response != null && response.routes != null)
                callback.onTaskComplete(PolyUtil.decode(response.routes[0].overviewPolyline.getEncodedPath()), null);
        }
    }
}
