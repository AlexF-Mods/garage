package io.github.alexf6.garage.client;

import io.github.alexf6.garage.GarageSounds;
import io.github.alexf6.garage.MotorVehicleEntity;
import io.github.alexf6.garage.config.ClientConfig;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * Música espacial de la radio de la Chiva: sigue en 3D la posición del vehículo para todos los jugadores
 * cercanos y se detiene sola al cambiar de pista, apagar la radio o eliminar la entidad.
 */
public class RadioSound extends AbstractTickableSoundInstance {

    private final MotorVehicleEntity vehicle;
    private final int track;

    public RadioSound(MotorVehicleEntity vehicle, int track, SoundEvent sound) {
        super(sound, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.vehicle = vehicle;
        this.track = track;
        this.looping = true;
        this.delay = 0;
        this.volume = ClientConfig.get().radioVolume();
        this.pitch = 1.0F;
        follow();
    }

    /** Crea el sonido de la pista indicada si existe registrada, o {@code null} si aún no hay pista válida. */
    public static RadioSound create(MotorVehicleEntity vehicle, int track) {
        SoundEvent sound = GarageSounds.radioTrack(track);
        return sound != null ? new RadioSound(vehicle, track, sound) : null;
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
        this.x = this.vehicle.getX();
        this.y = this.vehicle.getY();
        this.z = this.vehicle.getZ();
    }
}
