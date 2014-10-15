package com.myapp.partyspot.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;

import com.myapp.partyspot.activities.MainActivity;
import com.myapp.partyspot.R;
import com.myapp.partyspot.handlers.HTTPFunctions;

/**
 * Created by svaughan on 10/2/14.
 */
public class SlaveFragment extends Fragment {
    // This fragment holds the main view for the slave phone

    // class constructor
    public SlaveFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.hostmenu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                // this is your adapter that will be filtered
                return true;
            }

            public boolean onQueryTextSubmit(String query) {
                HTTPFunctions http = new HTTPFunctions(getActivity()); // HANDLE SPACES ALSO CWALLACE
                String URL = "https://api.spotify.com/v1/search?q=" + query + "&type=track";
                ((MainActivity) SlaveFragment.this.getActivity()).changeToSlaveSearchResults();
                http.getSlaveSearch(URL);
                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_slave_main, container, false);
        setHasOptionsMenu(true);

        // get buttons to add click listeners to them
        final Button main_menu = (Button) rootView.findViewById(R.id.main_menu);
        final Button slave_main = (Button) rootView.findViewById(R.id.slave_main);
        final Button volume = (Button) rootView.findViewById(R.id.volume);

        // set muted icon
        if (((MainActivity)getActivity()).muted) {
            volume.setBackground(getResources().getDrawable(R.drawable.volumeoff));
        } else {
            volume.setBackground(getResources().getDrawable(R.drawable.volumeon));
        }

        // change muted state and icon
        volume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((MainActivity)getActivity()).muted) {
                    ((MainActivity)getActivity()).setNotMuted();
                    volume.setBackground(getResources().getDrawable(R.drawable.volumeon));
                } else {
                    ((MainActivity)getActivity()).setMuted();
                    volume.setBackground(getResources().getDrawable(R.drawable.volumeoff));
                }
            }
        });

        // return to main menu
        main_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).changeToMainFragment();
            }
        });

        return rootView;
    }

}
