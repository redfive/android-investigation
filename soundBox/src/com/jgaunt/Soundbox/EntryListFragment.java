package com.jgaunt.Soundbox;

import com.jgaunt.Soundbox.R;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class SigmaListFragment extends ListFragment {

    public static interface ClickListener {
        abstract void onListItemClicked(SigmaItem item);
    }

    private String mPath = "";
    
    public static SigmaListFragment newInstance(String aPath) {
        Log.i("SoundBox", "SigmaListFragment.newInstance(" + aPath + ")");
        SigmaListFragment listFrag = new SigmaListFragment();

        // pass through the path and contents as arguments
        Bundle args = new Bundle();
        args.putString("path", aPath);
        listFrag.setArguments(args);

        return listFrag;
    }

    @Override
    public void onAttach(Activity aActivity) {
        super.onAttach(aActivity);
        Log.i("SoundBox", "SigmaListFragment.onAttach()");
    }

    @Override
    public void onCreate(Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        mPath = getArguments().getString("path");
        Log.i("SoundBox", "SigmaListFragment.onCreate(" + mPath + ")");
    }

    @Override
    public View onCreateView(LayoutInflater aInflater,
                             ViewGroup aContainer,
                             Bundle aSavedInstanceState) {
        Log.i("SoundBox", "SigmaListFragment.onCreateView()");

        return aInflater.inflate(R.layout.list_fragment,
                                 aContainer,
                                 false);
    }

    @Override
    public void onActivityCreated(Bundle aSavedInstanceState) {
        super.onActivityCreated(aSavedInstanceState);
        Log.i("SoundBox", "SigmaListFragment.onActivityCreated()" );
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("SoundBox", "SigmaListFragment.onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("SoundBox", "SigmaListFragment.onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("SoundBox", "SigmaListFragment.onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("SoundBox", "SigmaListFragment.onStop()");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("SoundBox", "SigmaListFragment.onDestroyView()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("SoundBox", "SigmaListFragment.onDestroy()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i("SoundBox", "SigmaListFragment.onDetach()");
    }

    @Override
    public void onSaveInstanceState(Bundle aSavedInstanceState) {
        super.onSaveInstanceState(aSavedInstanceState);
        Log.i("SoundBox", "SigmaListFragment.onSaveInstanceState()");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO: handle the case where this click is on an actual track
        //       - might need the Item being the actual Entry object so we can ask
        //         if (item.isDir())
        SigmaItem  clickedItem = getListView().getItemAtPosition(position);
        ((ClickListener)getActivity()).onItemClick(clickedItem);
        //String txt = clickedItem.toString();
        //((SoundBox)getActivity()).showMusicFolder(mPath + "/" + txt);
    }
}
