package dev.fadest.koth.game.reward;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Builder(builderClassName = "Builder")
@Getter
@Setter
public class Reward {

    private final RewardType rewardType;
    private ItemStack itemStack;
    private String command, message;
    private int minAmount;
    private int maxAmount;
    private double chance;
}
