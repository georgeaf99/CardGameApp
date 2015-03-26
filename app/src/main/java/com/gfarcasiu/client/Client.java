package com.gfarcasiu.client;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.gfarcasiu.game.*;
import com.gfarcasiu.utilities.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Client implements Runnable {
    public static Client instance;

    private Game game;
    private volatile boolean terminated = false;

    BluetoothSocket bluetoothSocket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    // Singleton design pattern
    private Client(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
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

        //if (oos == null || ois == null)
            //initialize();

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
        //if (oos == null || ois == null)
            initialize();

        try {
            try {
                game = (Game)ois.readObject();
                Log.i("Debug", "<Client thread has recieved game/>");

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
        }
    }

    public void terminate() {
        terminated = true;
    }

    public Game getGame() {
        return game;
    }
}
