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
 * Modelo de Motocarguero 200cc sobre lienzo 256x256
 * (modelado a 25.6 px/bloque con escala visual {@value #MODEL_SCALE} para conservar tamaño real con mayor detalle):
 * <ul>
 *   <li>{@code BODY}: tanque esculpido, faldón bajo el sillín y careta superior teñibles.</li>
 *   <li>{@code TRIM}: insignia {@code 200} en el tanque, motor monocilíndrico aletado 200cc expuesto con eje cardán
 *       y diferencial, escape, sillín, mampara metálica calada con respaldo acolchado, platón de acero corrugado con
 *       barandillas tubulares y compuerta con pasadores, guardafangos cromados curvos con faldones negros y 3 ruedas
 *       18x18 (delantera plateada con tapacubos rojo y traseras negras de 5 pernos).</li>
 * </ul>
 * Origen en el suelo ({@code y = 24}) y frente hacia {@code -Z}. Todas las piezas evitan caras coplanares y las
 * 3 ruedas cuelgan de {@code root} para permanecer siempre apoyadas sobre el suelo.
 */
public final class MotocargueroModels {

    /** Escala visual del modelo en {@link ClientVehicles}: convierte 25.6 px/bloque a 16 px/bloque reales. */
    static final float MODEL_SCALE = 0.625F;

    public static final ModelLayerLocation BODY = layer("body");
    public static final ModelLayerLocation TRIM = layer("trim");
    /** Lo que {@link ClientVehicles} necesita para registrar este modelo. */
    static final ClientVehicles.ModelDef DEF = new ClientVehicles.ModelDef(BODY, TRIM, Part::new,
            MotocargueroModels::createBodyLayer, MotocargueroModels::createTrimLayer);

    private static final String CHASSIS = "chassis";
    private static final String STEERING = "steering";
    private static final String[] WHEELS = {"wheel_front", "wheel_bl", "wheel_br"};
    private static final int FRONT_WHEELS = 1;

    /** Inclinación (rake) de la horquilla delantera de carga: -14° (-0.24 rad) hacia el frente (-Z). */
    private static final float FORK_RAKE = -0.24F;

    private static final ModelKit.IdleVibration IDLE = new ModelKit.IdleVibration(1.0F, 0.005F, 2.0F, 0.03F, 0.4F);
    private static final float IDLE_ROLL_SHARE = 0.45F;
    private static final float IDLE_STEER_SWAY_SPEED = 1.1F;
    private static final float IDLE_STEER_SWAY_RAD = 0.006F;
    /** Balanceo centrífugo hacia el exterior de la curva en virajes rápidos. */
    private static final float BODY_ROLL_AT_FULL_STEER = 0.18F;

    private static final int DARK_U = 130;
    private static final int DARK_V = 140;
    private static final int CHROME_U = 130;
    private static final int CHROME_V = 184;
    private static final ModelKit.OctagonWheel WHEEL_RING = new ModelKit.OctagonWheel(192, 0, 4.4F, 8.32F, 4.60F, 3.45F);

    private record WheelMount(String name, float x, float z, int hubsU, int hubsV) {}

    private static final WheelMount[] WHEEL_MOUNTS = {
            new WheelMount(WHEELS[0], 0.0F, -24.0F, 40, 136),
            new WheelMount(WHEELS[1], 19.0F, 25.0F, 0, 136),
            new WheelMount(WHEELS[2], -19.0F, 25.0F, 0, 136)
    };

    private static ModelLayerLocation layer(String name) {
        return ModelKit.layer("motocarguero", name);
    }

    /** Capa BODY: tanque esculpido, faldones bajo sillín y careta superior teñibles (blanco glaciar por defecto). */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition chassis = root.addOrReplaceChild(CHASSIS, CubeListBuilder.create(), at(0, 24, 0));

        // Tanque de combustible esculpido (8x6x11 exacto UV, deja expuesto el motor abajo) y faldones traseros
        chassis.addOrReplaceChild("fuel_tank", cube(0, 0)
                .addBox(-4.0F, -28.0F, -14.5F, 8, 6, 11), PartPose.ZERO);
        chassis.addOrReplaceChild("tank_rear_slope", cube(50, 0)
                .addBox(-3.5F, -25.0F, -3.5F, 7, 3, 4), PartPose.ZERO);
        chassis.addOrReplaceChild("under_seat_l", cube(50, 0)
                .addBox(3.5F, -22.5F, -3.5F, 1, 6, 9), PartPose.ZERO);
        chassis.addOrReplaceChild("under_seat_r", cube(50, 0)
                .addBox(-4.5F, -22.5F, -3.5F, 1, 6, 9), PartPose.ZERO);

        // Careta superior tras el faro (gira con la dirección, despejada de las tijas y barras de horquilla)
        PartDefinition steering = chassis.addOrReplaceChild(STEERING, CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -25.5F, -20.0F, FORK_RAKE, 0.0F, 0.0F));
        steering.addOrReplaceChild("headlight_cowl", cube(120, 0)
                .addBox(-3.2F, -4.5F, -2.8F, 6.4F, 5, 1.5F), PartPose.ZERO);

        return LayerDefinition.create(mesh, 256, 256);
    }

    /** Capa TRIM: chasis, motor 200cc expuesto con cardán, platón corrugado con reja, guardafangos cromados y ruedas. */
    public static LayerDefinition createTrimLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition chassis = root.addOrReplaceChild(CHASSIS, CubeListBuilder.create(), at(0, 24, 0));

        // 1. Chasis tubular reforzado, pipa de dirección, cuna, eje cardán, diferencial y ballestas
        chassis.addOrReplaceChild("head_tube", cube(DARK_U, DARK_V)
                .addBox(-1.6F, -2.5F, -1.5F, 3.2F, 7.5F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -24.5F, -19.5F, FORK_RAKE, 0.0F, 0.0F));
        chassis.addOrReplaceChild("backbone_tube", cube(DARK_U, DARK_V)
                .addBox(-1.8F, -23.0F, -18.5F, 3.6F, 2.5F, 18.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("downtube_l", cube(DARK_U, DARK_V)
                .addBox(2.0F, -22.5F, -16.0F, 1.4F, 12.0F, 1.8F), PartPose.ZERO);
        chassis.addOrReplaceChild("downtube_r", cube(DARK_U, DARK_V)
                .addBox(-3.4F, -22.5F, -16.0F, 1.4F, 12.0F, 1.8F), PartPose.ZERO);
        chassis.addOrReplaceChild("lower_cradle", cube(DARK_U, DARK_V)
                .addBox(-3.8F, -10.5F, -15.5F, 7.6F, 2.0F, 23.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("rear_chassis_l", cube(DARK_U, DARK_V)
                .addBox(9.0F, -11.0F, 8.0F, 2.5F, 2.5F, 28.0F), PartPose.ZERO);
        chassis.addOrReplaceChild("rear_chassis_r", cube(DARK_U, DARK_V)
                .addBox(-11.5F, -11.0F, 8.0F, 2.5F, 2.5F, 28.0F), PartPose.ZERO);
        chassis.addOrReplaceChild("rear_crossmember", cube(DARK_U, DARK_V)
                .addBox(-11.5F, -11.0F, 36.0F, 23, 2.5F, 2.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("driveshaft", cube(CHROME_U, CHROME_V)
                .addBox(-1.1F, -10.2F, 0.0F, 2.2F, 2.2F, 22.0F), PartPose.ZERO);
        chassis.addOrReplaceChild("rear_axle", cube(DARK_U, DARK_V)
                .addBox(-16.6F, -10.7F, 23.5F, 33.2F, 3.0F, 3.0F), PartPose.ZERO);
        chassis.addOrReplaceChild("differential", cube(DARK_U, DARK_V)
                .addBox(-3.5F, -12.0F, 22.0F, 7, 5.5F, 6), PartPose.ZERO);
        chassis.addOrReplaceChild("leaf_spring_l", cube(DARK_U, DARK_V)
                .addBox(9.0F, -12.5F, 18.0F, 2.5F, 1.5F, 14), PartPose.ZERO);
        chassis.addOrReplaceChild("leaf_spring_r", cube(DARK_U, DARK_V)
                .addBox(-11.5F, -12.5F, 18.0F, 2.5F, 1.5F, 14), PartPose.ZERO);

        // 2. Motor monocilíndrico aletado 200cc expuesto, escape lateral y numeral legible "200" en tanque
        chassis.addOrReplaceChild("engine_crankcase", cube(110, 24)
                .addBox(-4.5F, -16.0F, -12.5F, 9, 6, 8), PartPose.ZERO);
        chassis.addOrReplaceChild("engine_cylinder", cube(72, 24)
                .addBox(-3.5F, -22.0F, -13.0F, 7, 6, 8), PartPose.ZERO);
        chassis.addOrReplaceChild("exhaust_header", cube(DARK_U, DARK_V)
                .addBox(3.8F, -19.5F, -12.0F, 1.6F, 9.0F, 1.8F), PartPose.ZERO);
        chassis.addOrReplaceChild("exhaust_muffler", cube(80, 136)
                .addBox(4.0F, -10.5F, -7.5F, 3, 3, 18), PartPose.ZERO);
        chassis.addOrReplaceChild("footpeg_l", cube(DARK_U, DARK_V)
                .addBox(4.5F, -10.0F, -4.5F, 5.5F, 1.5F, 2.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("footpeg_r", cube(DARK_U, DARK_V)
                .addBox(-10.0F, -10.0F, -4.5F, 5.5F, 1.5F, 2.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("tank_decal_l", cube(0, 24)
                .addBox(3.05F, -28.0F, -14.0F, 1, 6, 11), PartPose.ZERO);
        chassis.addOrReplaceChild("tank_decal_r", cube(0, 24)
                .addBox(-4.05F, -28.0F, -14.0F, 1, 6, 11), PartPose.ZERO);

        // 3. Sillín escalonado del conductor (tope en y = -23 px = 0.90 bloques, z = -1..9)
        chassis.addOrReplaceChild("driver_saddle", cube(34, 24)
                .addBox(-4.0F, -23.0F, -1.0F, 8, 4, 10), PartPose.ZERO);
        chassis.addOrReplaceChild("saddle_support", cube(DARK_U, DARK_V)
                .addBox(-3.4F, -19.0F, 0.5F, 6.8F, 8.5F, 7.5F), PartPose.ZERO);

        // 4. Platón de acero corrugado con barandillas continuas hasta la mampara (z = 9..39)
        chassis.addOrReplaceChild("bed_floor", cube(0, 70)
                .addBox(-15.0F, -13.0F, 10.0F, 30, 2, 29), PartPose.ZERO);
        chassis.addOrReplaceChild("bed_front_wall", cube(0, 104)
                .addBox(-15.0F, -21.0F, 8.0F, 30, 8, 2), PartPose.ZERO);
        chassis.addOrReplaceChild("bed_side_wall_l", cube(120, 70)
                .addBox(14.0F, -21.0F, 10.0F, 2, 8, 29), PartPose.ZERO);
        chassis.addOrReplaceChild("bed_side_wall_r", cube(120, 70)
                .addBox(-16.0F, -21.0F, 10.0F, 2, 8, 29), PartPose.ZERO);
        chassis.addOrReplaceChild("bed_rail_l", cube(184, 70)
                .addBox(14.0F, -28.0F, 9.0F, 2, 7, 30), PartPose.ZERO);
        chassis.addOrReplaceChild("bed_rail_r", cube(184, 70)
                .addBox(-16.0F, -28.0F, 9.0F, 2, 7, 30), PartPose.ZERO);
        chassis.addOrReplaceChild("cab_guard_grid", cube(0, 116)
                .addBox(-14.0F, -36.0F, 8.5F, 28, 15, 1), PartPose.ZERO);
        chassis.addOrReplaceChild("backrest", cube(34, 40)
                .addBox(-4.5F, -31.5F, 5.5F, 9, 8, 3), PartPose.ZERO);
        chassis.addOrReplaceChild("tailgate_panel", cube(66, 108)
                .addBox(-15.0F, -21.0F, 39.0F, 30, 8, 2), PartPose.ZERO);
        chassis.addOrReplaceChild("tailgate_rail", cube(66, 120)
                .addBox(-15.0F, -28.0F, 39.5F, 30, 7, 1), PartPose.ZERO);
        chassis.addOrReplaceChild("taillight_bar", cube(134, 44)
                .addBox(-12.0F, -11.0F, 39.0F, 24, 3, 2), PartPose.ZERO);

        // 5. Guardafangos traseros cromados en arco con faldones negros de goma (sobre ruedas en X=±19, Z=25)
        chassis.addOrReplaceChild("fender_arch_bl", cube(CHROME_U, CHROME_V)
                .addBox(16.0F, -20.5F, 15.5F, 5.5F, 2, 19), PartPose.ZERO);
        chassis.addOrReplaceChild("fender_arch_br", cube(CHROME_U, CHROME_V)
                .addBox(-21.5F, -20.5F, 15.5F, 5.5F, 2, 19), PartPose.ZERO);
        chassis.addOrReplaceChild("mudflap_bl_f", cube(DARK_U, DARK_V)
                .addBox(16.0F, -18.5F, 14.0F, 5.5F, 10.5F, 1.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("mudflap_br_f", cube(DARK_U, DARK_V)
                .addBox(-21.5F, -18.5F, 14.0F, 5.5F, 10.5F, 1.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("mudflap_bl_r", cube(DARK_U, DARK_V)
                .addBox(16.0F, -18.5F, 34.5F, 5.5F, 10.5F, 1.5F), PartPose.ZERO);
        chassis.addOrReplaceChild("mudflap_br_r", cube(DARK_U, DARK_V)
                .addBox(-21.5F, -18.5F, 34.5F, 5.5F, 10.5F, 1.5F), PartPose.ZERO);

        // 6. Tren delantero: tijas interiores (X=±3.2), barras cromadas, arco curvo, faro y direccionales pegadas
        PartDefinition steering = chassis.addOrReplaceChild(STEERING, CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -25.5F, -20.0F, FORK_RAKE, 0.0F, 0.0F));

        steering.addOrReplaceChild("triple_clamp_top", cube(DARK_U, DARK_V)
                .addBox(-3.2F, -1.0F, -1.2F, 6.4F, 2, 2.4F), PartPose.ZERO);
        steering.addOrReplaceChild("triple_clamp_bot", cube(DARK_U, DARK_V)
                .addBox(-3.2F, 5.0F, -1.2F, 6.4F, 2, 2.4F), PartPose.ZERO);
        steering.addOrReplaceChild("fork_leg_l", cube(CHROME_U, CHROME_V)
                .addBox(3.2F, -3.0F, -1.0F, 2, 20, 2), PartPose.ZERO);
        steering.addOrReplaceChild("fork_leg_r", cube(CHROME_U, CHROME_V)
                .addBox(-5.2F, -3.0F, -1.0F, 2, 20, 2), PartPose.ZERO);

        // Guardafango delantero curvo en 3 tramos cromados (laterales escalonados) + faldón negro abrazando el neumático
        steering.addOrReplaceChild("fender_front_top", cube(CHROME_U, CHROME_V)
                .addBox(-3.1F, 5.8F, -4.0F, 6.2F, 1.5F, 8), PartPose.ZERO);
        steering.addOrReplaceChild("fender_front_beak", cube(CHROME_U, CHROME_V)
                .addBox(-2.8F, 0.0F, -4.0F, 5.6F, 1.5F, 4),
                PartPose.offsetAndRotation(0.0F, 5.8F, -4.0F, 0.48F, 0.0F, 0.0F));
        PartDefinition frontFenderRear = steering.addOrReplaceChild("fender_front_rear", cube(CHROME_U, CHROME_V)
                .addBox(-2.8F, 0.0F, 0.0F, 5.6F, 1.5F, 4.5F),
                PartPose.offsetAndRotation(0.0F, 5.8F, 4.0F, -0.52F, 0.0F, 0.0F));
        frontFenderRear.addOrReplaceChild("fender_front_flap", cube(DARK_U, DARK_V)
                .addBox(-2.6F, 1.5F, 2.5F, 5.2F, 4.5F, 1.2F), PartPose.ZERO);

        steering.addOrReplaceChild("headlight", cube(148, 24)
                .addBox(-3.0F, -4.5F, -5.8F, 6, 6, 3), PartPose.ZERO);
        steering.addOrReplaceChild("front_blinker_l", cube(148, 36)
                .addBox(3.2F, -3.0F, -3.0F, 2.4F, 2, 2), PartPose.ZERO);
        steering.addOrReplaceChild("front_blinker_r", cube(148, 36)
                .addBox(-5.6F, -3.0F, -3.0F, 2.4F, 2, 2), PartPose.ZERO);

        steering.addOrReplaceChild("riser_l", cube(DARK_U, DARK_V)
                .addBox(1.8F, -5.0F, -0.6F, 1.2F, 4.5F, 1.2F), PartPose.rotation(-FORK_RAKE, 0.0F, 0.0F));
        steering.addOrReplaceChild("riser_r", cube(DARK_U, DARK_V)
                .addBox(-3.0F, -5.0F, -0.6F, 1.2F, 4.5F, 1.2F), PartPose.rotation(-FORK_RAKE, 0.0F, 0.0F));
        steering.addOrReplaceChild("gauge_pod", cube(168, 24)
                .addBox(-2.5F, -5.5F, -1.5F, 5, 3, 3), PartPose.rotation(-FORK_RAKE, 0.0F, 0.0F));
        steering.addOrReplaceChild("handlebars", cube(DARK_U, DARK_V)
                .addBox(-11.5F, -6.0F, -0.5F, 23, 1.8F, 1.8F), PartPose.rotation(-FORK_RAKE, 0.0F, 0.0F));
        steering.addOrReplaceChild("mirror_stalk_l", cube(DARK_U, DARK_V)
                .addBox(8.5F, -10.0F, -0.2F, 1.2F, 4.5F, 1), PartPose.rotation(-FORK_RAKE, 0.0F, 0.0F));
        steering.addOrReplaceChild("mirror_stalk_r", cube(DARK_U, DARK_V)
                .addBox(-9.7F, -10.0F, -0.2F, 1.2F, 4.5F, 1), PartPose.rotation(-FORK_RAKE, 0.0F, 0.0F));
        steering.addOrReplaceChild("mirror_l", cube(DARK_U, DARK_V)
                .addBox(8.0F, -12.5F, -0.5F, 3.5F, 2.5F, 1), PartPose.rotation(-FORK_RAKE, 0.0F, 0.0F));
        steering.addOrReplaceChild("mirror_r", cube(DARK_U, DARK_V)
                .addBox(-11.5F, -12.5F, -0.5F, 3.5F, 2.5F, 1), PartPose.rotation(-FORK_RAKE, 0.0F, 0.0F));

        // 7. Tres ruedas 18x18 colgando de root (radio 9 px → 0.3516 bloques, apoyadas en y = 24)
        for (WheelMount mount : WHEEL_MOUNTS) {
            CubeListBuilder hubs = cube(mount.hubsU(), mount.hubsV())
                    .addBox(2.25F, -9.0F, -9.0F, 0.2F, 18, 18)
                    .addBox(-2.45F, -9.0F, -9.0F, 0.2F, 18, 18);
            ModelKit.addOctagonWheel(root, mount.name(), at(mount.x(), 15.0F, mount.z()), WHEEL_RING, hubs);
        }

        return LayerDefinition.create(mesh, 256, 256);
    }

    /**
     * Submodelo para una capa ({@code BODY} o {@code TRIM}).
     * Aplica dirección delantera, rodadura continua, balanceo en curva y ralentí suave del motor 200cc.
     */
    public static final class Part extends EntityModel<VehicleRenderState> {
        private final ModelPart chassis;
        private final ModelPart steering;
        private final ModelPart[] wheels;

        public Part(ModelPart root) {
            super(root, RenderTypes::entityCutoutCull);
            this.chassis = root.getChild(CHASSIS);
            this.steering = ModelKit.childOrNull(chassis, STEERING);
            this.wheels = ModelKit.wheels(root, WHEELS);
        }

        @Override
        public void setupAnim(VehicleRenderState state) {
            super.setupAnim(state);

            ModelKit.spinWheels(wheels, state.wheelRoll, state.steer, FRONT_WHEELS);
            if (this.steering != null) {
                this.steering.xRot = FORK_RAKE;
                this.steering.yRot = state.steer;
            }

            if (ModelKit.moveChassis(this.chassis, state, IDLE, IDLE_ROLL_SHARE, BODY_ROLL_AT_FULL_STEER, MODEL_SCALE)
                    && this.steering != null) {
                this.steering.yRot = state.steer + Mth.cos(state.ageInTicks * IDLE_STEER_SWAY_SPEED) * IDLE_STEER_SWAY_RAD;
            }
        }
    }

    private MotocargueroModels() {}
}
