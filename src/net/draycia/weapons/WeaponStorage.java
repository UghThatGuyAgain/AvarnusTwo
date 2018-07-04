package net.draycia.weapons;

import java.io.Serializable;
import java.util.ArrayList;

public class  WeaponStorage implements Serializable {

    private static final long serialVersionUID = 7734893759086758407L;

    ArrayList<Weapon> weapons = new ArrayList<>();
    int index = 0;

    public Weapon getWeapon() {
        Weapon weapon = weapons.get(index);
        weapons.remove(weapon);
        index = (index - 1) >= 0 ? (index - 1) : 0;
        return weapon;
    }

    public void storeWeapon(Weapon weapon) {
        weapons.add(weapon);
    }

    public void nextWeapon() {
        index = (index - 1) >= 0 ? (index - 1) : weapons.size() - 1;
    }

    public void previousWeapon() {
        index = (index + 1) <= weapons.size() - 1 ? (index + 1) : 0;
    }
}
