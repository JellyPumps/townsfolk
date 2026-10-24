package com.bouncyjelly.townsfolk.schedule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record Schedule(List<ScheduleEntry> entries) {

    public static final Codec<Schedule> CODEC = RecordCodecBuilder.create(i -> i.group(
            ScheduleEntry.CODEC.listOf().fieldOf("entries").forGetter(Schedule::entries)
    ).apply(i, Schedule::new));

    /** Used when the JSON file is missing or invalid. */
    public static final Schedule FALLBACK = new Schedule(List.of(
            new ScheduleEntry(0, 1000, Activity.HOME),
            new ScheduleEntry(1000, 9000, Activity.WORK),
            new ScheduleEntry(9000, 12000, Activity.LEISURE),
            new ScheduleEntry(12000, 14000, Activity.HOME),
            new ScheduleEntry(14000, 24000, Activity.SLEEP)));

    /** @return the entry covering this time, or null if there is a gap */
    @Nullable
    public ScheduleEntry at(long timeOfDay) {
        int t = (int) Math.floorMod(timeOfDay, 24000L);
        for (ScheduleEntry e : entries) {
            if (e.contains(t)) return e;
        }
        return null;
    }

    public List<String> problems() {
        List<String> out = new ArrayList<>();
        for (int a = 0; a < entries.size(); a++) {
            ScheduleEntry x = entries.get(a);
            if (x.start() >= x.end()) out.add("entry " + a + " has start >= end");
            for (int b = a + 1; b < entries.size(); b++) {
                ScheduleEntry y = entries.get(b);
                if (x.start() < y.end() && y.start() < x.end()) {
                    out.add("entries " + a + " and " + b + " overlap");
                }
            }
        }
        return out;
    }
}