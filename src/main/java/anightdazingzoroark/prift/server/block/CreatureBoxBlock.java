package anightdazingzoroark.prift.server.block;

import anightdazingzoroark.prift.client.rendering.block.CreatureBoxRangeRenderer;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxGuiFactory;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxLocations;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreatureBoxBlock extends Block implements ITileEntityProvider {
    public CreatureBoxBlock() {
        super(Material.ROCK);
        this.setHardness(7.5f);
    }

    @Override
    @Nullable
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new CreatureBoxTileEntity();
    }

    @Override
    public boolean onBlockActivated(
            @NotNull World world, @NotNull BlockPos position, @NotNull IBlockState state,
            @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing,
            float hitX, float hitY, float hitZ
    ) {
        if (player.isSneaking()) {
            if (world.isRemote && hand == EnumHand.MAIN_HAND) {
                CreatureBoxRangeRenderer.toggle(position);
            }
            return true;
        }
        if (!world.isRemote) CreatureBoxGuiFactory.INSTANCE.open(player, position, 0);
        return true;
    }

    @Override
    public boolean canPlaceBlockAt(@NotNull World world, @NotNull BlockPos position) {
        return super.canPlaceBlockAt(world, position) && (world.isRemote || !CreatureBoxLocations.overlaps(world, position));
    }

    @Override
    public void onBlockAdded(@NotNull World world, @NotNull BlockPos position, @NotNull IBlockState state) {
        super.onBlockAdded(world, position, state);
        if (!world.isRemote) CreatureBoxLocations.get(world).setPresent(position, true);
    }

    @Override
    public void breakBlock(@NotNull World world, @NotNull BlockPos position, @NotNull IBlockState state) {
        if (!world.isRemote) CreatureBoxLocations.get(world).setPresent(position, false);
        super.breakBlock(world, position, state);
    }

    public static boolean isOccupied(@NotNull IBlockAccess world, @NotNull BlockPos position) {
        return world.getTileEntity(position) instanceof CreatureBoxTileEntity tile && tile.hasCreatures();
    }

    @Override
    public float getBlockHardness(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos position) {
        return isOccupied(world, position) ? -1f : super.getBlockHardness(state, world, position);
    }

    @Override
    public boolean removedByPlayer(
            @NotNull IBlockState state, @NotNull World world, @NotNull BlockPos position,
            @NotNull EntityPlayer player, boolean willHarvest
    ) {
        if (isOccupied(world, position)) return false;
        return super.removedByPlayer(state, world, position, player, willHarvest);
    }

    @Override
    public boolean canEntityDestroy(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos position, @NotNull Entity entity) {
        return !isOccupied(world, position) && super.canEntityDestroy(state, world, position, entity);
    }

    @Override
    public float getExplosionResistance(@NotNull World world, @NotNull BlockPos position, @Nullable Entity entity, @NotNull Explosion explosion) {
        return isOccupied(world, position) ? Float.MAX_VALUE : super.getExplosionResistance(world, position, entity, explosion);
    }

    @Override
    public void onBlockExploded(@NotNull World world, @NotNull BlockPos position, @NotNull Explosion explosion) {
        if (!isOccupied(world, position)) super.onBlockExploded(world, position, explosion);
    }

    @Override
    @NotNull
    public EnumPushReaction getPushReaction(@NotNull IBlockState state) { return EnumPushReaction.BLOCK; }
}
