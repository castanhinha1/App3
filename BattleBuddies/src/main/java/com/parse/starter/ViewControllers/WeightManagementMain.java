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
import android.util.Log;

import com.onesignal.OSPermissionState;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

import Models.FollowTable;
import Models.User;


public class WeightManagementMain extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    // Enable Local Datastore.
    Parse.enableLocalDatastore(this);

    //Register Parse subclasses
    ParseUser.registerSubclass(User.class);
    ParseObject.registerSubclass(FollowTable.class);

    // Add your initialization code here
    Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                    .applicationId("4372hr8vnr2nvwh78h4w8")
                    .clientKey("gtvu83htw437hbv8745vh45wv")
                    .server("https://parseapi.back4app.com/")
    .build()
    );

    // This is the installation part
    ParseInstallation installation = ParseInstallation.getCurrentInstallation();
    installation.put("GCMSenderId", "521084357311");
    installation.saveInBackground();

    ParseFacebookUtils.initialize(getApplicationContext());

    ParseACL defaultACL = new ParseACL();
    // Optionally enable public read access.
    // defaultACL.setPublicReadAccess(true);
    ParseACL.setDefaultACL(defaultACL, true);

    OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init();

  }
}
