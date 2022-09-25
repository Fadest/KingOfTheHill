package dev.fadest.koth.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class Pair<K, V> implements Serializable {

    private final K key;
    private final V value;

    /**
     * Creates a new Pair using the provided Key and Value
     *
     * @param key   the key of the Pair
     * @param value the value of the pair
     * @return The created Pair
     */
    public static <K, V> Pair<K, V> of(@NotNull K key, @NotNull V value) {
        return new Pair<>(key, value);
    }

    @Override
    public String toString() {
        return "{" + key.toString() + ", " + value.toString() + "}";
    }
}