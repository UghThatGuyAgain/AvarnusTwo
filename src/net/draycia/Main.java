package net.draycia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import net.draycia.spells.Spell;
import net.draycia.spells.SpellBook;

public class Main extends JavaPlugin implements Listener {

    private HashMap<String, Weapon> weapons = new HashMap<>();
    private HashMap<String, Spell> spells = new HashMap<>();

    private HashMap<UUID, AvarnusPlayer> players = new HashMap<>();

    private URLClassLoader ucl;

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

        this.startHealthCounter();

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
        Player player = e.getPlayer();
        if (!players.containsKey(player.getUniqueId())) {
            HashMap<String, SpellBook> spellBook = new HashMap<>();

            File playerFile = new File(this.getDataFolder().getAbsolutePath() + "\\PlayerData\\SpellBook\\" + player.getUniqueId() + ".ser");

            if (playerFile.exists()) try {
                ClassLoaderObjectInputStream objInputStream = new ClassLoaderObjectInputStream(ucl, new FileInputStream(playerFile));

                @SuppressWarnings("unchecked")
                HashMap<String, SpellBook> loadSpellBook = (HashMap<String, SpellBook>) objInputStream.readObject();

                objInputStream.close();
                spellBook = loadSpellBook;

            } catch (IOException | ClassNotFoundException e1) {
                e1.printStackTrace();
            }

            players.put(player.getUniqueId(), new AvarnusPlayer(player.getUniqueId(), spellBook));
        }

        BossBar hBar = this.getServer().createBossBar(null, BarColor.RED, BarStyle.SEGMENTED_20, BarFlag.PLAY_BOSS_MUSIC);
        hBar.removeFlag(BarFlag.PLAY_BOSS_MUSIC);
        hBar.setProgress(player.getHealth() / 20);
        hBar.addPlayer(player);

        BossBar mBar = this.getServer().createBossBar(null, BarColor.BLUE, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);
        mBar.removeFlag(BarFlag.PLAY_BOSS_MUSIC);
        mBar.addPlayer(player);
        mBar.setVisible(false);

        BossBar eBar = this.getServer().createBossBar(null, BarColor.GREEN, BarStyle.SEGMENTED_20, BarFlag.PLAY_BOSS_MUSIC);
        eBar.removeFlag(BarFlag.PLAY_BOSS_MUSIC);
        eBar.addPlayer(player);
        eBar.setVisible(false);

        BossBar rBar = this.getServer().createBossBar(null, BarColor.YELLOW, BarStyle.SEGMENTED_20, BarFlag.PLAY_BOSS_MUSIC);
        rBar.removeFlag(BarFlag.PLAY_BOSS_MUSIC);
        rBar.addPlayer(player);
        rBar.setVisible(false);

        BossBar vBar = this.getServer().createBossBar(null, BarColor.PURPLE, BarStyle.SEGMENTED_6, BarFlag.PLAY_BOSS_MUSIC);
        vBar.removeFlag(BarFlag.PLAY_BOSS_MUSIC);
        vBar.addPlayer(player);
        vBar.setVisible(false);

        AvarnusPlayer avarnusPlayer = players.get(player.getUniqueId());

        HashMap<String, BossBar> bossBars = avarnusPlayer.bossBars;

        bossBars.put("health", hBar);
        bossBars.put("mana", mBar);
        bossBars.put("energy", eBar);
        bossBars.put("rage", rBar);
        bossBars.put("void", vBar);

        player.setGameMode(GameMode.CREATIVE);
        player.getInventory().clear();
        player.getInventory().addItem(weapons.get("DrayciaAvarnus").getProcessedItem());
        player.getInventory().addItem(weapons.get("DrayciaStrikers").getProcessedItem());
        player.getInventory().addItem(spells.get("DrayciaFireball").getSpellBook());
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

                    if (weapons.containsKey(weaponTag)) {
                        System.out.println("Weapon contains key.");
                        Weapon weapon = weapons.get(weaponTag);
                        String barType = weapon.getBarType().toString();
                        ResourceBar resourceBar = avPlayer.resources.get(barType);
                        double newVal = resourceBar.current + 5;
                        resourceBar.current = newVal > resourceBar.cap ? resourceBar.cap : newVal;
                        System.out.println("Bar current is " + avPlayer.resources.get(barType).current);
                    }
                }
            }

            String spellTag = Spell.getSpellTag(item);

            if (spellTag != null && !spellTag.isEmpty()) {
                for (Map.Entry<String, Weapon> entry : weapons.entrySet()) {

                    ArrayList<String> allowedSpells = entry.getValue().getAllowedSpells();
                    HashMap<String, SpellBook> knownSpells = avPlayer.spellBook;

                    if (allowedSpells.contains(spellTag)) {

                        if (!knownSpells.containsKey(entry.getKey())) {
                            knownSpells.put(entry.getKey(), new SpellBook(new ArrayList<>()));
                        }

                        knownSpells.get(entry.getKey()).addSpell(spells.get(spellTag));
                    }
                }
            }
        }
    }

    private void startHealthCounter() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Player player : this.getServer().getOnlinePlayers()) {
                AvarnusPlayer avarnusPlayer = players.get(player.getUniqueId());

                BossBar hBar = avarnusPlayer.bossBars.get("health");
                hBar.setProgress(player.getHealth() / 20);

                avarnusPlayer.hideAllResourceBars();
                avarnusPlayer.handleResourceRegen();

                String weaponTag = Weapon.getWeaponTag(player.getInventory().getItemInMainHand());
                if (weaponTag != null && !weaponTag.isEmpty() && weapons.containsKey(weaponTag)) {
                    Weapon weapon = weapons.get(weaponTag);
                    String barType = weapon.getBarType().toString();
                    avarnusPlayer.bossBars.get(barType).setVisible(true);
                }
            }
        }, 0L, 5L);
    }

    private void loadClasses(String dir, Class<?> type) {
        try {
            File classDir = new File(this.getDataFolder().getAbsolutePath() + dir);

            if (!classDir.exists()) {
                boolean dirsMade = classDir.mkdirs();
                if (!dirsMade) {
                    System.out.println("Failed to create the directory [" + dir + "]");
                }
                return;
            }

            URL url = classDir.toURI().toURL();
            URL[] urls = new URL[] { url };

            URLClassLoader ucl = new URLClassLoader(urls, this.getClassLoader());

            File[] classFiles = classDir.listFiles((File directory, String filename) -> filename.endsWith(".class"));

            if (classFiles != null && classFiles.length > 0) {
                for (File file : classFiles) {
                    Class<?> cls = ucl.loadClass(file.getName().replaceFirst("[.][^.]+$", ""));

                    if (type == Spell.class) {
                        Spell spell = (Spell) cls.getDeclaredConstructor().newInstance();
                        spells.put(((Spell) cls.getDeclaredConstructor().newInstance()).getSpellID(), (Spell) cls.cast(spell));
                        this.ucl = ucl;

                    } else if (type == Weapon.class) {
                        Weapon weapon = (Weapon) cls.getDeclaredConstructor().newInstance();
                        weapons.put(((Weapon) cls.getDeclaredConstructor().newInstance()).getItemID(), (Weapon) cls.cast(weapon));
                    }
                }
            }

            ucl.close();

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
