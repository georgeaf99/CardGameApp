package com.gfarcasiu.utilities;

import android.util.Log;

import com.gfarcasiu.client.Client;
import com.gfarcasiu.client.MultiServer;
import com.gfarcasiu.game.Game;
import com.gfarcasiu.game.PlayingCard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HelperFunctions {
    public static boolean executeMethod(Game game, Method incoming, Object ... inArgs) {
        try {
            switch (inArgs.length) {
                case 0: incoming.invoke(game); break;
                case 1: incoming.invoke(game, inArgs[0]); break;
                case 2: incoming.invoke(game, inArgs[0], inArgs[1]); break;
                case 3: incoming.invoke(game, inArgs[0], inArgs[1], inArgs[2]); break;
                default: Log.e("Error", "<Method argument number is incorrect./>");
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            Log.e("Error", "<Error executing method/>");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static Game getGame(boolean isServer) {
        if (!isServer)
            return Client.getInstance().getGame();
        else
            return MultiServer.getGame();
    }

    public static String getImageNameFromPlayingCard(PlayingCard card) {
        return "c_" + (4 * (14 - card.getValue()) + card.getSuit() + 1);
    }

    public static PlayingCard getPlayingCardFromImageName(String name) {
        int num = Integer.parseInt(name.substring(2));
        return new PlayingCard(
                14 - (num - 1) / 4,
                (num - 1) % 4,
                true);
    }
}
