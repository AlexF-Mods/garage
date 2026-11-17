package io.github.alexf6.garage.physics;

import static io.github.alexf6.garage.physics.HoldDetector.Event.LONG_PRESS;
import static io.github.alexf6.garage.physics.HoldDetector.Event.NONE;
import static io.github.alexf6.garage.physics.HoldDetector.Event.SHORT_PRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HoldDetectorTest {

    @Test
    void sinPulsarNoHayEventos() {
        HoldDetector d = new HoldDetector(3);
        assertEquals(NONE, d.update(false));
        assertEquals(NONE, d.update(false));
    }

    @Test
    void soltarAntesDeTiempoEsPulsacionCorta() {
        HoldDetector d = new HoldDetector(3);
        assertEquals(NONE, d.update(true));
        assertEquals(NONE, d.update(true));
        assertEquals(SHORT_PRESS, d.update(false));
        assertEquals(NONE, d.update(false));
    }

    @Test
    void mantenerDisparaLaLargaUnaSolaVezYAlSoltarNoHayCorta() {
        HoldDetector d = new HoldDetector(3);
        assertEquals(NONE, d.update(true));
        assertEquals(NONE, d.update(true));
        assertEquals(LONG_PRESS, d.update(true));
        assertEquals(NONE, d.update(true));
        assertEquals(NONE, d.update(false));
    }

    @Test
    void unaPulsacionDeUnTickEsCorta() {
        HoldDetector d = new HoldDetector(15);
        assertEquals(NONE, d.update(true));
        assertEquals(SHORT_PRESS, d.update(false));
    }

    @Test
    void resetOlvidaLaPulsacionEnCurso() {
        HoldDetector d = new HoldDetector(3);
        d.update(true);
        d.reset();
        assertEquals(NONE, d.update(false));
    }

    @Test
    void unMinimoDeUnTickEvitaLargaInmediataInvalida() {
        HoldDetector d = new HoldDetector(0);
        assertEquals(LONG_PRESS, d.update(true));
    }
}
