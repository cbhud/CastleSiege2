package me.cbhud.castlesiege.events;

import me.cbhud.castlesiege.CastleSiege;
import me.cbhud.castlesiege.arena.Arena;
import me.cbhud.castlesiege.arena.ArenaState;
import me.cbhud.castlesiege.team.Team;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class RightClickEffects implements Listener {

    private final CastleSiege plugin;
    private final Random rand = new Random();
    private static final int EFFECT_DURATION = 100;

    public RightClickEffects(CastleSiege plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack clickedItem = event.getItem();

        if (clickedItem == null) {
            return;
        }

        if (clickedItem.getType() == Material.COMPASS) {
            plugin.getArenaSelector().open(player);
            return;
        }

        Arena arena = plugin.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() == ArenaState.ENDED) {
            return;
        }

//        if (player.getInventory().getItemInMainHand().isSimilar(ItemManager.axe)) {
//            throwAxe(player);
//        }

        if (useSpecialItem(player, clickedItem, arena.getState())) {
            removeItem(player, clickedItem, arena.getState());
        }
    }

    private boolean useSpecialItem(Player player, ItemStack item, ArenaState arenaState) {
        if (item.getType() == Material.CLOCK && arenaState == ArenaState.WAITING) {
            plugin.getTeamSelector().open(player);
            return true;
        }

        if (item.getType() == Material.NETHER_STAR && arenaState == ArenaState.WAITING) {
            return true;
        }

//        if (item.isSimilar(ItemManager.stew)) {
//            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, EFFECT_DURATION, 1));
//            return true;
//        }
//
//        if (item.isSimilar(ItemManager.rage)) {
//            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, EFFECT_DURATION, 2));
//            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, EFFECT_DURATION, 1));
//            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, EFFECT_DURATION, 0));
//            return true;
//        }
//
//        if (item.isSimilar(ItemManager.ragnarok)) {
//            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, EFFECT_DURATION, 0));
//            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, EFFECT_DURATION, 0));
//            return true;
//        }
//
//        if (item.isSimilar(ItemManager.sight)) {
//            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, EFFECT_DURATION * 2, 1));
//            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, EFFECT_DURATION, 1));
//            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, EFFECT_DURATION, 1));
//            return true;
//        }
//
//        if (item.isSimilar(ItemManager.attack)) {
//            for (Player nearbyPlayer : player.getWorld().getPlayers()) {
//                if (nearbyPlayer.getLocation().distance(player.getLocation()) <= 10 &&
//                        plugin.getArenaManager().getArenaByPlayer(player).getTeam(player) == Team.Attackers) {
//                    applyRandomEffect(nearbyPlayer);
//                    player.sendMessage(ChatColor.RED + "Your spell has struck your opponents with powerful magic!");
//                }
//            }
//            return true;
//        }
//
//        if (item.isSimilar(ItemManager.support)) {
//            for (Player nearbyPlayer : player.getWorld().getPlayers()) {
//                if (nearbyPlayer.getLocation().distance(player.getLocation()) <= 10 &&
//                        plugin.getArenaManager().getArenaByPlayer(player).getTeam(player) == Team.Defenders) {
//                    applyRandomSupportEffect(nearbyPlayer);
//                    player.sendMessage(ChatColor.GREEN + "You have cast a supportive spell, empowering your nearby allies!");
//                }
//            }
//            return true;
//        }

        return false;
    }

    private void removeItem(Player player, ItemStack item, ArenaState arenaState) {
        if (arenaState != ArenaState.IN_GAME) {
            return;
        }

        player.getInventory().removeItem(item);
    }

    private void throwAxe(Player player) {
        try {
            Item axe = player.getWorld().dropItem(player.getEyeLocation(), player.getInventory().getItemInMainHand());
            axe.setVelocity(player.getEyeLocation().getDirection().multiply(1.1));
            player.getInventory().getItemInMainHand().setAmount(0);

            new BukkitRunnable() {
                public void run() {
                    for (Entity ent : axe.getNearbyEntities(0.5, 0.5, 0.5)) {
                        if (ent instanceof LivingEntity && ent != player) {
                            LivingEntity target = (LivingEntity) ent;
                            target.damage(2.5);
                            axe.setVelocity(new Vector(0, 0, 0));
                            this.cancel();
                            axe.remove();
                        }
                    }
                    if (axe.isOnGround()) {
                        axe.setVelocity(new Vector(0, 0, 0));
                        axe.remove();
                        this.cancel();
                    }
                }
            }.runTaskTimer(this.plugin, 0L, 1L);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void applyRandomEffect(Player player) {
        PotionEffectType[] effects = {PotionEffectType.POISON, PotionEffectType.SLOW, PotionEffectType.BLINDNESS};
        PotionEffectType effect = effects[rand.nextInt(effects.length)];
        player.addPotionEffect(new PotionEffect(effect, EFFECT_DURATION, 1));
    }

    private void applyRandomSupportEffect(Player player) {
        PotionEffectType[] effects = {PotionEffectType.REGENERATION, PotionEffectType.ABSORPTION, PotionEffectType.SPEED};
        PotionEffectType effect = effects[rand.nextInt(effects.length)];
        player.addPotionEffect(new PotionEffect(effect, EFFECT_DURATION, 1));
    }
}
