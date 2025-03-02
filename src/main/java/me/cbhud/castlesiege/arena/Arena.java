package me.cbhud.castlesiege.arena;

import me.cbhud.castlesiege.CastleSiege;
import me.cbhud.castlesiege.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.Set;

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
    private boolean autoStartActive;
    private int timer;
    private HashMap<Player, Team> teamMap;
    private int countdownTaskId = -1;

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
        this.autoStartActive = false;
        this.teamMap = new HashMap<>();
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

    public int getMax() {
        return maxPlayers;
    }

    public int getMin() {
        return minPlayers;
    }

    public int getAutoStart() {
        return autoStart;
    }

    public int getCountdown() {
        return countdown;
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

    public void setState(ArenaState state) {
        this.state = state;
    }

    public boolean addPlayer(Player player) {
        if (state != ArenaState.WAITING) return false;
        players.add(player);
        player.teleport(lobbySpawn);
        if (checkIfMin()){
            startAutoStart(autoStart);
        }
        plugin.getScoreboardManager().updateScoreboard(player, "pre-game");
        //update scoreboard
        //give items Kit Selector and Team Selector
        //check for min players to start auto-start timer
        return true;
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public void startGame() {
        if (state == ArenaState.WAITING) {
            state = ArenaState.IN_GAME;
            plugin.getMobManager().spawnCustomMob(kingSpawn);
            for (Player p : players) {
                p.teleport(defendersSpawn);
                p.sendMessage("The game has started");
                plugin.getScoreboardManager().updateScoreboard(p, "in-game");
            }

            startCountdown(countdown);

        }
    }

    public Zombie getKing(){
        return plugin.getMobManager().getKingZombie(kingSpawn.getWorld());
    }

    public double getKingZombieHealth(){
        if(plugin.getMobManager().getKingZombie(kingSpawn.getWorld()) != null){
        return plugin.getMobManager().getZombieHealth(plugin.getMobManager().getKingZombie(kingSpawn.getWorld()));
        }else {
            return 0.0;
        }
    }


    public boolean checkIfMin(){
        return players.size() >= minPlayers;
    }

    public void endGame() {
        state = ArenaState.ENDED;
        if (countdownTaskId != -1) {
            stopCountdown();
            for (Player player : players) {
                plugin.getScoreboardManager().updateScoreboard(player, "end");
                    player.sendMessage("Vikings won!");
            }
                } else {
            plugin.getMobManager().removeCustomZombie(this);
            for (Player player : players) {
                player.sendMessage("Franks won!");
            }
                }
        for (Player player : players) {
            player.teleport(plugin.getSlc().getLobby());
            plugin.getScoreboardManager().updateScoreboard(player, "lobby");

        }
        //clear arena
        //set arena to pre_game
        //update stats
        //gameendhandler
        players.clear();
        state = ArenaState.WAITING;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public void startCountdown(int seconds) {
        if (autoStartActive) {
            Bukkit.broadcastMessage("§cA countdown is already running!");
            return;
        }

        timer = seconds;
        autoStartActive = true;

        countdownTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (timer <= 0) {
                    stopCountdown(); // Stops the countdown when it reaches 0
                    endGame();
                    return;
                }

                for (Player player : players) {
                    plugin.getScoreboardManager().updateScoreboard(player,"in-game");
                    player.sendMessage("§eTime left: " + timer + " seconds");
                }

                timer--;
            }
        }, 0L, 20L); // Runs every 20 ticks (1 second)
    }

    public void stopCountdown() {
        if (countdownTaskId != -1) {
            Bukkit.getScheduler().cancelTask(countdownTaskId);
            countdownTaskId = -1;
            autoStartActive = false;
            Bukkit.broadcastMessage("§cCountdown has been canceled!");
        }
    }


    public int getTimer() {
        return timer;
    }

    public void startAutoStart(int seconds) {
        if (autoStartActive) {
            Bukkit.broadcastMessage("§cA countdown is already running!");
            return;
        }
        autoStartActive = true; // Mark countdown as active
        BukkitScheduler scheduler = Bukkit.getScheduler();

        scheduler.runTaskAsynchronously(plugin, () -> {
            for (int i = seconds; i >= 0; i--) {
                int timeLeft = i;
                try {
                    Thread.sleep(1000); // Sleep for 1 second (runs async)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    autoStartActive = false; // Reset flag if interrupted
                    return;
                }

                // Switch back to the main thread to safely interact with Bukkit API
                scheduler.runTask(plugin, () -> {
                    if (timeLeft > 0) {
                        for (Player player : players) {
                            player.sendMessage("§eStarting in: " + timeLeft + " seconds");
                        }
                    } else {
                        autoStartActive = false; // Reset flag when countdown ends
                        startGame();
                    }
                });
            }
        });
    }


}



