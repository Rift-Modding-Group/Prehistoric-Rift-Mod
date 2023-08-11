package anightdazingzoroark.rift.server.entity;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.entity.creature.Tyrannosaurus;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class RiftEntities {
    public static void registerEntities() {
        int id = 0;
        registerEntity("tyrannosaurus", Tyrannosaurus.class, id++, RiftInitialize.instance, 64, 3, true, 3670016, 2428687);
    }

    public static void registerEntity(String name, Class<? extends Entity> entityClass, int id, Object mod, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates, int eggPrimary, int eggSecondary) {
        EntityRegistry.registerModEntity(new ResourceLocation(RiftInitialize.MODID, ""+ name), entityClass, name, id, mod, trackingRange, updateFrequency, sendsVelocityUpdates, eggPrimary, eggSecondary);
    }
}
