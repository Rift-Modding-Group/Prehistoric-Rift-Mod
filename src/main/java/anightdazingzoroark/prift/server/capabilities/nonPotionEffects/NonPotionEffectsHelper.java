package anightdazingzoroark.prift.server.capabilities.nonPotionEffects;

import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftSetBleeding;
import anightdazingzoroark.prift.server.message.RiftSetBolaCaptured;
import net.minecraft.entity.Entity;

public class NonPotionEffectsHelper {
    public static void setBleeding(Entity entity, int strength, int ticks) {
        if (entity.world.isRemote) {
            INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
            if (nonPotionEffects != null) {
                nonPotionEffects.setBleeding(strength, ticks);
                RiftMessages.WRAPPER.sendToServer(new RiftSetBleeding(entity, strength, ticks));
            }
        }
        else {
            INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
            if (nonPotionEffects != null) {
                nonPotionEffects.setBleeding(strength, ticks);
                RiftMessages.WRAPPER.sendToAll(new RiftSetBleeding(entity, strength, ticks));
            }
        }
    }

    public static void setBolaCaptured(Entity entity, int ticks) {
        if (entity.world.isRemote) {
            INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
            if (nonPotionEffects != null) {
                nonPotionEffects.setBolaCaptured(ticks);
                RiftMessages.WRAPPER.sendToServer(new RiftSetBolaCaptured(entity, ticks));
            }
        }
        else {
            INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
            if (nonPotionEffects != null) {
                nonPotionEffects.setBolaCaptured(ticks);
                RiftMessages.WRAPPER.sendToAll(new RiftSetBolaCaptured(entity, ticks));
            }
        }
    }
}
