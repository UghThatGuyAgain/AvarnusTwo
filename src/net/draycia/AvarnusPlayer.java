package net.draycia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.draycia.spells.SpellBook;
import org.bukkit.boss.BossBar;

public class AvarnusPlayer implements Serializable {

    private static final long serialVersionUID = -2773227562613105767L;

    public AvarnusPlayer(UUID uuid, HashMap<String, SpellBook> spellBook) {
        this.uuid = uuid;
        this.spellBook = spellBook;

        this.resources.put("mana", new ResourceBar(100, 100, 1));
        this.resources.put("energy", new ResourceBar(100, 100, 5));
        this.resources.put("rage", new ResourceBar(20, 0, -1));
        this.resources.put("void", new ResourceBar(6, 0, 0));
    }

    public UUID uuid;

    public boolean isStunned = false;
    public int stunDuration = 0;

    public boolean isPoisoned = false;
    public int poisonDuration = 0;

    public boolean isSlowed = false;
    public int slowDuration = 0;

    public HashMap<String, ResourceBar> resources = new HashMap<>();

    public HashMap<String, SpellBook> spellBook;

    public HashMap<String, BossBar> bossBars = new HashMap<>();

    public ArrayList<Weapon> storedWeapons = new ArrayList<>();
    public int selectedWeapon = 0;

    public void hideAllResourceBars() {
        for (BossBar value : this.bossBars.values()) {
            value.setVisible(false);
        }
        this.bossBars.get("health").setVisible(true);
    }

    public void handleResourceRegen() {
        for (Map.Entry<String, ResourceBar> entry : this.resources.entrySet()) {
            ResourceBar resourceBar = entry.getValue();

            double regen = resourceBar.regen / 4;

            if (regen < 0 && resourceBar.current - regen <= 0) resourceBar.current = 0;
            else if (regen > 0 && resourceBar.current + regen >= resourceBar.cap) resourceBar.current = resourceBar.cap;
            resourceBar.current += regen;

            this.bossBars.get(entry.getKey()).setProgress(resourceBar.current / resourceBar.cap);
        }
    }
}
