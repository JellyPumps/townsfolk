package com.bouncyjelly.townsfolk.citizen;

import com.mojang.serialization.Codec;
import com.bouncyjelly.townsfolk.Townsfolk;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CitizenManager extends PersistentState {

    private static final Codec<List<Citizen>> LIST_CODEC = Citizen.CODEC.listOf();
    private static final String KEY = "citizens";

    private static final PersistentState.Type<CitizenManager> TYPE =
            new PersistentState.Type<>(CitizenManager::new, CitizenManager::fromNbt, null);

    private final Map<UUID, Citizen> citizens = new LinkedHashMap<>();

    public static CitizenManager get(ServerWorld anyWorld) {
        ServerWorld overworld = anyWorld.getServer().getOverworld();
        return overworld.getPersistentStateManager()
                .getOrCreate(TYPE, Townsfolk.MOD_ID + "_citizens");
    }

    public Citizen create(String name) {
        Citizen c = new Citizen(name);
        citizens.put(c.getId(), c);
        markDirty();
        return c;
    }

    @Nullable
    public Citizen getById(UUID id) { return citizens.get(id); }

    public Collection<Citizen> getAll() { return Collections.unmodifiableCollection(citizens.values()); }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        LIST_CODEC.encodeStart(NbtOps.INSTANCE, new ArrayList<>(citizens.values()))
                .resultOrPartial(err -> Townsfolk.LOGGER.error("Failed to save citizens: {}", err))
                .ifPresent(list -> nbt.put(KEY, list));
        return nbt;
    }

    public static CitizenManager fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        CitizenManager manager = new CitizenManager();
        NbtElement list = nbt.get(KEY);
        if (list != null) {
            LIST_CODEC.parse(NbtOps.INSTANCE, list)
                    .resultOrPartial(err -> Townsfolk.LOGGER.error("Failed to load citizens: {}", err))
                    .ifPresent(loaded -> loaded.forEach(c -> manager.citizens.put(c.getId(), c)));
        }
        return manager;
    }

    @Nullable
    public Citizen getByName(String name) {
        for (Citizen c : citizens.values()) {
            if (c.getName().equalsIgnoreCase(name)) return c;
        }
        return null;
    }

    public void assignHome(Citizen c, @Nullable String place_id) {
        c.setHomeId(place_id);
        markDirty();
    }

    public void assignWork(Citizen c, @Nullable String place_id) {
        c.setWorkId(place_id);
        markDirty();
    }

    public int clearPlaceReferences(String place_id) {
        int count = 0;
        for (Citizen c : citizens.values()) {
            boolean changed = false;
            if (place_id.equals(c.getHomeId())) { c.setHomeId(null); changed = true; }
            if (place_id.equals(c.getWorkId())) { c.setWorkId(null); changed = true; }
            if (changed) count++;
        }
        if (count > 0) markDirty();
        return count;
    }
}