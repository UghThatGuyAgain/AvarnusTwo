package net.draycia.commands;

import net.draycia.AvarnusPlayer;
import net.draycia.Main;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandBarColor implements CommandExecutor {

    Main main;

    public CommandBarColor(Main main) {
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
                BarColor barColor = BarColor.valueOf(args[1].toUpperCase());
                avarnusPlayer.customBarColors.put(args[0], barColor);
                main.updateBarColors(avarnusPlayer);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "The specified color does not exist. Please make sure you typed it correctly.");
            }
        }
        return true;
    }
}
