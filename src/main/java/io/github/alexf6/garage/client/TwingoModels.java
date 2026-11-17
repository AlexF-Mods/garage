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
 * Modelo del Renault Twingo (primera generación) en dos capas sobre lienzo 256x256
 * (modelado a 25.6 px/bloque con escala visual {@value #MODEL_SCALE} para conservar tamaño real con mayor detalle):
 * <ul>
 *   <li>{@code BODY} (franja {@code v=0..108}): capó monovolumen inclinado, aletas delanteras, puertas, pasos de rueda,
 *       parales A y C, techo bajo, portalón trasero y carcasas de espejos teñibles (gris plata por defecto).</li>
 *   <li>{@code TRIM} (franja {@code v=111..232}): bómperes bitono con exploradoras y parrilla, faros redondeados,
 *       placas europeas {@code twingo}, tomas de aire del capó, emblemas Renault, calcomanía floral lateral,
 *       molduras negras, tablero con velocímetro central y perillas de clima, volante giratorio, 4 asientos en
 *       tela cuadriculada, vidrios translúcidos con limpiaparabrisas, antena de techo y 4 rines de 5 radios.</li>
 * </ul>
 * Origen en el suelo ({@code y = 24}) y frente hacia {@code -Z}. Todas las piezas se apoyan borde con borde o con
 * resalte mínimo en calcomanías para evitar z-fighting, y los vidrios van al final de {@code chassis}.
 */
public final class TwingoModels {

    /** Escala visual del modelo en {@link ClientVehicles}: convierte 25.6 px/bloque a 16 px/bloque reales. */
    static final float MODEL_SCALE = 0.625F;

    public static final ModelLayerLocation BODY = layer("body");
    public static final ModelLayerLocation TRIM = layer("trim");
    /** Lo que {@link ClientVehicles} necesita para registrar este modelo. */
    static final ClientVehicles.ModelDef DEF = new ClientVehicles.ModelDef(BODY, TRIM, Part::new,
            TwingoModels::createBodyLayer, TwingoModels::createTrimLayer);

    public static final String[] WHEELS = {"wheel_fl", "wheel_fr", "wheel_bl", "wheel_br"};
    private static final int FRONT_WHEELS = 2;
    private static final String CHASSIS = "chassis";
    private static final String STEERING_WHEEL = "steering_wheel";

    private static final float HOOD_PITCH = 0.24F;
    private static final float WINDSHIELD_RAKE = -0.62F;
    private static final float HATCH_PITCH = 0.24F;
    private static final float BODY_ROLL_AT_FULL_STEER = 0.09F;
    private static final float STEERING_WHEEL_RATIO = 1.5F;

    private static final ModelKit.IdleVibration IDLE = new ModelKit.IdleVibration(0.9F, 0.003F, 1.8F, 0.02F, 0.35F);

    private static final int DARK_U = 0;
    private static final int DARK_V = 175;
    private static final ModelKit.OctagonWheel WHEEL_RING = new ModelKit.OctagonWheel(174, 195, 4.0F, 7.40F, 4.40F, 3.06F);

    private record WheelMount(String name, float x, float z) {}

    private static final WheelMount[] WHEEL_MOUNTS = {
            new WheelMount(WHEELS[0], 19.0F, -28.0F),
            new WheelMount(WHEELS[1], -19.0F, -28.0F),
            new WheelMount(WHEELS[2], 19.0F, 26.0F),
            new WheelMount(WHEELS[3], -19.0F, 26.0F)
    };

    private static ModelLayerLocation layer(String name) {
        return ModelKit.layer("twingo", name);
    }

    private static void addSidePair(PartDefinition parent, String baseName, CubeListBuilder box, float leftX,
                                    float rightX) {
        parent.addOrReplaceChild(baseName + "_l", box, at(leftX, 0.0F, 0.0F));
        parent.addOrReplaceChild(baseName + "_r", box, at(rightX, 0.0F, 0.0F));
    }

    /** Capa BODY: paneles exteriores de carrocería y espejos teñibles con el color del vehículo. */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition chassis = root.addOrReplaceChild(CHASSIS, CubeListBuilder.create(), at(0.0F, 24.0F, 0.0F));

        // 1. Frente monovolumen inclinado (capó y aletas delanteras inclinadas hacia -Z)
        PartDefinition hoodGroup = chassis.addOrReplaceChild("hood_group", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -24.0F, -22.0F, HOOD_PITCH, 0.0F, 0.0F));
        hoodGroup.addOrReplaceChild("hood", cube(73, 0).addBox(-18.0F, 0.0F, -19.0F, 36, 5, 19), PartPose.ZERO);
        addSidePair(hoodGroup, "fender_slope", cube(184, 0).addBox(0.0F, 0.0F, -19.0F, 4, 4, 19), 18.0F, -22.0F);
        chassis.addOrReplaceChild("nose_panel", cube(118, 43).addBox(-18.0F, -19.0F, -41.0F, 36, 5, 3), PartPose.ZERO);
        addSidePair(chassis, "front_corner", cube(206, 58).addBox(0.05F, -18.5F, -41.0F, 1.9F, 4, 3), 20.0F, -22.0F);

        // 2. Costados: estribos de torpedo, puertas, arcos traseros y esquinas traseras
        addSidePair(chassis, "cowl_side", cube(180, 58).addBox(0.05F, -24.0F, -22.0F, 1.9F, 6, 4), 20.0F, -22.0F);
        addSidePair(chassis, "door", cube(0, 0).addBox(0.0F, -24.0F, -18.0F, 2, 18, 34), 20.0F, -22.0F);
        addSidePair(chassis, "rear_arch_top", cube(73, 25).addBox(0.0F, -24.0F, 16.0F, 2, 6, 20), 20.0F, -22.0F);
        addSidePair(chassis, "rear_corner", cube(193, 58).addBox(0.0F, -24.0F, 36.0F, 2, 10, 4), 20.0F, -22.0F);

        // 3. Parales A inclinados (-0.62 rad), techo bajo (y = -43..-41) y parales C
        PartDefinition windshieldFrame = chassis.addOrReplaceChild("windshield_frame", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -24.0F, -22.0F, WINDSHIELD_RAKE, 0.0F, 0.0F));
        addSidePair(windshieldFrame, "a_pillar", cube(235, 0).addBox(0.05F, -21.0F, 0.0F, 1.9F, 21, 3), 20.0F, -22.0F);

        chassis.addOrReplaceChild("roof_main", cube(0, 56).addBox(-22.0F, -43.0F, -10.0F, 44, 2, 45), PartPose.ZERO);
        chassis.addOrReplaceChild("roof_brow", cube(0, 104).addBox(-20.0F, -43.0F, -12.0F, 40, 2, 2), PartPose.ZERO);

        PartDefinition rearPillarGroup = chassis.addOrReplaceChild("c_pillar_group", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -24.0F, 40.0F, HATCH_PITCH, 0.0F, 0.0F));
        addSidePair(rearPillarGroup, "c_pillar", cube(220, 25).addBox(0.05F, -18.0F, -5.0F, 1.9F, 18, 5), 20.0F, -22.0F);

        // 4. Portalón trasero y carcasas de espejos retrovisores
        chassis.addOrReplaceChild("rear_hatch_lower", cube(118, 25).addBox(-20.0F, -25.0F, 37.0F, 40, 12, 3),
                PartPose.ZERO);
        PartDefinition hatchFrame = chassis.addOrReplaceChild("hatch_frame", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -25.0F, 40.0F, HATCH_PITCH, 0.0F, 0.0F));
        addSidePair(hatchFrame, "hatch_pillar", cube(205, 25).addBox(0.0F, -17.0F, -3.0F, 4, 17, 3), 16.0F, -20.0F);
        hatchFrame.addOrReplaceChild("hatch_top_bar", cube(85, 104).addBox(-16.0F, -17.0F, -3.0F, 32, 2, 3),
                PartPose.ZERO);

        chassis.addOrReplaceChild("mirror_cap_l", cube(217, 58).addBox(23.5F, -27.5F, -18.5F, 3, 3, 3), PartPose.ZERO);
        chassis.addOrReplaceChild("mirror_cap_r", cube(217, 58).addBox(-26.5F, -27.5F, -18.5F, 3, 3, 3), PartPose.ZERO);

        return LayerDefinition.create(mesh, 256, 256);
    }

    /** Capa TRIM: chasis, bómperes, faros, calcomanías, interior detallado, vidrios translúcidos y ruedas. */
    public static LayerDefinition createTrimLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition chassis = root.addOrReplaceChild(CHASSIS, CubeListBuilder.create(), at(0.0F, 24.0F, 0.0F));

        // 1. Piso, mamparo frontal, pasos de rueda interiores cerrados y bandeja trasera del baúl
        for (int i = 0; i < 5; i++) {
            chassis.addOrReplaceChild("floor_" + i, cube(DARK_U, DARK_V)
                    .addBox(-20.0F, -7.0F, -38.0F + i * 15, 40, 2, 15), PartPose.ZERO);
        }
        chassis.addOrReplaceChild("front_firewall", cube(DARK_U, DARK_V)
                .addBox(-18.0F, -16.0F, -23.0F, 36, 9, 2), PartPose.ZERO);
        chassis.addOrReplaceChild("parcel_shelf", cube(DARK_U, DARK_V)
                .addBox(-20.0F, -24.5F, 22.0F, 40, 1, 15), PartPose.ZERO);
        addSidePair(chassis, "front_well", cube(106, 111).addBox(0.0F, -18.0F, -38.0F, 1, 11, 20), 11.0F, -12.0F);
        addSidePair(chassis, "front_well_front", cube(DARK_U, DARK_V).addBox(0.0F, -16.0F, -38.0F, 8, 8, 1),
                12.0F, -20.0F);
        addSidePair(chassis, "front_well_rear", cube(DARK_U, DARK_V).addBox(0.0F, -18.0F, -19.0F, 8, 11, 1),
                12.0F, -20.0F);
        addSidePair(chassis, "rear_well", cube(106, 111).addBox(0.0F, -18.0F, 16.0F, 1, 11, 20), 14.0F, -15.0F);
        addSidePair(chassis, "rear_well_front", cube(DARK_U, DARK_V).addBox(0.0F, -18.0F, 16.0F, 6, 11, 1),
                14.0F, -20.0F);
        addSidePair(chassis, "rear_well_rear", cube(DARK_U, DARK_V).addBox(0.0F, -18.0F, 35.0F, 6, 11, 1),
                14.0F, -20.0F);

        // 2. Frente: tomas del capó, faros ojo de rana, emblema Renault, bómper bitono y placa "twingo"
        PartDefinition hoodTrim = chassis.addOrReplaceChild("hood_trim", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -24.0F, -22.0F, HOOD_PITCH, 0.0F, 0.0F));
        hoodTrim.addOrReplaceChild("hood_vents", cube(222, 219).addBox(-16.0F, -0.15F, -7.0F, 8, 1, 5),
                PartPose.ZERO);

        chassis.addOrReplaceChild("headlight_l", cube(188, 219).addBox(11.0F, -20.0F, -42.2F, 6, 5, 2), PartPose.ZERO);
        chassis.addOrReplaceChild("headlight_r", cube(205, 219).addBox(-17.0F, -20.0F, -42.2F, 6, 5, 2), PartPose.ZERO);
        chassis.addOrReplaceChild("front_logo", cube(17, 227).addBox(-2.0F, -19.5F, -41.6F, 4, 4, 1), PartPose.ZERO);
        chassis.addOrReplaceChild("front_bumper_upper", cube(115, 210).addBox(-22.0F, -14.0F, -44.0F, 44, 3, 4),
                PartPose.ZERO);
        chassis.addOrReplaceChild("front_bumper_lower", cube(0, 210).addBox(-21.0F, -11.0F, -43.5F, 42, 4, 4),
                PartPose.ZERO);
        chassis.addOrReplaceChild("front_plate", cube(37, 227).addBox(-5.0F, -12.5F, -44.6F, 10, 3, 1), PartPose.ZERO);

        // 3. Costados: parales B negros, molduras laterales, calcomanía floral, manijas, direccionales, tapa tanque y espejos
        addSidePair(chassis, "b_pillar", cube(183, 111).addBox(0.0F, -41.0F, 10.0F, 2, 17, 4), 20.0F, -22.0F);
        addSidePair(chassis, "rub_strip_f", cube(DARK_U, DARK_V).addBox(0.0F, -12.5F, -17.0F, 0.5F, 2, 17),
                22.0F, -22.5F);
        addSidePair(chassis, "rub_strip_m", cube(DARK_U, DARK_V).addBox(0.0F, -12.5F, 0.0F, 0.5F, 2, 17),
                22.0F, -22.5F);
        addSidePair(chassis, "rub_strip_r", cube(DARK_U, DARK_V).addBox(0.0F, -12.5F, 36.0F, 0.5F, 2, 4),
                22.0F, -22.5F);
        addSidePair(chassis, "flower_decal", cube(111, 175).addBox(0.0F, -22.5F, -14.0F, 1, 10, 9), 21.15F, -22.15F);
        addSidePair(chassis, "door_handle", cube(104, 210).addBox(0.0F, -20.5F, 8.0F, 1, 4, 4), 21.20F, -22.20F);
        addSidePair(chassis, "side_blinker", cube(28, 227).addBox(0.0F, -16.5F, -17.0F, 1, 2, 3), 21.30F, -22.30F);
        chassis.addOrReplaceChild("fuel_cap", cube(93, 210).addBox(-22.15F, -21.5F, 20.0F, 1, 4, 4), PartPose.ZERO);
        chassis.addOrReplaceChild("mirror_stalk_l", cube(DARK_U, DARK_V).addBox(22.0F, -26.0F, -18.0F, 1.5F, 1.5F, 2),
                PartPose.ZERO);
        chassis.addOrReplaceChild("mirror_stalk_r", cube(DARK_U, DARK_V).addBox(-23.5F, -26.0F, -18.0F, 1.5F, 1.5F, 2),
                PartPose.ZERO);

        // Antena de techo y vierteaguas frontal con limpiaparabrisas
        chassis.addOrReplaceChild("antenna_base", cube(DARK_U, DARK_V).addBox(-17.5F, -44.0F, -6.5F, 1.5F, 1, 2),
                PartPose.ZERO);
        chassis.addOrReplaceChild("antenna_mast", cube(DARK_U, DARK_V).addBox(-0.4F, -13.0F, -0.4F, 0.8F, 13, 0.8F),
                PartPose.offsetAndRotation(-16.8F, -43.5F, -5.5F, -0.58F, 0.0F, 0.0F));
        PartDefinition windshieldTrim = chassis.addOrReplaceChild("windshield_trim", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -24.0F, -22.0F, WINDSHIELD_RAKE, 0.0F, 0.0F));
        windshieldTrim.addOrReplaceChild("ws_cowl", cube(DARK_U, DARK_V).addBox(-20.0F, -2.0F, 0.0F, 40, 2, 2),
                PartPose.ZERO);
        windshieldTrim.addOrReplaceChild("ws_wiper", cube(DARK_U, DARK_V).addBox(-12.0F, -3.5F, -0.2F, 22, 1, 1),
                PartPose.ZERO);

        // 4. Trasera opaca: pilotos, emblema, placa, insignia "twingo", bómper bitono y limpiaparabrisas trasero
        chassis.addOrReplaceChild("taillight_l", cube(140, 195).addBox(14.0F, -24.0F, 39.6F, 7, 10, 1), PartPose.ZERO);
        chassis.addOrReplaceChild("taillight_r", cube(157, 195).addBox(-21.0F, -24.0F, 39.6F, 7, 10, 1), PartPose.ZERO);
        chassis.addOrReplaceChild("rear_logo", cube(17, 227).addBox(-2.0F, -24.5F, 39.8F, 4, 4, 1), PartPose.ZERO);
        chassis.addOrReplaceChild("rear_plate", cube(37, 227).addBox(-5.0F, -20.0F, 39.8F, 10, 3, 1), PartPose.ZERO);
        chassis.addOrReplaceChild("twingo_badge", cube(60, 227).addBox(-13.0F, -18.5F, 39.8F, 7, 2, 1), PartPose.ZERO);
        chassis.addOrReplaceChild("rear_bumper_upper", cube(0, 219).addBox(-22.0F, -13.5F, 39.5F, 44, 3, 4),
                PartPose.ZERO);
        chassis.addOrReplaceChild("rear_bumper_lower", cube(97, 219).addBox(-21.0F, -10.5F, 39.5F, 42, 4, 3),
                PartPose.ZERO);

        PartDefinition hatchTrim = chassis.addOrReplaceChild("hatch_trim", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -25.0F, 40.0F, HATCH_PITCH, 0.0F, 0.0F));
        hatchTrim.addOrReplaceChild("rear_wiper_hub", cube(DARK_U, DARK_V).addBox(-1.0F, -2.0F, -1.2F, 2, 2, 1),
                PartPose.ZERO);
        hatchTrim.addOrReplaceChild("rear_wiper_arm", cube(DARK_U, DARK_V).addBox(-11.0F, -0.5F, -0.5F, 11, 1, 1),
                PartPose.offsetAndRotation(0.0F, -1.5F, -0.8F, 0.0F, 0.0F, 0.25F));

        // 5. Interior: tablero con velocímetro central, columna/volante y 4 asientos en tela cuadriculada
        chassis.addOrReplaceChild("dash_main", cube(0, 195).addBox(-20.0F, -24.0F, -22.0F, 40, 7, 6), PartPose.ZERO);
        chassis.addOrReplaceChild("dash_speedo_pod", cube(212, 210).addBox(-5.0F, -26.0F, -21.0F, 10, 2, 4),
                PartPose.ZERO);
        chassis.addOrReplaceChild("steering_column", cube(DARK_U, DARK_V).addBox(8.5F, -21.0F, -16.0F, 3, 2, 3),
                PartPose.ZERO);
        chassis.addOrReplaceChild(STEERING_WHEEL, cube(195, 195).addBox(-4.0F, -4.0F, -0.5F, 8, 8, 1),
                at(10.0F, -20.0F, -12.5F));

        CubeListBuilder cushion = cube(93, 195).addBox(-6.0F, -9.5F, -5.5F, 12, 3, 11);
        CubeListBuilder frontBack = cube(211, 175).addBox(-6.0F, -20.5F, -1.5F, 12, 11, 3);
        CubeListBuilder headrest = cube(0, 227).addBox(-3.0F, -4.0F, -1.0F, 6, 4, 2);

        float[][] seatPos = {{10.0F, -6.5F}, {-10.0F, -6.5F}, {10.0F, 13.5F}, {-10.0F, 13.5F}};
        String[] seatNames = {"fl", "fr", "bl", "br"};
        for (int i = 0; i < seatNames.length; i++) {
            float sx = seatPos[i][0];
            float sz = seatPos[i][1];
            chassis.addOrReplaceChild("cushion_" + seatNames[i], cushion, at(sx, 0.0F, sz));
            float hrY = i < 2 ? -20.5F : -21.5F;
            float hrZ = i < 2 ? 0.5F : 20.5F;
            chassis.addOrReplaceChild("headrest_" + seatNames[i], headrest, at(sx, hrY, hrZ));
        }
        chassis.addOrReplaceChild("back_fl", frontBack, at(10.0F, 0.0F, 0.5F));
        chassis.addOrReplaceChild("back_fr", frontBack, at(-10.0F, 0.0F, 0.5F));
        chassis.addOrReplaceChild("rear_backrest", cube(132, 175).addBox(-18.0F, -21.5F, 19.0F, 36, 12, 3),
                PartPose.ZERO);

        // 6. Vidrios translúcidos al final de chassis para evitar recortes del interior
        PartDefinition windshieldGlass = chassis.addOrReplaceChild("windshield_glass_group", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -24.0F, -22.0F, WINDSHIELD_RAKE, 0.0F, 0.0F));
        windshieldGlass.addOrReplaceChild("ws_glass", cube(106, 146).addBox(-20.0F, -21.0F, 0.5F, 40, 19, 1),
                PartPose.ZERO);

        addSidePair(chassis, "side_window_front", cube(0, 111).addBox(0.0F, -41.0F, -19.0F, 1, 17, 29),
                20.5F, -21.5F);
        addSidePair(chassis, "side_window_rear", cube(61, 111).addBox(0.0F, -41.0F, 14.0F, 1, 17, 21),
                20.5F, -21.5F);

        PartDefinition rearGlassGroup = chassis.addOrReplaceChild("rear_glass_group", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -25.0F, 40.0F, HATCH_PITCH, 0.0F, 0.0F));
        rearGlassGroup.addOrReplaceChild("rear_glass", cube(189, 146).addBox(-16.0F, -15.0F, -2.5F, 32, 15, 1),
                PartPose.ZERO);

        // 7. Cuatro ruedas de 5 radios 16x16 colgando de root (radio 8 px → 0.3125 bloques, apoyadas en y = 24)
        CubeListBuilder hubs = cube(149, 111)
                .addBox(2.02F, -8.0F, -8.0F, 0.2F, 16, 16)
                .addBox(-2.22F, -8.0F, -8.0F, 0.2F, 16, 16);
        for (WheelMount mount : WHEEL_MOUNTS) {
            ModelKit.addOctagonWheel(root, mount.name(), at(mount.x(), 16.0F, mount.z()), WHEEL_RING, hubs);
        }

        return LayerDefinition.create(mesh, 256, 256);
    }

    /**
     * Submodelo para una capa ({@code BODY} o {@code TRIM}).
     * Aplica suspensión, balanceo en curva, ralentí, rodado de las 4 ruedas, dirección delantera y giro del volante.
     */
    public static final class Part extends EntityModel<VehicleRenderState> {
        private final ModelPart chassis;
        private final ModelPart steeringWheel;
        private final ModelPart[] wheels;

        public Part(ModelPart root) {
            super(root, RenderTypes::entityCutoutCull);
            this.chassis = root.getChild(CHASSIS);
            this.steeringWheel = ModelKit.childOrNull(chassis, STEERING_WHEEL);
            this.wheels = ModelKit.wheels(root, WHEELS);
        }

        @Override
        public void setupAnim(VehicleRenderState state) {
            super.setupAnim(state);
            ModelKit.spinWheels(wheels, state.wheelRoll, state.steer, FRONT_WHEELS);
            if (this.steeringWheel != null) {
                this.steeringWheel.zRot = -state.steer * STEERING_WHEEL_RATIO;
            }
            ModelKit.moveChassis(this.chassis, state, IDLE, 0.4F, BODY_ROLL_AT_FULL_STEER, MODEL_SCALE);
        }
    }

    private TwingoModels() {}
}
