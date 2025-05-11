package anightdazingzoroark.prift.client;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundEvent;

public class RiftSoundLooper {
    private final EntityLivingBase entityLivingBase;
    private final SoundEvent soundEvent;
    private final int interval;
    private final float volume;
    private final float pitch;
    private int time;

    public RiftSoundLooper(EntityLivingBase entityLivingBase, SoundEvent soundEvent, int interval, float volume, float pitch) {
        this.entityLivingBase = entityLivingBase;
        this.soundEvent = soundEvent;
        this.interval = interval;
        this.volume = volume;
        this.pitch = pitch;
    }

    public void playSound() {
        if (this.time == 0) this.entityLivingBase.playSound(this.soundEvent, this.volume, this.pitch);
        if (this.time >= this.interval) this.time = -1;
        this.time++;
    }

    public void resetLooper() {
        this.time = 0;
    }
}
