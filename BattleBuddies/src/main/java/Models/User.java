package Models;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.parse.starter.R;

import java.util.ArrayList;
import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 * Created by Dylan Castanhinha on 3/30/2017.
 */

@ParseClassName("_User")
public class User extends ParseUser {

    public User() {
        super();
    }

    public String getFullName() {
        return getString("fullname");
    }
    public void setFullName(String value) {
        put("fullname", value);
    }

    public boolean getTrainerStatus(){
        return getBoolean("trainerstatus");
    }
    public void setTrainerStatus(boolean value){
        put("trainerstatus", value);
    }

    public String getLocation(){
        return getString("location");
    }

    public void setLocation(String value){
        put("location", value);
    }

    public ParseGeoPoint getGeopoint(){
        return getParseGeoPoint("geopoint");
    }

    public void setGeopoint(ParseGeoPoint value){
        put("geopoint", value);
    }

    public String getFirstName(){
        return getString("firstname");
    }
    public void setFirstName(String value){
        put("firstname", value);
    }
    public String getLastName(){
        return getString("lastname");
    }
    public void setLastName(String value){
        put("lastname", value);
    }

    public String getCalories(){
        return getString("calories");
    }
    public void setCalories(String value){
        put("calories", value);
    }

    public String getSex(){
        return getString("sex");
    }
    public void setSex(String value){
        put("sex", value);
    }
    public String getAge(){
        return getString("age");
    }
    public void setAge(String value){
        put("age", value);
    }
    public String getWeight(){
        return getString("weight");
    }
    public void setWeight(String value){
        put("weight", value);
    }
    public String getBodyFat(){
        return getString("bodyfat");
    }
    public void setBodyFat(String value){
        put("bodyfat", value);
    }
    public String getHeight(){
        return getString("height");
    }
    public void setHeight(String value){
        put("height", value);
    }
    public User getTrainer(){
        return (User) getParseUser("trainer");
    }
    public void setTrainer(User value) {
        put("trainer", value);
    }
    public ParseRelation<User> getClients() {
        return getRelation("client");
    }
    public void setClients(ParseRelation<User> clients) {
        put("client", clients);
    }

    public ParseFile getProfilePictureFile(){
        return getParseFile("profilepicture");
    }

    public Bitmap getProfilePicture(){
        Bitmap bmp = null;
        ParseFile profilePicture = getProfilePictureFile();
        if (profilePicture != null) {
            try {
                bmp = BitmapFactory.decodeStream(profilePicture.getDataStream());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            bmp = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.com_facebook_profile_picture_blank_square);
        }
        return bmp;
    }
    public void setProfilePicture(byte[] value){
        final ParseFile profilePicture = new ParseFile("profilepicture.png", value);
        profilePicture.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i("AppInfo", "Photo saved");
                    User currentUser = (User) ParseUser.getCurrentUser();
                    currentUser.put("profilepicture", profilePicture);
                    currentUser.saveInBackground();
                } else {
                    Log.i("AppInfo", e.getMessage());
                }
            }
        }, new ProgressCallback() {
            @Override
            public void done(Integer percentDone) {
                Log.i("AppInfo", "Percent Done: "+percentDone.toString());
            }
        });
    }

    public LatLng getLatLng(){
        LatLng latLng = new LatLng(getGeopoint().getLatitude(), getGeopoint().getLongitude());
        return latLng;
    }
}

