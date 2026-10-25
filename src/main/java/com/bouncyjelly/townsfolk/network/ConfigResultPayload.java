package com.bouncyjelly.townsfolk.network;

import com.bouncyjelly.townsfolk.Townsfolk;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ConfigResultPayload(boolean ok, String message) implements CustomPayload {

    public static final CustomPayload.Id<ConfigResultPayload> ID =
            new CustomPayload.Id<>(Identifier.of(Townsfolk.MOD_ID, "config_result"));

    public static final PacketCodec<RegistryByteBuf, ConfigResultPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, ConfigResultPayload::ok,
            PacketCodecs.string(256), ConfigResultPayload::message,
            ConfigResultPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}
