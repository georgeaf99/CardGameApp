package com.gfarcasiu.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Entity implements Serializable {
    private ArrayList<PlayingCard> cards = new ArrayList<>();
    private int maxCards;

    // CONSTRUCTOR

    public Entity(int maxCards) {
        checkMaxCards(maxCards);

        this.maxCards = maxCards;
    }

    public Entity() {
        this(Integer.MAX_VALUE);
    }

    public void addCard(PlayingCard c) {
        if (cards.size() == maxCards)
            throw new IllegalArgumentException(
                    "Cards greater than maximum number of cards.");

        cards.add(c);
    }

    public boolean removeCard(PlayingCard c) {
        if (!cards.contains(c))
            throw new IllegalArgumentException(
                    "Entity does not contain card to be removed");

        return cards.remove(c);
    }

    public PlayingCard[] getCards() {
        PlayingCard[] toReturn = new PlayingCard[cards.size()];

        int curLoc = 0;
        for (PlayingCard c : cards)
            toReturn[curLoc++] = c;

        return toReturn;
    }

    public int getMaxCards() {
        return maxCards;
    }

    // OVERRIDDEN METHOD

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Entity))
            return false;

        return obj == this ||
            this.maxCards == ((Entity) obj).getMaxCards() &&
            Arrays.equals(this.getCards(), ((Entity) obj).getCards());
    }

    // HELPER METHODS

    private static void checkMaxCards(int maxCards) {
        if (maxCards < 0)
            throw new IllegalArgumentException();
    }

    // TESTING ONLY
    @Override
    public String toString() {
        return maxCards + " " + cards.toString();
    }

    // OVERRIDDEN METHODS

    /*private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(maxCards);

        out.writeInt(cards.size()); // Writes the size of the array list

        for (PlayingCard c : cards)
            out.writeObject(c);

        //out.close();
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        maxCards = in.readInt();

        int numCards = in.readInt();

        cards = new ArrayList<>();

        for (int i = 0; i < numCards; i++)
            cards.add((PlayingCard)in.readObject());

        //in.close();
    }*/

}
