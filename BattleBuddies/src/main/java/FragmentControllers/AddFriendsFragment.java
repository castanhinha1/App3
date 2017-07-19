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
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.R;
import com.terrakok.phonematter.PhoneFormat;

import java.io.IOException;
import java.util.List;

import ConfigClasses.MyProfilePictureView;
import Models.User;

import static android.R.attr.format;


/**
 * Created by Dylan Castanhinha on 3/31/2017.
 */

public class AddFriendsFragment extends Fragment {

    private static final int REQUEST_CONTACT = 3007;
    int lengthOfTime;
    TextView nametv;
    TextView phonetv;
    TextView detailstv;
    MyProfilePictureView profilepictureview;
    SingleSelectToggleGroup singleSelectToggleGroup;
    Contact selectedContact;
    User currentUser;
    User selectedUser;
    String phonenumber;
    PhoneFormat phoneFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_friends, container, false);
        getActivity().invalidateOptionsMenu();
        currentUser = (User) ParseUser.getCurrentUser();
        phoneFormat = new PhoneFormat("us", getContext());
        //Toolbar top
        final TextView titleTextView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        titleTextView.setText("Provide Location");
        ImageButton backButton = (ImageButton) getActivity().findViewById(R.id.toolbar_left_button);
        backButton.setVisibility(View.INVISIBLE);
        Button cancelButton = (Button) getActivity().findViewById(R.id.toolbar_left_button_text);
        cancelButton.setText("Cancel");
        cancelButton.setOnClickListener(new CancelButtonListener());
        Button sendButton = (Button) getActivity().findViewById(R.id.toolbar_right_button_text);
        sendButton.setText("Send");
        sendButton.setOnClickListener(new SendButtonClickListener());
        //Hide bottom navigation view
        BottomNavigationView bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation_navbar);
        bottomNavigationView.setVisibility(View.INVISIBLE);
        //Instantiate textviews and photo
        nametv = (TextView) rootView.findViewById(R.id.add_friend_name_tv);
        phonetv = (TextView) rootView.findViewById(R.id.add_friend_phonenumber_tv);
        detailstv = (TextView) rootView.findViewById(R.id.add_friend_details_tv);
        profilepictureview = (MyProfilePictureView) rootView.findViewById(R.id.add_friend_photo);
        singleSelectToggleGroup = (SingleSelectToggleGroup) rootView.findViewById(R.id.group_choices);
        singleSelectToggleGroup.setOnCheckedChangeListener(new SelectToggleListener());

        askForContactPermission();

        return rootView;

    }

    public void getContact(){

        Intent intent = new Intent(getActivity(), ContactPickerActivity.class)
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_BADGE_TYPE, ContactPictureType.ROUND.name())
                .putExtra(ContactPickerActivity.EXTRA_SELECT_CONTACTS_LIMIT, 1)
                .putExtra(ContactPickerActivity.EXTRA_LIMIT_REACHED_MESSAGE, "You can't pick more than 1 contact!")
                .putExtra(ContactPickerActivity.EXTRA_ONLY_CONTACTS_WITH_PHONE, true)
                .putExtra(ContactPickerActivity.EXTRA_SHOW_CHECK_ALL, false)
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION, ContactDescription.ADDRESS.name())
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                .putExtra(ContactPickerActivity.EXTRA_CONTACT_SORT_ORDER, ContactSortOrder.AUTOMATIC.name());

            startActivityForResult(intent, REQUEST_CONTACT);
    }

    public void setContactInView(Contact contact) throws IOException {
        if (verifyContactUsesApp(contact)){
            //Load views with information from database and get query on user
            //callSelectedUser();
        } else if (verifyContactUsesApp(contact) == false){
            //Load views with information from contact
            nametv.setText(contact.getDisplayName());
            detailstv.setText("Something");
            if (contact.getDisplayName() != null){
                phonetv.setText(contact.getPhone(ContactsContract.CommonDataKinds.Phone.TYPE_MAIN));
            } else {
                phonetv.setText("Contact Name");
            }
            //Convert uri to bitmap
            if (contact.getPhotoUri() != null) {
                Uri imageUri = contact.getPhotoUri();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                profilepictureview.setImageBitmap(profilepictureview.getRoundedBitmap(bitmap));
            } else {
                profilepictureview.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
            }
        }
    }

    public boolean verifyContactUsesApp(Contact contact){
        boolean isUser;
        //return isUser;
        return false;
    }

    public boolean checkIfCurrentlyFollowing(Contact contact){
        boolean isFollowing;
        return false;
    }

    public User callSelectedUser(){
        String number = selectedContact.getPhone(ContactsContract.CommonDataKinds.Phone.TYPE_MAIN);
        String formattedNumber = phoneFormat.format(number);
        Log.i("AppInfo", "Formatted phone number" +formattedNumber);
        //Query to find user with that phone number
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("phonenumber", formattedNumber);
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser object, ParseException e) {
                if (e == null && object != null){
                    selectedUser = (User) object;
                } else {
                    Log.i("AppInfo", e.getMessage());
                }
            }
        });
        return selectedUser;
    }

    private class SendButtonClickListener implements Button.OnClickListener{

        @Override
        public void onClick(View v) {
            if (/*verifyContactUsesApp(selectedContact)*/ true &&  !(checkIfCurrentlyFollowing(selectedContact))) {
                ParseRelation<User> relation = currentUser.getRelation("isFollowed");
                relation.add(callSelectedUser());
                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null){
                            Log.i("AppInfo", "Relation saved!");
                        } else {
                            Log.i("AppInfo", e.getMessage());
                        }
                    }
                });
            } else{
                //Send link for user to use app
            }
        }
    }

    private class CancelButtonListener implements Button.OnClickListener{
        @Override
        public void onClick(View v) {
            getFragmentManager().popBackStack();
        }
    }

    private class SelectToggleListener implements SingleSelectToggleGroup.OnCheckedChangeListener{

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONTACT && resultCode == Activity.RESULT_OK &&
                data != null && data.hasExtra(ContactPickerActivity.RESULT_CONTACT_DATA)) {

            // we got a result from the contact picker

            // process contacts
            List<Contact> contacts = (List<Contact>) data.getSerializableExtra(ContactPickerActivity.RESULT_CONTACT_DATA);
            for (Contact contact : contacts) {
                // process the contacts...
                try {
                    setContactInView(contact);
                    selectedContact = contact;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // process groups
            List<Group> groups = (List<Group>) data.getSerializableExtra(ContactPickerActivity.RESULT_GROUP_DATA);
            for (Group group : groups) {
                // process the groups...
            }
        }
    }

    //Permissions request for contacts
    public void askForContactPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.READ_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Contacts access needed");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("please confirm Contacts access");//TODO put real question
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(
                                    new String[]
                                            {Manifest.permission.READ_CONTACTS}
                                    , REQUEST_CONTACT);
                        }
                    });
                    builder.show();
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_CONTACTS},
                            REQUEST_CONTACT);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }else{
                getContact();
            }
        }
        else{
            getContact();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CONTACT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContact();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
