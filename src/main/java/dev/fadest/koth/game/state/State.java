package dev.fadest.koth.game.state;

import dev.fadest.koth.game.state.logic.EndingStateLogic;
import dev.fadest.koth.game.state.logic.InGameStateLogic;
import dev.fadest.koth.game.state.logic.PreparingStateLogic;
import dev.fadest.koth.game.state.logic.StartingStateLogic;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum State {
    PREPARING(new PreparingStateLogic()),
    STARTING(new StartingStateLogic()),
    IN_GAME(new InGameStateLogic()),
    ENDING(new EndingStateLogic());

    private final StateLogic stateLogic;
}