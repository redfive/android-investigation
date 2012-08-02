package com.jgaunt.Soundbox;

import com.jgaunt.Soundbox.R;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SoundBoxListFragment extends ListFragment {

    private String mPath = "";
    
    public static SoundBoxListFragment newInstance(String aPath, String [] aContents) {
        SoundBoxListFragment listFrag = new SoundBoxListFragment();

        // pass through the path and contents as arguments
        Bundle args = new Bundle();
        args.putString("path", aPath);
        args.putStringArray("contents", aContents);
        listFrag.setArguments(args);

        return listFrag;
    }

    @Override
    public void onActivityCreated(Bundle aSavedInstanceState) {
        super.onActivityCreated(aSavedInstanceState);

        String [] contents = getArguments().getStringArray("contents");
        mPath = getArguments().getString("path");

        // TODO: there is a bug here that caused this next call to fail with a null pointer
        //       in the ArrayAdapter class, a null list.
        // Populate list with our static array of titles.
        setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, contents));

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO: handle the case where this click is on an actual track
        //       - might need the Item being the actual Entry object so we can ask
        //         if (item.isDir())
        Object  clickedItem = getListView().getItemAtPosition(position);
        String txt = clickedItem.toString();
        ((SoundBox)getActivity()).showMusicFolder(mPath + "/" + txt);
    }
}