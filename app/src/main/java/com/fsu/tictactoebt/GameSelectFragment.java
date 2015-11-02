package com.fsu.tictactoebt;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class GameSelectFragment extends Fragment implements View.OnClickListener {

    Button mBtnPlayAsHost;
    Button mBtnPlayAsClient;
    ProgressDialog mSpinnerDialog;
    private BluetoothAdapter mBluetoothAdapter;

    public final static String EXTRA_KEY = "com.fsu.tictactoebt";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View gameSelectView = inflater.inflate(R.layout.game_select, container, false);

        mBtnPlayAsHost = (Button) gameSelectView.findViewById(R.id.btn_play_as_host);
        mBtnPlayAsClient = (Button) gameSelectView.findViewById(R.id.btn_play_as_client);

        mBtnPlayAsHost.setOnClickListener(this);
        mBtnPlayAsClient.setOnClickListener(this);

        return gameSelectView;
    }

    public void onClick(View v) {

        Fragment nextFragment;
        BluetoothConnect btConnActivity = (BluetoothConnect) getActivity();

        if (v == mBtnPlayAsHost) {

            BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
            if (!bt.isEnabled()) {
                Toast.makeText(getActivity(), "Bluetooth denied by user", Toast.LENGTH_LONG).show();
                return;
            }

            mSpinnerDialog = new ProgressDialog(getActivity());
            mSpinnerDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mSpinnerDialog.setTitle("Host Server Started\nWaiting for player...");
            mSpinnerDialog.show();

            btConnActivity.startAcceptThread();


        } else if (v == mBtnPlayAsClient) {

            btConnActivity.scanForDevices();


        }

    }

}
