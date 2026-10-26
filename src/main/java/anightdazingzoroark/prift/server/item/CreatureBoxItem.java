package anightdazingzoroark.prift.server.item;

import anightdazingzoroark.prift.server.block.CreatureBoxBlock;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxLocations;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class CreatureBoxItem extends ItemBlock {
    public CreatureBoxItem(@NotNull CreatureBoxBlock block) {
        super(block);
    }

    @Override
    @NotNull
    public EnumActionResult onItemUse(
            @NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos position,
            @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ
    ) {
        EnumActionResult result = super.onItemUse(player, world, position, hand, facing, hitX, hitY, hitZ);
        if (world.isRemote || result != EnumActionResult.FAIL) return result;

        BlockPos placement = position;
        if (!world.getBlockState(position).getBlock().isReplaceable(world, position)) placement = position.offset(facing);
        if (CreatureBoxLocations.overlaps(world, placement)) {
            player.sendStatusMessage(new TextComponentTranslation("creature_box.warning.overlap"), false);
        }
        return result;
    }
}
