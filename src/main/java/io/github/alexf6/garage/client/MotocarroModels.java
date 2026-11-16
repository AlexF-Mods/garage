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
 * Modelo de Motocarro urbano (Bajaj RE / "Torito") sobre lienzo 256x256
 * (modelado a 25.6 px/bloque con escala visual {@value #MODEL_SCALE} para conservar tamaño real con mayor detalle):
 * <ul>
 *   <li>{@code BODY}: carrocería teñible (guardabarros delantero, carenado frontal esculpido, tabique divisorio,
 *       estribos, costados traseros, guardafangos y base de banca).</li>
 *   <li>{@code TRIM}: piso, parrilla frontal {@code RE} con doble faro redondo y direccionales ámbar, parabrisas
 *       inclinado translúcido con espejos, capota de lona negra con ventanas trasera y laterales, paneles acolchados
 *       cafés, compuerta trasera ventilada {@code RE}, pilotos traseros, asientos individuales y 3 ruedas 16x16.</li>
 * </ul>
 * Origen en el suelo ({@code y = 24}) y frente hacia {@code -Z}. Todas las piezas se apoyan borde con borde
 * sin caras coplanares para evitar z-fighting.
 */
public final class MotocarroModels {

    /** Escala visual del modelo en {@link ClientVehicles}: convierte 25.6 px/bloque a 16 px/bloque reales. */
    static final float MODEL_SCALE = 0.625F;

    public static final ModelLayerLocation BODY = layer("body");
    public static final ModelLayerLocation TRIM = layer("trim");
    /** Lo que {@link ClientVehicles} necesita para registrar este modelo. */
    static final ClientVehicles.ModelDef DEF = new ClientVehicles.ModelDef(BODY, TRIM, Part::new,
            MotocarroModels::createBodyLayer, MotocarroModels::createTrimLayer);

    public static final String[] WHEELS = {"wheel_front", "wheel_bl", "wheel_br"};
    // Ralentí: la vibración se reparte entre el balanceo lateral (zRot) y el cabeceo
    private static final ModelKit.IdleVibration IDLE = new ModelKit.IdleVibration(1.6F, 0.02F, 3.2F, 0.12F, 0.5F);
    private static final int FRONT_WHEELS = 1; // las primeras de WHEELS: la delantera gira con el manillar
    private static final String CHASSIS = "chassis";
    private static final String STEERING = "steering";

    /** Balanceo (rad) hacia fuera de la curva con la dirección y la velocidad al máximo; crece con la velocidad². */
    private static final float BODY_ROLL_AT_FULL_STEER = 0.22F;

    private static ModelLayerLocation layer(String name) {
        return ModelKit.layer("motocarro", name);
    }

    /** Capa BODY: paneles de carrocería que se tiñen con el color del motocarro (blanco glaciar por defecto). */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition chassis = root.addOrReplaceChild(CHASSIS, CubeListBuilder.create(), at(0, 24, 0));

        // Guardabarros delantero sobre la rueda única (gira junto con la dirección en X=0, Z=-24, bajo el carenado)
        PartDefinition steering = chassis.addOrReplaceChild(STEERING, CubeListBuilder.create(), at(0, -8, -24));
        steering.addOrReplaceChild("fender_front", cube(0, 0)
                .addBox(-4.0F, -10.5F, -7.0F, 8, 2.5F, 14), PartPose.ZERO);

        // Carenado frontal esculpido Bajaj RE: más bajo para amplia visión frontal del conductor
        chassis.addOrReplaceChild("nose_center", cube(46, 0)
                .addBox(-9.5F, -30.5F, -29.0F, 19, 12.0F, 5), PartPose.ZERO);
        chassis.addOrReplaceChild("nose_brow", cube(96, 0)
                .addBox(-10.5F, -32.0F, -28.0F, 21, 1.5F, 4), PartPose.ZERO);
        chassis.addOrReplaceChild("hood_side_l", cube(0, 28)
                .addBox(9.5F, -32.0F, -27.0F, 6.5F, 23, 9), PartPose.ZERO);
        chassis.addOrReplaceChild("hood_side_r", cube(0, 28)
                .addBox(-16.0F, -32.0F, -27.0F, 6.5F, 23, 9), PartPose.ZERO);

        // Tabique divisorio tras el conductor y estribos bajos de acceso
        chassis.addOrReplaceChild("divider", cube(34, 28)
                .addBox(-14.5F, -21.0F, -4.0F, 29, 12, 3), PartPose.ZERO);
        chassis.addOrReplaceChild("sill_l", cube(96, 8)
                .addBox(13.0F, -12.0F, -2.0F, 3, 3, 12), PartPose.ZERO);
        chassis.addOrReplaceChild("sill_r", cube(96, 8)
                .addBox(-16.0F, -12.0F, -2.0F, 3, 3, 12), PartPose.ZERO);

        // Costados traseros (interiores a las ruedas en X=±12..±14), arcos y espalda trasera
        chassis.addOrReplaceChild("rear_side_l", cube(104, 28)
                .addBox(12.0F, -25.5F, 10.0F, 2, 16.5F, 17.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("rear_side_r", cube(104, 28)
                .addBox(-14.0F, -25.5F, 10.0F, 2, 16.5F, 17.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("fender_bl_top", cube(34, 46)
                .addBox(14.0F, -20.5F, 10.0F, 5, 3, 18), PartPose.ZERO);
        chassis.addOrReplaceChild("fender_br_top", cube(34, 46)
                .addBox(-19.0F, -20.5F, 10.0F, 5, 3, 18), PartPose.ZERO);
        chassis.addOrReplaceChild("fender_bl_front", cube(82, 46)
                .addBox(14.0F, -17.5F, 8.5F, 5, 8.5F, 2), PartPose.ZERO);
        chassis.addOrReplaceChild("fender_br_front", cube(82, 46)
                .addBox(-19.0F, -17.5F, 8.5F, 5, 8.5F, 2), PartPose.ZERO);
        chassis.addOrReplaceChild("fender_bl_back", cube(82, 46)
                .addBox(14.0F, -17.5F, 27.5F, 5, 8.5F, 2), PartPose.ZERO);
        chassis.addOrReplaceChild("fender_br_back", cube(82, 46)
                .addBox(-19.0F, -17.5F, 27.5F, 5, 8.5F, 2), PartPose.ZERO);
        chassis.addOrReplaceChild("rear_back", cube(148, 28)
                .addBox(-12.0F, -25.5F, 24.5F, 24, 16.5F, 3), PartPose.ZERO);
        chassis.addOrReplaceChild("rear_seat_base", cube(148, 49)
                .addBox(-12.0F, -16.0F, 10.0F, 24, 7, 14.5F), PartPose.ZERO);

        return LayerDefinition.create(mesh, 256, 256);
    }

    /** Capa TRIM: piso, parrilla RE, parabrisas, capota con ventanas, interior acolchado café y 3 ruedas 16x16. */
    public static LayerDefinition createTrimLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition chassis = root.addOrReplaceChild(CHASSIS, CubeListBuilder.create(), at(0, 24, 0));

        // 1. Piso metálico antideslizante (detrás de la rueda delantera para evitar clipping al girar)
        chassis.addOrReplaceChild("floor", cube(0, 124)
                .addBox(-16.0F, -9.0F, -18.0F, 32, 3, 45.5F), PartPose.ZERO);

        // 2. Parrilla frontal Bajaj RE con doble faro redondo, emblema RE, direccionales ámbar y bómper
        chassis.addOrReplaceChild("grille_mask", cube(162, 80)
                .addBox(-9.5F, -29.5F, -30.5F, 19, 11, 1.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("blinker_front_l", cube(206, 80)
                .addBox(5.5F, -31.5F, -30.5F, 4, 2, 1.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("blinker_front_r", cube(206, 80)
                .addBox(-9.5F, -31.5F, -30.5F, 4, 2, 1.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("front_bumper", cube(162, 96)
                .addBox(-14.0F, -14.0F, -29.5F, 5, 3, 2.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("front_bumper_r", cube(162, 96)
                .addBox(9.0F, -14.0F, -29.5F, 5, 3, 2.5F), PartPose.ZERO);

        // 3. Compuerta trasera ventilada RE, luces traseras verticales y guardabarros traseros
        chassis.addOrReplaceChild("rear_hatch", cube(220, 80)
                .addBox(-6.0F, -23.5F, 27.5F, 12, 12, 1), PartPose.ZERO);
        chassis.addOrReplaceChild("taillight_l", cube(220, 96)
                .addBox(7.5F, -22.0F, 27.5F, 4, 5, 1), PartPose.ZERO);
        chassis.addOrReplaceChild("taillight_r", cube(220, 96)
                .addBox(-11.5F, -22.0F, 27.5F, 4, 5, 1), PartPose.ZERO);
        chassis.addOrReplaceChild("mudflap_l", cube(190, 200)
                .addBox(15.5F, -10.5F, 26.5F, 4, 4, 1), PartPose.ZERO);
        chassis.addOrReplaceChild("mudflap_r", cube(190, 200)
                .addBox(-19.5F, -10.5F, 26.5F, 4, 4, 1), PartPose.ZERO);

        // 4. Interior café: asientos individuales, banca trasera y laterales acolchados en rombos
        chassis.addOrReplaceChild("driver_pedestal", cube(190, 200)
                .addBox(-5.0F, -13.0F, -12.0F, 10, 4, 8), PartPose.ZERO);
        chassis.addOrReplaceChild("driver_cushion", cube(36, 200)
                .addBox(-5.5F, -16.0F, -13.0F, 11, 3, 10), PartPose.ZERO);
        chassis.addOrReplaceChild("driver_back", cube(80, 200)
                .addBox(-5.5F, -25.0F, -5.0F, 11, 9, 2), PartPose.ZERO);
        chassis.addOrReplaceChild("cushion_bl", cube(108, 200)
                .addBox(1.0F, -19.0F, 10.5F, 11.5F, 3, 12), PartPose.ZERO);
        chassis.addOrReplaceChild("cushion_br", cube(108, 200)
                .addBox(-12.5F, -19.0F, 10.5F, 11.5F, 3, 12), PartPose.ZERO);
        chassis.addOrReplaceChild("back_bl", cube(158, 200)
                .addBox(1.0F, -30.0F, 22.5F, 11.5F, 11, 2.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("back_br", cube(158, 200)
                .addBox(-12.5F, -30.0F, 22.5F, 11.5F, 11, 2.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("quilt_panel_l", cube(212, 154)
                .addBox(13.0F, -22.0F, -2.0F, 2, 10, 12), PartPose.ZERO);
        chassis.addOrReplaceChild("quilt_panel_r", cube(212, 154)
                .addBox(-15.0F, -22.0F, -2.0F, 2, 10, 12), PartPose.ZERO);

        // Consola frontal y manillar de dirección (pivote en X=0, Z=-24) conectado con su tija
        chassis.addOrReplaceChild("dashboard", cube(36, 216)
                .addBox(-7.0F, -27.0F, -24.0F, 14, 4, 4), PartPose.ZERO);
        PartDefinition steering = chassis.addOrReplaceChild(STEERING, CubeListBuilder.create(), at(0, -8, -24));
        steering.addOrReplaceChild("fork_bridge", cube(190, 200)
                .addBox(-3.5F, -9.0F, -1.0F, 7, 1.5F, 6), PartPose.ZERO);
        steering.addOrReplaceChild("fork_col", cube(190, 200)
                .addBox(-1.5F, -17.5F, 3.0F, 3, 9, 2.5F), PartPose.ZERO);
        steering.addOrReplaceChild("handlebar", cube(190, 200)
                .addBox(-8.0F, -18.5F, 3.5F, 16, 1.5F, 1.5F), PartPose.ZERO);
        steering.addOrReplaceChild("nacelle", cube(74, 216)
                .addBox(-3.0F, -19.5F, 2.5F, 6, 3, 3), PartPose.ZERO);
        steering.addOrReplaceChild("fork_arm_l", cube(190, 200)
                .addBox(2.2F, -7.5F, -1.0F, 1.5F, 8.5F, 2), PartPose.ZERO);
        steering.addOrReplaceChild("fork_arm_r", cube(190, 200)
                .addBox(-3.7F, -7.5F, -1.0F, 1.5F, 8.5F, 2), PartPose.ZERO);

        // 5. Estructura continua de capota: parales completos hasta el piso (Y=-51.5..-9.0) y techo alto (2.01 bloques)
        chassis.addOrReplaceChild("mid_pillar_l", cube(190, 200)
                .addBox(14.5F, -51.5F, -4.0F, 1.5F, 42.5F, 2), PartPose.ZERO);
        chassis.addOrReplaceChild("mid_pillar_r", cube(190, 200)
                .addBox(-16.0F, -51.5F, -4.0F, 1.5F, 42.5F, 2), PartPose.ZERO);
        chassis.addOrReplaceChild("top_side_rail_l", cube(190, 200)
                .addBox(14.2F, -51.5F, -17.0F, 1.8F, 1.5F, 27.0F), PartPose.ZERO);
        chassis.addOrReplaceChild("top_side_rail_r", cube(190, 200)
                .addBox(-16.0F, -51.5F, -17.0F, 1.8F, 1.5F, 27.0F), PartPose.ZERO);
        chassis.addOrReplaceChild("roof_main", cube(0, 72)
                .addBox(-16.5F, -54.5F, -18.5F, 33, 3, 46.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("roof_visor_front", cube(162, 72)
                .addBox(-16.0F, -54.0F, -21.5F, 32, 2.5F, 3), PartPose.ZERO);
        chassis.addOrReplaceChild("roof_rear_cap", cube(162, 72)
                .addBox(-16.0F, -54.0F, 28.0F, 32, 2.5F, 2), PartPose.ZERO);

        // 6. Parabrisas amplio y ventanas laterales/traseras translúcidas al final de chassis
        PartDefinition windshield = chassis.addOrReplaceChild("windshield_group", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -31.5F, -24.0F, -0.335F, 0.0F, 0.0F));
        windshield.addOrReplaceChild("ws_post_l", cube(190, 200)
                .addBox(14.2F, -21.5F, 0.0F, 1.8F, 21.5F, 2), PartPose.ZERO);
        windshield.addOrReplaceChild("ws_post_r", cube(190, 200)
                .addBox(-16.0F, -21.5F, 0.0F, 1.8F, 21.5F, 2), PartPose.ZERO);
        windshield.addOrReplaceChild("ws_top_bar", cube(190, 200)
                .addBox(-14.2F, -21.5F, 0.0F, 28.4F, 1.5F, 2), PartPose.ZERO);
        windshield.addOrReplaceChild("ws_bot_bar", cube(190, 200)
                .addBox(-14.2F, -1.5F, 0.0F, 28.4F, 1.5F, 2), PartPose.ZERO);
        windshield.addOrReplaceChild("mirror_arm_l", cube(190, 200)
                .addBox(16.0F, -12.0F, 0.5F, 2.0F, 1, 1), PartPose.ZERO);
        windshield.addOrReplaceChild("mirror_arm_r", cube(190, 200)
                .addBox(-18.0F, -12.0F, 0.5F, 2.0F, 1, 1), PartPose.ZERO);
        windshield.addOrReplaceChild("mirror_l", cube(190, 200)
                .addBox(17.5F, -14.5F, 0.0F, 2.0F, 3.5F, 1.5F), PartPose.ZERO);
        windshield.addOrReplaceChild("mirror_r", cube(190, 200)
                .addBox(-19.5F, -14.5F, 0.0F, 2.0F, 3.5F, 1.5F), PartPose.ZERO);
        windshield.addOrReplaceChild("ws_glass", cube(162, 104)
                .addBox(-14.2F, -20.0F, 0.5F, 28.4F, 18.5F, 1), PartPose.ZERO);

        chassis.addOrReplaceChild("side_canvas_l", cube(170, 154)
                .addBox(14.0F, -51.5F, 10.0F, 2, 26, 17.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("side_canvas_r", cube(170, 154)
                .addBox(-16.0F, -51.5F, 10.0F, 2, 26, 17.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("rear_canvas_back", cube(170, 124)
                .addBox(-16.0F, -51.5F, 27.5F, 32, 26, 1.5F), PartPose.ZERO);

        // 7. Tres ruedas Bajaj 16x16 (radio 8 px → 8 * 0.625 / 16 = 0.3125 bloques reales, centro en y = 16).
        // Octágono regular exacto (R_plano = 7.40, semiancho 3.06 -> esquina 8.01): borde continuo sin dientes.
        CubeListBuilder tread0 = cube(192, 0)
                .addBox(-2.00F, -7.40F, -3.06F, 4.0F, 3.00F, 6.12F)
                .addBox(-2.00F, 4.40F, -3.06F, 4.0F, 3.00F, 6.12F)
                .addBox(-1.95F, -3.06F, -7.40F, 3.9F, 6.12F, 3.00F)
                .addBox(-1.95F, -3.06F, 4.40F, 3.9F, 6.12F, 3.00F);
        CubeListBuilder tread45 = cube(192, 0)
                .addBox(-1.90F, -7.40F, -3.06F, 3.8F, 3.00F, 6.12F)
                .addBox(-1.90F, 4.40F, -3.06F, 3.8F, 3.00F, 6.12F)
                .addBox(-1.85F, -3.06F, -7.40F, 3.7F, 6.12F, 3.00F)
                .addBox(-1.85F, -3.06F, 4.40F, 3.7F, 6.12F, 3.00F);
        CubeListBuilder hubs = cube(0, 200)
                .addBox(1.85F, -8.0F, -8.0F, 0.2F, 16, 16)
                .addBox(-2.05F, -8.0F, -8.0F, 0.2F, 16, 16);

        float[][] wheelPos = {{0.0F, -24.0F}, {16.5F, 19.0F}, {-16.5F, 19.0F}};
        for (int i = 0; i < WHEELS.length; i++) {
            ModelKit.addWheel(root, WHEELS[i], at(wheelPos[i][0], 16.0F, wheelPos[i][1]), tread0, tread45, hubs);
        }

        return LayerDefinition.create(mesh, 256, 256);
    }

    /**
     * Submodelo para una capa (BODY o TRIM).
     * Aplica la cinemática de triciclo Delta, balanceo centrífugo, cabeceo de suspensión y ralentí.
     */
    public static final class Part extends EntityModel<VehicleRenderState> {
        private final ModelPart chassis;
        private final ModelPart steering;
        private final ModelPart[] wheels;

        public Part(ModelPart root) {
            super(root, RenderTypes::entityTranslucentCull);
            this.chassis = root.getChild(CHASSIS);
            this.steering = ModelKit.childOrNull(chassis, STEERING);
            this.wheels = ModelKit.wheels(root, WHEELS);
        }

        @Override
        public void setupAnim(VehicleRenderState state) {
            super.setupAnim(state);

            // Ruedas en el suelo: las 3 ruedan y la delantera (índice 0) gira con el manillar
            ModelKit.spinWheels(wheels, state.wheelRoll, state.steer, FRONT_WHEELS);

            // Dirección del manillar y guardabarros delantero
            if (this.steering != null) {
                this.steering.yRot = state.steer;
            }

            // Suspensión, ralentí y balanceo centrífugo escalados al tamaño visual del modelo
            float heavePx = ModelKit.heavePx(state.bodyHeave) / MODEL_SCALE;
            if (state.isEngineRunning && state.speedRatio < ModelKit.IDLE_SPEED_RATIO) {
                float idleBuzz = IDLE.sway(state.ageInTicks);
                this.chassis.y = ModelKit.GROUND_Y_PX + heavePx + IDLE.bobPx(state.ageInTicks) / MODEL_SCALE;
                this.chassis.xRot = state.bodyPitch + idleBuzz * IDLE.pitchShare();
                this.chassis.zRot = idleBuzz;
            } else {
                this.chassis.y = ModelKit.GROUND_Y_PX + heavePx;
                this.chassis.xRot = state.bodyPitch;
                this.chassis.zRot = -state.steer * (state.speedRatio * state.speedRatio) * BODY_ROLL_AT_FULL_STEER;
            }
        }
    }

    private MotocarroModels() {}
}
