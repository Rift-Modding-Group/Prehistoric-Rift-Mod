package anightdazingzoroark.rift.client.creativetab;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.items.RiftItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RiftCreativeTabs {
    public static final CreativeTabs creativeItemsTab = new CreativeTabs(RiftInitialize.MODID) {
        @Override
        @SideOnly(Side.CLIENT)
        public ItemStack createIcon() {
            return new ItemStack(RiftItems.TYRANNOSAURUS_ARM);
        }

        @Override
        public String getTranslationKey() {
            return "itemGroup.rift_items";
        }
    };
}
