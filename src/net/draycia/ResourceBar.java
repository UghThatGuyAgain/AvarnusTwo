package net.draycia;

import java.io.Serializable;

public class ResourceBar implements Serializable {

    private static final long serialVersionUID = 3065998223739472606L;

    public int cap;
    public double current;
    public double regen;

    public ResourceBar(int cap, double current, double regen) {
        this.cap = cap;
        this.current = current;
        this.regen = regen;
    }
}
