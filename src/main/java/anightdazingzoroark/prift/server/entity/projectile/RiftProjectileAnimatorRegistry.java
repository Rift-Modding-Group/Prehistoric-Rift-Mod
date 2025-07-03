package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.server.items.RiftItems;
import net.minecraft.item.Item;

public class RiftProjectileAnimatorRegistry {
    public static Item CANNONBALL;
    public static Item MORTAR_SHELL;
    public static Item CATAPULT_BOULDER;
    public static Item VENOM_BOMB;

    public static void registerProjectiles() {
        CANNONBALL = new WeaponProjectileAnimator().setRegistryName("cannonball_projectile_animator").setTranslationKey("cannonball_projectile_animator");
        MORTAR_SHELL = new WeaponProjectileAnimator().setRegistryName("mortar_shell_projectile_animator").setTranslationKey("mortar_shell_projectile_animator");
        CATAPULT_BOULDER = new WeaponProjectileAnimator().setRegistryName("catapult_boulder_projectile_animator").setTranslationKey("catapult_boulder_projectile_animator");

        VENOM_BOMB = new VenomBombAnimator().setRegistryName("venom_bomb_animator").setTranslationKey("venom_bomb_animator");

        RiftItems.ITEMS.add(CANNONBALL);
        RiftItems.ITEMS.add(MORTAR_SHELL);
        RiftItems.ITEMS.add(CATAPULT_BOULDER);

        RiftItems.ITEMS.add(VENOM_BOMB);
    }
}
