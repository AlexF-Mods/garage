package io.github.alexf6.garage;

import io.github.alexf6.garage.config.GarageConfig;
import io.github.alexf6.garage.network.HornPayload;
import io.github.alexf6.garage.network.RadioPayload;
import io.github.alexf6.garage.network.TuningSyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GarageMod implements ModInitializer {
    public static final String MOD_ID = "garage";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        GarageConfig.load(FabricLoader.getInstance().getConfigDir().resolve(GarageConfig.FILE_NAME));
        ModVehicles.register();

        PayloadTypeRegistry.clientboundPlay().register(TuningSyncPayload.TYPE, TuningSyncPayload.CODEC);
        // El manejo de los vehículos lo dicta el servidor: cada cliente lo recibe al entrar
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (ServerPlayNetworking.canSend(handler.getPlayer(), TuningSyncPayload.TYPE)) {
                ServerPlayNetworking.send(handler.getPlayer(), TuningSyncPayload.of(VehicleSpecs.ALL));
            }
        });
        registerDriverAction(HornPayload.TYPE, HornPayload.CODEC, (vehicle, player, payload) -> vehicle.honk(player));
        registerDriverAction(RadioPayload.TYPE, RadioPayload.CODEC,
                (vehicle, player, payload) -> vehicle.toggleRadio(player, payload.trackCount()));

        LOGGER.info("GarageMod inicializado correctamente.");
    }

    @FunctionalInterface
    private interface DriverAction<T extends CustomPacketPayload> {
        void run(MotorVehicleEntity vehicle, ServerPlayer player, T payload);
    }

    private static <T extends CustomPacketPayload> void registerDriverAction(
            CustomPacketPayload.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec,
            DriverAction<T> action) {
        PayloadTypeRegistry.serverboundPlay().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
            if (context.player().getVehicle() instanceof MotorVehicleEntity vehicle) {
                action.run(vehicle, context.player(), payload);
            }
        });
    }
}
