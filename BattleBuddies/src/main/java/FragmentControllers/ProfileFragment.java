package FragmentControllers;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.R;
import com.parse.starter.ViewControllers.LoginController;

import ConfigClasses.MyProfilePictureView;
import Models.User;

import static android.R.attr.background;
import static android.R.attr.name;
import static android.app.Activity.RESULT_OK;

/**
 * Created by Dylan Castanhinha on 4/12/2017.
 */

public class ProfileFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    User currentUser;
    OnRowSelected activityCallBack;
    BottomNavigationView bottomNavigationView;

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
    EditText nameTV;
    EditText locationTV;
    CheckBox trainerCheckbox;
    MyProfilePictureView profilepicture;
    Button logoutButton;
    ImageButton editOrSaveButton;
    boolean buttonState;

    public interface OnRowSelected{
        void onRowSelected(int position);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentUser = (User) ParseUser.getCurrentUser();
        //Toolbar top
        TextView titleTextView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        titleTextView.setText("Me");
        ImageButton doneButton = (ImageButton) getActivity().findViewById(R.id.toolbar_left_button);
        doneButton.setImageResource(R.drawable.ic_back_button);
        doneButton.setOnClickListener(new DoneButtonClickListener());
        //Navbar Bottom
        bottomNavigationView = (BottomNavigationView)getActivity().findViewById(R.id.bottom_navigation_navbar);
        bottomNavigationView.setVisibility(View.GONE);
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        //ProfileView
        currentUser = (User) ParseUser.getCurrentUser();
        profilepicture = (MyProfilePictureView) rootView.findViewById(R.id.profile_picture);
        nameTV = (EditText) rootView.findViewById(R.id.nameTV);
        nameTV.setFocusableInTouchMode(false);
        nameTV.setFocusable(false);
        locationTV = (EditText) rootView.findViewById(R.id.locationTV);
        locationTV.setFocusableInTouchMode(false);
        locationTV.setFocusable(false);
        logoutButton = (Button) rootView.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new LogoutButtonListener());
        editOrSaveButton = (ImageButton) rootView.findViewById(R.id.edit_or_save_button);
        editOrSaveButton.setOnClickListener(new EditOrSaveButtonClickListener());
        buttonState = true;
        setUserData();
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

    public void setUserData(){
        profilepicture.setImageBitmap(profilepicture.getRoundedBitmap(currentUser.getProfilePicture()));
        nameTV.setText(currentUser.getFullName());
        locationTV.setText(currentUser.getLocation());
    }

    private class LogoutButtonListener implements Button.OnClickListener{

        @Override
        public void onClick(View v) {
            ParseUser.getCurrentUser().logOut();
            Intent intent = new Intent(getActivity(), LoginController.class);
            startActivity(intent);
        }
    }

    public class EditOrSaveButtonClickListener implements ImageButton.OnClickListener{

        @Override
        public void onClick(View v) {
            if (buttonState) {
                editOrSaveButton.setImageResource(R.drawable.ic_done_button_white);
                buttonState = false;
                highlightFields();
            }else {
                editOrSaveButton.setImageResource(R.drawable.ic_edit_button);
                buttonState = true;
                saveNewInformation();
            }
        }

    }

    private void highlightFields() {
        nameTV.setFocusableInTouchMode(true);
        nameTV.setFocusable(true);
        nameTV.setBackgroundResource(R.color.bb_tabletRightBorderDark);

        locationTV.setFocusableInTouchMode(true);
        locationTV.setFocusable(true);
        locationTV.setBackgroundResource(R.color.bb_tabletRightBorderDark);
    }

    private void saveNewInformation() {
        nameTV.setFocusableInTouchMode(false);
        nameTV.setFocusable(false);
        nameTV.setBackgroundResource(0);

        locationTV.setFocusableInTouchMode(false);
        locationTV.setFocusable(false);
        locationTV.setBackgroundResource(0);
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

    public class DoneButtonClickListener implements ImageButton.OnClickListener{
        @Override
        public void onClick(View v) {
            getFragmentManager().popBackStack();
        }
    }

}
