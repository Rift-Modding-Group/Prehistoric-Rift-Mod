package anightdazingzoroark.prift.server.items;


import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Constructor;
import java.util.function.Function;

public class RiftLargeWeaponItem extends Item {
    private final RiftLargeWeaponType weaponType;

    public RiftLargeWeaponItem(RiftLargeWeaponType weaponType) {
        this.weaponType = weaponType;
        this.maxStackSize = 1;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        BlockPos blockpos1 = pos.up();
        boolean flag1 = !world.isAirBlock(blockpos1) && !world.getBlockState(blockpos1).getBlock().isReplaceable(world, blockpos1);
        if (flag1) {
            return EnumActionResult.FAIL;
        }
        BlockPos offset = pos.offset(facing);
        boolean success = this.spawnItem(world, this.weaponType, offset.getX() + 0.5F, offset.getY() + 0.5F, offset.getZ() + 0.5F);
        if (success && !player.capabilities.isCreativeMode) {
            player.getHeldItem(hand).shrink(1);
        }
        return success ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
    }

    private boolean spawnItem(World world, RiftLargeWeaponType largeWeaponType, double x, double y, double z) {
        Function<World, RiftLargeWeapon> constructor = largeWeaponType.getConstructor();
        RiftLargeWeapon weapon = constructor.apply(world);
        weapon.setPositionAndRotation(x, y, z, world.rand.nextFloat() * 360.0F, 0.0F);
        if (!world.isRemote) world.spawnEntity(weapon);
        return weapon != null;
    }

    public RiftLargeWeaponType getWeaponType() {
        return this.weaponType;
    }
}
