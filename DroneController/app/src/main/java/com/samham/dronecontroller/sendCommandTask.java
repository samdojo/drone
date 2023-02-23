package com.samham.dronecontroller;

import android.os.AsyncTask;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class sendCommandTask extends AsyncTask<DatagramPacket, Void, Void> {
    private static DatagramSocket socket = null;

    protected Void doInBackground(DatagramPacket... packets) {
        try {
            if (socket != null) {
                socket.send(packets[0]);
            }
            else {
                Log.e("TAG", "Did not set socket");
            }
        } catch (Exception e) {
            Log.e("TAG", "Failed to send packet");
        }
        return null;
    }
    public static void setSocket(DatagramSocket socket) {
        sendCommandTask.socket = socket;
    }
}
