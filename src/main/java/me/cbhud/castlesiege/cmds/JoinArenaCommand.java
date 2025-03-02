package me.cbhud.castlesiege.cmds;

import me.cbhud.castlesiege.CastleSiege;
import me.cbhud.castlesiege.arena.Arena;
import me.cbhud.castlesiege.arena.ArenaState;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinArenaCommand implements CommandExecutor {
    private final CastleSiege plugin;

    public JoinArenaCommand(CastleSiege plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            plugin.getArenaSelector().open(player);
            return true;
        }

        String arenaId = args[0];
        Arena arena = plugin.getArenaManager().getArena(arenaId);

        if (arena == null) {
            player.sendMessage(ChatColor.RED + "Arena not found!");
            return true;
        }

        if (arena.getState() != ArenaState.WAITING) {
            player.sendMessage(ChatColor.RED + "This arena is not available right now!");
            return true;
        }

        if (arena.getPlayers().size() >= arena.getMax()) {
            player.sendMessage(ChatColor.RED + "This arena is full!");
            return true;
        }

        if (arena.addPlayer(player)) {
            player.sendMessage(ChatColor.GREEN + "You have joined arena " + arenaId + "!");
            player.teleport(arena.getLobbySpawn()); // Teleport to the lobby spawn
        } else {
            player.sendMessage(ChatColor.RED + "Could not join the arena!");
        }

        return true;
    }
}
