package net.draycia;

import net.draycia.spells.SpellBook;
import net.draycia.weapons.Weapon;
import net.draycia.weapons.WeaponStorage;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.Serializable;
import java.util.HashMap;
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
    public boolean showBarsWhenSneaking = false;
    public boolean showHealthBar = true;

    HashMap<String, ResourceBar> resources = new HashMap<>();

    HashMap<String, SpellBook> spellBook;

    public transient HashMap<String, BossBar> bossBars = new HashMap<>();

    public WeaponStorage weapons = new WeaponStorage();

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
        if (this.showHealthBar) this.bossBars.get("health").setVisible(true);
    }

    void handleResourceRegen() {
        for (var entry : this.resources.entrySet()) {
            ResourceBar resourceBar = entry.getValue();

            double regen = resourceBar.regen / 4;

            if (regen < 0 && resourceBar.current + regen <= 0) resourceBar.current = 0;
            else if (regen > 0 && resourceBar.current + regen >= resourceBar.cap) resourceBar.current = resourceBar.cap;
            else resourceBar.current += regen;

            System.out.println("Current: [" + resourceBar.current + "], Cap: [" + resourceBar.cap + "], Percentage: [" + resourceBar.current / resourceBar.cap);

            this.bossBars.get(entry.getKey()).setProgress(resourceBar.current / resourceBar.cap);
        }
    }

    void storeWeapon(Player player) {
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        String itemTag = Weapon.getWeaponTag(itemStack);
        if (itemTag == null || itemTag.isEmpty()) return;
        if (!Main.instance.weapons.containsKey(itemTag)) return;
        Weapon weapon = Main.instance.weapons.get(itemTag);
        this.weapons.storeWeapon(weapon);
        player.getInventory().setItemInMainHand(null);
    }

    void getWeapon(Player player) {
        ItemStack itemStack = this.weapons.getWeapon().getProcessedItem();
        player.getInventory().setItemInMainHand(itemStack);
    }

    public void nextWeapon() {

    }

    public void previousWeapon() {

    }

    public void updateWeapon() {

    }
}
