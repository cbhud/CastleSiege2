package me.cbhud.castlesiege.arena;

import org.bukkit.Bukkit;
import me.cbhud.castlesiege.CastleSiege;
import me.cbhud.castlesiege.team.Team;
import me.cbhud.castlesiege.team.TeamManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import io.github.regenerato.worldedit.SchematicProcessor;
import io.github.regenerato.worldedit.NoSchematicException;
import org.bukkit.scheduler.BukkitRunnable;


import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Arena {
    private final String id;
    private Location lobbySpawn;
    private Location kingSpawn;
    private Location attackersSpawn;
    private Location defendersSpawn;
    private final int maxPlayers;
    private final int minPlayers;
    private final int autoStart;
    private final int countdown;
    private final Set<Player> players;
    private final CastleSiege plugin;
    private ArenaState state;
    private int countdownTimer;
    private int autoStartTimer;
    private int countdownTaskId = -1;
    private int autostartTaskId = -1;
    private TeamManager teamManager;
    private int winner;
    String worldName;

    public Arena(CastleSiege plugin, String id, Location lobbySpawn, Location kingSpawn, Location attackersSpawn, Location defendersSpawn, int max, int min, int autoStart, int countdown, String worldName) {
        this.plugin = plugin;
        this.id = id;
        this.lobbySpawn = lobbySpawn;
        this.kingSpawn = kingSpawn;
        this.attackersSpawn = attackersSpawn;
        this.defendersSpawn = defendersSpawn;
        this.maxPlayers = max;
        this.minPlayers = min;
        this.autoStart = autoStart;
        this.countdown = countdown;
        this.players = new HashSet<>();
        this.state = ArenaState.WAITING;
        this.teamManager = new TeamManager(plugin, plugin.getConfigManager().getConfig());
        this.worldName = worldName;
        this.winner = -1;
        Objects.requireNonNull(Bukkit.getWorld(worldName)).setAutoSave(false);
        Bukkit.broadcastMessage("WORLD AUTO SAVE JE: "+ Objects.requireNonNull(Bukkit.getWorld(worldName)).isAutoSave());
        Bukkit.broadcastMessage("WORLD AUTO SAVE JE: "+ Objects.requireNonNull(Bukkit.getWorld(worldName)).isAutoSave());
        Bukkit.broadcastMessage("WORLD AUTO SAVE JE: "+ Objects.requireNonNull(Bukkit.getWorld(worldName)).isAutoSave());
    }


    public Location getKingSpawn() {
        return kingSpawn;
    }

    public Location getAttackersSpawn() {
        return attackersSpawn;
    }

    public Location getDefendersSpawn() {
        return defendersSpawn;
    }

    public int getNoPlayers() {
        return players.size();
    }

    public int getMax() {
        return maxPlayers;
    }

    public int getMin() {
        return minPlayers;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public String getId() {
        return id;
    }

    public ArenaState getState() {
        return state;
    }

    public boolean addPlayer(Player player) {
        if (state == ArenaState.ENDED){
        player.sendMessage("§cThis arena is currently not available.");
        return false;
        }

        if (players.size() >= getMax()) {
            player.sendMessage("§cArena is full!");
            return false;
        }

        player.sendMessage("§aYou have joined the arena: " + getId());

        if(state == ArenaState.IN_GAME || !teamManager.tryRandomTeamJoin(player)) {
            player.sendTitle("§8You are now spectating!", "§7Please wait until game concludes or /leave", 10, 70, 20);
            plugin.getPlayerManager().setPlayerAsSpectating(player);
            player.teleport(getKingSpawn());
            players.add(player);
            return true;
        }
        players.add(player);
        player.teleport(getLobbySpawn());

        if (players.size() >= getMin()) {
            startAutoStart(autoStart);
        }
        plugin.getPlayerManager().setPlayerAsWaiting(player);

        return true;
    }

    public void removePlayer(Player player) {
        players.remove(player);
        teamManager.removePlayerFromTeam(player);
        plugin.getScoreboardManager().updateScoreboard(player, "lobby");
        if (players.size() < minPlayers && state == ArenaState.WAITING && autostartTaskId != -1) {
            stopAutoStart();
        }
        if (player != null) {
            plugin.getPlayerManager().setPlayerAsLobby(player);
        }
        plugin.getArenaManager().playerArenaMap.remove(player.getUniqueId());
    }

    public void startGame() {
        if (state != ArenaState.WAITING) {
            Bukkit.broadcastMessage("You cannot start game in this state!");
        }
        state = ArenaState.IN_GAME;
        plugin.getMobManager().spawnCustomMob(kingSpawn);
        players.forEach(player -> {
            plugin.getMsg().getMessage("game-start-msg", player).forEach(player::sendMessage);

        });
        teleportTeamsToSpawns();
        startCountdown(countdown);
    }

    public double getKingZombieHealth() {
        if (plugin.getMobManager().getKingZombie(kingSpawn.getWorld()) != null) {
            return plugin.getMobManager().getZombieHealth(plugin.getMobManager().getKingZombie(kingSpawn.getWorld()));
        } else {
            return 0.0;
        }
    }

    public void endGame() {
        state = ArenaState.ENDED;

        // Stop countdown if needed
        if (countdownTaskId != -1) {
            stopCountdown();
            winner = 1;

            players.forEach(player -> {
                plugin.getScoreboardManager().updateScoreboard(player, "end");
                plugin.getMsg().getMessage("attackers-win-msg", player).forEach(player::sendMessage);
                player.sendTitle(ChatColor.RED + plugin.getConfigManager().getAttacker(), "§ewon the game!", 10, 70, 20);

            });
        } else {
            winner = 0;
            plugin.getMobManager().removeCustomZombie(this);
            players.forEach(player -> {
                plugin.getScoreboardManager().updateScoreboard(player, "end");
                plugin.getMsg().getMessage("defenders-win-msg", player).forEach(player::sendMessage);
                player.sendTitle(ChatColor.AQUA + plugin.getConfigManager().getDefender(), "§ewon the game!", 10, 70, 20);
            });
        }

        // 20-second delay before teleporting players to the lobby
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Async teleportation to the lobby
            CompletableFuture.runAsync(() -> {
                players.forEach(player -> Bukkit.getScheduler().runTask(plugin, () -> {
                    player.teleport(plugin.getSlc().getLobby());
                    plugin.getArenaManager().removePlayerFromArena(player);
                    plugin.getScoreboardManager().updateScoreboard(player, "lobby");
                    plugin.getPlayerManager().setPlayerAsLobby(player);
                }));
                players.clear();
                teamManager.clearTeams();


                // TODO: clear arena
                // TODO: update stats
                // TODO: game end handler
            });
        }, 10 * 20L);

        Bukkit.getScheduler().runTaskLater(plugin, this::resetArena, 20 * 20L);



    }



    private void resetArena() {
        try {
            File schematicFolder = new File(plugin.getDataFolder(), "schematics");
            SchematicProcessor processor = SchematicProcessor.newSchematicProcessor(plugin.getWorldEdit(), getId(), schematicFolder);
            World world = Bukkit.getWorld(worldName);

            File configFile = new File(plugin.getDataFolder(), "arenas.yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            String arenaPath = "arenas." + getId();

            if (!config.contains(arenaPath + ".minX")) {
                Bukkit.broadcastMessage(ChatColor.RED + "Arena location not set! Use /arenasave first.");
                return;
            }

            int minX = config.getInt(arenaPath + ".minX");
            int maxX = config.getInt(arenaPath + ".maxX");
            int minY = config.getInt(arenaPath + ".minY");
            int maxY = config.getInt(arenaPath + ".maxY");
            int minZ = config.getInt(arenaPath + ".minZ");
            int maxZ = config.getInt(arenaPath + ".maxZ");

            int pasteX = config.getInt(arenaPath + ".pasteX");
            int pasteY = config.getInt(arenaPath + ".pasteY");
            int pasteZ = config.getInt(arenaPath + ".pasteZ");


            // Wait until clearing is done, then paste
            clearArena(world, minX, maxX, minY, maxY, minZ, maxZ, () -> {
                try {
                    Location pasteLocation = new Location(world, pasteX, pasteY, pasteZ);
                    processor.paste(pasteLocation);
                } catch (NoSchematicException e) {
                    throw new RuntimeException(e);
                }
                Bukkit.broadcastMessage(ChatColor.GREEN + "Arena " + getId() + " reset successfully!");
                state = ArenaState.WAITING;
                winner = -1;
            });


    }catch (Exception e) {
        Bukkit.broadcastMessage(ChatColor.RED + "Error resetting arena " + getId() + "!");
        e.printStackTrace();
    }
    }


    private void clearArena(World world, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Runnable onComplete) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            int batchSize = 5000;
            List<Block> blocks = new ArrayList<>();

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int y = minY; y <= maxY; y++) {
                        blocks.add(world.getBlockAt(x, y, z));
                    }
                }
            }

            new BukkitRunnable() {
                int index = 0;

                @Override
                public void run() {
                    int processed = 0;
                    while (index < blocks.size() && processed < batchSize) {
                        Block block = blocks.get(index++);
                        block.setType(Material.AIR, false);
                        processed++;
                    }

                    if (index >= blocks.size()) {
                        cancel();
                        if (onComplete != null) {
                            Bukkit.getScheduler().runTask(plugin, onComplete);
                        }
                    }
                }
            }.runTaskTimer(plugin, 1L, 1L);
        });
    }







    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public void startCountdown(int seconds) {
        if (countdownTaskId != -1) {
            return;
        }

        countdownTimer = seconds;

        countdownTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (countdownTimer <= 0) {
                    stopCountdown(); // Stops the countdown when it reaches 0
                    endGame();
                    return;
                }

                players.forEach(player -> plugin.getScoreboardManager().updateScoreboard(player, "in-game"));

                countdownTimer--;
            }
        }, 0L, 20L); // Runs every 20 ticks (1 second)
    }

    public void stopCountdown() {
        if (countdownTaskId != -1) {
            Bukkit.getScheduler().cancelTask(countdownTaskId);
            countdownTaskId = -1;
        }
    }

    public int getTimer() {
        return countdownTimer;
    }

    public void startAutoStart(int seconds) {
        if (autostartTaskId != -1) {
            return;
        }

        autoStartTimer = seconds;

        autostartTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (autoStartTimer <= 0) {
                    stopAutoStart();
                    startGame();
                    return;
                }

                players.forEach(player -> {
                    switch (autoStartTimer) {
                        case 60:
                            player.sendMessage("Starting in 60 seconds!");
                            break;
                        case 45:
                            player.sendMessage("Starting in 45 seconds!");
                            break;
                        case 30:
                            player.sendMessage("Starting in 30 seconds!");
                            break;
                        case 15:
                            player.sendMessage("Starting in 15 seconds!");
                            break;
                        case 10:
                            player.sendMessage("Starting in 10 seconds!");
                            break;
                        case 5:
                            player.sendMessage("Starting in 5 seconds!");
                            break;
                        case 4:
                            player.sendMessage("Starting in 4 seconds!");
                            break;
                        case 3:
                            player.sendMessage("Starting in 3 seconds!");
                            break;
                        case 2:
                            player.sendMessage("Starting in 2 seconds!");
                            break;
                        case 1:
                            player.sendMessage("Starting in 1 second!");
                            break;
                        default:
                            break;  // Optional: just in case the value doesn't match any case
                    }
                });

                autoStartTimer--;
            }
        }, 0L, 20L); // Runs every 20 ticks (1 second)
    }

    public void stopAutoStart() {
        if (autostartTaskId != -1) {
            Bukkit.getScheduler().cancelTask(autostartTaskId);
            autostartTaskId = -1;
            Bukkit.broadcastMessage("§cAuto-Start has been stopped!");
        }
    }

    public int getAttackersSize() {
        return teamManager.getPlayersInTeam(Team.Attackers);
    }

    public int getDefendersSize() {
        return teamManager.getPlayersInTeam(Team.Defenders);
    }

    public Team getTeam(Player player) {
        return teamManager.getTeam(player);
    }

    public boolean joinTeam(Player p, Team t) {
        teamManager.joinTeam(p, t);
        return true;
    }

    public void teleportTeamsToSpawns() {
        CompletableFuture.runAsync(() -> {
            for (Team team : Team.values()) {
                Location teamSpawn = getSpawnLocationForTeam(team);
                if (teamSpawn == null) continue;

                Set<Player> teamPlayers = teamManager.getPlayersInTeams(team);
                if (teamPlayers.isEmpty()) continue;

                Bukkit.getScheduler().runTask(plugin, () -> teamPlayers.forEach(player -> {
                    plugin.getPlayerManager().setPlayerAsPlaying(player);
                    player.teleport(teamSpawn);
                    if (!plugin.getPlayerKitManager().hasSelectedKit(player)){
                        plugin.getPlayerKitManager().setDefaultKit(player);
                    }
                    plugin.getPlayerKitManager().giveKit(player, plugin.getPlayerKitManager().getSelectedKit(player));
                }));
            }
        });
    }

    private Location getSpawnLocationForTeam(Team team) {
        if (team == Team.Attackers) {
            return getAttackersSpawn();
        } else {
            return getDefendersSpawn();
        }
    }

    public int getWinner() {
        return winner;
    }

    public Location getTeamSpawn(Team team) {
    if (team == Team.Attackers) {return attackersSpawn;}
    return defendersSpawn;
    }

    public void setLobbySpawn(Location loc) {
        this.lobbySpawn = loc;
    }

    public void setKingSpawn(Location loc) {
        this.kingSpawn = loc;
    }

    public void setDefendersSpawn(Location loc) {
        defendersSpawn = loc;
    }

    public void setAttackersSpawn(Location loc) {
        attackersSpawn = loc;
    }



}