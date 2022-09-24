package dev.fadest.koth.listeners;

import dev.fadest.koth.KOTHPlugin;
import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

@AllArgsConstructor
public class WorldListener implements Listener {

    private final KOTHPlugin plugin;

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().hasPermission("koth.break.bypass")) return;

        Location location = event.getBlock().getLocation();
        if (locationIsInsideAGame(location)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().hasPermission("koth.place.bypass")) return;

        Location location = event.getBlock().getLocation();
        if (locationIsInsideAGame(location)) {
            event.setCancelled(true);
        }
    }

    private boolean locationIsInsideAGame(Location location) {
        return plugin.getGameManager().getGameAtLocation(location).isPresent();
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getPlayer().hasPermission("koth.drop.bypass")) return;
        if (!plugin.getBannedItems().contains(event.getItemDrop().getItemStack().getType())) return;
        if (!locationIsInsideAGame(event.getPlayer().getLocation())) return;

        event.setCancelled(true);
    }

}
