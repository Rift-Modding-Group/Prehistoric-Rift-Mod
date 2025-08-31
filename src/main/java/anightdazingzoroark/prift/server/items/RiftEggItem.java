package anightdazingzoroark.prift.server.items;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftEgg;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class RiftEggItem extends Item {
    protected RiftCreatureType creature;

    public RiftEggItem(RiftCreatureType creature) {
        super();
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
        boolean success = this.spawnEgg(world, player, this.creature, offset.getX() + 0.5F, offset.getY() + 0.5F, offset.getZ() + 0.5F);
        if (success && !player.capabilities.isCreativeMode) {
            player.getHeldItem(hand).shrink(1);
        }
        return success ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
    }

    protected boolean spawnEgg(World world, EntityPlayer player, RiftCreatureType creature, double x, double y, double z) {
        if (PlayerTamedCreaturesHelper.canAddToParty(player) || PlayerTamedCreaturesHelper.canAddCreatureToBox(player)) {
            RiftEgg egg = new RiftEgg(world);
            egg.setLocationAndAngles(x, y, z, world.rand.nextFloat() * 360.0F, 0.0F);
            egg.setCreatureType(creature);
            egg.setOwnerId(player.getUniqueID());
            egg.setGrowingAge(0);
            egg.enablePersistence();
            egg.setHatchTime(creature.getHatchTime() * 20);

            if (!world.isRemote) world.spawnEntity(egg);
            return true;
        }
        else {
            player.sendStatusMessage(new TextComponentTranslation("reminder.cannot_hatch_more_eggs"), false);
            return false;
        }
    }
}
