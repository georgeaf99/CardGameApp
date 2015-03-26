package com.gfarcasiu.cardgameapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gfarcasiu.client.Client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.UUID;


public class ClientActivity extends Activity {

    // Request Codes
    private static final int ENABLE_BT_REQUEST = 1;

    private Client client;

    private BluetoothAdapter bluetoothAdapter;
    private HashMap<String, BluetoothDevice> discoveredDevices = new HashMap<>();

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device == null) return; // Should not happen often

                Log.i("Debug", "<Device found: " + device.getName() + " " + device.getAddress());

                if (device.getName() != null) {
                    // Set device texts
                    if (!discoveredDevices.containsKey(device.getName())) {
                        Button deviceView = new Button(getApplicationContext());
                        deviceView.setText(device.getName());
                        deviceView.setTextAppearance(getApplicationContext(), R.style.device_list_theme);
                        deviceView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                connectTo(((Button)v).getText().toString());
                            }
                        });

                        ((LinearLayout) findViewById(R.id.device_layout)).addView(deviceView);
                    }

                    discoveredDevices.put(device.getName(), device);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        setContentView(R.layout.activity_client);

        // Start the bluetooth client
        initSequence();
        registerReceiver();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mReceiver);
        //if (client != null)
            //client.terminate();

        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_BT_REQUEST) {
            Log.i("Debug", "<Bluetooth enabled callback/>");
            bluetoothAdapter.startDiscovery();
        }
    }

    // HELPER METHODS
    public void connectTo(final String name) {
        Log.i("Debug", "<On click called: " + name + "/>");

        new Thread() {
            public void run() {
                BluetoothDevice serverDevice = discoveredDevices.get(name);

                try {
                    BluetoothSocket bluetoothSocket = serverDevice.createRfcommSocketToServiceRecord(
                            UUID.fromString("d76816b3-e96c-4a23-8c34-34fe39355e10"));
                    bluetoothSocket.connect();

                    ObjectOutputStream oos = new ObjectOutputStream(bluetoothSocket.getOutputStream());

                    oos.writeObject(serverDevice.getName());

                    // Start real client
                    Client.createInstance(bluetoothSocket);
                    client = Client.getInstance();

                    Thread clientThread = new Thread(client);
                    clientThread.setPriority(Thread.MAX_PRIORITY);
                    clientThread.start();

                    ClientActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(ClientActivity.this, HandActivity.class);
                            intent.putExtra("isServer", false);
                            intent.putExtra("isNewGame", true);

                            startActivity(intent);
                        }
                    });
                } catch (IOException e) {
                    Log.e("Error", "<Connecting to server device failed/>");
                }
            }
        }.start();

        // Change UI to reflect current state
        ((TextView)findViewById(R.id.title)).setText(name + " Lobby");
    }

    private void initSequence() {
        bluetoothAdapter = getAdapter();
        if (bluetoothAdapter == null) {
            Log.e("Error", "<Bluetooth adapter not supported./>");
            this.onStop();
            return;
        }

        enableBluetooth(); // possibly async call
    }

    private void registerReceiver() {
        Log.i("Debug", "<Device being registered/>");
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }


    private void enableBluetooth() {
        Log.i("Debug", "<Enabling bluetooth/>");
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST);
    }

    private BluetoothAdapter getAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }
}
