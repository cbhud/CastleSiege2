package me.cbhud.castlesiege.player;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import me.cbhud.castlesiege.CastleSiege;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import static me.cbhud.castlesiege.player.PlayerState.*;

public class PlayerManager {

    private final CastleSiege plugin;
    private final Map<Player, PlayerState> playerStates;

    public PlayerManager(CastleSiege plugin) {
        this.plugin = plugin;
        this.playerStates = new HashMap<>();
    }

    public PlayerState getPlayerState(Player player) {
        return (player != null) ? playerStates.getOrDefault(player, IN_LOBBY) : IN_LOBBY;
    }

    public void setPlayerAsPlaying(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.setHealth(20);
            player.setLevel(0);
            player.getActivePotionEffects().clear();
            playerStates.put(player, PLAYING);
        });
    }

    public void setPlayerAsLobby(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().clear();
            player.setHealth(20);
            player.setLevel(0);
            player.getActivePotionEffects().clear();
            player.getInventory().setItem(4, ItemBuilder.from(Material.COMPASS)
                    .name(Component.text("§aSelect Arena"))
                    .lore(Component.text("§7Right-click to select arena")).build());
            playerStates.put(player, IN_LOBBY);

            // Teleport synchronously
            player.teleport(plugin.getSlc().getLobbySpawn());

            // Scoreboard updates can be async for better performance
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getScoreboardManager().setupScoreboard(player));
        });
    }

    public void setPlayerAsWaiting(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.setHealth(20);
            player.setLevel(0);
            player.getActivePotionEffects().clear();
            player.getInventory().setItem(3, ItemBuilder.from(Material.CLOCK)
                    .name(Component.text("§eSelect Team"))
                    .lore(Component.text("§7Right-click to select team"))
                    .build());
            player.getInventory().setItem(5, ItemBuilder.from(Material.NETHER_STAR)
                    .name(Component.text("§eSelect Kit"))
                    .lore(Component.text("§7Right-click to select the kit"))
                    .build());
            playerStates.put(player, WAITING);
            player.getInventory().setItem(8, ItemBuilder.from(Material.RED_DYE)
                    .name(Component.text("§cLeave Arena"))
                    .lore(Component.text("§7Right-click to leave arena"))
                    .build());
            playerStates.put(player, WAITING);

            // Scoreboard update async
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getScoreboardManager().updateScoreboard(player, "pre-game"));
        });
    }

    public void setPlayerAsSpectating(Player player) {
            player.setGameMode(GameMode.SPECTATOR);
            playerStates.put(player, SPECTATOR);
    }



}