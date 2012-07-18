package com.jgaunt.Soundbox;

// Android imports
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;


// Dropbox imports
import com.dropbox.client2.android.AndroidAuthSession;
//import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;


public class SoundBox extends Activity
{
    // XXXredfive - Obfuscate this somehow
    final static private String APP_KEY = "f0bb7lauf2hwczh";
    final static private String APP_SECRET = "rdlit805hwt3wke";

    // To get to the root music folder, full Dropbox access is needed.
    final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;

    // The name of the shared preferences file for account related data
    final static private String ACCOUNT_PREFS_NAME = "soundbox-account-prefs";

    // Key names for prefs held in the shared preferences file
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    // member var for hanging on to the AuthSession
    private DropboxAPI<AndroidAuthSession> mDBApi;

    // track our login stated
    private boolean mLoggedIn;

    // handle to the UI so we can change the text
    private Button mSubmit;

    /* ********************************************** *
     *     Android Activity lifetime mgmt methods     *
     * ********************************************** */

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Create a session
        AndroidAuthSession session = buildSession();
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);

        // Get the UI for the button and set an event listener
        mSubmit = (Button)findViewById(R.id.auth_button);
        mSubmit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // This logs you out if you're logged in, or vice versa
                if (mLoggedIn) {
                    logOut();
                } else {
                    // Start the remote authentication
                    mDBApi.getSession().startAuthentication(SoundBox.this);
                }
            }
        });

        setLoggedIn(mDBApi.getSession().isLinked());
    } // onCreate

    /** Called after being paused or stopped */
    @Override
    protected void onResume() {
        super.onResume();
        AndroidAuthSession session = mDBApi.getSession();

        if (session.authenticationSuccessful()) {
            try {
                // MANDATORY call to complete auth.
                // Sets the access token on the session
                session.finishAuthentication();
    
                AccessTokenPair tokens = session.getAccessTokenPair();

                storeKeys(tokens.key, tokens.secret);
                setLoggedIn(true);
                showMusicFolder();
            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }  // onResume

    /* ************************************************
     *               UI Modification                  *
     * ********************************************** */

    /**
      * Sets the login state to the value passed in; adjusts
      *  the text on the Button accordingly.
      */
    private void setLoggedIn(boolean loggedIn) {
        mLoggedIn = loggedIn;
        if (loggedIn) {
            mSubmit.setText("Unlink from Dropbox");
            //mDisplay.setVisibility(View.VISIBLE);
        } else {
            mSubmit.setText("Link with Dropbox");
            //mDisplay.setVisibility(View.GONE);
            //mImage.setImageDrawable(null);
        }
    }

    /** Convenience method to pop a toast message on the screen */
    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }
    
    /* ************************************************
     *           Dropbox API calls                    *
     * ********************************************** */

    private void showMusicFolder() {
        // Metadata
        try {
            // working, now just need to parse the names of the folders and
            // create entries in a UI for them.
            // for item in existingEntry.contents
            //   print regex("\/.*", item.path)
            Entry existingEntry = mDBApi.metadata("/Music", 0, null, true, null);
            Log.i("DbExampleLog", "The file's rev is now: " + existingEntry.rev);
            Log.i("DbExampleLog", "The file's contents has : " + existingEntry.contents);
        } catch (DropboxException e) {
            Log.e("DbExampleLog", "Something went wrong while getting metadata.");
        }
    }

    /* ************************************************
     *           Authentication mgmt                  *
     * ********************************************** */

    /** Create a session; use the persisted session token if it exists */
    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }

    /** store the access token and secret linking the session to the dropbox app (?) */
    private void storeKeys(String key, String secret) {
        // Save the access key for later
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }

    /** retrieve the access token and secret that links the session to the dropbox app (?) */
    private String[] getKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
                String[] ret = new String[2];
                ret[0] = key;
                ret[1] = secret;
                return ret;
        } else {
                return null;
        }
    }

    /** disable this session from the dropbox app - forces a re-authentication */
    private void logOut() {
        // Remove credentials from the session
        mDBApi.getSession().unlink();

        // Clear our stored keys
        clearKeys();
        // Change UI state to display logged out version
        setLoggedIn(false);
    }

    // XXXredfive - change this to erase only the key & secret
    /** remove the stored access token and secret */
    private void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }
}
