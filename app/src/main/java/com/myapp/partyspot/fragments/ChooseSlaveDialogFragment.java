package com.myapp.partyspot.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.myapp.partyspot.activities.MainActivity;
import com.myapp.partyspot.R;

/**
 * Created by svaughan on 10/10/14.
 */
public class ChooseSlaveDialogFragment extends DialogFragment {
    // this class makes the slave choose which playlist to join

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_choose_slave_dialog, null);

        final MainActivity activity = (MainActivity) getActivity();
        final EditText myEditText = (EditText) view.findViewById(R.id.choose_dialog);
        builder.setView(view)
                .setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String name = myEditText.getText().toString();
                        if (!name.equals("")) { // check for empty string
                            activity.validate(name); // make sure that the playlist exists. If so, user will be redirected to playlist by HTTPFunctions
                        }
                    }
                })
                .setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

}
