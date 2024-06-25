package anightdazingzoroark.prift.client.renderer;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.blocks.RiftBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;

public class FluidRenderer {
    public static void registerRenderers() {
        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(RiftBlocks.PYROBERRY_JUICE_FLUID), new ItemMeshDefinition() {
            @Override
            public ModelResourceLocation getModelLocation(ItemStack stack) {
                return new ModelResourceLocation(RiftInitialize.MODID+":pyroberry_juice", "fluid");
            }
        });
        ModelLoader.setCustomStateMapper(RiftBlocks.PYROBERRY_JUICE_FLUID, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation(RiftInitialize.MODID+":pyroberry_juice", "fluid");
            }
        });

        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(RiftBlocks.CRYOBERRY_JUICE_FLUID), new ItemMeshDefinition() {
            @Override
            public ModelResourceLocation getModelLocation(ItemStack stack) {
                return new ModelResourceLocation(RiftInitialize.MODID+":cryoberry_juice", "fluid");
            }
        });
        ModelLoader.setCustomStateMapper(RiftBlocks.CRYOBERRY_JUICE_FLUID, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation(RiftInitialize.MODID+":cryoberry_juice", "fluid");
            }
        });
    }
}
