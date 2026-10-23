package com.bouncyjelly.townsfolk;

import com.bouncyjelly.townsfolk.entity.ModEntities;
import com.bouncyjelly.townsfolk.item.ModItems;
import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Townsfolk implements ModInitializer {
	public static final String MOD_ID = "townsfolk";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModEntities.register();
		ModItems.register();

		LOGGER.info("Initialize");
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
