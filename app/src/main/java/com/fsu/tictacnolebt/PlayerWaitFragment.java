package com.fsu.tictacnolebt;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Quothmar on 7/7/2015.
 */
public class PlayerWaitFragment extends Fragment {

    // SOURCE: http://stackoverflow.com/questions/15585749/progressdialog-spinning-circle
    ProgressDialog mSpinner;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View playerWaitView = inflater.inflate(R.layout.player_wait, container, false);
        mSpinner = new ProgressDialog(getActivity());
        mSpinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mSpinner.setTitle("Waiting for players...");
        mSpinner.show();
        return playerWaitView;
    }
}
