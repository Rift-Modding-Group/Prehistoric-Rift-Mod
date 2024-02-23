package anightdazingzoroark.prift.server.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class RiftSpecialMeatItem extends ItemFood {
    private final PotionEffect eatenEffect;

    public RiftSpecialMeatItem(int amount, float saturation, PotionEffect eatenEffect) {
        super(amount, saturation, true);
        this.eatenEffect = eatenEffect;
    }

    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        if (!worldIn.isRemote) player.addPotionEffect(this.eatenEffect);
    }
}
