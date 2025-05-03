package anightdazingzoroark.prift.client.renderer;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.blocks.RiftBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraftforge.client.model.ModelLoader;

public class FluidRenderer {
    public static void registerRenderers() {
        ModelLoader.setCustomStateMapper(RiftBlocks.PYROBERRY_JUICE_FLUID, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation(RiftInitialize.MODID+":fluid_block", "pyroberry_juice");
            }
        });

        ModelLoader.setCustomStateMapper(RiftBlocks.CRYOBERRY_JUICE_FLUID, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation(RiftInitialize.MODID+":fluid_block", "cryoberry_juice");
            }
        });
    }
}