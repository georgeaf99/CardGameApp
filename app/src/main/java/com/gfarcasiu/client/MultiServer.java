package com.gfarcasiu.client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

import com.gfarcasiu.game.*;
import com.gfarcasiu.utilities.*;

public class MultiServer implements Runnable {
    public static final int PORT = 19257;
    private static final int MAX_PLAYERS = 4;

    private boolean terminated = false;

    private Game game;
    private BluetoothAdapter adapter;

    public MultiServer(Game game, BluetoothAdapter adapter) {
        this.game = game;
        this.adapter = adapter;
    }

    public void run() { // NOTE : does not handle port collisions
        MultiServerThread.setGame(game); // SETS THE GAME

        Log.i("Debug", "<Bluetooth server thread started/>");


        // TODO: the UUID should be something unique
        try (BluetoothServerSocket bluetoothServerSocket =
                     adapter.listenUsingRfcommWithServiceRecord("CardGameApp", UUID.randomUUID())) {

            Log.i("Debug", "<Bluetooth server thread looking for connections/>");

            while (!terminated && MultiServerThread.getThreadCount() < MAX_PLAYERS) {
                new MultiServerThread(bluetoothServerSocket.accept()).start();
                Log.i("Debug", "<Bluetooth device connection established/>");
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + PORT);
            System.exit(-1);
        }
    }

    public void terminate() {
        terminated = true;
    }

    public Game getGame() {
        return game;
    }

    // TESTING ONLY
    /*public static void main(String[] args) {
        MultiServer server = new MultiServer(new Game());
        new Thread(server).start();
    }*/
}

class MultiServerThread extends Thread {
    private static Game game; // Game object to make changes to
    private static int threadCount = 0;

    private BluetoothSocket socket = null;
    private volatile boolean terminated = false;

    public MultiServerThread(BluetoothSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        threadCount++;

        try (
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        ) {
            os.writeObject(game); // Shares current state of game when client connects

            try {
                while (!terminated) {
                    // Read method
                    Method incoming = ((SerializableMethod) ois.readObject()).getMethod();
                    Object[] inArgs = (Object[]) ois.readObject();

                    // Execute method
                    try {
                        switch (inArgs.length) {
                            case 0: incoming.invoke(game); break;
                            case 1: incoming.invoke(game, inArgs[0]); break;
                            case 2: incoming.invoke(game, inArgs[0], inArgs[1]); break;
                            case 3: incoming.invoke(game, inArgs[0], inArgs[1], inArgs[2]); break;
                            default: System.out.println("<Method argument number is incorrect/>");
                        }
                    } catch (IllegalArgumentException e) {
                        System.err.println("<Invalid request: " + e.getMessage() + "/>");
                        continue;
                    }

                    os.writeObject(new SerializableMethod(incoming));
                    os.writeObject(inArgs);
                }
            } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
                System.err.println("<Exception encountered during client execution/>");
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println("<Exception encountered initializing resources/>");
            e.printStackTrace();
        }
    }

    // GETTERS & SETTERS

    public void terminate() {
        terminated = true;
    }

    public static Game getGame() {
        return game;
    }

    public static void setGame(Game game) {
        MultiServerThread.game = game;
    }

    public static int getThreadCount() {
        return threadCount;
    }
}