package dev.fadest.koth.game;

import dev.fadest.koth.game.area.AreaRunnable;
import dev.fadest.koth.game.loader.GameFactory;
import dev.fadest.koth.game.runnable.GameRunnable;
import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
public class GameManager {

    private final Set<Game> games;

    public GameManager() {
        this.games = new HashSet<>(GameFactory.loadGames());

        for (Game game : this.games) {
            game.init();
        }
    }

    /**
     * Loops through the game list and search for a game contains the provided location
     *
     * @param location The location to search for
     * @return An {@link Optional} that may contain the found {@link Game} that contains the provided {@link Location}
     */
    @NotNull
    public Optional<Game> getGameAtLocation(@NotNull Location location) {
        return this.games.stream().filter(game -> game.getGlobalBoundingBox() != null && game.getGlobalBoundingBox().contains(location)).findFirst();
    }

    /**
     * Loops through the game list and search for a game that matches the provided name
     *
     * @param name The name of the game to search
     * @return An {@link Optional} that may contain the found {@link Game}
     */
    @NotNull
    public Optional<Game> getGameFromName(@NotNull String name) {
        return this.games.stream().filter(game -> game.getName().equalsIgnoreCase(name)).findFirst();
    }

    /**
     * Creates and game, adding it to the list and creating a new file with its information
     *
     * @param game The game that will be removed
     */
    public void createAndSaveGame(@NotNull Game game) {
        this.games.add(game);

        GameFactory.saveFile(game);
    }

    /**
     * Deletes a game, removing it from the list and removing its file
     * This method will also cancel the {@link AreaRunnable} and {@link GameRunnable} of the Game
     *
     * @param game The game that will be removed
     */
    public void deleteGame(@NotNull Game game) {
        this.games.remove(game);

        if (game.getAreaRunnable() != null) {
            game.getAreaRunnable().cancel();
            game.setAreaRunnable(null);
        }

        game.getGameRunnable().cancel();
        game.setGameRunnable(null);

        game.getGameFile().delete();
    }
}
