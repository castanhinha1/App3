package FragmentControllers;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.TextView;

import com.parse.ParseUser;
import com.parse.starter.R;

import Models.User;

/**
 * Created by Dylan Castanhinha on 4/24/2017.
 */

public class CalendarFragment extends Fragment {

    CalendarView calendarView;
    User currentUser;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView titleTextView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        titleTextView.setText("Your Calendar");
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);
        currentUser = (User) ParseUser.getCurrentUser();
        calendarView = (CalendarView) rootView.findViewById(R.id.calendarView);
        return rootView;
    }
}
