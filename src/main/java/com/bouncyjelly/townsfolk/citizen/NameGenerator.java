package com.bouncyjelly.townsfolk.citizen;

import net.minecraft.util.math.random.Random;

import java.util.List;

public final class NameGenerator {

    private static final List<String> NAMES = List.of(
            "NPC", "Bob", "Sakura", "Ren", "Nero", "Ethan", "Eddie", "Mio",
            "Chris", "Leonard", "Nai", "Nus");

    private NameGenerator() {}

    public static String pick(CitizenManager manager, Random random) {
        for (int attempt = 0; attempt < 20; attempt++) {
            String name = NAMES.get(random.nextInt(NAMES.size()));
            if (manager.getByName(name) == null) return name;
        }

        int n = 2;
        String base = NAMES.get(random.nextInt(NAMES.size()));
        while (manager.getByName(base + n) != null) n++;
        return base + n;
    }
}