package anightdazingzoroark.prift.server.entity.interfaces;

import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundEvent;

import java.util.List;

public interface IPackHunter {
    void setPackBuffing(boolean value);
    boolean isPackBuffing();
    List<PotionEffect> packBuffEffect();
    void setPackBuffCooldown(int value);
    int getPackBuffCooldown();
    SoundEvent getCallSound();
}
