package com.parse.starter.ViewControllers;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.parse.ParseUser;
import com.parse.starter.R;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.Map;

import FragmentControllers.AddNewClientsOrTrainerFragment;
import FragmentControllers.CalendarFragment;
import FragmentControllers.CaloriesFragment;
import FragmentControllers.ChangeDetailsFragment;
import FragmentControllers.CurrentClientsOrTrainerFragment;
import FragmentControllers.EditDetailsFragment;
import FragmentControllers.HomeViewFragment;
import FragmentControllers.MapFragment;
import FragmentControllers.SelectedUserDetailsFragment;
import FragmentControllers.YourProfileFragment;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class NavigationController extends AppCompatActivity implements CurrentClientsOrTrainerFragment.OnAddNewUserButtonClicked, AddNewClientsOrTrainerFragment.OnUserSelected, SelectedUserDetailsFragment.DismissDialogListener, HomeViewFragment.OnEditDetailsButton, ChangeDetailsFragment.DismissEditDialogListener, EditDetailsFragment.OnRowSelected {

    private Toolbar toolbar;
    private PopupMenu mPopupMenu;
    Bundle savedInstanceState1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        this.savedInstanceState1 = savedInstanceState;
        //Toolbar (Top)
        toolbar = (Toolbar) findViewById(R.id.custom_toolbar);
        ImageButton menuButton = (ImageButton) findViewById(R.id.toolbar_right_button);
        menuButton.setImageResource(R.drawable.ic_menu_button);
        mPopupMenu = new PopupMenu(this, menuButton);
        MenuInflater menuInflater = mPopupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, mPopupMenu.getMenu());
        menuButton.setOnClickListener(new MenuButtonClickListener());
        setSupportActionBar(toolbar);

        //Status bar very top
        Window window = this.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.palette_lightprimarycolor));

        //Navigation Bar (Bottom)
        final BottomBar navigationBar = (BottomBar) findViewById(R.id.bottomBar);
        navigationBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.home) {
                    home(savedInstanceState);
                } else if (tabId == R.id.goal) {
                    goal(savedInstanceState);
                } else if (tabId == R.id.calendar) {
                    calendar(savedInstanceState);
                } else if (tabId == R.id.trainer) {
                    trainer(savedInstanceState);
                } else if (tabId == R.id.profile) {
                    profile(savedInstanceState);
                } else {
                    Log.i("AppInfo", "Home button pressed");
                }
            }
        });
        navigationBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                if (tabId == R.id.home) {
                    home(savedInstanceState);
                } else if (tabId == R.id.goal) {
                    goal(savedInstanceState);
                } else if (tabId == R.id.calendar) {
                    calendar(savedInstanceState);
                } else if (tabId == R.id.trainer) {
                    trainer(savedInstanceState);
                } else if (tabId == R.id.profile) {
                    profile(savedInstanceState);
                } else {
                    Log.i("AppInfo", "Home button pressed");
                }
            }
        });
    }

    private class MenuButtonClickListener implements ImageButton.OnClickListener{
        @Override
        public void onClick(View view) {
            mPopupMenu.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0){
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Log Out?")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ParseUser.getCurrentUser().logOut();
                            finish();
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        } else {
            getFragmentManager().popBackStack();
        }

    }

    @Override
    public void onRowSelected(int position) {
        if (position == 7){
            CaloriesFragment caloriesFragment = new CaloriesFragment();
            FragmentManager fm = getFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, caloriesFragment);
            fragmentTransaction.commit();
        } else {
            FragmentManager fm = getFragmentManager();
            ChangeDetailsFragment changeDetailsFragment = ChangeDetailsFragment.newInstance(position);
            changeDetailsFragment.show(fm, "fragment_selected_user");
        }

    }

    @Override
    public void onEditDetailsClicked() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState1 != null) {
                return;
            }
            // Create a new Fragment to be placed in the activity layout
            EditDetailsFragment editDetailsFragment = new EditDetailsFragment();
            // Add the fragment to the 'fragment_container' FrameLayout
            fragmentTransaction
                    .setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_left)
                    .replace(R.id.fragment_container, editDetailsFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onAddUserClicked() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState1 != null) {
                return;
            }
            // Create a new Fragment to be placed in the activity layout
            AddNewClientsOrTrainerFragment addNewClientsOrTrainerFragment = new AddNewClientsOrTrainerFragment();
            // Add the fragment to the 'fragment_container' FrameLayout
            fragmentTransaction
                    .setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_left)
                    .replace(R.id.fragment_container, addNewClientsOrTrainerFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onEditDialogDismissal() {
        Log.i("AppInfo", String.valueOf(getFragmentManager().findFragmentById(R.id.fragment_container)));
        //Reload data here
        //getFragmentManager().popBackStack();
    }

    @Override
    public void onDialogDismissal() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState1 != null) {
                return;
            }
            // Create a new Fragment to be placed in the activity layout
            CurrentClientsOrTrainerFragment firstFragment = new CurrentClientsOrTrainerFragment();
            // Add the fragment to the 'fragment_container' FrameLayout
            fragmentTransaction.replace(R.id.fragment_container, firstFragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onUserSelected(String userId) {
        FragmentManager fm = getFragmentManager();
        SelectedUserDetailsFragment selectedUserDetailsFragment = SelectedUserDetailsFragment.newInstance(userId);
        selectedUserDetailsFragment.show(fm, "fragment_selected_user");
    }

    public void home(Bundle savedInstanceState){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            // Create a new Fragment to be placed in the activity layout
            CurrentClientsOrTrainerFragment firstFragment = new CurrentClientsOrTrainerFragment();
            MapFragment mapFragment = new MapFragment();
            // Add the fragment to the 'fragment_container' FrameLayout
            fragmentTransaction
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.fragment_bottom, firstFragment)
                    .replace(R.id.fragment_top, mapFragment)
                    .commit();
        }
    }

    public void goal(Bundle savedInstanceState){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            // Create a new Fragment to be placed in the activity layout
            MapFragment mapFragment = new MapFragment();
            // Add the fragment to the 'fragment_container' FrameLayout
            fragmentTransaction
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.fragment_container, mapFragment)
                    .commit();
        }
    }

    public void calendar(Bundle savedInstanceState){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            // Create a new Fragment to be placed in the activity layout
            CalendarFragment calendarFragment = new CalendarFragment();
            // Add the fragment to the 'fragment_container' FrameLayout
            fragmentTransaction
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.fragment_container, calendarFragment)
                    .commit();
        }
    }

    public void trainer(Bundle savedInstanceState) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            // Create a new Fragment to be placed in the activity layout
            CurrentClientsOrTrainerFragment firstFragment = new CurrentClientsOrTrainerFragment();
            // Add the fragment to the 'fragment_container' FrameLayout
            fragmentTransaction
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.fragment_container, firstFragment)
                    .commit();

        }
    }

    public void profile(Bundle savedInstanceState) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            // Create a new Fragment to be placed in the activity layout
            YourProfileFragment profileFragment = new YourProfileFragment();
            // Add the fragment to the 'fragment_container' FrameLayout
            fragmentTransaction
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.fragment_container, profileFragment)
                    .commit();
        }

    }
}

