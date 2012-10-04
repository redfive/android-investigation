package com.jgaunt.Soundbox;

// Android imports
import android.app.ActionBar;
import android.app.Activity;
//import android.app.FragmentTransaction;
//import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
//import android.os.Bundle;
import android.util.Log;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
import android.widget.Toast;

// Dropbox imports
//import com.dropbox.client2.android.AndroidAuthSession;
//import com.dropbox.client2.DropboxAPI;
//import com.dropbox.client2.session.AccessTokenPair;
//import com.dropbox.client2.session.AppKeyPair;
//import com.dropbox.client2.session.Session.AccessType;


/**
 * Activity baseclass for all activities.
 */
public class BaseActivity extends Activity
{
    // TODO: Obfuscate/encrypt this
    final static private String APP_KEY = "f0bb7lauf2hwczh";
    final static private String APP_SECRET = "rdlit805hwt3wke";

    // To get to the root music folder, full Dropbox access is needed.
    final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;

    // The name of the shared preferences file for account related data
    final static private String ACCOUNT_PREFS_NAME = "account-prefs";

    // Default location we'll search from 
    final static private String DROPBOX_MUSIC_ROOT = "/Music";

    // Key names for prefs held in the shared preferences file
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    // Do the network requests on an AsyncTask
    static private SoundBoxAsyncFetcher mFetcher = null;

    // TODO: if this is static can we keep from doing yet another connection?
    // member var for hanging on to the AuthSession
    private DropboxAPI<AndroidAuthSession> mDBApi;

    // track the login stated
    private boolean mLoggedIn;

    // mCurrentPath needs to be static to persist when we rotate
    static private String mCurrentPath = DROPBOX_MUSIC_ROOT;
    static private String mListTag = "krList";

    // handle to the UI so we can change the text
    private Button mSubmit;

    /* ********************************************************************* *
     *                 Android Activity lifetime mgmt methods                *
     * ********************************************************************* */

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("BaseActivity", "onCreate()");

        // Allow clicking on the ActionBar icon to take you home
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);

        // CHANGE: will need to create/start the service in charge of DB access
        //         might want to retain the stuff about showing the folder or login page

        // Create a session
        AndroidAuthSession session = buildSession();
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);

        setLoggedIn(mDBApi.getSession().isLinked());
        if (mLoggedIn) {
            // We are logged in so load the last known path we were looking at
            //Log.i("BaseActivity", "in onCreate, calling ShowMusicFolder with " + mCurrentPath);
            showMusicFolder(mCurrentPath);
        } else {
            // We are not logged in so load the fragment for the link button
            showLogin();
        }
    } // onCreate

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("BaseActivity", "onStart()");
    } // onStart

    /** Called after being paused or stopped */
    @Override
    protected void onResume() {
        super.onResume();
        Log.i("BaseActivity", "onResume()");

        // CHANGE: will need to create/start the service in charge of DB access
        AndroidAuthSession session = mDBApi.getSession();

        if (session.authenticationSuccessful()) {
            try {
                // MANDATORY call to complete auth.
                // Sets the access token on the session
                session.finishAuthentication();
    
                AccessTokenPair tokens = session.getAccessTokenPair();

                storeKeys(tokens.key, tokens.secret);
                setLoggedIn(true);

            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                Log.i("BaseActivity", "Error authenticating", e);
            }
        }

        if (null == mFetcher) {
            // a null fetcher here means we need to show some content
            //Log.i("BaseActivity", "in onResume, calling ShowMusicFolder with " + mCurrentPath);
            showMusicFolder(mCurrentPath);
        } else {
            // If we've just been paused there should be a fetcher and we need
            // to reconnect to it; we've already attempted to showMusicFolder().
            mFetcher.setDropboxAPI(mDBApi);
            mFetcher.registerCompletionHandler(this);
        }
    } // onResume

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("BaseActivity", "onPause()");
    } // onPause

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("BaseActivity", "onRestart()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("BaseActivity", "onStop()");
    } // onStop

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("BaseActivity", "onDestroy()");
    } // onDestroy

    @Override
    public void onSaveInstanceState(Bundle aSavedInstanceState) {
        super.onSaveInstanceState(aSavedInstanceState);
        Log.i("BaseActivity", "onSaveInstanceState()");
    } // onSaveInstanceState

    @Override
    public void onRestoreInstanceState(Bundle aSavedInstanceState) {
        super.onRestoreInstanceState(aSavedInstanceState);
        Log.i("BaseActivity", "onRestoreInstanceState()");
    } // onRestoreInstanceState

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (android.R.id.home) :
                // ActionBar icon clicked
                showMusicFolder(DROPBOX_MUSIC_ROOT);
                return true;
            default :
            return super.onOptionsItemSelected(item);
        }
    } // onOptionsItemSelected

    /* ********************************************************************* *
     *                           UI Modification                             *
     * ********************************************************************* */

    /** Convenience method to pop a toast message on the screen */
    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    // CHANGE: make this launch the new activity - LoginActivity
    private void showLogin() {
        Log.i("BaseActivity", "showLogin()");
        // currently only show this when the user is logged out
        // eventually allow calling this when the user is logged in so they
        // can log out.
        if (mLoggedIn) {
            // in this case we need to do a replace, not an add transaction
            return;
        }

        SoundBoxLoginFragment loginFrag = SoundBoxLoginFragment.newInstance();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.main_frame, loginFrag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();

        // XXX have to figure out where this goes
        // Get the UI for the button and set an event listener
        mSubmit = (Button)findViewById(R.id.auth_button);
        mSubmit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // This logs you out if you're logged in, or vice versa
                if (mLoggedIn) {
                    logOut();
                } else {
                    // Start the remote authentication
                    mDBApi.getSession().startAuthentication(BaseActivity.this);
                }
            }
        });
    }
    
    /* ********************************************************************* *
     *                          Dropbox API calls                            *
     * ********************************************************************* */

    // CHANGE: make this launch the new activity - ListActivity
    public void showMusicFolder(String aPath) {
        Log.i("BaseActivity", "showMusicFolder()");
        // TODO: check the input param, if null set to / or /Music
        // TODO: handle the case where path is a file
        // Metadata

        Log.i("BaseActivity", "Showing Folder: " + aPath);

        // if there is a fetcher we're already grabbing something, wait for it...
        if (mFetcher != null) {
            Log.i("BaseActivity", "still have mFetcher, aborting");
            return;
        }
        mCurrentPath = aPath;
        // TODO replace this with a DropboxService
        mFetcher = new SoundBoxAsyncFetcher();
        mFetcher.setDropboxAPI(mDBApi);
        mFetcher.registerCompletionHandler(this);
        mFetcher.execute(mCurrentPath);
    }
}
