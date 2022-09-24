package dev.fadest.koth;

import dev.fadest.koth.commands.KOTHCommand;
import dev.fadest.koth.game.GameManager;
import dev.fadest.koth.listeners.WorldListener;
import dev.fadest.koth.menu.MenuListener;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

@Getter
public class KOTHPlugin extends JavaPlugin {

    @Getter
    private static KOTHPlugin instance;

    private final Set<Material> bannedItems = EnumSet.of(
            Material.DIAMOND_AXE,
            Material.BOW,
            Material.ARROW,
            Material.LEATHER_HELMET,
            Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS,
            Material.LEATHER_BOOTS);
    private GameManager gameManager;

    @Override
    public void onEnable() {
        instance = this;
        this.gameManager = new GameManager();

        loadListeners();
        Objects.requireNonNull(getCommand("koth")).setExecutor(new KOTHCommand(gameManager));
    }

    public void loadListeners() {
        final PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new WorldListener(this), this);
        pluginManager.registerEvents(new MenuListener(), this);
    }
}
