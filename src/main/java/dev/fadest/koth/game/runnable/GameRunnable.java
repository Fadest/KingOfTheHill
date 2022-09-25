package dev.fadest.koth.game.runnable;

import dev.fadest.koth.game.Game;
import dev.fadest.koth.game.state.State;
import dev.fadest.koth.utils.BoundingBox;
import dev.fadest.koth.utils.StringUtilities;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@RequiredArgsConstructor
public class GameRunnable extends BukkitRunnable {

    private static final Set<Long> BROADCAST_SECONDS = LongStream.of(30, 60, 300, 600, 1800, 3600)
            .boxed().collect(Collectors.toSet());
    @Getter
    private final AtomicLong countdown = new AtomicLong(0);
    private final Game game;
    @Setter
    private boolean startedCounter = false;

    @Override
    public void run() {
        final State state = game.getState();
        switch (state) {
            case PREPARING:
                preparingTick();
                break;
            case STARTING:
                startingTick();
                break;
            case IN_GAME:
                gameTick();
                break;
            case ENDING:
                endingTick();
                break;
        }
    }

    private void preparingTick() {
        if (!startedCounter) {
            countdown.set(1);
            startedCounter = true;
        }

        final long timeRemaining = countdown.getAndDecrement();
        if (timeRemaining <= 0) {
            countdown.set(0);
            startedCounter = false;
            game.setState(State.STARTING);
        }
    }

    private void startingTick() {
        if (game.getDateOfNextStart() == null) {
            countdown.set(-1);
            startedCounter = false;
            return;
        }

        if (!startedCounter) {
            countdown.set(LocalDateTime.now().until(game.getDateOfNextStart(), ChronoUnit.SECONDS) + 1);
            startedCounter = true;
        }

        final long timeRemaining = countdown.getAndDecrement();
        BoundingBox boundingBox = game.getGlobalBoundingBox();

        if (BROADCAST_SECONDS.contains(timeRemaining)) {
            Bukkit.broadcastMessage(StringUtilities.color(
                            String.format("&6ℹ &6&lKOTH &r&7❙ &fThe game &6%s &flocated at &6X: &f%.2f &6Y: &f%.2f &6Z: " +
                                            "&f%.2f will start in &6%s",
                                    game.getName(),
                                    boundingBox.getMinX(),
                                    boundingBox.getMinY(),
                                    boundingBox.getMinZ(),
                                    StringUtilities.formatRemainingTime(TimeUnit.SECONDS.toMillis(timeRemaining))
                            )
                    )
            );
        }
        if (timeRemaining <= 10) {
            if (timeRemaining > 0) {
                Bukkit.broadcastMessage(StringUtilities.color(String.format("&fThe game &a%s &fwill start in &a%s", game.getName(), StringUtilities.formatRemainingTime(TimeUnit.SECONDS.toMillis(timeRemaining)))));
            }

            if (timeRemaining <= 0) {
                countdown.set(0);
                startedCounter = false;
                game.setState(State.IN_GAME);
            }
        }
    }

    private void gameTick() {
        if (!startedCounter) {
            countdown.set(game.getGameDuration() + 1);
            startedCounter = true;
        }

        final long timeRemaining = countdown.getAndDecrement();
        if (timeRemaining <= 0) {
            countdown.set(0);
            startedCounter = false;
            game.setState(State.ENDING);
        }
    }

    private void endingTick() {
        if (!startedCounter) {
            countdown.set(16);
            startedCounter = true;
        }

        final long timeRemaining = countdown.getAndDecrement();
        if (timeRemaining <= 0) {
            game.setDateOfNextStart(null);
            countdown.set(0);
            startedCounter = false;

            game.setState(State.PREPARING);
        }
    }
}
