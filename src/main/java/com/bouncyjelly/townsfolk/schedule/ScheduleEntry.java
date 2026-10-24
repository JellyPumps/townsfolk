package com.bouncyjelly.townsfolk.schedule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ScheduleEntry(int start, int end, Activity activity) {
    public static final Codec<ScheduleEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(0, 24000).fieldOf("start").forGetter(ScheduleEntry::start),
            Codec.intRange(0, 24000).fieldOf("end").forGetter(ScheduleEntry::end),
            Activity.CODEC.fieldOf("activity").forGetter(ScheduleEntry::activity)
    ).apply(i, ScheduleEntry::new));

    public boolean contains(int tick_of_day) {
        return tick_of_day >= start && tick_of_day < end;
    }
}
