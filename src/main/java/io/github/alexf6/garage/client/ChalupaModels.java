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
import net.minecraft.util.Mth;

/**
 * Modelo de Chalupa fluvial colombiana con motor fuera de borda Yamaha sobre lienzo 256x256
 * (modelado a 25.6 px/bloque con escala visual {@value #MODEL_SCALE}):
 * <ul>
 *   <li>{@code BODY} (franja {@code v=0..85}): casco en V, proa afilada escalonada, cubierta de proa, espejo de
 *       popa, parales frontales, ceja superior de cabina y bordes de capota teñibles (azul {@code #1488D8} por defecto).</li>
 *   <li>{@code TRIM} (franja {@code v=86..240}): quilla azul marino, regala roja, mamparo y banda blanca de cabina,
 *       techo rojiblanco con parrilla, carga colombiana y bicicleta tumbada sobre la cubierta superior, foco de proa,
 *       matrícula {@code RN 10320643}, escalerilla lateral, pilares de capota y cenefas, consola de mando, 6 asientos
 *       de franjas rojas y azules, bandera ondeante, motor fuera de borda {@code YAMAHA} giratorio con hélice activa
 *       y vidrios translúcidos de cabina.</li>
 * </ul>
 * Origen en la quilla ({@code y = 24}) y proa hacia {@code -Z}.
 */
public final class ChalupaModels {

    /** Escala visual del modelo en {@link ClientVehicles}: convierte 25.6 px/bloque a 16 px/bloque reales. */
    static final float MODEL_SCALE = 0.625F;

    public static final ModelLayerLocation BODY = layer("body");
    public static final ModelLayerLocation TRIM = layer("trim");
    /** Lo que {@link ClientVehicles} necesita para registrar este modelo. */
    static final ClientVehicles.ModelDef DEF = new ClientVehicles.ModelDef(BODY, TRIM, Part::new,
            ChalupaModels::createBodyLayer, ChalupaModels::createTrimLayer);

    private static final String CHASSIS = "chassis";
    private static final String OUTBOARD = "outboard";
    private static final String PROPELLER = "propeller";
    private static final String FLAG = "flag_cloth";
    private static final String STEERING_WHEEL = "steering_wheel";

    private static final float WINDSHIELD_RAKE = -0.46F;
    private static final float BODY_ROLL_AT_FULL_STEER = 0.14F;
    private static final float FLAG_SPEED = 0.35F;
    private static final float FLAG_SWING_IDLE = 0.10F;
    private static final float FLAG_SWING_FAST = 0.32F;

    private static final ModelKit.IdleVibration IDLE = new ModelKit.IdleVibration(0.85F, 0.004F, 1.7F, 0.04F, 0.45F);

    private static final int NAVY_U = 0;
    private static final int NAVY_V = 156;
    private static final int DARK_U = 103;
    private static final int DARK_V = 156;
    private static final int RED_U = 0;
    private static final int RED_V = 181;
    private static final int WHT_U = 103;
    private static final int WHT_V = 181;

    private static ModelLayerLocation layer(String name) {
        return ModelKit.layer("chalupa", name);
    }

    private static void addSidePair(PartDefinition parent, String baseName, CubeListBuilder box, float leftX,
                                    float rightX) {
        parent.addOrReplaceChild(baseName + "_l", box, at(leftX, 0.0F, 0.0F));
        parent.addOrReplaceChild(baseName + "_r", box, at(rightX, 0.0F, 0.0F));
    }

    /** Capa BODY: casco principal, proa escalonada con cubierta continua, espejo y bordes de cabina teñibles. */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition chassis = root.addOrReplaceChild(CHASSIS, CubeListBuilder.create(), at(0.0F, 24.0F, 0.0F));

        // 1. Costados principales del casco en 3 tramos (z = -24..+54)
        for (int i = 0; i < 3; i++) {
            float zStart = -24.0F + i * 26.0F;
            addSidePair(chassis, "hull_side_" + i, cube(0, 0).addBox(0.0F, -16.0F, zStart, 6, 12, 26),
                    16.0F, -22.0F);
        }

        // 2. Proa en V escalonada con cubierta continua al ras de los costados
        addSidePair(chassis, "hull_bow_1", cube(199, 28).addBox(0.0F, -16.0F, -38.0F, 7, 11, 12), 12.0F, -19.0F);
        chassis.addOrReplaceChild("bow_deck_1", cube(0, 39).addBox(-19.0F, -18.0F, -38.0F, 38, 2, 12), PartPose.ZERO);

        addSidePair(chassis, "hull_bow_2", cube(180, 52).addBox(0.0F, -16.0F, -49.0F, 7, 10, 11), 7.0F, -14.0F);
        chassis.addOrReplaceChild("bow_deck_2", cube(101, 39).addBox(-14.0F, -18.0F, -49.0F, 28, 2, 11), PartPose.ZERO);

        chassis.addOrReplaceChild("hull_bow_3", cube(0, 54).addBox(-9.0F, -17.0F, -57.0F, 18, 10, 8), PartPose.ZERO);
        chassis.addOrReplaceChild("bow_deck_3", cube(0, 73).addBox(-9.0F, -18.0F, -57.0F, 18, 1, 8), PartPose.ZERO);
        chassis.addOrReplaceChild("bow_prow_tip", cube(217, 52).addBox(-3.5F, -18.0F, -62.0F, 7, 9, 5), PartPose.ZERO);

        // 3. Espejo trasero con muesca central para el motor fuera de borda
        addSidePair(chassis, "transom_lower", cube(210, 0).addBox(0.0F, -16.0F, 52.0F, 13, 12, 2), 7.0F, -20.0F);
        chassis.addOrReplaceChild("transom_notch", cube(210, 15).addBox(-7.0F, -9.0F, 52.0F, 14, 5, 2), PartPose.ZERO);

        // 4. Parales frontales inclinados, ceja superior continua de cabina y bordes azules del techo
        PartDefinition windshieldFrame = chassis.addOrReplaceChild("windshield_frame", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -24.0F, -26.0F, WINDSHIELD_RAKE, 0.0F, 0.0F));
        addSidePair(windshieldFrame, "a_pillar", cube(199, 0).addBox(0.0F, -23.0F, 0.0F, 2, 23, 3), 20.0F, -22.0F);
        windshieldFrame.addOrReplaceChild("a_pillar_mid", cube(199, 0).addBox(-2.0F, -23.0F, 0.0F, 4, 23, 3),
                PartPose.ZERO);

        for (int i = 0; i < 2; i++) {
            float browZ = -17.0F + i * 31.0F;
            float edgeZ = -12.0F + i * 31.0F;
            addSidePair(chassis, "cabin_brow_" + i, cube(65, 0).addBox(0.0F, -45.0F, browZ, 2, 6, 31), 20.0F, -22.0F);
            addSidePair(chassis, "roof_edge_side_" + i, cube(132, 0).addBox(0.0F, -47.0F, edgeZ, 2, 2, 31),
                    20.0F, -22.0F);
        }
        addSidePair(chassis, "stern_brow", cube(154, 53).addBox(0.0F, -45.0F, 45.0F, 2, 6, 9), 20.0F, -22.0F);
        chassis.addOrReplaceChild("roof_bow", cube(53, 54).addBox(-22.0F, -47.0F, -18.0F, 44, 2, 6), PartPose.ZERO);
        chassis.addOrReplaceChild("roof_edge_rear", cube(53, 63).addBox(-20.0F, -47.0F, 50.0F, 40, 2, 3), PartPose.ZERO);

        return LayerDefinition.create(mesh, 256, 256);
    }

    /** Capa TRIM: quilla, regala roja, mamparo y banda blanca, techo, carga, matrícula, interior, motor y vidrios. */
    public static LayerDefinition createTrimLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition chassis = root.addOrReplaceChild(CHASSIS, CubeListBuilder.create(), at(0.0F, 24.0F, 0.0F));

        // 1. Quilla inferior azul marino y piso antideslizante interior (cierra todo el fondo de proa a popa)
        for (int i = 0; i < 4; i++) {
            float z = -24.0F + i * 19.0F;
            chassis.addOrReplaceChild("keel_" + i, cube(NAVY_U, NAVY_V).addBox(-16.0F, -5.0F, z, 32, 5, 19),
                    PartPose.ZERO);
            chassis.addOrReplaceChild("deck_" + i, cube(DARK_U, DARK_V).addBox(-16.0F, -8.0F, z, 32, 3, 19),
                    PartPose.ZERO);
        }
        chassis.addOrReplaceChild("keel_bow_1", cube(NAVY_U, NAVY_V).addBox(-12.0F, -5.0F, -38.0F, 24, 5, 14),
                PartPose.ZERO);
        chassis.addOrReplaceChild("keel_bow_2", cube(NAVY_U, NAVY_V).addBox(-7.0F, -6.0F, -49.0F, 14, 5, 11),
                PartPose.ZERO);
        chassis.addOrReplaceChild("keel_bow_3", cube(NAVY_U, NAVY_V).addBox(-4.0F, -7.0F, -57.0F, 8, 5, 8),
                PartPose.ZERO);

        // 2. Mamparo delantero bajo el parabrisas, regala roja continua y banda blanca de cabina
        chassis.addOrReplaceChild("cabin_front_red", cube(0, 234).addBox(-20.0F, -18.0F, -26.0F, 40, 2, 3),
                PartPose.ZERO);
        chassis.addOrReplaceChild("cabin_front_white", cube(0, 224).addBox(-20.0F, -24.0F, -26.0F, 40, 6, 3),
                PartPose.ZERO);

        for (int i = 0; i < 4; i++) {
            float z = -26.0F + i * 19.0F;
            addSidePair(chassis, "red_stripe_" + i, cube(RED_U, RED_V).addBox(0.0F, -18.0F, z, 2, 2, 19),
                    20.0F, -22.0F);
            addSidePair(chassis, "white_band_" + i, cube(WHT_U, WHT_V).addBox(0.0F, -24.0F, z, 2, 6, 19),
                    20.0F, -22.0F);
        }
        addSidePair(chassis, "red_stripe_end", cube(RED_U, RED_V).addBox(0.0F, -18.0F, 50.0F, 2, 2, 4), 20.0F, -22.0F);
        addSidePair(chassis, "white_band_end", cube(WHT_U, WHT_V).addBox(0.0F, -24.0F, 50.0F, 2, 6, 4), 20.0F, -22.0F);

        // Cinta roja perimetral al ras del borde de proa (sin muros blancos sobresaliendo)
        addSidePair(chassis, "red_bow_1", cube(RED_U, RED_V).addBox(0.0F, -18.0F, -38.0F, 1.5F, 2, 12), 19.0F, -20.5F);
        addSidePair(chassis, "red_bow_2", cube(RED_U, RED_V).addBox(0.0F, -18.0F, -49.0F, 1.5F, 2, 11), 14.0F, -15.5F);
        addSidePair(chassis, "red_bow_3", cube(RED_U, RED_V).addBox(0.0F, -18.0F, -57.0F, 1.5F, 2, 8), 9.0F, -10.5F);
        chassis.addOrReplaceChild("red_prow_tip", cube(RED_U, RED_V).addBox(-4.0F, -18.0F, -62.5F, 8, 1.5F, 5.5F),
                PartPose.ZERO);

        addSidePair(chassis, "red_transom", cube(RED_U, RED_V).addBox(0.0F, -18.0F, 52.0F, 13, 2, 2), 7.0F, -20.0F);
        addSidePair(chassis, "white_transom", cube(WHT_U, WHT_V).addBox(0.0F, -24.0F, 52.0F, 13, 6, 2), 7.0F, -20.0F);
        addSidePair(chassis, "stern_corner_white", cube(229, 178).addBox(0.0F, -39.0F, 48.0F, 2, 15, 6), 20.0F, -22.0F);

        // 3. Pilares rojiblancos de capota, cenefas rojas, matrícula RN 10320643 y escalerilla lateral
        float[] pillarZ = {8.0F, 25.0F, 42.0F};
        for (int i = 0; i < pillarZ.length; i++) {
            addSidePair(chassis, "canopy_pillar_" + i, cube(212, 136).addBox(0.0F, -39.0F, pillarZ[i], 2, 15, 3),
                    20.0F, -22.0F);
        }
        addSidePair(chassis, "valance_a", cube(RED_U, RED_V).addBox(0.0F, -39.0F, 11.0F, 2, 3, 14), 20.0F, -22.0F);
        addSidePair(chassis, "valance_b", cube(RED_U, RED_V).addBox(0.0F, -39.0F, 28.0F, 2, 3, 14), 20.0F, -22.0F);
        addSidePair(chassis, "reg_decal", cube(182, 178).addBox(0.0F, -44.5F, -14.0F, 1, 5, 22), 21.15F, -22.15F);
        addSidePair(chassis, "boarding_steps", cube(225, 136).addBox(0.0F, -15.0F, 14.0F, 1, 9, 6), 21.15F, -22.15F);

        // 4. Techo rojiblanco, parrilla superior, foco de proa, carga colombiana, bicicleta tumbada y bandera
        chassis.addOrReplaceChild("roof_deck_f", cube(0, 88).addBox(-20.0F, -47.0F, -12.0F, 40, 2, 31), PartPose.ZERO);
        chassis.addOrReplaceChild("roof_deck_r", cube(0, 122).addBox(-20.0F, -47.0F, 19.0F, 40, 2, 31), PartPose.ZERO);
        chassis.addOrReplaceChild("bow_spotlight", cube(178, 207).addBox(-2.0F, -50.0F, -15.0F, 4, 3, 4), PartPose.ZERO);
        chassis.addOrReplaceChild("cargo_pack", cube(76, 207).addBox(4.0F, -52.0F, -4.0F, 10, 5, 12), PartPose.ZERO);
        addSidePair(chassis, "rack_rail", cube(DARK_U, DARK_V).addBox(0.0F, -49.0F, 26.0F, 1, 2, 20), 17.0F, -18.0F);
        chassis.addOrReplaceChild("rack_bar_f", cube(DARK_U, DARK_V).addBox(-18.0F, -49.0F, 24.0F, 36, 1, 2),
                PartPose.ZERO);
        chassis.addOrReplaceChild("rack_bar_r", cube(DARK_U, DARK_V).addBox(-18.0F, -49.0F, 46.0F, 36, 1, 2),
                PartPose.ZERO);

        // Bicicleta tumbada sobre el techo (pivotes centrados en y = -47.5 sobre la cubierta superior)
        chassis.addOrReplaceChild("bike_wheel_f", cube(146, 207).addBox(-0.5F, -3.0F, -3.0F, 1, 6, 6),
                PartPose.offsetAndRotation(-7.0F, -47.5F, 16.0F, 0.0F, 0.0F, (float) Math.PI / 2.0F));
        chassis.addOrReplaceChild("bike_wheel_r", cube(146, 207).addBox(-0.5F, -3.0F, -3.0F, 1, 6, 6),
                PartPose.offsetAndRotation(-7.0F, -47.5F, 27.0F, 0.0F, 0.0F, (float) Math.PI / 2.0F));
        chassis.addOrReplaceChild("bike_frame", cube(RED_U, RED_V).addBox(-10.0F, -48.0F, 16.0F, 8, 1, 11),
                PartPose.ZERO);
        chassis.addOrReplaceChild("bike_bar", cube(DARK_U, DARK_V).addBox(-10.0F, -49.0F, 15.0F, 8, 1, 1), PartPose.ZERO);

        // Bandera de Colombia en la esquina trasera del techo
        chassis.addOrReplaceChild("flag_pole", cube(DARK_U, DARK_V).addBox(18.5F, -62.0F, 48.0F, 1, 15, 1),
                PartPose.ZERO);
        chassis.addOrReplaceChild(FLAG, cube(121, 207).addBox(-0.5F, 0.0F, -10.5F, 1, 7, 11), at(19.0F, -62.0F, 48.5F));

        // 5. Interior: consola de timón, palanca y 6 asientos rojiazules
        chassis.addOrReplaceChild("helm_console", cube(DARK_U, DARK_V).addBox(4.0F, -24.0F, -23.0F, 12, 16, 6),
                PartPose.ZERO);
        chassis.addOrReplaceChild("steering_column", cube(DARK_U, DARK_V).addBox(9.0F, -21.0F, -17.0F, 2, 2, 4),
                PartPose.ZERO);
        chassis.addOrReplaceChild(STEERING_WHEEL, cube(161, 207).addBox(-3.5F, -3.5F, -0.5F, 7, 7, 1),
                at(10.0F, -20.0F, -13.0F));
        chassis.addOrReplaceChild("throttle_lever", cube(DARK_U, DARK_V).addBox(16.0F, -26.0F, -20.0F, 1, 5, 2),
                PartPose.ZERO);

        CubeListBuilder cushion = cube(0, 207).addBox(-6.0F, -13.0F, -5.0F, 12, 3, 10);
        CubeListBuilder back = cube(45, 207).addBox(-6.0F, -23.0F, 5.0F, 12, 10, 3);
        CubeListBuilder seatBase = cube(DARK_U, DARK_V).addBox(-5.0F, -10.0F, -4.0F, 10, 2, 8);
        float[] rowZ = {-6.0F, 14.0F, 34.0F};
        for (int r = 0; r < rowZ.length; r++) {
            for (int side = 0; side < 2; side++) {
                float sx = side == 0 ? 10.0F : -10.0F;
                String tag = r + "_" + (side == 0 ? "l" : "r");
                chassis.addOrReplaceChild("seat_base_" + tag, seatBase, at(sx, 0.0F, rowZ[r]));
                chassis.addOrReplaceChild("seat_cushion_" + tag, cushion, at(sx, 0.0F, rowZ[r]));
                chassis.addOrReplaceChild("seat_back_" + tag, back, at(sx, 0.0F, rowZ[r]));
            }
        }

        // 6. Motor fuera de borda Yamaha en la muesca del espejo (gira con el timón y su hélice rota)
        PartDefinition outboard = chassis.addOrReplaceChild(OUTBOARD, CubeListBuilder.create(), at(0.0F, -14.0F, 54.0F));
        outboard.addOrReplaceChild("motor_clamp", cube(DARK_U, DARK_V).addBox(-3.0F, -1.0F, -2.0F, 6, 5, 4),
                PartPose.ZERO);
        outboard.addOrReplaceChild("motor_cowling", cube(212, 111).addBox(-4.0F, -9.0F, 1.0F, 8, 11, 13),
                PartPose.ZERO);
        outboard.addOrReplaceChild("motor_leg", cube(DARK_U, DARK_V).addBox(-1.5F, 2.0F, 4.0F, 3, 16, 5),
                PartPose.ZERO);
        outboard.addOrReplaceChild("motor_skeg", cube(DARK_U, DARK_V).addBox(-1.0F, 15.0F, 3.0F, 2, 4, 6),
                PartPose.ZERO);
        PartDefinition propeller = outboard.addOrReplaceChild(PROPELLER, CubeListBuilder.create(), at(0.0F, 16.0F, 9.5F));
        propeller.addOrReplaceChild("prop_blade_a", cube(195, 207).addBox(-3.5F, -1.0F, -0.5F, 7, 2, 1),
                PartPose.ZERO);
        propeller.addOrReplaceChild("prop_blade_b", cube(195, 207).addBox(-3.5F, -1.0F, -0.5F, 7, 2, 1),
                PartPose.rotation(0.0F, 0.0F, (float) Math.PI / 2.0F));

        // 7. Vidrios translúcidos de cabina al final de chassis (altura completa hasta roof_bow)
        PartDefinition windshieldGlass = chassis.addOrReplaceChild("windshield_glass_group", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -24.0F, -26.0F, WINDSHIELD_RAKE, 0.0F, 0.0F));
        windshieldGlass.addOrReplaceChild("ws_glass_l", cube(212, 86).addBox(2.0F, -23.0F, 0.5F, 18, 23, 1),
                PartPose.ZERO);
        windshieldGlass.addOrReplaceChild("ws_glass_r", cube(212, 86).addBox(-20.0F, -23.0F, 0.5F, 18, 23, 1),
                PartPose.ZERO);

        addSidePair(chassis, "side_window_cabin", cube(143, 88).addBox(0.0F, -39.0F, -24.0F, 2, 15, 32),
                20.0F, -22.0F);

        return LayerDefinition.create(mesh, 256, 256);
    }

    /**
     * Submodelo para una capa ({@code BODY} o {@code TRIM}).
     * Anima balanceo en el agua, ralentí, bandera trasera, timón, giro del motor fuera de borda y rotación de hélice.
     */
    public static final class Part extends EntityModel<VehicleRenderState> {
        private final ModelPart chassis;
        private final ModelPart outboard;
        private final ModelPart propeller;
        private final ModelPart flag;
        private final ModelPart steeringWheel;

        public Part(ModelPart root) {
            super(root, RenderTypes::entityCutoutCull);
            this.chassis = root.getChild(CHASSIS);
            this.outboard = ModelKit.childOrNull(chassis, OUTBOARD);
            this.propeller = this.outboard != null ? ModelKit.childOrNull(this.outboard, PROPELLER) : null;
            this.flag = ModelKit.childOrNull(chassis, FLAG);
            this.steeringWheel = ModelKit.childOrNull(chassis, STEERING_WHEEL);
        }

        @Override
        public void setupAnim(VehicleRenderState state) {
            super.setupAnim(state);
            ModelKit.moveChassis(this.chassis, state, IDLE, 0.5F, BODY_ROLL_AT_FULL_STEER, MODEL_SCALE);

            if (this.outboard != null) {
                this.outboard.yRot = -state.steer;
            }
            if (this.propeller != null) {
                this.propeller.zRot = state.wheelRoll;
            }
            if (this.steeringWheel != null) {
                this.steeringWheel.zRot = -state.steer * 1.5F;
            }
            if (this.flag != null) {
                float swing = Mth.lerp(state.speedRatio, FLAG_SWING_IDLE, FLAG_SWING_FAST);
                this.flag.yRot = Mth.sin(state.ageInTicks * FLAG_SPEED) * swing;
            }
        }
    }

    private ChalupaModels() {}
}
