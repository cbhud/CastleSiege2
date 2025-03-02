package me.cbhud.castlesiege.events;

import me.cbhud.castlesiege.CastleSiege;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinEvent implements Listener {

    private final CastleSiege plugin;

    public JoinEvent(CastleSiege plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        e.setJoinMessage(null);
        Player p = e.getPlayer();

        if(plugin.getSlc().getLobby() == null){
            p.sendMessage("Lobby location is not set!");
            p.sendMessage("Use /setlobby to set lobby location");
        }else{
        p.teleport(plugin.getSlc().getLobby());}
        p.setGameMode(GameMode.ADVENTURE);
        for (String i:  plugin.getMsg().getMessage("join-server-msg", p)){
            p.sendMessage(i);
        }

        //give him lobby items
        plugin.getScoreboardManager().setupScoreboard(p);
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        plugin.getScoreboardManager().removeScoreboard(p);
        //unregister scoreboards
        //
    }

}