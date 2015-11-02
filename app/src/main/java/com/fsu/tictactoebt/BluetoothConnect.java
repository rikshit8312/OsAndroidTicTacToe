package com.fsu.tictactoebt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;


public class BluetoothConnect extends MainActivity {

    //UUID for application (will need a static UUID)
    private static final UUID MY_UUID =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private static final String NAME = "ParanoidAndroidBT";
    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothAdapter mBluetoothAdapter;

    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;

    public AcceptThread getAcceptThread() {
        return mAcceptThread;
    }

    public ConnectThread getConnectThread() {
        return mConnectThread;
    }

    public final static String EXTRA_KEY = "com.fsu.tictactoebt";

    public void startAcceptThread() {

        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
    }

    public void startConnectThread(BluetoothDevice device) {

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }


    private HashMap<TextView, BluetoothDevice> mScannedDevices = new HashMap<TextView, BluetoothDevice>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "This phone does not support Bluetooth", Toast.LENGTH_SHORT);
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);


        }

    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                TextView deviceEntry = new TextView(BluetoothConnect.this);
                String deviceName = new String(device.getAddress());
                if (device.getName() != null)
                    deviceName += " (" + device.getName() + ")";
                deviceEntry.setText(deviceName);
                deviceEntry.setTextSize(25);
                deviceEntry.setTextColor(Color.parseColor("#ffffff"));

                deviceEntry.setOnClickListener(new TextView.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        startConnectThread(mScannedDevices.get(v));

                    }
                });

                mScannedDevices.put(deviceEntry, device);

                LinearLayout scannedDevices = (LinearLayout) findViewById(R.id.bt_scanned_list);
                scannedDevices.addView(deviceEntry);
            }
        }
    };

    public void scanForDevices() {

        DeviceScanFragment deviceScanFragment = new DeviceScanFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, deviceScanFragment);
        transaction.addToBackStack(null);   // so user can press 'back'
        transaction.commit();

        // Register the BroadcastReceiver to receive signals back from devices.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        mBluetoothAdapter.startDiscovery();
    }

    private class AcceptThread extends Thread {

        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                if (socket != null) {
                    BluetoothControl.setmSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }

            if (socket != null) {
                Intent intent = new Intent(BluetoothConnect.this, TicTacToe.class);
                intent.putExtra(EXTRA_KEY + ".numHumans", 2);
                intent.putExtra(EXTRA_KEY + ".role", "host");
                startActivity(intent);
            }
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }

    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {

            BluetoothSocket tmp = null;

            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        public void run() {

            mBluetoothAdapter.cancelDiscovery();

            try {

                mmSocket.connect();


            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }
            BluetoothControl.setmSocket(mmSocket);
            Intent intent = new Intent(BluetoothConnect.this, TicTacToe.class);
            intent.putExtra(EXTRA_KEY + ".numHumans", 2);
            intent.putExtra(EXTRA_KEY + ".role", "client");
            startActivity(intent);

        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }


}
