package anightdazingzoroark.prift.server.item;

import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.server.entity.projectile.RiftProjectile;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * helper for throwable items
 * */
public abstract class RiftThrowableItem extends Item {
    public RiftThrowableItem() {
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, new BehaviorProjectileDispense() {
            @Override
            @NotNull
            protected RiftProjectile getProjectileEntity(@NotNull World world, @NotNull IPosition position, @NotNull ItemStack stack) {
                return new RiftProjectile(world, position.getX(), position.getY(), position.getZ(), RiftThrowableItem.this.getProjectileBuilder());
            }
        });
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @NotNull
    public abstract ProjectileBuilder getProjectileBuilder();

    @NotNull
    public Function<EntityPlayer, Double> getDamageByPlayer() {
        return player -> 0D;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        EntityPlayer player = (EntityPlayer) entityLiving;
        RiftProjectile projectile = new RiftProjectile(player, this.getProjectileBuilder(), this.getDamageByPlayer());
        projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0f, 1.6f, 0f);
        if (!worldIn.isRemote) worldIn.spawnEntity(projectile);
        if (!player.isCreative()) stack.shrink(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }
}
