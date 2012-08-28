package com.jgaunt.omegalist;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class OmegaList extends Activity
{
    public class OmegaListItem {
        public String omegaName = "";

        public OmegaListItem( String aName ) {
            omegaName = aName;
        }

        public String toString () {
            return omegaName;
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        OmegaListItem [] items = {
                             new OmegaListItem("one"),
                             new OmegaListItem("two"),
                             new OmegaListItem("three") };

        ArrayAdapter<OmegaListItem> listAdapter = new ArrayAdapter<OmegaListItem> (this, android.R.layout.simple_list_item_1, items);

        ListView lView = (ListView) findViewById(R.id.list);
        lView.setAdapter(listAdapter);
    }

}
