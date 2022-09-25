package dev.fadest.koth.utils.item;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.Map;

public class ItemLoader implements JsonDeserializer<ItemStack>, JsonSerializer<ItemStack> {

    private final static Gson GSON = new GsonBuilder().create();

    @Override
    public ItemStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return ItemStack.deserialize(GSON.fromJson(jsonElement, new TypeToken<Map<String, Object>>() {
        }.getType()));
    }

    @Override
    public JsonElement serialize(ItemStack itemStack, Type type, JsonSerializationContext jsonSerializationContext) {
        return GSON.toJsonTree(itemStack.serialize());
    }
}