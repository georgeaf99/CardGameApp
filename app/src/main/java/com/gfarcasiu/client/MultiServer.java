package com.gfarcasiu.client;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.HashSet;

import com.gfarcasiu.game.*;
import com.gfarcasiu.utilities.*;

public class MultiServer extends Thread {
    // Indicates if the server has ever been started
    private volatile static boolean serverStarted = false;

    private volatile static Game game; // Game object to make changes to

    private volatile static HashSet<ObjectOutputStream> outputStreams = new HashSet<>();

    private volatile static int threadCount = 0;
    private volatile static boolean terminated = false;
    private volatile static boolean[] terminatedCallback = new boolean[4];

    private volatile BluetoothSocket socket = null;

    public MultiServer(BluetoothSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        Log.i("Debug", "<MultiServer thread started/>");

        serverStarted = true;

        int threadId = threadCount;
        if (threadCount++ >= 4)
            return;

        // Init game
        if (game == null) {
            game = new Game();
            Game.defaultInitGame(game);
        }

        try (
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        ) {
            outputStreams.add(oos);

            oos.writeObject(game); // Shares current state of game when client connects
            Log.i("Debug", "<Multiserver game state successfully shared/>");

            try {
                while (!terminated) {
                    // Read method
                    Method incoming = ((SerializableMethod) ois.readObject()).getMethod();
                    Object[] inArgs = (Object[]) ois.readObject();

                    executeAction(incoming, inArgs);
                }
            } catch (ClassNotFoundException e) {
                System.err.println("<Exception encountered during client execution/>");
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println("<Exception encountered initializing streams/>");
            e.printStackTrace();
        }

        // Mark thread as terminated
        terminatedCallback[threadId] = true;
    }

    // NOTE : should only be used by the device hosting client
    public static void executeAction(Method incoming, Object ... inArgs) {
        // Init game
        if (game == null) {
            game = new Game();
            Game.defaultInitGame(game);
        }

        boolean executeWorked = HelperFunctions.executeMethod(game, incoming, inArgs);

        if (!executeWorked) return;

        try {
            for (ObjectOutputStream stream : outputStreams) {
                stream.writeObject(new SerializableMethod(incoming));
                stream.writeObject(inArgs);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reset() {
        Log.i("Debug", "<Multiserver reset called/>");

        terminate();

        while (true) {
            boolean threadsDone = true;
            for (int i = 0; i < threadCount; i++)
                threadsDone &= terminatedCallback[i];

            if (threadsDone) break;

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Log.e("Error", "<Failed to stop threads/>");
            }
        }

        terminated = false;
        outputStreams = new HashSet<>();
        threadCount = 0;
        terminatedCallback = new boolean[4];
        game = null;
    }

    public static void terminate() {
        terminated = true;
    }


    // GETTERS & SETTERS

    public static Game getGame() {
        return game;
    }

    public static void setGame(Game game) {
        MultiServer.game = game;
    }

    public static int getThreadCount() {
        return threadCount;
    }

    public static boolean isServerStarted() {
        return serverStarted;
    }
}