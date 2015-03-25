package com.gfarcasiu.cardgameapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.gfarcasiu.client.MultiServerThread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.UUID;


public class HostGameActivity extends Activity {

    // Request Codes
    private static final int ENABLE_BT_REQUEST = 1;
    private static final int DISCOVERABLE_DURATION = 300;

    private BluetoothAdapter bluetoothAdapter;

    private HashSet<MultiServerThread> threads = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("Debug", "<Host Game on Create/>");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        setContentView(R.layout.activity_host_game);

        // Start Bluetooth Server
        initSequence();
        enableDiscoverablility();

        // TODO replace this thread with the multi-threaded server
        new Thread() {
            public void run() {
                try {
                    BluetoothServerSocket bluetoothServerSocket =
                            bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                                    "BluetoothTest", UUID.fromString("d76816b3-e96c-4a23-8c34-34fe39355e10"));

                    BluetoothSocket bluetoothSocket = bluetoothServerSocket.accept();

                    ObjectInputStream ois = new ObjectInputStream(bluetoothSocket.getInputStream());

                    String input = null;
                    while (input == null) {
                        Thread.sleep(100);
                        input = (String) ois.readObject();
                    }

                    // TODO this is bad form, change later
                    final String finalInput = input;

                    Log.i("Debug", "<Recieved message: " + input + "/>");
                    HostGameActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(getApplicationContext(), finalInput, Toast.LENGTH_LONG).show();
                            Button deviceView = new Button(getApplicationContext());
                            deviceView.setText(finalInput);
                            deviceView.setTextAppearance(getApplicationContext(), R.style.device_list_theme);
                            ((LinearLayout)findViewById(R.id.device_layout)).addView(deviceView);
                        }
                    });

                    //ois.close();

                    // Start the real server thread
                    MultiServerThread serverThread = new MultiServerThread(bluetoothSocket);
                    serverThread.start();
                    threads.add(serverThread);
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    Log.e("Error", "<Creating server failed/>");
                }
            }
        }.start();
    }

    @Override
    protected void onStop() {
        for (MultiServerThread thread : threads)
            thread.terminate();

        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_BT_REQUEST) {
            Log.i("Debug", "<Bluetooth enabled callback/>");
            bluetoothAdapter.startDiscovery();
        } else if (requestCode == DISCOVERABLE_DURATION) {
            // Discoverable accepted
            Log.i("Debug", "<Discoverable duration callback/>");
        } else if (requestCode == RESULT_CANCELED) {
            // Discoverable not accepted
            Log.i("Debug", "<Discoverable not accepted callback/>");
        }
    }

    public void startGame(View view) {
        startActivity(new Intent(this, HandActivity.class));
    }

    // HELPER METHODS

    private void initSequence() {
        bluetoothAdapter = getAdapter();
        if (bluetoothAdapter == null) {
            Log.e("Error", "<Bluetooth adapter not supported./>");
            this.onStop();
            return;
        }

        enableBluetooth(); // possibly async call
    }

    private BluetoothAdapter getAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    private void enableBluetooth() {
        Log.i("Debug", "<Enabling bluetooth/>");
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST);
    }

    private void enableDiscoverablility() {
        Log.i("Debug", "<Making device discoverable/>");
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
        startActivity(discoverableIntent);
    }
}
