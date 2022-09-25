package dev.fadest.koth.menu;

import dev.fadest.koth.utils.StringUtilities;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class Menu implements InventoryHolder {

    @Getter
    private final Inventory inventory;

    public Menu(int size, String title) {
        this.inventory = Bukkit.createInventory(this, size, StringUtilities.color(title));
    }

    /**
     * Opens the inventory to the provided player
     *
     * @param player The player to open the inventory for
     */
    public void open(@NotNull Player player) {
        player.openInventory(getInventory());
    }

    /**
     * This method will be executed when the player clicks on a slot
     *
     * @param player    The player who clicked
     * @param slot      The clicked slot
     * @param clickType The click type
     */
    public abstract void click(@NotNull Player player, int slot, @NotNull ClickType clickType);
}