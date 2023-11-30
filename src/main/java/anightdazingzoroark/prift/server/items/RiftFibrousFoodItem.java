package anightdazingzoroark.prift.server.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class RiftFibrousFoodItem extends ItemFood {
    public RiftFibrousFoodItem(int amount, boolean isWolfFood) {
        super(amount, 0.45f, isWolfFood);
    }

    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        if (!worldIn.isRemote) player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 300, 1));
    }
}
