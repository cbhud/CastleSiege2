package me.cbhud.castlesiege.utils;

import me.cbhud.castlesiege.CastleSiege;
import me.cbhud.castlesiege.arena.Arena;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomPlaceholder extends PlaceholderExpansion {
    private final CastleSiege plugin;

    public CustomPlaceholder(CastleSiege plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "cs"; // %castlesiege_<placeholder>%
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true; // Prevents the placeholder from being removed when PAPI reloads
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String identifier) {
        if (offlinePlayer == null) return null;

        // Convert OfflinePlayer to Player (only if online)
        Player player = offlinePlayer.getPlayer();
        if (player == null) return null; // Ensures the player is online before proceeding

        // Custom placeholders
        switch (identifier.toLowerCase()) {
            case "timer":
                return String.valueOf(getTimer(player)); // Example: %castlesiege_timer%
            case "king":
                return String.valueOf(getKingHealth(player)); // Example: %castlesiege_timer%
            case "team":
                return getPlayerTeam(player); // Example: %castlesiege_team%
            case "arena":
                return String.valueOf(getArena(plugin.getArenaManager().getArenaByPlayer(player)));
            case "arenasize":
                return String.valueOf(getArenaSize(plugin.getArenaManager().getArenaByPlayer(player)));
            default:
                return null; // Unknown placeholder
        }
    }

    private int getTimer(Player player) {
        return plugin.getArenaManager().getArenaByPlayer(player).getTimer();
    }

    private String getArena(Arena arena){
        return arena.getId();
    }

    private int getArenaSize(Arena arena){
        return arena.getPlayers().size();
    }

    private double getKingHealth(Player player){
        return plugin.getArenaManager().getArenaByPlayer(player).getKingZombieHealth();
    }

    private String getPlayerTeam(Player player) {
        // Placeholder logic: Fetch team from CastleSiege data
        return "Red"; // Replace with actual logic
    }
}
