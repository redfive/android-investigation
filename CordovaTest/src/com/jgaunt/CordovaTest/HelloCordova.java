package com.jgaunt.CordovaTest;

import android.app.Activity;
import android.os.Bundle;
import org.apache.cordova.*;

public class HelloCordova extends DroidGap
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //super.loadUrl("file:///android_asset/www/index.html");
        super.loadUrl("file:///android_asset/www/accelerometer.html");
    }
}
