package FragmentControllers;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
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

import com.parse.ParseUser;
import com.parse.starter.R;

import java.util.ArrayList;

import Models.User;

public class EditProfileFragment extends Fragment {

    User currentUser;
    ListView listView;
    EditDetailsAdapter adapter;
    int position;

    public EditProfileFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        position = getArguments().getInt("position");
        currentUser = (User) ParseUser.getCurrentUser();
        Log.i("AppInfo", "Position: "+position);
        //Toolbar top
        TextView titleTextView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        titleTextView.setText("First Name");
        ImageButton previousButton = (ImageButton) getActivity().findViewById(R.id.toolbar_left_button);
        previousButton.setImageResource(R.drawable.ic_back_button);
        previousButton.setVisibility(View.VISIBLE);
        previousButton.bringToFront();
        previousButton.setOnClickListener(new PreviousButtonClickListener());
        Button doneButton = (Button) getActivity().findViewById(R.id.toolbar_right_button_text);
        doneButton.setText("Done");
        doneButton.setOnClickListener(new DoneButtonClickListener());
        ImageButton leftButton = (ImageButton) getActivity().findViewById(R.id.toolbar_left_button);
        leftButton.setImageResource(0);
        leftButton.setClickable(false);

        //Edit Details List View
        ArrayList<User> users = new ArrayList<User>();
        listView = (ListView) rootView.findViewById(R.id.edit_profile_details_list_view);
        adapter = new EditDetailsAdapter(getActivity().getApplicationContext(), users);
        listView.setAdapter(adapter);
        adapter.add(currentUser);

        return rootView;
    }

    public class EditDetailsAdapter extends ArrayAdapter<User> {
        public EditDetailsAdapter(Context context, ArrayList<User> users){
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

            details.setText(user.getFullName());
            button.setImageResource(R.drawable.ic_user_buttpn);

            return convertView;
        }
    }

    public class PreviousButtonClickListener implements ImageButton.OnClickListener{
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
