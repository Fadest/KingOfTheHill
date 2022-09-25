package dev.fadest.koth.game.state.logic;

import dev.fadest.koth.game.Game;
import dev.fadest.koth.game.state.StateLogic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class PreparingStateLogic implements StateLogic {

    private final static Random RANDOM = ThreadLocalRandom.current();

    @Override
    public void doLogic(@NotNull Game game) {
        Objects.requireNonNull(game, "Game must not be null");

        Optional<Map.Entry<UUID, Long>> optionalWinningPlayer = game.getPlayerWithMostPoints();
        if (!optionalWinningPlayer.isPresent()) return;

        Map.Entry<UUID, Long> entry = optionalWinningPlayer.get();

        Player player = Bukkit.getPlayer(entry.getKey());
        int amountOfRewards = RANDOM.nextInt(game.getMinRewards(), game.getMaxRewards() + 1);
        for (int i = 0; i < amountOfRewards; i++) {
            game.giveReward(player);
        }
        game.getPlayerPoints().clear();
    }
}
