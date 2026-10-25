package com.bouncyjelly.townsfolk.network;

import com.bouncyjelly.townsfolk.Townsfolk;
import com.bouncyjelly.townsfolk.citizen.CitizenConfigData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SaveConfigPayload(int entityId, CitizenConfigData data) implements CustomPayload {

    public static final CustomPayload.Id<SaveConfigPayload> ID =
            new CustomPayload.Id<>(Identifier.of(Townsfolk.MOD_ID, "save_config"));

    public static final PacketCodec<RegistryByteBuf, SaveConfigPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, SaveConfigPayload::entityId,
            PacketCodecs.codec(CitizenConfigData.CODEC), SaveConfigPayload::data,
            SaveConfigPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}
