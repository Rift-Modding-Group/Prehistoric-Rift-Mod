package anightdazingzoroark.prift.server.items;

import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftEgg;
import anightdazingzoroark.prift.server.entity.RiftSac;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RiftSacItem extends Item {
    protected RiftCreatureType creature;

    public RiftSacItem(RiftCreatureType creature) {
        this.creature = creature;
        this.setMaxDamage(0);
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
        boolean success = this.spawnSac(world, player, this.creature, offset.getX() + 0.5F, offset.getY() + 0.5F, offset.getZ() + 0.5F);
        if (success && !player.capabilities.isCreativeMode) {
            player.getHeldItem(hand).shrink(1);
        }
        return success ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
    }

    protected boolean spawnSac(World world, EntityPlayer player, RiftCreatureType creature, double x, double y, double z) {
        RiftSac sac = new RiftSac(world);
        sac.setLocationAndAngles(x, y, z, world.rand.nextFloat() * 360.0F, 0.0F);
        sac.setCreatureType(creature);
        sac.setOwnerId(player.getUniqueID());
        sac.setGrowingAge(0);
        sac.enablePersistence();
        sac.setHatchTime(creature.getHatchTime() * 20);

        if (!world.isRemote) {
            if (world.getBlockState(new BlockPos(x, y, z)).getMaterial() == Material.WATER) world.spawnEntity(sac);
        }
        return true;
    }
}
