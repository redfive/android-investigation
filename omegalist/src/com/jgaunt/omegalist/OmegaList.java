package com.jgaunt.omegalist;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class OmegaList extends Activity
                       implements OmegaListFragment.ClickListener
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

        OmegaListFragment listFrag = OmegaListFragment.newInstance();
        listFrag.setListAdapter(listAdapter);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.main_frame, listFrag);
        ft.commit();

    }

    @Override
    public void onListItemClicked( OmegaListItem aItem ) {
        showToast( "Fragment " + aItem.toString() + " status: " + aItem.omegaStatus );
    }

    /** Convenience method to pop a toast message on the screen */
    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

}
