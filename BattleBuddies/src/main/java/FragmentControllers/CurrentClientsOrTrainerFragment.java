package FragmentControllers;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.starter.R;

import java.util.List;

import ConfigClasses.MyProfilePictureView;
import ConfigClasses.ParseAdapterCustomList;
import Models.User;

public class CurrentClientsOrTrainerFragment extends Fragment {

    TextView labelTV;
    ListView listview;
    CurrentClients adapter;
    User currentUser;
    SwipeRefreshLayout swipeContainer;
    OnAddNewUserButtonClicked activityCallback;
    AddNewClientsOrTrainerFragment.OnUserSelected activityCallBack;

    public interface OnAddNewUserButtonClicked {
        void onAddUserClicked();
    }
    public interface OnUserSelected {
        void onUserSelected(String userId);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            activityCallback = (OnAddNewUserButtonClicked) context;
            activityCallBack = (AddNewClientsOrTrainerFragment.OnUserSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentUser = (User) ParseUser.getCurrentUser();
        //Toolbar top
        final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.custom_toolbar);
        TextView titleTextView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        titleTextView.setText(currentUser.getFirstName() + "'s "+"Clients");
        ImageButton addUserButton = (ImageButton) getActivity().findViewById(R.id.toolbar_left_button);
        addUserButton.setImageResource(R.drawable.ic_add_user_green);
        addUserButton.setOnClickListener(new AddNewClientButtonListener());
        View rootView = inflater.inflate(R.layout.fragment_current_clients, container, false);
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        listview = (ListView) rootView.findViewById(R.id.current_client_list_view);
        adapter = new CurrentClients(getActivity());
        listview.setAdapter(adapter);
        swipeContainer.setOnRefreshListener(new SwipeToRefresh());
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_green_light);
        return rootView;
    }

    private class SwipeToRefresh implements SwipeRefreshLayout.OnRefreshListener{

        @Override
        public void onRefresh() {
            adapter.loadObjects();
        }
    }

    private class AddNewClientButtonListener implements ImageButton.OnClickListener{
        @Override
        public void onClick(View v) {
            activityCallback.onAddUserClicked();
        }
    }


    private class CurrentClients extends ParseAdapterCustomList implements ParseQueryAdapter.OnQueryLoadListener {
        Context context;
        private CurrentClients(final Context context){
            super(context, new ParseQueryAdapter.QueryFactory<User>(){
               public ParseQuery<User> create() {
                   ParseRelation<User> relation = currentUser.getRelation("client");
                   ParseQuery<User> query = relation.getQuery();
                   query.whereEqualTo("objectId", false);
                   query.whereNotEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
                   return query;
               }
            });
            addOnQueryLoadListener(this);
        }


        @Override
        public void onLoading() {
            swipeContainer.setRefreshing(true);
            Log.i("AppInfo", "Loading");
        }

        @Override
        public void onLoaded(List objects, Exception e) {
            swipeContainer.setRefreshing(false);
            Log.i("AppInfo", "Loaded");
        }


        @Override
        public View getItemView(final User user, View v, ViewGroup parent){
            if (v == null){
                v = View.inflate(getContext(), R.layout.list_layout_current_clients, null);
            }
            super.getItemView(user, v, parent);

            //Add the title view
            TextView nameTextView = (TextView) v.findViewById(R.id.current_client_text_view_name);
            nameTextView.setText(user.getFullName());

            //Add the objectid
            TextView objectId = (TextView) v.findViewById(R.id.current_client_object_id);
            objectId.setText(user.getLocation());

            //Add the image
            MyProfilePictureView imageView = (MyProfilePictureView) v.findViewById(R.id.imageView3);
            imageView.setImageBitmap(imageView.getRoundedBitmap(user.getProfilePicture()));

            //On click listener for selection
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activityCallBack.onUserSelected(user.getObjectId());
                }
            });
            return v;
        }
    }
}

