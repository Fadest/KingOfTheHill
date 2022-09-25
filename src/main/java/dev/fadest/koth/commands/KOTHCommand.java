package dev.fadest.koth.commands;

import dev.fadest.koth.KOTHPlugin;
import dev.fadest.koth.game.Game;
import dev.fadest.koth.game.GameManager;
import dev.fadest.koth.game.loader.GameFactory;
import dev.fadest.koth.game.reward.menu.RewardMainMenu;
import dev.fadest.koth.game.state.State;
import dev.fadest.koth.utils.BoundingBox;
import dev.fadest.koth.utils.StringUtilities;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class KOTHCommand implements CommandExecutor, TabCompleter {

    private final GameManager gameManager;

    @Override
    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "schedule": {
                if (!sender.hasPermission("koth.schedule")) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou don't have enough permissions to use this command."));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou need to specify a game in order to start it."));
                    sender.sendMessage(StringUtilities.color("&fCorrect Usage: &7/koth schedule <name> <time> (-add)"));
                    return true;
                }

                String name = args[1];
                Optional<Game> gameOptional = gameManager.getGameFromName(name);
                if (!gameOptional.isPresent()) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fCouldn't find game with the name &c" + name + "&f."));
                    return true;
                }

                Game game = gameOptional.get();
                if (game.getState() == State.IN_GAME) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fThe game &c" + name + " &fhas already started."));
                    return true;
                }

                if (game.getState() != State.STARTING) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fThe game &c" + name + " &fis not yet prepared."));
                    return true;
                }

                if (game.getWorldName() == null || game.getWorld() == null) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fThe game &c" + name + " &fhas an invalid world."));
                    return true;
                }

                if (game.getCaptureZoneBoundingBox() == null) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fThe game &c" + name + " &fdoesn't contains a capture zone."));
                    return true;
                }

                if (game.getGlobalBoundingBox() == null) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fThe game &c" + name + " &fdoesn't contains a global zone."));
                    return true;
                }

                if (game.getGameDuration() == 0) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fThe game &c" + name + " &fdoesn't have a setup duration."));
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou need to specify a time to schedule the game."));
                    sender.sendMessage(StringUtilities.color("&fExamples: &c05:10&f, &c0:15&f, &c100"));
                    return true;
                }

                LocalDateTime expectedTime = LocalDateTime.now();

                try {
                    int secondsFromNow = Integer.parseInt(args[2]);

                    expectedTime = expectedTime.plus(secondsFromNow, ChronoUnit.SECONDS);
                } catch (NumberFormatException e) {
                    String timeArguments = args[2];
                    String[] timeSplit = timeArguments.split(":");

                    boolean add = args.length > 3 && args[3].equalsIgnoreCase("-add");

                    if (timeSplit.length == 1) {
                        sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou need to input a proper format."));
                        sender.sendMessage(StringUtilities.color("&fExamples: &c05:10&f, &c0:15"));
                        return true;
                    }

                    try {
                        int hours = Integer.parseInt(timeSplit[0]);
                        int minutes = Integer.parseInt(timeSplit[1]);

                        if (add) {
                            expectedTime = expectedTime.plus(hours, ChronoUnit.HOURS).plus(minutes, ChronoUnit.MINUTES);
                        } else {
                            expectedTime = expectedTime.withHour(hours).withMinute(minutes);
                        }
                    } catch (NumberFormatException e1) {
                        sender.sendMessage(StringUtilities.color("&4✕&c&lKOTH &r&c❙ &fYou need to input a proper format."));
                        sender.sendMessage(StringUtilities.color("&fExamples: &c05:10&f, &c0:15"));
                        return true;
                    }
                }

                LocalDateTime currentTime = LocalDateTime.now();
                if (expectedTime.isBefore(currentTime)) {
                    sender.sendMessage(StringUtilities.color(
                            String.format(
                                    "&4✕ &c&lKOTH &r&7❙ &fYou tried to schedule a game to the past &7(&c%s&7)&f. Current Time is &c%s",
                                    expectedTime.getHour() + ":" + expectedTime.getMinute(),
                                    currentTime.getHour() + ":" + currentTime.getMinute()
                            )
                    ));
                    return true;
                }

                game.getGameRunnable().setStartedCounter(false);

                game.setDateOfNextStart(expectedTime);
                sender.sendMessage(StringUtilities.color(
                        String.format(
                                "&2✔ &a&lKOTH &r&7❙ &fYou have scheduled the game &a%s &fto start at &a%s&7. &a%s &fremaining.",
                                game.getName(),
                                expectedTime.getHour() + ":" + expectedTime.getMinute(),
                                StringUtilities.formatRemainingTime(currentTime.until(expectedTime, ChronoUnit.MILLIS))
                        )
                ));
                break;
            }
            case "list": {
                Set<Game> games = gameManager.getGames();
                if (games.isEmpty()) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fThere aren't any games loaded"));
                    return true;
                }

                if (!sender.hasPermission("koth.list.all") && games.stream().noneMatch(game -> game.getDateOfNextStart() != null)) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fThere aren't any scheduled games."));
                    return true;
                }

                final Comparator<Game> comparator = (o1, o2) -> Boolean.compare(o1.getState() == State.IN_GAME, o2.getState() == State.IN_GAME);

                List<Game> sortedGames = games.stream().sorted(
                        comparator.reversed()
                                .thenComparing((o1, o2) ->
                                        Comparator.nullsLast(LocalDateTime::compareTo).compare(o1.getDateOfNextStart(), o2.getDateOfNextStart())
                                )).collect(Collectors.toList()
                );

                boolean hasPermission = sender.hasPermission("koth.list.all");
                long totalGames = hasPermission ? sortedGames.size() : sortedGames.stream().filter(game -> game.getDateOfNextStart() != null).count();
                if (totalGames == 0) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fThere aren't any games."));
                    return true;
                }

                sender.sendMessage(StringUtilities.color("&c&lKOTH &r&7❙ &fGames: &e" + totalGames + "&f."));

                LocalDateTime currentTime = LocalDateTime.now();
                for (Game game : sortedGames) {
                    if (game.getState() == State.IN_GAME) {
                        sender.sendMessage(StringUtilities.color(String.format(
                                "&a" + game.getName() + " &7(&eSTARTED &7[&e%s left&7])",
                                StringUtilities.formatTimeMinutesAndSeconds(game.getGameRunnable().getCountdown().get()))));
                        continue;
                    }

                    if (!hasPermission) continue;

                    if (game.getDateOfNextStart() == null) {
                        sender.sendMessage(StringUtilities.color("&7" + game.getName() + " (INACTIVE)"));
                        continue;
                    }

                    sender.sendMessage(StringUtilities.color(String.format("&a" + game.getName() + " &7[&e%s&7]",
                            StringUtilities.formatRemainingTime(currentTime.until(game.getDateOfNextStart(), ChronoUnit.MILLIS)))));
                }
                break;
            }
            case "info": {
                if (!sender.hasPermission("koth.info")) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou don't have enough permissions to use this command."));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou need to specify a game in order to get the information of it."));
                    sender.sendMessage(StringUtilities.color("&fCorrect Usage: &7/koth info <name>"));
                    return true;
                }

                String name = args[1];
                Optional<Game> gameOptional = gameManager.getGameFromName(name);
                if (!gameOptional.isPresent()) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fCouldn't find game with the name &c" + name + "&f."));
                    return true;
                }

                Game game = gameOptional.get();
                sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fInformation about &c" + name + "&f:"));
                sender.sendMessage(StringUtilities.color("&f• &cWorld Name: &f" + (game.getWorldName() == null ? "&eNot setup" : game.getWorldName())));
                sender.sendMessage(StringUtilities.color("&f• &cFile: &f" + game.getGameFileName()));
                sender.sendMessage(StringUtilities.color("&f• &cState: &f" + game.getState().name()));
                sender.sendMessage(StringUtilities.color("&f• &cRewards: &f" + game.getRewards().size()));
                sender.sendMessage(StringUtilities.color("&f• &cMinimum Rewards: &f" + game.getMinRewards()));
                sender.sendMessage(StringUtilities.color("&f• &cMaximum Rewards: &f" + game.getMaxRewards()));
                sender.sendMessage(StringUtilities.color("&f• &cGame Duration (in seconds): &f" + game.getGameDuration()));
                final LocalDateTime currentTime = LocalDateTime.now();
                if (game.getDateOfNextStart() != null && game.getDateOfNextStart().isAfter(currentTime)) {
                    sender.sendMessage(StringUtilities.color("&f• &cNext game: &f" +
                            StringUtilities.formatRemainingTime(currentTime.until(game.getDateOfNextStart(), ChronoUnit.MILLIS))));
                }
                if (game.getState() == State.IN_GAME) {
                    sender.sendMessage(StringUtilities.color("&f• &cEnds in: &f" +
                            StringUtilities.formatRemainingTime(TimeUnit.SECONDS.toMillis(game.getGameRunnable().getCountdown().get()))));
                }

                BoundingBox globalBoundingBox = game.getGlobalBoundingBox();

                sender.sendMessage(StringUtilities.color("&f• &cGlobal Area: " + (game.getGlobalBoundingBox() == null ? "&eNot setup" :
                        String.format(
                                "&ffrom &7(&f%.0f %.0f %.0f&7) &fto &7(&f%.0f %.0f %.0f&7)",
                                globalBoundingBox.getMinX(),
                                globalBoundingBox.getMinY(),
                                globalBoundingBox.getMinZ(),
                                globalBoundingBox.getMaxX(),
                                globalBoundingBox.getMaxY(),
                                globalBoundingBox.getMaxZ()
                        )))
                );

                BoundingBox captureZoneBoundingBox = game.getCaptureZoneBoundingBox();

                sender.sendMessage(StringUtilities.color("&f• &cCapture Zone Area: " + (game.getCaptureZoneBoundingBox() == null ? "&eNot setup" :
                        String.format(
                                "&ffrom &7(&f%.0f %.0f %.0f&7) &fto &7(&f%.0f %.0f %.0f&7)",
                                captureZoneBoundingBox.getMinX(),
                                captureZoneBoundingBox.getMinY(),
                                captureZoneBoundingBox.getMinZ(),
                                captureZoneBoundingBox.getMaxX(),
                                captureZoneBoundingBox.getMaxY(),
                                captureZoneBoundingBox.getMaxZ()
                        )))
                );
                break;
            }
            case "add":
            case "create":
            case "c":
            case "new": {
                if (!sender.hasPermission("koth.create")) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou don't have enough permissions to use this command."));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou need to specify a name in order to create it."));
                    sender.sendMessage(StringUtilities.color("&fCorrect Usage: &7/koth create <name>"));
                    return true;
                }

                String name = args[1];
                Optional<Game> gameOptional = gameManager.getGameFromName(name);
                if (gameOptional.isPresent()) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fA game with the name &c" + name + "&f already exists."));
                    return true;
                }

                File folderFile = new File(KOTHPlugin.getInstance().getDataFolder() + File.separator + "games");
                if (!folderFile.exists()) {
                    folderFile.mkdirs();
                }

                File file = new File(folderFile, name + ".json");
                if (file.exists()) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fThere was an error while creating the game, a file with the same name already exists."));
                    return true;
                }
                file.createNewFile();

                Game game = new Game(name, file);
                gameManager.createAndSaveGame(game);

                sender.sendMessage(StringUtilities.color(
                        String.format("&2✔ &a&lKOTH &r&7❙ &fYou have created a new game named &a%s&f.", game.getName())
                ));
                break;
            }
            case "remove":
            case "delete":
            case "del": {
                if (!sender.hasPermission("koth.delete")) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou don't have enough permissions to use this command."));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou need to specify a game in order to delete it."));
                    sender.sendMessage(StringUtilities.color("&fCorrect Usage: &7/koth delete <name>"));
                    return true;
                }

                String name = args[1];
                Optional<Game> gameOptional = gameManager.getGameFromName(name);
                if (!gameOptional.isPresent()) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fCouldn't find game with the name &c" + name + "&f."));
                    return true;
                }

                Game game = gameOptional.get();
                if (game.getState() == State.IN_GAME || game.getState() == State.ENDING) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou can't delete a game in progress."));
                    return true;
                }

                gameManager.deleteGame(game);

                sender.sendMessage(StringUtilities.color(
                        String.format("&2✔ &a&lKOTH &r&7❙ &fYou have deleted the game named &a%s&f.", game.getName())
                ));
                break;
            }
            case "cancel":
            case "cancel_schedule": {
                if (!sender.hasPermission("koth.cancel")) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou don't have enough permissions to use this command."));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou need to specify a game in order to cancel it."));
                    sender.sendMessage(StringUtilities.color("&fCorrect Usage: &7/koth cancel <name>"));
                    return true;
                }

                String name = args[1];
                Optional<Game> gameOptional = gameManager.getGameFromName(name);
                if (!gameOptional.isPresent()) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fCouldn't find game with the name &c" + name + "&f."));
                    return true;
                }

                Game game = gameOptional.get();
                if (game.getState() == State.IN_GAME || game.getState() == State.ENDING) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou can't cancel a game in progress."));
                    return true;
                }

                game.setDateOfNextStart(null);

                sender.sendMessage(StringUtilities.color(
                        String.format("&2✔ &a&lKOTH &r&7❙ &fYou have cancelled the game named &a%s&f.", game.getName())
                ));
                break;
            }
            case "stop":
            case "stop_game": {
                if (!sender.hasPermission("koth.stop")) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou don't have enough permissions to use this command."));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou need to specify a game in order to stop it."));
                    sender.sendMessage(StringUtilities.color("&fCorrect Usage: &7/koth stop <name>"));
                    return true;
                }

                String name = args[1];
                Optional<Game> gameOptional = gameManager.getGameFromName(name);
                if (!gameOptional.isPresent()) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fCouldn't find game with the name &c" + name + "&f."));
                    return true;
                }

                Game game = gameOptional.get();
                if (game.getState() != State.IN_GAME && game.getState() != State.ENDING) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou can't stop a game that hasn't started."));
                    return true;
                }

                game.getAreaRunnable().cancel();
                game.getGameRunnable().setStartedCounter(false);
                game.setDateOfNextStart(null);
                game.getPlayerPoints().clear();
                game.setState(State.STARTING);

                sender.sendMessage(StringUtilities.color(
                        String.format("&2✔ &a&lKOTH &r&7❙ &fYou have stopped the game named &a%s&f.", game.getName())
                ));
                break;
            }
            case "edit": {
                if (!sender.hasPermission("koth.edit")) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou don't have enough permissions to use this command."));
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou need to specify a game in order to edit it."));
                    sender.sendMessage(StringUtilities.color("&fCorrect Usage: &7/koth edit <name> <world/rewards/capture_zone/global_zone/duration>"));
                    return true;
                }

                String name = args[1];
                Optional<Game> gameOptional = gameManager.getGameFromName(name);
                if (!gameOptional.isPresent()) {
                    sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fCouldn't find game with the name &c" + name + "&f."));
                    return true;
                }

                Game game = gameOptional.get();

                switch (args[2].toLowerCase(Locale.ROOT)) {
                    case "reward":
                    case "rewards":
                    case "loot":
                    case "loots": {
                        if (isNotPlayer(sender)) return true;

                        new RewardMainMenu(game).open((Player) sender);
                        return true;
                    }
                    case "min":
                    case "minimal":
                    case "minimal_rewards":
                    case "min_rewards":
                    case "minimum_rewards": {
                        if (args.length < 4) {
                            sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou need to specify the minimum rewards to edit it"));
                            sender.sendMessage(StringUtilities.color("&fCorrect Usage: &7/koth edit minimum_rewards <amount>"));
                            return true;
                        }

                        try {
                            int amount = Integer.parseInt(args[3]);

                            game.setMinRewards(amount);
                            sender.sendMessage(StringUtilities.color(
                                    String.format(
                                            "&2✔ &a&lKOTH &r&7❙ &fYou have changed &a%s &fminimum rewards to &a%d&f.",
                                            game.getName(),
                                            amount
                                    )
                            ));
                        } catch (NumberFormatException e) {
                            sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fInvalid number: &c" + args[3] + "&f."));
                            return true;
                        }
                        break;
                    }
                    case "max":
                    case "maximum":
                    case "maximal_rewards":
                    case "max_rewards":
                    case "maximum_rewards": {
                        if (args.length < 4) {
                            sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou need to specify the maximum rewards to edit it"));
                            sender.sendMessage(StringUtilities.color("&fCorrect Usage: &7/koth edit maximum_rewards <amount>"));
                            return true;
                        }

                        try {
                            int amount = Integer.parseInt(args[3]);

                            game.setMaxRewards(amount);
                            sender.sendMessage(StringUtilities.color(
                                    String.format(
                                            "&2✔ &a&lKOTH &r&7❙ &fYou have changed &a%s &fmaximum rewards to &a%d&f.",
                                            game.getName(),
                                            amount
                                    )
                            ));
                        } catch (NumberFormatException e) {
                            sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fInvalid number: &c" + args[3] + "&f."));
                            return true;
                        }
                        break;
                    }
                    case "world": {
                        if (args.length < 4) {
                            sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou need to specify a world to edit it"));
                            sender.sendMessage(StringUtilities.color("&fValid Worlds: &c" + Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.joining("&f, &c"))));
                            return true;
                        }

                        String worldName = args[3];
                        World world = Bukkit.getWorld(worldName);
                        if (world == null) {
                            sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fThe world &c" + worldName + " &f doesn't exists"));
                            sender.sendMessage(StringUtilities.color("fExamples: &cworld&f, &cgame_world&f, koth_world"));
                            return true;
                        }

                        game.setWorldName(world.getName());
                        sender.sendMessage(StringUtilities.color(
                                String.format(
                                        "&2✔ &a&lKOTH &r&7❙ &fYou have changed &a%s &fworld to &a%s.",
                                        game.getName(),
                                        game.getWorldName()
                                )
                        ));
                        break;
                    }
                    case "game_second":
                    case "game_seconds":
                    case "seconds":
                    case "duration": {
                        if (args.length < 4) {
                            sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou need to specify the duration to edit it"));
                            sender.sendMessage(StringUtilities.color("&fCorrect Usage: &7/koth edit duration <seconds>"));
                            return true;
                        }
                        try {
                            long seconds = Long.parseLong(args[3]);

                            game.setGameDuration(seconds);
                            sender.sendMessage(StringUtilities.color(
                                    String.format(
                                            "&2✔ &a&lKOTH &r&7❙ &fYou have changed &a%s &fduration to &a%d&f.",
                                            game.getName(),
                                            seconds
                                    )
                            ));
                        } catch (NumberFormatException e) {
                            sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fInvalid number: &c" + args[3] + "&f."));
                            return true;
                        }
                        break;
                    }
                    case "capture":
                    case "hill":
                    case "hill_zone":
                    case "capture_zone": {
                        if (isNotPlayer(sender)) return true;

                        if (args.length < 4) {
                            sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou need to specify what point of the area you will edit"));
                            sender.sendMessage(StringUtilities.color("&fCorrect Usage: &7/koth edit capture_zone <minimum/maximum)"));
                            return true;
                        }

                        Player player = (Player) sender;
                        BoundingBox boundingBox = modifyBoundingBox(player, game.getCaptureZoneBoundingBox(), args);
                        if (boundingBox == null) return true;

                        game.setCaptureZoneBoundingBox(boundingBox);

                        sender.sendMessage(StringUtilities.color("&2✔ &a&lKOTH &r&7❙ &fYou have modified &a" + game.getName() + " &fcapture zone&f."));
                        break;
                    }
                    case "global":
                    case "total":
                    case "total_zone":
                    case "global_zone": {
                        if (isNotPlayer(sender)) return true;

                        if (args.length < 4) {
                            sender.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou need to specify what point of the area you will edit"));
                            sender.sendMessage(StringUtilities.color("&fCorrect Usage: &7/koth edit global_zone <minimum/maximum)"));
                            return true;
                        }

                        Player player = (Player) sender;
                        BoundingBox boundingBox = modifyBoundingBox(player, game.getGlobalBoundingBox(), args);
                        if (boundingBox == null) return true;

                        game.setGlobalBoundingBox(boundingBox);

                        sender.sendMessage(StringUtilities.color("&2✔ &a&lKOTH &r&7❙ &fYou have modified &a" + game.getName() + " &fglobal zone&f."));
                        break;
                    }
                    default: {
                        sender.sendMessage(StringUtilities.color("&fCorrect Usage: &7/koth edit <name> <world/rewards/capture_zone/global_zone/duration>"));
                        return true;
                    }
                }

                GameFactory.saveFile(game);
                break;
            }
            default:
                sendHelpMessage(sender);
                break;
        }
        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(StringUtilities.color("&c&lKOTH &r&7❙ &fCommands:"));
        sender.sendMessage(" ");
        sender.sendMessage(StringUtilities.color("&c/&fkoth list &7- Shows the scheduled games"));
        if (sender.hasPermission("koth.info")) {
            sender.sendMessage(StringUtilities.color("&c/&fkoth info <name> &7- Show the information of a game"));
        }

        if (sender.hasPermission("koth.schedule")) {
            sender.sendMessage(StringUtilities.color("&c/&fkoth schedule <name> <time> (-add) &7- Schedule a game to run"));
        }

        if (sender.hasPermission("koth.cancel")) {
            sender.sendMessage(StringUtilities.color("&c/&fkoth cancel <name> &7- Cancels the schedule of a yet to start game"));
        }

        if (sender.hasPermission("koth.stop")) {
            sender.sendMessage(StringUtilities.color("&c/&fkoth stop <name> &7- Stops an started game"));
        }

        if (sender.hasPermission("koth.create")) {
            sender.sendMessage(StringUtilities.color("&c/&fkoth create <name> &7- Creates a new game"));
        }

        if (sender.hasPermission("koth.delete")) {
            sender.sendMessage(StringUtilities.color("&c/&fkoth delete <name> &7- Deletes a game"));
        }

        if (sender.hasPermission("koth.edit")) {
            sender.sendMessage(StringUtilities.color("&c/&fkoth edit <name> <world/rewards/capture_zone/global_zone/duration> &7 - Edit the specified game"));
        }
    }

    private boolean isNotPlayer(CommandSender sender) {
        if (sender instanceof Player) return false;

        sender.sendMessage(StringUtilities.color("&cYou need to be a player in order to execute this command"));
        return true;
    }

    private BoundingBox modifyBoundingBox(Player player, BoundingBox existingBoundingBox, String[] arguments) {
        boolean maximum = false;
        switch (arguments[3].toLowerCase(Locale.ROOT)) {
            case "minimum":
            case "min":
            case "minimal":
                break;
            case "maximum":
            case "max":
                maximum = true;
                break;
            default: {
                player.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fInvalid point: &c" + arguments[3] + "&f."));
                return null;
            }
        }

        Location playerLocation = player.getLocation();
        BoundingBox boundingBox = existingBoundingBox;
        if (boundingBox == null) {
            boundingBox = BoundingBox.empty();
        }

        if (maximum) {
            boundingBox.setMaxX(playerLocation.getBlockX());
            boundingBox.setMaxY(playerLocation.getBlockY());
            boundingBox.setMaxZ(playerLocation.getBlockZ());
        } else {
            boundingBox.setMinX(playerLocation.getBlockX());
            boundingBox.setMinY(playerLocation.getBlockY());
            boundingBox.setMinZ(playerLocation.getBlockZ());
        }

        boundingBox.normalize();

        return boundingBox;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("koth") || !sender.hasPermission("koth.completions")) return null;

        List<String> completions = Stream.of("list", "info", "schedule", "cancel", "stop", "create", "delete", "edit").collect(Collectors.toList());
        switch (args.length) {
            case 0:
                return completions;
            // Autocomplete using subcommands that start with the given string
            case 1:
                return completions.stream().filter(c -> c.startsWith(args[0].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
            default: {
                switch (args[0].toLowerCase(Locale.ROOT)) {
                    case "stop":
                    case "cancel":
                    case "create":
                    case "delete":
                    case "info":
                        return getAvailableGames(args[1]);
                    case "schedule":
                        switch (args.length) {
                            case 2:
                                return getAvailableGames(args[1]);
                            case 3:
                                return Collections.emptyList();
                            default: {
                                String timeArgument = args[2];
                                try {
                                    Integer.parseInt(timeArgument);
                                    return Collections.emptyList();
                                } catch (NumberFormatException e) {
                                    return Stream.of("-add").collect(Collectors.toList());
                                }
                            }
                        }
                    case "edit": {
                        switch (args.length) {
                            case 2:
                                return getAvailableGames(args[1]);
                            case 3:
                                return Stream.of("world", "rewards", "minimum_rewards", "maximum_rewards", "capture_zone", "global_zone", "duration").filter(c -> c.startsWith(args[2].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
                            default: {
                                switch (args[2].toLowerCase(Locale.ROOT)) {
                                    case "world":
                                        return Bukkit.getWorlds().stream().map(World::getName).filter(c -> c.startsWith(args[3].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
                                    case "capture_zone":
                                    case "global_zone":
                                        return Stream.of("maximum", "minimum").filter(c -> c.startsWith(args[3].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
                                }
                                break;
                            }
                        }
                    }
                    default:
                        return Collections.emptyList();
                }
            }
        }
    }

    private List<String> getAvailableGames(String typedGame) {
        return gameManager.getGames().stream().map(Game::getName).filter(c -> c.startsWith(typedGame.toLowerCase(Locale.ROOT))).collect(Collectors.toList());
    }
}
