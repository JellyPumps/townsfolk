package com.bouncyjelly.townsfolk.item;

import com.bouncyjelly.townsfolk.Townsfolk;
import com.bouncyjelly.townsfolk.entity.ModEntities;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final SpawnEggItem CITIZEN_SPAWN_EGG = Registry.register(
            Registries.ITEM,
            Identifier.of(Townsfolk.MOD_ID, "citizen_spawn_egg"),
            new SpawnEggItem(ModEntities.CITIZEN, 0x6B8E9F, 0xD9C8A9, new Item.Settings()));

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.add(CITIZEN_SPAWN_EGG);
        });
    }
}
