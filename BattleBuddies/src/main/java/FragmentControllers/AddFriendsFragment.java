package FragmentControllers;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nex3z.togglebuttongroup.SingleSelectToggleGroup;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.R;

import java.util.Calendar;
import java.util.Date;

import Models.FollowTable;
import Models.User;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;



/**
 * Created by Dylan Castanhinha on 3/31/2017.
 */

public class AddFriendsFragment extends Fragment {

    int lengthOfTime;
    String time;
    TextView nametv;
    TextView phonetv;
    TextView detailstv;
    CircleImageView profilepictureview;
    SingleSelectToggleGroup singleSelectToggleGroup;
    User currentUser;
    User selectedUser;
    boolean currentlyFollowing;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_friends, container, false);
        getActivity().invalidateOptionsMenu();
        //Get user id from selected contact
        Bundle arguments = getArguments();
        String selectedUserid = arguments.getString("userid");
        currentUser = (User) ParseUser.getCurrentUser();
        //Toolbar top
        final TextView titleTextView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        titleTextView.setText("Provide Location To");
        ImageButton backButton = (ImageButton) getActivity().findViewById(R.id.toolbar_left_button);
        backButton.setImageResource(R.drawable.ic_back_button);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new CancelButtonListener());
        Button cancelButton = (Button) getActivity().findViewById(R.id.toolbar_left_button_text);
        cancelButton.setVisibility(View.INVISIBLE);
        Button sendButton = (Button) getActivity().findViewById(R.id.toolbar_right_button_text);
        sendButton.setVisibility(View.VISIBLE);
        sendButton.setText("Send");
        sendButton.setOnClickListener(new SendButtonClickListener());
        //Hide bottom navigation view
        BottomNavigationView bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation_navbar);
        bottomNavigationView.setVisibility(View.INVISIBLE);
        //Instantiate textviews and photo
        nametv = (TextView) rootView.findViewById(R.id.add_friend_name_tv);
        phonetv = (TextView) rootView.findViewById(R.id.add_friend_phonenumber_tv);
        detailstv = (TextView) rootView.findViewById(R.id.add_friend_details_tv);
        profilepictureview = (CircleImageView) rootView.findViewById(R.id.add_friend_photo);
        singleSelectToggleGroup = (SingleSelectToggleGroup) rootView.findViewById(R.id.group_choices);
        singleSelectToggleGroup.setOnCheckedChangeListener(new SelectToggleListener());
        lengthOfTime = 0;
        time = "1 Hour";

        getContactDetails(selectedUserid);

        return rootView;

    }

    public void getContactDetails(final String selectedUserid) {
        ParseQuery<User> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("objectId", selectedUserid);
        query.getFirstInBackground(new GetCallback<User>() {
            @Override
            public void done(User object, ParseException e) {
                if (e == null && object != null){
                    selectedUser = object;
                    nametv.setText(selectedUser.getFullName());
                    phonetv.setText("4847072605");
                    profilepictureview.setImageBitmap(selectedUser.getProfilePicture());
                    areUsersCurrentlyFollowing();
                } else {
                    Log.i("AppInfo", e.getMessage());
                }
            }
        });

    }

    public void areUsersCurrentlyFollowing(){
        ParseQuery<FollowTable> query = ParseQuery.getQuery(FollowTable.class);
        query.whereEqualTo("following", selectedUser);
        query.whereEqualTo("isFollowed", currentUser);
        query.getFirstInBackground(new GetCallback<FollowTable>() {
            @Override
            public void done(FollowTable object, ParseException e) {
                if (object != null && e == null){
                    Log.i("AppInfo", "Currently Following: ");
                    currentlyFollowing = true;
                } else{
                    Log.i("AppInfo", "Not following");
                    currentlyFollowing = false;
                }
            }
        });
    }

    private class SendButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View v) {

            if (currentlyFollowing) {
                new SweetAlertDialog(getActivity())
                        .setTitleText(selectedUser.getFirstName() + " is already following you.")
                        .setConfirmText("Okay")
                        .show();
            }else {
                new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Share Location With " + selectedUser.getFirstName())
                        .setContentText("For: " + time)
                        .setCancelText("Cancel")
                        .setConfirmText("Send")
                        .showCancelButton(true)
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(final SweetAlertDialog sweetAlertDialog) {
                                if (selectedUser != null) {
                                    if (currentlyFollowing == false) {
                                        FollowTable followTable = new FollowTable();
                                        followTable.setFollowing(selectedUser);
                                        followTable.setIsFollowed(currentUser);
                                        followTable.setRequestConfirmed(false);
                                        ParseACL acl = new ParseACL();
                                        acl.setPublicReadAccess(true);
                                        acl.setPublicWriteAccess(true);
                                        followTable.setACL(acl);
                                        switch (lengthOfTime) {
                                            case 0:
                                                Calendar cal = Calendar.getInstance(); // creates calendar
                                                cal.setTime(new Date()); // sets calendar time/date
                                                cal.add(Calendar.HOUR_OF_DAY, 1); // adds one hour
                                                followTable.setExpirationDate(cal.getTime());
                                                break;
                                            case 1:
                                                Calendar cal2 = Calendar.getInstance(); // creates calendar
                                                cal2.setTime(new Date()); // sets calendar time/date
                                                cal2.add(Calendar.HOUR_OF_DAY, 4); // adds 4 hour
                                                followTable.setExpirationDate(cal2.getTime());
                                                break;
                                            case 2:
                                                Calendar cal3 = Calendar.getInstance(); // creates calendar
                                                cal3.setTime(new Date()); // sets calendar time/date
                                                cal3.add(Calendar.HOUR_OF_DAY, 24); // adds 1 day
                                                followTable.setExpirationDate(cal3.getTime());
                                                break;
                                            case 3:
                                                break;
                                        }
                                        followTable.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    //Popback twice to go to home screen
                                                    sweetAlertDialog.cancel();
                                                    getFragmentManager().popBackStack();
                                                    getFragmentManager().popBackStack();
                                                } else {
                                                    Log.i("AppInfo", e.getMessage());
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    new SweetAlertDialog(getActivity())
                                            .setTitleText("Select a user.")
                                            .setConfirmText("Okay")
                                            .show();
                                }
                            }
                        })
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.cancel();
                            }
                        })
                        .show();
            }
        }
    }

    private class CancelButtonListener implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            getFragmentManager().popBackStack();
        }
    }

    private class SelectToggleListener implements SingleSelectToggleGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(SingleSelectToggleGroup group, int checkedId) {
            Log.i("AppInfo", String.valueOf(checkedId));
            switch (checkedId) {
                case 2131755336:
                    //1 Hour
                    lengthOfTime = 0;
                    time = "1 Hour";
                    Log.i("AppInfo", time);
                    break;
                case 2131755337:
                    //4 Hours
                    lengthOfTime = 1;
                    time = "4 Hours";
                    Log.i("AppInfo", time);
                    break;
                case 2131755338:
                    //1 Day
                    lengthOfTime = 2;
                    time = "1 Day";
                    Log.i("AppInfo", time);
                    break;
                case 2131755339:
                    //Emergency Contact
                    lengthOfTime = 3;
                    time = "Indefinitely";
                    Log.i("AppInfo", time);
                    break;
            }
        }
    }
}
