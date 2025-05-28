package me.cbhud.castlesiege.event;

import me.cbhud.castlesiege.CastleSiege;

import me.cbhud.castlesiege.player.PlayerState;
import me.cbhud.castlesiege.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathEvent implements Listener {
    private final CastleSiege plugin;

    public DeathEvent(CastleSiege plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (plugin.getPlayerManager().getPlayerState(player) != PlayerState.PLAYING) {
            return;
        }

            event.getDrops().clear();

        player.sendTitle(plugin.getMsg().getMessage("respawnTitle", player).get(0), plugin.getMsg().getMessage("respawnTitle", player).get(1), 10, 70, 20);
            plugin.getPlayerManager().setPlayerAsSpectating(player);
            plugin.getDataManager().incrementDeaths(player.getUniqueId());
            if(event.getEntity().getKiller() != null) {
                plugin.getDataManager().addPlayerCoins(event.getEntity().getKiller().getUniqueId(), plugin.getConfigManager().getCoinsOnKill());
                plugin.getDataManager().incrementKills(event.getEntity().getKiller().getUniqueId(), 1);
            }
            Team team = plugin.getArenaManager().getArenaByPlayer(player.getUniqueId()).getTeam(player);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.spigot().respawn();
                player.teleport(plugin.getArenaManager().getArenaByPlayer(player.getUniqueId()).getTeamSpawn(team));
                plugin.getPlayerManager().setPlayerAsPlaying(player);
                plugin.getPlayerKitManager().giveKit(player, plugin.getPlayerKitManager().getSelectedKit(player));
            }, 5 * 20); // 5 seconds

    }


}