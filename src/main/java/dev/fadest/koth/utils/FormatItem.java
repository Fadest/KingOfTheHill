package dev.fadest.koth.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

public class FormatItem {
    protected ItemStack item;

    /**
     * Creates instance of FormatItem from an ItemStack
     *
     * @param item ItemStack you want to get the format of.
     */
    public FormatItem(ItemStack item) {
        this.item = item;
    }

    /**
     * Creates instance of FormatItem from a Material
     *
     * @param material Material you want to get the format of.
     */
    public FormatItem(Material material) {
        this(new ItemStack(material));
    }

    /**
     * Creates instance of FormatItem from a display name.
     *
     * @param displayName String you want to get the format of.
     */
    public FormatItem(String displayName) {
        try {
            Material material = Material.valueOf(displayName.toUpperCase().replace(" ", "_"));
            this.item = new ItemStack(material);
        } catch (Exception ex) {
            this.item = new ItemStack(Material.AIR);
        }
    }

    public static String format(Material material) {
        return new FormatItem(material).format();
    }

    public static String format(ItemStack item) {
        return new FormatItem(item).format();
    }

    /**
     * Returns the ItemStack value of the formatter.
     *
     * @return ItemStack
     */
    public ItemStack getItem() {
        return this.item;
    }

    /**
     * Get the format of the item's name.
     *
     * @return Format of the FormatItem instance
     */
    private String format() {
        if (this.item == null)
            return null;

        //Make sure the item doesn't have a custom display name set
        final ItemMeta im = this.item.getItemMeta();
        if (im != null && im.hasDisplayName() && !im.getDisplayName().equals(""))
            return im.getDisplayName();

        //Grab the material we're using for the name
        final Material material = this.item.getType();

        //Format to proper label: "Stone Bricks" instead of STONE_BRICKS
        StringBuilder builder = new StringBuilder();
        for (String word : material.toString().split("_"))
            builder.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase(Locale.ROOT)).append(" ");

        //Trim the string, and also return it
        return builder.toString().trim();
    }

    /**
     * Grabs the display name of the ItemFormat instance.
     *
     * @return String of the display name.
     */
    public String getName() {
        String format = this.format();
        if (format != null) return format;
        return "";
    }


}