package com.bouncyjelly.townsfolk.citizen;

import com.bouncyjelly.townsfolk.schedule.Schedule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Citizen {
    public static final Codec<Citizen> CODEC = RecordCodecBuilder.create(i -> i.group(
            Uuids.CODEC.fieldOf("id").forGetter(Citizen::getId),
            Codec.STRING.fieldOf("name").forGetter(Citizen::getName),
            Codec.INT.optionalFieldOf("skin", 0).forGetter(Citizen::getSkinId),
            Codec.STRING.optionalFieldOf("home").forGetter(c -> Optional.ofNullable(c.home_id)),
            Codec.STRING.optionalFieldOf("work").forGetter(c -> Optional.ofNullable(c.work_id)),
            Codec.INT.optionalFieldOf("wallet", 0).forGetter(Citizen::getWallet),
            Codec.STRING.optionalFieldOf("age_group", "adult").forGetter(Citizen::getAgeGroup),
            Codec.STRING.optionalFieldOf("personality", "").forGetter(Citizen::getPersonality),
            Schedule.CODEC.optionalFieldOf("schedule_override").forGetter(c -> Optional.ofNullable(c.schedule_override)),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("settings", Map.of()).forGetter(Citizen::getSettings)
    ).apply(i, Citizen::new));

    private final UUID id;
    private String name;
    private int skin_id;
    @Nullable private String home_id;
    @Nullable private String work_id;
    private int wallet;
    private String age_group;
    private String personality;
    @Nullable private Schedule schedule_override;
    private final Map<String, String> settings = new HashMap<>();

    private Citizen(UUID id, String name, int skin_id,
                    Optional<String> home, Optional<String> work, int wallet, String age_group, String personality,
                    Optional<Schedule> schedule_override, Map<String, String> settings) {
        this.id = id;
        this.name = name;
        this.skin_id = skin_id;
        this.home_id = home.orElse(null);
        this.work_id = work.orElse(null);
        this.wallet = wallet;
        this.age_group = age_group;
        this.personality = personality;
        this.schedule_override = schedule_override.orElse(null);
        this.settings.putAll(settings);
    }

    public Citizen(String name) {
        this(UUID.randomUUID(), name, 0, Optional.empty(), Optional.empty(), 0, "adult", "", Optional.empty(), Map.of());
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getSkinId() { return skin_id; }
    @Nullable public String getHomeId() { return home_id; }
    @Nullable public String getWorkId() { return work_id; }
    void setHomeId(@Nullable String id) { this.home_id = id; }
    void setWorkId(@Nullable String id) { this.work_id = id; }
    public int getWallet() { return wallet; }
    public String getAgeGroup() { return age_group; }
    public String getPersonality() { return personality; }
    @Nullable public Schedule getScheduleOverride() { return schedule_override; }
    public Map<String, String> getSettings() { return Collections.unmodifiableMap(settings); }

    void applyConfig(CitizenConfigData d) {
        this.name = d.name();
        this.skin_id = d.skin();
        this.age_group = d.age_group();
        this.personality = d.personality();
        this.home_id = d.home_id().orElse(null);
        this.work_id = d.work_id().orElse(null);
        this.wallet = d.wallet();
        this.schedule_override = d.scheduleOverride().orElse(null);
        this.settings.clear();
        this.settings.putAll(d.settings());
    }

}
