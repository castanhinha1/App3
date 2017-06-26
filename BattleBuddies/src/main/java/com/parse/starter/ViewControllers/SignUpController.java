package com.parse.starter.ViewControllers;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.parse.starter.R;

public class SignUpController extends AppCompatActivity {

    EditText firstname;
    EditText lastname;
    EditText email;
    EditText usernamefield;
    EditText passwordfield;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firstname = (EditText) findViewById(R.id.firstname);
        lastname = (EditText) findViewById(R.id.lastname);
        email = (EditText) findViewById(R.id.email);
        usernamefield = (EditText) findViewById(R.id.username);
        passwordfield = (EditText) findViewById(R.id.password);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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

    public void onContinueButtonClick(View view) {
        if (ParseUser.getCurrentUser() == null) {
            ParseUser user = new ParseUser();
            user.setUsername(String.valueOf(usernamefield.getText()));
            user.setPassword(String.valueOf(passwordfield.getText()));
            user.setEmail(String.valueOf(email.getText()));

            // other fields
            user.put("firstname", String.valueOf(firstname.getText()));
            user.put("lastname", String.valueOf(lastname.getText()));
            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        goToNavigationScreen();
                    } else {
                        Toast.makeText(getApplicationContext(), e.getMessage().substring(e.getMessage().indexOf(" ")), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            goToNavigationScreen();
        }
    }

    public void goToNavigationScreen() {
        Intent intent = new Intent(getApplicationContext(), NavigationController.class);
        startActivity(intent);
    }

    public void onBackButtonClick(View view) {
        Intent intent = new Intent(getApplicationContext(), LoginController.class);
        startActivity(intent);
    }
}