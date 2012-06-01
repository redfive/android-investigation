package com.jgaunt.RedfiveSensor;

import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;

// Copied
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.*;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

// Added
import android.widget.ArrayAdapter;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.Sensor;

import com.jgaunt.RedfiveSensor.RedfiveSensorView;

public class RedfiveSensor extends ListActivity
{
  private View mRootView = null;

  @Override
  public void onCreate(Bundle aBundle) {

    // Get the main View for the entire app, we'll load a child view in it that
    // will know how to populate the content for the sensors.
    mRootView = getLayoutInflater().inflate(R.layout.sensor_layout, null);
    setContentView(mRootView)

    super.onCreate(aBundle);

    // Get a handle to the area we will add other content into
    mContentArea = (RelativeLayout) findViewById(R.id.content_area);

    SensorManager sm = (SensorManager)getSystemService(SENSOR_SERVICE);
    List sensors  = sm.getSensorList(Sensor.TYPE_ALL);
    if (sensors.isEmpty()) {
      setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, new String [] {"No Sensors"} ));
    } else {
      setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, sensors));
    }

    //String[] sensorsAr = new String[] {"foo", "bar"};
    //setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, sensorsAr));


    ListView lv = getListView();
    lv.setTextFilterEnabled(true);

    lv.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view,
          int position, long id) {
        // When clicked, show a toast with the TextView text
        Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
            Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onResume(Bundle aBundle) {

  }
}
