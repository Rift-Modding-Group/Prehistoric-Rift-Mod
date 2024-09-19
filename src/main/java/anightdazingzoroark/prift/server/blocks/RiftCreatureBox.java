package anightdazingzoroark.prift.server.blocks;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.ServerProxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RiftCreatureBox extends Block {
    public RiftCreatureBox() {
        super(Material.ROCK);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            System.out.println("hellooo");
            playerIn.openGui(RiftInitialize.instance, ServerProxy.GUI_CREATURE_BOX, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }
}
