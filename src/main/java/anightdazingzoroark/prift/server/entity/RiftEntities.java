package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.config.RiftConfigList;
import anightdazingzoroark.prift.server.entity.creature.Coelacanth;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftCatapult;
import anightdazingzoroark.prift.server.entity.projectile.*;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import java.util.Iterator;

public class RiftEntities {
    public static void registerEntities() {
        //entity properties
        EntityPropertiesHandler.INSTANCE.registerProperties(RiftEntityProperties.class);
        EntityPropertiesHandler.INSTANCE.registerProperties(PlayerJournalProgress.class);

        //creatures
        for (int x = 0; x < RiftCreatureType.values().length; x++) {
            RiftCreatureType creature = RiftCreatureType.values()[x];
            registerEntity(creature.name().toLowerCase(), creature.getCreature(), x, RiftInitialize.instance, creature.getEggPrimary(), creature.getEggSecondary());
        }
        //weapons
        for (int x = RiftCreatureType.values().length; x < RiftLargeWeaponType.values().length + RiftCreatureType.values().length; x++) {
            if (x == RiftCreatureType.values().length) continue;
            int id = x - RiftCreatureType.values().length;
            RiftLargeWeaponType weaponType = RiftLargeWeaponType.values()[id];
            registerEntity(weaponType.name().toLowerCase()+"_entity", weaponType.getWeaponClass(), x, RiftInitialize.instance);
        }
        //everything else
        int miscId = RiftLargeWeaponType.values().length + RiftCreatureType.values().length;
        registerEntity("egg", RiftEgg.class, miscId++, RiftInitialize.instance);
        registerEntity("sac", RiftSac.class, miscId++, RiftInitialize.instance);
        registerEntity("thrown_stegosaurus_plate", ThrownStegoPlate.class, miscId++, RiftInitialize.instance);
        registerEntity("cannonball_projectile", RiftCannonball.class, miscId++, RiftInitialize.instance);
        registerEntity("mortar_shell_projectile", RiftMortarShell.class, miscId++, RiftInitialize.instance);
        registerEntity("catapult_boulder_projectile", RiftCatapultBoulder.class, miscId++, RiftInitialize.instance);
        registerEntity("thrown_bola", ThrownBola.class, miscId++, RiftInitialize.instance);
    }

    public static void registerEntity(String name, Class<? extends Entity> entityClass, int id, Object mod) {
        EntityRegistry.registerModEntity(new ResourceLocation(RiftInitialize.MODID, ""+ name), entityClass, name, id, mod, 64, 3, true);
    }

    public static void registerEntity(String name, Class<? extends Entity> entityClass, int id, Object mod,  int eggPrimary, int eggSecondary) {
        EntityRegistry.registerModEntity(new ResourceLocation(RiftInitialize.MODID, ""+ name), entityClass, name, id, mod, 64, 3, true, eggPrimary, eggSecondary);
    }

    public static void registerSpawn() {
        for (int x = 0; x < RiftConfigList.values().length; x++) {
            if (x > 0 && RiftConfigList.values()[x].getConfigInstance().canSpawn) {
                Class<? extends RiftCreature> creatureClass = RiftConfigList.values()[x].getCreatureClass();
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
                            int partSix = entry.indexOf(":", partFive + 1);
                            String category = entry.substring(partSix + 1);
                            String biomeIdentifier = entry.substring(partOne + 1, partThree);
                            int spawnWeight = Integer.parseInt(entry.substring(partThree + 1, partFour));
                            int minCount = Integer.parseInt(entry.substring(partFour + 1, partFive));
                            int maxCount = Integer.parseInt(entry.substring(partFive + 1, partSix));
                            for (Biome biome : Biome.REGISTRY) {
                                if (biome.getRegistryName().toString().equals(biomeIdentifier)) {
                                    EntityRegistry.addSpawn(creatureClass, spawnWeight, minCount, maxCount, EnumCreatureType.valueOf(category), biome);
                                }
                            }
                        }
                        else if (spawnerType.equals("tag")) {
                            int partFive = entry.indexOf(":", partFour + 1);
                            String biomeTag = entry.substring(partOne + 1, partTwo);
                            int spawnWeight = Integer.parseInt(entry.substring(partTwo + 1, partThree));
                            int minCount = Integer.parseInt(entry.substring(partThree + 1, partFour));
                            int maxCount = Integer.parseInt(entry.substring(partFour + 1, partFive));
                            String category = entry.substring(partFive + 1);
                            for (Biome biome : Biome.REGISTRY) {
                                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.getType(biomeTag))) {
                                    EntityRegistry.addSpawn(creatureClass, spawnWeight, minCount, maxCount, EnumCreatureType.valueOf(category), biome);
                                }
                            }
                        }
                        else if (spawnerType.equals("all")) {
                            int spawnWeight = Integer.parseInt(entry.substring(partOne + 1, partTwo));
                            int minCount = Integer.parseInt(entry.substring(partTwo + 1, partThree));
                            int maxCount = Integer.parseInt(entry.substring(partThree + 1, partFour));
                            String category = entry.substring(partFour + 1);
                            for (Biome biome : Biome.REGISTRY) {
                                EntityRegistry.addSpawn(creatureClass, spawnWeight, minCount, maxCount, EnumCreatureType.valueOf(category), biome);
                            }
                        }
                    }
                    else {
                        String spawnerType = entry.substring(1, partOne);
                        if (spawnerType.equals("biome")) {
                            String biomeIdentifier = entry.substring(partOne + 1);
                            for (Biome biome : Biome.REGISTRY) {
                                if (biome.getRegistryName().equals(biomeIdentifier)) {
                                    for (EnumCreatureType creatureType : EnumCreatureType.values())  EntityRegistry.removeSpawn(creatureClass, creatureType, biome);
                                }
                            }
                        }
                        else if (spawnerType.equals("tag")) {
                            String biomeTag = entry.substring(partOne + 1);
                            for (Biome biome : Biome.REGISTRY) {
                                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.getType(biomeTag))) {
                                    for (EnumCreatureType creatureType : EnumCreatureType.values())  EntityRegistry.removeSpawn(creatureClass, creatureType, biome);
                                }
                            }
                        }
                    }
                }
                if (RiftConfigList.values()[x].getIsWaterCreature()) {
                    EntitySpawnPlacementRegistry.setPlacementType(creatureClass, EntityLiving.SpawnPlacementType.IN_WATER);
                }
            }
        }
    }
}
