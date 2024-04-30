package anightdazingzoroark.prift.server.items;

import anightdazingzoroark.prift.server.entity.projectile.ThrownBola;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class RiftBola extends Item {
    public RiftBola() {
        this.maxStackSize = 1;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        if (!worldIn.isRemote) {
            EntityPlayer player = (EntityPlayer)entityLiving;
            ItemStack itemstack = player.getHeldItemMainhand();
            ThrownBola bola = new ThrownBola(worldIn, player);
            bola.shoot(player, player.rotationPitch, player.rotationYaw, 0f, 1.6f, 1f);
            worldIn.spawnEntity(bola);

            if (!player.capabilities.isCreativeMode) itemstack.shrink(1);
        }
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }
}
