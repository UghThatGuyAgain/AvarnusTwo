package net.draycia;

import java.io.Serializable;

public enum BarType implements Serializable {
    HEALTH("health"),
    MANA("mana"),
    ENERGY("energy"),
    RAGE("rage"),
    VOID("void");

    private final String text;

    BarType(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
