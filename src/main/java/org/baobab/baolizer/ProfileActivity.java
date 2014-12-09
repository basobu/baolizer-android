package org.baobab.baolizer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import java.util.Arrays;

public class ProfileActivity extends Activity implements Session.StatusCallback {

    private UiLifecycleHelper fbHelper;
    private LoginButton fbButton;
    private ProgressBar wheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        wheel = (ProgressBar) findViewById(R.id.wheel);
        fbButton = (LoginButton) findViewById(R.id.fbButton);
        fbButton.setReadPermissions(Arrays.asList("email"));
        fbHelper = new UiLifecycleHelper(this, this);
        fbHelper.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Session session = Session.getActiveSession();
        if (session != null && (session.isOpened() || session.isClosed()) ) {
            call(session, session.getState(), null);
        } else {
            fbButton.setVisibility(View.VISIBLE);
            wheel.setVisibility(View.GONE);
        }
        fbHelper.onResume();
    }

    @Override
    public void call(Session session, SessionState sessionState, Exception e) {
        final SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        if (session.isOpened()) {
            if (prefs.contains("user")) {
                Log.d(MapActivity.TAG, "cached");
                loggedIn(prefs.getString("user", null), prefs.getString("email", null));
            } else {
                Request.newMeRequest(session, new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (response == null) {
                            Log.d(MapActivity.TAG, "no user");
                            return;
                        }
                        Log.d(MapActivity.TAG, "LOGGED IN");
                        String email = user.getProperty("email").toString();
                        prefs.edit()
                                .putString("user", user.getName())
                                .putString("email", email).commit();
                        loggedIn(user.getName(), email);
                    }
                }).executeAsync();

            }
        } else {
            Log.d(MapActivity.TAG, "LOGOUT");
            prefs.edit().remove("user").commit();
            fbButton.setVisibility(View.VISIBLE);
            wheel.setVisibility(View.GONE);
            ((TextView) findViewById(R.id.email)).setText("");
            setTitle(R.string.profile);
        }
    }

    private void loggedIn(String name, String email) {
        setTitle(name);
        ((TextView) findViewById(R.id.email)).setText(email);
        fbButton.setVisibility(View.VISIBLE);
        wheel.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fbHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        fbHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fbHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        fbHelper.onSaveInstanceState(outState);
    }
}
