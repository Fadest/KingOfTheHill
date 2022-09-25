package dev.fadest.koth.game.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import dev.fadest.koth.KOTHPlugin;
import dev.fadest.koth.game.Game;
import dev.fadest.koth.utils.Pair;
import dev.fadest.koth.utils.item.ItemLoader;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@UtilityClass
public class GameFactory {

    private final static Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(ItemStack.class, new ItemLoader())
            .setPrettyPrinting()
            .setVersion(1.0)
            .create();

    /**
     * Loads all {@link Game}s from the config directory.
     * As well as the amount of games that failed to load.
     *
     * @return A {@link Pair} of the list of loaded games and the amount of games that failed to load.
     */
    @NotNull
    public List<Game> loadGames() {
        File folderFile = new File(KOTHPlugin.getInstance().getDataFolder() + File.separator + "games");
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }

        final Logger logger = KOTHPlugin.getInstance().getLogger();

        final Pair<List<Game>, AtomicInteger> pair = loadFrom(folderFile);
        logger.info(pair.getKey().size() + " games have been successfully loaded, and " + pair.getValue().get() + " have failed");

        return pair.getKey();
    }

    /**
     * Loads a {@link Game} from a file.
     * <p>
     * If the file is a directory, all files in the directory will be loaded.
     *
     * @param toLoad The file to load from.
     * @return A pair of the loaded games and the number of files loaded.
     */
    @NotNull
    public Pair<List<Game>, AtomicInteger> loadFrom(File toLoad) {
        final Logger logger = KOTHPlugin.getInstance().getLogger();
        final List<Game> loadedGames = new ArrayList<>();
        final AtomicInteger failedLoadedGames = new AtomicInteger(0);

        if (toLoad.isDirectory()) {
            File[] files = toLoad.listFiles((file, s) -> s.endsWith(".json"));

            if (files != null) {
                for (File file : files) {
                    Pair<List<Game>, AtomicInteger> pair = loadFrom(file);

                    loadedGames.addAll(pair.getKey());
                    failedLoadedGames.addAndGet(pair.getValue().get());
                }
            }

            return Pair.of(loadedGames, failedLoadedGames);
        }

        logger.warning("Loading file: " + toLoad.getName());

        try (FileReader fileReader = new FileReader(toLoad); JsonReader reader = new JsonReader(fileReader)) {
            final Game game = GSON.fromJson(reader, Game.class);
            game.setGameFileName(toLoad.getName());
            loadedGames.add(game);

            return Pair.of(loadedGames, failedLoadedGames);
        } catch (IOException e) {
            logger.warning("An error has occurred while loading a game, stack trace: "
                    + e.getMessage());
        }

        return Pair.of(loadedGames, failedLoadedGames);
    }

    /**
     * Save a {@link Game} to a file.
     *
     * @param game The game to save
     */
    @SneakyThrows
    public void saveFile(@NotNull Game game) {
        try (Writer writer = new FileWriter(game.getGameFile())) {
            GSON.toJson(game, writer);
        }
    }
}