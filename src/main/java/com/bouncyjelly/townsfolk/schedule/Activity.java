package com.bouncyjelly.townsfolk.schedule;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum Activity implements StringIdentifiable {
    HOME(Slot.HOME),
    SLEEP(Slot.HOME),
    WORK(Slot.WORK),
    LEISURE(Slot.LEISURE);

    public enum Slot { HOME, WORK, LEISURE }
    public static final Codec<Activity> CODEC = StringIdentifiable.createCodec(Activity::values);
    private final Slot slot;

    Activity(Slot slot) { this.slot = slot; }
    public Slot slot() { return slot; }

    @Override
    public String asString() { return name().toLowerCase(Locale.ROOT); }
}
