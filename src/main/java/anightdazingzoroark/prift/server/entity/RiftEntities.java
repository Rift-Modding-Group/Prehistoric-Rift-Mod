package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class RiftEntities {
    public static void registerEntities() {
        //NEW CREATURE REGISTRY
        List<String> creatureNames = new ArrayList<>(RiftCreatureRegistry.creatureBuilderMap.keySet());
        for (int x = 0; x < creatureNames.size(); x++) {
            String creatureName = creatureNames.get(x);
            registerEntity(
                    creatureName,
                    RiftCreatureRegistry.getCreatureRegistryClass(creatureName),
                    x
            );
        }
    }

    public static void registerEntity(String name, Class<? extends RiftCreature> entityClass, int id) {
        ResourceLocation registryName = new ResourceLocation(RiftInitialize.MODID, name);
        ForgeRegistries.ENTITIES.register(EntityEntryBuilder.<RiftCreature>create()
                .entity(entityClass)
                .factory(world -> RiftCreatureRegistry.createCreature(world, name))
                .id(registryName, id)
                .name(name)
                .tracker(64, 1, true)
                .build()
        );
    }
}
