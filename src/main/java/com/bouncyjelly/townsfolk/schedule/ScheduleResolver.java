package com.bouncyjelly.townsfolk.schedule;

import com.bouncyjelly.townsfolk.citizen.Citizen;
import com.bouncyjelly.townsfolk.place.Place;
import com.bouncyjelly.townsfolk.place.PlaceRegistry;
import com.bouncyjelly.townsfolk.place.PlaceType;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ScheduleResolver {

    private ScheduleResolver() {}

    public record Destination(Activity activity, @Nullable Place place) {}

    /** @return null if the schedule has a gap right now */
    @Nullable
    public static Destination resolve(ServerWorld world, Citizen citizen) {
        Schedule schedule = citizen.getScheduleOverride() != null
                ? citizen.getScheduleOverride()
                : ScheduleLoader.current();
        ScheduleEntry entry = schedule.at(world.getTimeOfDay());
        if (entry == null) return null;
        if (entry == null) return null;

        PlaceRegistry places = PlaceRegistry.get(world);
        Activity.Slot slot = entry.activity().slot();

        Place place = switch (slot) {
            case HOME -> lookup(places, citizen.getHomeId());
            case WORK -> lookup(places, citizen.getWorkId());
            case LEISURE -> pickLeisure(places, citizen);
        };
        // No work or leisure place available: fall back to home.
        if (place == null && slot != Activity.Slot.HOME) {
            place = lookup(places, citizen.getHomeId());
        }
        return new Destination(entry.activity(), place);
    }

    @Nullable
    private static Place lookup(PlaceRegistry places, @Nullable String id) {
        return id == null ? null : places.get(id);
    }

    @Nullable
    private static Place pickLeisure(PlaceRegistry places, Citizen citizen) {
        List<Place> options = places.getByType(PlaceType.LEISURE);
        if (options.isEmpty()) return null;
        return options.get(Math.floorMod(citizen.getId().hashCode(), options.size()));
    }
}