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
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.starter.R;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.io.IOException;
import java.util.List;

import ConfigClasses.MyProfilePictureView;
import ConfigClasses.ParseAdapterCustomList;
import Models.User;

import static android.app.Activity.RESULT_OK;

public class CurrentFriendsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener, LocationListener {

    //MapView
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private boolean mLocationUpdateState;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    MapView mMapView;

    OnAddNewUserButtonClicked activityCallback;
    OnProfileButtonClicked activityCallback2;
    AddFriendsFragment.OnUserSelected activityCallBack;
    ExpandableLayout expandableLayoutTop;
    ExpandableLayout expandableLayoutBottom;

    //ListView Friends sharing location with you
    ListView listview;
    CurrentClients adapter;
    User currentUser;
    SwipeRefreshLayout swipeContainer;

    //ListView Friends that you are sharing location with
    ListView listview2;
    CurrentClients adapter2;
    SwipeRefreshLayout swipeContainer2;

    //Toolbar
    Toolbar toolbar;
    ImageButton leftToolbarButton;
    ImageButton rightToolbarbutton;
    //NavBar
    BottomNavigationView bottomNavigationView;
    MyProfilePictureView myProfilePictureView;
    TextView nameLabel;
    TextView locationLabel;


    public interface OnAddNewUserButtonClicked {
        void onAddUserClicked();
    }

    public interface OnProfileButtonClicked {
        void onProfileButtonClicked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            activityCallback = (OnAddNewUserButtonClicked) context;
            activityCallback2 = (OnProfileButtonClicked) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentUser = (User) ParseUser.getCurrentUser();
        //Toolbar top
        toolbar = (Toolbar) getActivity().findViewById(R.id.custom_toolbar);
        Button backButton = (Button) getActivity().findViewById(R.id.toolbar_left_button_text);
        backButton.setText("");
        backButton.setClickable(false);
        Button doneButton = (Button) getActivity().findViewById(R.id.toolbar_right_button_text);
        doneButton.setText("");
        doneButton.setClickable(false);
        TextView titleTextView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        titleTextView.setText(currentUser.getFirstName() + "'s "+"Buddies");
        leftToolbarButton = (ImageButton) getActivity().findViewById(R.id.toolbar_left_button);
        leftToolbarButton.setImageResource(R.drawable.ic_add_user_green);
        leftToolbarButton.setOnClickListener(new AddNewClientButtonListener());
        rightToolbarbutton = (ImageButton) getActivity().findViewById(R.id.toolbar_right_button);
        rightToolbarbutton.setImageResource(R.drawable.ic_action_action_search);
        rightToolbarbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialSearchView searchView = new MaterialSearchView(getActivity());
                searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
                    @Override
                    public void onSearchViewShown() {

                    }

                    @Override
                    public void onSearchViewClosed() {

                    }
                });

                searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });
            }
        });
        //ListView Friend Sharing Location with You
        View rootView = inflater.inflate(R.layout.fragment_current_friends, container, false);
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        TextView textView = new TextView(getActivity());
        textView.setText("Friends Sharing Location With You");
        listview = (ListView) rootView.findViewById(R.id.current_client_list_view);
        listview.addHeaderView(textView);
        expandableLayoutTop = (ExpandableLayout) rootView.findViewById(R.id.expandable_layout_top);
        expandableLayoutBottom = (ExpandableLayout) rootView.findViewById(R.id.expandable_layout_bottom);
        adapter = new CurrentClients(getActivity());
        listview.setAdapter(adapter);
        swipeContainer.setOnRefreshListener(new SwipeToRefresh());

        //ListView Friends you are sharing location with
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        TextView textView2 = new TextView(getActivity());
        textView2.setText("Friends With Your Location");
        listview2 = (ListView) rootView.findViewById(R.id.friends_with_your_location_list_view);
        listview2.addHeaderView(textView2);
        adapter = new CurrentClients(getActivity());
        listview2.setAdapter(adapter);
        swipeContainer.setOnRefreshListener(new SwipeToRefresh());


        //NavBar
        bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation_navbar);
        bottomNavigationView.setOnClickListener(new BottomNavClickListener());
        myProfilePictureView = (MyProfilePictureView) getActivity().findViewById(R.id.profile_picture_navbar);
        nameLabel = (TextView) getActivity().findViewById(R.id.nameLabel);
        locationLabel = (TextView) getActivity().findViewById(R.id.locationLabel);
        myProfilePictureView.setImageBitmap(myProfilePictureView.getRoundedBitmap(currentUser.getProfilePicture()));
        nameLabel.setText(currentUser.getFullName());
        locationLabel.setText(currentUser.getLocation());

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

        public String calculateDistance(User user) {
            double distance = Math.round(user.getGeopoint().distanceInMilesTo(currentUser.getGeopoint()));
            String distanceInMiles = String.valueOf(distance);
            return distanceInMiles;
        }


        @Override
        public View getItemView(final User user, View v, ViewGroup parent){
            if (v == null){
                v = View.inflate(getContext(), R.layout.list_layout_current_friends, null);
            }
            super.getItemView(user, v, parent);

            //Add the title view
            TextView nameTextView = (TextView) v.findViewById(R.id.current_client_text_view_name);
            nameTextView.setText(user.getFullName());

            //Add the Location label
            TextView location = (TextView) v.findViewById(R.id.current_client_object_id);
            location.setText(user.getLocation());

            //Add the distance label
            TextView distanceLabel = (TextView) v.findViewById(R.id.distanceLabel);
            distanceLabel.setText(calculateDistance(user)+" miles");

            //Add the image
            MyProfilePictureView imageView = (MyProfilePictureView) v.findViewById(R.id.imageView3);
            imageView.setImageBitmap(imageView.getRoundedBitmap(user.getProfilePicture()));

            //On click listener for selection
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    placeMarkerOnMap(user);
                }
            });
            return v;
        }
    }


    private class BottomNavClickListener implements BottomNavigationView.OnClickListener{

        @Override
        public void onClick(View v) {
            activityCallback2.onProfileButtonClicked();
        }
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
        googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                leftToolbarButton.setImageResource(R.drawable.ic_back_button);
                leftToolbarButton.setOnClickListener(new BackButtonListener());
                expandableLayoutBottom.collapse();
            }
        });
    }

    private class BackButtonListener implements ImageButton.OnClickListener{
        @Override
        public void onClick(View v) {
            expandableLayoutBottom.expand();
            leftToolbarButton.setImageResource(R.drawable.ic_add_user_green);
            leftToolbarButton.setOnClickListener(new AddNewClientButtonListener());
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

    public void placeMarkerOnMap(User user) {
        LatLng latLng = user.getLatLng();
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        String titleStr = user.getFullName();  // add these two lines
        markerOptions.title(titleStr);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(user.getProfilePicture()));
        googleMap.addMarker(markerOptions);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom
                (latLng, 12));

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
        bottomNavigationView.setVisibility(View.VISIBLE);
        if (mGoogleApiClient.isConnected() && !mLocationUpdateState) {
            startLocationUpdates();
        }
    }
}

