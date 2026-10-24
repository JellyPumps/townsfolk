package com.bouncyjelly.townsfolk.schedule;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.bouncyjelly.townsfolk.Townsfolk;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Optional;

public class ScheduleLoader implements SimpleSynchronousResourceReloadListener {

    private static final Identifier FILE = Identifier.of(Townsfolk.MOD_ID, "schedules/default.json");
    private static volatile Schedule current = Schedule.FALLBACK;

    public static Schedule current() { return current; }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(Townsfolk.MOD_ID, "schedules");
    }

    @Override
    public void reload(ResourceManager manager) {
        Optional<Resource> resource = manager.getResource(FILE);
        if (resource.isEmpty()) {
            Townsfolk.LOGGER.warn("{} not found, using built-in schedule", FILE);
            current = Schedule.FALLBACK;
            return;
        }
        try (Reader reader = resource.get().getReader()) {
            JsonElement json = JsonParser.parseReader(reader);
            Optional<Schedule> parsed = Schedule.CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(err -> Townsfolk.LOGGER.error("Bad schedule file: {}", err));
            if (parsed.isEmpty()) {
                current = Schedule.FALLBACK;
                return;
            }
            List<String> problems = parsed.get().problems();
            if (!problems.isEmpty()) {
                Townsfolk.LOGGER.error("Schedule rejected: {}", problems);
                current = Schedule.FALLBACK;
                return;
            }
            current = parsed.get();
            Townsfolk.LOGGER.info("Loaded schedule with {} entries", current.entries().size());
        } catch (IOException | RuntimeException e) {
            Townsfolk.LOGGER.error("Could not read {}", FILE, e);
            current = Schedule.FALLBACK;
        }
    }
}