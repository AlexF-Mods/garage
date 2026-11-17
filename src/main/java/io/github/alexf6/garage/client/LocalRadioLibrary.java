package io.github.alexf6.garage.client;

import io.github.alexf6.garage.GarageMod;
import io.github.alexf6.garage.MotorVehicleEntity;
import io.github.alexf6.garage.physics.RadioTracks;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Canciones {@code .ogg} de {@code config/garage/radio/}: es la única fuente de la radio (el mod no incluye música).
 * Hasta {@link MotorVehicleEntity#MAX_RADIO_TRACKS} archivos por orden alfabético. Cada jugador tiene su propia
 * carpeta: para oír lo mismo que el conductor hay que tener los mismos archivos (mismos nombres).
 *
 * <p>La lista se lee del disco una vez al arrancar y en cada pulsación de la tecla, siempre fuera del hilo del
 * juego ({@link #refreshAsync()}). Leerla cuesta O(n log n) en el número de archivos de la carpeta.
 */
public final class LocalRadioLibrary {

    private static final ExecutorService IO = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "garage-radio-scan");
        thread.setDaemon(true);
        return thread;
    });

    private static volatile List<Path> tracks = List.of();

    public static Path radioDir() {
        return FabricLoader.getInstance().getConfigDir().resolve(GarageMod.MOD_ID).resolve("radio");
    }

    /** Relee la carpeta en un hilo aparte; se completa cuando la lista ya está actualizada. */
    public static CompletableFuture<Void> refreshAsync() {
        return CompletableFuture.runAsync(LocalRadioLibrary::refresh, IO);
    }

    /** Crea {@code config/garage/radio/} si no existe y recarga la lista. Bloquea: no llamar desde el hilo del juego. */
    public static void refresh() {
        Path dir = radioDir();
        List<Path> found;
        try {
            Files.createDirectories(dir);
            try (Stream<Path> files = Files.list(dir)) {
                found = files
                        .filter(Files::isRegularFile)
                        .filter(p -> RadioTracks.isTrackFile(p.getFileName().toString()))
                        .sorted(Comparator.comparing(p -> RadioTracks.sortKey(p.getFileName().toString())))
                        .limit(MotorVehicleEntity.MAX_RADIO_TRACKS)
                        .toList();
            }
        } catch (IOException e) {
            GarageMod.LOGGER.warn("No se pudo leer {}: {}", dir, e.toString());
            found = List.of();
        }
        if (found.size() != tracks.size()) {
            GarageMod.LOGGER.info("Radio local: {} pista(s) .ogg en {}", found.size(), dir.toAbsolutePath());
        }
        tracks = found;
    }

    /** Número de pistas disponibles; {@code 0} si la carpeta no tiene ningún {@code .ogg}. */
    public static int trackCount() {
        return tracks.size();
    }

    /** Archivo de la pista {@code track} (1-indexada), o {@code null} si está apagada o no existe en esta lista. */
    public static Path track(int track) {
        return RadioTracks.pick(tracks, track);
    }

    /** Nombre legible de la pista (sin {@code .ogg}), o {@code null} si no existe en esta lista. */
    public static String trackName(int track) {
        Path path = track(track);
        return path == null ? null : RadioTracks.displayName(path.getFileName().toString());
    }

    private LocalRadioLibrary() {}
}
