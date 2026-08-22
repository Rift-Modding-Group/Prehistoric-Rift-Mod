package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureHitboxed;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class RiftEntities {
    //builder friendly implementation of entity registry
    //only 2 classes r used, sweet
    public static void registerEntities() {
        for (String creatureName : RiftCreatureRegistry.getCreatureNames()) {
            ResourceLocation registryName = new ResourceLocation(RiftInitialize.MODID, creatureName);
            ForgeRegistries.ENTITIES.register(new CreatureTypeEntry(creatureName).setRegistryName(registryName));
        }

        ResourceLocation creatureRegistryName = new ResourceLocation(RiftInitialize.MODID, "internal/creature");
        ForgeRegistries.ENTITIES.register(EntityEntryBuilder.<RiftCreature>create()
                .entity(RiftCreature.class)
                .factory(RiftCreature::new)
                .id(creatureRegistryName, 0)
                .name("rift_creature")
                .tracker(64, 1, true)
                .build()
        );

        ResourceLocation hitboxedCreatureRegistryName = new ResourceLocation(RiftInitialize.MODID, "internal/creature_hitboxed");
        ForgeRegistries.ENTITIES.register(EntityEntryBuilder.<RiftCreatureHitboxed>create()
                .entity(RiftCreatureHitboxed.class)
                .factory(RiftCreatureHitboxed::new)
                .id(hitboxedCreatureRegistryName, 1)
                .name("rift_creature_hitboxed")
                .tracker(64, 1, true)
                .build()
        );
    }

    private static class CreatureTypeEntry extends EntityEntry {
        @NotNull
        private final String creatureName;

        private CreatureTypeEntry(@NotNull String creatureName) {
            super(RiftCreatureRegistry.creatureUsesHitboxes(creatureName) ? RiftCreatureHitboxed.class : RiftCreature.class, creatureName);
            this.creatureName = creatureName;
        }

        @Override
        public RiftCreature newInstance(World world) {
            return RiftCreatureRegistry.createCreature(world, this.creatureName);
        }
    }
}
