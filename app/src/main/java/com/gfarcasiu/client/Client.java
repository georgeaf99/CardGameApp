package com.gfarcasiu.client;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.gfarcasiu.game.*;
import com.gfarcasiu.utilities.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class Client implements Runnable {
    private Game game;
    private volatile boolean terminated = false;

    BluetoothSocket bluetoothSocket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public Client(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
    }

    // NOTE : Cannot be called before the thread starts or run is called
    public void executeAction(Method outgoing, Object ... args) {
        try {
            // Write serialized method
            oos.writeObject(new SerializableMethod(outgoing));
            oos.writeObject(args);
        } catch (IOException e) {
            Log.e("Error", "<IOException encountered when initializing resources/>");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        initialize();

        try {
            try {
                String test = (String)ois.readObject();
                Log.i("Debug", "!!!It worked: " + test);
                game = (Game)ois.readObject();

                while (!terminated) {
                    // Read method
                    Method incoming = ((SerializableMethod) ois.readObject()).getMethod();
                    Object[] inArgs = (Object[]) ois.readObject();

                    // Execute method
                    switch (inArgs.length) {
                        case 0: incoming.invoke(game); break;
                        case 1: incoming.invoke(game, inArgs[0]); break;
                        case 2: incoming.invoke(game, inArgs[0], inArgs[1]); break;
                        case 3: incoming.invoke(game, inArgs[0], inArgs[1], inArgs[2]); break;
                        default: System.err.println("<Method argument number is incorrect./>");
                    }
                }
            } catch (InvocationTargetException | ClassNotFoundException | IllegalAccessException e) {
                Log.e("Error", "<Exception encountered during client execution/>");
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bluetoothSocket.close();
                oos.close();
                ois.close();
            } catch (IOException e) {
                // DO NOTHING
            }
        }
    }

    private void initialize() {
        try {
            oos = new ObjectOutputStream(bluetoothSocket.getOutputStream());
            ois = new ObjectInputStream(bluetoothSocket.getInputStream());
        } catch (IOException e) {
            Log.i("Error", "<Initialization of resources failed/>");
        }
    }

    public void terminate() {
        terminated = true;
    }

    public Game getGame() {
        return game;
    }
}
