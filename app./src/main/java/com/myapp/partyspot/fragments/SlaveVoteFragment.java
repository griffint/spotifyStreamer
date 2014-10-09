package com.myapp.partyspot.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.myapp.partyspot.activities.MainActivity;
import com.myapp.partyspot.R;

/**
 * Created by svaughan on 10/2/14.
 */
public class SlaveVoteFragment extends Fragment {
    // This fragment allows the slave to vote on other suggested songs

    // class fields

    // class constructor
    public SlaveVoteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_slave_vote, container, false);

        final Button main_menu = (Button) rootView.findViewById(R.id.main_menu);

        main_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).changeToMainFragment();
            }
        });

        return rootView;
    }
}
