package dev.fadest.koth.game.state.logic;

import dev.fadest.koth.game.Game;
import dev.fadest.koth.game.state.StateLogic;
import dev.fadest.koth.utils.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class EndingStateLogic implements StateLogic {

    @Override
    public void doLogic(@NotNull Game game) {
        Bukkit.broadcastMessage(Utilities.color("&7The &cKing of the Hill &7game &e" + game.getName() + " &7has ended."));

        Optional<Map.Entry<UUID, Long>> optionalWinningPlayer = game.getWinningPlayerUniqueId();
        if (!optionalWinningPlayer.isPresent()) {
            Bukkit.broadcastMessage(Utilities.color("&cNo players were able to capture the zone, no one won :("));
            return;
        }

        Map.Entry<UUID, Long> entry = optionalWinningPlayer.get();

        Player player = Bukkit.getPlayer(entry.getKey());

        Bukkit.broadcastMessage(Utilities.color(
                String.format(
                        "&c%s &7has won the &cKing of the Hill &7game with &c%d &7points.",
                        player.getName(),
                        entry.getValue()
                )
        ));

        player.sendMessage(Utilities.color("&7&oYou will receive the rewards in &c15 &7&oseconds, get to a safe place!"));
    }
}
