package com.mapdemo.googlemapdemo;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sourabh on 14/04/18.
 */

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e(TAG, "Map is ready");
        googleMaps = googleMap;
        setLocationCallBackListener();
        if (permissionGranted) {
            checkGpsEnable();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            googleMaps.setMyLocationEnabled(true);
            googleMaps.getUiSettings().setMyLocationButtonEnabled(false);
            init();
            Log.e(TAG, "git checkout");
        }
    }

    private static final String TAG = "MapsActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private boolean permissionGranted = false;
    private static final int LOCATION_REQUEST_CODE = 123;
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int REQUEST_LOCATION = 199;
    private static final float DEFAULT_ZOOM = 17f;
    private GoogleMap googleMaps;
    private AutoCompleteTextView search_box;
    private ImageView current_location, location_info, place_picker;
    protected GeoDataClient mGeoDataClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private PlaceAutoCompleteAdapter placeAutoCompleteAdapter;
    private GoogleApiClient googleApiClient;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136)
    );
    private PlaceInfo placeInfo;
    private Marker marker;
    private LocationRequest locationRequest;
    private LocationCallback mLocationCallback;
    private Location mLastLocation;
    private String mAddressOutput;
    private AddressResultReceiver mResultReceiver;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);
        search_box = findViewById(R.id.input_search);
        current_location = findViewById(R.id.current_location);
        location_info = findViewById(R.id.current_info);
        place_picker = findViewById(R.id.place_picker);
        getLocationPermission();

    }

    private void init() {
        mGeoDataClient = Places.getGeoDataClient(this, null);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        search_box.setOnItemClickListener(itemSelectedListener);
        placeAutoCompleteAdapter = new PlaceAutoCompleteAdapter(this, mGeoDataClient,
                LAT_LNG_BOUNDS, null);
        search_box.setAdapter(placeAutoCompleteAdapter);
        search_box.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                    geoLocate();
                }

                return false;
            }
        });

        current_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });

        location_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (marker.isInfoWindowShown()) {
                    marker.hideInfoWindow();
                } else {
                    marker.showInfoWindow();
                }
            }
        });
        place_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(MapsActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    Log.e(TAG, "GooglePlayServicesRepairableException :" + e);
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
        hideSoftKeyboard(search_box);
    }

    private void geoLocate() {

        String searchString = search_box.getText().toString();
        Geocoder geocoder = new Geocoder(MapsActivity.this);

        List<Address> addresses = new ArrayList<>();

        try {
            addresses = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() > 0) {
            Address address = addresses.get(0);
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }

    private void getDeviceLocation() {
        Log.e(TAG, "calling getDeviceLocation");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
//                      Location currentLocation = (Location) task.getResult();
                        mLastLocation = location;
                        moveCamera(new LatLng(location.getLatitude(), location.getLongitude()),
                                DEFAULT_ZOOM, "My Location");
                    } else {
                        Toast.makeText(MapsActivity.this, "Could not find location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException :" + e.toString());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        googleMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        if (!title.equalsIgnoreCase("My Location")) {
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);
            googleMaps.addMarker(markerOptions);
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getLocationPermission() {
        String[] permission = {Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                permissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permission, LOCATION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permission, LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            permissionGranted = false;
                            return;
                        }
                    }
                    permissionGranted = true;
                    initMap();
                }
        }

    }

    private void hideSoftKeyboard(View view) {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    /**
     *    google place api autocomplete suggestion
     */

    private AdapterView.OnItemClickListener itemSelectedListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideSoftKeyboard(view);
            AutocompletePrediction item = placeAutoCompleteAdapter.getItem(position);
            String placeId = item.getPlaceId();
            PendingResult<PlaceBuffer> placeBufferPendingResult = Places.GeoDataApi.
                    getPlaceById(googleApiClient, placeId);
            placeBufferPendingResult.setResultCallback(updatePlaceBufferCallBack);
        }
    };

    private ResultCallback<PlaceBuffer> updatePlaceBufferCallBack = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                places.release();
            }
            final Place place = places.get(0);
            try {
                placeInfo = new PlaceInfo();
                placeInfo.setName(place.getName().toString());
                placeInfo.setAddress(place.getAddress().toString());
                placeInfo.setLatLng(place.getLatLng());
                placeInfo.setId(place.getId());
//              placeInfo.setAttributes(place.getAttributions().toString());
                placeInfo.setRating(place.getRating());
                placeInfo.setPhoneNumber(place.getPhoneNumber().toString());
                placeInfo.setWebsiteUrl(placeInfo.getWebsiteUrl());
            } catch (NullPointerException e) {
                Log.e(TAG, "NullPointerException :" + e);
            }
            moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude), DEFAULT_ZOOM, placeInfo);
            places.release();
        }
    };

    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo) {
        googleMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        googleMaps.clear();

        googleMaps.setInfoWindowAdapter(new CustomInfoAdapter(MapsActivity.this));
        if (placeInfo != null) {
            String snippet = "Address: " + placeInfo.getAddress() + "\n" +
                    "Phone Number: " + placeInfo.getPhoneNumber() + "\n" +
                    "Website: " + placeInfo.getWebsiteUrl() + "\n" +
                    "Rating: " + placeInfo.getRating();
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).
                    title(placeInfo.getName())
                    .snippet(snippet);
            marker = googleMaps.addMarker(markerOptions);
        } else {
            googleMaps.addMarker(new MarkerOptions().position(latLng));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                PendingResult<PlaceBuffer> placeBufferPendingResult = Places.GeoDataApi.
                        getPlaceById(googleApiClient, place.getId());
                placeBufferPendingResult.setResultCallback(updatePlaceBufferCallBack);
            }
        } else if (requestCode == REQUEST_LOCATION) {
            switch (resultCode) {
                case Activity.RESULT_OK: {
                    // All required changes were successfully made
                    startLocationUpdates();
                    Toast.makeText(MapsActivity.this, "Location enabled by user!", Toast.LENGTH_LONG).show();

                    break;
                }
                case Activity.RESULT_CANCELED: {
                    // The user was asked to change settings, but chose not to
                    Toast.makeText(MapsActivity.this, "Location not enabled, user cancelled.", Toast.LENGTH_LONG).show();

                    finish();

                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private boolean hasGPSDevice(Context context) {
        final LocationManager mgr = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null)
            return false;
        final List<String> providers = mgr.getAllProviders();
        if (providers == null)
            return false;
        return providers.contains(LocationManager.GPS_PROVIDER);
    }

    private void enableLoc() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(MapsActivity.this)
                    .addApi(LocationServices.API).build();
            googleApiClient.connect();
            locationRequest = LocationRequest.create();
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);

            SettingsClient client = LocationServices.getSettingsClient(this);
            com.google.android.gms.tasks.Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

            task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    // All location settings are satisfied. The client can initialize
                    // location requests here.
                    // ...
                   // setLocationCallBackListener();
                    startLocationUpdates();
                    Log.e(TAG, "getDeviceLocation calling form enable loc");
                }
            });

            task.addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MapsActivity.this,
                                    REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                    }
                }
            });
        }
    }

    private void checkGpsEnable() {
        LocationManager manager = (LocationManager) MapsActivity.this.getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(MapsActivity.this)) {
            Log.e("Sourabh", "Gps not enabled");
            enableLoc();
        } else {
            Log.e("Sourabh", "iss condition mein aaya kya vo ");
            getDeviceLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        enableLoc();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        if (fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            try {
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
//                      Location currentLocation = (Location) task.getResult();
                            mLastLocation = location;
                            moveCamera(new LatLng(location.getLatitude(), location.getLongitude()),
                                    DEFAULT_ZOOM, "My Location");
                        } else {
                            Toast.makeText(MapsActivity.this, "Could not find location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException :" + e.toString());
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    mLocationCallback,
                    null);
        }

    }

    private void stopLocationUpdates() {
        if(fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void setLocationCallBackListener(){
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Log.e(TAG, "mlastlocation :" + mLastLocation);
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    Log.e(TAG, "location :" + location.getLatitude() + "/" +
                            location.getLongitude());
                    moveCamera(new LatLng(location.getLatitude(), location.getLongitude()),
                            DEFAULT_ZOOM, "My Location");
                }
            }
        };
    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            if (mAddressOutput == null) {
                mAddressOutput = "";
            }
            Toast.makeText(MapsActivity.this, mAddressOutput, Toast.LENGTH_SHORT).show();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                showToast(getString(R.string.address_found));
            }

        }
    }

    private void showToast(String msg) {
        Toast.makeText(MapsActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
