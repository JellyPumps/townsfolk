package com.bouncyjelly.townsfolk.place;

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

public class PlaceRegistry extends PersistentState {

    private static final Codec<List<Place>> LIST_CODEC = Place.CODEC.listOf();
    private static final String KEY = "places";

    private static final PersistentState.Type<PlaceRegistry> TYPE =
            new PersistentState.Type<>(PlaceRegistry::new, PlaceRegistry::fromNbt, null);

    private final Map<String, Place> places = new LinkedHashMap<>();

    public static PlaceRegistry get(ServerWorld anyWorld) {
        ServerWorld overworld = anyWorld.getServer().getOverworld();
        return overworld.getPersistentStateManager()
                .getOrCreate(TYPE, Townsfolk.MOD_ID + "_places");
    }

    /** @return false if the id is already taken */
    public boolean add(Place place) {
        if (places.containsKey(place.id())) return false;
        places.put(place.id(), place);
        markDirty();
        return true;
    }

    public boolean remove(String id) {
        boolean removed = places.remove(id) != null;
        if (removed) markDirty();
        return removed;
    }

    @Nullable
    public Place get(String id) { return places.get(id); }

    public Collection<Place> getAll() { return Collections.unmodifiableCollection(places.values()); }

    public List<Place> getByType(PlaceType type) {
        return places.values().stream().filter(p -> p.type() == type).toList();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        LIST_CODEC.encodeStart(NbtOps.INSTANCE, new ArrayList<>(places.values()))
                .resultOrPartial(err -> Townsfolk.LOGGER.error("Failed to save places: {}", err))
                .ifPresent(list -> nbt.put(KEY, list));
        return nbt;
    }

    public static PlaceRegistry fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        PlaceRegistry registry = new PlaceRegistry();
        NbtElement list = nbt.get(KEY);
        if (list != null) {
            LIST_CODEC.parse(NbtOps.INSTANCE, list)
                    .resultOrPartial(err -> Townsfolk.LOGGER.error("Failed to load places: {}", err))
                    .ifPresent(loaded -> loaded.forEach(p -> registry.places.put(p.id(), p)));
        }
        return registry;
    }
}