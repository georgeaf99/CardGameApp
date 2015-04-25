package com.gfarcasiu.client;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.gfarcasiu.game.*;
import com.gfarcasiu.utilities.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;

public class Client implements Runnable {
    public static Client instance;

    private volatile Game game;
    private volatile boolean terminated = false;

    private BluetoothSocket bluetoothSocket;
    private volatile ObjectOutputStream oos;
    private volatile ObjectInputStream ois;

    // Singleton design pattern
    private Client(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;

        initialize();
    }

    public static Client getInstance() {
        return instance;
    }

    public static void createInstance(BluetoothSocket bluetoothSocket) {
        Log.i("Debug", "<Client instance created/>");
        instance = new Client(bluetoothSocket);
    }

    // NOTE : Cannot be called before the thread starts or run is called
    public void executeAction(Method outgoing, Object ... args) {
        Log.i("Debug", "<Client execute action: " + outgoing.getName() + " " + args.length + "/>");

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
        Log.i("Debug", "<Client thread running./>");

        try {
            try {
                game = (Game)ois.readObject();
                Log.i("Debug", "<Client thread has received game/>");

                while (!terminated) {
                    // Read method
                    Method incoming = ((SerializableMethod) ois.readObject()).getMethod();
                    Object[] inArgs = (Object[]) ois.readObject();

                   HelperFunctions.executeMethod(game, incoming, inArgs);
                }
            } catch (ClassNotFoundException e) {
                Log.e("Error", "<Exception encountered during client execution/>");
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // TODO close resources eventually
            //try {
                //Log.i("Debug", "<Closing all resources on the client side/>");
                //bluetoothSocket.close();
                //oos.close();
                //ois.close();
            //} catch (IOException e) {
                // DO NOTHING
            //}
        }
    }

    private void initialize() {
        try {
            oos = new ObjectOutputStream(bluetoothSocket.getOutputStream());
            ois = new ObjectInputStream(bluetoothSocket.getInputStream());
        } catch (IOException e) {
            Log.i("Error", "<Initialization of resources failed/>");
            e.printStackTrace();
        }
    }

    public void terminate() {
        terminated = true;
    }

    public Game getGame() {
        return game;
    }
}
