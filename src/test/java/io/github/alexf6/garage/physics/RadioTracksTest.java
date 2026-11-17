package io.github.alexf6.garage.physics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class RadioTracksTest {

    @Test
    void soloLosOggSonPistas() {
        assertTrue(RadioTracks.isTrackFile("a.ogg"));
        assertTrue(RadioTracks.isTrackFile("A.OGG"));
        assertFalse(RadioTracks.isTrackFile("a.mp3"));
        assertFalse(RadioTracks.isTrackFile("ogg"));
        assertFalse(RadioTracks.isTrackFile(""));
    }

    @Test
    void elNombreQuitaLaExtension() {
        assertEquals("salsa", RadioTracks.displayName("salsa.ogg"));
        assertEquals("salsa", RadioTracks.displayName("salsa.OGG"));
        assertEquals("a", RadioTracks.displayName("a.ogg"));
        assertEquals(".ogg", RadioTracks.displayName(".ogg"));
        assertEquals("sin extension", RadioTracks.displayName("sin extension"));
    }

    @Test
    void elOrdenNoDistingueMayusculas() {
        assertEquals("abc.ogg", RadioTracks.sortKey("AbC.ogg"));
    }

    @Test
    void pickEsUnoIndexadoYNoDaLaVuelta() {
        List<String> tracks = List.of("a", "b", "c");
        assertEquals("a", RadioTracks.pick(tracks, 1));
        assertEquals("c", RadioTracks.pick(tracks, 3));
        assertNull(RadioTracks.pick(tracks, 4));
        assertNull(RadioTracks.pick(tracks, 0));
        assertNull(RadioTracks.pick(tracks, -1));
        assertNull(RadioTracks.pick(List.of(), 1));
    }
}
