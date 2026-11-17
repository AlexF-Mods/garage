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
 * Modelo de Motocicleta Doble Propósito / Enduro ligera (Yamaha XTZ 125) en dos capas sobre lienzo 256x256
 * (modelado a 26.67 px/bloque con escala visual {@value #MODEL_SCALE} para conservar tamaño real con mayor detalle):
 * <ul>
 *   <li>{@code BODY}: plásticos teñibles (tanque y aletas laterales esculpidas, colín trasero, careta frontal y
 *       guardabarros delantero alto "pico de pato").</li>
 *   <li>{@code TRIM}: chasis perimetral negro, caja de aire, monoshock, motor monocilíndrico aletado con tapa
 *       redonda {@code YAMAHA}, calcomanías {@code XTZ} en aletas, tapas blancas {@code MONOCROSS}, sillín corrido,
 *       placa trasera con bandera de Colombia, escape elevado con escudo plateado, basculante de
 *       aluminio {@code YAMAHA}, horquilla con avance (-24°), fuelles acanalados, manillar con barra transversal
 *       y ruedas huecas de radios 20x20 con freno de disco.</li>
 * </ul>
 * Origen en el suelo ({@code y = 24}) y frente hacia {@code -Z}.
 */
public final class MotorcycleModels {

    /** Escala visual del modelo en {@link ClientVehicles}: convierte 26.67 px/bloque a 16 px/bloque reales. */
    static final float MODEL_SCALE = 0.60F;

    public static final ModelLayerLocation BODY = layer("body");
    public static final ModelLayerLocation TRIM = layer("trim");
    /** Lo que {@link ClientVehicles} necesita para registrar este modelo. */
    static final ClientVehicles.ModelDef DEF = new ClientVehicles.ModelDef(BODY, TRIM, Part::new,
            MotorcycleModels::createBodyLayer, MotorcycleModels::createTrimLayer);

    private static final String LEAN_ROOT = "lean_root";
    private static final String CHASSIS = "chassis";
    private static final String STEERING = "steering";
    private static final String FRONT_WHEEL = "front_wheel";
    private static final String SWINGARM = "swingarm";
    private static final String REAR_WHEEL = "rear_wheel";

    // Ralentí suave del monocilíndrico: ligera pulsación vertical y vaivén mínimo al estar detenida
    private static final ModelKit.IdleVibration IDLE = new ModelKit.IdleVibration(0.9F, 0.005F, 1.8F, 0.025F, 0.35F);

    /** Ángulo de avance (rake) de la horquilla Enduro: -24° (-0.42 rad), inclina las barras hacia adelante (-Z). */
    private static final float FORK_RAKE = -0.42F;

    private static final int FRAME_U = 44;
    private static final int FRAME_V = 96;
    private static final ModelKit.OctagonWheel WHEEL_RING = new ModelKit.OctagonWheel(192, 0, 3.1F, 9.25F, 7.20F, 3.83F);

    private static ModelLayerLocation layer(String name) {
        return ModelKit.layer("motorcycle", name);
    }

    /**
     * Capa BODY: plásticos teñibles (color por defecto {@code #1846C7} Yamaha Racing Blue).
     * Incluye tanque, aletas laterales, colín trasero, careta del faro y guardabarros pico de pato.
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition leanRoot = root.addOrReplaceChild(LEAN_ROOT, CubeListBuilder.create(), at(0.0F, 24.0F, 0.0F));
        PartDefinition chassis = leanRoot.addOrReplaceChild(CHASSIS, CubeListBuilder.create(), PartPose.ZERO);

        // 1. Tanque, aletas delanteras angulares (XTZ) y colín trasero deportivo
        chassis.addOrReplaceChild("tank_center", cube(0, 0)
                .addBox(-4.0F, -32.0F, -10.5F, 8, 7, 9), PartPose.ZERO);
        chassis.addOrReplaceChild("tank_shroud_l", cube(36, 0)
                .addBox(0.0F, 0.0F, -5.5F, 2, 8, 11),
                PartPose.offsetAndRotation(3.5F, -31.0F, -8.5F, 0.25F, 0.0F, 0.0F));
        chassis.addOrReplaceChild("tank_shroud_r", cube(36, 0)
                .addBox(-2.0F, 0.0F, -5.5F, 2, 8, 11),
                PartPose.offsetAndRotation(-3.5F, -31.0F, -8.5F, 0.25F, 0.0F, 0.0F));
        chassis.addOrReplaceChild("rear_fender", cube(64, 0)
                .addBox(-3.5F, -1.0F, 0.0F, 7, 2, 10),
                PartPose.offsetAndRotation(0.0F, -30.5F, 15.0F, 0.20F, 0.0F, 0.0F));

        // 2. Tren delantero con ángulo de avance (rake -24° = -0.42 rad)
        PartDefinition steering = chassis.addOrReplaceChild(STEERING, CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -29.0F, -12.5F, FORK_RAKE, 0.0F, 0.0F));

        steering.addOrReplaceChild("headlight_cowl", cube(100, 0)
                .addBox(-4.5F, -8.0F, -4.0F, 9, 10, 3), PartPose.ZERO);

        PartDefinition frontFender = steering.addOrReplaceChild("front_fender", cube(126, 0)
                .addBox(-3.5F, 2.0F, -12.0F, 7, 2, 12),
                PartPose.rotation(0.36F, 0.0F, 0.0F));
        frontFender.addOrReplaceChild("front_fender_tip", cube(166, 0)
                .addBox(-2.5F, 0.0F, -6.0F, 5, 2, 6),
                PartPose.offsetAndRotation(0.0F, 2.0F, -11.5F, 0.04F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 256, 256);
    }

    /**
     * Capa TRIM: chasis negro, motor aletado YAMAHA, calcomanías XTZ, tapas blancas MONOCROSS, sillín,
     * escape con escudo plateado, basculante de aluminio YAMAHA, horquilla con fuelles y ruedas de radios 20x20.
     */
    public static LayerDefinition createTrimLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition leanRoot = root.addOrReplaceChild(LEAN_ROOT, CubeListBuilder.create(), at(0.0F, 24.0F, 0.0F));
        PartDefinition chassis = leanRoot.addOrReplaceChild(CHASSIS, CubeListBuilder.create(), PartPose.ZERO);

        // 1. Chasis perimetral negro, caja de filtro, monoshock y reposapiés (parche sólido 56x26 en 44,96)
        PartDefinition frameEngine = chassis.addOrReplaceChild("frame_engine", CubeListBuilder.create()
                .texOffs(FRAME_U, FRAME_V).addBox(-2.5F, -13.5F, -11.0F, 5, 2, 14)   // Cuna inferior
                .texOffs(FRAME_U, FRAME_V).addBox(-1.5F, -27.5F, -12.5F, 3, 14, 3)   // Tubo frontal (downtube)
                .texOffs(FRAME_U, FRAME_V).addBox(-3.0F, -25.5F, 0.0F, 6, 3, 16)     // Subchasis trasero
                .texOffs(FRAME_U, FRAME_V).addBox(-3.2F, -25.0F, 1.5F, 6, 7, 7)      // Caja de aire negra bajo el sillín
                .texOffs(FRAME_U, FRAME_V).addBox(-7.5F, -12.0F, -1.5F, 15, 2, 2)    // Reposapiés conductor
                .texOffs(FRAME_U, FRAME_V).addBox(-6.0F, -18.5F, 7.5F, 12, 2, 2),    // Reposapiés pasajero
                PartPose.ZERO);
        frameEngine.addOrReplaceChild("frame_spar_l", cube(FRAME_U, FRAME_V)
                .addBox(2.5F, -1.0F, 0.0F, 1.5F, 2, 21),
                PartPose.offsetAndRotation(0.0F, -28.0F, -10.5F, -0.78F, 0.0F, 0.0F));
        frameEngine.addOrReplaceChild("frame_spar_r", cube(FRAME_U, FRAME_V)
                .addBox(-4.0F, -1.0F, 0.0F, 1.5F, 2, 21),
                PartPose.offsetAndRotation(0.0F, -28.0F, -10.5F, -0.78F, 0.0F, 0.0F));
        frameEngine.addOrReplaceChild("rear_stay_l", cube(FRAME_U, FRAME_V)
                .addBox(2.5F, -22.5F, 6.0F, 1, 6, 1.5F), PartPose.ZERO);
        frameEngine.addOrReplaceChild("rear_stay_r", cube(FRAME_U, FRAME_V)
                .addBox(-3.5F, -22.5F, 6.0F, 1, 6, 1.5F), PartPose.ZERO);
        frameEngine.addOrReplaceChild("monoshock", cube(44, 22)
                .addBox(-1.5F, -21.5F, 2.0F, 3, 9, 3), PartPose.ZERO);

        // Bloque motor monocilíndrico (cárter aluminio YAMAHA + cilindro inclinado con aletas)
        frameEngine.addOrReplaceChild("engine_crankcase", cube(58, 22)
                .addBox(-3.5F, -20.5F, -7.5F, 7, 7, 9), PartPose.ZERO);
        frameEngine.addOrReplaceChild("engine_cylinder", cube(92, 22)
                .addBox(-2.5F, -6.0F, -3.0F, 5, 6, 6),
                PartPose.offsetAndRotation(0.0F, -20.5F, -4.5F, 0.22F, 0.0F, 0.0F));

        // Escape elevado por el costado derecho (-X) con escudo térmico plateado
        frameEngine.addOrReplaceChild("exhaust_header", cube(116, 22)
                .addBox(-5.2F, -1.5F, -6.0F, 2, 3, 12),
                PartPose.offsetAndRotation(0.0F, -20.0F, -3.5F, 0.24F, 0.0F, 0.0F));
        frameEngine.addOrReplaceChild("exhaust_muffler", cube(146, 22)
                .addBox(-7.4F, -2.5F, 0.0F, 3, 5, 14),
                PartPose.offsetAndRotation(0.0F, -21.5F, 2.5F, 0.24F, 0.0F, 0.0F));

        // 2. Gráficos XTZ en aletas, lomo del tanque, sillín, tapas blancas MONOCROSS y colín con bandera pintada
        PartDefinition bodywork = chassis.addOrReplaceChild("bodywork_trim", CubeListBuilder.create()
                .texOffs(0, 44).addBox(-2.5F, -33.0F, -9.5F, 5, 1, 8), PartPose.ZERO);
        chassis.addOrReplaceChild("tank_decal_l", cube(28, 44)
                .addBox(0.1F, 1.0F, -4.5F, 1, 6, 9),
                PartPose.offsetAndRotation(5.5F, -31.0F, -8.5F, 0.25F, 0.0F, 0.0F));
        chassis.addOrReplaceChild("tank_decal_r", cube(28, 44)
                .addBox(-1.1F, 1.0F, -4.5F, 1, 6, 9),
                PartPose.offsetAndRotation(-5.5F, -31.0F, -8.5F, 0.25F, 0.0F, 0.0F));

        bodywork.addOrReplaceChild("seat_front", cube(50, 44)
                .addBox(-3.5F, -2.0F, -4.5F, 7, 4, 9),
                PartPose.offsetAndRotation(0.0F, -26.2F, 2.0F, 0.05F, 0.0F, 0.0F));
        bodywork.addOrReplaceChild("seat_rear", cube(84, 44)
                .addBox(-4.0F, -2.0F, -4.5F, 8, 4, 9),
                PartPose.offsetAndRotation(0.0F, -27.2F, 11.0F, 0.08F, 0.0F, 0.0F));

        bodywork.addOrReplaceChild("side_cowl_l", cube(120, 44)
                .addBox(3.6F, -3.0F, -7.0F, 1, 6, 14),
                PartPose.offsetAndRotation(0.0F, -24.0F, 7.0F, 0.12F, 0.0F, 0.0F));
        bodywork.addOrReplaceChild("side_cowl_r", cube(120, 44)
                .addBox(-4.6F, -3.0F, -7.0F, 1, 6, 14),
                PartPose.offsetAndRotation(0.0F, -24.0F, 7.0F, 0.12F, 0.0F, 0.0F));

        // Luz trasera, direccionales y guardafango/placa con bandera de Colombia alineados bajo el colín
        PartDefinition rearTail = bodywork.addOrReplaceChild("rear_tail", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -30.5F, 15.0F, 0.20F, 0.0F, 0.0F));
        rearTail.addOrReplaceChild("taillight", cube(152, 44)
                .addBox(-2.5F, 0.8F, 8.2F, 5, 3, 2), PartPose.ZERO);
        rearTail.addOrReplaceChild("rear_blinker_l", cube(168, 44)
                .addBox(2.5F, 1.3F, 8.2F, 2, 2, 2), PartPose.ZERO);
        rearTail.addOrReplaceChild("rear_blinker_r", cube(168, 44)
                .addBox(-4.5F, 1.3F, 8.2F, 2, 2, 2), PartPose.ZERO);
        rearTail.addOrReplaceChild("mudflap", cube(178, 44)
                .addBox(-2.5F, 0.0F, 0.0F, 5, 7, 1),
                PartPose.offsetAndRotation(0.0F, 3.2F, 8.8F, 0.18F, 0.0F, 0.0F));

        // 3. Basculante trasero de aluminio YAMAHA y cubrecadena
        chassis.addOrReplaceChild(SWINGARM, CubeListBuilder.create()
                .texOffs(0, 66).addBox(2.2F, -1.5F, 0.0F, 2, 3, 18)
                .texOffs(0, 66).addBox(-4.2F, -1.5F, 0.0F, 2, 3, 18)
                .texOffs(FRAME_U, FRAME_V).addBox(1.80F, -2.2F, 2.5F, 0.6F, 1, 12.5F),
                at(0.0F, -10.0F, 1.5F));

        // Discos de rin/radios/buje con espesor 0.2F (cero caras coplanares ni z-fighting)
        CubeListBuilder wheelHubs = cube(0, 96)
                .addBox(1.55F, -10.0F, -10.0F, 0.2F, 20, 20)
                .addBox(-1.75F, -10.0F, -10.0F, 0.2F, 20, 20);

        // Ruedas colgadas de leanRoot (fijas al suelo bajo la suspensión) con aro octogonal 20x20
        ModelKit.addOctagonWheel(leanRoot, REAR_WHEEL, at(0.0F, -10.0F, 18.0F), WHEEL_RING, wheelHubs);

        // 4. Horquillas telescópicas con fuelles, faro, direccionales, manillar cross-braced conectado y rueda delantera
        PartDefinition steering = chassis.addOrReplaceChild(STEERING, CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -29.0F, -12.5F, FORK_RAKE, 0.0F, 0.0F));

        steering.addOrReplaceChild("forks", CubeListBuilder.create()
                .texOffs(42, 66).addBox(-4.5F, -1.5F, -1.5F, 9, 2, 3)   // Tija superior
                .texOffs(42, 66).addBox(-4.5F, 3.5F, -1.5F, 9, 2, 3)    // Tija inferior
                .texOffs(68, 66).addBox(2.2F, -2.5F, -1.0F, 2, 8, 2)    // Barra cromada sup izq
                .texOffs(68, 66).addBox(-4.2F, -2.5F, -1.0F, 2, 8, 2)   // Barra cromada sup der
                .texOffs(78, 66).addBox(1.7F, 5.5F, -1.5F, 3, 7, 3)     // Fuelle goma negro izq
                .texOffs(78, 66).addBox(-4.7F, 5.5F, -1.5F, 3, 7, 3)    // Fuelle goma negro der
                .texOffs(92, 66).addBox(2.2F, 12.5F, -1.0F, 2, 9, 2)    // Botella aluminio izq
                .texOffs(92, 66).addBox(-4.2F, 12.5F, -1.0F, 2, 9, 2),  // Botella aluminio der
                PartPose.ZERO);

        steering.addOrReplaceChild("headlight_trim", CubeListBuilder.create()
                .texOffs(102, 66).addBox(-3.5F, -9.0F, -4.8F, 7, 4, 1)  // Portanúmeros frontal blanco
                .texOffs(120, 66).addBox(-2.5F, -5.0F, -5.0F, 5, 5, 1)  // Óptica cristalina
                .texOffs(134, 66).addBox(4.5F, -4.0F, -3.5F, 3, 2, 2)   // Direccional ámbar izq
                .texOffs(134, 66).addBox(-7.5F, -4.0F, -3.5F, 3, 2, 2), // Direccional ámbar der
                PartPose.ZERO);

        steering.addOrReplaceChild("handlebars", CubeListBuilder.create()
                .texOffs(FRAME_U, FRAME_V).addBox(1.6F, -5.5F, -0.5F, 1, 4.5F, 1)   // Torreta manillar izq
                .texOffs(FRAME_U, FRAME_V).addBox(-2.6F, -5.5F, -0.5F, 1, 4.5F, 1)  // Torreta manillar der
                .texOffs(146, 66).addBox(-2.5F, -5.0F, -1.0F, 5, 3, 2)              // Velocímetro sobre torretas
                .texOffs(162, 66).addBox(-9.0F, -6.5F, -0.5F, 18, 1, 1)             // Barra principal de aluminio
                .texOffs(FRAME_U, FRAME_V).addBox(3.8F, -7.2F, -0.4F, 1, 1.2F, 0.8F) // Abrazadera barra izq
                .texOffs(FRAME_U, FRAME_V).addBox(-4.8F, -7.2F, -0.4F, 1, 1.2F, 0.8F)// Abrazadera barra der
                .texOffs(162, 66).addBox(-6.0F, -7.8F, -0.5F, 12, 1, 1)             // Barra transversal de refuerzo
                .texOffs(FRAME_U, FRAME_V).addBox(7.0F, -6.7F, -0.7F, 4, 1.4F, 1.4F)
                .texOffs(FRAME_U, FRAME_V).addBox(-11.0F, -6.7F, -0.7F, 4, 1.4F, 1.4F)
                .texOffs(FRAME_U, FRAME_V).addBox(7.5F, -11.5F, -0.3F, 1, 5, 1)     // Varilla espejo izq
                .texOffs(FRAME_U, FRAME_V).addBox(-8.5F, -11.5F, -0.3F, 1, 5, 1)    // Varilla espejo der
                .texOffs(FRAME_U, FRAME_V).addBox(6.5F, -14.5F, -0.5F, 4, 3, 1)     // Espejo izq
                .texOffs(FRAME_U, FRAME_V).addBox(-10.5F, -14.5F, -0.5F, 4, 3, 1),  // Espejo der
                PartPose.rotation(-FORK_RAKE, 0.0F, 0.0F));

        ModelKit.addOctagonWheel(leanRoot, FRONT_WHEEL, at(0.0F, -10.0F, -21.0F), WHEEL_RING, wheelHubs);

        return LayerDefinition.create(mesh, 256, 256);
    }

    /**
     * Submodelo para una capa ({@code BODY} o {@code TRIM}).
     * Ejecuta la cinemática de la Yamaha XTZ 125: rodadura continua de ruedas, giro de la horquilla con
     * su ángulo de avance (-24°), pulsación monocilíndrica en ralentí y tumbada dinámica en curva.
     */
    public static final class Part extends EntityModel<VehicleRenderState> {
        private final ModelPart leanRoot;
        private final ModelPart chassis;
        private final ModelPart steering;
        private final ModelPart frontWheel;
        private final ModelPart rearWheel;

        public Part(ModelPart root) {
            super(root, RenderTypes::entityCutoutCull);
            this.leanRoot = root.getChild(LEAN_ROOT);
            this.chassis = leanRoot.getChild(CHASSIS);
            this.steering = ModelKit.childOrNull(chassis, STEERING);
            this.frontWheel = ModelKit.childOrNull(leanRoot, FRONT_WHEEL);
            this.rearWheel = ModelKit.childOrNull(leanRoot, REAR_WHEEL);
        }

        @Override
        public void setupAnim(VehicleRenderState state) {
            super.setupAnim(state);

            // a) Tumbada dinámica (lean angle) sobre la línea de contacto con el suelo (Y = 24)
            this.leanRoot.zRot = state.lean;

            // b) Suspensión de largo recorrido y ralentí monocilíndrico 4T (escalados a px del modelo)
            float heavePx = ModelKit.heavePx(state.bodyHeave) / MODEL_SCALE;
            if (ModelKit.isIdling(state)) {
                float idle = IDLE.sway(state.ageInTicks);
                this.chassis.y = heavePx + IDLE.bobPx(state.ageInTicks) / MODEL_SCALE;
                this.chassis.yRot = idle;
                this.chassis.xRot = state.bodyPitch + idle * IDLE.pitchShare();
            } else {
                this.chassis.y = heavePx;
                this.chassis.yRot = 0.0F;
                this.chassis.xRot = state.bodyPitch;
            }

            // c) Giro del manillar y la horquilla (conservando el avance FORK_RAKE = -0.42 rad)
            if (this.steering != null) {
                this.steering.xRot = FORK_RAKE;
                this.steering.yRot = state.steer;
            }

            // d) Rodadura continua y dirección en las ruedas ancladas a leanRoot
            if (this.frontWheel != null) {
                this.frontWheel.xRot = state.wheelRoll;
                this.frontWheel.yRot = state.steer;
            }
            if (this.rearWheel != null) {
                this.rearWheel.xRot = state.wheelRoll;
            }
        }
    }

    private MotorcycleModels() {}
}
