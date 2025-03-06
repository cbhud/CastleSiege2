package me.cbhud.castlesiege.arena;

import me.cbhud.castlesiege.CastleSiege;
import me.cbhud.castlesiege.team.Team;
import me.cbhud.castlesiege.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class Arena {
    private final String id;
    private final Location lobbySpawn;
    private final Location kingSpawn;
    private final Location attackersSpawn;
    private final Location defendersSpawn;
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

    public Arena(CastleSiege plugin, String id, Location lobbySpawn, Location kingSpawn, Location attackersSpawn, Location defendersSpawn, int max, int min, int autoStart, int countdown, Set<Player> players) {
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
        this.players = players;
        this.state = ArenaState.WAITING;
        this.teamManager = new TeamManager(plugin, plugin.getConfigManager().getConfig());
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
        if (players.size() >= getMax()) {
            player.sendMessage("Arena is full!");
            return false;
        }

        if (state != ArenaState.WAITING) return false;
        players.add(player);
        if (minPlayers >= getMin()) {
            startAutoStart(autoStart);
        }
        teamManager.tryRandomTeamJoin(player);
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
            });
        } else {
            winner = 0;
            plugin.getMobManager().removeCustomZombie(this);
            players.forEach(player -> {
                plugin.getScoreboardManager().updateScoreboard(player, "end");
                plugin.getMsg().getMessage("defenders-win-msg", player).forEach(player::sendMessage);
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

                // Clear the players, teams, and reset the state
                players.clear();
                teamManager.clearTeams();
                state = ArenaState.WAITING;
                winner = -1;

                // Perform any other cleanup (arena, stats, etc.)
                // TODO: clear arena
                // TODO: update stats
                // TODO: game end handler
            });
        }, 20 * 20L); // 20 seconds delay (20 ticks per second)
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
}