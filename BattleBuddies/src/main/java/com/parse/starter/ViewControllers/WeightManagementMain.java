/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter.ViewControllers;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;

import Models.Relation;
import Models.User;


public class WeightManagementMain extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    // Enable Local Datastore.
    Parse.enableLocalDatastore(this);

    //Register Parse subclasses
    ParseUser.registerSubclass(User.class);
    ParseObject.registerSubclass(Relation.class);

    // Add your initialization code here
    Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
            .applicationId("Oy5cUtCrVX6ZiWt1Ap91GpxCtF96tkrcbUaHKwHb")
            .clientKey("s2ie5DJct4evniqMU7LDz8lUUKOIhXqD2zywleG2")
            .server("https://parseapi.back4app.com/")
    .build()
    );

    ParseFacebookUtils.initialize(getApplicationContext());

    ParseACL defaultACL = new ParseACL();
    // Optionally enable public read access.
    // defaultACL.setPublicReadAccess(true);
    ParseACL.setDefaultACL(defaultACL, true);

  }
}
