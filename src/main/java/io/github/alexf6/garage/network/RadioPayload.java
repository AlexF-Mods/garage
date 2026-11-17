package io.github.alexf6.garage.network;

import io.github.alexf6.garage.GarageMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Cliente → servidor: el conductor pulsó la tecla de la radio (enciende, cambia pista o apaga).
 * Solo lleva cuántas canciones tiene en su carpeta; el servidor lo acota y decide la pista.
 */
public record RadioPayload(int trackCount) implements CustomPacketPayload {

    public static final Type<RadioPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(GarageMod.MOD_ID, "radio"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RadioPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, RadioPayload::trackCount, RadioPayload::new);

    @Override
    public Type<RadioPayload> type() {
        return TYPE;
    }
}
