package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.config.RiftConfigList;
import anightdazingzoroark.prift.server.entity.projectile.ThrownStegoPlate;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import java.util.Iterator;

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
        for (int x = 0; x < RiftConfigList.values().length; x++) {
            if (x > 0) {
                Class creatureClass = RiftConfigList.values()[x].getCreatureClass();
                for (int y = 0; y < RiftConfigList.values()[x].getConfigInstance().spawnPlaces.length; y++) {
                    String entry = RiftConfigList.values()[x].getConfigInstance().spawnPlaces[y];
                    int partOne = entry.indexOf(":");

                    if (!entry.substring(0, 1).equals("-")) {
                        String spawnerType = entry.substring(0, partOne);
                        int partTwo = entry.indexOf(":", partOne + 1);
                        int partThree = entry.indexOf(":", partTwo + 1);
                        int partFour = entry.indexOf(":", partThree + 1);

                        if (spawnerType.equals("biome")) {
                            int partFive = entry.indexOf(":", partFour + 1);
                            String biomeIdentifier = entry.substring(partOne + 1, partThree);
                            int spawnWeight = Integer.parseInt(entry.substring(partThree + 1, partFour));
                            int minCount = Integer.parseInt(entry.substring(partFour + 1, partFive));
                            int maxCount = Integer.parseInt(entry.substring(partFive + 1));
                            for (Biome biome : Biome.REGISTRY) {
                                if (biome.getRegistryName().equals(biomeIdentifier)) {
                                    biome.getSpawnableList(EnumCreatureType.CREATURE).add(new Biome.SpawnListEntry(creatureClass, spawnWeight, minCount, maxCount));
                                }
                            }
                        }
                        else if (spawnerType.equals("tag")) {
                            String biomeTag = entry.substring(partOne + 1, partTwo);
                            int spawnWeight = Integer.parseInt(entry.substring(partTwo + 1, partThree));
                            int minCount = Integer.parseInt(entry.substring(partThree + 1, partFour));
                            int maxCount = Integer.parseInt(entry.substring(partFour + 1));
                            for (Biome biome : Biome.REGISTRY) {
                                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.getType(biomeTag))) {
                                    biome.getSpawnableList(EnumCreatureType.CREATURE).add(new Biome.SpawnListEntry(creatureClass, spawnWeight, minCount, maxCount));
                                }
                            }
                        }
                    }
                    else {
                        String spawnerType = entry.substring(1, partOne);
                        if (spawnerType.equals("biome")) {
                            String biomeIdentifier = entry.substring(partOne + 1);
                            for (Biome biome : Biome.REGISTRY) {
                                if (biome.getRegistryName().equals(biomeIdentifier)) {
                                    Iterator<Biome.SpawnListEntry> iterator = biome.getSpawnableList(EnumCreatureType.CREATURE).iterator();
                                    while(iterator.hasNext()) {
                                        Biome.SpawnListEntry toRemove = iterator.next();
                                        if (toRemove.entityClass.equals(creatureClass)) {
                                            iterator.remove();
                                        }
                                    }
                                }
                            }
                        }
                        else if (spawnerType.equals("tag")) {
                            String biomeTag = entry.substring(partOne + 1);
                            for (Biome biome : Biome.REGISTRY) {
                                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.getType(biomeTag))) {
                                    Iterator<Biome.SpawnListEntry> iterator = biome.getSpawnableList(EnumCreatureType.CREATURE).iterator();
                                    while(iterator.hasNext()) {
                                        Biome.SpawnListEntry toRemove = iterator.next();
                                        if (toRemove.entityClass.equals(creatureClass)) {
                                            iterator.remove();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
