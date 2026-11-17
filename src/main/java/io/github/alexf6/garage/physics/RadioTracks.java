package io.github.alexf6.garage.physics;

import java.util.List;
import java.util.Locale;

/**
 * Reglas puras de la lista de canciones de la radio (sin {@code net.minecraft}).
 * Las pistas se numeran desde 1; {@code 0} es "apagada".
 */
public final class RadioTracks {

    public static final String EXTENSION = ".ogg";

    /** ¿Es un archivo de música válido? (extensión {@code .ogg}, sin distinguir mayúsculas). Coste O(1). */
    public static boolean isTrackFile(String fileName) {
        return fileName.toLowerCase(Locale.ROOT).endsWith(EXTENSION);
    }

    /** Clave de orden alfabético sin distinguir mayúsculas. Coste O(n) en el largo del nombre. */
    public static String sortKey(String fileName) {
        return fileName.toLowerCase(Locale.ROOT);
    }

    /** Nombre legible: el archivo sin la extensión {@code .ogg} (un nombre que solo es la extensión se conserva). */
    public static String displayName(String fileName) {
        if (isTrackFile(fileName) && fileName.length() > EXTENSION.length()) {
            return fileName.substring(0, fileName.length() - EXTENSION.length());
        }
        return fileName;
    }

    /**
     * Elemento de la pista {@code track} (1-indexada) o {@code null} si está apagada o no existe en esta lista.
     * Nunca da la vuelta: cada jugador tiene su propia carpeta, y sonar otro archivo sería peor que callar.
     * Coste O(1).
     */
    public static <T> T pick(List<T> tracks, int track) {
        if (track < 1 || track > tracks.size()) return null;
        return tracks.get(track - 1);
    }

    private RadioTracks() {}
}
