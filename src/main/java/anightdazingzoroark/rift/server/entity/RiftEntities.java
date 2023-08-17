package anightdazingzoroark.rift.server.entity;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.entity.creature.Tyrannosaurus;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class RiftEntities {
    public static void registerEntities() {
        int id = 0;
        for (int x = 0; x < RiftCreatureType.values().length; x++) {
            RiftCreatureType creature = RiftCreatureType.values()[x];
            registerEntity(creature.name().toLowerCase(), creature.getCreature(), x++, RiftInitialize.instance, 3670016, 2428687);
            id = x;
        }
        registerEntity("egg", RiftEgg.class, id, RiftInitialize.instance);
    }

    public static void registerEntity(String name, Class<? extends Entity> entityClass, int id, Object mod) {
        EntityRegistry.registerModEntity(new ResourceLocation(RiftInitialize.MODID, ""+ name), entityClass, name, id, mod, 64, 3, true);
    }

    public static void registerEntity(String name, Class<? extends Entity> entityClass, int id, Object mod,  int eggPrimary, int eggSecondary) {
        EntityRegistry.registerModEntity(new ResourceLocation(RiftInitialize.MODID, ""+ name), entityClass, name, id, mod, 64, 3, true, eggPrimary, eggSecondary);
    }
}
