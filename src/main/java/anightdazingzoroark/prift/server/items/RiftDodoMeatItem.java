package anightdazingzoroark.prift.server.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class RiftDodoMeatItem extends ItemFood {
    private final boolean isCooked;

    public RiftDodoMeatItem(int amount, float saturation, boolean isCooked, boolean isWolfFood) {
        super(amount, saturation, isWolfFood);
        this.isCooked = isCooked;
    }

    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        if (!worldIn.isRemote) {
            if (isCooked) player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 600, 4));
            else player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 200));
        }
    }
}
