package com.jgaunt.Soundbox;

// SoundBox imports
import com.jgaunt.Soundbox.R;
import com.jgaunt.Soundbox.SoundBoxAsyncFetcher.CompletionHandler;

// Android imports
import android.app.Activity;
import android.app.FragmentTransaction;
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
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
//import com.dropbox.client2.session.TokenPair;


public class SoundBox extends Activity
                      implements SoundBoxAsyncFetcher.CompletionHandler
{
    // TODO: Obfuscate/encrypt this
    final static private String APP_KEY = "f0bb7lauf2hwczh";
    final static private String APP_SECRET = "rdlit805hwt3wke";

    // To get to the root music folder, full Dropbox access is needed.
    final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;

    // The name of the shared preferences file for account related data
    final static private String ACCOUNT_PREFS_NAME = "soundbox-account-prefs";

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

    // handle to the UI so we can change the text
    private Button mSubmit;

    /* ********************************************************************* *
     *                 Android Activity lifetime mgmt methods                *
     * ********************************************************************* */

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_layout);

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
        if (mLoggedIn) {
            //Log.i("SoundBox", "in onCreate, calling ShowMusicFolder with " + mCurrentPath);
            showMusicFolder(mCurrentPath);
        }
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

            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                Log.i("SoundBox", "Error authenticating", e);
            }
        }

        if (null == mFetcher) {
            // a null fetcher here means we need to show some content
            //Log.i("SoundBox", "in onResume, calling ShowMusicFolder with " + mCurrentPath);
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
        if (null != mFetcher) {
            mFetcher.setDropboxAPI(null);
            mFetcher.registerCompletionHandler(null);
        }
    } // onPause

    /* ********************************************************************* *
     *                           UI Modification                             *
     * ********************************************************************* */

    /**
      * Sets the login state to the value passed in; adjusts
      *  the text on the Button accordingly.
      */
    private void setLoggedIn(boolean loggedIn) {
        // TODO: pull the display text from a resource
        mLoggedIn = loggedIn;
        if (loggedIn) {
            mSubmit.setText("Unlink from Dropbox");
        } else {
            mSubmit.setText("Link with Dropbox");
        }
    }

    /** Convenience method to pop a toast message on the screen */
    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }
    
    /* ********************************************************************* *
     *                          Dropbox API calls                            *
     * ********************************************************************* */

    public void showMusicFolder(String aPath) {
        // TODO: check the input param, if null set to / or /Music
        // TODO: handle the case where path is a file
        // Metadata

        Log.i("SoundBox", "Showing Folder: " + aPath);

        // if there is a fetcher we're already grabbing something, wait for it...
        if (mFetcher != null) {
            Log.i("SoundBox", "still have mFetcher, aborting");
            return;
        }
        mCurrentPath = aPath;
        mFetcher = new SoundBoxAsyncFetcher();
        mFetcher.setDropboxAPI(mDBApi);
        mFetcher.registerCompletionHandler(this);
        mFetcher.execute(mCurrentPath);
    }

    @Override
    public void onTaskComplete(String [] aContentArray) {
        // drop our handle on the AsyncFetcher
        mFetcher = null;
        Log.i("SoundBox", "Task Complete, have dropped mFetcher");
     
        // TODO: insert the fragment into a container view, also get it pushed to the backstack
        //       related - handle the back button!!!
        SoundBoxListFragment listFrag = (SoundBoxListFragment) getFragmentManager().findFragmentById(R.id.list_frame);
        //if (listFrag == null || listFrag.getShownIndex() != index) {
            // Make new fragment to show this selection.
            listFrag = SoundBoxListFragment.newInstance(mCurrentPath, aContentArray);

            // Execute a transaction, replacing any existing fragment
            // with this one inside the frame.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.list_frame, listFrag);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
        //}
    }

    /* ********************************************************************* *
     *                         Authentication mgmt                           *
     * ********************************************************************* */

    // BIG TODO: move the Dropbox specific pieces into a service that includes
    //           the fetching of folders and handing back Entry arrays, instead
    //           of arrays of strings.

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

    // TODO: change this to erase only the key & secret
    /** remove the stored access token and secret */
    private void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }
}
