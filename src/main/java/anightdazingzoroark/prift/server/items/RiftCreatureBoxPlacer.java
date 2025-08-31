package anightdazingzoroark.prift.server.items;

import anightdazingzoroark.prift.helper.ChunkPosWithVerticality;
import anightdazingzoroark.prift.server.blocks.RiftBlocks;
import anightdazingzoroark.prift.server.capabilities.creatureBoxData.CreatureBoxDataHelper;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class RiftCreatureBoxPlacer extends ItemBlock {
    public RiftCreatureBoxPlacer() {
        super(RiftBlocks.CREATURE_BOX);
    }

    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        //check for placeability based on whether or not resulting range will be within the
        //range of an already existing one
        List<ChunkPosWithVerticality> potentialChunkPositions = this.getPotentialChunkPositions(pos);

        //get already existing positions
        List<BlockPos> creatureBoxPositions = CreatureBoxDataHelper.getCreatureBoxData(world).getCreatureBoxPositions();

        //check for overlap in chunk positions
        boolean noOverlap = true;
        for (BlockPos creatureBoxPos : creatureBoxPositions) {
            TileEntity te = world.getTileEntity(creatureBoxPos);
            if (!(te instanceof RiftTileEntityCreatureBox)) continue;
            RiftTileEntityCreatureBox teCreatureBox = (RiftTileEntityCreatureBox) te;
            List<ChunkPosWithVerticality> creatureBoxChunks = teCreatureBox.chunksWithinDeploymentRange();

            noOverlap = !this.chunkBordersOverlap(potentialChunkPositions, creatureBoxChunks);
        }

        if (!noOverlap) {
            player.sendStatusMessage(new TextComponentTranslation("reminder.too_close_to_other_creature_box"), false);
            return false;
        }

        //continue as usual
        return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
    }

    private boolean chunkBordersOverlap(List<ChunkPosWithVerticality> chunkListOne, List<ChunkPosWithVerticality> chunkListTwo) {
        ChunkPosWithVerticality chunkListOneCornerOne = chunkListOne.get(0);
        ChunkPosWithVerticality chunkListOneCornerTwo = chunkListOne.get(chunkListOne.size() - 1);

        ChunkPosWithVerticality chunkListTwoCornerOne = chunkListTwo.get(0);
        ChunkPosWithVerticality chunkListTwoCornerTwo = chunkListTwo.get(chunkListTwo.size() - 1);

        int chunkListOneXMin = Math.min(chunkListOneCornerOne.x, chunkListOneCornerTwo.x);
        int chunkListOneXMax = Math.max(chunkListOneCornerOne.x, chunkListOneCornerTwo.x);
        int chunkListOneYMin = Math.min(chunkListOneCornerOne.y, chunkListOneCornerTwo.y);
        int chunkListOneYMax = Math.max(chunkListOneCornerOne.y, chunkListOneCornerTwo.y);
        int chunkListOneZMin = Math.min(chunkListOneCornerOne.z, chunkListOneCornerTwo.z);
        int chunkListOneZMax = Math.max(chunkListOneCornerOne.z, chunkListOneCornerTwo.z);

        int chunkListTwoXMin = Math.min(chunkListTwoCornerOne.x, chunkListTwoCornerTwo.x);
        int chunkListTwoXMax = Math.max(chunkListTwoCornerOne.x, chunkListTwoCornerTwo.x);
        int chunkListTwoYMin = Math.min(chunkListTwoCornerOne.y, chunkListTwoCornerTwo.y);
        int chunkListTwoYMax = Math.max(chunkListTwoCornerOne.y, chunkListTwoCornerTwo.y);
        int chunkListTwoZMin = Math.min(chunkListTwoCornerOne.z, chunkListTwoCornerTwo.z);
        int chunkListTwoZMax = Math.max(chunkListTwoCornerOne.z, chunkListTwoCornerTwo.z);

        boolean x = Math.max(chunkListOneXMin, chunkListTwoXMin) <= Math.min(chunkListOneXMax, chunkListTwoXMax);
        boolean y = Math.max(chunkListOneYMin, chunkListTwoYMin) <= Math.min(chunkListOneYMax, chunkListTwoYMax);
        boolean z = Math.max(chunkListOneZMin, chunkListTwoZMin) <= Math.min(chunkListOneZMax, chunkListTwoZMax);

        return x && y && z;
    }

    //this is for the initial list of potential chunk positions after the box might be placed
    //deploymentRange is 1 because thats the initial value
    private List<ChunkPosWithVerticality> getPotentialChunkPositions(BlockPos pos) {
        List<ChunkPosWithVerticality> toReturn = new ArrayList<>();
        int chunkX = pos.getX() >> 4;
        int chunkY = pos.getY() >> 4;
        int chunkZ = pos.getZ() >> 4;
        int deploymentRange = 1;

        for (int x = -deploymentRange; x <= deploymentRange; x++) {
            for (int y = -deploymentRange; y <= deploymentRange; y++) {
                for (int z = -deploymentRange; z <= deploymentRange; z++) {
                    toReturn.add(new ChunkPosWithVerticality(chunkX + x, chunkY + y, chunkZ + z));
                }
            }
        }

        return toReturn;
    }
}
