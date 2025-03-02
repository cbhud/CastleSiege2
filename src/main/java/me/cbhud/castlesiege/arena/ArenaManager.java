package me.cbhud.castlesiege.arena;

import me.cbhud.castlesiege.CastleSiege;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ArenaManager {
    private final Map<String, Arena> arenas = new HashMap<>();
    private final CastleSiege plugin;
    private final File configFile;
    private final FileConfiguration config;

    public ArenaManager(CastleSiege plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "arenas.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
        loadArenas();
    }

    private void loadArenas() {
        ConfigurationSection arenasSection = config.getConfigurationSection("arenas");
        if (arenasSection == null) return;

        for (String arenaId : arenasSection.getKeys(false)) {
            ConfigurationSection section = arenasSection.getConfigurationSection(arenaId);
            if (section == null) continue;

            Location lobbySpawn = parseLocation(section.getString("lobby-spawn"));
            Location kingSpawn = parseLocation(section.getString("king-spawn"));
            Location defendersSpawn = parseLocation(section.getString("defenders-spawn"));
            Location attackersSpawn = parseLocation(section.getString("attackers-spawn"));
            int autoStart = section.getInt("auto-start");
            int countdown = section.getInt("game-timer");
            int minPlayers = section.getInt("min-players");
            int maxPlayers = section.getInt("max-players");

            Arena arena = new Arena(plugin, arenaId, lobbySpawn, kingSpawn, attackersSpawn, defendersSpawn, maxPlayers, minPlayers, autoStart, countdown, new HashSet<>());
            arenas.put(arenaId, arena);
        }
    }

    private Location parseLocation(String locString) {
        if (locString == null || locString.isEmpty()) return null;
        String[] parts = locString.split(",");
        if (parts.length != 4) return null;
        return new Location(Bukkit.getWorld(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
    }

    public Arena getArena(String id) {
        return arenas.get(id);
    }

    public Set<Arena> getArenas() {
        return new HashSet<>(arenas.values());
    }

    public Arena getArenaByPlayer(Player player) {
        for (Arena arena : arenas.values()) {
            if (arena.getPlayers().contains(player)) {
                return arena;
            }
        }
        return null;
    }

}
