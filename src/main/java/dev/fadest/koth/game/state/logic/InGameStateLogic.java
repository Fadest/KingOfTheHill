package dev.fadest.koth.game.state.logic;

import dev.fadest.koth.KOTHPlugin;
import dev.fadest.koth.game.Game;
import dev.fadest.koth.game.area.AreaRunnable;
import dev.fadest.koth.game.state.StateLogic;
import dev.fadest.koth.utils.BoundingBox;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public class InGameStateLogic implements StateLogic {

    @Override
    public void doLogic(@NotNull Game game) {
        AreaRunnable areaRunnable = new AreaRunnable(game);
        areaRunnable.runTaskTimer(KOTHPlugin.getInstance(), 20L, 20L);
        game.setAreaRunnable(areaRunnable);

        BoundingBox boundingBox = game.getGlobalBoundingBox();
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                        String.format("&aℹ &a&lKOTH &r&7❙ &fThe game &a%s &flocated at &aX: &f%.2f &aY: &f%.2f &aZ: " +
                                        "&f%.2f has &astarted!",
                                game.getName(),
                                boundingBox.getMinX(),
                                boundingBox.getMinY(),
                                boundingBox.getMinZ()
                        )
                )
        );
    }
}
