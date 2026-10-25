package com.bouncyjelly.townsfolk.citizen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.bouncyjelly.townsfolk.schedule.Schedule;

import java.util.Map;
import java.util.Optional;

public record CitizenConfigData(String name, int skin, String ageGroup, String personality,
                                Optional<String> home_id, Optional<String> work_id, int wallet,
                                Optional<Schedule> scheduleOverride, Map<String, String> settings) {

    public static final Codec<CitizenConfigData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("name").forGetter(CitizenConfigData::name),
            Codec.intRange(0, 63).fieldOf("skin").forGetter(CitizenConfigData::skin),
            Codec.STRING.fieldOf("age_group").forGetter(CitizenConfigData::ageGroup),
            Codec.STRING.fieldOf("personality").forGetter(CitizenConfigData::personality),
            Codec.STRING.optionalFieldOf("home").forGetter(CitizenConfigData::home_id),
            Codec.STRING.optionalFieldOf("work").forGetter(CitizenConfigData::work_id),
            Codec.intRange(0, 1_000_000).fieldOf("wallet").forGetter(CitizenConfigData::wallet),
            Schedule.CODEC.optionalFieldOf("schedule").forGetter(CitizenConfigData::scheduleOverride),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("settings").forGetter(CitizenConfigData::settings)
    ).apply(i, CitizenConfigData::new));

    public static CitizenConfigData from(Citizen c) {
        return new CitizenConfigData(c.getName(), c.getSkinId(), c.getAgeGroup(), c.getPersonality(),
                Optional.ofNullable(c.getHomeId()), Optional.ofNullable(c.getWorkId()), c.getWallet(),
                Optional.ofNullable(c.getScheduleOverride()), Map.copyOf(c.getSettings()));
    }
}