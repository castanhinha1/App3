package FragmentControllers;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.contact.ContactDescription;
import com.onegravity.contactpicker.contact.ContactSortOrder;
import com.onegravity.contactpicker.core.ContactPickerActivity;
import com.onegravity.contactpicker.group.Group;
import com.onegravity.contactpicker.picture.ContactPictureType;
import com.parse.starter.R;

import java.io.IOException;
import java.util.List;

import ConfigClasses.MyProfilePictureView;

import static com.parse.starter.R.style.AppTheme;


/**
 * Created by Dylan Castanhinha on 3/31/2017.
 */

public class AddFriendsFragment extends Fragment {

    private static final int REQUEST_CONTACT = 3007;
    TextView nametv;
    TextView phonetv;
    TextView detailstv;
    MyProfilePictureView profilepictureview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_friends, container, false);
        getActivity().invalidateOptionsMenu();
        //Toolbar top
        final TextView titleTextView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        titleTextView.setText("Local Clients");
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
        //Instanstiate textviews and photo
        nametv = (TextView) rootView.findViewById(R.id.add_friend_name_tv);
        phonetv = (TextView) rootView.findViewById(R.id.add_friend_phonenumber_tv);
        detailstv = (TextView) rootView.findViewById(R.id.add_friend_details_tv);
        profilepictureview = (MyProfilePictureView) rootView.findViewById(R.id.add_friend_photo);

        getContactPickerr();

        return rootView;

    }

    public void getContactPickerr(){

        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS);
        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
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
        }else{
            //Toast explaining why you need to select yes to permission
        }
    }

    public void setContactInView(Contact contact) throws IOException {
        if (verifyContactUsesApp(contact)){
            //Load views with information from database
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
        return false;
    }

    private class SendButtonClickListener implements Button.OnClickListener{

        @Override
        public void onClick(View v) {

        }
    }

    private class CancelButtonListener implements Button.OnClickListener{
        @Override
        public void onClick(View v) {
            getFragmentManager().popBackStack();
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
}
