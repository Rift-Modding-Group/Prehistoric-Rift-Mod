package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.server.items.RiftItems;
import net.minecraft.item.Item;

public class RiftProjectileAnimatorRegistry {
    public static Item VENOM_BOMB;

    public static void registerProjectiles() {
        VENOM_BOMB = new VenomBombAnimator().setRegistryName("venom_bomb_animator").setTranslationKey("venom_bomb_animator");

        RiftItems.ITEMS.add(VENOM_BOMB);
    }
}
