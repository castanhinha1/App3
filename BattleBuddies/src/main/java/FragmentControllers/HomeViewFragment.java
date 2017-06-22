package FragmentControllers;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.starter.R;

import java.util.List;

import ConfigClasses.MyProfilePictureView;
import Models.Relation;
import Models.User;

/**
 * Created by Dylan Castanhinha on 4/12/2017.
 */

public class HomeViewFragment extends Fragment {

    ImageView profileImage;
    ImageButton editdetailsbutton;
    TextView nametv;
    TextView agesextv;
    TextView weighttv;
    TextView heighttv;
    TextView bodyfattv;
    TextView dailycaloriestv;
    TextView yourtrainertv;
    User currentUser;
    User trainer;
    OnEditDetailsButton activityCallback;

    public interface OnEditDetailsButton{
        void onEditDetailsClicked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            activityCallback = (OnEditDetailsButton) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView titleTextView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        titleTextView.setText("Weight Management");
        ImageButton toolbarLeftButton = (ImageButton) getActivity().findViewById(R.id.toolbar_left_button);
        toolbarLeftButton.setImageResource(0);
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        currentUser = (User) ParseUser.getCurrentUser();
        profileImage = (ImageView) rootView.findViewById(R.id.home_profile_picture);
        nametv = (TextView) rootView.findViewById(R.id.home_name_tv);
        agesextv = (TextView) rootView.findViewById(R.id.home_age_sex_tv);
        weighttv = (TextView) rootView.findViewById(R.id.home_weight_tv);
        heighttv = (TextView) rootView.findViewById(R.id.home_height_tv);
        bodyfattv = (TextView) rootView.findViewById(R.id.home_bodyfat_tv);
        dailycaloriestv = (TextView) rootView.findViewById(R.id.home_daily_calories_tv);
        yourtrainertv = (TextView) rootView.findViewById(R.id.home_your_trainer_tv);
        editdetailsbutton = (ImageButton) rootView.findViewById(R.id.home_edit_button);
        editdetailsbutton.setOnClickListener(new OnEditDetailsButtonListener());
        setUserData();
        return rootView;
    }



    public void setUserData() {
        //Get current users relation
        ParseQuery<Relation> query = ParseQuery.getQuery(Relation.class);
        query.whereEqualTo("client", currentUser);
        query.getFirstInBackground(new GetCallback<Relation>() {
            @Override
            public void done(Relation object, ParseException e) {
                if (e == null) {
                    trainer = object.getTrainer();
                    try {
                        trainer.fetch();
                        yourtrainertv.setText(trainer.getFullName()+"\nYour Trainer");
                        currentUser.setTrainer(trainer);
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }

                } else {
                    Log.i("AppInfo", e.getMessage());
                    yourtrainertv.setText("Trainer\nYour Trainer");
                }
            }
        });
        if (currentUser.getProfilePicture() != null) {
            profileImage.setImageBitmap(currentUser.getProfilePicture());
        } else {
            profileImage.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
        }
        if (currentUser.getFullName() != null) {
            nametv.setText(currentUser.getFullName());
        } else {
            nametv.setText("Full Name");
        }
        if (currentUser.getAge() != null && currentUser.getSex() != null) {
            String ageandsex = currentUser.getSex() + ", " + currentUser.getAge();
            agesextv.setText(ageandsex);
        } else {
            agesextv.setText("Sex, Age");
        }
        if (currentUser.getWeight() != null) {
            weighttv.setText(currentUser.getWeight() + " LBs");
        } else {
            weighttv.setText("--LBs");
        }
        if (currentUser.getHeight() != null) {
            heighttv.setText(currentUser.getHeight() + " HT");
        } else {
            heighttv.setText("--HT");
        }
        if (currentUser.getBodyFat() != null) {
            bodyfattv.setText(currentUser.getBodyFat() + "% BF");
        } else {
            bodyfattv.setText("--BF");
        }
        if (currentUser.getCalories() != null) {
            dailycaloriestv.setText(currentUser.getCalories() + "\nCalories Daily");
        } else {
            dailycaloriestv.setText("...\nCalories Daily");
        }
    }

    private class OnEditDetailsButtonListener implements ImageButton.OnClickListener {
        @Override
        public void onClick(View v) {
            activityCallback.onEditDetailsClicked();
        }
    }
}
