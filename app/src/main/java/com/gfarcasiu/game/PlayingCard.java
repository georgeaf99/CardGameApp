package com.gfarcasiu.game;

public class PlayingCard {
    // CONSTANTS

    public static final int DIAMONDS = 0;
    public static final int CLUBS = 1;
    public static final int HEARTS = 2;
    public static final int SPADES = 3;

    // INSTANCE VARS

    private int value;
    private int suit;
    private boolean isVisible;

    // CONSTRUCTORS

    public PlayingCard(int value, int suit, boolean isVisible) {
        checkValue(value);
        checkSuit(suit);

        this.value = value;
        this.isVisible = isVisible;
        this.suit = suit;
    }

    // SETTERS
    public void setValue(int value) {
        checkValue(value);
        this.value = value;
    }

    public void setSuit(int suit) {
        checkSuit(suit);
        this.suit = suit;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    // GETTERS

    public int getValue() {
        return value;
    }

    public int getSuit() {
        return suit;
    }

    public boolean isVisible() {
        return isVisible;
    }

    // OVERRIDDEN METHODS

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PlayingCard))
            return false;

        return obj == this ||
               this.value == ((PlayingCard)obj).getValue() &&
               this.suit == ((PlayingCard)obj).getSuit() &&
               this.isVisible == ((PlayingCard)obj).isVisible();
    }

    @Override
    public String toString() {
        String toReturn = value + " ";

        switch (suit) {
            case DIAMONDS : toReturn += "DIAMONDS"; break;
            case CLUBS : toReturn += "CLUBS"; break;
            case HEARTS : toReturn += "HEARTS"; break;
            case SPADES : toReturn += "SPADES"; break;
        }

        toReturn += " " + isVisible;

        return  toReturn;
    }

    // HELPER
    private static void checkValue(int value) {
        if (value < 2 || value > 13)
            throw new IllegalArgumentException();
    }

    private static void checkSuit(int suit) {
        if (suit < 0 || suit > 4)
            throw new IllegalArgumentException();
    }
}
