package dev.fadest.koth.game;

import dev.fadest.koth.game.loader.GameFactory;
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
        this.games = new HashSet<>();

        this.games.addAll(GameFactory.loadGames());
        for (Game game : this.games) {
            game.init();
        }
    }

    @NotNull
    public Optional<Game> getGameAtLocation(@NotNull Location location) {
        return this.games.stream().filter(game -> game.getGlobalBoundingBox() != null && game.getGlobalBoundingBox().contains(location)).findFirst();
    }

    @NotNull
    public Optional<Game> getGameFromName(@NotNull String name) {
        return this.games.stream().filter(game -> game.getName().equalsIgnoreCase(name)).findFirst();
    }

    public void createAndSaveGame(Game game) {
        this.games.add(game);

        GameFactory.saveFile(game);
    }

    public void deleteGame(Game game) {
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
