package dev.fadest.koth.menu;

import dev.fadest.koth.utils.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class Menu implements InventoryHolder {

    private final Inventory inventory;

    public Menu(int size, String title) {
        this.inventory = Bukkit.createInventory(this, size, Utilities.color(title));
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return this.inventory;
    }

    public void open(@NotNull Player player) {
        player.openInventory(getInventory());
    }


    public abstract void click(Player player, int slot, ClickType clickType);
}