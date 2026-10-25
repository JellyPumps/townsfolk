package com.bouncyjelly.townsfolk.network;

import com.bouncyjelly.townsfolk.Townsfolk;
import com.bouncyjelly.townsfolk.citizen.CitizenConfigData;
import com.bouncyjelly.townsfolk.config.ConfigContext;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OpenConfigPayload(int entityId, CitizenConfigData data, ConfigContext context)
        implements CustomPayload {

    public static final CustomPayload.Id<OpenConfigPayload> ID =
            new CustomPayload.Id<>(Identifier.of(Townsfolk.MOD_ID, "open_config"));

    public static final PacketCodec<RegistryByteBuf, OpenConfigPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, OpenConfigPayload::entityId,
            PacketCodecs.codec(CitizenConfigData.CODEC), OpenConfigPayload::data,
            PacketCodecs.codec(ConfigContext.CODEC), OpenConfigPayload::context,
            OpenConfigPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}