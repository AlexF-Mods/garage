package io.github.alexf6.garage.client;

import io.github.alexf6.garage.GarageMod;
import io.github.alexf6.garage.MotorVehicleEntity;
import io.github.alexf6.garage.config.ClientConfig;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.client.sounds.LoopingAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.ConstantFloat;

/**
 * Música espacial de la radio del vehículo: sigue en 3D la posición del vehículo para los jugadores
 * cercanos (y centrada para quien va dentro), reproduciendo pistas locales de {@code config/garage/radio/}.
 * El {@link Sound} se construye en {@link #getOrResolve}, así que no depende de {@code sounds.json} ni de
 * archivos dentro del mod. Volumen {@code [0, 1]} y alcance en bloques, ambos de {@link ClientConfig}.
 */
public class RadioSound extends AbstractTickableSoundInstance {

    /** Identificador del sonido; no está registrado ni en {@code sounds.json}. */
    private static final SoundEvent EVENT =
            SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(GarageMod.MOD_ID, "radio"));

    private final MotorVehicleEntity vehicle;
    private final int track;
    private final Path file;

    private RadioSound(MotorVehicleEntity vehicle, int track, Path file) {
        super(EVENT, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.vehicle = vehicle;
        this.track = track;
        this.file = file;
        GarageMod.LOGGER.info("Radio: pista {} -> {}", track, file);
        this.looping = true;
        this.delay = 0;
        this.volume = ClientConfig.get().radioVolume();
        this.pitch = 1.0F;
        follow();
    }

    /** Crea el sonido de la pista indicada (1-indexada), o {@code null} si no hay archivo para ella. */
    public static RadioSound create(MotorVehicleEntity vehicle, int track) {
        Path file = LocalRadioLibrary.track(track);
        return file != null ? new RadioSound(vehicle, track, file) : null;
    }

    @Override
    public WeighedSoundEvents getOrResolve(SoundManager soundManager) {
        WeighedSoundEvents event = new WeighedSoundEvents(this.identifier, "subtitles.garage.chiva_radio");
        this.sound = new Sound(this.identifier, ConstantFloat.of(1.0F), ConstantFloat.of(1.0F), 1,
                Sound.Type.FILE, true, false, ClientConfig.get().radioRange());
        event.addSound(this.sound);
        this.soundEvent = event;
        return event;
    }

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary soundBuffers, Identifier id,
                                                         boolean looping) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                InputStream in = Files.newInputStream(this.file);
                return looping ? new LoopingAudioStream(JOrbisAudioStream::new, in) : new JOrbisAudioStream(in);
            } catch (IOException e) {
                GarageMod.LOGGER.warn("No se pudo abrir la pista {}: {}", this.file, e.toString());
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public boolean canPlaySound() {
        return !this.vehicle.isSilent();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (this.vehicle.isRemoved() || this.vehicle.getRadioTrack() != this.track) {
            stop();
            return;
        }
        follow();
        this.volume = ClientConfig.get().radioVolume();
    }

    private void follow() {
        // Si el jugador local va dentro del vehículo, centramos el sonido en su cabeza para evitar
        // que se oiga solo por un oído al estar el asiento desplazado del centro del chasis.
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getVehicle() == this.vehicle) {
            this.x = player.getX();
            this.y = player.getEyeY();
            this.z = player.getZ();
            return;
        }
        this.x = this.vehicle.getX();
        this.y = this.vehicle.getY();
        this.z = this.vehicle.getZ();
    }
}
