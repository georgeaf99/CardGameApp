package com.gfarcasiu.client;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.gfarcasiu.game.*;
import com.gfarcasiu.utilities.*;

public class MultiServerThread extends Thread {
    private volatile static Game game = new Game(); // Game object to make changes to
    private volatile static int threadCount = 0;

    private BluetoothSocket socket = null;
    private volatile boolean terminated = false;

    public MultiServerThread(BluetoothSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        if (threadCount >= 4)
            return;

        threadCount++;

        try (
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        ) {
            os.writeObject("11;lfasdfas blah blah it worked yahoo!");
            Log.i("Debug", "<Server thread running/>");
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