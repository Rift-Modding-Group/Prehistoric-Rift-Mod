package anightdazingzoroark.prift.server.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class PlatformFloorBlock extends Block {
    public PlatformFloorBlock() {
        super(Material.BARRIER);
        this.setBlockUnbreakable();
        this.setResistance(6000000f);
        this.setLightOpacity(0);
        this.disableStats();
    }

    @Override
    @NotNull
    public EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean removedByPlayer(
            @NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos, @NotNull EntityPlayer player, boolean willHarvest
    ) {
        return false;
    }

    @Override
    public boolean canEntityDestroy(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull Entity entity) {
        return false;
    }

    @Override
    public int quantityDropped(@NotNull java.util.Random random) {
        return 0;
    }

    @Override
    @NotNull
    public EnumPushReaction getPushReaction(@NotNull IBlockState state) {
        return EnumPushReaction.BLOCK;
    }
}
