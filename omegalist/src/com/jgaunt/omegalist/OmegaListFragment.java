package com.jgaunt.omegalist;

import com.jgaunt.omegalist.OmegaList.OmegaListItem;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class OmegaListFragment extends ListFragment {
    private ClickListener mClickListener;

    public interface ClickListener { public void onListItemClicked(OmegaListItem aItem); }

    public static OmegaListFragment newInstance() {
        Log.i("OmegaList", "OmegaListFragment.newInstance()");
        OmegaListFragment listFrag = new OmegaListFragment();
        return listFrag;
    }


    @Override
    public View onCreateView(LayoutInflater aInflater,
                             ViewGroup aContainer,
                             Bundle aSavedInstanceState) {
        Log.i("OmegaList", "OmegaListFragment.onCreateView()");

        return aInflater.inflate(R.layout.list_fragment,
                                 aContainer,
                                 false);
    }

    @Override
    public void onAttach(Activity aActivity) {
        Log.i("SoundBox", "ListFragment.onAttach()");

        super.onAttach(aActivity);

        try {
            mClickListener = (ClickListener) aActivity;
        } catch (ClassCastException e) {
            throw new ClassCastException(aActivity.toString() + " must implement ClickListener");
        }
    }

    @Override
    public void onListItemClick(ListView aListView, View aView, int aPosition, long aId) {
        OmegaListItem clickedItem = (OmegaListItem) getListView().getItemAtPosition(aPosition);
        mClickListener.onListItemClicked(clickedItem);
    }
}
