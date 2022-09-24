package dev.fadest.koth.game.state;

import dev.fadest.koth.game.Game;
import org.jetbrains.annotations.NotNull;

public interface StateLogic {


    /**
     * Method that will be run when the Game State has been changed
     *
     * @param game The current game instance
     */
    void doLogic(@NotNull Game game);
}
