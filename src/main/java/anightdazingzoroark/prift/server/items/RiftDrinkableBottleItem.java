package anightdazingzoroark.prift.server.items;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.world.World;

public class RiftDrinkableBottleItem extends ItemFood {
    public RiftDrinkableBottleItem() {
        super(2, 0.4f, false);
        this.setMaxStackSize(1);
    }

    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)entityLiving;
            player.getFoodStats().addStats(this, stack);
            player.addStat(StatList.getObjectUseStats(this));
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
                if (stack.isEmpty()) return new ItemStack(Items.GLASS_BOTTLE);
            }
        }
        return stack;
    }

    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.DRINK;
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return new ItemStack(Items.GLASS_BOTTLE);
    }
}
