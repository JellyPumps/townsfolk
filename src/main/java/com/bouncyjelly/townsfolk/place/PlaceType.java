package com.bouncyjelly.townsfolk.place;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;
import java.util.Optional;

public enum PlaceType implements StringIdentifiable {
    HOME,
    WORKPLACE,
    SHOP_COUNTER,
    STATION_STOP,
    CROSSING,
    LEISURE;

    public static final Codec<PlaceType> CODEC = StringIdentifiable.createCodec(PlaceType::values);

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<PlaceType> byName(String s) {
        for (PlaceType t : values()) {
            if (t.asString().equalsIgnoreCase(s)) return Optional.of(t);
        }
        return Optional.empty();
    }
}
