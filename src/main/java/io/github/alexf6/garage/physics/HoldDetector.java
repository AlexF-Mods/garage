package io.github.alexf6.garage.physics;

/**
 * Distingue pulsación corta de larga en una tecla, a un tick por llamada (sin {@code net.minecraft}).
 * La larga se dispara mientras se mantiene, al cumplir {@code longPressTicks}; la corta, al soltar antes.
 * Cada pulsación da exactamente un evento. Coste O(1) por tick.
 */
public final class HoldDetector {

    public enum Event { NONE, SHORT_PRESS, LONG_PRESS }

    private final int longPressTicks;
    private int heldTicks;
    private boolean longFired;

    /** @param longPressTicks ticks (1/20 s) que hay que mantener para la pulsación larga; mínimo 1 */
    public HoldDetector(int longPressTicks) {
        this.longPressTicks = Math.max(1, longPressTicks);
    }

    /** Avanza un tick con el estado actual de la tecla. */
    public Event update(boolean down) {
        if (down) {
            heldTicks++;
            if (!longFired && heldTicks >= longPressTicks) {
                longFired = true;
                return Event.LONG_PRESS;
            }
            return Event.NONE;
        }
        boolean wasShort = heldTicks > 0 && !longFired;
        reset();
        return wasShort ? Event.SHORT_PRESS : Event.NONE;
    }

    /** Olvida una pulsación en curso (p. ej. al bajarse del vehículo). */
    public void reset() {
        heldTicks = 0;
        longFired = false;
    }
}
