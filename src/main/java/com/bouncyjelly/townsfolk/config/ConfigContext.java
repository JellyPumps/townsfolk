package com.bouncyjelly.townsfolk.config;

import com.bouncyjelly.townsfolk.schedule.Schedule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record ConfigContext(List<String> homeIds, List<String> workIds, String status, Schedule defaultSchedule) {
    public static final Codec<ConfigContext> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.listOf().fieldOf("homes").forGetter(ConfigContext::homeIds),
            Codec.STRING.listOf().fieldOf("works").forGetter(ConfigContext::workIds),
            Codec.STRING.fieldOf("status").forGetter(ConfigContext::status),
            Schedule.CODEC.fieldOf("default_schedule").forGetter(ConfigContext::defaultSchedule)
    ).apply(i, ConfigContext::new));
}