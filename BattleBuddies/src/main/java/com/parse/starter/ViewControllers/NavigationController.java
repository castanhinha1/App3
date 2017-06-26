package com.parse.starter.ViewControllers;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
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

import FragmentControllers.AddFriends;
import FragmentControllers.CurrentFriends;

public class NavigationController extends AppCompatActivity implements CurrentFriends.OnAddNewUserButtonClicked, AddFriends.OnUserSelected{

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

        home(savedInstanceState);

        //Status bar very top
        Window window = this.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.palette_lightprimarycolor));

    }

    public void home(Bundle savedInstanceState){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            // Create a new Fragment to be placed in the activity layout
            CurrentFriends firstFragment = new CurrentFriends();
            // Add the fragment to the 'fragment_container' FrameLayout
            fragmentTransaction
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.fragment_container, firstFragment, "firstFragment")
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
            AddFriends addFriends = new AddFriends();
            // Add the fragment to the 'fragment_container' FrameLayout
            fragmentTransaction
                    .setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_left)
                    .replace(R.id.fragment_container, addFriends)
                    .addToBackStack("mapFragment")
                    .addToBackStack("firstFragment")
                    .commit();
        }
    }

    @Override
    public void onUserSelected(String userId) {

        //Go to this users location
        Log.i("AppInfo", userId);

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

}
