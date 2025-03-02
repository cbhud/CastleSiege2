package me.cbhud.castlesiege.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.cbhud.castlesiege.CastleSiege;
import me.cbhud.castlesiege.arena.Arena;
import me.cbhud.castlesiege.arena.ArenaManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import static me.cbhud.castlesiege.arena.ArenaState.WAITING;

public class ArenaSelector {

    private final Gui gui;
    private final CastleSiege plugin;
    private final ArenaManager arenaManager;

    public ArenaSelector(CastleSiege plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();  // Assuming you have this in your CastleSiege plugin
        gui = Gui.gui()
                .title(Component.text("§eSelect Arena"))
                .rows(6)  // Adjust the rows depending on how many arenas you have
                .create();

        init();
    }

    private void init() {
        int slot = 0;  // To keep track of where to place the items in the GUI
        for (Arena arena : arenaManager.getArenas()) {
            // Get arena status (assuming `isActive()` returns if the arena is available for play)
            String status = arena.getState().toString();

            // Build an item for each arena
            GuiItem arenaItem = ItemBuilder.from(Material.GREEN_WOOL)  // You can change the material to fit your theme
                    .name(Component.text("§r" + arena.getId()))  // Arena name
                    .lore(
                            Component.text("§BStatus: " + "§e" +status),  // Arena status
                            Component.text("§7Click to join!")
                    )
                    .asGuiItem(event -> handleArenaSelection(event, arena));

            // Place the item in the GUI
            gui.setItem(slot++, arenaItem);
        }
    }

    private void handleArenaSelection(InventoryClickEvent event, Arena arena) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;

        event.setCancelled(true);

        // If arena is active, teleport the player to the arena
        if (arena.getState() == WAITING) {
            arena.addPlayer(player);
            player.sendMessage("§aYou have joined the arena: " + arena.getId());
        } else {
            player.sendMessage("§cThis arena is currently not available.");
        }

        gui.close(player);
    }

    public void open(Player player) {
        gui.open(player);
    }
}
