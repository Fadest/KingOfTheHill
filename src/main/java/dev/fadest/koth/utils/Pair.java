package dev.fadest.koth.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class Pair<K, V> implements Serializable {

    private final K key;
    private final V value;

    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }

    @Override
    public String toString() {
        return "{" + key.toString() + ", " + value.toString() + "}";
    }
}