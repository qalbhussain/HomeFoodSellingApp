package com.infinity.homefoodsellingapp.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.infinity.homefoodsellingapp.R;

public class NearbyChef extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //Request codes
    private final static int PERMISSION_LOCATION_REQUEST = 0xc8;
    private final static int LOCATION_SETTING_REQUEST_CODE = 0x1;

    //GoogleMap object to show map on fragment
    GoogleMap mGoogleMap;


    //GoogleApiClient to access GooglePlayServices Api's
    GoogleApiClient mGoogleApiClient;

    //Request user's location
    LocationRequest mLocationRequest;

    //only one marker will be visible on user current location
    Marker marker ;

    //circle radius object
    Circle circleRadius;




    //---------Fragment LifeCycle Methods--------------//

    //--this method is called when fragment view/user_interface is created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_nearby_chef, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //check & display the activity layout if google play services is available
        if (isGoogleServiceAvailable()) {
            Toast.makeText(getActivity(), "Connected to Google Play Services", Toast.LENGTH_SHORT).show();
            initMap();
        }
    }

    //------------------------------------------------//



    //initialize map fragment to display Google map
    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapfragment);
        mapFragment.getMapAsync(this);
    }


    /*--check whether or not google service is available-
    * if available than return true. Else check whether it is installed or not.
    * return false
    * */
    private boolean isGoogleServiceAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance(); //get google api instance
        int isGoogleServiceAvailable = googleApiAvailability.isGooglePlayServicesAvailable(getActivity());
        if (isGoogleServiceAvailable == ConnectionResult.SUCCESS) {
            return true; //return true if its available
        } else if (googleApiAvailability.isUserResolvableError(isGoogleServiceAvailable)) {
            Dialog dialog = googleApiAvailability.getErrorDialog(getActivity(), isGoogleServiceAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(getActivity(), "Cannot connect to services", Toast.LENGTH_SHORT).show();
        }

        return false;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(getContext(), "onMapReady", Toast.LENGTH_SHORT).show();
        mGoogleMap = googleMap;


        //--build the google api client
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //--connect GoogleApiClient to GooglePlayServices when app start
        mGoogleApiClient.connect();

    }


    //--Google api client connection callbacks
    //--goto user's locations when connection established
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(getContext(), "onConnected", Toast.LENGTH_SHORT).show();
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);

        //check whether location is enabled on user's device
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> results = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        results.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                final LocationSettingsStates states = locationSettingsResult.getLocationSettingsStates();

                switch (status.getStatusCode()){
                    case LocationSettingsStatusCodes.SUCCESS : //everything is fine, we're good to go. show user current location
                        getUserCurrentLocation();
                        break ;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED: //location is not enabled. Display a dialog to user
                        try {
                            status.startResolutionForResult(getActivity(), LOCATION_SETTING_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break ;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                    default:
                        break;
                }


            }
        });

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    //--display current location and show on map
    public void getUserCurrentLocation() {
        //--show user current location if permissions are granted
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {

            //--if the android api version is greater than 21
            //--ask user to give permissions
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQUEST);
            }
        }
    }



    //--check whether user give access to permissions
    // if not disable funtionality that depends on these permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getContext(), "LocationServies.FusedLocationApi called", Toast.LENGTH_SHORT).show();
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                    }

                }
                else{
                    Toast.makeText(getContext(), "Permission not granted", Toast.LENGTH_SHORT).show();
                }
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    //get the user current location and update map when location changes
    //location listener
    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getContext(), "onLocationChanged", Toast.LENGTH_SHORT).show();
        //get the location object
        if (location == null) {
            Toast.makeText(getContext(), "Can not access current location", Toast.LENGTH_SHORT).show();
        } else {
            LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 15);
            mGoogleMap.animateCamera(cameraUpdate);
            addMarkerToUserLocation(latlng);
        }

    }

    //add marker to user current location
    private void addMarkerToUserLocation(LatLng latlng) {
        //remove any previous added marker on GoogleMap
        if (marker != null){
            removePreviousMarkerWithRadius();
        }

        //add marker to user current location
        MarkerOptions markerOption = new MarkerOptions()
                .title("Current Location")
                .snippet("Qalb Hussain's home")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .position(latlng);

        marker = mGoogleMap.addMarker(markerOption);
        circleRadius = addRadiusToCurrentLocation(latlng);
    }

    //--this function will remove any previous marker added to google map
    //--with circular radius
    private void removePreviousMarkerWithRadius() {
        //--remove the marker from google map and set it equals to null
        marker.remove();
        marker = null;
        //--remove the radius from google map and set it equals to null
        circleRadius.remove();
        circleRadius = null;
    }


    //--this will add a radius of few kilometers to user's locations
    //-- the latlng parameter is just for getting user's locations
    private Circle addRadiusToCurrentLocation(LatLng latlng) {
        CircleOptions circleOption = new CircleOptions()
                .fillColor(R.color.colorPrimaryDark)
                .radius(2000)
                .center(latlng);

        return mGoogleMap.addCircle(circleOption);
    }


    //---------------ACTIVITY FUNCTIONS----------------//

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case LOCATION_SETTING_REQUEST_CODE :
                switch (resultCode)
                {
                    case Activity.RESULT_OK :
                        //All changes were made successfully
                        //get user current location
                        getUserCurrentLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        //user didnot change setting
                        Toast.makeText(getContext(), "Cannot get you current location. Please enable your device location", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                break;

            default:
                Toast.makeText(getContext(), "Location setting request code does not matched", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()){
            Toast.makeText(getContext(), "onStop: GoogleApiClient Disconnect", Toast.LENGTH_SHORT).show();
            mGoogleApiClient.disconnect();
        }
    }
}
