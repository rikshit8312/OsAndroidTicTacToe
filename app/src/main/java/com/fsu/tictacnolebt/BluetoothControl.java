package com.fsu.tictacnolebt;

import android.bluetooth.BluetoothSocket;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.UnknownHostException;

/**
 * Created by DanielCarroll on 7/5/2015.
 */
public class BluetoothControl {

    private static BluetoothSocket mSocket;

    private OutputStream outputStream;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    /**
     * sets up bluetooth controls/ sets buffer reader, output stream, and print writer
     */
    // Made public by Scott (7/26/2015)
    //private void setupBT(){
    public void setupBT() {
        try {

            bufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            outputStream = mSocket.getOutputStream();
            printWriter = new PrintWriter(outputStream);

        }catch (UnknownHostException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * recieves move from connected device
     * @return
     */
    public String receiveMove(){
        String receivedMessage;
        try {
            //get from buffer to save to string
            receivedMessage = new String(bufferedReader.readLine()+ '\n');
            //get rid of leading and trailing whitespace
            receivedMessage.trim();
            return receivedMessage;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sends moves from device
     * @param string
     */
    public void sendMove(String string){
        //passes string then flushes when done
        printWriter.println(string);
        printWriter.flush();
    }

    /**
     * sets socket for controls
     * @param _socket
     */
    public static void setmSocket(BluetoothSocket _socket){
        mSocket = _socket;
    }

    /**
     * gets bluetooth socket
     * @return
     */
    public BluetoothSocket getmSocket(){
        return mSocket;
    }

}
