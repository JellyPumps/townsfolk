package com.bouncyjelly.townsfolk.schedule;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum Activity implements StringIdentifiable {
    HOME(Slot.HOME, 4),
    SLEEP(Slot.HOME, 0),
    WORK(Slot.WORK, 2),
    LEISURE(Slot.LEISURE, 30);

    public enum Slot { HOME, WORK, LEISURE }
    public static final Codec<Activity> CODEC = StringIdentifiable.createCodec(Activity::values);
    private final Slot slot;
    private final int wander_radius;

    Activity(Slot slot, int wander_radius) {
        this.slot = slot;
        this.wander_radius = wander_radius;
    }
    public Slot slot() { return slot; }
    public int wanderRadius() { return wander_radius; }

    @Override
    public String asString() { return name().toLowerCase(Locale.ROOT); }
}
