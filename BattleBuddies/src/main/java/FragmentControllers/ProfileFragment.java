package FragmentControllers;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.R;
import com.parse.starter.ViewControllers.LoginController;

import java.util.ArrayList;
import java.util.List;

import ConfigClasses.MyProfilePictureView;
import ConfigClasses.ParseAdapterCustomList;
import Models.User;

import static android.app.Activity.RESULT_OK;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.parse.starter.R.id.swipeContainer;

/**
 * Created by Dylan Castanhinha on 4/12/2017.
 */

public class ProfileFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    User currentUser;
    BottomNavigationView bottomNavigationView;
    OnRowSelected activityCallBack;

    //MapView
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private boolean mLocationUpdateState;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    MapView mMapView;

    //ProfileView
    ListView listview;
    MyProfilePictureView profilepicture;
    CurrentDetailsAdapter adapter;
    Button logoutButton;
    boolean buttonState;

    public interface OnRowSelected{
        void onRowSelected(int position);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityCallBack = (OnRowSelected) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentUser = (User) ParseUser.getCurrentUser();
        //Toolbar top
        TextView titleTextView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        titleTextView.setText("Edit Profile");
        Button backButton = (Button) getActivity().findViewById(R.id.toolbar_left_button_text);
        backButton.setText("Cancel");
        backButton.setOnClickListener(new BackButtonClickListener());
        Button doneButton = (Button) getActivity().findViewById(R.id.toolbar_right_button_text);
        doneButton.setText("Done");
        doneButton.setOnClickListener(new DoneButtonClickListener());
        ImageButton leftButton = (ImageButton) getActivity().findViewById(R.id.toolbar_left_button);
        leftButton.setImageResource(0);
        leftButton.setClickable(false);
        //Navbar Bottom
        bottomNavigationView = (BottomNavigationView)getActivity().findViewById(R.id.bottom_navigation_navbar);
        bottomNavigationView.setVisibility(View.GONE);
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        //ProfileView
        currentUser = (User) ParseUser.getCurrentUser();
        profilepicture = (MyProfilePictureView) rootView.findViewById(R.id.profile_picture);
        logoutButton = (Button) rootView.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new LogoutButtonListener());
        buttonState = true;
        profilepicture.setImageBitmap(profilepicture.getRoundedBitmap(currentUser.getProfilePicture()));
        //User Details List View
        ArrayList<User> users = new ArrayList<User>();
        listview = (ListView) rootView.findViewById(R.id.profile_details_list_view);
        adapter = new CurrentDetailsAdapter(getActivity().getApplicationContext(), users);
        listview.setAdapter(adapter);
        for (int i = 0; i < 2; i++){
            adapter.add(currentUser);
        }
        //MapView
        mMapView = (MapView) rootView.findViewById(R.id.profileMapViewFragment);
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
                googleMap.getUiSettings().setAllGesturesEnabled(false);
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

    private class LogoutButtonListener implements Button.OnClickListener{

        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_disclaimer)
                    .setTitle("Log Out?")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            currentUser.logOut();
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }


    public class CurrentDetailsAdapter extends ArrayAdapter<User>{
        public CurrentDetailsAdapter(Context context, ArrayList<User> users){
            super(context,0, users);
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            User user = getItem(position);

            if (convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_layout_profile_details, parent, false);
            }
            final EditText details = (EditText) convertView.findViewById(R.id.profile_details_text_view);
            ImageButton button = (ImageButton) convertView.findViewById(R.id.profile_details_image_button);
            switch(position){
                case 0: {
                    details.setText(user.getFullName());
                    button.setImageResource(R.drawable.ic_user_buttpn);
                    break;
                }
                case 1: {
                    details.setText(user.getLocation());
                    button.setImageResource(R.drawable.ic_user_location);
                    break;
                }
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activityCallBack.onRowSelected(position);
                }
            });
            return convertView;
        }
    }

    //MapView Methods
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
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mLocationUpdateState) {
            startLocationUpdates();
        }
    }

    //ListView

    public class BackButtonClickListener implements ImageButton.OnClickListener{
        @Override
        public void onClick(View v) {
            getFragmentManager().popBackStack();
        }
    }

    public class DoneButtonClickListener implements ImageButton.OnClickListener{
        @Override
        public void onClick(View v) {

        }
    }


}
