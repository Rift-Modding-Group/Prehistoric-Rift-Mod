package anightdazingzoroark.prift.client.rendering.block;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.server.block.RiftBlocks;
import anightdazingzoroark.prift.server.block.SmokenutBush;
import anightdazingzoroark.riftlib.block.AnimatedBlockStateHolder;
import anightdazingzoroark.riftlib.core.manager.AnimationDataBlock;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRenderer;
import net.minecraft.block.BlockCrops;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class SmokenutBushBlockRenderer extends GeoBlockRenderer<AnimatedBlockStateHolder> {
    public SmokenutBushBlockRenderer() {
        super(
                new AnimatedGeoModel<AnimatedBlockStateHolder>() {
                    @Override
                    @NotNull
                    public String getModId() {
                        return RiftInitialize.MODID;
                    }

                    @Override
                    @NotNull
                    public String getModelIdentifier(AnimatedBlockStateHolder animatedBlockStateHolder) {
                        return "geometry.smokenut_bush";
                    }

                    @Override
                    @NotNull
                    public String getTextureLocation(AnimatedBlockStateHolder holder) {
                        return holder.getBlockState().getValue(SmokenutBush.HAS_SMOKENUTS)
                                ? "blocks/smokenut_bush.png" : "blocks/smokenut_bush_immature.png";
                    }
                },
                (world, blockPos, iBlockState) -> {
                    return new AnimatedBlockStateHolder(world, blockPos, iBlockState) {
                        @Override
                        public void initializeAnimationData(@NonNull AnimationDataBlock animationDataBlock) {
                            animationDataBlock.setScale(holder -> {
                                int age = holder.getBlockState().getValue(BlockCrops.AGE);
                                return MathUtil.slopeResult(age, true, 0, RiftBlocks.SMOKENUT_BUSH.getMaxAge(), 0.25f, 1f);
                            });
                        }
                    };
                }
        );
    }
}
