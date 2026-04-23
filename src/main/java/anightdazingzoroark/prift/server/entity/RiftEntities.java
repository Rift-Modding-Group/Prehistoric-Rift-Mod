package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureRegistry;
import anightdazingzoroark.prift.server.entity.creaturenew.builder.AbstractCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creaturenew.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.other.RiftEmbryo;
import anightdazingzoroark.prift.server.entity.other.RiftTrap;
import anightdazingzoroark.prift.server.entity.projectile.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import java.util.ArrayList;
import java.util.List;

public class RiftEntities {
    public static void registerEntities() {
        int id = 0;
        int prevMaxSize = 0;
        //creatures
        for (int x = 0; x < RiftCreatureType.values().length; x++) {
            RiftCreatureType creature = RiftCreatureType.values()[id];
            registerEntity(creature.name().toLowerCase(), creature.getCreature(), id, RiftInitialize.instance, creature.getEggPrimary(), creature.getEggSecondary());
            id++;
        }
        prevMaxSize += RiftCreatureType.values().length;
        //NEW CREATURE REGISTRY
        List<RiftCreatureBuilder> builderList = new ArrayList<>(RiftCreatureRegistry.creatureBuilderMap.values());
        for (int x = 0; x < builderList.size(); x++) {
            RiftCreatureBuilder builder = builderList.get(id - prevMaxSize);
            registerEntity(
                    builder.getName()+"_new", //this is temporary
                    builder.getCreatureClass(),
                    id, RiftInitialize.instance
            );
            id++;
        }
        prevMaxSize += builderList.size();
        //weapons
        for (int x = RiftCreatureType.values().length; x < RiftLargeWeaponType.values().length; x++) {
            RiftLargeWeaponType weaponType = RiftLargeWeaponType.values()[id - prevMaxSize];
            registerEntity(weaponType.name().toLowerCase()+"_entity", weaponType.getWeaponClass(), id, RiftInitialize.instance);
            id++;
        }
        prevMaxSize += RiftLargeWeaponType.values().length;
        //everything else
        registerEntity("egg", RiftEgg.class, id++, RiftInitialize.instance);
        registerEntity("sac", RiftSac.class, id++, RiftInitialize.instance);
        registerEntity("embryo", RiftEmbryo.class, id++, RiftInitialize.instance);
        registerEntity("cannonball_projectile", RiftCannonball.class, id++, RiftInitialize.instance);
        registerEntity("mortar_shell_projectile", RiftMortarShell.class, id++, RiftInitialize.instance);
        registerEntity("catapult_boulder_projectile", RiftCatapultBoulder.class, id++, RiftInitialize.instance);
        registerEntity("thrown_bola", ThrownBola.class, id++, RiftInitialize.instance);
        registerEntity("creature_projectile", RiftCreatureProjectileEntity.class, id++, RiftInitialize.instance);
        registerEntity("trap", RiftTrap.class, id++, RiftInitialize.instance);
    }

    public static void registerEntity(String name, Class<? extends Entity> entityClass, int id, Object mod) {
        EntityRegistry.registerModEntity(new ResourceLocation(RiftInitialize.MODID, name), entityClass, name, id, mod, 64, 1, true);
    }

    public static void registerEntity(String name, Class<? extends Entity> entityClass, int id, Object mod,  int eggPrimary, int eggSecondary) {
        EntityRegistry.registerModEntity(new ResourceLocation(RiftInitialize.MODID, name), entityClass, name, id, mod, 64, 1, true, eggPrimary, eggSecondary);
    }
}