package com.anightdazingzoroark.rift.server.blocks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import static com.anightdazingzoroark.rift.RiftInitialize.MODID;

public class RiftBlockRegistry {
    public static class Tags {
        public static final TagKey<Block> WOOD_AND_WEAKER = BlockTags.create(new ResourceLocation(MODID,"wood_and_weaker"));
    }
}
