package com.owambo.jvamcas.drivemehome.ui.driverpair;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.appspot.drivemehome_86841.drivemehomeapi.model.Person;
import com.appspot.drivemehome_86841.drivemehomeapi.model.Spot;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.snackbar.Snackbar;
import com.owambo.jvamcas.drivemehome.R;
import com.owambo.jvamcas.drivemehome.data.model.User;
import com.owambo.jvamcas.drivemehome.databinding.DriverPeakLayoutBinding;
import com.owambo.jvamcas.drivemehome.databinding.DriverSearchLayoutBinding;
import com.owambo.jvamcas.drivemehome.databinding.FragmentDriverPairBinding;
import com.owambo.jvamcas.drivemehome.ui.TopAppFragment;
import com.owambo.jvamcas.drivemehome.utils.ParseUtil;
import com.owambo.jvamcas.drivemehome.utils.callback.Callback;
import com.owambo.jvamcas.drivemehome.utils.transform.CircleTransformation;
import com.owambo.jvamcas.drivemehome.viewmodel.UserViewModel;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 *
 */
public class HomeFragment extends TopAppFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final int REQUEST_LOCATIONS = 2;//verify that location is not switched off
    private static final int START_LOCATION = 3, DESTINATION_LOCATION = 4;
    private static final float TRIP_START_HUE = BitmapDescriptorFactory.HUE_BLUE;
    private static final float TRIP_END_HUE = BitmapDescriptorFactory.HUE_CYAN;

    private FragmentDriverPairBinding driverBinding;

    private UserViewModel userModel;
    private GoogleMap googleMap;
    private CameraPosition mCameraPosition;

    public static TripManager tripManager;

    private LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // location retrieved by the Fused Location Provider.
    private LatLng mLastKnownLocation;

    private boolean mLocationPermissionGranted;

    private static final int DEFAULT_ZOOM = 15;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    //periodically receives updates about current device location based on setting in setting clients
    private LocationCallback mLocationCallback;

    private Spot startLocation;
    private Spot destinationLocation;

    private DriverSearchLayoutBinding tripBinding;
    private boolean mLocationEnabled;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String API_KEY = requireActivity().getResources().getString(R.string.google_map_api);
        Places.initialize(requireContext(), API_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);

        // Retrieve location and camera position location_from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        driverBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_pair, container, false);
        userModel = ViewModelProviders.of(requireActivity()).get(UserViewModel.class);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        //receives periodic location updates
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                User user = userModel.getUser().getValue();
                if (locationResult != null && user != null) {
                    Location mLocation = locationResult.getLastLocation();

                    mLastKnownLocation = mLocation == null ? //no new location - use location location_from DB
                            new LatLng(user.getLatitude(), user.getLongitude()) :
                            new LatLng(mLocation.getLatitude(), mLocation.getLongitude());//use latest location
                    userModel.updateUserLocation(mLastKnownLocation);
                }
            }
        };

        //move camera to point to current location on map

        userModel.getUser().observe(getViewLifecycleOwner(), user -> moveToCurrentLocation());

        createLocationRequest();
        mapInit();//build the map
        handleDriverSearch();
        handleTrip();
        return driverBinding.getRoot();
    }

    /***
     * Handle search for available drivers
     */

    private void handleDriverSearch() {
        //handle the click on driver search fab

        driverBinding.newTripFab.setOnClickListener(view -> {
            if (tripManager.existValidTrip()) {
                Snackbar.make(driverBinding.newTripFab, requireActivity().getString(R.string.cancel_trip_snackbar), Snackbar.LENGTH_LONG)
                        .setAction(requireActivity().getString(R.string.OK), v -> {//cancel this trip and present new trip setup dialog
                            tripManager.cancelCurrentTrip();
                            showTripSetupDialog();
                        }).show();
            } else
                showTripSetupDialog();
        });
    }

    /***
     * Create and show Alert dialog for setting up a new trip
     */
    private void showTripSetupDialog() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(requireContext());
        tripBinding = DataBindingUtil.inflate(getLayoutInflater(),
                R.layout.driver_search_layout, null, false);
        View mView = tripBinding.getRoot();
        mBuilder.setView(mView);
        AlertDialog mDialog = mBuilder.create();

        //Find nearest drivers
        tripBinding.findDriver.setOnClickListener(v -> {
            if (startLocation != null && destinationLocation != null) {
                tripManager.startNewTrip(startLocation, destinationLocation);
                mDialog.dismiss();
            } else
                showToast(requireActivity().getResources().getString(R.string.setup_trip));
        });
        //Autocomplete form for selecting start and destination form

        List<Place.Field> fields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);
        Autocomplete.IntentBuilder builder = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields);
        builder.setCountry("NA");
        Intent mIntent = builder.build(requireActivity());

        //start autocomplete intent for start location
        tripBinding.locationFrom.setOnClickListener(view1 -> startActivityForResult(mIntent, START_LOCATION));

        //start autocomplete intent for destination location
        tripBinding.locationDestination.setOnClickListener(view1 -> startActivityForResult(mIntent, DESTINATION_LOCATION));
        mDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            LatLng loc = place.getLatLng();

            if (loc != null) {
                if (requestCode == START_LOCATION) {
                    tripBinding.locationFrom.setText(place.getName());
                    startLocation = new Spot()
                            .setName(place.getName())
                            .setLatitude(loc.latitude)
                            .setLongitude(loc.longitude);
                } else if (requestCode == DESTINATION_LOCATION) {
                    tripBinding.locationDestination.setText(place.getName());
                    destinationLocation = new Spot()
                            .setName(place.getName())
                            .setLatitude(loc.latitude)
                            .setLongitude(loc.longitude);
                }
            }
        }
    }


    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(40000);
        mLocationRequest.setFastestInterval(20000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(requireContext().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                            String []permissions,
                                            int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
    }

    private void mapInit() {
        if (googleMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().
                    findFragmentById(R.id.google_map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnMarkerClickListener(this);

        // Prompt the user for permission.
        getLocationPermission();
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    /***
     * Set the location controls on the map
     */
    private void updateLocationUI() {
        if (googleMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /***
     * Verify that location settings enabled else prompt user to enable them
     */
    private void verifyLocationSettingSatisfied() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(requireActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(requireActivity(), locationSettingsResponse -> {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            mLocationEnabled = true;
        });

        task.addOnFailureListener(requireActivity(), e -> {
            mLocationEnabled = false;

            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(requireActivity(),
                            REQUEST_LOCATIONS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }

    /**
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted)
                mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                        Looper.getMainLooper());

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void handleTrip() {
        if (tripManager == null)
            tripManager = new TripManager(userModel, this);
        userModel.getCurrentTrip().observe(getViewLifecycleOwner(), trip -> {
            if (trip != null) {
                showTripMarkersOnMap(trip.getStart(), trip.getDestination());
                showNearbyDriversOnMap(trip.getNearByDriverList());
                drawTripRouteOnMap(trip.getRoutePoints());
            } else {
                moveToCurrentLocation();
            }
        });
    }

    /***
     * Move camera to point to current location on map
     */
    void moveToCurrentLocation() {
        User mUser = userModel.getUser().getValue();
        if (googleMap != null && mLocationPermissionGranted && mUser != null && !tripManager.existValidTrip()) {
            googleMap.clear();
            googleMap.moveCamera(CameraUpdateFactory.
                    newLatLngZoom(new LatLng(mUser.getLatitude(), mUser.getLongitude()), DEFAULT_ZOOM));
        }
    }

    /***
     * Draw nearby drivers on the map
     * @param nearbyDrivers a list of nearby drivers to be drawn on the map
     */
    private void showNearbyDriversOnMap(List<Person> nearbyDrivers) {
        if (googleMap != null && mLocationPermissionGranted && nearbyDrivers != null) {
            for (Person driver : nearbyDrivers) {
                LatLng location = new LatLng(driver.getLatitude(), driver.getLongitude());
                Marker driverMarker = googleMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(driver.getName()));

                driverMarker.setTag(driver.getId());
                new DownloadDriverAvatarTask((bitmap, mResult) ->
                        driverMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap)))
                        .execute(driver.getUserIconUrl());
            }
        }
    }

    private void showTripMarkersOnMap(Spot startLocation, Spot destinationLocation) {
        if (googleMap != null && mLocationPermissionGranted) {
            googleMap.clear();//remove old markers
            LatLng tripStart = new LatLng(startLocation.getLatitude(), startLocation.getLongitude());
            LatLng tripEnd = new LatLng(destinationLocation.getLatitude(), destinationLocation.getLongitude());

            googleMap.addMarker(new MarkerOptions()
                    .position(tripStart)
                    .title(startLocation.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(TRIP_START_HUE)));
            googleMap.addMarker(new MarkerOptions()
                    .position(tripEnd)
                    .title(destinationLocation.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(TRIP_END_HUE)));

            LatLngBounds.Builder builder = new LatLngBounds.Builder()
                    .include(tripStart)
                    .include(tripEnd);
            int width = requireActivity().getResources().getDisplayMetrics().widthPixels;
            int height = requireActivity().getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (height * 0.2f);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, padding));
        }
    }

    private void drawTripRouteOnMap(List<Spot> routePoints) {
        if (googleMap != null && mLocationPermissionGranted && routePoints != null) {
            PolylineOptions options = new PolylineOptions();
            for (Spot loc : routePoints)
                options.add(new LatLng(loc.getLatitude(), loc.getLongitude()));

            googleMap.addPolyline(options
                    .color(Color.BLUE))
                    .setWidth(8);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String driverId = marker.getTag() == null ? null : (String) marker.getTag();

        if (driverId != null && tripManager.existValidTrip()) {
            Person clickedDriver = tripManager.getNearbyDriver(driverId);
            String charge = tripManager.getNearbyDriverCharge(driverId);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            DriverPeakLayoutBinding peakLayoutBinding = DriverPeakLayoutBinding.inflate(getLayoutInflater(), null, false);

            View mView = peakLayoutBinding.getRoot();
            builder.setView(mView);//attach custom layout to alertdialog builder

            String serverUser = ParseUtil.toJson(clickedDriver);//convert server user to local user

            peakLayoutBinding.setMUser(ParseUtil.fromJson(serverUser, User.class));
            peakLayoutBinding.setTripCharge(charge);

            AlertDialog mDialog = builder.create();

            peakLayoutBinding.actionBtn.setOnClickListener(view -> {
                mDialog.dismiss();
                tripManager.allocateSelectedDriver(clickedDriver);
            });
            mDialog.show();
        }
        return false;
    }

    @Override
    public void onSaveInstanceState( Bundle outState) {
        if (googleMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, googleMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        verifyLocationSettingSatisfied();
    }

    /***
     * Download driver icon for nearby drivers
     */

    public static class DownloadDriverAvatarTask extends AsyncTask<String, Void, Bitmap> {

        private Callback<Bitmap> bitmapCallback;

        DownloadDriverAvatarTask(Callback<Bitmap> bitmapCallback) {
            this.bitmapCallback = bitmapCallback;
        }

        @Override
        protected Bitmap doInBackground(String... url) {
            Picasso.get().invalidate(url[0]);
            Bitmap bitmap = null;
            try {
                bitmap = Picasso.get()
                        .load(url[0])
                        .resize(100, 100)
                        .centerCrop()
                        .transform(new CircleTransformation())
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            bitmapCallback.onTaskComplete(bitmap, null);
        }
    }
}
