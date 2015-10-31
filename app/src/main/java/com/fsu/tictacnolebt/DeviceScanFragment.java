package com.fsu.tictacnolebt;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Quothmar on 7/24/2015.
 */
public class DeviceScanFragment extends Fragment {

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View scannedDeviceView = inflater.inflate(R.layout.bt_scanned_list, container, false);
        return scannedDeviceView;
    }
}
