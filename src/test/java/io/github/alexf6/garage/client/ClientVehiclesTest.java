package io.github.alexf6.garage.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.alexf6.garage.VehicleSpec;
import io.github.alexf6.garage.VehicleSpecs;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ClientVehiclesTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void cadaVehiculoTieneSuModeloEnElCliente() {
        VehicleRenderer.VehicleRenderState idle = new VehicleRenderer.VehicleRenderState();
        idle.isEngineRunning = true;
        idle.ageInTicks = 10.0F;
        VehicleRenderer.VehicleRenderState moving = new VehicleRenderer.VehicleRenderState();
        moving.isEngineRunning = true;
        moving.speedRatio = 0.8F;
        moving.steer = 0.3F;
        moving.wheelRoll = 1.2F;
        for (VehicleSpec spec : VehicleSpecs.ALL) {
            ClientVehicles.Model model = ClientVehicles.lookOf(spec);
            assertNotNull(model.look().texture(), spec.id());
            var body = model.bodyLayer().get().bakeRoot();
            var trim = model.trimLayer().get().bakeRoot();
            assertNotNull(body, spec.id() + " sin capa BODY");
            assertNotNull(trim, spec.id() + " sin capa TRIM");
            var bodyModel = model.look().partFactory().apply(body);
            var trimModel = model.look().partFactory().apply(trim);
            bodyModel.setupAnim(idle);
            trimModel.setupAnim(idle);
            bodyModel.setupAnim(moving);
            trimModel.setupAnim(moving);
        }
    }
}
