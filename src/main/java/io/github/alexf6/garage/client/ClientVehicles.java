package io.github.alexf6.garage.client;

import io.github.alexf6.garage.GarageMod;
import io.github.alexf6.garage.VehicleSpec;
import io.github.alexf6.garage.VehicleSpecs;
import io.github.alexf6.garage.client.VehicleRenderer.VehicleRenderState;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;

/**
 * Aspecto de cada {@link VehicleSpec} en el cliente: una entrada por vehículo, con su modelo y textura.
 * Si falta la entrada de un vehículo, {@link #lookOf} falla al arrancar y no al dibujarlo.
 */
final class ClientVehicles {

    /** Modelo de un vehículo: sus dos capas y cómo se animan. */
    record Model(VehicleRenderer.Look look, Supplier<LayerDefinition> bodyLayer, Supplier<LayerDefinition> trimLayer) {}

    /** Lo que hace falta para registrar el modelo de un vehículo: sus capas y la pieza que lo anima. */
    record ModelDef(ModelLayerLocation body, ModelLayerLocation trim,
                    Function<ModelPart, EntityModel<VehicleRenderState>> part,
                    Supplier<LayerDefinition> bodyLayer, Supplier<LayerDefinition> trimLayer) {}

    private static final Map<String, Model> MODELS = Map.ofEntries(
            entry(VehicleSpecs.CAR, 0.9F, 1.0F, CarModels.DEF),
            entry(VehicleSpecs.MOTORCYCLE, 0.5F, MotorcycleModels.MODEL_SCALE, MotorcycleModels.DEF),
            entry(VehicleSpecs.MOTOCARRO, 0.75F, MotocarroModels.MODEL_SCALE, MotocarroModels.DEF),
            entry(VehicleSpecs.MOTOCARGUERO, 0.8F, MotocargueroModels.MODEL_SCALE, MotocargueroModels.DEF),
            entry(VehicleSpecs.CHIVA, 1.8F, ChivaModels.MODEL_SCALE, ChivaModels.DEF),
            entry(VehicleSpecs.TWINGO, 0.85F, TwingoModels.MODEL_SCALE, TwingoModels.DEF),
            entry(VehicleSpecs.CHALUPA, 1.1F, ChalupaModels.MODEL_SCALE, ChalupaModels.DEF));

    /** La textura de entidad es {@code textures/entity/<id>.png}: sale del id del spec, no se repite a mano. */
    private static Map.Entry<String, Model> entry(VehicleSpec spec, float shadowRadius, float scale, ModelDef def) {
        VehicleRenderer.Look look = new VehicleRenderer.Look(texture(spec.id()), shadowRadius, scale, def.body(),
                def.trim(), def.part());
        return Map.entry(spec.id(), new Model(look, def.bodyLayer(), def.trimLayer()));
    }

    static Model lookOf(VehicleSpec spec) {
        Model model = MODELS.get(spec.id());
        if (model == null) {
            throw new IllegalStateException("Falta el modelo de cliente de " + spec.id() + " en ClientVehicles");
        }
        return model;
    }

    private static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath(GarageMod.MOD_ID, "textures/entity/" + name + ".png");
    }

    private ClientVehicles() {}
}
