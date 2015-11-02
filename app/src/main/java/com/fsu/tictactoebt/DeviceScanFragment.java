package com.fsu.tictactoebt;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DeviceScanFragment extends Fragment {

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View scannedDeviceView = inflater.inflate(R.layout.bt_scanned_list, container, false);
        return scannedDeviceView;
    }
}
