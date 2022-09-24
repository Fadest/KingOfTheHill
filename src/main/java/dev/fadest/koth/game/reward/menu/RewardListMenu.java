package dev.fadest.koth.game.reward.menu;

import dev.fadest.koth.game.Game;
import dev.fadest.koth.game.loader.GameFactory;
import dev.fadest.koth.game.reward.Reward;
import dev.fadest.koth.game.reward.RewardType;
import dev.fadest.koth.menu.Menu;
import dev.fadest.koth.utils.FormatItem;
import dev.fadest.koth.utils.Utilities;
import dev.fadest.koth.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class RewardListMenu extends Menu {

    private final static ItemStack nextPageButton = ItemBuilder.builder().name("&7Next Page").lore("&7Click to go to the next page.").material(Material.ARROW).build().make();
    private final static ItemStack previousPageButton = ItemBuilder.builder().name("&7Previous Page").lore("&7Click to go to the previous page.").material(Material.ARROW).build().make();
    private final Map<Integer, Reward> rewards = new HashMap<>();
    private final Game game;
    private final int page;

    public RewardListMenu(Game game, int page) {
        super(54, "&cRewards &7Page (&c" + page + "&7)");
        this.game = game;
        this.page = page;

        List<Reward> rewardList = new ArrayList<>(game.getRewards());

        if (page > 1) {
            rewardList.subList(0, (45 * (page - 1))).clear();
        }

        IntStream.rangeClosed(0, 44).forEachOrdered(slot -> {
            if (rewardList.isEmpty()) return;

            Reward reward = rewardList.remove(0);
            ItemBuilder.Builder itemBuilder = ItemBuilder.builder();
            if (reward.getRewardType() == RewardType.COMMAND) {
                itemBuilder.name("&cCommand").material(Material.PAPER).lore("&cCommand: &f" + reward.getCommand());
            } else {
                ItemStack itemStack = reward.getItemStack();
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta.hasLore()) {
                    for (String s : itemMeta.getLore()) {
                        itemBuilder.lore(s);
                    }
                }
                itemBuilder.name("&r&f" + FormatItem.format(itemStack)).material(itemStack.getType());
            }
            itemBuilder.lore(" ")
                    .lore(String.format("&cChances: &f%.2f", reward.getChance()) + "%")
                    .lore("&cMinimum Amount: &f" + reward.getMinAmount())
                    .lore("&cMaximum Amount: &f" + reward.getMaxAmount())
                    .lore("&cMessage: &f" + (reward.getMessage() == null ? "Nothing" : reward.getMessage()))
                    .lore(" ")
                    .lore("&7Left click to edit")
                    .lore("&7Right click to delete");

            getInventory().setItem(slot, itemBuilder.build().make());
            this.rewards.put(slot, reward);
        });

        if (page > 1) {
            getInventory().setItem(45, previousPageButton);
        }

        if (rewardList.size() > 45) {
            getInventory().setItem(53, nextPageButton);
        }
    }

    @Override
    public void click(Player player, int slot, ClickType clickType) {
        if (getInventory().getItem(slot) == null) return;

        switch (slot) {
            case 45:
                new RewardListMenu(game, page - 1).open(player);
                break;
            case 53:
                new RewardListMenu(game, page + 1).open(player);
                break;
            default: {
                Reward reward = rewards.get(slot);
                if (reward == null) return;
                if (clickType == ClickType.LEFT) {
                    new RewardEditMenu(game, reward).open(player);
                } else if (clickType == ClickType.RIGHT) {
                    game.getRewards().remove(reward);
                    player.sendMessage(Utilities.color("&2✔ &a&lKOTH &r&7❙ &fYou have deleted a reward"));
                    GameFactory.saveFile(game);
                }
            }
        }
    }
}
