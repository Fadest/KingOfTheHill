package dev.fadest.koth.game.reward.menu;

import dev.fadest.koth.KOTHPlugin;
import dev.fadest.koth.game.Game;
import dev.fadest.koth.game.loader.GameFactory;
import dev.fadest.koth.game.reward.Reward;
import dev.fadest.koth.game.reward.RewardType;
import dev.fadest.koth.menu.Menu;
import dev.fadest.koth.utils.FormatItem;
import dev.fadest.koth.utils.Utilities;
import dev.fadest.koth.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class RewardEditMenu extends Menu {

    private final Game game;
    private final Reward reward;

    public RewardEditMenu(Game game, Reward reward) {
        super(18, "&cEdit Reward");
        this.game = game;
        this.reward = reward;

        ItemStack minimumRewardsItem = ItemBuilder.builder().name("&cChange Minimum Amount").lore("&7Current amount: &f" + reward.getMinAmount()).lore(" ").lore("&7Click to change").material(Material.APPLE).build().make();
        ItemStack maximumRewardsItem = ItemBuilder.builder().name("&cChange Maximum Amount").lore("&7Current amount: &f" + reward.getMaxAmount()).lore(" ").lore("&7Click to change").material(Material.GOLDEN_APPLE).build().make();
        ItemStack currentChanceItem = ItemBuilder.builder().name("&cChange Chance").lore("&7Current chance: &f" + String.format("%.2f", reward.getChance()) + "%").lore(" ").lore("&7Click to change").material(Material.ANVIL).build().make();
        ItemStack messageItem = ItemBuilder.builder().name("&cChange message").lore("&7Current message: &f" + (reward.getMessage() == null ? "Nothing" : reward.getMessage())).lore(" ").lore("&7Click to change").material(Material.SIGN).build().make();

        getInventory().setItem(1, minimumRewardsItem);
        getInventory().setItem(3, maximumRewardsItem);
        getInventory().setItem(5, currentChanceItem);
        getInventory().setItem(7, messageItem);

        ItemStack currentItem;
        if (reward.getRewardType() == RewardType.ITEM) {
            ItemStack itemStack = reward.getItemStack();
            ItemMeta itemMeta = itemStack.getItemMeta();
            ItemBuilder.Builder builder = ItemBuilder.builder();
            if (itemMeta.hasLore()) {
                for (String s : itemMeta.getLore()) {
                    builder.lore(s);
                }
            }
            currentItem = builder.name(FormatItem.format(itemStack)).material(itemStack.getType()).lore(" ").lore("&eClick to change").build().make();
        } else {
            currentItem = ItemBuilder.builder().name("&cCommand").material(Material.PAPER).lore("&cCommand: &7" + reward.getCommand()).lore(" ").lore("&7Click to change").build().make();
        }

        getInventory().setItem(13, currentItem);
    }

    @Override
    public void click(Player player, int slot, ClickType clickType) {
        player.closeInventory();

        switch (slot) {
            case 1:
                startNumberConversation(player, (amount) -> {
                    reward.setMinAmount(amount);
                    player.sendMessage(Utilities.color("&2✔ &a&lKOTH &r&7❙ &fYou have changed the minimum reward amount to &a" + amount));
                });
                break;
            case 3:
                startNumberConversation(player, (amount) -> {
                    reward.setMaxAmount(amount);
                    player.sendMessage(Utilities.color("&2✔ &a&lKOTH &r&7❙ &fYou have changed the maximum reward amount to &a" + amount));
                });
                break;
            case 6:
                startFloatConversation(player, (chance) -> {
                    reward.setChance(chance);
                    player.sendMessage(Utilities.color("&2✔ &a&lKOTH &r&7❙ &fYou have changed the chances to &a" + chance));
                });
                break;
            case 7:
                startStringConversation(player, "message", (message) -> {
                    reward.setMessage(message);
                    player.sendMessage(Utilities.color("&2✔ &a&lKOTH &r&7❙ &fYou have changed the message to &f" + message));
                });
                break;
            case 13:
                if (reward.getRewardType() == RewardType.COMMAND) {
                    startStringConversation(player, "command", (string) -> {
                        reward.setCommand(string);
                        player.sendMessage(Utilities.color("&2✔ &a&lKOTH &r&7❙ &fYou have changed the command to &f/" + string));
                    });
                } else {
                    startEditItemConversation(player);
                }
                break;
        }

    }

    private void startNumberConversation(Player player, Consumer<Integer> runnable) {
        player.beginConversation(new ConversationFactory(KOTHPlugin.getInstance())
                .withModality(false)
                .withLocalEcho(false)
                .withEscapeSequence("CANCEL")
                .withTimeout(10)
                .withFirstPrompt(new ValidatingPrompt() {
                    @Override
                    protected boolean isInputValid(@NotNull ConversationContext conversationContext, @NotNull String input) {
                        try {
                            Integer.parseInt(input);
                            return true;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }

                    @Override
                    protected String getFailedValidationText(ConversationContext context, String invalidInput) {
                        return Utilities.color("&c" + invalidInput + " &f is not a number");
                    }

                    @Override
                    protected Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String input) {
                        conversationContext.setSessionData("Amount", input.trim());
                        return Prompt.END_OF_CONVERSATION;
                    }

                    @Override
                    @NotNull
                    public String getPromptText(@NotNull ConversationContext conversationContext) {
                        return Utilities.color("&eInput the new amount\n&c&lOR &r&7Type &cCANCEL &7to cancel or wait &c10 &7seconds");
                    }
                }).addConversationAbandonedListener(conversationEvent -> {
                    if (!conversationEvent.gracefulExit()) return;
                    final int amount = Integer.parseInt((String) conversationEvent.getContext().getSessionData("Amount"));

                    runnable.accept(amount);
                    GameFactory.saveFile(game);

                    new RewardEditMenu(game, reward).open(player);
                }).buildConversation(player));
    }

    private void startFloatConversation(Player player, Consumer<Float> runnable) {
        player.beginConversation(new ConversationFactory(KOTHPlugin.getInstance())
                .withModality(false)
                .withLocalEcho(false)
                .withEscapeSequence("CANCEL")
                .withTimeout(10)
                .withFirstPrompt(new ValidatingPrompt() {
                    @Override
                    protected boolean isInputValid(@NotNull ConversationContext conversationContext, @NotNull String input) {
                        try {
                            Float.parseFloat(input);
                            return true;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }

                    @Override
                    protected String getFailedValidationText(ConversationContext context, String invalidInput) {
                        return Utilities.color("&c" + invalidInput + " &f is not a number");
                    }

                    @Override
                    protected Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String input) {
                        conversationContext.setSessionData("Amount", input.trim());
                        return Prompt.END_OF_CONVERSATION;
                    }

                    @Override
                    @NotNull
                    public String getPromptText(@NotNull ConversationContext conversationContext) {
                        return Utilities.color("&eInput the new chance\n&c&lOR &r&7Type &cCANCEL &7to cancel or wait &c10 &7seconds");
                    }
                }).addConversationAbandonedListener(conversationEvent -> {
                    if (!conversationEvent.gracefulExit()) return;
                    final float amount = Float.parseFloat((String) conversationEvent.getContext().getSessionData("Amount"));

                    runnable.accept(amount);
                    GameFactory.saveFile(game);

                    new RewardEditMenu(game, reward).open(player);
                }).buildConversation(player));
    }

    private void startStringConversation(Player player, String promptText, Consumer<String> runnable) {
        player.beginConversation(new ConversationFactory(KOTHPlugin.getInstance())
                .withModality(false)
                .withLocalEcho(false)
                .withEscapeSequence("CANCEL")
                .withTimeout(10)
                .withFirstPrompt(new ValidatingPrompt() {
                    @Override
                    protected boolean isInputValid(@NotNull ConversationContext conversationContext, @NotNull String input) {
                        return true;
                    }

                    @Override
                    protected Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String input) {
                        conversationContext.setSessionData("String", input.trim().replace("/", ""));
                        return Prompt.END_OF_CONVERSATION;
                    }

                    @Override
                    @NotNull
                    public String getPromptText(@NotNull ConversationContext conversationContext) {
                        return Utilities.color("&eInput the new " + promptText + "\n&c&lOR &r&7Type &cCANCEL &7to cancel or wait &c10 &7seconds");
                    }
                }).addConversationAbandonedListener(conversationEvent -> {
                    if (!conversationEvent.gracefulExit()) return;

                    final String string = (String) conversationEvent.getContext().getSessionData("String");

                    runnable.accept(string);
                    GameFactory.saveFile(game);

                    new RewardEditMenu(game, reward).open(player);
                }).buildConversation(player));
    }

    private void startEditItemConversation(Player player) {
        player.beginConversation(new ConversationFactory(KOTHPlugin.getInstance())
                .withModality(false)
                .withLocalEcho(false)
                .withEscapeSequence("CANCEL")
                .withTimeout(10)
                .withFirstPrompt(new ValidatingPrompt() {
                    @Override
                    protected boolean isInputValid(@NotNull ConversationContext conversationContext, @NotNull String input) {
                        return true;
                    }

                    @Override
                    protected Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String input) {
                        return Prompt.END_OF_CONVERSATION;
                    }

                    @Override
                    @NotNull
                    public String getPromptText(@NotNull ConversationContext conversationContext) {
                        return Utilities.color("&eSelect the new item in your hotbar, then type something in chat to confirm.\n&c&lOR &r&7Type &cCANCEL &7to cancel or wait &c10 &7seconds");
                    }
                }).addConversationAbandonedListener(conversationEvent -> {
                    if (!conversationEvent.gracefulExit() || player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) {
                        player.sendMessage(Utilities.color("&4✕ &c&lKOTH &r&7❙ &fYou didn't select a new item."));
                        return;
                    }
                    ItemStack itemStack = player.getItemInHand().clone();
                    itemStack.setAmount(1);
                    reward.setItemStack(itemStack);

                    player.sendMessage(Utilities.color("&2✔ &a&lKOTH &r&7❙ &fYou have updated the reward item"));

                    GameFactory.saveFile(game);

                    new RewardEditMenu(game, reward).open(player);
                }).buildConversation(player));
    }
}
