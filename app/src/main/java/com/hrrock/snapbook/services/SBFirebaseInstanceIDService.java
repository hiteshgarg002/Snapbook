package com.hrrock.snapbook.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class SBFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String USER_PREFERENCES="userinfo";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        SharedPreferences preferences = getApplicationContext().getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("firebaseToken", FirebaseInstanceId.getInstance().getToken());
        editor.putString("firebaseID", FirebaseInstanceId.getInstance().getId());
        editor.apply();
    }
}
