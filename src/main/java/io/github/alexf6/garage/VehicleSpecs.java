package io.github.alexf6.garage;

import io.github.alexf6.garage.config.GarageConfig;
import io.github.alexf6.garage.physics.VehicleTuning;
import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

/** Catálogo de vehículos del mod. Para añadir uno basta otra entrada aquí y en {@link #ALL}. */
public final class VehicleSpecs {

    public static final VehicleSpec CAR = VehicleSpec.builder("car")
            .size(2.4F, 2.5F)
            // Conductor y copiloto adelantados/elevados para despejar el marco superior y el volante; dos atrás
            .seats(new Vec3(0.40, 0.80, 0.12), new Vec3(-0.40, 0.80, 0.12),
                    new Vec3(0.44, 0.75, -0.75), new Vec3(-0.44, 0.75, -0.75))
            .color(0xC82127) // rojo Willys
            .chestRows(4) // jeep cafetero de carga: 36 casillas
            .wheelRadius(0.375F)
            .headTurnLimit(105.0F)
            .directTurnRate(1.2F)
            .enginePitch(0.5F, 1.4F)
            .hasRadio(true)
            .impactMultiplier(1.20F)
            .horn(20, SoundEvents.NOTE_BLOCK_PLING, 1.4F, 0.6F, 0.75F)
            .tuning(GarageConfig::carTuning)
            .build();

    public static final VehicleSpec MOTORCYCLE = VehicleSpec.builder("motorcycle")
            .size(0.85F, 1.25F)
            // Sillín de dos niveles: conductor (tope 1.05, z de +0.09 a -0.25) y pasajero (tope 1.12, hasta -0.59)
            .seats(new Vec3(0.0, 1.05, -0.08), new Vec3(0.0, 1.12, -0.42))
            .color(0x1846C7) // azul Yamaha Racing Blue
            .chestRows(1) // alforja de 9 casillas
            .wheelRadius(0.375F) // rueda de 20 px a escala 0.60x = 12 px reales = radio 0.375 bloques
            .headTurnLimit(85.0F)
            .directTurnRate(2.8F)
            .enginePitch(0.7F, 1.7F)
            .leanPerSteer(0.48F)
            .mobsCanBoard(false) // una moto no es para criaturas
            .impactMultiplier(0.50F)
            .horn(15, SoundEvents.NOTE_BLOCK_BELL, 1.2F, 1.4F)
            .tuning(() -> VehicleTuning.ENDURO)
            .build();

    public static final VehicleSpec MOTOCARRO = VehicleSpec.builder("motocarro")
            .size(1.50F, 2.15F)
            .seats(new Vec3(0.0, 0.62, 0.31), new Vec3(0.28, 0.75, -0.62), new Vec3(-0.28, 0.75, -0.62))
            .color(0xE8ECEF) // blanco glaciar
            .chestRows(2) // baúl de 18 casillas
            .wheelRadius(0.3125F) // rin de 10 px = radio 5 px (5/16)
            .headTurnLimit(105.0F)
            .directTurnRate(1.2F)
            .enginePitch(0.65F, 1.55F)
            .impactMultiplier(0.75F)
            .horn(20, SoundEvents.NOTE_BLOCK_BELL, 1.4F, 1.6F, 1.9F)
            .tuning(() -> VehicleTuning.MOTOCARRO)
            .build();

    public static final VehicleSpec MOTOCARGUERO = VehicleSpec.builder("motocarguero")
            .size(1.68F, 1.70F)
            .seats(new Vec3(0.0, 0.90, -0.17)) // sobre el sillín (tope a 0.907, centro en z -0.17)
            .color(0xE8ECEF) // blanco glaciar
            .chestRows(6) // platón de carga de 54 casillas (9x6)
            .wheelRadius(0.35F) // rin de carga de 0.35
            .headTurnLimit(85.0F)
            .directTurnRate(1.8F)
            .enginePitch(0.60F, 1.50F)
            .impactMultiplier(0.90F)
            .horn(20, SoundEvents.NOTE_BLOCK_BELL, 1.4F, 1.3F, 1.6F)
            .tuning(() -> VehicleTuning.MOTOCARGUERO)
            .build();

    public static final VehicleSpec CHIVA = VehicleSpec.builder("chiva")
            .size(3.6F, 3.75F)
            // Cabina elevada (conductor + copiloto) centrada hacia el parabrisas para despejar pilar y capó, y 3 bancas
            .seats(new Vec3(0.48, 1.36, 1.30), new Vec3(-0.48, 1.36, 1.30),
                    new Vec3(0.57, 1.06, -0.11), new Vec3(-0.57, 1.06, -0.11),
                    new Vec3(0.57, 1.06, -1.42), new Vec3(-0.57, 1.06, -1.42),
                    new Vec3(0.57, 1.06, -2.73), new Vec3(-0.57, 1.06, -2.73))
            .color(0xD94B1A) // rojo anaranjado tradicional de chiva
            .chestRows(6) // capacete / parrilla de carga en el techo de 54 casillas (9x6)
            .wheelRadius(0.56875F) // llanta de camión (7 px * 1.30x = 9.1 px)
            .headTurnLimit(105.0F)
            .directTurnRate(0.9F)
            .enginePitch(0.45F, 1.25F)
            .hasRadio(true)
            .impactMultiplier(1.60F)
            .horn(25, SoundEvents.NOTE_BLOCK_PLING, 1.5F, 0.5F, 0.63F)
            .tuning(() -> VehicleTuning.CHIVA)
            .build();

    public static final VehicleSpec TWINGO = VehicleSpec.builder("twingo")
            .size(2.0F, 1.75F)
            // 2 asientos delanteros y banca trasera para 2 pasajeros; el cojín está a y = -9.5 px (0.37 bloques)
            .seats(new Vec3(0.40, 0.37, 0.25), new Vec3(-0.40, 0.37, 0.25),
                    new Vec3(0.40, 0.37, -0.53), new Vec3(-0.40, 0.37, -0.53))
            .color(0xC4CBD2) // gris plata metálico Twingo
            .chestRows(2) // compacto urbano: baúl trasero de 18 casillas
            .wheelRadius(0.3125F) // llanta de 16 px a escala 0.625x = radio 5/16 bloques
            .headTurnLimit(105.0F)
            .directTurnRate(1.5F)
            .enginePitch(0.60F, 1.55F)
            .hasRadio(true)
            .impactMultiplier(0.95F)
            .horn(18, SoundEvents.NOTE_BLOCK_PLING, 1.35F, 0.85F, 1.06F)
            .tuning(() -> VehicleTuning.TWINGO)
            .build();

    public static final VehicleSpec CHALUPA = VehicleSpec.builder("chalupa")
            .size(2.2F, 1.85F)
            // 3 filas de 2 asientos: cabina delantera (piloto y copiloto) y 2 filas abiertas bajo la capota
            .seats(new Vec3(0.39, 0.51, 0.23), new Vec3(-0.39, 0.51, 0.23),
                    new Vec3(0.39, 0.51, -0.55), new Vec3(-0.39, 0.51, -0.55),
                    new Vec3(0.39, 0.51, -1.33), new Vec3(-0.39, 0.51, -1.33))
            .color(0x1488D8) // azul fluvial colombiano
            .chestRows(4) // bodega de carga de 36 casillas
            .wheelRadius(0.25F) // hélice del motor fuera de borda
            .headTurnLimit(105.0F)
            .directTurnRate(1.2F)
            .enginePitch(0.55F, 1.50F)
            .mobsCanBoard(false)
            .hasRadio(true)
            .watercraft(true)
            .impactMultiplier(1.10F)
            .horn(20, SoundEvents.NOTE_BLOCK_PLING, 1.45F, 0.52F, 0.65F)
            .tuning(() -> VehicleTuning.CHALUPA)
            .build();

    /** En el orden en que se registran y aparecen en la pestaña creativa. */
    public static final List<VehicleSpec> ALL = List.of(CAR, MOTORCYCLE, MOTOCARRO, MOTOCARGUERO, CHIVA, TWINGO, CHALUPA);

    private VehicleSpecs() {}
}
