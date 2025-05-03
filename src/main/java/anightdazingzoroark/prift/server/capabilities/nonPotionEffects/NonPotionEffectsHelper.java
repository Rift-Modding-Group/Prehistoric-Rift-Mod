package anightdazingzoroark.prift.server.capabilities.nonPotionEffects;

import anightdazingzoroark.prift.server.message.*;
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

    public static boolean isBolaCaptured(Entity entity) {
        if (entity == null) return false;
        INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        if (nonPotionEffects != null) return nonPotionEffects.isBolaCaptured();
        else return false;
    }

    public static void setGrabbed(Entity entity, boolean isGrabbed) {
        if (entity == null) return;
        if (entity.world.isRemote) {
            INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
            if (nonPotionEffects != null) {
                nonPotionEffects.setGrabbed(isGrabbed);
                RiftMessages.WRAPPER.sendToServer(new RiftSetEntityGrabbed(entity, isGrabbed));
            }
        }
        else {
            INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
            if (nonPotionEffects != null) {
                nonPotionEffects.setGrabbed(isGrabbed);
                RiftMessages.WRAPPER.sendToAll(new RiftSetEntityGrabbed(entity, isGrabbed));
            }
        }
    }

    public static boolean isGrabbed(Entity entity) {
        if (entity == null) return false;
        INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        if (nonPotionEffects != null) return nonPotionEffects.isGrabbed();
        else return false;
    }

    public static void setRiding(Entity entity, boolean value) {
        if (entity.world.isRemote) {
            INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
            if (nonPotionEffects != null) {
                nonPotionEffects.setRiding(value);
                RiftMessages.WRAPPER.sendToServer(new RiftSetRiding(entity, value));
            }
        }
        else {
            INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
            if (nonPotionEffects != null) {
                nonPotionEffects.setRiding(value);
                RiftMessages.WRAPPER.sendToAll(new RiftSetRiding(entity, value));
            }
        }
    }

    public static boolean isRiding(Entity entity) {
        if (entity == null) return false;
        INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        if (nonPotionEffects != null) return nonPotionEffects.isRiding();
        else return false;
    }
}
