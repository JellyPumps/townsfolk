package com.bouncyjelly.townsfolk.place;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;

public record Place(String id, PlaceType type, BlockPos pos, float yaw) {
    public static final Codec<Place> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("id").forGetter(Place::id),
            PlaceType.CODEC.fieldOf("type").forGetter(Place::type),
            BlockPos.CODEC.fieldOf("pos").forGetter(Place::pos),
            Codec.FLOAT.fieldOf("yaw").forGetter(Place::yaw)
    ).apply(i, Place::new));
}
