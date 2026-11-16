package io.github.alexf6.garage;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Eventos de sonido registrados por el mod (como las pistas de radio de la Chiva).
 * Se registra una sola vez desde {@link GarageMod#onInitialize()}.
 */
public final class GarageSounds {

    /** Número de pistas disponibles en la radio de la Chiva. */
    public static final int RADIO_TRACK_COUNT = 3;

    private static volatile List<Holder<SoundEvent>> radioTracks = List.of();

    public static void register() {
        List<Holder<SoundEvent>> tracks = new ArrayList<>(RADIO_TRACK_COUNT);
        for (int i = 1; i <= RADIO_TRACK_COUNT; i++) {
            Identifier id = Identifier.fromNamespaceAndPath(GarageMod.MOD_ID, "chiva_radio_" + i);
            tracks.add(Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, id,
                    SoundEvent.createVariableRangeEvent(id)));
        }
        radioTracks = List.copyOf(tracks);
    }

    /** Sonido de la pista {@code track} (1-indexada), o {@code null} si está apagada o fuera de rango. */
    public static SoundEvent radioTrack(int track) {
        if (track < 1 || track > radioTracks.size()) return null;
        return radioTracks.get(track - 1).value();
    }

    private GarageSounds() {}
}
