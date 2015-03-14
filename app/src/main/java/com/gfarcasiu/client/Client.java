package com.gfarcasiu.client;

import com.gfarcasiu.game.*;
import com.gfarcasiu.utilities.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

public class Client implements Runnable {
    private Game game;
    private volatile boolean terminated = false;

    private String ipAddress;

    private Socket socket;
    private ObjectOutputStream os;
    private ObjectInputStream ois;

    // INITIALIZATION BLOCK
    {
        try {
            socket = new Socket(ipAddress, MultiServer.PORT);
            os = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // NOTE : Cannot be called before the thread starts or run is called
    public void executeAction(Method outgoing, Object ... args) {
        try {
            // Write serialized method
            os.writeObject(new SerializableMethod(outgoing));
            os.writeObject(args);
        } catch (IOException e) {
            System.err.println("<IOException encountered when initializing resources/>");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        ipAddress = findServerIpAddress();

        try {
            try {
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
                System.err.println("<Exception encountered during client execution/>");
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                os.close();
                ois.close();
            } catch (IOException e) {
                // DO NOTHING
            }
        }
    }

    public void terminate() {
        terminated = true;
    }

    public String findServerIpAddress() {
        return "192.168.1.6";
    }

    public Game getGame() {
        return game;
    }
}
