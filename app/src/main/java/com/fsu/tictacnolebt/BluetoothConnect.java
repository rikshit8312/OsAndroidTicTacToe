package com.fsu.tictacnolebt;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.location.SettingInjectorService;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Daniel Carroll on 7/6/2015.
 */
public class BluetoothConnect extends MainActivity {

    //UUID for application (will need a static UUID)
    private static final UUID MY_UUID =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private static final String NAME = "ParanoidAndroidBT";
    private static final int REQUEST_ENABLE_BT = 2;
    //Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter;

    // Data members and methods for accept/connect threads, used by GameSelectFragment.
    // Added by Scott on 7/24/2015
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    public AcceptThread getAcceptThread() { return mAcceptThread; }
    public ConnectThread getConnectThread() { return mConnectThread; }

    public final static String EXTRA_KEY = "com.fsu.tictacnolebt";

    public void startAcceptThread() {

        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
    }
    public void startConnectThread(BluetoothDevice device) {

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    // An array to hold the devices found by Bluetooth scan.
    // NOTE: Changed to HashMap (7/26/2015).
    //private ArrayList<BluetoothDevice> mScannedDevices = new ArrayList<BluetoothDevice>();
    private HashMap<TextView, BluetoothDevice> mScannedDevices = new HashMap<TextView, BluetoothDevice>();

    /**
     * Overrides onCreate method for setting up bluetooth when class called
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Check if adapter is present, if not show toast
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null){
            Toast.makeText(this, "This phone does not support Bluetooth", Toast.LENGTH_SHORT);
        }

        //if present check to see if bluetooth turned on
        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);


        }

    }


   /*
   * We don't have to use the dialog box, it's there if we need it
   * *//*
    Creates an Alert Dialog box that gives an option
     *//*
    public void relationshipDialog(){

        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(this);

        mAlertDialog.setMessage("Choose Game Type");

        mAlertDialog.setPositiveButton("Host Game", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               //Accpet Thread here
            }
        });

        mAlertDialog.setNegativeButton("Find Player", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
            }
        });

    }
*/


    /**
     * Checks paired bluetooth device and then created array adapter so list can be displayed
     */
    public void checkPaired(){

        //Array Adapter for paired list to choose from
        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this, R.layout.bt_paired_list);

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if(pairedDevices.size() > 0){
            for (BluetoothDevice device : pairedDevices){
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    // Event listener for devices discovered.
    // [SOURCE] http://developer.android.com/guide/topics/connectivity/bluetooth.html
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            // If a device has been found...
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Get the bluetooth device.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //mScannedDevices.add(device);

                // TODO: Communicate with the device found to make sure it is hosting TicTacNole.

                // Create a view for it to be clicked on.
                TextView deviceEntry = new TextView(BluetoothConnect.this);
                String deviceName = new String(device.getAddress());
                if (device.getName() != null)
                    deviceName += " (" + device.getName() + ")";
                deviceEntry.setText(deviceName);
                deviceEntry.setTextSize(20);   // hard-coded for now, can change later

                // Set the click listener of the event.
                deviceEntry.setOnClickListener(new TextView.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // TODO: Attempt connection with device using connectThread.
                        //Toast.makeText(BluetoothConnect.this, "TODO: Connect with device and begin game", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(BluetoothConnect.this, mScannedDevices.get(v).getAddress(), Toast.LENGTH_SHORT).show();
                        //mBluetoothAdapter.cancelDiscovery();
                        startConnectThread(mScannedDevices.get(v));

                    }
                });

                // Put the TextView as the key and the BluetoothDevice as the object in a HashMap.
                mScannedDevices.put(deviceEntry, device);

                // Show it on the list.
                LinearLayout scannedDevices = (LinearLayout)findViewById(R.id.bt_scanned_list);
                scannedDevices.addView(deviceEntry);
            }
        }
    };

    // Scan for nearby Bluetooth devices.
    public void scanForDevices() {

        // Switch to a scanned device fragment.
        // SOURCE: http://developer.android.com/training/basics/fragments/fragment-ui.html
        DeviceScanFragment deviceScanFragment = new DeviceScanFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, deviceScanFragment);
        transaction.addToBackStack(null);   // so user can press 'back'
        transaction.commit();

        // Register the BroadcastReceiver to receive signals back from devices.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        // Begin the device scan.
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * Accept Thread Class - gets incoming connection then hands socket off to Bluetooth Control
     */
    private class AcceptThread extends Thread{

        private final BluetoothServerSocket mmServerSocket;

        /**
         * accept thread for incoming bt connection/socket
         */
        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        /**
         *
         */
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
                //If connection was accepted
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

            // If connection was successful, start the Tic Tac Nole activity as a 'Host'.
            if (socket != null) {
                Intent intent = new Intent(BluetoothConnect.this, TicTacNole.class);
                intent.putExtra(EXTRA_KEY + ".numHumans", 2);
                intent.putExtra(EXTRA_KEY + ".role", "host");
                startActivity(intent);
            }
        }

        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }

    }

    /**
     *
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        /**
         * Constructor - store UUID in tmp socket
         * @param device
         */
        public ConnectThread(BluetoothDevice device){

            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try{
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        /**
         * ConnectThread run, will try to connect through socket
         */
        public void run(){

            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {

                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();

                //pass socket

            }catch (IOException connectException){
                try {
                    mmSocket.close();
                }catch (IOException closeException){}
                return;
            }
            // Do work to manage the connection (in a separate thread)
            BluetoothControl.setmSocket(mmSocket);

            // After connecting, start the Tic Tac Nole activity as a 'Client'.
            Intent intent = new Intent(BluetoothConnect.this, TicTacNole.class);
            intent.putExtra(EXTRA_KEY + ".numHumans", 2);
            intent.putExtra(EXTRA_KEY + ".role", "client");
            startActivity(intent);

        }

        /**
         *  will cancel an in progress connection and close socket
         */
        public void cancel(){
            try {
                mmSocket.close();
            }catch (IOException e) {}
        }
    }



}
