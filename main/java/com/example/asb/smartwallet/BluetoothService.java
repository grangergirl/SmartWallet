package com.example.asb.smartwallet;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;
import static com.example.asb.smartwallet.MainActivity.addNotification;
import static com.example.asb.smartwallet.MainActivity.filter1;
import static com.example.asb.smartwallet.MainActivity.filter2;
import static com.example.asb.smartwallet.MainActivity.filter3;
import static com.example.asb.smartwallet.MainActivity.flag;
import static com.example.asb.smartwallet.MainActivity.mBluetoothAdapter;
import static com.example.asb.smartwallet.MainActivity.mDevice;
import static com.example.asb.smartwallet.MainActivity.mydb;
import static com.example.asb.smartwallet.MainActivity.noteDenomination;
import static com.example.asb.smartwallet.MainActivity.noteInOutNotificationBuilder;
import static com.example.asb.smartwallet.MainActivity.transactionType;

public class BluetoothService extends Service {
    public static WalletConnect arduinoConnect = null;
    public static ConnectedThread mConnectedThread = null;
    private static String LOG_TAG = "Wallet Service";
    private IBinder mBinder = new MyBinder();
    public static final String BROADCAST_ACTION = "com.example.asb.smartwallet.eventReceiver";

   Intent intent;

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "in onBind");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "in onUnbind");
        return true;
    }

    public class MyBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    public BluetoothService() {
    }

    @Override
    public void onCreate() {
       // Toast.makeText(this, "Smart Wallet service created!", Toast.LENGTH_LONG).show();
        intent = new Intent(BROADCAST_ACTION);
        startBTService();
//        try{
//            mConnectedThread.write("2".getBytes());
//        }catch(Exception e){
//
//        }
    }
    public void updateUI() {
        Log.d(TAG, "entered updateUI in service");
        intent.putExtra("updateUI", true);
        intent.putExtra("enableBuzzzing", false);
        intent.putExtra("disableBuzzing", false);
        sendBroadcast(intent);
    }
    public void enableBuzzing(){
        Log.d(TAG, "entered enable buzzer in service");
        intent.putExtra("updateUI", false);
        intent.putExtra("enableBuzzzing", true);
        intent.putExtra("disableBuzzing", false);
        sendBroadcast(intent);
    }
    public void disableBuzzing(){
        Log.d(TAG, "entered disable buzzer in service");
        intent.putExtra("updateUI", false);
        intent.putExtra("enableBuzzzing", false);
        intent.putExtra("disableBuzzing", true);
        sendBroadcast(intent);
    }

    @Override
    public void onStart(Intent intent, int startId) {
      //  Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
    }
    @Override
    public void onDestroy() {
        try {
            try {
                this.unregisterReceiver(mReceiver);
            }catch(Exception e){

            }
            arduinoConnect.cancel();
        } catch (Exception e){
            Log.i("Wallet","No arduino to disconnect");
        }


    }
    public void buzzAway(){
        mConnectedThread.write("2".getBytes());
        Toast.makeText(getApplicationContext(),"Triggering ringer",Toast.LENGTH_SHORT).show();
    }
    public void startBTService(){
       // enableBuzzing();
        if(mBluetoothAdapter!=null)
        {
            try{
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        Log.i("Wallet","Detecting "+device.getName());
                        try{
                            if(device.getName().equals("HC-05")){
                                Log.i("Wallet","Found "+device.getName()+"!");

                                mDevice = device;
                                filter1 = new IntentFilter(mDevice.ACTION_ACL_CONNECTED);
                                filter2 = new IntentFilter(mDevice.ACTION_ACL_DISCONNECT_REQUESTED);
                                filter3 = new IntentFilter(mDevice.ACTION_ACL_DISCONNECTED);
                                this.registerReceiver(mReceiver, filter1);
                                this.registerReceiver(mReceiver, filter2);
                                this.registerReceiver(mReceiver, filter3);
                                Log.i("Wallet","Assigned device!");
                                break;
                            }
                        } catch(Exception e){
                            mDevice = null;
                        }
                    }
                }
                try{
                    Log.i("Wallet","Going to start Arduino connect");
                    arduinoConnect = new WalletConnect(mDevice);
                    arduinoConnect.start();
                    Log.i("Wallet","Started Arduino connect");
                }catch (Exception e){
                    e.printStackTrace();
                }
            } catch (Exception e){
                Toast.makeText(this,"Not paired with wallet!",Toast.LENGTH_SHORT).show();
            }
        }
    }
    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                MainActivity.removeNotification();
                Toast.makeText(getApplicationContext(), "Wallet connected", Toast.LENGTH_SHORT).show();
            }
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Wallet out of range!", Toast.LENGTH_SHORT).show();
                addNotification();
            }
        }
    };
    public class WalletConnect extends Thread{
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private UUID MY_UUID;

        public WalletConnect(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try{
                //uuid = tManager.getImei();
                MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                Log.i("Wallet","My UUID is "+MY_UUID.toString());
            }catch (SecurityException e){
                Log.i("Wallet","Error getting UUID");
            }
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(),"Error connecting to module", Toast.LENGTH_SHORT).show();
            }
            mmSocket = tmp;
        }
        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                Log.i("Wallet","run() in WalletConnect");
                mmSocket.connect();
                Log.i("Wallet","Connected socket, enable buzzer");

            } catch (IOException connectException) {
                try {
                    //disableBuzzing();
                    mmSocket.close();
                    Log.i("Wallet","Closed socket");

                } catch (IOException closeException) {
                    Log.i("Wallet","Can't close socket");
                }
            }

            Log.i("Wallet","Started connect thread");
            mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.start();
            Log.i("Wallet","Should have started connect thread");
        }
        public void cancel() {
            try {
                mConnectedThread.cancel();
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            noteDenomination.put(98,20);
            noteDenomination.put(99,50);
            noteDenomination.put(100,100);
            transactionType.put(48,0);
            transactionType.put(49,1);
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {

            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;
            int iterator = 0;
            enableBuzzing();
            while (true) {
                // Log.i("Wallet","Data incoming?");
//                if(flag){
//                    flag = !flag;
//                MainActivity.notificationManager.notify(002, MainActivity.cardNotificationBuilder.build());
//                enableBuzzing();
//                }
                try {
                    Integer amount = -1;
                    Integer type = -1;
                    String timestamp;
                    int newBytes = mmInStream.read(buffer, bytes, buffer.length - bytes);
                    bytes += newBytes;
                    for(int i = begin; i < bytes; i++) {
                        Integer val = (int)buffer[i];
                        if(val == 65) {
                            Date d=new Date(new Date().getTime());
                            timestamp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(d);
                            iterator++;
                            Log.i("Wallet","End of one set of values");
                            try {
                                mydb.insertTransaction(type, amount, timestamp);
                                noteInOutNotificationBuilder.setContentText("Transaction of Rs."+amount.toString()+" detected");
                                MainActivity.notificationManager.notify(002, MainActivity.noteInOutNotificationBuilder.build());
                                updateUI();
                            }catch(Exception e){
                                Log.i("Wallet","Error receiving transaction details");
                            }
                            begin = i + 1;
                            if(i == bytes - 1) {
                                bytes = 0;
                                begin = 0;
                            }
                        } else if(val >=97 && val<=102){
                            amount = noteDenomination.get(val);
                            Log.i("Wallet","Note obtained: "+amount.toString());
                        }else if(val == 48 || val == 49){
                            type = transactionType.get(val);
                            if(val == 48)
                            Log.i("Wallet","Incoming note");
                            else
                                Log.i("Wallet","Outgoing note");
                        } else{
                            Log.i("Wallet","Obtained value - 48:"+val.toString());
                        }
                    }
                } catch (IOException e) {
                    // Log.i("Wallet",  "Error reading data");
                    //e.printStackTrace();
                }
            }
        }
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }
        public void cancel() {
            try {
                disableBuzzing();
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}