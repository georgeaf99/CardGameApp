package com.gfarcasiu.client;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    private volatile static HashSet<ObjectOutputStream> outputStreams = new HashSet<>();

    private volatile static int threadCount = 0;
    private volatile boolean terminated = false;

    private volatile BluetoothSocket socket = null;

    public MultiServer(BluetoothSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        Log.i("Debug", "<MultiServer thread started/>");
        if (threadCount++ >= 4)
            return;

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
    }

    // NOTE : should only be used by the device hosting client
    public static void executeAction(Method incoming, Object ... inArgs) {
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