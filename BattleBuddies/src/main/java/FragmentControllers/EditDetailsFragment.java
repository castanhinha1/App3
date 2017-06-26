package FragmentControllers;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseUser;
import com.parse.starter.R;

import java.util.ArrayList;

import Models.User;

/**
 * Created by Dylan Castanhinha on 4/12/2017.
 */

public class EditDetailsFragment extends Fragment {

    ListView listview;
    User currentUser;
    EditDetailsAdapter adapter;
    OnRowSelected activityCallBack;

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
        TextView titleTextView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        titleTextView.setText("Edit Profile");
        ImageButton doneButton = (ImageButton) getActivity().findViewById(R.id.toolbar_left_button);
        doneButton.setImageResource(R.drawable.ic_done_button);
        doneButton.setOnClickListener(new DoneButtonClickListener());
        View rootView = inflater.inflate(R.layout.fragment_edit_details, container, false);
        ArrayList<User> users = new ArrayList<User>();
        listview = (ListView) rootView.findViewById(R.id.edit_details_list_view);
        adapter = new EditDetailsAdapter(getActivity().getApplicationContext(), users);
        listview.setAdapter(adapter);
        for (int i = 0; i < 9; i++){
            adapter.add(currentUser);
        }
        return rootView;
    }

    public class DoneButtonClickListener implements ImageButton.OnClickListener{
        @Override
        public void onClick(View v) {
            getFragmentManager().popBackStack();
        }
    }

    public class EditDetailsAdapter extends ArrayAdapter<User>{
        public EditDetailsAdapter(Context context, ArrayList<User> users){
            super(context,0, users);
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            User user = getItem(position);

            if (convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_layout_edit_details, parent, false);
            }
            TextView details = (TextView) convertView.findViewById(R.id.edit_details_text_view);
            TextView userDescription = (TextView) convertView.findViewById(R.id.edit_details_row_details);
            switch(position){
                case 0: {
                    details.setText("Profile Photo");
                    userDescription.setText("");
                    break;
                }
                case 1: {
                    details.setText("Name");
                    if (user.getFullName() != null){
                        userDescription.setText(user.getFullName());
                    } else {
                        userDescription.setText("");
                    }
                    break;
                }
                case 2: {
                    details.setText("Sex");
                    if (user.getSex() != null){
                        userDescription.setText(user.getSex());
                    } else {
                        userDescription.setText("");
                    }
                    break;
                }
                case 3: {
                    details.setText("Age");
                    if (user.getAge() != null){
                        userDescription.setText(user.getAge());
                    } else {
                        userDescription.setText("");
                    }
                    break;
                }
                case 4: {
                    details.setText("Weight");
                    if (user.getWeight() != null){
                        userDescription.setText(user.getWeight()+" Lbs");
                    } else {
                        userDescription.setText("");
                    }
                    break;
                }
                case 5: {
                    details.setText("Height");
                    if (user.getHeight() != null){
                        userDescription.setText(user.getHeight());
                    } else {
                        userDescription.setText("");
                    }
                    break;
                }
                case 6: {
                    details.setText("Body Fat %");
                    if (user.getBodyFat() != null){
                        userDescription.setText(user.getBodyFat()+"%");
                    } else {
                        userDescription.setText("");
                    }
                    break;
                }
                case 7: {
                    details.setText("Daily Caloric Intake");
                    if (user.getCalories() != null){
                        userDescription.setText("Daily: "+user.getCalories());
                    } else {
                        userDescription.setText("");
                    }
                    break;
                }
                case 8: {
                    details.setText("Trainer");
                    if (user.getTrainer() != null){
                        userDescription.setText(user.getTrainer().getFullName());
                    } else {
                        userDescription.setText("");
                    }
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
    public void reloadData(){
        adapter.notifyDataSetChanged();
    }
}
