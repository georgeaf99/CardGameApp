package com.gfarcasiu.client;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.gfarcasiu.game.*;
import com.gfarcasiu.utilities.*;

public class MultiServer extends Thread {
    private volatile static Game game = new Game(); // Game object to make changes to

    // Static init block
    static {
        List<PlayingCard> cards = new ArrayList<>();
        for (int val = 2; val <= 14; val++)
            for (int suit = 0; suit <= 4; suit++)
                cards.add(new PlayingCard(val, suit, true));

        game.initDeck(cards);
    }

    private volatile static int threadCount = 0;

    // TODO poor software engineering design
    private volatile static HashSet<ObjectOutputStream> outputStreams = new HashSet<>();
    private volatile static HashSet<ObjectInputStream> inputStreams = new HashSet<>();

    private BluetoothSocket socket = null;
    private volatile boolean terminated = false;

    // Singleton design pattern
    public MultiServer(BluetoothSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        Log.i("Debug", "<MultiServer thread started/>");
        if (threadCount >= 4)
            return;

        threadCount++;

        try (
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        ) {
            outputStreams.add(oos);
            inputStreams.add(ois);

            oos.writeObject(game); // Shares current state of game when client connects
            Log.i("Debug", "<Multiserver game state successfully shared/>");

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

                    for (ObjectOutputStream stream : outputStreams) {
                        stream.writeObject(new SerializableMethod(incoming));
                        stream.writeObject(inArgs);
                    }
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

    // TODO poor adaption to bluetooth, comes from lack of loop back on bluetooth
    public static void executeAction(Method incoming, Object ... inArgs) {
        // Execute method
        try {
            switch (inArgs.length) {
                case 0: incoming.invoke(game); break;
                case 1: incoming.invoke(game, inArgs[0]); break;
                case 2: incoming.invoke(game, inArgs[0], inArgs[1]); break;
                case 3: incoming.invoke(game, inArgs[0], inArgs[1], inArgs[2]); break;
                default: System.out.println("<Method argument number is incorrect/>");
            }
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            Log.e("Error", "<Invalid request: " + e.getMessage() + "/>");
            e.printStackTrace();
            return;
        }

        try {
            for (ObjectOutputStream stream : outputStreams) {
                stream.writeObject(new SerializableMethod(incoming));
                stream.writeObject(inArgs);
            }
        } catch (IOException e) {
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
        MultiServer.game = game;
    }

    public static int getThreadCount() {
        return threadCount;
    }
}