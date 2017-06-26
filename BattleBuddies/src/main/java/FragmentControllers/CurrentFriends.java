package FragmentControllers;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.starter.R;

import java.io.IOException;
import java.util.List;

import ConfigClasses.MyProfilePictureView;
import ConfigClasses.ParseAdapterCustomList;
import Models.User;

import static android.app.Activity.RESULT_OK;

public class CurrentFriends extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener, LocationListener {

    //MapView
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private boolean mLocationUpdateState;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    MapView mMapView;

    //ListView
    TextView labelTV;
    ListView listview;
    CurrentClients adapter;
    User currentUser;
    SwipeRefreshLayout swipeContainer;
    OnAddNewUserButtonClicked activityCallback;
    AddFriends.OnUserSelected activityCallBack;

    public interface OnAddNewUserButtonClicked {
        void onAddUserClicked();
    }
    public interface OnUserSelected {
        void onUserSelected(String userId);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            activityCallback = (OnAddNewUserButtonClicked) context;
            activityCallBack = (AddFriends.OnUserSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentUser = (User) ParseUser.getCurrentUser();
        //Toolbar top
        final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.custom_toolbar);
        TextView titleTextView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        titleTextView.setText(currentUser.getFirstName() + "'s "+"Clients");
        ImageButton addUserButton = (ImageButton) getActivity().findViewById(R.id.toolbar_left_button);
        addUserButton.setImageResource(R.drawable.ic_add_user_green);
        addUserButton.setOnClickListener(new AddNewClientButtonListener());
        //ListView
        View rootView = inflater.inflate(R.layout.fragment_current_clients, container, false);
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        listview = (ListView) rootView.findViewById(R.id.current_client_list_view);
        adapter = new CurrentClients(getActivity());
        listview.setAdapter(adapter);
        swipeContainer.setOnRefreshListener(new SwipeToRefresh());
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_green_light);
        //MapView
        mMapView = (MapView) rootView.findViewById(R.id.mapViewFragment);
        mMapView.onCreate(savedInstanceState);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                mMap.getUiSettings().setZoomControlsEnabled(true);

                // For dropping a marker at a point on the Map
                LatLng currentUserLocation = new LatLng(currentUser.getGeopoint().getLatitude(), currentUser.getGeopoint().getLongitude());

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(currentUserLocation).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        createLocationRequest();
        return rootView;
    }

    //MAPVIEW METHODS

    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                    {android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        googleMap.setMyLocationEnabled(true);

        LocationAvailability locationAvailability =
                LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);
        if (null != locationAvailability && locationAvailability.isLocationAvailable()) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                LatLng currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation
                        .getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom
                        (currentLocation, 12));
            }
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop(){
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() ){
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        setUpMap();
        if (mLocationUpdateState) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (null != mLastLocation) {
            ParseGeoPoint point = new ParseGeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            currentUser.setGeopoint(point);
            currentUser.saveInBackground();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private String getAddress( LatLng latLng ) {
        Geocoder geocoder = new Geocoder( getActivity() );
        String addressText = "";
        List<Address> addresses = null;
        Address address = null;
        try {
            addresses = geocoder.getFromLocation( latLng.latitude, latLng.longitude, 1 );
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses.get(0);
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressText += (i == 0)?address.getAddressLine(i):("\n" + address.getAddressLine(i));
                }
            }
        } catch (IOException e ) {
        }
        return addressText;
    }

    protected void placeMarkerOnMap(LatLng location) {
        MarkerOptions markerOptions = new MarkerOptions().position(location);
        String titleStr = getAddress(location);  // add these two lines
        markerOptions.title(titleStr);
        googleMap.addMarker(markerOptions);
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                this);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        mLocationUpdateState = true;
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                mLocationUpdateState = true;
                startLocationUpdates();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mLocationUpdateState) {
            startLocationUpdates();
        }
    }

    //LISTVIEW METHODS

    private class SwipeToRefresh implements SwipeRefreshLayout.OnRefreshListener{

        @Override
        public void onRefresh() {
            adapter.loadObjects();
        }
    }

    private class AddNewClientButtonListener implements ImageButton.OnClickListener{
        @Override
        public void onClick(View v) {
            activityCallback.onAddUserClicked();
        }
    }


    private class CurrentClients extends ParseAdapterCustomList implements ParseQueryAdapter.OnQueryLoadListener {
        Context context;
        private CurrentClients(final Context context){
            super(context, new ParseQueryAdapter.QueryFactory<User>(){
               public ParseQuery<User> create() {
                   ParseRelation<User> relation = currentUser.getRelation("client");
                   ParseQuery<User> query = relation.getQuery();
                   query.whereEqualTo("objectId", false);
                   query.whereNotEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
                   return query;
               }
            });
            addOnQueryLoadListener(this);
        }


        @Override
        public void onLoading() {
            swipeContainer.setRefreshing(true);
            Log.i("AppInfo", "Loading");
        }

        @Override
        public void onLoaded(List objects, Exception e) {
            swipeContainer.setRefreshing(false);
            Log.i("AppInfo", "Loaded");
        }


        @Override
        public View getItemView(final User user, View v, ViewGroup parent){
            if (v == null){
                v = View.inflate(getContext(), R.layout.list_layout_current_clients, null);
            }
            super.getItemView(user, v, parent);

            //Add the title view
            TextView nameTextView = (TextView) v.findViewById(R.id.current_client_text_view_name);
            nameTextView.setText(user.getFullName());

            //Add the objectid
            TextView objectId = (TextView) v.findViewById(R.id.current_client_object_id);
            objectId.setText(user.getLocation());

            //Add the image
            MyProfilePictureView imageView = (MyProfilePictureView) v.findViewById(R.id.imageView3);
            imageView.setImageBitmap(imageView.getRoundedBitmap(user.getProfilePicture()));

            //On click listener for selection
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activityCallBack.onUserSelected(user.getObjectId());
                }
            });
            return v;
        }
    }
}

