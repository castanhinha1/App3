package FragmentControllers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.R;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ConfigClasses.MyProfilePictureView;
import Models.Relation;
import Models.User;


public class SelectedUserDetailsFragment extends DialogFragment {

    String objectId;
    User displayedUser;
    User currentUser;
    TextView nameTV;
    TextView locationTV;
    Button addRemovebutton;
    ImageButton exitButton;
    MyProfilePictureView profilePictureView;
    boolean isClient;
    DismissDialogListener activityCallBack;

    public SelectedUserDetailsFragment(){
    }

    public static SelectedUserDetailsFragment newInstance(String objectId){
        SelectedUserDetailsFragment frag = new SelectedUserDetailsFragment();
        Bundle args = new Bundle();
        args.putString("userId", objectId);
        frag.setArguments(args);
        return frag;
    }

    public interface DismissDialogListener{
        public void onDialogDismissal();
    }

    @Override
    public void dismiss() {
        super.dismiss();

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_selected_user_details, container, false);
        objectId = getArguments().getString("userId");
        Log.i("AppInfo", "Getting created with: "+objectId);
        currentUser = (User) ParseUser.getCurrentUser();
        nameTV = (TextView) rootView.findViewById(R.id.client_profile_nameTV);
        locationTV = (TextView) rootView.findViewById(R.id.client_profile_locationTV);
        profilePictureView = (MyProfilePictureView) rootView.findViewById(R.id.client_profile_profile_picture);
        getUser(objectId);
        addRemovebutton = (Button) rootView.findViewById(R.id.client_profile_addRemove);
        addRemovebutton.setOnClickListener(new AddRemoveButtonListener());
        exitButton = (ImageButton) rootView.findViewById(R.id.dismiss_button);
        exitButton.setOnClickListener(new DismissButton());

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            activityCallBack = (DismissDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    private class DismissButton implements ImageButton.OnClickListener{
        @Override
        public void onClick(View v) {
            dismiss();
        }
    }

    private class AddRemoveButtonListener implements Button.OnClickListener{
        @Override
        public void onClick(View v) {
            if (isClient) {
                ParseRelation<User> relation = currentUser.getRelation("client");
                relation.remove(displayedUser);
                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            addRemovebutton.setText("Add Client");
                            isClient = false;
                            activityCallBack.onDialogDismissal();
                            dismiss();
                        } else {
                            Log.i("AppInfo", e.getMessage());
                        }
                    }
                });
            } else {
                ParseRelation<User> relation = currentUser.getRelation("client");
                relation.add(displayedUser);
                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            ParseACL groupACL = new ParseACL();
                            groupACL.setReadAccess(currentUser, true);
                            groupACL.setWriteAccess(currentUser, true);
                            groupACL.setReadAccess(displayedUser, true);
                            groupACL.setWriteAccess(displayedUser, true);
                            Relation relation = new Relation();
                            relation.setACL(groupACL);
                            relation.setTrainer(currentUser);
                            relation.setClient(displayedUser);
                            relation.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Log.i("AppInfo", "Saved");
                                    } else {
                                        Log.i("AppInfo", e.getMessage());
                                    }
                                }
                            });
                            addRemovebutton.setText("Delete Client");
                            isClient = true;
                            activityCallBack.onDialogDismissal();
                            dismiss();
                        } else {
                            Log.i("AppInfo", e.getMessage());
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public void getUser(String userId){
        ParseQuery<User> query = ParseQuery.getQuery(User.class);
        query.whereEqualTo("objectId", objectId);
        query.getFirstInBackground(new GetCallback<User>() {
            @Override
            public void done(User object, ParseException e) {
                if (object == null){
                } else {
                    displayedUser = object;
                    setView();
                    try {
                        displayedUser.fetchIfNeeded();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    public void setView(){
        //Determine if selected user is a current client or not
        ParseRelation<User> relation = currentUser.getRelation("client");
        ParseQuery<User> query = relation.getQuery();
        query.whereEqualTo("objectId", displayedUser.getObjectId());
        query.findInBackground(new FindCallback<User>() {
            @Override
            public void done(List<User> objects, ParseException e) {
                if(objects.size() != 0){
                    isClient = true;
                    addRemovebutton.setText("Delete Client");
                } else {
                    isClient = false;
                    addRemovebutton.setText("Add Client");
                }
            }
        });
        nameTV.setText(displayedUser.getFullName());
        locationTV.setText(displayedUser.getLocation());
        profilePictureView.setImageBitmap(profilePictureView.getRoundedBitmap(displayedUser.getProfilePicture()));
    }
}
