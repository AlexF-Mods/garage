package io.github.alexf6.garage.network;

import io.github.alexf6.garage.GarageMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Cliente → servidor: el conductor pulsó la tecla de la radio (enciende, cambia pista o apaga). Sin datos. */
public record RadioPayload() implements CustomPacketPayload {

    public static final Type<RadioPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(GarageMod.MOD_ID, "radio"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RadioPayload> CODEC =
            StreamCodec.unit(new RadioPayload());

    @Override
    public Type<RadioPayload> type() {
        return TYPE;
    }
}
