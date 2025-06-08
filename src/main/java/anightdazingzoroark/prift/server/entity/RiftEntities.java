package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.other.RiftTrap;
import anightdazingzoroark.prift.server.entity.projectile.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class RiftEntities {
    public static void registerEntities() {
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
        registerEntity("embryo", RiftSac.class, miscId++, RiftInitialize.instance);
        registerEntity("thrown_stegosaurus_plate", ThrownStegoPlate.class, miscId++, RiftInitialize.instance);
        registerEntity("cannonball_projectile", RiftCannonball.class, miscId++, RiftInitialize.instance);
        registerEntity("mortar_shell_projectile", RiftMortarShell.class, miscId++, RiftInitialize.instance);
        registerEntity("catapult_boulder_projectile", RiftCatapultBoulder.class, miscId++, RiftInitialize.instance);
        registerEntity("thrown_bola", ThrownBola.class, miscId++, RiftInitialize.instance);
        registerEntity("dilophosaurus_spit", DilophosaurusSpit.class, miscId++, RiftInitialize.instance);
        registerEntity("venom_bomb", VenomBomb.class, miscId++, RiftInitialize.instance);
        registerEntity("trap", RiftTrap.class, miscId++, RiftInitialize.instance);
    }

    public static void registerEntity(String name, Class<? extends Entity> entityClass, int id, Object mod) {
        EntityRegistry.registerModEntity(new ResourceLocation(RiftInitialize.MODID, name), entityClass, name, id, mod, 64, 1, true);
    }

    public static void registerEntity(String name, Class<? extends Entity> entityClass, int id, Object mod,  int eggPrimary, int eggSecondary) {
        EntityRegistry.registerModEntity(new ResourceLocation(RiftInitialize.MODID, name), entityClass, name, id, mod, 64, 1, true, eggPrimary, eggSecondary);
    }
}