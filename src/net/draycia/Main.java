package net.draycia;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import spells.Spell;
import spells.SpellBook;

public class Main extends JavaPlugin implements Listener {

    HashMap<String, Weapon> weapons = new HashMap<String, Weapon>();
    HashMap<String, Spell> spells = new HashMap<String, Spell>();

    HashMap<UUID, AvarnusPlayer> players = new HashMap<UUID, AvarnusPlayer>();

    @Override
    public void onEnable() {

        this.loadClasses("\\plugins\\AvarnusNew\\Weapons\\", Weapon.class);
        this.loadClasses("\\plugins\\AvarnusNew\\Spells\\", Spell.class);
        System.out.println(this.weapons.toString());

        getServer().getPluginManager().registerEvents(this, this);

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!players.containsKey(e.getPlayer().getUniqueId())) {
            // ArrayList<Spell> spellList = new ArrayList<Spell>(spells.values());
            // SpellBook spellBook = new SpellBook(spellList);
            // players.put(e.getPlayer().getUniqueId(), new
            // AvarnusPlayer(e.getPlayer().getUniqueId(), spellBook));
        }
        e.getPlayer().setGameMode(GameMode.CREATIVE);
        e.getPlayer().getInventory().clear();
        e.getPlayer().getInventory().addItem(weapons.get("DrayciaAvarnus").getProcessedItem());
        e.getPlayer().getInventory().addItem(spells.get("DrayciaFireball").getSpellBook());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!e.getHand().equals(EquipmentSlot.HAND))
            return;

        if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            Player player = e.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            AvarnusPlayer avPlayer = players.get(player.getUniqueId());

            String weaponTag = Weapon.getWeaponTag(item);
            if (weaponTag != null && !weaponTag.isEmpty())
                avPlayer.mageSpells.get(0).cast(e.getPlayer());

            String spellTag = Spell.getSpellTag(item);
            if (spellTag != null && !spellTag.isEmpty()) {
                for (Map.Entry<String, Weapon> entry : weapons.entrySet()) {
                    ArrayList<String> allowedSpells = entry.getValue().getAllowedSpells();
                    HashMap<String, SpellBook> knownSpells = avPlayer.spellBook;
                    if (!allowedSpells.contains(spellTag))
                        knownSpells.put(entry.getKey(), new SpellBook(new ArrayList<Spell>()));
                    knownSpells.get(entry.getKey()).addSpell(spells.get(spellTag));
                }
            }
        }
    }

    public void loadClasses(String dir, Class<?> type) {
        try {
            File classDir = new File(new File("").getAbsolutePath() + dir);

            if (!classDir.exists()) {
                classDir.mkdirs();
                return;
            }

            URL url = classDir.toURI().toURL();
            URL[] urls = new URL[] { url };

            URLClassLoader cl = new URLClassLoader(urls, type.getClassLoader());

            File[] classFiles = classDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".class");
                }
            });

            if (classFiles.length == 0) {
                cl.close();
                return;
            }

            for (File file : classFiles) {
                Class<?> cls = cl.loadClass(file.getName().replaceFirst("[.][^.]+$", ""));

                if (type == Weapon.class) {
                    Weapon weapon = (Weapon) cls.newInstance();
                    weapons.put(((Weapon) cls.newInstance()).getItemID(), (Weapon) cls.cast(weapon));
                } else if (type == Spell.class) {
                    Spell spell = (Spell) cls.newInstance();
                    spells.put(((Spell) cls.newInstance()).getSpellID(), (Spell) cls.cast(spell));
                }
            }

            cl.close();

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException e) {
            e.printStackTrace();
        }
    }
}
