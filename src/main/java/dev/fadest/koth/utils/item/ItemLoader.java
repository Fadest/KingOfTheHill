package dev.fadest.koth.utils.item;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;

public class ItemLoader implements JsonDeserializer<ItemStack>, JsonSerializer<ItemStack> {

    private final static Gson GSON = new GsonBuilder().create();

    /**
     * Saves an {@link ItemStack} to a JSON file.
     * <p>
     * The item must be in the following format:
     * <pre>
     *     {
     *         "material": "STONE",
     *         "name": "Stone",
     *         "lore": [
     *             "This is a stone"
     *          ],
     *          "enchants": [
     *              {
     *                  "name": "unbreaking",
     *                  "level": 1
     *              }
     *          ],
     *          "flags": [
     *              "HIDE_ENCHANTS",
     *              "HIDE_ATTRIBUTES"
     *          ]
     *    }
     * </pre>
     *
     * @param writer    The writer to save the item
     * @param itemStack The item to save
     */
    public void write(JsonWriter writer, ItemStack itemStack) throws IOException {
        if (itemStack == null) {
            writer.nullValue();
            return;
        }

        writer.beginObject();

        writer.name("material");
        writer.value(itemStack.getType().name());

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta.hasLore()) {
            writer.name("lore").beginArray();
            for (String s : itemMeta.getLore()) {
                writer.value(s);
            }
            writer.endArray();
        }

        if (itemMeta.hasDisplayName()) {
            writer.name("name").value(itemMeta.getDisplayName());
        }

        if (itemMeta.spigot().isUnbreakable()) {
            writer.name("unbreakable").value(true);
        }

        if (!itemMeta.getItemFlags().isEmpty()) {
            writer.name("flags").beginArray();
            for (ItemFlag flag : itemMeta.getItemFlags()) {
                writer.value(flag.name());
            }
            writer.endArray();
        }

        writer.name("durability").value(itemStack.getDurability());
        writer.name("glow").value(!itemMeta.getEnchants().isEmpty());

        if (!itemMeta.getEnchants().isEmpty()) {
            writer.name("enchantments").beginArray();

            for (Map.Entry<Enchantment, Integer> entry : itemMeta.getEnchants().entrySet()) {
                writer.beginObject();
                writer.name("name").value(entry.getKey().getName());
                writer.name("level").value(entry.getValue());
                writer.endObject();
            }

            writer.endArray();
        }

        writer.endObject();
    }

    /**
     * Loads an {@link ItemStack} from a JSON file.
     * <p>
     * The item must be in the following format:
     * <pre>
     *     {
     *         "material": "STONE",
     *         "name": "Stone",
     *         "lore": [
     *             "This is a stone"
     *          ],
     *          "enchants": [
     *              {
     *                  "name": "unbreaking",
     *                  "level": 1
     *              }
     *          ],
     *          "flags": [
     *              "HIDE_ENCHANTS",
     *              "HIDE_ATTRIBUTES"
     *          ]
     *    }
     * </pre>
     *
     * @param reader The reader to read from
     * @return The loaded item.
     */
    public ItemStack read(JsonReader reader) throws IOException {
        final ItemBuilder.Builder builder = ItemBuilder.builder().material(Material.AIR);
        reader.beginObject();

        while (reader.hasNext()) {
            switch (reader.nextName().toLowerCase(Locale.ROOT)) {
                case "type":
                case "material":
                    builder.material(Material.valueOf(reader.nextString().toUpperCase()));
                    break;
                case "name":
                    builder.name(reader.nextString());
                    break;
                case "lore":
                case "description":
                    reader.beginArray();
                    while (reader.hasNext()) {
                        builder.lore(reader.nextString());
                    }
                    reader.endArray();
                    break;
                case "enchantments":
                case "enchants":
                    reader.beginArray();

                    while (reader.hasNext()) {
                        reader.beginObject();

                        while (reader.hasNext()) {
                            Enchantment enchantment = Enchantment.DURABILITY;
                            int level = 1;
                            switch (reader.nextName().toLowerCase(Locale.ROOT)) {
                                case "name":
                                    enchantment = Enchantment.getByName(reader.nextString().toUpperCase(Locale.ROOT));
                                    break;
                                case "level":
                                    level = reader.nextInt();
                                    break;
                                default:
                                    reader.skipValue();
                                    break;
                            }
                            builder.enchant(enchantment, level);
                        }
                        reader.endObject();
                    }
                    reader.endArray();
                    break;
                case "unbreakable":
                    builder.unbreakable(reader.nextBoolean());
                    break;
                case "allflags":
                case "hideflags":
                    builder.allFlags(reader.nextBoolean());
                    break;
                case "flag":
                case "item_flag":
                    builder.flag(ItemFlag.valueOf(reader.nextString().toUpperCase(Locale.ROOT)));
                    break;
                case "flags":
                    reader.beginArray();
                    while (reader.hasNext()) {
                        builder.flag(ItemFlag.valueOf(reader.nextString().toUpperCase(Locale.ROOT)));
                    }
                    reader.endArray();
                    break;
                case "durability":
                case "damage":
                    builder.durability(Integer.valueOf(reader.nextInt()).byteValue());
                    break;
                case "glowing":
                case "addglow":
                case "glow":
                    builder.glow(reader.nextBoolean());
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }

        reader.endObject();
        return builder.build().make();
    }

    @Override
    public ItemStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        Map<String, Object> map = GSON.fromJson(jsonElement, new TypeToken<Map<String, Object>>() {
        }.getType());
        return ItemStack.deserialize(map);
    }

    @Override
    public JsonElement serialize(ItemStack itemStack, Type type, JsonSerializationContext jsonSerializationContext) {
        return GSON.toJsonTree(itemStack.serialize());
    }
}