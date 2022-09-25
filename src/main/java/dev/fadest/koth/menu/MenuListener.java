package dev.fadest.koth.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof Menu) {
            event.setCancelled(true);
            Menu menu = (Menu) event.getClickedInventory().getHolder();

            menu.click((Player) event.getWhoClicked(), event.getSlot(), event.getClick());
        }
    }

}
