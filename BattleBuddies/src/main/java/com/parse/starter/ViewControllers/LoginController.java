/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter.ViewControllers;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import Models.User;

public class LoginController extends ActionBarActivity {

    byte[] imageData;
    EditText usernamefield;
    EditText passwordfield;
    Button facebookbutton;
    User currentUser;
    String fullname = null, email = null, location = null, firstname = null, lastname = null, facebookid = null;
    public static final List<String> mPermissions = new ArrayList<String>() {{
        add("public_profile");
        add("email");
        add("user_location");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_view);

        usernamefield = (EditText) findViewById(R.id.username);
        passwordfield = (EditText) findViewById(R.id.password);
        facebookbutton = (Button) findViewById(R.id.facebook_button);
        if (ParseUser.getCurrentUser() != null) {
            Log.i("AppInfo", "ObjectID: "+ParseUser.getCurrentUser().getObjectId());
            goToNavigationScreen();
        }
        facebookbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginController.this, mPermissions, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user == null && e != null) {
                            Log.i("AppInfo", e.getMessage());
                        } else if (user.isNew()) {
                            currentUser = (User) ParseUser.getCurrentUser();
                            currentUser.setTrainerStatus(false);
                            getUserDetailsFromFB();
                        } else {
                            currentUser = (User) ParseUser.getCurrentUser();
                            getUserDetailsFromParse();
                        }
                    }
                });
            }
        });
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    public void goToNavigationScreen() {
        Intent intent = new Intent(getApplicationContext(), NavigationController.class);
        startActivity(intent);
    }

    private void getUserDetailsFromFB() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    location = object.getJSONObject("location").getString("name");
                    fullname = object.getString("name");
                    email = object.getString("email");
                    facebookid = object.getString("id");
                    String[] parts = fullname.split("\\s+");
                    firstname = parts[0];
                    lastname = parts[1];
                    //Profile picture code
                    String pictureUrl = "https://graph.facebook.com/" + facebookid + "/picture?type=large";
                    new ProfilePhotoAsync(pictureUrl).execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,location,name,email,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    class ProfilePhotoAsync extends AsyncTask<String, String, String> {
        String url;
        public ProfilePhotoAsync(String url) {
            this.url = url;
        }
        @Override
        protected String doInBackground(String... params) {
            imageData = DownloadImageBitmap(url);
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            saveNewUser();
        }
    }

    public static byte[] DownloadImageBitmap(String url){
        byte[] imageArray = null;
        Bitmap bmp = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bmp = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e){
            Log.i("AppInfo", e.getMessage());
        }
        //Convert Bitmap to Byte Array for Storage in Parse
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        imageArray = stream.toByteArray();
        return imageArray;
    }

    private void saveNewUser() {
        currentUser.setFullName(fullname);
        currentUser.setEmail(email);
        currentUser.setLocation(location);
        currentUser.setFirstName(firstname);
        currentUser.setLastName(lastname);
        currentUser.setProfilePicture(imageData);
        //Finally save all the user details
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    goToNavigationScreen();
                } else {
                    Log.i("AppInfo", "Message: "+e.getMessage());
                }
            }
        });
    }

    private void getUserDetailsFromParse() {
        Toast.makeText(LoginController.this, "Welcome back "+currentUser.getFirstName()+"!", Toast.LENGTH_SHORT).show();
        goToNavigationScreen();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
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
    public void onLoginButtonClick(View view) {
        //Check to make sure user is not already logged in
        ParseUser.logInInBackground(String.valueOf(usernamefield.getText()), String.valueOf(passwordfield.getText()), new LogInCallback() {

            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    goToNavigationScreen();
                } else {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void onSignUpButtonClick(View view) {
        //Move to Sign Up Activity
        Intent intent = new Intent(this, SignUpController.class);
        startActivity(intent);
    }
}
