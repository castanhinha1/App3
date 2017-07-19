package FragmentControllers;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nex3z.togglebuttongroup.SingleSelectToggleGroup;
import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.contact.ContactDescription;
import com.onegravity.contactpicker.contact.ContactSortOrder;
import com.onegravity.contactpicker.core.ContactPickerActivity;
import com.onegravity.contactpicker.group.Group;
import com.onegravity.contactpicker.picture.ContactPictureType;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.R;
import com.terrakok.phonematter.PhoneFormat;

import java.io.IOException;
import java.util.List;

import ConfigClasses.MyProfilePictureView;
import ConfigClasses.ParseAdapterCustomList;
import Models.User;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.format;


/**
 * Created by Dylan Castanhinha on 3/31/2017.
 */

public class AddFriendsFragment extends Fragment {

    int lengthOfTime;
    TextView nametv;
    TextView phonetv;
    TextView detailstv;
    CircleImageView profilepictureview;
    SingleSelectToggleGroup singleSelectToggleGroup;
    User currentUser;
    User selectedUser;



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
        backButton.setVisibility(View.INVISIBLE);
        Button cancelButton = (Button) getActivity().findViewById(R.id.toolbar_left_button_text);
        cancelButton.setText("Cancel");
        cancelButton.setOnClickListener(new CancelButtonListener());
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
                } else {
                    Log.i("AppInfo", e.getMessage());
                }
            }
        });

    }

    private class SendButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            if (selectedUser != null) {
                ParseRelation<User> relation = currentUser.getRelation("following");
                relation.add(selectedUser);
                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null){
                            Log.i("AppInfo", "user relation saved");
                        } else {
                            Log.i("AppInfo", e.getMessage());
                        }
                    }
                });
            } else {
                getFragmentManager().popBackStack();
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
            switch (checkedId) {
                case 2131755320:
                    //1 Hour
                    lengthOfTime = 0;
                    break;
                case 2131755321:
                    //4 Hours
                    lengthOfTime = 1;
                    break;
                case 2131755322:
                    //1 Day
                    lengthOfTime = 2;
                    break;
                case 2131755323:
                    //Emergency Contact
                    lengthOfTime = 3;
                    break;
            }
        }
    }
}
