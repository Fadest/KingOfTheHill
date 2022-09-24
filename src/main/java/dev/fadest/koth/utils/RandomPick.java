package dev.fadest.koth.utils;

import lombok.Getter;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A class that picks a random item that has been added to it based on a weight.
 * The weight is the chance of being picked.
 *
 * @param <E> the type of the item to pick
 */
public class RandomPick<E> {
    @Getter
    private final NavigableMap<Double, E> map;
    private final Random random;
    private double total;

    public RandomPick() {
        this.map = new TreeMap<>();
        this.total = 0.0;
        this.random = ThreadLocalRandom.current();
    }

    /**
     * Adds a value to the random pick.
     *
     * @param weight The weight of the value.
     * @param value  The value to add.
     */
    public void add(double weight, E value) {
        if (weight <= 0.0) {
            return;
        }
        this.total += weight;
        this.map.put(this.total, value);
    }

    /**
     * Calculates the next random result.
     *
     * @return The next random result.
     */
    public E next() {
        double value = this.random.nextDouble() * this.total;
        return this.map.ceilingEntry(value).getValue();
    }
}