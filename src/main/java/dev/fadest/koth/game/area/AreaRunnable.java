package dev.fadest.koth.game.area;

import dev.fadest.koth.game.Game;
import dev.fadest.koth.game.state.State;
import dev.fadest.koth.utils.BoundingBox;
import dev.fadest.koth.utils.StringUtilities;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.stream.Collectors;

public class AreaRunnable extends BukkitRunnable {

    private final Game game;
    private final World world;
    private final BoundingBox captureZoneBoundingBox, globalBoundingBox;

    public AreaRunnable(Game game) {
        this.game = game;
        this.world = game.getWorld();
        this.captureZoneBoundingBox = game.getCaptureZoneBoundingBox();
        this.globalBoundingBox = game.getGlobalBoundingBox();
    }

    @Override
    public void run() {
        if (game.getState() == State.ENDING) {
            game.setAreaRunnable(null);
            this.cancel();
            return;
        }

        final String timeFormat = StringUtilities.formatTimeMinutesAndSeconds(game.getGameRunnable().getCountdown().get());
        for (Player player : world.getPlayers().stream().filter(player -> globalBoundingBox.contains(player.getLocation())).collect(Collectors.toList())) {
            PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(StringUtilities.color("&fTime Remaining: &c" + timeFormat
                    + " &r&f❙ &7(&c" + this.game.getPlayerPoints().getOrDefault(player.getUniqueId(), 0L) + " &7points)")), (byte) 2);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }

        List<Player> players = world.getPlayers().stream().filter(player -> captureZoneBoundingBox.contains(player.getLocation())).collect(Collectors.toList());
        if (players.size() != 1) return; // We only want 1 player in the capture zone

        handlePlayerCaptureTick(players.get(0));
    }

    private void handlePlayerCaptureTick(Player player) {
        this.game.getPlayerPoints().compute(player.getUniqueId(), (uuid, oldPoints) -> {
            long newPoints = 0L;
            if (oldPoints != null) {
                newPoints = oldPoints;
            }

            return newPoints + 1;
        });

        player.playSound(player.getLocation(), Sound.CLICK, 0.3f, 1f);
    }


}
