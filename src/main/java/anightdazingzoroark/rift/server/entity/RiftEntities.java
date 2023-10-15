package anightdazingzoroark.rift.server.entity;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.entity.creature.Stegosaurus;
import anightdazingzoroark.rift.server.entity.creature.Tyrannosaurus;
import anightdazingzoroark.rift.server.entity.projectile.ThrownStegoPlate;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import java.util.List;

public class RiftEntities {
    public static void registerEntities() {
        EntityPropertiesHandler.INSTANCE.registerProperties(RiftEntityProperties.class);
        //creatures
        for (int x = 0; x < RiftCreatureType.values().length; x++) {
            RiftCreatureType creature = RiftCreatureType.values()[x];
            registerEntity(creature.name().toLowerCase(), creature.getCreature(), x, RiftInitialize.instance, creature.getEggPrimary(), creature.getEggSecondary());
        }
        //everything else
        int miscId = RiftCreatureType.values().length;
        registerEntity("egg", RiftEgg.class, miscId++, RiftInitialize.instance);
        registerEntity("thrown_stegosaurus_plate", ThrownStegoPlate.class, miscId++, RiftInitialize.instance);
    }

    public static void registerEntity(String name, Class<? extends Entity> entityClass, int id, Object mod) {
        EntityRegistry.registerModEntity(new ResourceLocation(RiftInitialize.MODID, ""+ name), entityClass, name, id, mod, 64, 3, true);
    }

    public static void registerEntity(String name, Class<? extends Entity> entityClass, int id, Object mod,  int eggPrimary, int eggSecondary) {
        EntityRegistry.registerModEntity(new ResourceLocation(RiftInitialize.MODID, ""+ name), entityClass, name, id, mod, 64, 3, true, eggPrimary, eggSecondary);
    }

    public static void registerSpawn() {
        //tyrannosaurus
        for (Biome biome : Biome.REGISTRY) {
            if (biome != null) {
                //regular plains
                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.PLAINS) && !BiomeDictionary.hasType(biome, BiomeDictionary.Type.SAVANNA)) {
                    biome.getSpawnableList(EnumCreatureType.CREATURE).add(new Biome.SpawnListEntry(Tyrannosaurus.class, 15, 1, 1));
                }
                //mountains
                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.MOUNTAIN)) {
                    biome.getSpawnableList(EnumCreatureType.CREATURE).add(new Biome.SpawnListEntry(Tyrannosaurus.class, 20, 1, 1));
                }
            }
        }

        //stegosaurus
        for (Biome biome : Biome.REGISTRY) {
            if (biome != null) {
                //regular plains
                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.PLAINS) && !BiomeDictionary.hasType(biome, BiomeDictionary.Type.SAVANNA)) {
                    biome.getSpawnableList(EnumCreatureType.CREATURE).add(new Biome.SpawnListEntry(Stegosaurus.class, 20, 4, 6));
                }
            }
        }
    }
}
