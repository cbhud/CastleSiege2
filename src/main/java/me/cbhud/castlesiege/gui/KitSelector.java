package me.cbhud.castlesiege.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.cbhud.castlesiege.CastleSiege;
import me.cbhud.castlesiege.kit.KitManager;
import me.cbhud.castlesiege.kit.KitManager.KitData;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class KitSelector {

    private final Gui gui;
    private final CastleSiege plugin;

    public KitSelector(CastleSiege plugin) {
        this.plugin = plugin;
        this.gui = Gui.gui()
                .title(Component.text("§eSelect Kit"))
                .rows(3)
                .create();

        init();
    }

    private void init() {
        KitManager kitManager = plugin.getKitManager();
        int slot = 9;

        for (KitData kit : kitManager.loadKits()) {
            if (slot == 13){
                gui.setItem(slot++, new GuiItem(Material.AIR));
            }
            ItemStack displayItem = getDisplayItem(kit);
            String priceDisplay = kit.getPrice() <= 0 ? "§aFree" : "§6" + kit.getPrice() + " §ecoins";

            GuiItem guiItem = ItemBuilder.from(displayItem)
                    .name(Component.text("§r§c" + kit.getName()))
                    .lore(
                            Component.text("§r§eTeam: " + kit.getTeam()),
                            Component.text("§r"),
                            Component.text("§r§ePrice: " + priceDisplay),
                            Component.text("§r"),
                            Component.text("§r§7Left-click to select"),
                            Component.text("§r§7Right-click to purchase"))
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                    .asGuiItem(event -> handleKitSelection(event, kit));

            gui.setItem(slot++, guiItem);
        }
    }

    private ItemStack getDisplayItem(KitData kit) {
        if (!kit.getKitIcon().isEmpty()) {
            return new ItemStack(Material.valueOf(kit.getKitIcon()));
        }
        return new ItemStack(Material.CHEST);
    }

    public void open(Player player) {
        gui.open(player);
    }

    private void handleKitSelection(InventoryClickEvent event, KitData selectedKit) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;

        event.setCancelled(true);

        if (event.isRightClick()) {
            // Add purchase logic if needed
            // attemptToPurchaseKit(player, selectedKit);
        } else if (event.isLeftClick()) {
            selectKit(player, selectedKit);
            gui.close(player);
        }
    }

    private void selectKit(Player player, KitData selectedKit) {
        plugin.getPlayerKitManager().selectKit(player, selectedKit.getName());
    }
}
