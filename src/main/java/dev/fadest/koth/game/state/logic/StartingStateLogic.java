package dev.fadest.koth.game.state.logic;

import dev.fadest.koth.game.Game;
import dev.fadest.koth.game.state.StateLogic;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StartingStateLogic implements StateLogic {

    @Override
    public void doLogic(@NotNull Game game) {
        Objects.requireNonNull(game, "Game must not be null");
    }
}
