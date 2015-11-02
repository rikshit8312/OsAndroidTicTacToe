package com.fsu.tictactoebt;

import android.bluetooth.BluetoothSocket;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.UnknownHostException;


public class BluetoothControl {

    private static BluetoothSocket mSocket;

    private OutputStream outputStream;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    public void setupBT() {
        try {

            bufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            outputStream = mSocket.getOutputStream();
            printWriter = new PrintWriter(outputStream);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String receiveMove() {
        String receivedMessage;
        try {
            receivedMessage = new String(bufferedReader.readLine() + '\n');
            receivedMessage.trim();
            return receivedMessage;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendMove(String string) {
        printWriter.println(string);
        printWriter.flush();
    }


    public static void setmSocket(BluetoothSocket socket) {
        mSocket = socket;
    }


    public BluetoothSocket getmSocket() {
        return mSocket;
    }

}
