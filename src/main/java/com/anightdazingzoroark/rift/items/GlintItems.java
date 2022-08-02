package com.anightdazingzoroark.rift.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GlintItems extends Item {
    public GlintItems(Item.Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
