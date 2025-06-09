package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.server.items.RiftItems;
import net.minecraft.item.Item;

public class RiftProjectileAnimatorRegistry {
    public static Item THROWN_STEGOSAURUS_PLATE_ONE;
    public static Item THROWN_STEGOSAURUS_PLATE_TWO;
    public static Item THROWN_STEGOSAURUS_PLATE_THREE;
    public static Item THROWN_STEGOSAURUS_PLATE_FOUR;
    public static Item CANNONBALL;
    public static Item MORTAR_SHELL;
    public static Item CATAPULT_BOULDER;
    public static Item THROWN_BOLA;
    public static Item DILOPHOSAURUS_SPIT;
    public static Item VENOM_BOMB;

    public static void registerProjectiles() {
        THROWN_STEGOSAURUS_PLATE_ONE = new ThrownStegoPlateAnimator().setRegistryName("thrown_stegosaurus_plate_animator_one").setTranslationKey("thrown_stegosaurus_plate_animator_one");
        THROWN_STEGOSAURUS_PLATE_TWO = new ThrownStegoPlateAnimator().setRegistryName("thrown_stegosaurus_plate_animator_two").setTranslationKey("thrown_stegosaurus_plate_animator_two");
        THROWN_STEGOSAURUS_PLATE_THREE = new ThrownStegoPlateAnimator().setRegistryName("thrown_stegosaurus_plate_animator_three").setTranslationKey("thrown_stegosaurus_plate_animator_three");
        THROWN_STEGOSAURUS_PLATE_FOUR = new ThrownStegoPlateAnimator().setRegistryName("thrown_stegosaurus_plate_animator_four").setTranslationKey("thrown_stegosaurus_plate_animator_four");

        CANNONBALL = new WeaponProjectileAnimator().setRegistryName("cannonball_projectile_animator").setTranslationKey("cannonball_projectile_animator");
        MORTAR_SHELL = new WeaponProjectileAnimator().setRegistryName("mortar_shell_projectile_animator").setTranslationKey("mortar_shell_projectile_animator");
        CATAPULT_BOULDER = new WeaponProjectileAnimator().setRegistryName("catapult_boulder_projectile_animator").setTranslationKey("catapult_boulder_projectile_animator");

        THROWN_BOLA = new ThrownBolaAnimator().setRegistryName("thrown_bola_animator").setTranslationKey("thrown_bola_animator");

        DILOPHOSAURUS_SPIT = new DilophosaurusSpitAnimator().setRegistryName("dilophosaurus_spit_animator").setTranslationKey("dilophosaurus_spit_animator");
        VENOM_BOMB = new VenomBombAnimator().setRegistryName("venom_bomb_animator").setTranslationKey("venom_bomb_animator");

        RiftItems.ITEMS.add(THROWN_STEGOSAURUS_PLATE_ONE);
        RiftItems.ITEMS.add(THROWN_STEGOSAURUS_PLATE_TWO);
        RiftItems.ITEMS.add(THROWN_STEGOSAURUS_PLATE_THREE);
        RiftItems.ITEMS.add(THROWN_STEGOSAURUS_PLATE_FOUR);

        RiftItems.ITEMS.add(CANNONBALL);
        RiftItems.ITEMS.add(MORTAR_SHELL);
        RiftItems.ITEMS.add(CATAPULT_BOULDER);

        RiftItems.ITEMS.add(THROWN_BOLA);

        RiftItems.ITEMS.add(DILOPHOSAURUS_SPIT);
        RiftItems.ITEMS.add(VENOM_BOMB);
    }
}
