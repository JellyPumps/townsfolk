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
            BlockPos.CODEC.optionalFieldOf("home").forGetter(c -> Optional.ofNullable(c.home)),
            BlockPos.CODEC.optionalFieldOf("work").forGetter(c -> Optional.ofNullable(c.work)),
            Codec.INT.optionalFieldOf("wallet", 0).forGetter(Citizen::getWallet)
    ).apply(i, Citizen::new));

    private final UUID id;
    private String name;
    private int skin_id;
    @Nullable private BlockPos home;
    @Nullable
    private BlockPos work;
    private int wallet;

    private Citizen(UUID id, String name, int skin_id,
                    Optional<BlockPos> home, Optional<BlockPos> work, int wallet) {
        this.id = id;
        this.name = name;
        this.skin_id = skin_id;
        this.home = home.orElse(null);
        this.work = work.orElse(null);
        this.wallet = wallet;
    }

    public Citizen(String name) {
        this(UUID.randomUUID(), name, 0, Optional.empty(), Optional.empty(), 0);
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getSkinId() { return skin_id; }
    @Nullable public BlockPos getHome() { return home; }
    @Nullable public BlockPos getWork() { return work; }
    public int getWallet() { return wallet; }

}
