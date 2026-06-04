package anightdazingzoroark.prift.client;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.item.RiftItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//icons are bones until i add back the tribute items
public class RiftCreativeTabs {
    public static final CreativeTabs creativeItemsTab = new CreativeTabs(RiftInitialize.MODID) {
        @Override
        @SideOnly(Side.CLIENT)
        public ItemStack createIcon() {
            //return new ItemStack(RiftItems.TYRANNOSAURUS_ARM);
            return new ItemStack(Items.BONE);
        }

        @Override
        public String getTranslationKey() {
            return "itemGroup.prift_items";
        }
    };

    public static final CreativeTabs spawnEggsTab = new CreativeTabs(RiftInitialize.MODID) {
        @Override
        @SideOnly(Side.CLIENT)
        public ItemStack createIcon() {
            //return new ItemStack(RiftItems.TYRANNOSAURUS_ARM);
            return new ItemStack(Items.BONE);
        }

        @Override
        public String getTranslationKey() {
            return "itemGroup.prift_spawn_eggs";
        }
    };
}