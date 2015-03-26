package com.gfarcasiu.game;

import java.util.Arrays;

public class Player extends Entity {
    private String unique;

    public Player(String unique, int maxCards) {
        super(maxCards);

        this.unique = unique;
    }

    public String getUnique() {
        return unique;
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }

    // OVERRIDEN METHODS
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Player))
            return false;

        boolean uniquesEqual;
        if (unique == null)
            uniquesEqual = ((Player) obj).getUnique() == null;
        else
            uniquesEqual = unique.equals(((Player) obj).getUnique());

        return
            uniquesEqual &&
            super.getMaxCards() == ((Entity) obj).getMaxCards() &&
            Arrays.equals(this.getCards(), ((Entity) obj).getCards());
    }

    @Override
    public String toString() {
        return super.toString() + " " + unique;
    }
}
