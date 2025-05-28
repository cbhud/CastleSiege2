package me.cbhud.castlesiege.kit;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import me.cbhud.castlesiege.CastleSiege;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class ItemManager {

    private final CastleSiege plugin;

    public static ItemStack axe, stew, rage, ragnarok, sight, harm, sword, attack, support, spear;

    public ItemManager(CastleSiege plugin) {
        this.plugin = plugin;
        initItems();
    }

    private void initItems() {
        createItem("axe", Material.IRON_AXE, item -> axe = item);
        createItem("spear", Material.TRIDENT, item -> {
            item.addUnsafeEnchantment(Enchantment.LOYALTY, 1);
            spear = item;
        });
        createItem("stew", Material.MUSHROOM_STEW, item -> stew = item);
        createItem("rage", Material.NETHER_WART, item -> rage = item);
        createItem("ragnarok", Material.MAGMA_CREAM, item -> ragnarok = item);
        createItem("sight", Material.FERMENTED_SPIDER_EYE, item -> sight = item);
        createItem("harm", Material.TIPPED_ARROW, item -> harm = item);
        createItem("sword", Material.STONE_SWORD, item -> sword = item);
        createItem("attack", Material.BLAZE_ROD, item -> attack = item);
        createItem("support", Material.STICK, item -> support = item);
    }

    private void createItem(String key, Material material, java.util.function.Consumer<ItemStack> consumer) {
        String name = plugin.getMsg().getItemMessage("items." + key + ".name");
        java.util.List<String> loreLines = plugin.getMsg().getItemMessages("items." + key + ".lore");

        ItemBuilder builder = ItemBuilder.from(material)
                .name(Component.text(name))
                .flags(ItemFlag.HIDE_ATTRIBUTES);

        for (String line : loreLines) {
            builder.lore(Component.text(line));
        }

        consumer.accept(builder.build());
    }
}
