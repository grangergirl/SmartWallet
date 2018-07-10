package com.example.asb.smartwallet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;
    private static final int WALLET_NOTIFICATION_ID = 001;
    public static BluetoothAdapter mBluetoothAdapter;
    public static BluetoothDevice mDevice;
    private ListView obj;
    public static WalletDBHelper mydb;
    public static NotificationCompat.Builder notificationBuilder, cardNotificationBuilder, noteInOutNotificationBuilder;
    public static NotificationManager notificationManager;
    public static boolean flag = true;
    public static IntentFilter filter1, filter2, filter3;
    public static Notification walletNotification;
    public static SparseIntArray noteDenomination = new SparseIntArray();
    public static SparseIntArray transactionType = new SparseIntArray();
    public BluetoothService bluetoothService;
    public static Intent intentService;
    public ImageButton buzzerButton;
    boolean mServiceBound = false;
    public static SharedPreferences sharedPref;
    public static SharedPreferences.Editor editSharedPref;
    public static boolean service_status;
    Switch serviceSwitch;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        service_status = sharedPref.getBoolean(getString(R.string.Service_started),false);
        intentService = new Intent(getApplicationContext(), BluetoothService.class);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buzzerButton = findViewById(R.id.buzz);
        setUpNotifications();
        serviceSwitch = findViewById(R.id.service);
        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_CODE);
                    } else {
                        if (!mServiceBound) {
                            startService(intentService);
                            bindService(intentService, mServiceConnection, Context.BIND_AUTO_CREATE);
                            registerReceiver(broadcastReceiver, new IntentFilter(BluetoothService.BROADCAST_ACTION));
                        }
                    }
                }
                else
                {
                    if (mServiceBound) {
                        unbindService(mServiceConnection);
                        mServiceBound = false;
                    }
                    try {
                        unregisterReceiver(broadcastReceiver);
                    } catch (Exception e){

                    }
                    stopService(intentService);
                }
                editSharedPref = sharedPref.edit();
                editSharedPref.putBoolean(getString(R.string.Service_started), isChecked);
                editSharedPref.apply();
            }
        });
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getBooleanExtra("updateUI",false)){
                onResume();
            }
           else if(intent.getBooleanExtra("enableBuzzzing",false)){
                try{
                    buzzerButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                         //   Toast.makeText(getApplicationContext(),"Buzzer enabled",Toast.LENGTH_SHORT).show();
                            bluetoothService.buzzAway();
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                }
                buzzerButton.setClickable(true);
            }
           else if(intent.getBooleanExtra("disableBuzzing",false)){
                buzzerButton.setClickable(false);
                Toast.makeText(getApplicationContext(),"Buzzer disabled",Toast.LENGTH_SHORT).show();
            }

        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        //Toast.makeText(getApplicationContext(),"In onResume()",Toast.LENGTH_SHORT).show();
        service_status = sharedPref.getBoolean(getString(R.string.Service_started),false);
        serviceSwitch.setChecked(service_status);
        mydb = new WalletDBHelper(this);
        ArrayList<Transaction> arrayList = mydb.getCompleteTransactionsWithIDs();
        Integer totalBalance = 0;
        Transaction transaction;
        String[] type = new String[arrayList.size()];
        String[] amount = new String[arrayList.size()];
        String[] tmstp = new String[arrayList.size()];
        Integer[] ids = new Integer[arrayList.size()];
        int j=0;
        for(int i=arrayList.size()-1; i >=0; i--){
            transaction = arrayList.get(i);
            if(transaction.type == 0){
                totalBalance+= transaction.amount;
                type[j] = "Inserted";
            } else {

                totalBalance-= transaction.amount;
                type[j] = "Removed";

            }
            amount[j] = "Rs. "+transaction.amount.toString();
            tmstp[j] = transaction.timestamp.substring(11,16);
            ids[j] = transaction.id;
            j++;
        }
        TransactionAdapter adapter=new TransactionAdapter(this, type,amount,tmstp,ids);
        TextView balance = findViewById(R.id.balance_info);
        balance.setText("Current Balance: "+totalBalance.toString());
        Log.i("Wallet",String.valueOf(j)+"are number of rows");
        obj=(ListView)findViewById(R.id.listView1);
        obj.setAdapter(adapter);
        obj.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                // TODO Auto-generated method stub
                //Log.i("Wallet",String.valueOf(view.getId())+" view "+String.valueOf(position)+" position "+String.valueOf(id)+" ID");
                int id_To_Search = view.getId();
                Bundle dataBundle = new Bundle();
                dataBundle.putInt(WalletDBHelper.TRANSACTIONS_COLUMN_ID, id_To_Search);
                Intent displayIntent = new Intent(getApplicationContext(),DisplayTransaction.class);
                displayIntent.putExtras(dataBundle);
                startActivity(displayIntent);
            }
        });
}
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.MyBinder myBinder = (BluetoothService.MyBinder) service;
            bluetoothService = myBinder.getService();
            mServiceBound = true;
        }
    };

//    public void initBluetoothSettings(){
// if (!mBluetoothAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_CODE);
//        }
//        else{
//            service_status = sharedPref.getBoolean(getString(R.string.Service_started),false);
//            if(service_status) {
//                startService(intentService);
//                bindService(intentService, mServiceConnection, Context.BIND_AUTO_CREATE);
//                registerReceiver(broadcastReceiver, new IntentFilter(BluetoothService.BROADCAST_ACTION));
//            }
//        }
//    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                service_status = sharedPref.getBoolean(getString(R.string.Service_started),false);
                if(service_status) {
                   // Toast.makeText(getApplicationContext(),"Starting BT service", Toast.LENGTH_SHORT).show();
                    startService(intentService);
                    bindService(intentService, mServiceConnection, Context.BIND_AUTO_CREATE);
                }
            }
        }
    }
//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//
//            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
//                //Do something if connected
//                removeNotification();
//                Toast.makeText(getApplicationContext(), "Wallet connected", Toast.LENGTH_SHORT).show();
//            }
//            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
//                //Do something if disconnected
//                Toast.makeText(getApplicationContext(), "Wallet out of range!", Toast.LENGTH_SHORT).show();
//                addNotification();
//            }
//            //else if...
//        }
//    };
    public void initDBSettings(){

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);

        switch(item.getItemId()) {
            case R.id.item1:Bundle dataBundle = new Bundle();
                dataBundle.putInt(WalletDBHelper.TRANSACTIONS_COLUMN_ID, 0);
                Intent intent = new Intent(getApplicationContext(),DisplayTransaction.class);
                intent.putExtras(dataBundle);
                startActivity(intent);
                return true;
            case R.id.item2:
                Intent showPlots = new Intent(getApplicationContext(),ShowStats.class);
                startActivity(showPlots);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keycode, event);
    }





    public void setUpNotifications(){
        Context context = getApplicationContext();
        Drawable drawableWallet = context.getResources().getDrawable(R.drawable.wallet);
        Bitmap bitmapWallet = ((BitmapDrawable)drawableWallet).getBitmap();
        Drawable drawableCards = context.getResources().getDrawable(R.drawable.cards);
        Bitmap bitmapCards = ((BitmapDrawable)drawableCards).getBitmap();
        notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.alert)
                        .setLargeIcon(bitmapWallet)
                        .setContentTitle("Smart Wallet")
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentText("Looks like you're forgetting your wallet!");
        cardNotificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.alert)
                        .setLargeIcon(bitmapCards)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentTitle("Card Alert")
                        .setContentText("Looks like you're forgetting your card!");
        noteInOutNotificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.alert)
                        .setLargeIcon(bitmapWallet)
                        .setContentTitle("Transaction detected")
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        notificationManager =  (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        walletNotification = notificationBuilder.build();
    }
    public static void addNotification() {
        try {
            // When you issue multiple notifications about the same type of event,
            // it’s best practice for your app to try to update an existing notification
            // with this new information, rather than immediately creating a new notification.
            // If you want to update this notification at a later date, you need to assign it an ID.
            // You can then use this ID whenever you issue a subsequent notification.
            // If the previous notification is still visible, the system will update this existing notification,
            // rather than create a new one. In this example, the notification’s ID is 001//
            notificationManager.notify(WALLET_NOTIFICATION_ID, walletNotification);
        } catch(Exception e){
            Log.i("Wallet","Error notifying");
        }

    }
    public static void removeNotification() {
        try {
            notificationManager.cancel(WALLET_NOTIFICATION_ID);
        } catch(Exception e){
            Log.i("Wallet","Error notifying");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mServiceBound) {
//            unbindService(mServiceConnection);
//            mServiceBound = false;
//        }
//        if (mBluetoothAdapter != null) {
//            mBluetoothAdapter.cancelDiscovery();
//        }
//        notificationManager.cancelAll();
//        stoptimertask();


        // Unregister broadcast listeners
        //this.unregisterReceiver(mReceiver);
    }
}

