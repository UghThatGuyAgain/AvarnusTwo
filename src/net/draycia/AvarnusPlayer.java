package net.draycia;

import net.draycia.spells.SpellBook;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AvarnusPlayer implements Serializable {

    private static final long serialVersionUID = -2773227562613105767L;

    AvarnusPlayer(UUID uuid, HashMap<String, SpellBook> spellBook) {
        this.uuid = uuid;
        this.spellBook = spellBook;

        this.resources.put("mana", new ResourceBar(100, 100, 1));
        this.resources.put("energy", new ResourceBar(100, 100, 5));
        this.resources.put("rage", new ResourceBar(20, 20, -1));
        this.resources.put("void", new ResourceBar(6, 3, 0));
    }

    public UUID uuid;

    boolean isStunned = false;
    double stunDuration = 0;

    public HashMap<String, BarColor> customBarColors = new HashMap<>();
    public HashMap<String, BarStyle> customBarStyles = new HashMap<>();

    HashMap<String, ResourceBar> resources = new HashMap<>();

    HashMap<String, SpellBook> spellBook;

    public transient HashMap<String, BossBar> bossBars = new HashMap<>();

    public ArrayList<Weapon> storedWeapons = new ArrayList<>();
    public int selectedWeapon = 0;

    void stun(double duration) {
        if (stunDuration < duration) {
            isStunned = true;
            stunDuration = duration;
            Player player = Bukkit.getPlayer(this.uuid);
            player.setGlowing(true);
            Main.scoreboard.getTeam("StunStatus").addPlayer(player);
        }
    }

    void poison(int duration, int amplifier) {
        Player player = Bukkit.getPlayer(this.uuid);
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, amplifier));
    }

    void hideAllResourceBars() {
        for (BossBar value : this.bossBars.values()) {
            value.setVisible(false);
        }
        this.bossBars.get("health").setVisible(true);
    }

    public void handleResourceRegen() {
        for (Map.Entry<String, ResourceBar> entry : this.resources.entrySet()) {
            ResourceBar resourceBar = entry.getValue();

            double regen = resourceBar.regen / 4;

            if (regen < 0 && resourceBar.current + regen <= 0) resourceBar.current = 0;
            else if (regen > 0 && resourceBar.current + regen >= resourceBar.cap) resourceBar.current = resourceBar.cap;
            else resourceBar.current += regen;

            System.out.println("Current: [" + resourceBar.current + "], Cap: [" + resourceBar.cap + "], Percentage: [" + resourceBar.current / resourceBar.cap);

            this.bossBars.get(entry.getKey()).setProgress(resourceBar.current / resourceBar.cap);
        }
    }
}
