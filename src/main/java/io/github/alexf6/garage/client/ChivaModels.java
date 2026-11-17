package io.github.alexf6.garage.client;

import static io.github.alexf6.garage.client.ModelKit.at;
import static io.github.alexf6.garage.client.ModelKit.cube;

import io.github.alexf6.garage.client.VehicleRenderer.VehicleRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * Chiva colombiana (bus escalera) hecha con cubos, en dos capas que comparten textura (256x256):
 * <ul>
 *   <li>{@code BODY} (franja {@code v=0..64}): capó, guardabarros delanteros, cabina/puertas y faldones laterales
 *       teñibles (rojo anaranjado por defecto).</li>
 *   <li>{@code TRIM} (resto del lienzo): parrilla cromada ({@code 154,66}), bómperes, placas {@code TBJ 274}
 *       ({@code 222,80}), parabrisas ({@code 154,104}), 4 bancas de madera ({@code 24,120}/{@code 86,120}), parales y
 *       paneles con filetes típicos, friso ({@code 0,142}), ventanilla superior azul ({@code 106,142}), techo azul
 *       ({@code 0,198}), capacete de madera ({@code 170,198}), trasera ({@code 184,0..38}), escalera ({@code 248,0})
 *       y 4 llantas de camión ({@code 214,142}/{@code 214,164}).</li>
 * </ul>
 * Origen en el suelo ({@code y = 24}) y frente hacia {@code -Z}. Todas las piezas solo se tocan borde con borde
 * para evitar z-fighting y cuelgan de {@code chassis} salvo las 4 ruedas.
 */
public final class ChivaModels {

    /** Escala visual del modelo en {@link ClientVehicles}. */
    static final float MODEL_SCALE = 1.30F;

    /** Capa de carrocería teñible (capó, guardabarros, cabina y faldones). */
    public static final ModelLayerLocation BODY = layer("body");
    /** Capa de detalles sin teñir (parrilla, techo azul, madera, frisos, bancas, escalera y ruedas). */
    public static final ModelLayerLocation TRIM = layer("trim");
    /** Lo que {@link ClientVehicles} necesita para registrar este modelo. */
    static final ClientVehicles.ModelDef DEF = new ClientVehicles.ModelDef(BODY, TRIM, Part::new,
            ChivaModels::createBodyLayer, ChivaModels::createTrimLayer);

    /** Nombres de las 4 ruedas en el orden esperado (delanteras primero). */
    public static final String[] WHEELS = {"wheel_fl", "wheel_fr", "wheel_bl", "wheel_br"};
    private static final int FRONT_WHEELS = 2;
    private static final String CHASSIS = "chassis";
    /** Alarga la carrocería al largo de 1.75x (~8 bloques) manteniendo la altura y anchura base de 1.30x y ruedas redondas. */
    private static final float CHASSIS_LENGTH_STRETCH = 1.75F / 1.30F;
    private static final int RACK_WOOD_U = 170;
    private static final int RACK_WOOD_V = 198;

    private record WheelMount(String name, float x, float z) {}

    private static final WheelMount[] WHEEL_MOUNTS = {
            new WheelMount(WHEELS[0], 13.5F, -24.0F * CHASSIS_LENGTH_STRETCH),
            new WheelMount(WHEELS[1], -13.5F, -24.0F * CHASSIS_LENGTH_STRETCH),
            new WheelMount(WHEELS[2], 13.5F, 22.0F * CHASSIS_LENGTH_STRETCH),
            new WheelMount(WHEELS[3], -13.5F, 22.0F * CHASSIS_LENGTH_STRETCH)
    };

    private static ModelLayerLocation layer(String name) {
        return ModelKit.layer("chiva", name);
    }

    private static void addSidePair(PartDefinition parent, String baseName, CubeListBuilder box, float leftX,
                                    float rightX, float z) {
        parent.addOrReplaceChild(baseName + "_l", box, at(leftX, 0, z));
        parent.addOrReplaceChild(baseName + "_r", box, at(rightX, 0, z));
    }

    private static void addIndexedSidePair(PartDefinition parent, String baseName, int index, CubeListBuilder box,
                                           float leftX, float rightX, float z) {
        parent.addOrReplaceChild(baseName + "_l_" + index, box, at(leftX, 0, z));
        parent.addOrReplaceChild(baseName + "_r_" + index, box, at(rightX, 0, z));
    }

    /** Capa BODY: capó, guardabarros, cabina y faldones bajos teñibles con el color del vehículo. */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition chassis = root.addOrReplaceChild(CHASSIS, CubeListBuilder.create(), at(0, 24, 0));

        // Capó clásico de camión y coronilla superior rebajados para ampliar la vista frontal del conductor
        chassis.addOrReplaceChild("hood", cube(0, 0).addBox(-8, -19, -32, 16, 10, 16), PartPose.ZERO);
        chassis.addOrReplaceChild("hood_crown", cube(66, 0).addBox(-6, -20, -31, 12, 1, 15), PartPose.ZERO);

        // Guardabarros delanteros con paso de rueda de 2 px sobre la llanta de radio 7 px (evita recortes en baches)
        addSidePair(chassis, "fender_top", cube(122, 0).addBox(0, -19, -32, 6, 3, 16), 8, -14, 0);
        addSidePair(chassis, "fender_front", cube(168, 0).addBox(0, -16, -32, 6, 7, 1), 8, -14, 0);
        addSidePair(chassis, "fender_rear", cube(168, 0).addBox(0, -16, -17, 6, 7, 1), 8, -14, 0);

        // Cabina delantera: puertas bajas, parales finos hasta el friso (y = -28) y dintel reducido (2 px) en y = -36..-34
        addSidePair(chassis, "cab_cowl", cube(0, 30).addBox(0, -20, -16, 2, 12, 12), 13, -15, 0);
        addSidePair(chassis, "cab_pillar_f", cube(30, 42).addBox(0, -28, -16, 2, 8, 1), 13, -15, 0);
        addSidePair(chassis, "cab_pillar_b", cube(30, 30).addBox(0, -28, -6, 2, 8, 2), 13, -15, 0);
        chassis.addOrReplaceChild("cab_header", cube(40, 30).addBox(-13, -36, -16, 26, 2, 2), PartPose.ZERO);

        // Faldones laterales de la carrocería abierta, aleta sobre el paso de rueda trasero y faldón trasero
        addSidePair(chassis, "side_skirt_mid", cube(98, 30).addBox(0, -12, -4, 2, 4, 18), 13, -15, 0);
        addSidePair(chassis, "side_arch", cube(140, 30).addBox(0, -16, 14, 1, 2, 16), 15, -16, 0);
        addSidePair(chassis, "side_skirt_rear", cube(178, 30).addBox(0, -12, 30, 2, 4, 2), 13, -15, 0);
        chassis.addOrReplaceChild("rear_valance", cube(0, 58).addBox(-15, -12, 32, 30, 4, 2), PartPose.ZERO);

        return LayerDefinition.create(mesh, 256, 256);
    }

    /** Capa TRIM: chasis, parrilla, luces, bancas, decoración típica, techo azul, capacete, escalera y ruedas. */
    public static LayerDefinition createTrimLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition chassis = root.addOrReplaceChild(CHASSIS, CubeListBuilder.create(), at(0, 24, 0));

        // Chasis inferior de acero (dividido en tramos de 16 px) y piso de madera
        for (int i = 0; i < 4; i++) {
            chassis.addOrReplaceChild("frame_" + i, cube(184, 40).addBox(-8, -8, -32 + i * 16, 16, 2, 16),
                    PartPose.ZERO);
        }
        chassis.addOrReplaceChild("floor", cube(0, 66).addBox(-13, -10, -16, 26, 2, 48), PartPose.ZERO);

        // Frente: parrilla cromada, emblema, faros, direccionales, bómper y placa TBJ 274 (profundidad entera = 1)
        chassis.addOrReplaceChild("grille", cube(154, 66).addBox(-8, -18, -33, 16, 8, 1), PartPose.ZERO);
        chassis.addOrReplaceChild("hood_badge", cube(154, 90).addBox(-4, -20, -33, 8, 2, 1), PartPose.ZERO);
        addSidePair(chassis, "headlight", cube(190, 66).addBox(0, -16, -33, 4, 4, 1), 9, -13, 0);
        addSidePair(chassis, "turn_signal", cube(202, 66).addBox(0, -18, -33, 4, 1, 1), 9, -13, 0);
        chassis.addOrReplaceChild("front_bumper", cube(154, 80).addBox(-15, -10, -35, 30, 4, 3), PartPose.ZERO);
        chassis.addOrReplaceChild("front_plate", cube(222, 80).addBox(-3, -9.5F, -36, 6, 3, 1), PartPose.ZERO);

        // Cabina: tablero al ras del parabrisas (-20..-17), volante y placas TBJ 274
        chassis.addOrReplaceChild("dash", cube(154, 96).addBox(-13, -20, -16, 26, 3, 3), PartPose.ZERO);
        chassis.addOrReplaceChild("steering_wheel", cube(210, 104).addBox(-3, -3, 0, 6, 6, 1), at(6, -19, -13));
        addSidePair(chassis, "door_plaque", cube(0, 120).addBox(0, -19, -15, 1, 8, 10), 15, -16, 0);

        // 4 bancas transversales (la de cabina elevada 4 px para buena vista sobre el capó, + 3 de pasajeros)
        CubeListBuilder cushion = cube(24, 120).addBox(-12, -13, -3, 24, 2, 6);
        CubeListBuilder back = cube(86, 120).addBox(-12, -21, 3, 24, 8, 2);
        int[] rowZ = {-11, 1, 13, 25};
        for (int i = 0; i < rowZ.length; i++) {
            int seatY = i == 0 ? -4 : 0;
            chassis.addOrReplaceChild("bench_cushion_" + i, cushion, at(0, seatY, rowZ[i]));
            chassis.addOrReplaceChild("bench_back_" + i, back, at(0, seatY, rowZ[i]));
        }

        // 3 paneles pintados de extremo de banca, 3 parales coloridos por costado y riel roji-blanco
        CubeListBuilder sidePanel = cube(140, 120).addBox(0, -21, 0, 2, 9, 5);
        CubeListBuilder sidePost = cube(156, 120).addBox(0, -28, 0, 1, 16, 2);
        int[][] bayOffsets = {{1, -4}, {13, 8}, {25, 20}};
        for (int i = 0; i < bayOffsets.length; i++) {
            addIndexedSidePair(chassis, "side_panel", i, sidePanel, 13, -15, bayOffsets[i][0]);
            addIndexedSidePair(chassis, "side_post", i, sidePost, 13.5F, -14.5F, bayOffsets[i][1]);
        }
        addSidePair(chassis, "side_rail", cube(164, 120).addBox(0, -12, -4, 1, 2, 18), 15, -16, 0);
        addSidePair(chassis, "side_rail_rear", cube(164, 120).addBox(0, -12, 30, 1, 2, 2), 15, -16, 0);

        // Friso multicolor, ventanilla superior azul y techo azul cielo
        addSidePair(chassis, "frieze", cube(0, 142).addBox(0, -31, -16, 2, 3, 48), 13, -15, 0);
        addSidePair(chassis, "clerestory", cube(106, 142).addBox(0, -36, -16, 2, 5, 48), 13, -15, 0);
        chassis.addOrReplaceChild("roof", cube(0, 198).addBox(-16, -38, -17, 32, 2, 52), PartPose.ZERO);

        // Capacete / parrilla de madera sobre el techo (escalonado más alto al frente, como en la referencia)
        addSidePair(chassis, "rack_front", cube(RACK_WOOD_U, RACK_WOOD_V).addBox(0, -43, -15, 1, 5, 24), 14, -15, 0);
        addSidePair(chassis, "rack_rear", cube(RACK_WOOD_U, RACK_WOOD_V).addBox(0, -43, 9, 1, 5, 24), 14, -15, 0);
        addSidePair(chassis, "rack_upper", cube(RACK_WOOD_U, RACK_WOOD_V).addBox(0, -46, -15, 1, 3, 20), 14, -15, 0);
        chassis.addOrReplaceChild("rack_cross_front",
                cube(RACK_WOOD_U, RACK_WOOD_V).addBox(-14, -46, -15, 28, 8, 1), PartPose.ZERO);
        chassis.addOrReplaceChild("rack_cross_mid",
                cube(RACK_WOOD_U, RACK_WOOD_V).addBox(-14, -42, 8, 28, 4, 1), PartPose.ZERO);
        chassis.addOrReplaceChild("rack_cross_rear",
                cube(RACK_WOOD_U, RACK_WOOD_V).addBox(-14, -43, 32, 28, 5, 1), PartPose.ZERO);
        int[] slatX = {-10, -4, 2, 8};
        for (int i = 0; i < slatX.length; i++) {
            chassis.addOrReplaceChild("rack_slat_front_" + i,
                    cube(RACK_WOOD_U, RACK_WOOD_V).addBox(slatX[i], -39, -14, 2, 1, 22), PartPose.ZERO);
            chassis.addOrReplaceChild("rack_slat_rear_" + i,
                    cube(RACK_WOOD_U, RACK_WOOD_V).addBox(slatX[i], -39, 9, 2, 1, 23), PartPose.ZERO);
        }

        // Parte trasera inferior: tablero ornamental con stops, bómper y placa
        chassis.addOrReplaceChild("rear_lower", cube(184, 18).addBox(-15, -22, 32, 30, 10, 2), PartPose.ZERO);
        chassis.addOrReplaceChild("rear_bumper", cube(184, 32).addBox(-15, -11, 35, 30, 4, 2), PartPose.ZERO);
        chassis.addOrReplaceChild("rear_plate", cube(222, 80).addBox(-3, -10.5F, 37, 6, 3, 1), PartPose.ZERO);

        // Escalera trasera hacia el techo (en z = 35..36, dibujada antes que las ventanillas translúcidas)
        CubeListBuilder ladderRail = cube(248, 0).addBox(0, -41, 35, 1, 30, 1);
        chassis.addOrReplaceChild("ladder_rail_l", ladderRail, at(-12, 0, 0));
        chassis.addOrReplaceChild("ladder_rail_r", ladderRail, at(-8, 0, 0));
        CubeListBuilder ladderRung = cube(240, 80).addBox(-11, 0, 35, 3, 1, 1);
        int[] rungY = {-39, -34, -29, -24, -19, -14};
        for (int i = 0; i < rungY.length; i++) {
            chassis.addOrReplaceChild("ladder_rung_" + i, ladderRung, at(0, rungY[i], 0));
        }

        // Piezas con cristal translúcido al final de chassis para que todo el interior y la escalera se dibujen antes
        chassis.addOrReplaceChild("rear_upper", cube(184, 0).addBox(-15, -36, 32, 30, 14, 2), PartPose.ZERO);
        chassis.addOrReplaceChild("windshield", cube(154, 104).addBox(-13.0F, -34.0F, -15.5F, 26.0F, 14.0F, 1.0F),
                PartPose.ZERO);

        // 4 llantas de camión (radio 7 px, apoyadas en y = 24 con eje en y = 17)
        CubeListBuilder tread0 = cube(214, 142)
                .addBox(-1.95F, -7, -3, 3.9F, 14, 6)
                .addBox(-1.90F, -3, -7, 3.8F, 6, 14);
        CubeListBuilder tread45 = cube(214, 142)
                .addBox(-1.85F, -7, -3, 3.7F, 14, 6)
                .addBox(-1.80F, -3, -7, 3.6F, 6, 14);
        CubeListBuilder hubs = cube(214, 164)
                .addBox(2.05F, -7, -7, 0, 14, 14)
                .addBox(-2.05F, -7, -7, 0, 14, 14);
        for (WheelMount mount : WHEEL_MOUNTS) {
            ModelKit.addWheel(root, mount.name(), at(mount.x(), 17, mount.z()), tread0, tread45, hubs);
        }

        return LayerDefinition.create(mesh, 256, 256);
    }

    /**
     * Submodelo para una capa ({@code BODY} o {@code TRIM}).
     * Aplica la suspensión (cabeceo, balanceo y rebote) a {@code chassis} y hace rodar y girar las 4 ruedas.
     */
    public static final class Part extends EntityModel<VehicleRenderState> {
        private final ModelPart chassis;
        private final ModelPart[] wheels;

        /** Crea el submodelo horneado resolviendo {@code chassis} y las 4 ruedas en esta capa. */
        public Part(ModelPart root) {
            super(root, RenderTypes::entityTranslucent);
            this.chassis = root.getChild(CHASSIS);
            this.wheels = ModelKit.wheels(root, WHEELS);
        }

        /** Anima suspensión de carrocería y rodadura/dirección de las 4 ruedas según {@code state}. */
        @Override
        public void setupAnim(VehicleRenderState state) {
            super.setupAnim(state);
            chassis.zScale = CHASSIS_LENGTH_STRETCH;
            chassis.xRot = state.bodyPitch;
            chassis.zRot = state.bodyRoll;
            chassis.y = ModelKit.GROUND_Y_PX + ModelKit.heavePx(state.bodyHeave);

            ModelKit.spinWheels(wheels, state.wheelRoll, state.steer, FRONT_WHEELS);
        }
    }

    private ChivaModels() {}
}
