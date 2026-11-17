package io.github.alexf6.garage.physics;

/**
 * Modelo de manejo tipo "bicicleta" (Ackermann simplificado): funciones puras, O(1) por tick y sin
 * dependencias de Minecraft, para poder probarlas y reutilizarlas en otros vehículos.
 */
public final class VehiclePhysics {

    private static final double STOP_EPSILON = 0.003;
    /** Cuánto más rápido vuelve el volante al centro al soltar las teclas, respecto a {@code steerRate}. */
    private static final double STEER_RETURN_BOOST = 1.8;
    /** Peso de {@code grip} frente a {@code airDrag} (0.5 = mitad y mitad) en la velocidad lateral en el aire. */
    private static final double AIR_GRIP_BLEND = 0.5;
    /** Factor de frenado de una embarcación varada en tierra firme (sin agua bajo el casco). */
    public static final double WATERCRAFT_LAND_FRICTION = 0.80;

    /** Nueva velocidad longitudinal para vehículos terrestres (signo = sentido respecto al frente). */
    public static double nextSpeed(double speed, VehicleInput input, boolean fuelled, boolean onGround,
                                   boolean inWater, VehicleTuning t) {
        return nextSpeed(speed, input, fuelled, onGround, inWater, false, t);
    }

    /**
     * Nueva velocidad longitudinal (bloques/tick). Las embarcaciones ({@code watercraft = true}) aceleran y frenan en
     * el agua y se detienen rápido si quedan varadas en tierra; los terrestres pierden tracción en el agua.
     */
    public static double nextSpeed(double speed, VehicleInput input, boolean fuelled, boolean onGround,
                                   boolean inWater, boolean watercraft, VehicleTuning t) {
        double v = speed;
        if (watercraft && !inWater) {
            v *= onGround ? WATERCRAFT_LAND_FRICTION : t.airDrag();
        } else if (!watercraft && inWater) {
            v *= t.waterDrag(); // sin tracción en el agua para vehículos terrestres
        } else {
            double friction = (watercraft || onGround) ? t.rollingFriction() : t.airDrag();
            if (input.brake()) {
                v = towardZero(v, t.handbrake());
            } else if (input.throttle() > 0) {
                if (v < 0) v = towardZero(v, t.brake());
                else v = fuelled ? v + t.accel() * (1.0 - v / t.maxSpeed()) : v * friction;
            } else if (input.throttle() < 0) {
                if (v > 0) v = towardZero(v, t.brake());
                else v = fuelled ? v - t.reverseAccel() * (1.0 + v / t.maxReverse()) : v * friction;
            } else {
                v *= friction;
            }
        }

        v = clamp(v, -t.maxReverse(), t.maxSpeed());
        return Math.abs(v) < STOP_EPSILON ? 0.0 : v;
    }

    /**
     * Ángulo de dirección: se acerca al objetivo a {@code steerRate} por tick (y vuelve al centro más rápido
     * al soltar las teclas) y el máximo baja con la velocidad, para maniobrar cerrado sin perder estabilidad.
     */
    public static double nextSteer(double steer, VehicleInput input, double speed, VehicleTuning t) {
        double ratio = Math.min(Math.abs(speed) / t.maxSpeed(), 1.0);
        double maxSteer = t.maxSteerLow() + (t.maxSteerHigh() - t.maxSteerLow()) * ratio;
        double target = input.steering() * maxSteer;
        double rate = input.steering() == 0 ? t.steerRate() * STEER_RETURN_BOOST : t.steerRate();
        return steer + clamp(target - steer, -rate, rate);
    }

    /** Velocidad lateral (derrape) que queda tras un tick para vehículos terrestres. */
    public static double nextLateral(double lateral, VehicleInput input, boolean onGround, boolean inWater, VehicleTuning t) {
        return nextLateral(lateral, input, onGround, inWater, false, t);
    }

    /**
     * Velocidad lateral (derrape) que queda tras un tick. En embarcaciones el casco mantiene agarre lateral en el agua
     * y frena en seco al tocar tierra; en vehículos terrestres la tierra aporta el agarre y el aire conserva parte.
     */
    public static double nextLateral(double lateral, VehicleInput input, boolean onGround, boolean inWater,
                                     boolean watercraft, VehicleTuning t) {
        double v;
        if (watercraft) {
            if (inWater) v = lateral * (input.brake() ? t.driftGrip() : t.grip());
            else if (!onGround) v = lateral * ((t.grip() + t.airDrag()) * AIR_GRIP_BLEND);
            else v = lateral * WATERCRAFT_LAND_FRICTION;
        } else {
            if (inWater) v = lateral * t.waterDrag();
            else if (!onGround) v = lateral * ((t.grip() + t.airDrag()) * AIR_GRIP_BLEND);
            else v = lateral * (input.brake() ? t.driftGrip() : t.grip());
        }
        return Math.abs(v) < STOP_EPSILON ? 0.0 : v;
    }

    /**
     * Grados que gira el carro en un tick: ω = v · tan(δ) / L. En reversa el giro se invierte solo.
     * Derrapando, la cola se suelta y gira más ({@code driftYawBoost}).
     */
    public static float yawDelta(double speed, double steer, boolean drifting, VehicleTuning t) {
        double omega = speed * Math.tan(steer) / t.wheelBase();
        return (float) Math.toDegrees(drifting ? omega * t.driftYawBoost() : omega);
    }

    /** Ángulo de dirección que explica un giro observado (inversa de {@link #yawDelta}); para animar ruedas. */
    public static double steerFromMotion(double yawDeltaDeg, double forward, VehicleTuning t) {
        if (Math.abs(forward) < 1.0e-3) return Double.NaN;
        return Math.atan(Math.toRadians(yawDeltaDeg) * t.wheelBase() / forward);
    }

    static double towardZero(double value, double amount) {
        return value > 0 ? Math.max(0.0, value - amount) : Math.min(0.0, value + amount);
    }

    static double clamp(double value, double min, double max) {
        return value < min ? min : Math.min(value, max);
    }

    private VehiclePhysics() {}
}
