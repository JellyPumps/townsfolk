package com.bouncyjelly.townsfolk.entity;

import com.bouncyjelly.townsfolk.Townsfolk;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<CitizenEntity> CITIZEN = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(Townsfolk.MOD_ID, "citizen"),
            EntityType.Builder.create(CitizenEntity::new, SpawnGroup.MISC)
                    .dimensions(0.6f, 1.8f)
                    .build("citizen"));

    public static void register() {
        FabricDefaultAttributeRegistry.register(CITIZEN, CitizenEntity.createCitizenAttributes());
    }
}
