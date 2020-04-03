package com.p3ng00.nametether;

import com.p3ng00.P3Plugin.P3Plugin;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static org.bukkit.Material.*;

public class NameTether extends P3Plugin {

    private static boolean allowAnyItem;
    private static boolean allowBow;
    private static boolean allowCrossbow;
    private static boolean allowTrident;

    public NameTether() {
        super(true);
    }

    @Override
    public void onEnable() {
        allowAnyItem = CONFIG.getBoolean("allow_any_item");
        allowBow = CONFIG.getBoolean("allow.bow");
        allowCrossbow = CONFIG.getBoolean("allow.crossbow");
        allowTrident = CONFIG.getBoolean("allow.trident");
        registerListener(new NTListener());
    }

    private static class NTListener implements Listener {

        @EventHandler
        public void projectileLaunched(ProjectileLaunchEvent event) {
            if (!(event.getEntity().getShooter() instanceof Player)) {
                return;
            }
            Player player = (Player)event.getEntity().getShooter();
            // stack[0] is bow
            // stack[1] is other item
            ItemStack[] stack = new ItemStack[2];
            // stack[0] is main
            // stack[1] is offhand
            ItemStack[] tempStack = {player.getInventory().getItemInMainHand(), player.getInventory().getItemInOffHand()};
            if (((allowBow && tempStack[0].getType() == BOW) || (allowCrossbow && tempStack[0].getType() == CROSSBOW) || (allowTrident && tempStack[0].getType() == TRIDENT)) && ((allowAnyItem && tempStack[1].getType() != AIR) || tempStack[1].getType() == NAME_TAG)) {
                stack[0] = tempStack[0];
                stack[1] = tempStack[1];
            } else if (((allowBow && tempStack[1].getType() == BOW) || (allowCrossbow && tempStack[1].getType() == CROSSBOW) || (allowTrident && tempStack[1].getType() == TRIDENT)) && ((allowAnyItem && tempStack[0].getType() != AIR) || tempStack[0].getType() == NAME_TAG)) {
                stack[0] = tempStack[1];
                stack[1] = tempStack[0];
            }
            if (stack[0] == null) {
                return;
            }
            if ((allowAnyItem || stack[1].hasItemMeta()) && (event.getEntity() instanceof AbstractArrow || event.getEntity() instanceof Trident)) {
                if (!(event.getEntity() instanceof Trident) && stack[1].getType() == ARROW || stack[1].getType() == SPECTRAL_ARROW || stack[1].getType() == TIPPED_ARROW) {
                    return;
                }
                ItemStack riding = new ItemStack(stack[1]);
                riding.setAmount(1);
                event.getEntity().addPassenger(player.getWorld().dropItem(player.getLocation(), riding));
                stack[1].setAmount(stack[1].getAmount() - 1);
            }
        }

        @EventHandler
        public void projectileHit(ProjectileHitEvent event) {
            if (event.getHitEntity() != null && (event.getEntity() instanceof AbstractArrow || event.getEntity() instanceof Trident)) {
                List<Entity> passengers = event.getEntity().getPassengers();
                if (passengers.size() == 1 && passengers.get(0) instanceof Item) {
                    Item item = (Item)passengers.get(0);
                    if (item.getItemStack().getType() == NAME_TAG && item.getItemStack().hasItemMeta()) {
                        event.getHitEntity().setCustomName(item.getItemStack().getItemMeta().getDisplayName());
                        item.remove();
                    }
                }
            }
        }
    }
}
