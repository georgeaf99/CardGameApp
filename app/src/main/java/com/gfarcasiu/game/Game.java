package com.gfarcasiu.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Game implements Serializable {
    private volatile HashMap<String, Player> players = new HashMap<>();

    private Entity deck = new Entity();
    private Entity vis = new Entity();
    private Entity trash = new Entity();

    private static final Random randGen = new Random();

    // INIT METHODS

    public void addPlayer(Player p) {
        players.put(p.getUnique(), p);
    }

    public void removePlayer(Entity p) {
        players.remove(p);
    }

    public void initDeck(List<PlayingCard> cards) {
        for (PlayingCard c : cards)
            deck.addCard(c);
    }

    // CARD MOVEMENT

    /*public void playerToPlayer(PlayingCard c, Entity p1, Entity p2) {
        p1.removeCard(c);
        p2.addCard(c);
    }*/

    public void playerToVisible(PlayingCard c, String unique) {
        players.get(unique).removeCard(c);
        vis.addCard(c);
    }

    public void playerToDeck(PlayingCard c, String unique) {
        players.get(unique).removeCard(c);
        deck.addCard(c);
    }

    public void playerToTrash(PlayingCard c, String unique) {
        players.get(unique).removeCard(c);
        trash.addCard(c);
    }

    public void visibleToPlayer(PlayingCard c, String unique) {
        vis.removeCard(c);
        players.get(unique).addCard(c);
    }

    public void visibleToDeck(PlayingCard c) {
        vis.removeCard(c);
        deck.addCard(c);
    }

    public void visibleToTrash(PlayingCard c) {
        vis.removeCard(c);
        trash.addCard(c);
    }

    public void deckToPlayer(PlayingCard c, String unique) {
        deck.removeCard(c);
        players.get(unique).addCard(c);
    }

    public void deckToVis(PlayingCard c) {
        deck.removeCard(c);
        vis.addCard(c);
    }

    public void deckToTrash(PlayingCard c) {
        deck.removeCard(c);
        trash.addCard(c);
    }

    public void trashToPlayer(PlayingCard c, Entity p) {
        trash.removeCard(c);
        p.addCard(c);
    }

    public void trashToDeck(PlayingCard c) {
        trash.removeCard(c);
        deck.addCard(c);
    }

    public void trashToVis(PlayingCard c) {
        trash.removeCard(c);
        vis.addCard(c);
    }

    // GETTERS

    public Entity[] getPlayers() {
        Entity[] toReturn = new Entity[players.size()];

        int curLoc = 0;
        for (String unique : players.keySet())
            toReturn[curLoc++] = players.get(unique);

        return toReturn;
    }

    public Entity getPlayer(String uniqueId) {
        return players.get(uniqueId);
    }

    public Entity getDeck() {
        return deck;
    }

    public Entity getTrash() {
        return trash;
    }

    public Entity getVis() {
        return vis;
    }

    // HELPER

    public static PlayingCard getRandomCard(Entity e) {
        PlayingCard[] cards = e.getCards();

        if (cards.length == 0)
            throw new IllegalArgumentException(
                    "Entity does not have any cards.");

        return cards[randGen.nextInt(cards.length)];
    }

    // OVERRIDDEN METHODS

    @Override
    public String toString() {
        String toReturn = "Players:\n";

        for (String unique : players.keySet())
            toReturn += "\t" + players.get(unique) + "\n";

        toReturn += "Deck: " + deck.toString() + "\n";
        toReturn += "Vis: " + vis.toString() + "\n";
        toReturn += "Trash: " + trash.toString() + "\n";

        return toReturn;
    }
}