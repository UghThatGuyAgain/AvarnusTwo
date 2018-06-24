package net.draycia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import net.draycia.spells.SpellBook;

public class AvarnusPlayer {

    public AvarnusPlayer(UUID uuid, HashMap<String, SpellBook> spellBook) {
        this.uuid = uuid;
        this.spellBook = spellBook;
    }

    public UUID uuid;

    public boolean isStunned = false;
    public int stunDuration = 0;

    public boolean isPoisoned = false;
    public int poisonDuration = 0;

    public boolean isSlowed = false;
    public int slowDuration = 0;

    public ArrayList<Weapon> storedWeapons = new ArrayList<Weapon>();
    public int selectedWeapon = 0;

    public HashMap<String, SpellBook> spellBook;
}
