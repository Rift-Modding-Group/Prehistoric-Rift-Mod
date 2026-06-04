package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import anightdazingzoroark.prift.server.entity.creature.builder.RiftCreatureBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import java.util.ArrayList;
import java.util.List;

public class RiftEntities {
    public static void registerEntities() {
        //NEW CREATURE REGISTRY
        List<RiftCreatureBuilder> builderList = new ArrayList<>(RiftCreatureRegistry.creatureBuilderMap.values());
        for (int x = 0; x < builderList.size(); x++) {
            RiftCreatureBuilder builder = builderList.get(x);
            registerEntity(
                    builder.getName(),
                    builder.getCreatureClass(),
                    x, RiftInitialize.instance
            );
        }
    }

    public static void registerEntity(String name, Class<? extends Entity> entityClass, int id, Object mod) {
        EntityRegistry.registerModEntity(new ResourceLocation(RiftInitialize.MODID, name), entityClass, name, id, mod, 64, 1, true);
    }

    public static void registerEntity(String name, Class<? extends Entity> entityClass, int id, Object mod,  int eggPrimary, int eggSecondary) {
        EntityRegistry.registerModEntity(new ResourceLocation(RiftInitialize.MODID, name), entityClass, name, id, mod, 64, 1, true, eggPrimary, eggSecondary);
    }
}
