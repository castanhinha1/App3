package FragmentControllers;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.R;
import com.parse.starter.ViewControllers.LoginController;

import ConfigClasses.MyProfilePictureView;
import Models.User;

public class YourProfileFragment extends Fragment {

    TextView nameTV;
    TextView locationTV;
    CheckBox trainerCheckbox;
    User currentUser;
    MyProfilePictureView profilepicture;
    Button logoutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_your_profile, container, false);
        //Find variable references
        currentUser = (User) ParseUser.getCurrentUser();
        TextView titleTextView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        titleTextView.setText("Your Profile");
        profilepicture = (MyProfilePictureView) rootView.findViewById(R.id.profile_picture);
        nameTV = (TextView) rootView.findViewById(R.id.nameTV);
        locationTV = (TextView) rootView.findViewById(R.id.locationTV);
        logoutButton = (Button) rootView.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new LogoutButtonListener());
        trainerCheckbox = (CheckBox) rootView.findViewById(R.id.trainerCheckBox);
        trainerCheckbox.setOnCheckedChangeListener(new TrainerCheckBoxListener());
        setUserData();
        return rootView;
    }

    public void setUserData(){
        profilepicture.setImageBitmap(profilepicture.getRoundedBitmap(currentUser.getProfilePicture()));
        nameTV.setText(currentUser.getFullName());
        locationTV.setText(currentUser.getLocation());
        //Trainer or Not
        if (currentUser.getTrainerStatus()){
            trainerCheckbox.setChecked(true);
        } else {
            trainerCheckbox.setChecked(false);
        }
    }

    private class LogoutButtonListener implements Button.OnClickListener{

        @Override
        public void onClick(View v) {
            ParseUser.getCurrentUser().logOut();
            Intent intent = new Intent(getActivity(), LoginController.class);
            startActivity(intent);
        }
    }

    private class TrainerCheckBoxListener implements CheckBox.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                currentUser.setTrainerStatus(true);
                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                        } else {
                            Log.i("AppInfo", e.getMessage());
                        }
                    }
                });
            } else {
                currentUser.setTrainerStatus(false);
                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                        } else {
                            Log.i("AppInfo", e.getMessage());
                        }
                    }
                });
            }
        }
    }
}
