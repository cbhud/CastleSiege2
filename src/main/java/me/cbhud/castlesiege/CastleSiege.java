package me.cbhud.castlesiege;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import io.github.regenerato.Regenerato;
import me.cbhud.castlesiege.arena.ArenaManager;
import me.cbhud.castlesiege.cmds.CreateArenaCommand;
import me.cbhud.castlesiege.cmds.LeaveArenaCommand;
import me.cbhud.castlesiege.cmds.SetLobbyCommand;
import me.cbhud.castlesiege.events.DeathEvent;
import me.cbhud.castlesiege.events.JoinEvent;
import me.cbhud.castlesiege.events.MiscEvents;
import me.cbhud.castlesiege.events.RightClickEffects;
import me.cbhud.castlesiege.gui.ArenaSelector;
import me.cbhud.castlesiege.gui.TeamSelector;
import me.cbhud.castlesiege.player.PlayerManager;
import me.cbhud.castlesiege.scoreboard.ScoreboardManager;
import me.cbhud.castlesiege.team.TeamManager;
import me.cbhud.castlesiege.utils.ConfigManager;
import me.cbhud.castlesiege.utils.CustomPlaceholder;
import me.cbhud.castlesiege.utils.Messages;
import me.cbhud.castlesiege.utils.MobManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CastleSiege extends JavaPlugin {

    Messages msg;
    SetLobbyCommand slc;
    ConfigManager configManager;
    TeamManager teamManager;
    ScoreboardManager scoreboardManager;

    ArenaManager arenaManager;

    MobManager mobManager;

    ArenaSelector arenaSelector;
    TeamSelector teamSelector;

    PlayerManager playerManager;
    Regenerato regenerato;
    WorldEditPlugin worldEdit;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        arenaManager = new ArenaManager(this);
        getCommand("cs").setExecutor(new CreateArenaCommand(this, arenaManager));
        arenaSelector = new ArenaSelector(this);
        msg = new Messages(this);
        teamSelector = new TeamSelector(this);
        getCommand("leave").setExecutor(new LeaveArenaCommand(this));
        getCommand("setlobby").setExecutor(slc = new SetLobbyCommand(this));
        getServer().getPluginManager().registerEvents(new JoinEvent(this), this);
        getServer().getPluginManager().registerEvents(new DeathEvent(this), this);
        getServer().getPluginManager().registerEvents(new RightClickEffects(this), this);
        teamManager = new TeamManager(this, this.getConfig());
        mobManager = new MobManager(this);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CustomPlaceholder(this).register();
        }
        scoreboardManager = new ScoreboardManager(this);
        playerManager = new PlayerManager(this);
        getServer().getPluginManager().registerEvents(new MiscEvents(this), this);
        regenerato = (Regenerato) Bukkit.getPluginManager().getPlugin("Regenerato");
        worldEdit = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");

        if (regenerato == null || worldEdit == null) {
            getLogger().severe("Regenerato or WorldEdit plugin not found! Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
    }

    @Override
    public void onDisable() {

    }

    public MobManager getMobManager() {
        return mobManager;
    }

    public TeamSelector getTeamSelector() {
        return teamSelector;
    }

    public ArenaSelector getArenaSelector() {
        return arenaSelector;
    }

    public Messages getMsg(){
        return msg;
    }

    public SetLobbyCommand getSlc() {
        return slc;
    }

    public ConfigManager getConfigManager(){
        return configManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public ArenaManager getArenaManager(){
        return arenaManager;
    }

    public ScoreboardManager getScoreboardManager(){
        return scoreboardManager;
    }

    public Regenerato getRegenerato() {
        return regenerato;
    }
    public WorldEditPlugin getWorldEdit() {
        return worldEdit;
    }

}
