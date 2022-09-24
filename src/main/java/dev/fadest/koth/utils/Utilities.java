package dev.fadest.koth.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

import java.util.concurrent.TimeUnit;

@UtilityClass
public class Utilities {

    public String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }


    public String formatTimeMinutesAndSeconds(long totalSeconds) {
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    public String formatRemainingTime(final long millis) {
        final StringBuilder builder = new StringBuilder();

        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;
        days %= 7;

        if (seconds > 0) {
            builder.insert(0, seconds + " second" + (seconds > 1 ? "s" : ""));
        }

        if (minutes > 0) {
            if (builder.length() > 0) {
                builder.insert(0, ' ');
            }

            builder.insert(0, minutes + " minute" + (minutes > 1 ? "s" : ""));
        }

        if (hours > 0) {
            if (builder.length() > 0) {
                builder.insert(0, ' ');
            }

            builder.insert(0, hours + " hour" + (hours > 1 ? "s" : ""));
        }

        if (days > 0) {
            if (builder.length() > 0) {
                builder.insert(0, ' ');
            }

            builder.insert(0, days + " day" + (days > 1 ? "s" : ""));
        }

        if (weeks > 0) {
            if (builder.length() > 0) {
                builder.insert(0, ' ');
            }

            builder.insert(0, weeks + " week" + (weeks > 1 ? "s" : ""));
        }

        return builder.toString();
    }
}
