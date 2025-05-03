package anightdazingzoroark.prift.client.creativetab;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.items.RiftItems;
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
            return "itemGroup.prift_items";
        }
    };
}
