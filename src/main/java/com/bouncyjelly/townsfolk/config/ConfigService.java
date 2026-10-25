package com.bouncyjelly.townsfolk.config;

import com.bouncyjelly.townsfolk.citizen.*;
import com.bouncyjelly.townsfolk.entity.CitizenEntity;
import com.bouncyjelly.townsfolk.place.*;
import com.bouncyjelly.townsfolk.schedule.Schedule;
import com.bouncyjelly.townsfolk.schedule.ScheduleLoader;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class ConfigService {

    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_-]{1,24}");
    private static final Pattern TEXT = Pattern.compile("[A-Za-z0-9 _'-]{1,24}");

    private ConfigService() {}

    public static ConfigContext buildContext(ServerWorld world, CitizenEntity entity) {
        PlaceRegistry places = PlaceRegistry.get(world);
        List<String> homes = places.getByType(PlaceType.HOME).stream().map(Place::id).toList();
        List<String> works = Stream.concat(
                places.getByType(PlaceType.WORKPLACE).stream(),
                places.getByType(PlaceType.SHOP_COUNTER).stream()).map(Place::id).toList();
        return new ConfigContext(homes, works, entity.describeState(), ScheduleLoader.current());
    }

    public static Optional<String> validate(ServerWorld world, Citizen citizen, CitizenConfigData d) {
        CitizenManager citizens = CitizenManager.get(world);
        PlaceRegistry places = PlaceRegistry.get(world);

        if (!NAME.matcher(d.name()).matches())
            return Optional.of("Names can use letters, digits, '_' and '-' (1 to 24 characters).");
        Citizen other = citizens.getByName(d.name());
        if (other != null && other != citizen)
            return Optional.of("A citizen called '" + d.name() + "' already exists.");

        if (!ConfigOptions.AGE_GROUPS.contains(d.ageGroup())) return Optional.of("Unknown age group.");
        if (!d.personality().isEmpty() && !ConfigOptions.PERSONALITIES.contains(d.personality()))
            return Optional.of("Unknown personality.");

        if (d.home_id().isPresent()) {
            Place p = places.get(d.home_id().get());
            if (p == null || p.type() != PlaceType.HOME) return Optional.of("Home must be a registered home place.");
        }
        if (d.work_id().isPresent()) {
            Place p = places.get(d.work_id().get());
            if (p == null || (p.type() != PlaceType.WORKPLACE && p.type() != PlaceType.SHOP_COUNTER))
                return Optional.of("Work must be a registered workplace or shop counter.");
        }

        if (d.scheduleOverride().isPresent()) {
            Schedule s = d.scheduleOverride().get();
            if (s.entries().isEmpty() || s.entries().size() > ConfigOptions.MAX_SCHEDULE_ENTRIES)
                return Optional.of("Schedule needs 1 to " + ConfigOptions.MAX_SCHEDULE_ENTRIES + " entries.");
            List<String> problems = s.problems();
            if (!problems.isEmpty()) return Optional.of("Schedule: " + problems.get(0));
        }

        for (Map.Entry<String, String> e : d.settings().entrySet()) {
            ConfigOptions.PlannedField f = ConfigOptions.planned(e.getKey());
            if (f == null) return Optional.of("Unknown setting: " + e.getKey());
            boolean ok = f.isChoice() ? f.options().contains(e.getValue()) : TEXT.matcher(e.getValue()).matches();
            if (!ok) return Optional.of("Invalid value for " + f.label() + ".");
        }
        return Optional.empty();
    }

    public static void apply(ServerWorld world, Citizen citizen, CitizenEntity entity, CitizenConfigData d) {
        CitizenManager.get(world).applyConfig(citizen, d);
        entity.setCustomName(Text.literal(citizen.getName()));
    }
}