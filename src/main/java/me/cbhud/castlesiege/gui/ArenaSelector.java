package me.cbhud.castlesiege.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.cbhud.castlesiege.CastleSiege;
import me.cbhud.castlesiege.arena.Arena;
import me.cbhud.castlesiege.arena.ArenaManager;
import me.cbhud.castlesiege.arena.ArenaState;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;


public class ArenaSelector {

    private final Gui gui;
    private final CastleSiege plugin;
    private final ArenaManager arenaManager;

    public ArenaSelector(CastleSiege plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();
        gui = Gui.gui()
                .title(Component.text("§eSelect Arena"))
                .rows(4)
                .create();
    }

    // Removed synchronization
    private void init() {
        int slot = 0;
        for (Arena arena : arenaManager.getArenas()) {
            ArenaState state = arena.getState();
            Material woolMaterial;
            ChatColor statusColor;
            String status;

            switch (state) {
                case IN_GAME:
                    woolMaterial = Material.GREEN_WOOL;
                    statusColor = ChatColor.GREEN;
                    status = "§aIn Game";
                    break;
                case WAITING:
                    woolMaterial = Material.LIME_WOOL;
                    statusColor = ChatColor.YELLOW;
                    status = "§eWaiting for players...";
                    break;
                case ENDED:
                    woolMaterial = Material.RED_WOOL;
                    statusColor = ChatColor.DARK_RED;
                    status = "§4Restarting...";
                    break;
                default:
                    woolMaterial = Material.WHITE_WOOL;
                    statusColor = ChatColor.GRAY;
                    status = "§7Unknown";
                    break;
            }

            GuiItem arenaItem = ItemBuilder.from(woolMaterial)
                    .name(Component.text(statusColor + arena.getId()))
                    .lore(
                            Component.text(""),
                            Component.text("§bPlayers: §e" + arena.getNoPlayers()),
                            Component.text("§bStatus: " + status),
                            Component.text(""),
                            Component.text("§7Click to join!")
                    )
                    .asGuiItem(event -> handleArenaSelection(event, arena));

            gui.setItem(slot++, arenaItem);
        }
    }

    // Removed synchronization here as well
    private void handleArenaSelection(InventoryClickEvent event, Arena arena) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;

        event.setCancelled(true);

            plugin.getArenaManager().addPlayerToArena(player, arena);

        gui.close(player);
    }

    // Open GUI with sync task
    public void open(Player player) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            init();
            gui.open(player);
        });
    }
}
