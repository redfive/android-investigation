package com.jgaunt.omegalist;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class OmegaList extends ListActivity
{
    public class OmegaListItem {
        public String omegaName = "";
        public String omegaStatus = "";

        public OmegaListItem( String aName, String aStatus ) {
            omegaName = aName;
            omegaStatus = aStatus;
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
                             new OmegaListItem("one", "Alpha"),
                             new OmegaListItem("two", "Beta"),
                             new OmegaListItem("three", "Gamma") };

        ArrayAdapter<OmegaListItem> listAdapter = new ArrayAdapter<OmegaListItem> (this, android.R.layout.simple_list_item_1, items);

        ListView lView = (ListView) findViewById(android.R.id.list);
        lView.setAdapter(listAdapter);
    }

    @Override
    protected void onListItemClick( ListView aListView , View aView, int aPosition, long aId ) {
        OmegaListItem item = (OmegaListItem) getListView().getItemAtPosition(aPosition);
        showToast( "> " + item.toString() + " status: " + item.omegaStatus );
        
    }

    /** Convenience method to pop a toast message on the screen */
    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

}
