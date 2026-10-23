package com.bouncyjelly.townsfolk.citizen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class Citizen {
    public static final Codec<Citizen> CODEC = RecordCodecBuilder.create(i -> i.group(
            Uuids.CODEC.fieldOf("id").forGetter(Citizen::getId),
            Codec.STRING.fieldOf("name").forGetter(Citizen::getName),
            Codec.INT.optionalFieldOf("skin", 0).forGetter(Citizen::getSkinId),
            Codec.STRING.optionalFieldOf("home").forGetter(c -> Optional.ofNullable(c.home_id)),
            Codec.STRING.optionalFieldOf("work").forGetter(c -> Optional.ofNullable(c.work_id)),
            Codec.INT.optionalFieldOf("wallet", 0).forGetter(Citizen::getWallet)
    ).apply(i, Citizen::new));

    private final UUID id;
    private String name;
    private int skin_id;
    @Nullable private String home_id;
    @Nullable private String work_id;
    private int wallet;

    private Citizen(UUID id, String name, int skin_id,
                    Optional<String> home, Optional<String> work, int wallet) {
        this.id = id;
        this.name = name;
        this.skin_id = skin_id;
        this.home_id = home.orElse(null);
        this.work_id = work.orElse(null);
        this.wallet = wallet;
    }

    public Citizen(String name) {
        this(UUID.randomUUID(), name, 0, Optional.empty(), Optional.empty(), 0);
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

}
