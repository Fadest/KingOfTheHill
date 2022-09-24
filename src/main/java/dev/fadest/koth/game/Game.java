package dev.fadest.koth.game;

import dev.fadest.koth.KOTHPlugin;
import dev.fadest.koth.game.area.AreaRunnable;
import dev.fadest.koth.game.reward.Reward;
import dev.fadest.koth.game.reward.RewardType;
import dev.fadest.koth.game.runnable.GameRunnable;
import dev.fadest.koth.game.state.State;
import dev.fadest.koth.utils.BoundingBox;
import dev.fadest.koth.utils.RandomPick;
import dev.fadest.koth.utils.Utilities;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class Game {

    private final static Random RANDOM = ThreadLocalRandom.current();

    private transient Map<UUID, Long> playersCapturing = new HashMap<>();
    private transient State state = State.PREPARING;
    private transient LocalDateTime dateOfNextStart;
    private transient AreaRunnable areaRunnable;
    private transient GameRunnable gameRunnable;
    private transient String gameFileName;
    private String name, worldName;
    private BoundingBox captureZoneBoundingBox;
    private BoundingBox globalBoundingBox;
    private long gameSeconds;
    private int minRewards = 1;
    private int maxRewards = 1;
    private List<Reward> rewards = new ArrayList<>();

    public Game(String name, File gameFile) {
        this.name = name;
        this.gameFileName = gameFile.getName();
    }

    public void init() {
        this.state = State.PREPARING;
        this.gameRunnable = new GameRunnable(this);
        gameRunnable.runTaskTimerAsynchronously(KOTHPlugin.getInstance(), 20L, 20L);
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public void setState(@NotNull State state) {
        this.state = state;

        Bukkit.getScheduler().runTask(KOTHPlugin.getInstance(), () -> state.getStateLogic().doLogic(this));
    }
    //TODO IMPLEMENT DOCUMENTATION

    /**
     * Gets the winning pplayer
     */
    public Optional<Map.Entry<UUID, Long>> getWinningPlayerUniqueId() {
        return getPlayersCapturing().entrySet().stream()
                .filter(entry -> Objects.nonNull(Bukkit.getPlayer(entry.getKey())))
                .max(Comparator.comparingLong(Map.Entry::getValue));
    }

    /**
     * Selects a random reward and then give it to the {@param player}
     * This method will loop through itself until a reward is found is none was present
     *
     * @param player The player that will receive a Reward
     */
    public void giveReward(@NotNull Player player) {
        Optional<Reward> rewardOptional = getRandomReward();
        if (!rewardOptional.isPresent()) {
            //Loop until we find a reward
            giveReward(player);
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
                player.sendMessage(Utilities.color(rewardMessage
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
     * Gets a random reward from the list of rewards
     *
     * @return An {@link Optional} of {@link Reward}
     */
    public Optional<Reward> getRandomReward() {
        final RandomPick<Reward> randomRewards = new RandomPick<>();
        for (Reward reward : rewards) {
            randomRewards.add(reward.getChance(), reward);
        }

        return Optional.ofNullable(randomRewards.next());
    }

    public File getGameFile() {
        return new File(KOTHPlugin.getInstance().getDataFolder() + File.separator + "games", gameFileName);
    }

    public Map<UUID, Long> getPlayersCapturing() {
        // We have to declare a new instance, because transient fields go back to their default state, null in the case of Map
        if (this.playersCapturing == null) {
            this.playersCapturing = new HashMap<>();
        }
        return playersCapturing;
    }
}
