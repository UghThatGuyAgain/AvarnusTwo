package net.draycia.commands;

import net.draycia.AvarnusPlayer;
import net.draycia.Main;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandBarStyle implements CommandExecutor {

    Main main;

    public CommandBarStyle(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length < 2) return false;
            Player player = (Player)sender;
            AvarnusPlayer avarnusPlayer = main.players.get(player.getUniqueId());
            if (!avarnusPlayer.bossBars.containsKey(args[0].toLowerCase())) {
                sender.sendMessage(ChatColor.RED + "The specified bar does not exist. Please make sure you typed it correctly.");
            }
            try {
                BarStyle barStyle = BarStyle.valueOf(args[1].toUpperCase());
                avarnusPlayer.customBarStyles.put(args[0], barStyle);
                main.updateBarStyles(avarnusPlayer);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "The specified style does not exist. Please make sure you typed it correctly.");
            }
        }
        return true;
    }
}
