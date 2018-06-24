package net.draycia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;
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

import net.draycia.spells.Spell;
import net.draycia.spells.SpellBook;

public class Main extends JavaPlugin implements Listener {

    HashMap<String, Weapon> weapons = new HashMap<String, Weapon>();
    HashMap<String, Spell> spells = new HashMap<String, Spell>();

    HashMap<UUID, AvarnusPlayer> players = new HashMap<UUID, AvarnusPlayer>();

    URLClassLoader ucl;

    @Override
    public void onEnable() {
        try {
            this.getClassLoader().loadClass("net.draycia.spells.SpellBook");
            this.getClassLoader().loadClass("net.draycia.spells.Spell");
            this.getClassLoader().loadClass("net.draycia.Weapon");

        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }

        this.loadClasses("\\Spells\\", Spell.class);
        this.loadClasses("\\Weapons\\", Weapon.class);
        System.out.println(this.weapons.toString());

        getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onDisable() {
        for (Map.Entry<UUID, AvarnusPlayer> entry : players.entrySet()) {
            File playerFile = new File(this.getDataFolder().getAbsolutePath() + "\\PlayerData\\SpellBook\\" + entry.getKey() + ".ser");

            File pfParent = playerFile.getParentFile();

            System.out.println(pfParent);

            if (!pfParent.exists() && !pfParent.mkdirs()) {
                System.out.println("Unable to save SpellBook for player with UUID " + entry.getKey());
            }

            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(playerFile));
                oos.writeObject(entry.getValue().spellBook);
                oos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!players.containsKey(e.getPlayer().getUniqueId())) {
            HashMap<String, SpellBook> spellBook = new HashMap<String, SpellBook>();

            File playerFile = new File(this.getDataFolder().getAbsolutePath() + "\\PlayerData\\SpellBook\\" + e.getPlayer().getUniqueId() + ".ser");

            if (playerFile.exists()) {
                try {
                    ClassLoaderObjectInputStream clois = new ClassLoaderObjectInputStream(ucl, new FileInputStream(playerFile));

                    @SuppressWarnings("unchecked")
                    HashMap<String, SpellBook> loadSpellBook = (HashMap<String, SpellBook>) clois.readObject();

                    clois.close();
                    spellBook = loadSpellBook;

                } catch (IOException | ClassNotFoundException e1) {
                    e1.printStackTrace();
                }
            }

            players.put(e.getPlayer().getUniqueId(), new AvarnusPlayer(e.getPlayer().getUniqueId(), spellBook));
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

            if (weaponTag != null && !weaponTag.isEmpty()) {
                HashMap<String, SpellBook> knownSpells = avPlayer.spellBook;
                if (knownSpells.containsKey(weaponTag)) {
                    SpellBook spellBook = knownSpells.get(weaponTag);
                    spellBook.getCurrentSpell().cast(player);
                }
            }

            String spellTag = Spell.getSpellTag(item);

            if (spellTag != null && !spellTag.isEmpty()) {
                for (Map.Entry<String, Weapon> entry : weapons.entrySet()) {

                    ArrayList<String> allowedSpells = entry.getValue().getAllowedSpells();
                    HashMap<String, SpellBook> knownSpells = avPlayer.spellBook;

                    if (allowedSpells.contains(spellTag)) {

                        if (!knownSpells.containsKey(entry.getKey())) {
                            knownSpells.put(entry.getKey(), new SpellBook(new ArrayList<Spell>()));
                        }

                        knownSpells.get(entry.getKey()).addSpell(spells.get(spellTag));
                    }
                }
            }
        }
    }

    public void loadClasses(String dir, Class<?> type) {
        try {
            File classDir = new File(this.getDataFolder().getAbsolutePath() + dir);

            if (!classDir.exists()) {
                classDir.mkdirs();
                return;
            }

            URL url = classDir.toURI().toURL();
            URL[] urls = new URL[] { url };

            URLClassLoader ucl = new URLClassLoader(urls, this.getClassLoader());

            File[] classFiles = classDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".class");
                }
            });

            if (classFiles.length == 0) {
                ucl.close();
                return;
            }

            for (File file : classFiles) {
                Class<?> cls = ucl.loadClass(file.getName().replaceFirst("[.][^.]+$", ""));

                if (type == Spell.class) {
                    Spell spell = (Spell) cls.newInstance();
                    spells.put(((Spell) cls.newInstance()).getSpellID(), (Spell) cls.cast(spell));
                    this.ucl = ucl;

                } else if (type == Weapon.class) {
                    Weapon weapon = (Weapon) cls.newInstance();
                    weapons.put(((Weapon) cls.newInstance()).getItemID(), (Weapon) cls.cast(weapon));
                }
            }

            ucl.close();

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException e) {
            e.printStackTrace();
        }
    }
}
