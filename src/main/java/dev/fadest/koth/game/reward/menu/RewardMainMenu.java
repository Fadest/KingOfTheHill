package dev.fadest.koth.game.reward.menu;

import dev.fadest.koth.KOTHPlugin;
import dev.fadest.koth.game.Game;
import dev.fadest.koth.game.loader.GameFactory;
import dev.fadest.koth.game.reward.Reward;
import dev.fadest.koth.game.reward.RewardType;
import dev.fadest.koth.menu.Menu;
import dev.fadest.koth.utils.StringUtilities;
import dev.fadest.koth.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class RewardMainMenu extends Menu {

    private final static ItemStack addRewardItem = ItemBuilder.builder().name("&cAdd Reward").lore("&7Click to add a new reward.").material(Material.GOLD_INGOT).build().make();
    private final static ItemStack listRewardsItem = ItemBuilder.builder().name("&cList Rewards").lore("&7Click to list all rewards.").material(Material.SIGN).build().make();

    private final Game game;

    public RewardMainMenu(Game game) {
        super(9, "&eEdit Rewards");

        this.game = game;
        getInventory().setItem(3, addRewardItem);
        getInventory().setItem(5, listRewardsItem);
    }

    @Override
    public void click(@NotNull Player player, int slot, @NotNull ClickType clickType) {
        if (slot == 3) {
            openAddRewardConversation(player);
        } else if (slot == 5) {
            new RewardListMenu(game, 1).open(player);
        }
    }

    private void openAddRewardConversation(Player player) {
        player.closeInventory();
        player.beginConversation(new ConversationFactory(KOTHPlugin.getInstance())
                .withModality(false)
                .withLocalEcho(false)
                .withEscapeSequence("CANCEL")
                .withTimeout(10)
                .withFirstPrompt(new ValidatingPrompt() {
                    @Override
                    protected boolean isInputValid(@NotNull ConversationContext conversationContext, @NotNull String input) {
                        return input.equalsIgnoreCase("command") || input.equalsIgnoreCase("item");
                    }

                    @Override
                    protected Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String input) {
                        conversationContext.setSessionData("Type", input.trim());
                        return Prompt.END_OF_CONVERSATION;
                    }

                    @Override
                    @NotNull
                    public String getPromptText(@NotNull ConversationContext conversationContext) {
                        return StringUtilities.color(
                                "&eInput if the type of the reward will be either command or item\n&c&lOR &r&7Type &cCANCEL &7to cancel or wait &c10 &7seconds");
                    }
                }).addConversationAbandonedListener(conversationEvent -> {
                    if (!conversationEvent.gracefulExit()) return;
                    final String string = (String) conversationEvent.getContext().getSessionData("Type");

                    if (string.equalsIgnoreCase("command")) {
                        openCommandConversation(player, Reward.builder().rewardType(RewardType.COMMAND));
                    } else {
                        openItemConversation(player, Reward.builder().rewardType(RewardType.ITEM));
                    }
                }).buildConversation(player));
    }

    private void openCommandConversation(Player player, Reward.Builder builder) {
        player.beginConversation(new ConversationFactory(KOTHPlugin.getInstance())
                .withModality(false)
                .withLocalEcho(false)
                .withEscapeSequence("CANCEL")
                .withTimeout(15)
                .withFirstPrompt(new ValidatingPrompt() {
                    @Override
                    protected boolean isInputValid(@NotNull ConversationContext conversationContext, @NotNull String input) {
                        return true;
                    }

                    @Override
                    protected Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String input) {
                        conversationContext.setSessionData("Type", input.trim());
                        return Prompt.END_OF_CONVERSATION;
                    }

                    @Override
                    @NotNull
                    public String getPromptText(@NotNull ConversationContext conversationContext) {
                        return StringUtilities.color("&eInput the command that will be executed\n&c&lOR &r&7Type &cCANCEL &7to cancel or wait &c15 &7seconds");
                    }
                }).addConversationAbandonedListener(conversationEvent -> {
                    if (!conversationEvent.gracefulExit()) return;
                    final String string = (String) conversationEvent.getContext().getSessionData("Type");

                    builder.command(string);

                    startNumberConversation(player, "minimum reward amount", maxAmount -> {
                        builder.maxAmount(maxAmount);
                        startNumberConversation(player, "maximum reward amount", minAmount -> {
                            builder.minAmount(minAmount);
                            startChanceConversation(player, builder);
                        });
                    });
                }).buildConversation(player));
    }

    private void openItemConversation(Player player, Reward.Builder builder) {
        player.beginConversation(new ConversationFactory(KOTHPlugin.getInstance())
                .withModality(false)
                .withLocalEcho(false)
                .withEscapeSequence("CANCEL")
                .withTimeout(15)
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
                        return StringUtilities.color("&eSelect the new item in your hotbar, then type something in chat to confirm.\n&c&lOR &r&7Type &cCANCEL &7to cancel or wait &c10 &7seconds");
                    }
                }).addConversationAbandonedListener(conversationEvent -> {
                    if (!conversationEvent.gracefulExit() || player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) {
                        player.sendMessage(StringUtilities.color("&4✕ &c&lKOTH &r&7❙ &fYou didn't select a new item."));
                        return;
                    }
                    ItemStack itemStack = player.getItemInHand().clone();
                    itemStack.setAmount(1);

                    builder.itemStack(itemStack);

                    startNumberConversation(player, "minimum reward amount", minAmount -> {
                        builder.minAmount(minAmount);
                        startNumberConversation(player, "maximum reward amount", maxAmount -> {
                            builder.maxAmount(maxAmount);
                            startChanceConversation(player, builder);
                        });
                    });
                }).buildConversation(player));
    }

    private void startNumberConversation(Player player, String input, Consumer<Integer> runnable) {
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
                        return StringUtilities.color("&c" + invalidInput + " &f is not a number");
                    }

                    @Override
                    protected Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String input) {
                        conversationContext.setSessionData("Amount", input.trim());
                        return Prompt.END_OF_CONVERSATION;
                    }

                    @Override
                    @NotNull
                    public String getPromptText(@NotNull ConversationContext conversationContext) {
                        return StringUtilities.color("&eInput the " + input + "\n&c&lOR &r&7Type &cCANCEL &7to cancel or wait &c10 &7seconds");
                    }
                }).addConversationAbandonedListener(conversationEvent -> {
                    if (!conversationEvent.gracefulExit()) return;
                    final int amount = Integer.parseInt((String) conversationEvent.getContext().getSessionData("Amount"));

                    runnable.accept(amount);
                }).buildConversation(player));
    }

    private void startChanceConversation(Player player, Reward.Builder builder) {
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
                        return StringUtilities.color("&c" + invalidInput + " &f is not a number");
                    }

                    @Override
                    protected Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String input) {
                        conversationContext.setSessionData("Amount", input.trim());
                        return Prompt.END_OF_CONVERSATION;
                    }

                    @Override
                    @NotNull
                    public String getPromptText(@NotNull ConversationContext conversationContext) {
                        return StringUtilities.color("&eInput the chance\n&c&lOR &r&7Type &cCANCEL &7to cancel or wait &c10 &7seconds");
                    }
                }).addConversationAbandonedListener(conversationEvent -> {
                    if (!conversationEvent.gracefulExit()) return;
                    final float amount = Float.parseFloat((String) conversationEvent.getContext().getSessionData("Amount"));

                    builder.chance(amount);
                    game.getRewards().add(builder.build());

                    player.sendMessage(StringUtilities.color("&2✔ &a&lKOTH &r&7❙ &fYou have created a new reward."));

                    GameFactory.saveFile(game);
                }).buildConversation(player));
    }

}
