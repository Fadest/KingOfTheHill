package dev.fadest.koth.game;

import dev.fadest.koth.KOTHPlugin;
import dev.fadest.koth.game.area.AreaRunnable;
import dev.fadest.koth.game.reward.Reward;
import dev.fadest.koth.game.reward.RewardType;
import dev.fadest.koth.game.runnable.GameRunnable;
import dev.fadest.koth.game.state.State;
import dev.fadest.koth.game.state.StateLogic;
import dev.fadest.koth.utils.BoundingBox;
import dev.fadest.koth.utils.RandomPick;
import dev.fadest.koth.utils.StringUtilities;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class Game {

    private final static Random RANDOM = ThreadLocalRandom.current();

    private transient Map<UUID, Long> playerPoints = new HashMap<>();
    private transient State state = State.PREPARING;
    private transient LocalDateTime dateOfNextStart;
    private transient AreaRunnable areaRunnable;
    private transient GameRunnable gameRunnable;
    private transient String gameFileName;
    private String name, worldName;
    private BoundingBox captureZoneBoundingBox, globalBoundingBox;
    private long gameDuration;
    private int minRewards = 1, maxRewards = 1;
    private List<Reward> rewards = new ArrayList<>();

    public Game(@NotNull String name, @NotNull File gameFile) {
        this.name = name;
        this.gameFileName = gameFile.getName();

        init();
    }

    /**
     * Inits this Game, setting up the {@link State} AND {@link GameRunnable} instances
     */
    public void init() {
        this.state = State.PREPARING;
        this.gameRunnable = new GameRunnable(this);
        gameRunnable.runTaskTimerAsynchronously(KOTHPlugin.getInstance(), 20L, 20L);
    }

    /**
     * Selects a random reward and then give it to the {@param player}
     * This method will loop through itself until a reward is found is none was present
     *
     * @param player        The player that will receive a Reward
     * @param randomRewards The random pick instance with loaded rewards
     */
    private void giveReward(@NotNull Player player, @NotNull RandomPick<Reward> randomRewards) {
        Optional<Reward> rewardOptional = getRandomReward(randomRewards);
        if (!rewardOptional.isPresent()) {
            // Loop until we find a reward
            giveReward(player, randomRewards);
            return;
        }

        Reward reward = rewardOptional.get();

        int amount = RANDOM.nextInt(reward.getMinAmount(), reward.getMaxAmount() + 1);

        Runnable runnable = () -> {
            if (reward.getRewardType() == RewardType.COMMAND) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), reward.getCommand()
                        .replace("%player%", player.getName())
                        .replace("%amount%", Integer.toString(amount)));
            } else {
                ItemStack itemStack = reward.getItemStack().clone();
                if (itemStack.getMaxStackSize() > 1) {
                    itemStack.setAmount(amount);
                }

                final Location playerLocation = player.getLocation();

                player.getInventory().addItem(itemStack).values().forEach(item -> playerLocation.getWorld().dropItemNaturally(playerLocation, item));
            }

            final String rewardMessage = reward.getMessage();
            if (rewardMessage != null) {
                player.sendMessage(StringUtilities.color(rewardMessage
                        .replace("%amount%", Integer.toString(amount))
                        .replace("%player%", player.getName())
                ));
            }
        };

        // If it's running async, then we need to run it on the main thread
        // This also helps if the plugin is stopping so the runnable can be executed
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(KOTHPlugin.getInstance(), runnable);
        }
    }

    /**
     * Selects a random reward and then give it to the {@param player}
     * This method will loop through itself until a reward is found is none was present
     * <p>
     * It will also create a new {@link RandomPick} instance based on the rewards of this game and pass it
     * to the {@link Game#giveReward(Player, RandomPick)) method
     * <p>
     *
     * @param player The player that will receive a Reward
     */
    public void giveReward(@NotNull Player player) {
        RandomPick<Reward> randomRewards = new RandomPick<>();
        for (Reward reward : rewards) {
            randomRewards.add(reward.getChance(), reward);
        }

        this.giveReward(player, randomRewards);
    }

    /**
     * Gets a random reward from the list of rewards
     *
     * @return An {@link Optional} of {@link Reward}
     */
    public Optional<Reward> getRandomReward(@NotNull RandomPick<Reward> randomRewards) {
        return Optional.ofNullable(randomRewards.next());
    }

    /**
     * Gets a built File using the gameFileName field and in the games folder
     *
     * @return The file that matches this Game in configuration
     */
    public File getGameFile() {
        return new File(KOTHPlugin.getInstance().getDataFolder() + File.separator + "games", gameFileName);
    }

    /**
     * Gets the World loaded by Bukkit
     *
     * @return An instance of the {@link World} used by this game
     */
    @UnknownNullability
    public World getWorld() {
        if (this.worldName == null) return null;

        return Bukkit.getWorld(worldName);
    }

    /**
     * Sets the game state to the provided one and execute its logic
     * Using this method will use the logic from {@link StateLogic#doLogic(Game)}
     *
     * @param state the new state of this Game
     */
    public void setState(@NotNull State state) {
        this.state = state;

        Bukkit.getScheduler().runTask(KOTHPlugin.getInstance(), () -> state.getStateLogic().doLogic(this));
    }

    /**
     * Gets the player that have points in the game
     * <p>
     * Using this method will check if the playerPoints field is not null, and it is, it'll create a new instance of it
     * GSON doesn't use the default value of transients value, and therefore it will be null by default
     *
     * @return A map with players {@link UUID}s and their points
     */
    public Map<UUID, Long> getPlayerPoints() {
        if (this.playerPoints == null) {
            this.playerPoints = new HashMap<>();
        }
        return playerPoints;
    }

    /**
     * Selects the player who has more points in this game
     *
     * @return An {@link Optional} containing an entry of the player {@link UUID} and points
     */
    public Optional<Map.Entry<UUID, Long>> getPlayerWithMostPoints() {
        return getPlayerPoints().entrySet().stream()
                .filter(entry -> Objects.nonNull(Bukkit.getPlayer(entry.getKey())))
                .max(Comparator.comparingLong(Map.Entry::getValue));
    }
}
