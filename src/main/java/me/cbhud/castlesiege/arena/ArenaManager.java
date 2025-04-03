package me.cbhud.castlesiege.arena;

import org.bukkit.Bukkit;
import java.io.File;

import me.cbhud.castlesiege.CastleSiege;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ArenaManager {
    private final Map<String, Arena> arenas = new ConcurrentHashMap<>();
    final Map<UUID, Arena> playerArenaMap = new ConcurrentHashMap<>();
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

            String worldName = section.getString("king-spawn").split(",")[0];
            Location lobbySpawn = parseLocation(section.getString("lobby-spawn"));
            Location kingSpawn = parseLocation(section.getString("king-spawn"));
            Location defendersSpawn = parseLocation(section.getString("defenders-spawn"));
            Location attackersSpawn = parseLocation(section.getString("attackers-spawn"));
            int autoStart = section.getInt("auto-start");
            int countdown = section.getInt("game-timer");
            int minPlayers = section.getInt("min-players");
            int maxPlayers = section.getInt("max-players");
            Arena arena = new Arena(plugin, arenaId, lobbySpawn, kingSpawn, attackersSpawn, defendersSpawn, maxPlayers, minPlayers, autoStart, countdown, worldName);
            arenas.put(arenaId, arena);
        }
    }

    private Location parseLocation(String locString) {
        if (locString == null || locString.isEmpty()) return null;
        String[] parts = locString.split(",");
        if (parts.length != 4) return null;
        return new Location(Bukkit.getWorld(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
    }

    public Set<Arena> getArenas() {
        return new HashSet<>(arenas.values());
    }

    public boolean addArena(Arena arena) {
        if (arenas.containsKey(arena.getId())) return false;
        arenas.put(arena.getId(), arena);
        saveArena(arena);
        return true;
    }

    public Arena getArenaByPlayer(UUID playerId) {
        return playerArenaMap.get(playerId);
    }

    public void addPlayerToArena(Player player, Arena arena) {
        UUID playerId = player.getUniqueId();
        playerArenaMap.put(playerId, arena);
        arena.addPlayer(player);
    }

    public void removePlayerFromArena(Player player) {
        UUID playerId = player.getUniqueId();
        Arena arena = getArenaByPlayer(playerId);
        if (arena != null) {
            arena.removePlayer(player);
            playerArenaMap.remove(playerId);
        }
    }

    public void saveArena(Arena arena) {
        ConfigurationSection arenasSection = config.getConfigurationSection("arenas");
        if (arenasSection == null) {
            arenasSection = config.createSection("arenas");
        }

        ConfigurationSection section = arenasSection.createSection(arena.getId());
        section.set("lobby-spawn", formatLocation(arena.getLobbySpawn()));
        section.set("king-spawn", formatLocation(arena.getKingSpawn()));
        section.set("defenders-spawn", formatLocation(arena.getDefendersSpawn()));
        section.set("attackers-spawn", formatLocation(arena.getAttackersSpawn()));
        section.set("auto-start", 60);
        section.set("game-timer", 300);
        section.set("min-players", arena.getMin());
        section.set("max-players", arena.getMax());

        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to format a Location as a string
    private String formatLocation(Location location) {
        if (location == null) return null;
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();
    }






}