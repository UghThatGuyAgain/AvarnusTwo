package net.draycia;

import net.draycia.commands.CommandBarColor;
import net.draycia.commands.CommandBarStyle;
import net.draycia.spells.Spell;
import net.draycia.spells.SpellBook;
import net.draycia.weapons.Weapon;
import org.apache.commons.io.input.ClassLoaderObjectInputStream;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {

    public static Main instance;

    public HashMap<String, Weapon> weapons = new HashMap<>();
    private HashMap<String, Spell> spells = new HashMap<>();

    public HashMap<UUID, AvarnusPlayer> players = new HashMap<>();

    private URLClassLoader ucl;

    static Scoreboard scoreboard;

    @Override
    public void onEnable() {
        Main.instance = this;
        scoreboard = Bukkit.getServer().getScoreboardManager().getMainScoreboard();
        try {
            this.getClassLoader().loadClass("net.draycia.spells.SpellBook");
            this.getClassLoader().loadClass("net.draycia.spells.Spell");
            this.getClassLoader().loadClass("net.draycia.weapons.Weapon");

        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }

        this.loadClasses("\\Spells\\", Spell.class);
        this.loadClasses("\\Weapons\\", Weapon.class);

        Main.scoreboard.registerNewTeam("StunStatus");
        Main.scoreboard.getTeam("StunStatus").setSuffix("STUNNED! ");
        Main.scoreboard.getTeam("StunStatus").setPrefix(ChatColor.RED.toString());

        getServer().getPluginManager().registerEvents(this, this);

        this.getCommand("barcolor").setExecutor(new CommandBarColor(this));
        this.getCommand("barstyle").setExecutor(new CommandBarStyle(this));

        this.startHealthCounter();
        this.handleStatusEffects();
        this.startSaveTimer();

    }

    @Override
    public void onDisable() {
        for (AvarnusPlayer avarnusPlayer : players.values()) {
            this.savePlayer(avarnusPlayer);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        this.createPlayer(player);

        BossBar hBar = this.createBossBar("Health", BarColor.RED,    BarStyle.SEGMENTED_20, player);
        BossBar mBar = this.createBossBar("Mana",   BarColor.BLUE,   BarStyle.SOLID,        player);
        BossBar eBar = this.createBossBar("Energy", BarColor.GREEN,  BarStyle.SEGMENTED_20, player);
        BossBar rBar = this.createBossBar("Rage",   BarColor.YELLOW, BarStyle.SEGMENTED_20, player);
        BossBar vBar = this.createBossBar("Void",   BarColor.PURPLE, BarStyle.SEGMENTED_6,  player);

        AvarnusPlayer avarnusPlayer = players.get(player.getUniqueId());

        HashMap<String, BossBar> bossBars = avarnusPlayer.bossBars;

        bossBars.put("health", hBar);
        bossBars.put("mana", mBar);
        bossBars.put("energy", eBar);
        bossBars.put("rage", rBar);
        bossBars.put("void", vBar);

        this.updateBarColors(avarnusPlayer);
        this.updateBarStyles(avarnusPlayer);

        player.setGameMode(GameMode.SURVIVAL);

        player.getInventory().clear();
        player.getInventory().addItem(weapons.get("DrayciaAvarnus").getProcessedItem());
        player.getInventory().addItem(weapons.get("DrayciaStrikers").getProcessedItem());
        player.getInventory().addItem(spells.get("DrayciaFireball").getSpellBook());

        avarnusPlayer.stun(5);
        avarnusPlayer.poison(5, 1);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        AvarnusPlayer avPlayer = players.get(e.getPlayer().getUniqueId());
        if (avPlayer.isStunned) e.setCancelled(true);
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        AvarnusPlayer avarnusPlayer = players.get(player.getUniqueId());

        if (player.isSneaking()) {

            ItemStack item = player.getInventory().getItemInMainHand();
            String weaponTag = Weapon.getWeaponTag(item);

            int oldSlot = e.getPreviousSlot();
            int newSlot = e.getNewSlot();

            if (oldSlot - newSlot == 1 || (oldSlot == 0 && newSlot == 8)) {
                e.setCancelled(true);
                // If hand is empty, change selected weapon
                if (item == null || item.getType() == Material.AIR) {
                    avarnusPlayer.weapons.previousWeapon();
                    // Display new weapon here
                } else {
                    // Hand isn't empty, do spell changing
                }
            }

            if (newSlot - oldSlot == 1 || (oldSlot == 8 && newSlot == 0)) {
                e.setCancelled(true);
                // If hand is empty, change selected weapon
                if (item == null || item.getType() == Material.AIR) {
                    avarnusPlayer.weapons.nextWeapon();
                    // Display new weapon here
                } else {
                    // Hand isn't empty, do spell changing
                }
            }
        }
    }

    @EventHandler
    public void itemDropEvent(PlayerDropItemEvent event) {
        Item it = event.getItemDrop();
        ItemStack item = it.getItemStack();
        ItemMeta im = item.getItemMeta();

        if (Weapon.getWeaponTag(item) != null) {
            it.setInvulnerable(true);

            if (im.hasDisplayName()) {
                it.setCustomName(im.getDisplayName());
                it.setCustomNameVisible(true);
            }
        }
    }

    @EventHandler
    public void itemDespawnEvent(ItemDespawnEvent e) {
        if (Weapon.getWeaponTag(e.getEntity().getItemStack()) != null)
            e.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!e.getHand().equals(EquipmentSlot.HAND))
            return;

        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        AvarnusPlayer avPlayer = players.get(player.getUniqueId());

        // Handle obtaining weapons from storage
        if (e.getAction().equals(Action.LEFT_CLICK_AIR)) {
            if (player.isSneaking()) {
                if (item == null || item.getType() == Material.AIR) {
                    avPlayer.getWeapon(player);
                }
            }
        }

        if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            String weaponTag = Weapon.getWeaponTag(item);

            if (weaponTag != null && !weaponTag.isEmpty()) {
                // Handle storing weapons in storage
                if (player.isSneaking()) {
                    avPlayer.storeWeapon(player);
                }

                // Handle spell casting
                HashMap<String, SpellBook> knownSpells = avPlayer.spellBook;
                if (knownSpells.containsKey(weaponTag)) {
                    SpellBook spellBook = knownSpells.get(weaponTag);
                    spellBook.getCurrentSpell().cast(player, 5 * 20, 5, 40);

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

            // Handle spell book reading
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

    private void handleSpellDisplay() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Player player : this.getServer().getOnlinePlayers()) {

            }
        }, 0, 20L);
    }

    private BossBar createBossBar(String title, BarColor color, BarStyle style, Player player) {
        BossBar bar = this.getServer().createBossBar(title, color, style, BarFlag.PLAY_BOSS_MUSIC);
        bar.removeFlag(BarFlag.PLAY_BOSS_MUSIC);
        bar.addPlayer(player);
        bar.setVisible(false);
        return bar;
    }

    public void updateBarColors(AvarnusPlayer avarnusPlayer) {
        for (Map.Entry<String, BarColor> entry : avarnusPlayer.customBarColors.entrySet()) {
            BossBar bossBar = avarnusPlayer.bossBars.get(entry.getKey());
            bossBar.setColor(entry.getValue());
        }
    }

    public void updateBarStyles(AvarnusPlayer avarnusPlayer) {
        for (Map.Entry<String, BarStyle> entry : avarnusPlayer.customBarStyles.entrySet()) {
            BossBar bossBar = avarnusPlayer.bossBars.get(entry.getKey());
            bossBar.setStyle(entry.getValue());
        }
    }

    private void startSaveTimer() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (AvarnusPlayer avarnusPlayer : players.values()) {
                this.savePlayer(avarnusPlayer);
            }
        }, 12000, 12000);
    }

//    private void savePlayer(AvarnusPlayer avarnusPlayer) {
//        File playerFile = new File(this.getDataFolder().getAbsolutePath() + "\\PlayerData\\SpellBook\\" + avarnusPlayer.uuid + ".ser");
//
//        File pfParent = playerFile.getParentFile();
//
//        if (!pfParent.exists() && !pfParent.mkdirs()) {
//            System.out.println("Unable to save SpellBook for player with UUID " + avarnusPlayer.uuid);
//        }
//
//        try {
//            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(playerFile));
//            oos.writeObject(avarnusPlayer.spellBook);
//            oos.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void createPlayer(Player player) {
        if (!players.containsKey(player.getUniqueId())) {
            File playerFile = new File(this.getDataFolder().getAbsolutePath() + "\\PlayerData\\" + player.getUniqueId() + ".ser");

            if (playerFile.exists()) try {
                ClassLoaderObjectInputStream objInputStream = new ClassLoaderObjectInputStream(ucl, new FileInputStream(playerFile));

                @SuppressWarnings("unchecked")
                AvarnusPlayer loadAvarnusPlayer = (AvarnusPlayer) objInputStream.readObject();

                objInputStream.close();

                players.put(player.getUniqueId(), loadAvarnusPlayer);
            } catch (IOException | ClassNotFoundException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void savePlayer(AvarnusPlayer avarnusPlayer) {
        File playerFile = new File(this.getDataFolder().getAbsolutePath() + "\\PlayerData\\" + avarnusPlayer.uuid + ".ser");
        File parentFile = playerFile.getParentFile();

        if (!parentFile.exists() && !parentFile.mkdirs()) System.out.println("Unable to save SpellBook for player with UUID " + avarnusPlayer.uuid);

        try {
            ObjectOutputStream objectOutStream = new ObjectOutputStream(new FileOutputStream(playerFile));
            objectOutStream.writeObject(avarnusPlayer);
            objectOutStream.close();
        } catch (IOException e) {
            e.printStackTrace();
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

    private void handleStatusEffects() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Player player : this.getServer().getOnlinePlayers()) {
                AvarnusPlayer avarnusPlayer = players.get(player.getUniqueId());

                if (!avarnusPlayer.isStunned)
                    return;

                if (avarnusPlayer.stunDuration < .25) {
                    avarnusPlayer.isStunned = false;
                    player.setGlowing(false);
                    //noinspection deprecation
                    Main.scoreboard.getTeam("StunStatus").removePlayer(player);
                } else if (avarnusPlayer.stunDuration >= .25) {
                    avarnusPlayer.stunDuration -= .25;
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
